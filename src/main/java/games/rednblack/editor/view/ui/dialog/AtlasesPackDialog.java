package games.rednblack.editor.view.ui.dialog;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.utils.ResourceGridAdapter;
import games.rednblack.editor.utils.ResourceListAdapter;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPane;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPaneListener;
import games.rednblack.puremvc.Facade;

import java.util.Set;

import static com.kotcrab.vis.ui.util.adapter.AbstractListAdapter.ListSelection.DEFAULT_KEY;

public class AtlasesPackDialog extends H2DDialog {

    private final ImageTabbedPane tabbedPane;
    private final String addNewNotification;
    private final String moveRegionNotification, updateCurrentNotification, removeNotification;
    private final String applyNotification;
    private final SimpleListAdapter<String> mainPackAdapter, currentPackAdapter;
    private final VisImageButton insertButton, removeButton;
    private final VisLabel currentSelectedPackLabel;
    private final VisLabel mainEmptyLabel, currentEmptyLabel;

    private final Facade facade = Facade.getInstance();

    private final Array<String> mainList = new Array<>();
    private final Array<String> currentList = new Array<>();

    private final ListView<String> mainPackList;
    private final ListView<String> currentPackList;

    public AtlasesPackDialog(String title, String add, String move, String updateList, String removeNotification, String applyNotification) {
        super(title);
        addNewNotification = add;
        moveRegionNotification = move;
        updateCurrentNotification = updateList;
        this.removeNotification = removeNotification;
        this.applyNotification = applyNotification;

        addCloseButton();
        closeOnEscape();
        getContentTable().top().left();

        tabbedPane = new ImageTabbedPane("chip") {
            @Override
            public boolean remove(ImageTab tab, boolean ignoreTabDirty) {
                H2DDialogs.showOptionDialog(PluginUIBridge.get().getSandbox().getUIStage(), "Remove Pack", "Are you sure to remove this pack?",
                        H2DDialogs.OptionDialogType.YES_NO_CANCEL, new OptionDialogAdapter() {
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

        // right side shows the pack contents as a thumbnail grid (image + name under it)
        currentPackAdapter = new ResourceGridAdapter(currentList);
        currentPackAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.MULTIPLE);
        currentPackAdapter.getSelectionManager().setProgrammaticChangeEvents(false);

        mainPackList = new ListView<>(mainPackAdapter);
        mainPackList.getScrollPane().addListener(new ScrollFocusListener());
        currentPackList = new ListView<>(currentPackAdapter);
        currentPackList.getScrollPane().addListener(new ScrollFocusListener());
        // grid must size to the viewport width (vertical scrolling only), otherwise the items table
        // takes its preferred width and the growX cells never expand to fill the column
        currentPackList.getScrollPane().setScrollingDisabled(true, false);
        currentPackList.getScrollPane().setFadeScrollBars(false);

        mainPackList.setItemClickListener(this::selectMainItem);
        currentPackList.setItemClickListener(this::selectCurrentItem);

        getContentTable().add(addNewPackTable).growX().padTop(5).row();
        getContentTable().add(tabbedPane.getTable()).height(30).growX().padTop(4).padBottom(4).row();

        // Move buttons: arrow icons + tooltips are clearer than bare ">" / "<". ">" moves the
        // selected main regions into the current pack; "<" moves them back to main.
        insertButton = new VisImageButton(VisUI.getSkin().getDrawable("arrow-right"));
        StandardWidgetsFactory.addTooltip(insertButton, "Move into pack");
        insertButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!insertButton.isDisabled())
                    facade.sendNotification(moveRegionNotification);
                AtlasesPackDialog.this.getStage().setKeyboardFocus(AtlasesPackDialog.this);
            }
        });
        removeButton = new VisImageButton(VisUI.getSkin().getDrawable("arrow-left"));
        StandardWidgetsFactory.addTooltip(removeButton, "Back to main");
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
        opButtons.add(insertButton).size(44).pad(2).row();
        opButtons.add(removeButton).size(44).pad(2).row();
        opButtonsContainer.add(opButtons);

        // Titled panels: a header strip over each list so the two columns read as labelled panels
        // rather than plain labels floating above gray boxes. Header uses the skin's "grey" (lighter
        // than the list's DARK_GRAY) for a subtle title-bar contrast.
        NinePatchDrawable headerBg = ((NinePatchDrawable) VisUI.getSkin().getDrawable("sticky-note"))
                .tint(VisUI.getSkin().getColor("grey"));
        NinePatchDrawable listBg = ((NinePatchDrawable) VisUI.getSkin().getDrawable("sticky-note"))
                .tint(Color.DARK_GRAY);

        VisTable mainHeader = new VisTable();
        mainHeader.background(headerBg);
        mainHeader.add(new VisLabel("Main Pack", Align.center)).growX().pad(4).padLeft(8).padRight(8);

        currentSelectedPackLabel = new VisLabel("Select Pack", Align.center);
        VisTable currentHeader = new VisTable();
        currentHeader.background(headerBg);
        currentHeader.add(currentSelectedPackLabel).growX().pad(4).padLeft(8).padRight(8);

        mainPackList.getMainTable().background(listBg);
        currentPackList.getMainTable().background(listBg);

        // Empty-state placeholders, layered over each list and toggled by refreshEmptyStates()
        mainEmptyLabel = new VisLabel("No regions", Align.center);
        mainEmptyLabel.setColor(new Color(1, 1, 1, 0.4f));
        mainEmptyLabel.setTouchable(Touchable.disabled);
        currentEmptyLabel = new VisLabel("Select a pack", Align.center);
        currentEmptyLabel.setColor(new Color(1, 1, 1, 0.4f));
        currentEmptyLabel.setTouchable(Touchable.disabled);
        Stack mainStack = new Stack();
        mainStack.add(mainPackList.getMainTable());
        mainStack.add(mainEmptyLabel);
        Stack currentStack = new Stack();
        currentStack.add(currentPackList.getMainTable());
        currentStack.add(currentEmptyLabel);

        VisTable opTable = new VisTable();
        opTable.add(mainHeader).uniformX().growX();
        opTable.add().width(50);
        opTable.add(currentHeader).uniformX().growX().row();
        opTable.add(mainStack).uniformX().grow();
        opTable.add(opButtonsContainer).growY();
        opTable.add(currentStack).uniformX().grow().row();

        getContentTable().add(opTable).grow().row();
        refreshEmptyStates();

        // Bottom bar: OK/Cancel/Apply, mirroring SettingsDialog. Edits are buffered by the mediator
        // in a copy of the packs VO, so Apply/OK send APPLY to commit (save + repack once); Cancel
        // and the X/Escape close paths just discard the copy (the live VO is never touched until
        // commit), so Cancel needs no notification.
        VisTextButton okButton = StandardWidgetsFactory.createTextButton("OK");
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                facade.sendNotification(applyNotification);
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
                facade.sendNotification(applyNotification);
            }
        });
        getButtonsTable().add(okButton).width(65).pad(2);
        getButtonsTable().add(cancelButton).width(65).pad(2);
        getButtonsTable().add(applyButton).width(65).pad(2);
        getCell(getButtonsTable()).right();

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
                        // grid: DOWN moves a whole row, not a single cell
                        if (lastSelected + ResourceGridAdapter.COLUMNS < currentPackAdapter.size()) {
                            String currentItem = currentPackAdapter.get(lastSelected);
                            String nextItem = currentPackAdapter.get(lastSelected + ResourceGridAdapter.COLUMNS);

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
                        // grid: UP moves a whole row, not a single cell
                        if (lastSelected - ResourceGridAdapter.COLUMNS >= 0) {
                            String currentItem = currentPackAdapter.get(lastSelected);
                            String nextItem = currentPackAdapter.get(lastSelected - ResourceGridAdapter.COLUMNS);

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
        refreshEmptyStates();
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
        refreshEmptyStates();
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
        refreshEmptyStates();
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
        boolean insertDisabled = tabbedPane.getActiveTab() == null || mainPackAdapter.getSelection().size == 0;
        boolean removeDisabled = tabbedPane.getActiveTab() == null || currentPackAdapter.getSelection().size == 0;
        insertButton.setDisabled(insertDisabled);
        removeButton.setDisabled(removeDisabled);
        // dim the whole button when disabled (no imageDisabled in the skin), so the inactive
        // state is obvious rather than identical to enabled
        insertButton.setColor(1f, 1f, 1f, insertDisabled ? 0.45f : 1f);
        removeButton.setColor(1f, 1f, 1f, removeDisabled ? 0.45f : 1f);
    }

    /** Toggles the faint empty-state labels over the two lists. */
    private void refreshEmptyStates() {
        mainEmptyLabel.setVisible(mainPackAdapter.size() == 0);
        boolean noSelection = tabbedPane.getActiveTab() == null;
        currentEmptyLabel.setVisible(currentPackAdapter.size() == 0);
        currentEmptyLabel.setText(noSelection ? "Select a pack" : "No regions");
    }

    public Array<String> getCurrentSelected() {
        return currentPackAdapter.getSelection();
    }

    public Array<String> getMainSelected() {
        return mainPackAdapter.getSelection();
    }

    @Override
    public float getPrefWidth() {
        return PluginUIBridge.get().getSandbox().getUIStage().getWidth() * 0.5f;
    }

    @Override
    public float getPrefHeight() {
        return PluginUIBridge.get().getSandbox().getUIStage().getHeight() * 0.5f;
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
