package br.com.joaofelipefaria.arch.patterns.gof.structural;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Flyweight</b> (Structural).
 * <p>
 * <b>Intent:</b> use sharing to support large numbers of fine-grained
 * objects efficiently, by factoring out the state that can be shared
 * (intrinsic state) from the state that varies per use (extrinsic state).
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Flyweight} ({@link ProductCategoryStyle}) - declares the
 *         interface through which flyweights can receive and act on
 *         extrinsic state.</li>
 *     <li>{@code ConcreteFlyweight} (also {@link ProductCategoryStyle}) -
 *         stores intrinsic state (shared, category-wide styling) which must
 *         be independent of the flyweight's context.</li>
 *     <li>{@code FlyweightFactory} ({@link CategoryStyleFactory}) - creates
 *         and manages flyweight objects, returning an existing instance if
 *         one already exists for the given key.</li>
 *     <li>{@code Client} ({@link CatalogEntry}, {@link #main}) - maintains a
 *         reference to a flyweight and supplies the extrinsic state (this
 *         specific product's own name/price) when it needs it.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> an application needs a huge number of similar objects
 * (e.g. every product row in a huge catalog carrying its own copy of
 * category label/icon/color) and most of that per-object state is actually
 * identical across many objects - sharing it as a flyweight cuts memory use
 * dramatically.
 */
public class GofFlyweightPatternSample {

    /** Flyweight: intrinsic (shared) state - identical for every product in the same category. */
    static class ProductCategoryStyle {
        private final String categoryName;
        private final String icon;
        private final String colorHex;

        ProductCategoryStyle(String categoryName, String icon, String colorHex) {
            this.categoryName = categoryName;
            this.icon = icon;
            this.colorHex = colorHex;
        }

        /** Uses the shared (intrinsic) style plus caller-supplied (extrinsic) state to render one line. */
        String render(String productName, double price) {
            return "[%s %s] %s - $%.2f (color: %s)".formatted(icon, categoryName, productName, price, colorHex);
        }
    }

    /** FlyweightFactory: ensures only ONE ProductCategoryStyle instance exists per category. */
    static class CategoryStyleFactory {
        private final Map<String, ProductCategoryStyle> cache = new HashMap<>();

        ProductCategoryStyle styleFor(String categoryName) {
            return cache.computeIfAbsent(categoryName, name -> {
                System.out.println("(creating a NEW shared style object for category: " + name + ")");
                return new ProductCategoryStyle(name, iconFor(name), colorFor(name));
            });
        }

        private String iconFor(String category) {
            return switch (category) {
                case "Electronics" -> "\u26A1";
                case "Books" -> "\uD83D\uDCDA";
                default -> "\u2022";
            };
        }

        private String colorFor(String category) {
            return switch (category) {
                case "Electronics" -> "#3366FF";
                case "Books" -> "#996633";
                default -> "#333333";
            };
        }
    }

    /** Client-side object holding only the extrinsic (per-instance) state, plus a shared flyweight reference. */
    record CatalogEntry(String name, double price, ProductCategoryStyle style) {
        String render() {
            return style.render(name, price);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Flyweight Pattern ===");

        CategoryStyleFactory factory = new CategoryStyleFactory();

        CatalogEntry[] entries = {
                new CatalogEntry("Headphones", 89.90, factory.styleFor("Electronics")),
                new CatalogEntry("USB-C Charger", 19.90, factory.styleFor("Electronics")),
                new CatalogEntry("Design Patterns", 55.0, factory.styleFor("Books")),
        };

        for (CatalogEntry entry : entries) {
            System.out.println(entry.render());
        }

        boolean sharedInstance = entries[0].style() == entries[1].style();
        System.out.println("Both Electronics entries share the same style instance: " + sharedInstance);
    }
}
