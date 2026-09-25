package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.plugin.performance.data.Fmt;
import games.rednblack.editor.plugin.performance.data.PerformanceModel;
import games.rednblack.editor.renderer.utils.profiling.SystemProfiler;

/**
 * What the scene's time went on, system by system, worst first.
 * <p>
 * The rows are display slots rather than systems: sorting swaps what a slot shows, so the list can reorder
 * every few frames without building a single object. Systems the editor puts in place of the ones a game
 * would run are marked, because their cost is the editor's, not the game's.
 */
public class SystemBreakdown extends VisTable {

    /** Swapped in by the editor for its own version, or added by it outright. */
    private static final String[] EDITOR_SUBSTITUTED = {
            "PhysicsAdjustSystem",
            "ParticleContinuousSystem",
            "TalosContinuousSystem",
            "TalosAnchorConstraintSystem",
            "EditorButtonSystem",
            "EditorSliderSystem",
            "EditorScrollPaneSystem",
            "EditorTextFieldSystem",
            "EngineSerializationManager",
            "HyperLap2dRendererMiniMap"
    };

    private static final int SORT_INTERVAL = 10;

    private final PerformanceModel model;
    private final Array<Row> rows = new Array<>();
    private final StringBuilder text = new StringBuilder(32);

    private VisScrollPane scrollPane;
    private int[] order = new int[0];
    private int described = -1;
    private int sinceSort;

    public SystemBreakdown(PerformanceModel model) {
        this.model = model;
        defaults().growX();
        //the scroll bar sits over the right edge of the viewport, and the numbers must clear it
        padRight(14f);
    }

    /** Told about its own pane so a new list starts at the top, where the expensive systems are. */
    public void setScrollPane(VisScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public void update() {
        SystemProfiler systems = model.systemProfiler();
        if (systems == null) return;

        if (described != systems.systemCount()) rebuild(systems);
        if (rows.size == 0) return;

        if (sinceSort-- <= 0) {
            sinceSort = SORT_INTERVAL;
            sort(systems);
        }

        float total = Math.max(0.05f, systems.totalAverageMillis());

        for (int slot = 0; slot < order.length; slot++) {
            rows.get(slot).show(systems, order[slot], total, text);
        }

        rows.peek().showSync(systems, total, text);
    }

    private void rebuild(SystemProfiler systems) {
        described = systems.systemCount();
        clearChildren();
        rows.clear();

        order = new int[described];
        for (int i = 0; i < described; i++) {
            order[i] = i;

            Row row = new Row();
            rows.add(row);
            add(row).growX().row();
        }

        //the last row is never a system: it is what keeping the subscriptions in step costs
        Row sync = new Row();
        rows.add(sync);
        add(sync).growX().padTop(3f).row();

        if (scrollPane != null) {
            scrollPane.setScrollY(0f);
            scrollPane.updateVisualScroll();
        }
    }

    private void sort(SystemProfiler systems) {
        for (int i = 1; i < order.length; i++) {
            int value = order[i];
            float millis = systems.averageMillis(value);
            int j = i - 1;
            while (j >= 0 && systems.averageMillis(order[j]) < millis) {
                order[j + 1] = order[j];
                j--;
            }
            order[j + 1] = value;
        }
    }

    private static boolean isEditorSubstituted(String name) {
        for (String substituted : EDITOR_SUBSTITUTED) {
            if (name.startsWith(substituted)) return true;
        }
        return false;
    }

    /** One line of the list. What it shows is decided every frame; what it is made of never changes. */
    private static class Row extends VisTable {

        private final VisLabel name = Labels.fitted("small");
        private final VisLabel note = new VisLabel("", "small");
        private final VisLabel value = new VisLabel("", "small");
        private final Bar bar = new Bar();
        private boolean idle;

        Row() {
            //the gap belongs to the row, so a row that collapses takes its gap with it
            padBottom(1f);
            name.setColor(Palette.TEXT);
            note.setColor(Palette.TEXT_MUTED);
            value.setColor(Palette.TEXT_MUTED);
            value.setAlignment(Align.right);

            //name and bar split what is left over between them, half each. Both ask for the same width to
            //begin with - neither sizes itself to its text - so the split comes out identical on every row
            //and the bars still start in the same place down the list.
            add(name).left().growX().minWidth(80f);
            add(note).left().width(26f).padLeft(4f);
            add(bar).growX().minWidth(80f).height(8f).padLeft(4f).padRight(8f);
            add(value).right().width(46f);
        }

        /** A system with nothing to do takes no room: an empty list of zeroes is worse than a short list. */
        @Override
        public float getPrefHeight() {
            return idle ? 0f : super.getPrefHeight();
        }

        void show(SystemProfiler systems, int system, float total, StringBuilder text) {
            float millis = systems.averageMillis(system);
            int runs = systems.runs(system);

            //on how often it runs, not on whether it ran this frame: a fixed timestep system skips frames,
            //and hiding it on those made the row blink in and out
            setIdle(millis < 0.002f && systems.averageRuns(system) < 0.01f);
            if (idle) return;

            name.setText(systems.name(system));
            name.setColor(isEditorSubstituted(systems.name(system)) ? Palette.TEXT_MUTED : Palette.TEXT);

            Fmt.clear(text);
            if (isEditorSubstituted(systems.name(system))) {
                text.append("ed.");
            } else if (runs > 1) {
                text.append('x').append(runs);
            }
            note.setText(text);

            bar.set(millis / total, Palette.BUCKET[systems.bucket(system)]);

            Fmt.clear(text);
            Fmt.number(text, millis, 2);
            value.setText(text);
        }

        private void setIdle(boolean value) {
            if (idle == value) return;

            idle = value;
            setVisible(!value);
            invalidateHierarchy();
        }

        void showSync(SystemProfiler systems, float total, StringBuilder text) {
            setIdle(false);
            name.setText("Entity state sync");
            name.setColor(Palette.TEXT_MUTED);
            note.setText("");

            bar.set(systems.syncAverageMillis() / total, Palette.OTHER);

            Fmt.clear(text);
            Fmt.number(text, systems.syncAverageMillis(), 2);
            value.setText(text);
        }
    }
}
