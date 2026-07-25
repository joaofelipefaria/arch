# arch

A collection of small, self-contained projects demonstrating **software
architecture concepts** and **design patterns** in Java. Each subfolder is
an independent, runnable project - meant to be read, built, and executed on
its own, not as parts of a single deployable system.

The goal isn't feature-complete implementations, but clear, minimal
examples of how a given architectural piece or pattern actually works in
practice.

## Projects

| Project | Description |
|---|---|
| [`api-gateway`](./api-gateway) | A simple API Gateway built with **Spring Boot** and **Java 17**, showing the core mechanics of how an API gateway works (routing requests to downstream services). Intentionally simple - no advanced features (rate limiting, auth, circuit breakers, etc.) - just enough to demonstrate the concept clearly. |
| [`gof-patterns`](./gof-patterns) | Standalone Java (Maven) examples of all **23 classic Gang of Four design patterns**, organized by category (Creational, Structural, Behavioral). Each pattern is a single runnable class (`Gof<Pattern>PatternSample`) with its own `main` method and Javadoc explaining intent, participants, and when to use it. |

More projects will be added over time as new architectural concepts and
patterns are explored.

## Prerequisites

- Java 17+
- Maven 3.8+

Each project has its own `README.md` (and `pom.xml`/build instructions)
with specifics - start there once you're inside a given folder.

## Author

**João Felipe Faria**
Software Architect | Lead Software Engineer
