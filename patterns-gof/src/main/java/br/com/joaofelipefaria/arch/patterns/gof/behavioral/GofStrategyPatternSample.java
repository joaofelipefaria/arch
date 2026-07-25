package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Strategy</b> (Behavioral).
 * <p>
 * <b>Intent:</b> define a family of algorithms, encapsulate each one, and
 * make them interchangeable. Strategy lets the algorithm vary independently
 * from the clients that use it.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Strategy} ({@link PricingStrategy}) - declares an interface
 *         common to all supported algorithms.</li>
 *     <li>{@code ConcreteStrategy} ({@link RegularPricing}, {@link BlackFridayPricing},
 *         {@link ClearancePricing}) - implements one specific algorithm
 *         using the Strategy interface.</li>
 *     <li>{@code Context} ({@link PriceCalculator}) - is configured with a
 *         ConcreteStrategy object and calls it to do the work, without
 *         knowing which concrete algorithm it's using.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you have several interchangeable variants of an
 * algorithm (here, pricing rules) and want to select/swap between them at
 * runtime without a big conditional inside the class that uses them.
 */
public class GofStrategyPatternSample {

    /** Strategy: the interface every pricing algorithm must implement. */
    interface PricingStrategy {
        double finalPrice(ProductDTO product);
    }

    /** ConcreteStrategy #1: no discount. */
    static class RegularPricing implements PricingStrategy {
        @Override
        public double finalPrice(ProductDTO product) {
            return product.price();
        }
    }

    /** ConcreteStrategy #2: flat 30% off. */
    static class BlackFridayPricing implements PricingStrategy {
        @Override
        public double finalPrice(ProductDTO product) {
            return product.price() * 0.70;
        }
    }

    /** ConcreteStrategy #3: 50% off, floored at a minimum of $1. */
    static class ClearancePricing implements PricingStrategy {
        @Override
        public double finalPrice(ProductDTO product) {
            return Math.max(1.0, product.price() * 0.50);
        }
    }

    /** Context: uses whichever strategy it's configured with, without knowing its concrete type. */
    static class PriceCalculator {
        private PricingStrategy strategy;

        PriceCalculator(PricingStrategy strategy) {
            this.strategy = strategy;
        }

        void setStrategy(PricingStrategy strategy) {
            this.strategy = strategy;
        }

        double calculate(ProductDTO product) {
            return strategy.finalPrice(product);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Strategy Pattern ===");

        ProductDTO product = new ProductDTO("P-1", "Air Fryer", 199.90);
        PriceCalculator calculator = new PriceCalculator(new RegularPricing());

        System.out.println("Regular price: $" + calculator.calculate(product));

        calculator.setStrategy(new BlackFridayPricing());
        System.out.println("Black Friday price: $" + calculator.calculate(product));

        calculator.setStrategy(new ClearancePricing());
        System.out.println("Clearance price: $" + calculator.calculate(product));
    }
}
