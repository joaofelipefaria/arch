package br.com.joaofelipefaria.arch.patterns.gof.structural;

import java.util.ArrayList;
import java.util.List;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Composite</b> (Structural).
 * <p>
 * <b>Intent:</b> compose objects into tree structures to represent
 * part-whole hierarchies. Composite lets clients treat individual objects
 * and compositions of objects uniformly.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Component} ({@link CatalogNode}) - declares the interface
 *         common to both leaves and composites (here, just {@code totalPrice()}).</li>
 *     <li>{@code Leaf} ({@link ProductNode}) - represents a leaf object (no children).</li>
 *     <li>{@code Composite} ({@link CategoryNode}) - stores child components
 *         and implements child-related operations, delegating to them.</li>
 *     <li>{@code Client} ({@link #main}) - manipulates every object in the
 *         hierarchy through the Component interface, without caring whether
 *         it's a single product or a whole category.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to represent a part-whole hierarchy (a product
 * catalog made of categories, which can themselves contain other categories
 * and individual products) and want client code to treat a single leaf and
 * an entire subtree the exact same way.
 */
public class GofCompositePatternSample {

    /** Component: the common operation both leaves and composites support. */
    interface CatalogNode {
        double totalPrice();
        void print(String indent);
    }

    /** Leaf: a single product, with no children. */
    static class ProductNode implements CatalogNode {
        private final ProductDTO product;

        ProductNode(ProductDTO product) {
            this.product = product;
        }

        @Override
        public double totalPrice() {
            return product.price();
        }

        @Override
        public void print(String indent) {
            System.out.println(indent + "- " + product.name() + " ($" + product.price() + ")");
        }
    }

    /** Composite: a category that can contain products AND other categories. */
    static class CategoryNode implements CatalogNode {
        private final String name;
        private final List<CatalogNode> children = new ArrayList<>();

        CategoryNode(String name) {
            this.name = name;
        }

        CategoryNode add(CatalogNode child) {
            children.add(child);
            return this;
        }

        @Override
        public double totalPrice() {
            return children.stream().mapToDouble(CatalogNode::totalPrice).sum();
        }

        @Override
        public void print(String indent) {
            System.out.println(indent + "+ " + name + " (subtotal: $" + totalPrice() + ")");
            for (CatalogNode child : children) {
                child.print(indent + "  ");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Composite Pattern ===");

        CategoryNode electronics = new CategoryNode("Electronics")
                .add(new ProductNode(new ProductDTO("E-1", "Headphones", 89.90)))
                .add(new ProductNode(new ProductDTO("E-2", "USB-C Charger", 19.90)));

        CategoryNode accessories = new CategoryNode("Accessories")
                .add(new ProductNode(new ProductDTO("A-1", "Phone Case", 14.90)));

        CategoryNode wholeCatalog = new CategoryNode("Catalog")
                .add(electronics)
                .add(accessories)
                .add(new ProductNode(new ProductDTO("M-1", "Gift Card", 50.0)));

        wholeCatalog.print("");
        System.out.println("Grand total: $" + wholeCatalog.totalPrice());
    }
}
