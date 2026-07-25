package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import java.util.NoSuchElementException;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Iterator</b> (Behavioral).
 * <p>
 * <b>Intent:</b> provide a way to access the elements of an aggregate object
 * sequentially without exposing its underlying representation.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Iterator} ({@link CatalogIterator}) - defines an interface
 *         for accessing and traversing elements.</li>
 *     <li>{@code ConcreteIterator} ({@link InStockOnlyIterator}) - implements
 *         the Iterator interface and keeps track of the current traversal
 *         position, applying its own custom traversal rule (skip out-of-stock items).</li>
 *     <li>{@code Aggregate} ({@link Catalog}) - defines an interface for
 *         creating an Iterator object.</li>
 *     <li>{@code ConcreteAggregate} (also {@link Catalog}) - implements the
 *         Iterator creation interface, returning an instance of the
 *         appropriate ConcreteIterator.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to traverse a collection in a specific way
 * (here, silently skipping out-of-stock products) without exposing the
 * collection's internal storage (array, list, tree, ...) to the code doing
 * the traversal - this hand-rolled version doesn't use {@code java.util.Iterator}
 * on purpose, to show the pattern's own shape explicitly.
 */
public class GofIteratorPatternSample {

    /** Iterator: the traversal interface. */
    interface CatalogIterator {
        boolean hasNext();
        ProductDTO next();
    }

    /** Aggregate: declares how to obtain an iterator over its elements. */
    interface Catalog {
        CatalogIterator createIterator();
    }

    /** ConcreteAggregate: a simple fixed-size catalog, some items in stock, some not. */
    static class InMemoryCatalog implements Catalog {
        private final ProductDTO[] products;
        private final boolean[] inStock;

        InMemoryCatalog(ProductDTO[] products, boolean[] inStock) {
            this.products = products;
            this.inStock = inStock;
        }

        @Override
        public CatalogIterator createIterator() {
            return new InStockOnlyIterator(this);
        }
    }

    /** ConcreteIterator: walks the catalog, silently skipping out-of-stock products. */
    static class InStockOnlyIterator implements CatalogIterator {
        private final InMemoryCatalog catalog;
        private int position = 0;

        InStockOnlyIterator(InMemoryCatalog catalog) {
            this.catalog = catalog;
            advanceToNextInStock();
        }

        @Override
        public boolean hasNext() {
            return position < catalog.products.length;
        }

        @Override
        public ProductDTO next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            ProductDTO current = catalog.products[position];
            position++;
            advanceToNextInStock();
            return current;
        }

        private void advanceToNextInStock() {
            while (position < catalog.products.length && !catalog.inStock[position]) {
                position++;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Iterator Pattern ===");

        Catalog catalog = new InMemoryCatalog(
                new ProductDTO[] {
                        new ProductDTO("P-1", "Keyboard", 99.0),
                        new ProductDTO("P-2", "Mouse", 25.0),
                        new ProductDTO("P-3", "Monitor", 250.0)
                },
                new boolean[] { true, false, true } // Mouse is out of stock
        );

        CatalogIterator iterator = catalog.createIterator();
        while (iterator.hasNext()) {
            System.out.println("In stock: " + iterator.next());
        }
    }
}
