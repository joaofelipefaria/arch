package br.com.joaofelipefaria.arch.patterns.gof.dto;

/**
 * Simple, immutable data holder shared across several pattern samples in
 * this project, so the examples read as variations on one consistent
 * domain (a small product catalog) instead of 23 unrelated toy problems.
 * <p>
 * Not every pattern needs a "product" to make sense (e.g. Singleton,
 * Iterator), so not every sample uses this record - it's used wherever it
 * naturally fits the pattern being demonstrated.
 *
 * @param id    unique identifier of the product
 * @param name  display name of the product
 * @param price base price of the product, before any pattern-specific
 *              decoration/strategy is applied
 */
public record ProductDTO(String id, String name, double price) {
}
