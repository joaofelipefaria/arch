package br.com.joaofelipefaria.arch.patterns.gof.structural;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Facade</b> (Structural).
 * <p>
 * <b>Intent:</b> provide a unified, higher-level interface to a set of
 * interfaces in a subsystem. Facade defines a simpler interface that makes
 * the subsystem easier to use.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Facade} ({@link CheckoutFacade}) - knows which subsystem
 *         classes are responsible for a request, and delegates client
 *         requests to the appropriate subsystem objects in the right order.</li>
 *     <li>{@code Subsystem classes} ({@link InventoryService}, {@link PaymentService},
 *         {@link ShippingService}) - implement subsystem functionality and
 *         have no knowledge of the facade (they're perfectly usable directly too).</li>
 * </ul>
 * <p>
 * <b>Use it when</b> a task (like "check out an order") requires
 * coordinating several independent subsystems in a specific sequence, and
 * you want to give callers one simple entry point instead of forcing them
 * to know and correctly orchestrate every subsystem themselves.
 */
public class GofFacadePatternSample {

    /** Subsystem #1. */
    static class InventoryService {
        boolean reserve(ProductDTO product, int quantity) {
            System.out.println("[Inventory] Reserved " + quantity + "x " + product.name());
            return true;
        }
    }

    /** Subsystem #2. */
    static class PaymentService {
        boolean charge(double amount) {
            System.out.println("[Payment] Charged $" + amount);
            return true;
        }
    }

    /** Subsystem #3. */
    static class ShippingService {
        void scheduleDelivery(ProductDTO product, int quantity) {
            System.out.println("[Shipping] Scheduled delivery for " + quantity + "x " + product.name());
        }
    }

    /** Facade: one simple method that correctly orchestrates all three subsystems. */
    static class CheckoutFacade {
        private final InventoryService inventoryService = new InventoryService();
        private final PaymentService paymentService = new PaymentService();
        private final ShippingService shippingService = new ShippingService();

        boolean checkout(ProductDTO product, int quantity) {
            if (!inventoryService.reserve(product, quantity)) {
                return false;
            }
            if (!paymentService.charge(product.price() * quantity)) {
                return false;
            }
            shippingService.scheduleDelivery(product, quantity);
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Facade Pattern ===");

        CheckoutFacade checkout = new CheckoutFacade();
        ProductDTO product = new ProductDTO("P-1", "Yoga Mat", 29.90);

        boolean success = checkout.checkout(product, 2);
        System.out.println("Checkout successful: " + success);
    }
}
