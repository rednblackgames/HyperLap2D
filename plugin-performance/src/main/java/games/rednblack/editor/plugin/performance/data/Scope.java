package games.rednblack.editor.plugin.performance.data;

/**
 * Which part of the frame the panel is reporting on. The scene is what a game would pay for; the editor is
 * everything the editor adds around it and nobody ships.
 */
public enum Scope {
    RUNTIME("Runtime"),
    EDITOR("Editor"),
    BOTH("Both");

    public final String label;

    Scope(String label) {
        this.label = label;
    }
}
