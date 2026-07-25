package br.com.joaofelipefaria.arch.patterns.gof.creational;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <b>Singleton</b> (Creational).
 * <p>
 * <b>Intent:</b> ensure a class has only one instance, and provide a global
 * point of access to it.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Singleton} ({@link CatalogSequenceGenerator}) - declares
 *         the single {@code getInstance()} access point and is responsible
 *         for creating/holding its own unique instance.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> exactly one instance of a class must coordinate shared
 * state or a shared resource across the whole application (e.g. a
 * sequence/id generator, a connection pool, a configuration registry) - and
 * you're prepared to accept the well-known downsides of Singleton (hidden
 * global state, harder unit testing) in exchange for that guarantee.
 * <p>
 * Implemented here as an <b>enum singleton</b> (Joshua Bloch's recommended
 * approach): it's inherently thread-safe, serialization-safe, and immune to
 * reflection-based instantiation attacks, without any manual synchronization.
 */
public class GofSingletonPatternSample {

    /** Singleton: enum-based, the JVM guarantees exactly one instance per enum constant. */
    enum CatalogSequenceGenerator {
        INSTANCE;

        private final AtomicLong counter = new AtomicLong(0);

        long nextId() {
            return counter.incrementAndGet();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Singleton Pattern ===");

        long first = CatalogSequenceGenerator.INSTANCE.nextId();
        long second = CatalogSequenceGenerator.INSTANCE.nextId();

        System.out.println("First id generated:  " + first);
        System.out.println("Second id generated: " + second);
        System.out.println("Same instance everywhere: "
                + (CatalogSequenceGenerator.INSTANCE == CatalogSequenceGenerator.INSTANCE));
    }
}
