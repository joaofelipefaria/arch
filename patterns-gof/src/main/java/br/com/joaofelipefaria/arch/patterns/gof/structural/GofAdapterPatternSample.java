package br.com.joaofelipefaria.arch.patterns.gof.structural;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Adapter</b> (Structural).
 * <p>
 * <b>Intent:</b> convert the interface of a class into another interface
 * clients expect. Adapter lets classes work together that couldn't
 * otherwise because of incompatible interfaces.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Target} ({@link ProductFeed}) - the interface the client expects to use.</li>
 *     <li>{@code Adaptee} ({@link LegacySupplierApi}) - the existing
 *         interface/class with an incompatible shape that needs adapting.</li>
 *     <li>{@code Adapter} ({@link LegacySupplierAdapter}) - implements the
 *         Target interface and translates calls to the Adaptee.</li>
 *     <li>{@code Client} ({@link #main}) - only knows about the Target interface.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to use an existing class (often third-party or
 * legacy, which you can't or don't want to change) but its interface
 * doesn't match what the rest of your code expects - here, a legacy
 * supplier API returning pipe-delimited strings is adapted to the modern
 * {@link ProductDTO}-based {@link ProductFeed} interface.
 */
public class GofAdapterPatternSample {

    /** Target: the interface the rest of the application expects to consume. */
    interface ProductFeed {
        ProductDTO nextProduct();
    }

    /**
     * Adaptee: an existing legacy API with an incompatible shape (returns a
     * raw delimited string instead of a proper object) that we cannot modify.
     */
    static class LegacySupplierApi {
        String fetchNextRawRecord() {
            return "SUP-001|Industrial Bolt|0.35";
        }
    }

    /** Adapter: implements the Target interface by translating calls to the Adaptee. */
    static class LegacySupplierAdapter implements ProductFeed {
        private final LegacySupplierApi legacyApi;

        LegacySupplierAdapter(LegacySupplierApi legacyApi) {
            this.legacyApi = legacyApi;
        }

        @Override
        public ProductDTO nextProduct() {
            String[] parts = legacyApi.fetchNextRawRecord().split("\\|");
            return new ProductDTO(parts[0], parts[1], Double.parseDouble(parts[2]));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Adapter Pattern ===");

        ProductFeed feed = new LegacySupplierAdapter(new LegacySupplierApi());
        ProductDTO product = feed.nextProduct();

        System.out.println("Client received a proper ProductDTO: " + product);
    }
}
