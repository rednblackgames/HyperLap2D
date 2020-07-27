package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import com.puremvc.patterns.facade.Facade;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.SettingsNodeValue;

public class SettingsDialog extends H2DDialog {

    private static final String prefix = "games.rednblack.editor.view.ui.dialog.SettingsDialog";
    public static final String ADD_SETTINGS = prefix + ".ADD_SETTINGS";

    private final VisTree<SettingsNode, SettingsNodeValue<?>> settingsTree;

    SettingsDialog() {
        super("Settings");

        addCloseButton();
        setModal(true);
        closeOnEscape();

        VisTable containerTable = new VisTable();
        VisScrollPane containerScrollPane = StandardWidgetsFactory.createScrollPane(containerTable);
        settingsTree = new VisTree<>();
        VisScrollPane treeScrollPane = StandardWidgetsFactory.createScrollPane(settingsTree);

        VisSplitPane splitPane = new VisSplitPane(treeScrollPane, containerScrollPane, false);
        splitPane.setMinSplitAmount(0.28f);
        splitPane.setMaxSplitAmount(0.35f);
        splitPane.setSplitAmount(0.3f);

        settingsTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                containerTable.clear();
                settingsTree.getSelectedValue().translateSettingsToView();
                containerTable.add(settingsTree.getSelectedValue().getContentTable()).expand().fill().pad(5);
            }
        });

        getContentTable().add(splitPane).expand().fill().padTop(5);

        VisTextButton okButton = StandardWidgetsFactory.createTextButton("OK");
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                applyAllSettings();
                close();
            }
        });
        VisTextButton cancelButton = StandardWidgetsFactory.createTextButton("Cancel");
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                close();
            }
        });
        VisTextButton applyButton = StandardWidgetsFactory.createTextButton("Apply");
        applyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                applyAllSettings();
            }
        });

        getButtonsTable().add(okButton).width(65).pad(2).right();
        getButtonsTable().add(cancelButton).width(65).pad(2).right();
        getButtonsTable().add(applyButton).width(65).pad(2).right();
        getCell(getButtonsTable()).right();
    }

    private void applyAllSettings() {
        for (SettingsNode node : settingsTree.getNodes()) {
            if (node.getValue().validateSettings()) {
                node.getValue().translateViewToSettings();
            }
            if (node.getChildren().size > 0) {
                for (SettingsNode child : node.getChildren()) {
                    if (child.getValue().validateSettings()) {
                        child.getValue().translateViewToSettings();
                    }
                }
            }
        }
    }

    @Override
    public VisDialog show(Stage stage) {
        super.show(stage);
        if (settingsTree.getSelection().size() == 0 && settingsTree.getNodes().size > 0) {
            settingsTree.getSelection().add(settingsTree.getNodes().first());
        }

        if (settingsTree.getSelection().size() > 0) {
            settingsTree.getSelectedValue().translateSettingsToView();
        }
        return this;
    }

    @Override
    public float getPrefWidth() {
        return 700;
    }

    @Override
    public float getPrefHeight() {
        return 500;
    }

    public SettingsNode addSettingsNode(SettingsNodeValue<?> nodeValue) {
        SettingsNode node = new SettingsNode(nodeValue.getName());
        int existingIndex = settingsTree.getNodes().indexOf(node, false);
        if (existingIndex == -1) {
            node.setValue(nodeValue);
            settingsTree.add(node);
        } else {
            settingsTree.getNodes().get(existingIndex).setValue(nodeValue);
        }
        return node;
    }

    public SettingsNode addChildSettingsNode(SettingsNode parent, SettingsNodeValue<?> nodeValue) {
        SettingsNode node = new SettingsNode(nodeValue.getName());
        int existingIndex = parent.getChildren().indexOf(node, false);
        if (existingIndex == -1) {
            node.setValue(nodeValue);
            parent.add(node);
        } else {
            parent.getChildren().get(existingIndex).setValue(nodeValue);
        }
        return node;
    }

    public static class SettingsNode extends Tree.Node<SettingsNode, SettingsNodeValue<?>, VisLabel> {
        private final String name;
        public SettingsNode(String name) {
            super(new VisLabel(name));
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SettingsNode)) {
                return false;
            }
            return name.equals(((SettingsNode) o).name);
        }
    }
}
