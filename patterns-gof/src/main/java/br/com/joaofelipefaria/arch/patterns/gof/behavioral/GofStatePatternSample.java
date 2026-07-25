package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

/**
 * <b>State</b> (Behavioral).
 * <p>
 * <b>Intent:</b> allow an object to alter its behavior when its internal
 * state changes. The object will appear to change its class.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Context} ({@link Order}) - maintains an instance of a
 *         ConcreteState subclass that defines the current state, and
 *         delegates state-specific requests to it.</li>
 *     <li>{@code State} ({@link OrderState}) - defines an interface for
 *         encapsulating the behavior associated with a particular state.</li>
 *     <li>{@code ConcreteState} ({@link PendingState}, {@link PaidState},
 *         {@link ShippedState}, {@link CancelledState}) - each implements
 *         behavior specific to one state of the Context, including which
 *         transitions are legal from that state.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> an object's behavior depends on its state and must
 * change at runtime depending on that state - here, which actions are legal
 * on an order (pay, ship, cancel) depends entirely on its current state,
 * and adding a new state/transition means adding one new class instead of
 * editing a growing if/else or switch statement scattered across the Context.
 */
public class GofStatePatternSample {

    /** State: the behavior (and legal transitions) for one particular state. */
    interface OrderState {
        OrderState pay(Order order);
        OrderState ship(Order order);
        OrderState cancel(Order order);
        String name();
    }

    /** Context: delegates every action to its current state, and swaps state as a result. */
    static class Order {
        private OrderState state = new PendingState();

        void pay() {
            state = state.pay(this);
        }

        void ship() {
            state = state.ship(this);
        }

        void cancel() {
            state = state.cancel(this);
        }

        String currentState() {
            return state.name();
        }
    }

    /** ConcreteState: initial state - can be paid or cancelled, cannot be shipped yet. */
    static class PendingState implements OrderState {
        @Override
        public OrderState pay(Order order) {
            System.out.println("Payment received, order is now PAID");
            return new PaidState();
        }

        @Override
        public OrderState ship(Order order) {
            System.out.println("Cannot ship an order that hasn't been paid yet");
            return this;
        }

        @Override
        public OrderState cancel(Order order) {
            System.out.println("Order cancelled before payment");
            return new CancelledState();
        }

        @Override
        public String name() {
            return "PENDING";
        }
    }

    /** ConcreteState: can be shipped or cancelled (e.g. refunded), cannot be paid again. */
    static class PaidState implements OrderState {
        @Override
        public OrderState pay(Order order) {
            System.out.println("Order is already paid");
            return this;
        }

        @Override
        public OrderState ship(Order order) {
            System.out.println("Order shipped");
            return new ShippedState();
        }

        @Override
        public OrderState cancel(Order order) {
            System.out.println("Order cancelled after payment, refund issued");
            return new CancelledState();
        }

        @Override
        public String name() {
            return "PAID";
        }
    }

    /** ConcreteState: terminal-ish state - can no longer be paid, shipped again, or cancelled. */
    static class ShippedState implements OrderState {
        @Override
        public OrderState pay(Order order) {
            System.out.println("Order is already paid and shipped");
            return this;
        }

        @Override
        public OrderState ship(Order order) {
            System.out.println("Order is already shipped");
            return this;
        }

        @Override
        public OrderState cancel(Order order) {
            System.out.println("Cannot cancel an order that has already shipped");
            return this;
        }

        @Override
        public String name() {
            return "SHIPPED";
        }
    }

    /** ConcreteState: terminal state - no further transitions allowed. */
    static class CancelledState implements OrderState {
        @Override
        public OrderState pay(Order order) {
            System.out.println("Cannot pay a cancelled order");
            return this;
        }

        @Override
        public OrderState ship(Order order) {
            System.out.println("Cannot ship a cancelled order");
            return this;
        }

        @Override
        public OrderState cancel(Order order) {
            System.out.println("Order is already cancelled");
            return this;
        }

        @Override
        public String name() {
            return "CANCELLED";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== State Pattern ===");

        Order order = new Order();
        System.out.println("State: " + order.currentState());

        order.ship();  // illegal from PENDING
        order.pay();
        System.out.println("State: " + order.currentState());

        order.ship();
        System.out.println("State: " + order.currentState());

        order.cancel(); // illegal from SHIPPED
        System.out.println("Final state: " + order.currentState());
    }
}
