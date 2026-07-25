package br.com.joaofelipefaria.arch.patterns.gof.structural;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Decorator</b> (Structural).
 * <p>
 * <b>Intent:</b> attach additional responsibilities to an object dynamically.
 * Decorators provide a flexible alternative to subclassing for extending
 * functionality.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Component} ({@link PricedItem}) - defines the interface for
 *         objects that can have responsibilities added dynamically.</li>
 *     <li>{@code ConcreteComponent} ({@link BaseItem}) - the plain object
 *         that decorators wrap and extend.</li>
 *     <li>{@code Decorator} ({@link PricedItemDecorator}) - maintains a
 *         reference to a Component and conforms to its interface.</li>
 *     <li>{@code ConcreteDecorator} ({@link GiftWrapDecorator}, {@link ExpressShippingDecorator}) -
 *         adds one specific responsibility (and its cost) to the component.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to add optional, combinable behavior to
 * individual objects at runtime (gift wrap, express shipping, insurance,
 * ...) without creating a new subclass for every possible combination -
 * decorators can be stacked in any order and any quantity.
 */
public class GofDecoratorPatternSample {

    /** Component: something that has a description and a price. */
    interface PricedItem {
        String description();
        double price();
    }

    /** ConcreteComponent: the plain product, with no extras applied yet. */
    static class BaseItem implements PricedItem {
        private final ProductDTO product;

        BaseItem(ProductDTO product) {
            this.product = product;
        }

        @Override
        public String description() {
            return product.name();
        }

        @Override
        public double price() {
            return product.price();
        }
    }

    /** Decorator: wraps another PricedItem and conforms to the same interface. */
    abstract static class PricedItemDecorator implements PricedItem {
        protected final PricedItem wrapped;

        PricedItemDecorator(PricedItem wrapped) {
            this.wrapped = wrapped;
        }
    }

    /** ConcreteDecorator #1: adds gift wrapping and its extra cost. */
    static class GiftWrapDecorator extends PricedItemDecorator {
        GiftWrapDecorator(PricedItem wrapped) {
            super(wrapped);
        }

        @Override
        public String description() {
            return wrapped.description() + " + gift wrap";
        }

        @Override
        public double price() {
            return wrapped.price() + 4.90;
        }
    }

    /** ConcreteDecorator #2: adds express shipping and its extra cost. */
    static class ExpressShippingDecorator extends PricedItemDecorator {
        ExpressShippingDecorator(PricedItem wrapped) {
            super(wrapped);
        }

        @Override
        public String description() {
            return wrapped.description() + " + express shipping";
        }

        @Override
        public double price() {
            return wrapped.price() + 12.00;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Decorator Pattern ===");

        PricedItem plain = new BaseItem(new ProductDTO("P-1", "Board Game", 45.0));
        System.out.println(plain.description() + " -> $" + plain.price());

        // decorators stack freely, in any order/combination
        PricedItem giftWrapped = new GiftWrapDecorator(plain);
        System.out.println(giftWrapped.description() + " -> $" + giftWrapped.price());

        PricedItem giftWrappedAndExpress = new ExpressShippingDecorator(giftWrapped);
        System.out.println(giftWrappedAndExpress.description() + " -> $" + giftWrappedAndExpress.price());
    }
}
