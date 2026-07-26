package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.BusyBar;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Shows a long job as a checklist of its phases, plus one bar for the run.
 * <p>
 * A job declares its phases with {@link #SET_PHASES} when it opens the dialog, walks them with
 * {@link #SET_PHASE}, and says what it is doing inside the current one with {@link #SET_DETAIL}.
 * Phases are appended, never replaced, so a repack can declare its own phase together with those of
 * the load that follows it and the checklist reads as a single run.
 * <p>
 * A job that declares nothing still gets the plain busy bar it used to.
 */
public class LoadingBarDialog extends H2DDialog {

    private static final String PREFIX = "games.rednblack.editor.view.ui.dialog.LoadingBarDialog";
    /** Body: {@code String[]}, the phases of the job, appended in order. */
    public static final String SET_PHASES = PREFIX + ".SET_PHASES";
    /** Body: {@code String}, the phase now running — the ones before it are marked done. */
    public static final String SET_PHASE = PREFIX + ".SET_PHASE";
    /** Body: {@code String}, what the current phase is working on. */
    public static final String SET_DETAIL = PREFIX + ".SET_DETAIL";
    /** Body: {@code Float} in {@code 0..1}. Sending it switches the dialog off the indeterminate bar. */
    public static final String SET_PROGRESS = PREFIX + ".SET_PROGRESS";
    /** No body. The run is over: every phase is ticked off and the bar filled. */
    public static final String SET_COMPLETE = PREFIX + ".SET_COMPLETE";

    private static final float BAR_ANIMATE_DURATION = 0.3f;
    private static final String DONE_MARKER = "icon-phase-done";
    private static final String CURRENT_MARKER = "icon-phase-current";
    private static final int MARKER_SIZE = 16;
    private static final int MARKER_GAP = 10;
    private static final int ROW_GAP = 4;

    private final VisTable listTable;
    private final Array<Phase> phases = new Array<>();
    private final VisLabel messageLabel;
    private final BusyBar busyBar;
    private final VisProgressBar progressBar;
    private final VisLabel percentLabel;

    private final Color doneColor = new Color(VisUI.getSkin().getColor("hyperlap2d-menuitem-grey"));
    private final Color currentColor = new Color(VisUI.getSkin().getColor("white"));
    private final Color pendingColor = new Color(VisUI.getSkin().getColor("hyperlap2d-menuitem-disabled-grey"));

    private int currentPhase = -1;

    public LoadingBarDialog() {
        super("Loading...");

        setFadeOutDelay(BAR_ANIMATE_DURATION);

        listTable = new VisTable();
        messageLabel = StandardWidgetsFactory.createLabel("");

        busyBar = new BusyBar();
        progressBar = new VisProgressBar(0, 1, 0.001f, false);
        progressBar.setAnimateDuration(BAR_ANIMATE_DURATION);

        // Both bars share a cell: the busy bar covers jobs that cannot say how far along they are.
        Stack bars = new Stack();
        bars.add(busyBar);
        bars.add(progressBar);

        percentLabel = StandardWidgetsFactory.createLabel("", "property-label", Align.right);

        VisTable barRow = new VisTable();
        barRow.add(bars).growX();
        barRow.add(percentLabel).width(42).padLeft(8).right();

        getContentTable().add(listTable).growX().padBottom(10).padTop(10).row();
        getContentTable().add(barRow).growX();

        reset();
    }

    /** Back to empty, so a reused dialog never opens on the last job's checklist. */
    public void reset() {
        phases.clear();
        currentPhase = -1;
        messageLabel.setText("");

        percentLabel.setText("");
        progressBar.setValue(0);
        progressBar.setVisible(false);
        busyBar.setVisible(true);

        refresh();
    }

    /** Adds the phases not on the checklist yet, in the order they arrive. */
    public void addPhases(String[] names) {
        boolean added = false;
        for (String name : names) {
            if (indexOf(name) >= 0) continue;
            phases.add(new Phase(name));
            added = true;
        }
        if (!added) return;

        refresh();
        resize();
    }

    public void setPhase(String name) {
        int index = indexOf(name);
        if (index < 0) {
            addPhases(new String[]{name});
            index = phases.size - 1;
        }
        if (index == currentPhase) return;

        currentPhase = index;
        refresh();
    }

    /**
     * Ticks every phase off and fills the bar. Called as the job ends, while the dialog is still on
     * its way out: the checklist is only ever complete during the fade.
     */
    public void setComplete() {
        currentPhase = phases.size;
        refresh();

        // Snapped, not animated: the dialog is closing and an animated bar would never reach the end.
        progressBar.setAnimateDuration(0f);
        setProgress(1f);
        progressBar.setAnimateDuration(BAR_ANIMATE_DURATION);
    }

    /** What the current phase is working on; it stays on the row once that phase is done. */
    public void setDetail(String detail) {
        String text = detail == null ? "" : detail;
        boolean onPhase = currentPhase >= 0 && currentPhase < phases.size;
        VisLabel target = onPhase ? phases.get(currentPhase).detailLabel : messageLabel;
        if (text.contentEquals(target.getText())) return;
        target.setText(text);
    }

    public void setProgress(float progress) {
        busyBar.setVisible(false);
        progressBar.setVisible(true);
        progressBar.setValue(progress);
        percentLabel.setText(Math.round(progress * 100) + "%");
    }

    /**
     * Lays the checklist out: done rows carry a tick, the running one a caret, the ones still to come
     * wait dimmed. Jobs that declared no phases get the plain message line instead.
     */
    private void refresh() {
        listTable.clear();

        if (phases.size == 0) {
            listTable.add(messageLabel).growX().left();
            return;
        }

        for (int i = 0; i < phases.size; i++) {
            Phase phase = phases.get(i);

            if (i < currentPhase) {
                phase.marker.setDrawable(VisUI.getSkin().getDrawable(DONE_MARKER));
                phase.nameLabel.setColor(doneColor);
            } else if (i == currentPhase) {
                phase.marker.setDrawable(VisUI.getSkin().getDrawable(CURRENT_MARKER));
                phase.nameLabel.setColor(currentColor);
            } else {
                phase.marker.setDrawable(null);
                phase.nameLabel.setColor(pendingColor);
            }

            listTable.add(phase.marker).size(MARKER_SIZE).padRight(MARKER_GAP);
            listTable.add(phase.nameLabel).growX().left();
            listTable.add(phase.detailLabel).right();
            listTable.row().padTop(ROW_GAP);
        }
    }

    /** A checklist that grew has to take its new size and stay centred on the stage. */
    private void resize() {
        invalidateHierarchy();
        pack();
        if (getStage() != null) centerWindow();
    }

    private int indexOf(String name) {
        for (int i = 0; i < phases.size; i++) {
            if (phases.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    @Override
    public float getPrefWidth() {
        return 360;
    }

    private static class Phase {
        final String name;
        final Image marker = new Image();
        final VisLabel nameLabel;
        final VisLabel detailLabel = StandardWidgetsFactory.createLabel("", "property-label", Align.right);

        Phase(String name) {
            this.name = name;
            this.nameLabel = StandardWidgetsFactory.createLabel(name);
            this.nameLabel.setAlignment(Align.left);
            // The tick is 14x14 but the caret is 6x11: drawn at their own size, not stretched to the cell.
            marker.setScaling(Scaling.none);
        }
    }
}
