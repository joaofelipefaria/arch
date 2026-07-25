package br.com.joaofelipefaria.arch.patterns.gof.creational;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Abstract Factory</b> (Creational).
 * <p>
 * <b>Intent:</b> provide an interface for creating families of related or
 * dependent objects without specifying their concrete classes.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code AbstractFactory} ({@link CatalogFactory}) - declares an
 *         interface for operations that create each kind of product in the family.</li>
 *     <li>{@code ConcreteFactory} ({@link StandardCatalogFactory}, {@link PremiumCatalogFactory}) -
 *         implements the creation operations for one specific family/variant.</li>
 *     <li>{@code AbstractProduct} ({@link Label}) - declares an interface for a
 *         kind of product object.</li>
 *     <li>{@code ConcreteProduct} ({@link StandardLabel}, {@link PremiumLabel}) -
 *         a specific product produced by the corresponding concrete factory.</li>
 *     <li>{@code Client} ({@link #main}) - uses only the AbstractFactory/AbstractProduct
 *         interfaces, never a concrete class directly.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> a system must stay independent of how its products are
 * created/composed, must be configured with one of several families of
 * products, and you want to enforce that products from one family are
 * always used together (e.g. never mix a "standard" product with a
 * "premium" label).
 */
public class GofAbstractFactoryPatternSample {

    /** AbstractFactory: creates a whole family of related catalog objects. */
    interface CatalogFactory {
        ProductDTO createProduct(String id, String name, double price);
        Label createLabel(ProductDTO product);
    }

    /** AbstractProduct: a label rendered for a product, produced by the same family. */
    interface Label {
        String render();
    }

    /** ConcreteFactory #1: the "standard" product family. */
    static class StandardCatalogFactory implements CatalogFactory {
        @Override
        public ProductDTO createProduct(String id, String name, double price) {
            return new ProductDTO(id, name, price);
        }

        @Override
        public Label createLabel(ProductDTO product) {
            return new StandardLabel(product);
        }
    }

    /** ConcreteFactory #2: the "premium" product family. */
    static class PremiumCatalogFactory implements CatalogFactory {
        @Override
        public ProductDTO createProduct(String id, String name, double price) {
            // premium family always adds a 20% markup at creation time
            return new ProductDTO(id, name, price * 1.20);
        }

        @Override
        public Label createLabel(ProductDTO product) {
            return new PremiumLabel(product);
        }
    }

    static class StandardLabel implements Label {
        private final ProductDTO product;

        StandardLabel(ProductDTO product) {
            this.product = product;
        }

        @Override
        public String render() {
            return "%s - $%.2f".formatted(product.name(), product.price());
        }
    }

    static class PremiumLabel implements Label {
        private final ProductDTO product;

        PremiumLabel(ProductDTO product) {
            this.product = product;
        }

        @Override
        public String render() {
            return "*** %s *** $%.2f (premium)".formatted(product.name().toUpperCase(), product.price());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Abstract Factory Pattern ===");

        for (CatalogFactory factory : new CatalogFactory[] {
                new StandardCatalogFactory(), new PremiumCatalogFactory() }) {

            ProductDTO product = factory.createProduct("P-1", "Wireless Mouse", 25.0);
            Label label = factory.createLabel(product);

            System.out.println(factory.getClass().getSimpleName() + " -> " + label.render());
        }
    }
}
