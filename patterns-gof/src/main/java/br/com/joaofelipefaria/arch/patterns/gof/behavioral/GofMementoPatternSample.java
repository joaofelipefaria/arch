package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

import java.util.ArrayDeque;
import java.util.Deque;

import br.com.joaofelipefaria.arch.patterns.gof.dto.ProductDTO;

/**
 * <b>Memento</b> (Behavioral).
 * <p>
 * <b>Intent:</b> without violating encapsulation, capture and externalize an
 * object's internal state so it can be restored to that state later.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Originator} ({@link ProductDraftEditor}) - creates a
 *         memento containing a snapshot of its current internal state, and
 *         uses a memento to restore its state.</li>
 *     <li>{@code Memento} ({@link EditorSnapshot}) - stores the internal
 *         state of the Originator, but exposes nothing to any object other
 *         than the Originator (note it's a private nested record - the
 *         caretaker can hold it, but can't read or modify what's inside).</li>
 *     <li>{@code Caretaker} ({@link EditHistory}) - keeps track of mementos,
 *         but never inspects or operates on their contents.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> you need "undo" functionality for an object's state
 * (here, editing a draft product listing) without exposing that object's
 * internal fields to the code responsible for managing the undo history.
 */
public class GofMementoPatternSample {

    /** Memento: an opaque snapshot - private constructor, only the Originator can create/read it meaningfully. */
    static final class EditorSnapshot {
        private final ProductDTO state;

        private EditorSnapshot(ProductDTO state) {
            this.state = state;
        }
    }

    /** Originator: the object whose state we want to be able to save and restore. */
    static class ProductDraftEditor {
        private ProductDTO current;

        ProductDraftEditor(ProductDTO initial) {
            this.current = initial;
        }

        void edit(ProductDTO newState) {
            System.out.println("[Editor] Now editing: " + newState);
            this.current = newState;
        }

        ProductDTO current() {
            return current;
        }

        EditorSnapshot save() {
            return new EditorSnapshot(current);
        }

        void restore(EditorSnapshot snapshot) {
            this.current = snapshot.state;
            System.out.println("[Editor] Restored to: " + current);
        }
    }

    /** Caretaker: stores mementos over time, without ever looking inside them. */
    static class EditHistory {
        private final Deque<EditorSnapshot> snapshots = new ArrayDeque<>();

        void push(EditorSnapshot snapshot) {
            snapshots.push(snapshot);
        }

        EditorSnapshot popLast() {
            return snapshots.pop();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Memento Pattern ===");

        ProductDraftEditor editor = new ProductDraftEditor(new ProductDTO("P-1", "Draft Title", 0.0));
        EditHistory history = new EditHistory();

        history.push(editor.save()); // snapshot before first real edit
        editor.edit(new ProductDTO("P-1", "Wireless Keyboard", 79.90));

        history.push(editor.save()); // snapshot before second edit
        editor.edit(new ProductDTO("P-1", "Wireless Keyboard (Typo)", 7990.0)); // oops, wrong price

        System.out.println("Current (buggy) state: " + editor.current());

        editor.restore(history.popLast()); // undo the bad edit
        System.out.println("Current state after undo: " + editor.current());
    }
}
