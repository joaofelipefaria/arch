package br.com.joaofelipefaria.arch.patterns.gof.structural;

import java.util.HashMap;
import java.util.Map;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Proxy</b> (Structural).
 * <p>
 * <b>Intent:</b> provide a surrogate or placeholder for another object to
 * control access to it.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Subject} ({@link ProductRepository}) - the common interface
 *         for the RealSubject and the Proxy, so the proxy is interchangeable
 *         with the real object.</li>
 *     <li>{@code RealSubject} ({@link RemoteProductRepository}) - the real
 *         object the proxy represents (here, standing in for a slow remote/database call).</li>
 *     <li>{@code Proxy} ({@link CachingProductRepositoryProxy}) - maintains a
 *         reference to the RealSubject and controls access to it (in this
 *         case, a caching proxy that avoids repeated slow calls).</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need to control access to an expensive or remote
 * object - common variants are caching proxies (this example), lazy-loading
 * (virtual) proxies, protection proxies (access control), and remote proxies.
 */
public class GofProxyPatternSample {

    /** Subject: the common interface, so client code can't tell proxy from real object. */
    interface ProductRepository {
        ProductDTO findById(String id);
    }

    /** RealSubject: the actual (here, simulated-as-slow) data access. */
    static class RemoteProductRepository implements ProductRepository {
        @Override
        public ProductDTO findById(String id) {
            simulateSlowCall();
            return new ProductDTO(id, "Product " + id, 42.0);
        }

        private void simulateSlowCall() {
            System.out.println("[RemoteProductRepository] ...simulating a slow remote/database call...");
        }
    }

    /** Proxy: adds caching in front of the RealSubject, transparently to the client. */
    static class CachingProductRepositoryProxy implements ProductRepository {
        private final ProductRepository realRepository;
        private final Map<String, ProductDTO> cache = new HashMap<>();

        CachingProductRepositoryProxy(ProductRepository realRepository) {
            this.realRepository = realRepository;
        }

        @Override
        public ProductDTO findById(String id) {
            if (cache.containsKey(id)) {
                System.out.println("[Proxy] Cache hit for id=" + id + ", real repository was NOT called");
                return cache.get(id);
            }
            System.out.println("[Proxy] Cache miss for id=" + id + ", delegating to the real repository");
            ProductDTO product = realRepository.findById(id);
            cache.put(id, product);
            return product;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Proxy Pattern ===");

        ProductRepository repository = new CachingProductRepositoryProxy(new RemoteProductRepository());

        System.out.println("1st call: " + repository.findById("P-1"));
        System.out.println("2nd call: " + repository.findById("P-1")); // served from cache, no slow call
    }
}
