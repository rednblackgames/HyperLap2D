package games.rednblack.editor.view.ui.dialog;

import com.kotcrab.vis.ui.widget.BusyBar;
import games.rednblack.h2d.common.H2DDialog;

public class LoadingBarDialog extends H2DDialog {

    public LoadingBarDialog() {
        super("Loading...");

        BusyBar progressBar = new BusyBar();
        getContentTable().add(progressBar).padTop(5).growX();
    }

    @Override
    public float getPrefWidth() {
        return 250;
    }
}
