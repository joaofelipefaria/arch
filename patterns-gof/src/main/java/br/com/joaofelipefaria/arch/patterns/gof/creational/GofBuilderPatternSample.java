package br.com.joaofelipefaria.arch.patterns.gof.creational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <b>Builder</b> (Creational).
 * <p>
 * <b>Intent:</b> separate the construction of a complex object from its
 * representation, so the same construction process can create different
 * representations.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Builder}/{@code ConcreteBuilder} ({@link PurchaseOrderBuilder}) -
 *         a fluent, self-typed builder that declares and implements the
 *         step-by-step construction interface.</li>
 *     <li>{@code Product} ({@link PurchaseOrder}) - the complex object under construction.</li>
 *     <li>{@code Director} ({@link #main}, acting as the director directly) -
 *         drives construction through the Builder interface, without knowing
 *         the product's internal representation.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> object construction involves many optional parts/steps
 * (order lines, discounts, shipping options, etc.) and a telescoping
 * constructor (or one giant constructor with a dozen parameters) would be
 * error-prone and hard to read.
 */
public class GofBuilderPatternSample {

    /** Product: the complex, immutable object being built step by step. */
    static final class PurchaseOrder {
        private final String customerName;
        private final List<String> lineItems;
        private final boolean giftWrapped;

        private PurchaseOrder(String customerName, List<String> lineItems, boolean giftWrapped) {
            this.customerName = customerName;
            this.lineItems = lineItems;
            this.giftWrapped = giftWrapped;
        }

        @Override
        public String toString() {
            return "PurchaseOrder{customer='%s', items=%s, giftWrapped=%s}"
                    .formatted(customerName, lineItems, giftWrapped);
        }
    }

    /** Builder + ConcreteBuilder: fluent step-by-step construction of a PurchaseOrder. */
    static class PurchaseOrderBuilder {
        private String customerName;
        private final List<String> lineItems = new ArrayList<>();
        private boolean giftWrapped;

        PurchaseOrderBuilder forCustomer(String customerName) {
            this.customerName = customerName;
            return this;
        }

        PurchaseOrderBuilder addItem(String item) {
            this.lineItems.add(item);
            return this;
        }

        PurchaseOrderBuilder giftWrapped() {
            this.giftWrapped = true;
            return this;
        }

        PurchaseOrder build() {
            if (customerName == null || customerName.isBlank()) {
                throw new IllegalStateException("customerName is required");
            }
            if (lineItems.isEmpty()) {
                throw new IllegalStateException("at least one line item is required");
            }
            return new PurchaseOrder(customerName, Collections.unmodifiableList(new ArrayList<>(lineItems)), giftWrapped);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Builder Pattern ===");

        PurchaseOrder order = new PurchaseOrderBuilder()
                .forCustomer("Joao Felipe")
                .addItem("Wireless Mouse")
                .addItem("Mechanical Keyboard")
                .giftWrapped()
                .build();

        System.out.println(order);

        // a second, differently-configured order reuses the same builder steps
        PurchaseOrder minimalOrder = new PurchaseOrderBuilder()
                .forCustomer("Ana Souza")
                .addItem("USB-C Cable")
                .build();

        System.out.println(minimalOrder);
    }
}
