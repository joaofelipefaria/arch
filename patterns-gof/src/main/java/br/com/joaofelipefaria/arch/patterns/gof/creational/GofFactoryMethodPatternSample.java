package br.com.joaofelipefaria.arch.patterns.gof.creational;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Factory Method</b> (Creational).
 * <p>
 * <b>Intent:</b> define an interface for creating an object, but let
 * subclasses decide which class to instantiate. Factory Method lets a class
 * defer instantiation to subclasses.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Creator} ({@link ProductImporter}) - declares the factory
 *         method, which returns an object of type Product. May also define a
 *         default implementation and call the factory method from its own logic.</li>
 *     <li>{@code ConcreteCreator} ({@link ElectronicsImporter}, {@link BookImporter}) -
 *         overrides the factory method to return an instance of a
 *         concrete-product-specific configuration.</li>
 *     <li>{@code Product} ({@link ProductDTO}) - the object being created.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> a class can't anticipate which concrete "flavor" of
 * object it needs to create ahead of time, and wants subclasses to specify
 * that instead - here, each importer knows how to build the ProductDTO for
 * its own category (import tax, category-specific pricing rules, etc.),
 * while the shared import workflow stays in the base class.
 */
public class GofFactoryMethodPatternSample {

    /** Creator: defines the workflow, delegates actual product creation to subclasses. */
    abstract static class ProductImporter {

        /** Factory method - subclasses decide how the ProductDTO is actually built. */
        abstract ProductDTO createProduct(String id, String name, double basePrice);

        /** Template-ish workflow that USES the factory method, without knowing the concrete type. */
        final ProductDTO importProduct(String id, String name, double basePrice) {
            ProductDTO product = createProduct(id, name, basePrice);
            System.out.println("Imported via " + getClass().getSimpleName() + ": " + product);
            return product;
        }
    }

    /** ConcreteCreator: applies electronics-specific import tax. */
    static class ElectronicsImporter extends ProductImporter {
        @Override
        ProductDTO createProduct(String id, String name, double basePrice) {
            return new ProductDTO(id, name, basePrice * 1.15); // 15% import tax
        }
    }

    /** ConcreteCreator: books are tax-exempt in this example. */
    static class BookImporter extends ProductImporter {
        @Override
        ProductDTO createProduct(String id, String name, double basePrice) {
            return new ProductDTO(id, name, basePrice);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Factory Method Pattern ===");

        ProductImporter electronics = new ElectronicsImporter();
        electronics.importProduct("E-1", "Bluetooth Speaker", 40.0);

        ProductImporter books = new BookImporter();
        books.importProduct("B-1", "Design Patterns", 55.0);
    }
}
