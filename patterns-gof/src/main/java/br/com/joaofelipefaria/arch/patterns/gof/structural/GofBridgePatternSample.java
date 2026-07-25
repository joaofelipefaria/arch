package br.com.joaofelipefaria.arch.patterns.gof.structural;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Bridge</b> (Structural).
 * <p>
 * <b>Intent:</b> decouple an abstraction from its implementation so the two
 * can vary independently.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Abstraction} ({@link ProductExporter}) - defines the
 *         high-level interface, holds a reference to an Implementor.</li>
 *     <li>{@code RefinedAbstraction} ({@link DetailedProductExporter}) -
 *         extends the abstraction's interface with extra behavior.</li>
 *     <li>{@code Implementor} ({@link ExportFormat}) - defines the interface
 *         for the low-level, format-specific operations.</li>
 *     <li>{@code ConcreteImplementor} ({@link CsvExportFormat}, {@link JsonExportFormat}) -
 *         implements the Implementor interface for one specific format.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you have two dimensions that both vary - here,
 * "what kind of export" (simple vs. detailed) and "which format" (CSV,
 * JSON) - and you want to combine any exporter with any format without an
 * explosion of subclasses (SimpleCsvExporter, SimpleJsonExporter,
 * DetailedCsvExporter, DetailedJsonExporter, ...). The abstraction and the
 * implementation are connected by a "bridge" (composition) instead of
 * inheritance.
 */
public class GofBridgePatternSample {

    /** Implementor: the low-level, format-specific operation. */
    interface ExportFormat {
        String format(String label, ProductDTO product);
    }

    /** ConcreteImplementor #1. */
    static class CsvExportFormat implements ExportFormat {
        @Override
        public String format(String label, ProductDTO product) {
            return "%s,%s,%s,%.2f".formatted(label, product.id(), product.name(), product.price());
        }
    }

    /** ConcreteImplementor #2. */
    static class JsonExportFormat implements ExportFormat {
        @Override
        public String format(String label, ProductDTO product) {
            return "{\"label\":\"%s\",\"id\":\"%s\",\"name\":\"%s\",\"price\":%.2f}"
                    .formatted(label, product.id(), product.name(), product.price());
        }
    }

    /** Abstraction: holds a reference to the Implementor and delegates to it. */
    static class ProductExporter {
        protected final ExportFormat format;

        ProductExporter(ExportFormat format) {
            this.format = format;
        }

        String export(ProductDTO product) {
            return format.format("product", product);
        }
    }

    /** RefinedAbstraction: extends the abstraction with extra behavior, still via any Implementor. */
    static class DetailedProductExporter extends ProductExporter {
        DetailedProductExporter(ExportFormat format) {
            super(format);
        }

        @Override
        String export(ProductDTO product) {
            return format.format("product-detailed", product) + " [exported-with-details]";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Bridge Pattern ===");

        ProductDTO product = new ProductDTO("P-1", "Desk Lamp", 32.5);

        ProductExporter[] exporters = {
                new ProductExporter(new CsvExportFormat()),
                new ProductExporter(new JsonExportFormat()),
                new DetailedProductExporter(new CsvExportFormat()),
                new DetailedProductExporter(new JsonExportFormat())
        };

        for (ProductExporter exporter : exporters) {
            System.out.println(exporter.getClass().getSimpleName() + " -> " + exporter.export(product));
        }
    }
}
