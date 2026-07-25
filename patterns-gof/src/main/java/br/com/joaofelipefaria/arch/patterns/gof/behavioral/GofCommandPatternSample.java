package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import java.util.ArrayDeque;
import java.util.Deque;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Command</b> (Behavioral).
 * <p>
 * <b>Intent:</b> encapsulate a request as an object, thereby letting you
 * parameterize clients with different requests, queue or log requests, and
 * support undoable operations.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Command} ({@link Command}) - declares an interface for
 *         executing (and here, undoing) an operation.</li>
 *     <li>{@code ConcreteCommand} ({@link AddToCartCommand}) - binds a
 *         Receiver to an action and implements execute()/undo() by calling
 *         the corresponding operations on the Receiver.</li>
 *     <li>{@code Receiver} ({@link ShoppingCart}) - knows how to perform the
 *         actual work needed to carry out the request.</li>
 *     <li>{@code Invoker} ({@link CommandHistory}) - asks the command to
 *         carry out the request, and keeps a history for undo.</li>
 *     <li>{@code Client} ({@link #main}) - creates ConcreteCommand objects
 *         and sets their receiver.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to decouple "what should happen" from "who
 * triggers it" - e.g. to support undo/redo, queue operations, or log every
 * action for auditing, exactly the way a shopping cart's add/remove actions
 * need to be undoable here.
 */
public class GofCommandPatternSample {

    /** Command: the common execute()/undo() interface. */
    interface Command {
        void execute();
        void undo();
    }

    /** Receiver: knows how to actually perform the work. */
    static class ShoppingCart {
        private final java.util.List<ProductDTO> items = new java.util.ArrayList<>();

        void add(ProductDTO product) {
            items.add(product);
            System.out.println("[Cart] Added " + product.name());
        }

        void remove(ProductDTO product) {
            items.remove(product);
            System.out.println("[Cart] Removed " + product.name());
        }

        double total() {
            return items.stream().mapToDouble(ProductDTO::price).sum();
        }
    }

    /** ConcreteCommand: binds the receiver to a specific action, and knows how to reverse it. */
    static class AddToCartCommand implements Command {
        private final ShoppingCart cart;
        private final ProductDTO product;

        AddToCartCommand(ShoppingCart cart, ProductDTO product) {
            this.cart = cart;
            this.product = product;
        }

        @Override
        public void execute() {
            cart.add(product);
        }

        @Override
        public void undo() {
            cart.remove(product);
        }
    }

    /** Invoker: executes commands and keeps a history so it can undo the last one. */
    static class CommandHistory {
        private final Deque<Command> history = new ArrayDeque<>();

        void run(Command command) {
            command.execute();
            history.push(command);
        }

        void undoLast() {
            if (!history.isEmpty()) {
                history.pop().undo();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Command Pattern ===");

        ShoppingCart cart = new ShoppingCart();
        CommandHistory history = new CommandHistory();

        history.run(new AddToCartCommand(cart, new ProductDTO("P-1", "Notebook", 8.5)));
        history.run(new AddToCartCommand(cart, new ProductDTO("P-2", "Pen Set", 12.0)));

        System.out.println("Total before undo: $" + cart.total());

        history.undoLast(); // undoes adding the Pen Set

        System.out.println("Total after undo: $" + cart.total());
    }
}
