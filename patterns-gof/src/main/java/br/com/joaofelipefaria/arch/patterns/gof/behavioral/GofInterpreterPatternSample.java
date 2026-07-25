package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

/**
 * <b>Interpreter</b> (Behavioral).
 * <p>
 * <b>Intent:</b> given a language, define a representation for its grammar
 * along with an interpreter that uses the representation to interpret
 * sentences in the language.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code AbstractExpression} ({@link PriceExpression}) - declares an
 *         abstract {@code interpret()} operation common to every node in the
 *         grammar's abstract syntax tree.</li>
 *     <li>{@code TerminalExpression} ({@link Amount}) - implements
 *         interpret() for terminal symbols of the grammar (a literal number).</li>
 *     <li>{@code NonterminalExpression} ({@link Discount}, {@link Surcharge}) -
 *         implements interpret() for non-terminal grammar rules, typically
 *         by recursively interpreting its child expressions.</li>
 *     <li>{@code Client} ({@link #main}) - builds an abstract syntax tree
 *         representing a particular sentence in the language, then asks it
 *         to interpret itself.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you have a small, well-defined grammar to evaluate
 * repeatedly - here, a tiny pricing-rule "language" (base amount, percentage
 * discount, flat surcharge) built as a tree of expression objects instead of
 * as a one-off parser/string-eval function.
 */
public class GofInterpreterPatternSample {

    /** AbstractExpression: every node in the tree can interpret itself into a number. */
    interface PriceExpression {
        double interpret();
    }

    /** TerminalExpression: a literal numeric amount. */
    record Amount(double value) implements PriceExpression {
        @Override
        public double interpret() {
            return value;
        }
    }

    /** NonterminalExpression: applies a percentage discount on top of another expression. */
    record Discount(PriceExpression base, double percentage) implements PriceExpression {
        @Override
        public double interpret() {
            return base.interpret() * (1 - percentage / 100.0);
        }
    }

    /** NonterminalExpression: adds a flat surcharge on top of another expression. */
    record Surcharge(PriceExpression base, double flatAmount) implements PriceExpression {
        @Override
        public double interpret() {
            return base.interpret() + flatAmount;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Interpreter Pattern ===");

        // Represents the pricing "sentence": (100 with 10% discount) plus a $5 surcharge
        PriceExpression expression = new Surcharge(new Discount(new Amount(100), 10), 5);

        System.out.println("Result: $" + expression.interpret()); // (100 * 0.9) + 5 = 95.0
    }
}
