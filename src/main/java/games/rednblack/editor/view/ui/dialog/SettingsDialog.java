package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.Main;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.editor.view.ui.widget.SettingsTree;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class SettingsDialog extends H2DDialog {

    private static final String prefix = "games.rednblack.editor.view.ui.dialog.SettingsDialog";
    public static final String ADD_SETTINGS = prefix + ".ADD_SETTINGS";

    /** Settings category name -> atlas region used as its tree icon. */
    private static final ObjectMap<String, String> SETTINGS_ICONS = new ObjectMap<>();
    static {
        SETTINGS_ICONS.put("General", "icon-settings-general");
        SETTINGS_ICONS.put("Sandbox", "icon-settings-sandbox");
        SETTINGS_ICONS.put("Plugins", "icon-settings-plugins");
        SETTINGS_ICONS.put("Live Preview", "icon-settings-live-preview");
        SETTINGS_ICONS.put("Project Export", "icon-settings-export");
    }

    private final SettingsTree settingsTree;

    private static final float TRANSITION_TIME = 0.1f;

    SettingsDialog() {
        super("Settings");

        addCloseButton();
        setModal(true);
        closeOnEscape();

        VisTable containerTable = new VisTable();
        VisScrollPane containerScrollPane = StandardWidgetsFactory.createScrollPane(containerTable);
        settingsTree = new SettingsTree();
        VisScrollPane treeScrollPane = StandardWidgetsFactory.createScrollPane(settingsTree);

        VisSplitPane splitPane = new VisSplitPane(treeScrollPane, containerScrollPane, false);
        splitPane.setMinSplitAmount(0.28f);
        splitPane.setMaxSplitAmount(0.35f);
        splitPane.setSplitAmount(0.3f);

        settingsTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsTree.getSelectedNode().setExpanded(true);
                settingsTree.getSelectedValue().translateSettingsToView();

                if (containerTable.getChildren().size > 0) {
                    Actor oldContent = containerTable.getChild(0);
                    oldContent.clearActions();
                    oldContent.addAction(Actions.sequence(
                            Actions.alpha(0, TRANSITION_TIME),
                            Actions.run(() -> {
                                containerTable.clear();
                                Actor newContent = settingsTree.getSelectedValue().getContentTable();
                                newContent.clearActions();
                                newContent.getColor().a = 0;
                                newContent.addAction(Actions.alpha(1, TRANSITION_TIME));
                                containerTable.add(newContent).expand().fill().pad(5);
                            })
                    ));
                } else {
                    Actor newContent = settingsTree.getSelectedValue().getContentTable();
                    newContent.clearActions();
                    newContent.getColor().a = 0;
                    newContent.addAction(Actions.alpha(1, TRANSITION_TIME));
                    containerTable.add(newContent).expand().fill().pad(5);
                }
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
        boolean askToRestart = false;
        for (SettingsNode node : settingsTree.getRootNodes()) {
            if (node.getValue().validateSettings()) {
                if (node.getValue().requireRestart()) askToRestart = true;
                node.getValue().translateViewToSettings();
            }
            if (node.getChildren().size > 0) {
                for (SettingsNode child : node.getChildren()) {
                    if (child.getValue().validateSettings()) {
                        if (child.getValue().requireRestart()) askToRestart = true;
                        child.getValue().translateViewToSettings();
                    }
                }
            }
        }
        if (askToRestart) {
            H2DDialogs.showConfirmDialog(getStage(), "Restart is required", "Some changes needs to restart the editor,\nwould you like to do it now?",
                    new String[]{"Yes, restart now", "Cancel"}, new Integer[]{0, 1}, r -> {
                        if (r == 0) {
                            Main.restartJVM();
                        }
                    }).padBottom(20).pack();
        }
    }

    @Override
    public VisDialog show(Stage stage) {
        super.show(stage);
        if (settingsTree.getSelection().size() == 0 && settingsTree.getRootNodes().size > 0) {
            settingsTree.getSelection().add(settingsTree.getRootNodes().first());
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
        int existingIndex = settingsTree.getRootNodes().indexOf(node, false);
        if (existingIndex == -1) {
            node.setValue(nodeValue);
            applyNodeIcon(node, nodeValue.getName());
            settingsTree.add(node);
        } else {
            settingsTree.getRootNodes().get(existingIndex).setValue(nodeValue);
        }
        return node;
    }

    public SettingsNode addChildSettingsNode(SettingsNode parent, SettingsNodeValue<?> nodeValue) {
        SettingsNode node = new SettingsNode(nodeValue.getName());
        int existingIndex = parent.getChildren().indexOf(node, false);
        if (existingIndex == -1) {
            node.setValue(nodeValue);
            applyNodeIcon(node, nodeValue.getName());
            parent.add(node);
        } else {
            parent.getChildren().get(existingIndex).setValue(nodeValue);
        }
        return node;
    }

    /**
     * Gives a settings node its category icon. Plugin-contributed nodes have arbitrary names and
     * simply get no icon, so a missing mapping is not an error.
     */
    private void applyNodeIcon(SettingsNode node, String name) {
        String region = SETTINGS_ICONS.get(name);
        if (region == null) return;
        Skin skin = VisUI.getSkin();
        if (!skin.has(region, Drawable.class) && skin.getAtlas().findRegion(region) == null) return;
        node.setIcon(skin.getDrawable(region));
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
