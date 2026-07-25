# GoF Design Patterns - Java Samples

Standalone, runnable Java examples of all **23 classic Gang of Four (GoF)**
design patterns, from *Design Patterns: Elements of Reusable Object-Oriented
Software* (Gamma, Helm, Johnson, Vlissides - 1994).

Every pattern lives in its own self-contained class, runnable directly via
its own `main` method - no test framework, no external dependencies, just
`java.base`. Most samples share a common domain (a small **product
catalog**) via [`ProductDTO`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/dto/ProductDTO.java),
so the differences between patterns are easier to compare - they're variations
on one theme instead of 23 unrelated toy problems.

## Project info

| | |
|---|---|
| **Group ID** | `br.com.joaofelipefaria.arch` |
| **Artifact ID** | `patterns.gof` |
| **Base package** | `br.com.joaofelipefaria.arch.patterns.gof` |
| **Java version** | 17 |
| **Build tool** | Maven |

## How to run

```bash
mvn compile

# run any single pattern, e.g.:
mvn exec:java -Dexec.mainClass="br.com.joaofelipefaria.arch.patterns.gof.creational.GofSingletonPatternSample"
```

(If you don't have the `exec-maven-plugin` configured, you can also just run
the compiled class directly with `java -cp target/classes <fully-qualified-class-name>`,
or run any sample's `main` method straight from your IDE.)

## Project structure

```
src/main/java/br/com/joaofelipefaria/arch/patterns/gof/
├── dto/
│   └── ProductDTO.java              # shared record used across multiple samples
├── creational/                      # 5 patterns
├── structural/                      # 7 patterns
└── behavioral/                      # 11 patterns
```

Every pattern class is named `Gof<PatternName>PatternSample` and contains:
- A Javadoc block explaining **Intent**, **Participants** (mapped to the
  actual classes in the sample), and **when to use it**.
- The minimal set of nested classes/interfaces needed to demonstrate the
  pattern's structure honestly (not a "hello world" oversimplification).
- A `main` method that exercises the pattern and prints what's happening,
  so you can literally run it and watch the pattern's mechanics play out.

---

## Creational Patterns (5)

Concerned with **how objects are created**, abstracting away the
instantiation process so a system is independent of how its objects are
created, composed, and represented.

| Pattern | Class | Description |
|---|---|---|
| **Abstract Factory** | [`GofAbstractFactoryPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/creational/GofAbstractFactoryPatternSample.java) | Provides an interface for creating **families** of related objects without specifying their concrete classes - here, a "standard" vs. "premium" catalog factory, each producing a matching product + label pair. |
| **Builder** | [`GofBuilderPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/creational/GofBuilderPatternSample.java) | Separates the construction of a complex object from its representation, so the same step-by-step process can build different configurations - a fluent `PurchaseOrderBuilder` instead of a telescoping constructor. |
| **Factory Method** | [`GofFactoryMethodPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/creational/GofFactoryMethodPatternSample.java) | Defines an interface for creating an object, but lets subclasses decide which concrete instance to create - each product "importer" subclass knows how to build its own category-specific `ProductDTO`. |
| **Prototype** | [`GofPrototypePatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/creational/GofPrototypePatternSample.java) | Creates new objects by **cloning** a fully-configured prototype instance instead of building from scratch - a `CatalogEntry` template is cloned and tweaked per variant. |
| **Singleton** | [`GofSingletonPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/creational/GofSingletonPatternSample.java) | Ensures a class has exactly one instance and provides a single global access point - implemented as a thread-safe **enum singleton** (the idiomatic Java approach). |

## Structural Patterns (7)

Concerned with **how classes and objects are composed** to form larger
structures, while keeping those structures flexible and efficient.

| Pattern | Class | Description |
|---|---|---|
| **Adapter** | [`GofAdapterPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofAdapterPatternSample.java) | Converts the interface of an existing class into another interface clients expect - adapts a legacy, pipe-delimited-string supplier API to the modern `ProductDTO`-based interface. |
| **Bridge** | [`GofBridgePatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofBridgePatternSample.java) | Decouples an abstraction from its implementation so both can vary independently - "what kind of exporter" (simple/detailed) is bridged to "which format" (CSV/JSON) via composition, avoiding a subclass explosion. |
| **Composite** | [`GofCompositePatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofCompositePatternSample.java) | Composes objects into tree structures to represent part-whole hierarchies, letting clients treat a single product and a whole category tree uniformly. |
| **Decorator** | [`GofDecoratorPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofDecoratorPatternSample.java) | Attaches additional responsibilities to an object dynamically - gift-wrap and express-shipping decorators stack on top of a plain priced item, in any combination. |
| **Facade** | [`GofFacadePatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofFacadePatternSample.java) | Provides a single, simplified interface to a set of subsystems - a `CheckoutFacade` correctly orchestrates inventory, payment, and shipping subsystems behind one method call. |
| **Flyweight** | [`GofFlyweightPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofFlyweightPatternSample.java) | Shares fine-grained objects efficiently by separating shared (intrinsic) state from per-instance (extrinsic) state - one `ProductCategoryStyle` object is shared across every product in that category. |
| **Proxy** | [`GofProxyPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/structural/GofProxyPatternSample.java) | Provides a surrogate that controls access to another object - a caching proxy sits in front of a (simulated) slow repository, transparently avoiding repeated slow calls. |

## Behavioral Patterns (11)

Concerned with **algorithms and the assignment of responsibilities**
between objects - how objects interact and distribute responsibility.

| Pattern | Class | Description |
|---|---|---|
| **Chain of Responsibility** | [`GofChainOfResponsibilityPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofChainOfResponsibilityPatternSample.java) | Passes a request along a chain of handlers until one handles it - a discount request escalates through team lead -> manager -> director approvers. |
| **Command** | [`GofCommandPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofCommandPatternSample.java) | Encapsulates a request as an object, enabling undo/redo and history - adding an item to a shopping cart is wrapped as an undoable `Command`. |
| **Interpreter** | [`GofInterpreterPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofInterpreterPatternSample.java) | Given a small grammar, defines a representation for it plus an interpreter - a tiny pricing-rule "language" (amount, discount, surcharge) is built and evaluated as a tree of expression objects. |
| **Iterator** | [`GofIteratorPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofIteratorPatternSample.java) | Provides sequential access to an aggregate's elements without exposing its internal representation - a hand-rolled iterator walks a catalog, silently skipping out-of-stock products. |
| **Mediator** | [`GofMediatorPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofMediatorPatternSample.java) | Encapsulates how a set of objects interact, so they don't reference each other directly - a checkout form's fields all talk only to a central mediator, never to each other. |
| **Memento** | [`GofMementoPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofMementoPatternSample.java) | Captures and externalizes an object's internal state for later restoration, without breaking encapsulation - powers undo for a product draft editor. |
| **Observer** | [`GofObserverPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofObserverPatternSample.java) | Defines a one-to-many dependency so dependents are notified automatically on state change - email alerts and a dashboard both react to a product's price changing. |
| **State** | [`GofStatePatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofStatePatternSample.java) | Lets an object alter its behavior when its internal state changes, appearing to change its class - an `Order`'s legal actions (pay/ship/cancel) depend entirely on its current state object. |
| **Strategy** | [`GofStrategyPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofStrategyPatternSample.java) | Defines a family of interchangeable algorithms - regular, Black Friday, and clearance pricing strategies are swapped into the same `PriceCalculator` at runtime. |
| **Template Method** | [`GofTemplateMethodPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofTemplateMethodPatternSample.java) | Defines the skeleton of an algorithm in a base class, deferring specific steps to subclasses - a shared import workflow (validate -> parse -> save -> notify) with CSV/JSON-specific parsing steps. |
| **Visitor** | [`GofVisitorPatternSample`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/behavioral/GofVisitorPatternSample.java) | Represents an operation to perform on elements of an object structure, without changing their classes - separate visitors compute totals and generate shipping labels over the same catalog elements. |

---

## Design notes

- **`ProductDTO`** ([`dto/ProductDTO.java`](src/main/java/br/com/joaofelipefaria/arch/patterns/gof/dto/ProductDTO.java))
  is a plain `record(id, name, price)`, reused wherever a pattern naturally
  needs "a thing being created/decorated/observed/etc." Patterns where a
  generic product doesn't fit the pattern's own vocabulary (e.g. Singleton,
  Iterator, Mediator, Memento, State) use their own small, purpose-built
  types instead - forcing every sample to reuse the same DTO would have
  made a few of them feel contrived.
- Every supporting type (Handler, Strategy, Visitor, etc.) is a **nested
  static class/interface** inside the pattern's own file, per the "one class
  per pattern" requirement - this keeps each pattern's full structure
  readable in a single file instead of scattered across many small ones.
- These are **teaching samples**, optimized for clarity over production
  hardening (e.g. no null-checks beyond what's needed to make the pattern's
  point, no logging framework, `System.out` instead of SLF4J). Treat them as
  a reference for the pattern's *shape*, not as production-ready code to
  copy verbatim.
