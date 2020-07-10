package games.rednblack.editor.plugin.skincomposer;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.UIDraggablePanel;

public class DownloadingDialog extends UIDraggablePanel implements ProgressHandler {
    private VisLabel downloadingLabel;
    private VisProgressBar progressBar;

    public DownloadingDialog () {
        super("Skin Composer Plugin");
        setMovable(false);
        setModal(false);

        setHeight(100);
        setWidth(250);

        VisTable mainTable = new VisTable();
        add(mainTable).fill().expand();

        downloadingLabel = new VisLabel("Downloading info ...");
        mainTable.add(downloadingLabel).left();
        mainTable.row().padBottom(5);

        progressBar = new VisProgressBar(0, 100, 1, false);
        mainTable.add(progressBar).fillX().pad(5).width(240);
        mainTable.row().padBottom(5);

        pack();
    }

    public void setCurrentVersion(String name) {
        downloadingLabel.setText("Downloading "+ name + " ...");
    }

    @Override
    public void progressStarted() {

    }

    @Override
    public void progressChanged(float value) {
        progressBar.setValue(value);
    }

    @Override
    public void progressComplete() {
        close();
    }

    @Override
    public void progressFailed() {
        downloadingLabel.setText("Download failed!");
        addCloseButton();
    }
}
