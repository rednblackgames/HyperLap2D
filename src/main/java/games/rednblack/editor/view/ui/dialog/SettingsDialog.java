package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.h2d.common.H2DDialog;

public class SettingsDialog extends H2DDialog {

    SettingsDialog() {
        super("Settings");

        addCloseButton();
        setModal(true);
        setResizable(true);
        closeOnEscape();

        VisTable containerTable = new VisTable();
        VisTree<SettingsNode, SettingsNodeValue> tree = new VisTree<>();
        VisSplitPane splitPane = new VisSplitPane(tree, containerTable, false);
        splitPane.setMinSplitAmount(0.28f);
        splitPane.setMaxSplitAmount(0.35f);
        splitPane.setSplitAmount(0.3f);

        getContentTable().add(splitPane).expand().fill().padTop(5);

        VisTextButton okButton = StandardWidgetsFactory.createTextButton("OK");
        VisTextButton cancelButton = StandardWidgetsFactory.createTextButton("Cancel");
        VisTextButton applyButton = StandardWidgetsFactory.createTextButton("Apply");

        getButtonsTable().add(okButton).width(65).pad(2).right();
        getButtonsTable().add(cancelButton).width(65).pad(2).right();
        getButtonsTable().add(applyButton).width(65).pad(2).right();
        getCell(getButtonsTable()).right();
    }

    @Override
    public float getPrefWidth() {
        return 700;
    }

    @Override
    public float getPrefHeight() {
        return 500;
    }

    public static class SettingsNode extends Tree.Node<SettingsNode, SettingsNodeValue, VisLabel> {

        public SettingsNode(VisLabel actor) {
            super(actor);
        }
    }

    public static class SettingsNodeValue {

    }
}
