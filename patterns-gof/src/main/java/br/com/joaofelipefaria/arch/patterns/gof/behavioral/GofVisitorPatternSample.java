package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import java.util.List;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Visitor</b> (Behavioral).
 * <p>
 * <b>Intent:</b> represent an operation to be performed on the elements of
 * an object structure. Visitor lets you define a new operation without
 * changing the classes of the elements on which it operates.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Visitor} ({@link CatalogVisitor}) - declares a visit
 *         operation for each ConcreteElement class in the object structure.</li>
 *     <li>{@code ConcreteVisitor} ({@link TotalPriceVisitor}, {@link ShippingLabelVisitor}) -
 *         implements each operation declared by Visitor - each concrete
 *         visitor represents a whole new operation over the structure.</li>
 *     <li>{@code Element} ({@link CatalogElement}) - defines an {@code accept()}
 *         operation that takes a visitor as an argument.</li>
 *     <li>{@code ConcreteElement} ({@link PhysicalProductElement}, {@link DigitalProductElement}) -
 *         implements {@code accept()} by calling back the visitor's matching
 *         visit method (this is the "double dispatch" trick the pattern relies on).</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to perform several unrelated operations
 * (compute totals, generate shipping labels, ...) across a fixed hierarchy
 * of element types, and want to add a whole new operation without touching
 * every element class - here, physical products need shipping labels while
 * digital ones don't, and both element types just call back into whichever
 * visitor's logic is running.
 */
public class GofVisitorPatternSample {

    /** Element: accepts a visitor and double-dispatches to the correct visit method. */
    interface CatalogElement {
        void accept(CatalogVisitor visitor);
    }

    /** Visitor: one visit method per concrete element type in the structure. */
    interface CatalogVisitor {
        void visit(PhysicalProductElement element);
        void visit(DigitalProductElement element);
    }

    /** ConcreteElement #1: a product that needs shipping. */
    static class PhysicalProductElement implements CatalogElement {
        final ProductDTO product;
        final double weightKg;

        PhysicalProductElement(ProductDTO product, double weightKg) {
            this.product = product;
            this.weightKg = weightKg;
        }

        @Override
        public void accept(CatalogVisitor visitor) {
            visitor.visit(this);
        }
    }

    /** ConcreteElement #2: a product with no physical shipping concerns. */
    static class DigitalProductElement implements CatalogElement {
        final ProductDTO product;

        DigitalProductElement(ProductDTO product) {
            this.product = product;
        }

        @Override
        public void accept(CatalogVisitor visitor) {
            visitor.visit(this);
        }
    }

    /** ConcreteVisitor #1: an operation that sums up prices - identical logic for every element type here. */
    static class TotalPriceVisitor implements CatalogVisitor {
        private double total = 0;

        @Override
        public void visit(PhysicalProductElement element) {
            total += element.product.price();
        }

        @Override
        public void visit(DigitalProductElement element) {
            total += element.product.price();
        }

        double total() {
            return total;
        }
    }

    /** ConcreteVisitor #2: an operation that only makes sense for SOME element types. */
    static class ShippingLabelVisitor implements CatalogVisitor {
        @Override
        public void visit(PhysicalProductElement element) {
            System.out.println("Shipping label -> " + element.product.name() + " (" + element.weightKg + "kg)");
        }

        @Override
        public void visit(DigitalProductElement element) {
            System.out.println("No shipping label needed for digital product: " + element.product.name());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Visitor Pattern ===");

        List<CatalogElement> catalog = List.of(
                new PhysicalProductElement(new ProductDTO("P-1", "Coffee Mug", 12.90), 0.4),
                new DigitalProductElement(new ProductDTO("P-2", "E-book: Design Patterns", 29.90))
        );

        TotalPriceVisitor totalPriceVisitor = new TotalPriceVisitor();
        ShippingLabelVisitor shippingLabelVisitor = new ShippingLabelVisitor();

        for (CatalogElement element : catalog) {
            element.accept(totalPriceVisitor);
            element.accept(shippingLabelVisitor);
        }

        System.out.println("Total catalog price: $" + totalPriceVisitor.total());
    }
}
