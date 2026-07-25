package br.com.joaofelipefaria.arch.patterns.gof.creational;

import java.util.ArrayList;
import java.util.List;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Prototype</b> (Creational).
 * <p>
 * <b>Intent:</b> specify the kinds of objects to create using a prototypical
 * instance, and create new objects by copying (cloning) this prototype
 * instead of building them from scratch.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Prototype} ({@link CatalogEntry}) - declares an interface
 *         for cloning itself.</li>
 *     <li>{@code ConcretePrototype} ({@link CatalogEntry} itself, since the
 *         example needs only one shape of prototype) - implements the
 *         cloning operation.</li>
 *     <li>{@code Client} ({@link #main}) - creates new objects by asking a
 *         prototype to clone itself, instead of calling {@code new} with a
 *         concrete class name.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> creating an instance from scratch is expensive (e.g.
 * database lookups to assemble a fully-configured object) or when you want
 * to produce many near-identical objects that only differ in a couple of
 * fields - here, a fully-configured {@link CatalogEntry} template is cloned
 * and only the SKU-specific fields are tweaked per copy.
 */
public class GofPrototypePatternSample {

    /** Prototype: any catalog entry that knows how to clone itself. */
    interface Prototype<T> {
        T copy();
    }

    /** ConcretePrototype: a fully-configured catalog entry, cheap to clone, expensive to build from scratch. */
    static class CatalogEntry implements Prototype<CatalogEntry> {
        private ProductDTO product;
        private final List<String> tags;

        CatalogEntry(ProductDTO product, List<String> tags) {
            this.product = product;
            this.tags = new ArrayList<>(tags); // defensive copy so clones don't share mutable state
        }

        void setProduct(ProductDTO product) {
            this.product = product;
        }

        void addTag(String tag) {
            this.tags.add(tag);
        }

        @Override
        public CatalogEntry copy() {
            // deep-enough copy: new tag list, so mutating the clone never affects the original
            return new CatalogEntry(product, tags);
        }

        @Override
        public String toString() {
            return "CatalogEntry{product=%s, tags=%s}".formatted(product, tags);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Prototype Pattern ===");

        CatalogEntry template = new CatalogEntry(
                new ProductDTO("TPL-0", "Base T-Shirt", 19.90),
                List.of("apparel", "unisex"));

        CatalogEntry red = template.copy();
        red.setProduct(new ProductDTO("TS-RED", "T-Shirt (Red)", 19.90));
        red.addTag("red");

        CatalogEntry blue = template.copy();
        blue.setProduct(new ProductDTO("TS-BLUE", "T-Shirt (Blue)", 19.90));
        blue.addTag("blue");

        System.out.println("template -> " + template);
        System.out.println("red      -> " + red);
        System.out.println("blue     -> " + blue);
    }
}
