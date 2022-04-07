package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.ResourceListAdapter;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPane;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPaneListener;

import java.util.Set;

import static com.kotcrab.vis.ui.util.adapter.AbstractListAdapter.ListSelection.DEFAULT_KEY;

public class AtlasesPackDialog extends H2DDialog {

    private final ImageTabbedPane tabbedPane;
    private final String addNewNotification;
    private final String moveRegionNotification, updateCurrentNotification, removeNotification;
    private final SimpleListAdapter<String> mainPackAdapter, currentPackAdapter;
    private final VisTextButton insertButton, removeButton;
    private final VisLabel currentSelectedPackLabel;

    private final HyperLap2DFacade facade = HyperLap2DFacade.getInstance();

    private final Array<String> mainList = new Array<>();
    private final Array<String> currentList = new Array<>();

    private final ListView<String> mainPackList;
    private final ListView<String> currentPackList;

    public AtlasesPackDialog(String title, String add, String move, String updateList, String removeNotification) {
        super(title);
        addNewNotification = add;
        moveRegionNotification = move;
        updateCurrentNotification = updateList;
        this.removeNotification = removeNotification;

        addCloseButton();
        getContentTable().top().left();

        tabbedPane = new ImageTabbedPane("chip") {
            @Override
            public boolean remove(ImageTab tab, boolean ignoreTabDirty) {
                Dialogs.showOptionDialog(Sandbox.getInstance().getUIStage(), "Remove Pack", "Are you sure to remove this pack?",
                        Dialogs.OptionDialogType.YES_NO_CANCEL, new OptionDialogAdapter() {
                            @Override
                            public void yes () {
                                facade.sendNotification(removeNotification, ((PackTab) tab).getName());
                                packRemove(tab, ignoreTabDirty);
                            }

                            @Override
                            public void no () {

                            }
                        });
                return false;
            }

            private boolean packRemove(ImageTab tab, boolean ignoreTabDirty) {
                return super.remove(tab, ignoreTabDirty);
            }
        };
        tabbedPane.addListener(new ImageTabbedPaneListener() {
            @Override
            public void switchedTab(ImageTab tab) {
                facade.sendNotification(updateCurrentNotification);
                updateOpButtons();
                currentSelectedPackLabel.setText(((PackTab) tab).getName());
            }

            @Override
            public void removedTab(ImageTab tab) {

            }

            @Override
            public void removedAllTabs() {

            }
        });

        VisTable addNewPackTable = new VisTable();
        VisTextField newPackName = StandardWidgetsFactory.createTextField();
        newPackName.setMessageText("Add new atlas pack");
        VisTextButton newPackButton = StandardWidgetsFactory.createTextButton("Add");
        newPackButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!newPackName.getText().equals(""))
                    facade.sendNotification(addNewNotification, newPackName.getText());
                newPackName.setText("");
            }
        });
        addNewPackTable.add(newPackName).growX().padRight(5);
        addNewPackTable.add(newPackButton).width(80);

        mainPackAdapter = new ResourceListAdapter(mainList);
        mainPackAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.MULTIPLE);
        mainPackAdapter.getSelectionManager().setProgrammaticChangeEvents(false);

        currentPackAdapter = new ResourceListAdapter(currentList);
        currentPackAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.MULTIPLE);
        currentPackAdapter.getSelectionManager().setProgrammaticChangeEvents(false);

        mainPackList = new ListView<>(mainPackAdapter);
        mainPackList.getScrollPane().addListener(new ScrollFocusListener());
        currentPackList = new ListView<>(currentPackAdapter);
        currentPackList.getScrollPane().addListener(new ScrollFocusListener());

        mainPackList.setItemClickListener(this::selectMainItem);
        currentPackList.setItemClickListener(this::selectCurrentItem);

        getContentTable().add(addNewPackTable).growX().padTop(5).row();
        getContentTable().add(tabbedPane.getTable()).height(30).growX().padTop(4).padBottom(4).row();

        insertButton = StandardWidgetsFactory.createTextButton(">");
        insertButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!insertButton.isDisabled())
                    facade.sendNotification(moveRegionNotification);
                AtlasesPackDialog.this.getStage().setKeyboardFocus(AtlasesPackDialog.this);
            }
        });
        removeButton = StandardWidgetsFactory.createTextButton("<");
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!removeButton.isDisabled())
                    facade.sendNotification(moveRegionNotification);
                AtlasesPackDialog.this.getStage().setKeyboardFocus(AtlasesPackDialog.this);
            }
        });
        updateOpButtons();
        VisTable opButtonsContainer = new VisTable();
        VisTable opButtons = new VisTable();
        opButtons.add(insertButton).width(80).row();
        opButtons.add(removeButton).width(80).row();
        opButtonsContainer.add(opButtons);

        VisTable opTable = new VisTable();
        opTable.add(new VisLabel("Main Pack", Align.center)).uniformX().growX();
        opTable.add().width(80);
        currentSelectedPackLabel = new VisLabel("Select Pack", Align.center);
        opTable.add(currentSelectedPackLabel).uniformX().growX().row();
        NinePatchDrawable bg = ((NinePatchDrawable) VisUI.getSkin().getDrawable("sticky-note")).tint(Color.DARK_GRAY);
        mainPackList.getMainTable().background(bg);
        opTable.add(mainPackList.getMainTable()).uniformX().grow();
        opTable.add(opButtonsContainer).growY();
        currentPackList.getMainTable().background(bg);
        opTable.add(currentPackList.getMainTable()).uniformX().grow().row();

        getContentTable().add(opTable).grow().row();

        addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.DOWN) {
                    if (mainPackAdapter.getSelection().size > 0) {
                        int lastSelected = mainPackAdapter.indexOf(mainPackAdapter.getSelection().get(mainPackAdapter.getSelection().size - 1));
                        if (lastSelected + 1 < mainPackAdapter.size()) {
                            String currentItem = mainPackAdapter.get(lastSelected);
                            String nextItem = mainPackAdapter.get(lastSelected + 1);

                            if (!isGroupMultiSelectKeyPressed(mainPackAdapter))
                                mainPackAdapter.getSelectionManager().deselectAll();

                            if (mainPackAdapter.getSelectionManager().getSelection().contains(nextItem, false)) {
                                mainPackAdapter.getSelectionManager().deselect(currentItem);
                            } else {
                                mainPackAdapter.getSelectionManager().select(nextItem);
                            }

                            Actor item = mainPackAdapter.getView(nextItem);
                            mainPackList.getScrollPane().scrollTo(0, item.getY(), item.getWidth(), item.getHeight());
                        }
                    } else if (currentPackAdapter.getSelection().size > 0) {
                        int lastSelected = currentPackAdapter.indexOf(currentPackAdapter.getSelection().get(currentPackAdapter.getSelection().size - 1));
                        if (lastSelected + 1 < currentPackAdapter.size()) {
                            String currentItem = currentPackAdapter.get(lastSelected);
                            String nextItem = currentPackAdapter.get(lastSelected + 1);

                            if (!isGroupMultiSelectKeyPressed(currentPackAdapter))
                                currentPackAdapter.getSelectionManager().deselectAll();

                            if (currentPackAdapter.getSelectionManager().getSelection().contains(nextItem, false)) {
                                currentPackAdapter.getSelectionManager().deselect(currentItem);
                            } else {
                                currentPackAdapter.getSelectionManager().select(nextItem);
                            }

                            Actor item = currentPackAdapter.getView(nextItem);
                            currentPackList.getScrollPane().scrollTo(0, item.getY(), item.getWidth(), item.getHeight());
                        }
                    }
                    return true;
                }

                if (keycode == Input.Keys.UP) {
                    if (mainPackAdapter.getSelection().size > 0) {
                        int lastSelected = mainPackAdapter.indexOf(mainPackAdapter.getSelection().get(mainPackAdapter.getSelection().size - 1));
                        if (lastSelected - 1 >= 0) {
                            String currentItem = mainPackAdapter.get(lastSelected);
                            String nextItem = mainPackAdapter.get(lastSelected - 1);

                            if (!isGroupMultiSelectKeyPressed(mainPackAdapter))
                                mainPackAdapter.getSelectionManager().deselectAll();

                            if (mainPackAdapter.getSelectionManager().getSelection().contains(nextItem, false)) {
                                mainPackAdapter.getSelectionManager().deselect(currentItem);
                            } else {
                                mainPackAdapter.getSelectionManager().select(nextItem);
                            }

                            Actor item = mainPackAdapter.getView(nextItem);
                            mainPackList.getScrollPane().scrollTo(0, item.getY(), item.getWidth(), item.getHeight());
                        }
                    } else if (currentPackAdapter.getSelection().size > 0) {
                        int lastSelected = currentPackAdapter.indexOf(currentPackAdapter.getSelection().get(currentPackAdapter.getSelection().size - 1));
                        if (lastSelected - 1 >= 0) {
                            String currentItem = currentPackAdapter.get(lastSelected);
                            String nextItem = currentPackAdapter.get(lastSelected - 1);

                            if (!isGroupMultiSelectKeyPressed(currentPackAdapter))
                                currentPackAdapter.getSelectionManager().deselectAll();

                            if (currentPackAdapter.getSelectionManager().getSelection().contains(nextItem, false)) {
                                currentPackAdapter.getSelectionManager().deselect(currentItem);
                            } else {
                                currentPackAdapter.getSelectionManager().select(nextItem);
                            }

                            Actor item = currentPackAdapter.getView(nextItem);
                            currentPackList.getScrollPane().scrollTo(0, item.getY(), item.getWidth(), item.getHeight());
                        }
                    }
                    return true;
                }

                return false;
            }
        });
    }

    private void selectCurrentItem(String item) {
        mainPackAdapter.getSelectionManager().deselectAll();
        updateOpButtons();
    }

    private void selectMainItem(String item) {
        currentPackAdapter.getSelectionManager().deselectAll();
        updateOpButtons();
    }

    public void initPacks(Set<String> packs) {
        tabbedPane.removeAll();

        for (String name : packs) {
            if (name.equals("main")) continue;
            tabbedPane.add(new PackTab(name));
        }

        if (tabbedPane.getTabs().size > 0) tabbedPane.switchTab(0);
    }

    public void clearCurrentPack() {
        currentList.clear();
        currentPackAdapter.itemsChanged();
        currentSelectedPackLabel.setText("Select Pack");
    }

    public void updateCurrentPack(Set<String> regions) {
        String toSelect = null;
        if (currentPackAdapter.getSelection().size > 0) {
            String selected = currentPackAdapter.getSelection().get(0);
            int nextIndex = currentList.indexOf(selected, false);
            if (nextIndex + 1 < currentList.size)
                toSelect = currentList.get(nextIndex + 1);

        }
        currentList.clear();
        for (String item : regions)
            currentList.add(item);
        currentList.sort();
        if (currentPackAdapter.getSelection().size > 0) {
            selectCurrentItem(currentList.contains(toSelect, false) ? toSelect : null);
        }

        currentPackAdapter.itemsChanged();
        if (toSelect != null && currentList.contains(toSelect, false)) {
            currentPackAdapter.getSelectionManager().select(toSelect);
        }
    }

    public void updateMainPack(Set<String> regions) {
        String toSelect = null;
        if (mainPackAdapter.getSelection().size > 0) {
            String selected = mainPackAdapter.getSelection().get(0);
            int nextIndex = mainList.indexOf(selected, false);
            if (nextIndex + 1 < mainList.size)
                toSelect = mainList.get(nextIndex + 1);
            selectMainItem(toSelect);
        }
        mainList.clear();
        for (String item : regions)
            mainList.add(item);
        mainList.sort();

        mainPackAdapter.itemsChanged();
        if (toSelect != null) {
            mainPackAdapter.getView(toSelect);
            mainPackAdapter.getSelectionManager().select(toSelect);
        }
    }

    public void addNewPack(String name) {
        tabbedPane.add(new PackTab(name));
        pack();
    }

    public String getSelectedTab() {
        if (tabbedPane.getActiveTab() == null)
            return null;
        return ((PackTab) tabbedPane.getActiveTab()).getName();
    }

    private void updateOpButtons() {
       insertButton.setDisabled(tabbedPane.getActiveTab() == null || mainPackAdapter.getSelection().size == 0);
       removeButton.setDisabled(tabbedPane.getActiveTab() == null || currentPackAdapter.getSelection().size == 0);
    }

    public Array<String> getCurrentSelected() {
        return currentPackAdapter.getSelection();
    }

    public Array<String> getMainSelected() {
        return mainPackAdapter.getSelection();
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth() * 0.5f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.5f;
    }

    public static class PackTab extends ImageTab {
        String name;
        public PackTab (String name) {
            super(false, true);
            this.name = name;
        }

        @Override
        public String getTabTitle() {
            return name + " ";
        }

        public String getName() {
            return name;
        }

        @Override
        public String getTabIconStyle() {
            return null;
        }

        @Override
        public String getCloseButtonStyle() {
            return "close-chip-tab";
        }

        @Override
        public Table getContentTable() {
            return null;
        }
    }

    private boolean isGroupMultiSelectKeyPressed(AbstractListAdapter adapter) {
        if (adapter.getSelectionManager().getGroupMultiSelectKey() == DEFAULT_KEY)
            return UIUtils.shift();
        else
            return Gdx.input.isKeyPressed(adapter.getSelectionManager().getGroupMultiSelectKey());
    }
}
