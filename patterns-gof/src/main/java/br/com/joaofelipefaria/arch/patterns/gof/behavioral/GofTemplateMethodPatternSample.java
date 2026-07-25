package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Template Method</b> (Behavioral).
 * <p>
 * <b>Intent:</b> define the skeleton of an algorithm in an operation,
 * deferring some steps to subclasses. Template Method lets subclasses
 * redefine certain steps of an algorithm without changing the algorithm's
 * overall structure.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code AbstractClass} ({@link ProductImportJob}) - implements the
 *         template method ({@code run()}) defining the skeleton of the
 *         algorithm, and declares abstract "hook" steps for subclasses to fill in.</li>
 *     <li>{@code ConcreteClass} ({@link CsvImportJob}, {@link JsonImportJob}) -
 *         implements the abstract steps to carry out format-specific parts
 *         of the algorithm.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> several classes share the same overall algorithm
 * structure (validate -> parse -> save -> notify) but differ in one or two
 * specific steps - the shared structure lives once in the base class, so it
 * can't drift between subclasses.
 */
public class GofTemplateMethodPatternSample {

    /** AbstractClass: defines the fixed skeleton, delegating format-specific steps to subclasses. */
    abstract static class ProductImportJob {

        /** Template method: the algorithm's structure, marked final so subclasses can't reorder/skip steps. */
        final void run(String rawData) {
            System.out.println("[" + getClass().getSimpleName() + "] Starting import...");
            if (!validate(rawData)) {
                System.out.println("Validation failed, aborting import");
                return;
            }
            ProductDTO product = parse(rawData);
            save(product);
            notifyDone(product);
        }

        boolean validate(String rawData) {
            return rawData != null && !rawData.isBlank();
        }

        abstract ProductDTO parse(String rawData);

        void save(ProductDTO product) {
            System.out.println("Saving to database: " + product);
        }

        void notifyDone(ProductDTO product) {
            System.out.println("Import finished for " + product.name());
        }
    }

    /** ConcreteClass: fills in the CSV-specific parsing step. */
    static class CsvImportJob extends ProductImportJob {
        @Override
        ProductDTO parse(String rawData) {
            String[] parts = rawData.split(",");
            return new ProductDTO(parts[0], parts[1], Double.parseDouble(parts[2]));
        }
    }

    /** ConcreteClass: fills in a (simplified) JSON-specific parsing step. */
    static class JsonImportJob extends ProductImportJob {
        @Override
        ProductDTO parse(String rawData) {
            // simplified/manual parsing just for this example - real code would use a JSON library
            String id = extract(rawData, "id");
            String name = extract(rawData, "name");
            double price = Double.parseDouble(extract(rawData, "price"));
            return new ProductDTO(id, name, price);
        }

        private String extract(String json, String field) {
            String marker = "\"" + field + "\":";
            int start = json.indexOf(marker) + marker.length();
            int end = json.indexOf(',', start);
            if (end == -1) {
                end = json.indexOf('}', start);
            }
            return json.substring(start, end).replace("\"", "").trim();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Template Method Pattern ===");

        new CsvImportJob().run("P-1,Wall Clock,24.90");
        new JsonImportJob().run("{\"id\":\"P-2\",\"name\":\"Desk Fan\",\"price\":39.90}");
        new CsvImportJob().run(""); // triggers the shared validation step, no format-specific code involved
    }
}
