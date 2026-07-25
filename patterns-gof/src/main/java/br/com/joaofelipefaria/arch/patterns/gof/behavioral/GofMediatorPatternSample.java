package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

/**
 * <b>Mediator</b> (Behavioral).
 * <p>
 * <b>Intent:</b> define an object that encapsulates how a set of objects
 * interact. Mediator promotes loose coupling by keeping objects from
 * referring to each other explicitly, letting you vary their interaction
 * independently.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Mediator} ({@link CheckoutMediator}) - defines the
 *         interface for communicating with Colleague objects.</li>
 *     <li>{@code ConcreteMediator} ({@link CheckoutPageMediator}) -
 *         implements cooperative behavior by coordinating Colleague objects,
 *         and knows/maintains its colleagues.</li>
 *     <li>{@code Colleague} ({@link StockField}, {@link CouponField},
 *         {@link CheckoutButton}) - each colleague only talks to the
 *         mediator, never directly to another colleague.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> a group of UI-like components need to react to each
 * other's changes (e.g. a checkout form where the "stock" and "coupon"
 * fields both affect whether the "checkout" button is enabled), and you
 * want to avoid every component holding direct references to every other
 * component (which turns into a tangled many-to-many mess as the form grows).
 */
public class GofMediatorPatternSample {

    /** Mediator: the interface colleagues use to notify the mediator of their changes. */
    interface CheckoutMediator {
        void notifyChanged(Object colleague);
    }

    /** ConcreteMediator: knows all colleagues and coordinates them. */
    static class CheckoutPageMediator implements CheckoutMediator {
        private StockField stockField;
        private CouponField couponField;
        private CheckoutButton checkoutButton;

        void register(StockField stockField, CouponField couponField, CheckoutButton checkoutButton) {
            this.stockField = stockField;
            this.couponField = couponField;
            this.checkoutButton = checkoutButton;
        }

        @Override
        public void notifyChanged(Object colleague) {
            boolean canCheckout = stockField.inStock() && couponField.isValidOrEmpty();
            checkoutButton.setEnabled(canCheckout);
            System.out.println("[Mediator] Re-evaluated form after " + colleague.getClass().getSimpleName()
                    + " changed -> checkout enabled: " + canCheckout);
        }
    }

    /** Colleague #1: only talks to the mediator, never directly to the coupon field or the button. */
    static class StockField {
        private final CheckoutMediator mediator;
        private boolean inStock = true;

        StockField(CheckoutMediator mediator) {
            this.mediator = mediator;
        }

        boolean inStock() {
            return inStock;
        }

        void setInStock(boolean inStock) {
            this.inStock = inStock;
            mediator.notifyChanged(this);
        }
    }

    /** Colleague #2. */
    static class CouponField {
        private final CheckoutMediator mediator;
        private String code = "";

        CouponField(CheckoutMediator mediator) {
            this.mediator = mediator;
        }

        boolean isValidOrEmpty() {
            return code.isEmpty() || code.equals("VALID10");
        }

        void setCode(String code) {
            this.code = code;
            mediator.notifyChanged(this);
        }
    }

    /** Colleague #3: purely reactive, its only job is to reflect what the mediator decides. */
    static class CheckoutButton {
        private boolean enabled;

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Mediator Pattern ===");

        CheckoutPageMediator mediator = new CheckoutPageMediator();
        StockField stockField = new StockField(mediator);
        CouponField couponField = new CouponField(mediator);
        CheckoutButton checkoutButton = new CheckoutButton();

        mediator.register(stockField, couponField, checkoutButton);

        stockField.setInStock(true);           // -> checkout enabled
        couponField.setCode("INVALID-CODE");    // -> checkout disabled
        couponField.setCode("VALID10");         // -> checkout enabled again

        System.out.println("Final checkout button state: " + checkoutButton.isEnabled());
    }
}
