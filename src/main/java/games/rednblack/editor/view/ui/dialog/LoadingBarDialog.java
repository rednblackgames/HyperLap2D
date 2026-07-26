package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.kotcrab.vis.ui.widget.BusyBar;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class LoadingBarDialog extends H2DDialog {

    private static final String PREFIX = "games.rednblack.editor.view.ui.dialog.LoadingBarDialog";
    /** Body: {@code String}, what is being loaded right now. */
    public static final String SET_MESSAGE = PREFIX + ".SET_MESSAGE";
    /** Body: {@code Float} in {@code 0..1}. Sending it switches the dialog off the indeterminate bar. */
    public static final String SET_PROGRESS = PREFIX + ".SET_PROGRESS";

    private final VisLabel messageLabel;
    private final BusyBar busyBar;
    private final VisProgressBar progressBar;

    public LoadingBarDialog() {
        super("Loading...");

        messageLabel = StandardWidgetsFactory.createLabel("");
        busyBar = new BusyBar();
        progressBar = new VisProgressBar(0, 1, 0.001f, false);
        progressBar.setAnimateDuration(0.3f);

        // Both bars share a cell: the busy bar covers jobs that cannot say how far along they are.
        Stack bars = new Stack();
        bars.add(busyBar);
        bars.add(progressBar);

        getContentTable().add(messageLabel).growX().left().row();
        getContentTable().add(bars).padTop(5).growX();

        reset();
    }

    /** Back to indeterminate and empty, so a reused dialog never opens on the last job's state. */
    public void reset() {
        messageLabel.setText("");
        progressBar.setValue(0);
        progressBar.setVisible(false);
        busyBar.setVisible(true);
    }

    public void setMessage(String message) {
        messageLabel.setText(message == null ? "" : message);
    }

    public void setProgress(float progress) {
        busyBar.setVisible(false);
        progressBar.setVisible(true);
        progressBar.setValue(progress);
    }

    @Override
    public float getPrefWidth() {
        return 250;
    }
}
