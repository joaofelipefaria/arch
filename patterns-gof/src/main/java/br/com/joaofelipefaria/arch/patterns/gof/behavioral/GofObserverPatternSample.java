package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import java.util.ArrayList;
import java.util.List;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Observer</b> (Behavioral).
 * <p>
 * <b>Intent:</b> define a one-to-many dependency between objects so that
 * when one object changes state, all its dependents are notified and
 * updated automatically.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Subject} ({@link PriceWatchable}) - knows its observers and
 *         provides an interface for attaching/detaching them.</li>
 *     <li>{@code ConcreteSubject} ({@link Product}) - stores state of
 *         interest and sends a notification to its observers when it changes.</li>
 *     <li>{@code Observer} ({@link PriceChangeListener}) - defines an
 *         updating interface for objects that should be notified of changes.</li>
 *     <li>{@code ConcreteObserver} ({@link EmailAlert}, {@link StockDashboard}) -
 *         reacts to notifications, keeping its own state consistent with the subject's.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> a change to one object (a product's price) requires
 * changing/notifying an open-ended, dynamic set of other objects, and you
 * don't want the subject to be hard-coded to know about each one.
 */
public class GofObserverPatternSample {

    /** Observer: the update callback every listener must implement. */
    interface PriceChangeListener {
        void onPriceChanged(ProductDTO product, double oldPrice, double newPrice);
    }

    /** Subject: declares attach/detach for observers. */
    interface PriceWatchable {
        void addListener(PriceChangeListener listener);
        void removeListener(PriceChangeListener listener);
    }

    /** ConcreteSubject: holds the product's state and notifies observers when the price changes. */
    static class Product implements PriceWatchable {
        private final List<PriceChangeListener> listeners = new ArrayList<>();
        private ProductDTO data;

        Product(ProductDTO data) {
            this.data = data;
        }

        @Override
        public void addListener(PriceChangeListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeListener(PriceChangeListener listener) {
            listeners.remove(listener);
        }

        void changePrice(double newPrice) {
            double oldPrice = data.price();
            data = new ProductDTO(data.id(), data.name(), newPrice);
            for (PriceChangeListener listener : listeners) {
                listener.onPriceChanged(data, oldPrice, newPrice);
            }
        }
    }

    /** ConcreteObserver #1. */
    static class EmailAlert implements PriceChangeListener {
        @Override
        public void onPriceChanged(ProductDTO product, double oldPrice, double newPrice) {
            System.out.println("[EmailAlert] " + product.name() + " changed from $" + oldPrice + " to $" + newPrice);
        }
    }

    /** ConcreteObserver #2. */
    static class StockDashboard implements PriceChangeListener {
        @Override
        public void onPriceChanged(ProductDTO product, double oldPrice, double newPrice) {
            System.out.println("[StockDashboard] Refreshing tile for " + product.name() + " -> $" + newPrice);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Observer Pattern ===");

        Product product = new Product(new ProductDTO("P-1", "Graphics Card", 599.90));
        PriceChangeListener emailAlert = new EmailAlert();
        product.addListener(emailAlert);
        product.addListener(new StockDashboard());

        product.changePrice(549.90);

        product.removeListener(emailAlert);
        System.out.println("-- EmailAlert unsubscribed --");
        product.changePrice(499.90); // only StockDashboard reacts now
    }
}
