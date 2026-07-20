package games.rednblack.editor.controller.commands;

import java.util.Set;

/**
 * Typed payload for {@link SetSelectionCommand}, replacing the polymorphic
 * {@code Integer|Set<Integer>|null} notification body (Phase 1 typed payloads).
 */
public sealed interface SelectionPayload {
    record Single(int entity) implements SelectionPayload {}
    record Multiple(Set<Integer> entities) implements SelectionPayload {}
    record Empty() implements SelectionPayload {}

    static SelectionPayload single(int entity) { return new Single(entity); }
    static SelectionPayload multiple(Set<Integer> entities) { return new Multiple(entities); }
    static SelectionPayload empty() { return new Empty(); }
}