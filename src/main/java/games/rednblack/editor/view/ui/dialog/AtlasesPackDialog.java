package games.rednblack.editor.view.ui.dialog;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
import games.rednblack.editor.utils.ScrollPaneUtils;
import games.rednblack.editor.view.ui.widget.CullingTable;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPane;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPaneListener;
import games.rednblack.puremvc.Facade;

import java.util.Set;

import static com.kotcrab.vis.ui.util.adapter.AbstractListAdapter.ListSelection.DEFAULT_KEY;

public class AtlasesPackDialog extends H2DDialog {

    private static final String TABLE_BG = "table-bg";
    private static final String HEADER_BG = "table-header";
    /** Inset of the rows from the container's border, matching ListTable's. */
    private static final int LIST_PAD = 3;
    private static final int HEADER_HEIGHT = 26;
    private static final int MOVE_BUTTON_SIZE = 38;
    /** Held arrow keys: the wait before the first repeat, then the time between two. */
    private static final float REPEAT_DELAY = 0.4f;
    private static final float REPEAT_INTERVAL = 0.05f;

    private final ImageTabbedPane tabbedPane;
    private final String addNewNotification;
    private final String moveRegionNotification, updateCurrentNotification, removeNotification;
    private final String applyNotification;
    private final String editorOnlyNotification;
    private final SimpleListAdapter<String> mainPackAdapter, currentPackAdapter;
    private final VisImageButton insertButton, removeButton;
    private final VisLabel currentSelectedPackLabel;
    private final VisCheckBox editorOnlyCheckBox;
    private final VisLabel mainEmptyLabel, currentEmptyLabel;

    private final Facade facade = Facade.getInstance();

    private final Array<String> mainList = new Array<>();
    private final Array<String> currentList = new Array<>();

    private final ListView<String> mainPackList;
    private final ListView<String> currentPackList;

    /** The arrow key being held, -1 for none, and the time left before it moves the selection again. */
    private int repeatKey = -1;
    private float repeatIn;

    public AtlasesPackDialog(String title, String add, String move, String updateList, String removeNotification,
                             String applyNotification, String editorOnlyNotification) {
        super(title);
        addNewNotification = add;
        moveRegionNotification = move;
        updateCurrentNotification = updateList;
        this.removeNotification = removeNotification;
        this.applyNotification = applyNotification;
        this.editorOnlyNotification = editorOnlyNotification;

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
        //a pack with hundreds of regions draws every thumbnail of it otherwise, scrolled out of sight or not
        enableCulling(mainPackList);
        enableCulling(currentPackList);

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
        opButtons.add(insertButton).size(MOVE_BUTTON_SIZE).pad(3).row();
        opButtons.add(removeButton).size(MOVE_BUTTON_SIZE).pad(3).row();
        opButtonsContainer.add(opButtons);

        // Titled panels sharing the look of the panels' data tables: a header band over a sunken,
        // bordered container, so the two columns read as one designed pair rather than tinted boxes.
        VisTable mainHeader = headerBand(StandardWidgetsFactory.createLabel("Main Pack",
                PropertyGrid.style(PropertyGrid.SECTION_STYLE_LARGE), Align.center));

        currentSelectedPackLabel = StandardWidgetsFactory.createLabel("Select Pack",
                PropertyGrid.style(PropertyGrid.SECTION_STYLE_LARGE), Align.center);

        // Editor-only packs are still packed so they show up while authoring, but the export skips
        // both the atlas and the project entry, so the game never carries them.
        editorOnlyCheckBox = StandardWidgetsFactory.createCheckBox("Editor only");
        editorOnlyCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(editorOnlyNotification, editorOnlyCheckBox.isChecked());
            }
        });
        VisTable currentHeader = headerBand(currentSelectedPackLabel, editorOnlyCheckBox);

        mainPackList.getMainTable().background(VisUI.getSkin().getDrawable(TABLE_BG));
        mainPackList.getMainTable().pad(LIST_PAD);
        currentPackList.getMainTable().background(VisUI.getSkin().getDrawable(TABLE_BG));
        currentPackList.getMainTable().pad(LIST_PAD);

        // Empty-state placeholders, layered over each list and toggled by refreshEmptyStates()
        mainEmptyLabel = StandardWidgetsFactory.createLabel("No regions",
                PropertyGrid.style(PropertyGrid.LABEL_STYLE_LARGE), Align.center);
        mainEmptyLabel.setTouchable(Touchable.disabled);
        currentEmptyLabel = StandardWidgetsFactory.createLabel("Select a pack",
                PropertyGrid.style(PropertyGrid.LABEL_STYLE_LARGE), Align.center);
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
                if (stepFor(keycode) == 0) return false;
                //the desktop backend sends one keyDown however long the key is held, so the repeat is ours
                moveSelection(keycode);
                repeatKey = keycode;
                repeatIn = REPEAT_DELAY;
                return true;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == repeatKey) repeatKey = -1;
                return false;
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (repeatKey == -1) return;
        //a keyUp can go missing when the focus moves on while the key is held
        if (!Gdx.input.isKeyPressed(repeatKey) || !focusIsInside()) {
            repeatKey = -1;
            return;
        }
        repeatIn -= delta;
        while (repeatIn <= 0) {
            moveSelection(repeatKey);
            repeatIn += REPEAT_INTERVAL;
        }
    }

    private boolean focusIsInside() {
        Actor focus = getStage() == null ? null : getStage().getKeyboardFocus();
        return focus != null && (focus == this || focus.isDescendantOf(this));
    }

    /**
     * How far an arrow moves the selection in the list that holds it: one row in the main list, and in
     * the grid one card sideways or a whole row up and down. 0 for a key that does not move it there.
     */
    private int stepFor(int keycode) {
        boolean grid = mainPackAdapter.getSelection().size == 0 && currentPackAdapter.getSelection().size > 0;
        switch (keycode) {
            case Input.Keys.UP: return grid ? -ResourceGridAdapter.COLUMNS : -1;
            case Input.Keys.DOWN: return grid ? ResourceGridAdapter.COLUMNS : 1;
            case Input.Keys.LEFT: return grid ? -1 : 0;
            case Input.Keys.RIGHT: return grid ? 1 : 0;
            default: return 0;
        }
    }

    private void moveSelection(int keycode) {
        int step = stepFor(keycode);
        if (step == 0) return;
        if (mainPackAdapter.getSelection().size > 0)
            moveSelection(mainPackAdapter, mainPackList, step);
        else if (currentPackAdapter.getSelection().size > 0)
            moveSelection(currentPackAdapter, currentPackList, step);
    }

    /** Moves from the last selected item by {@code step}, stopping at either end of the list. */
    private void moveSelection(SimpleListAdapter<String> adapter, ListView<String> list, int step) {
        Array<String> selection = adapter.getSelection();
        int lastSelected = adapter.indexOf(selection.get(selection.size - 1));
        int next = lastSelected + step;
        if (next < 0 || next >= adapter.size()) return;

        String currentItem = adapter.get(lastSelected);
        String nextItem = adapter.get(next);

        if (!isGroupMultiSelectKeyPressed(adapter))
            adapter.getSelectionManager().deselectAll();

        if (adapter.getSelectionManager().getSelection().contains(nextItem, false)) {
            adapter.getSelectionManager().deselect(currentItem);
        } else {
            adapter.getSelectionManager().select(nextItem);
        }

        ScrollPaneUtils.scrollIntoView(list.getScrollPane(), adapter.getView(nextItem));
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
        editorOnlyCheckBox.setChecked(false);
        setEditorOnlyEnabled(false);
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

    /**
     * Lets the scroll pane of a list skip the rows it is not showing. The list keeps its items in a
     * table inside a table of its own, neither of them cullable, so its own table goes into a
     * {@link CullingTable} that hands the area down to the items table. The list rebuilds by clearing
     * and refilling those same two tables, so the target never goes stale.
     */
    private static void enableCulling(ListView<String> list) {
        VisScrollPane scrollPane = list.getScrollPane();
        Group scrollTable = (Group) scrollPane.getActor();
        CullingTable root = new CullingTable();
        root.top().left();
        root.add(scrollTable).grow();
        root.setCullingTarget((Group) scrollTable.getChildren().first());
        scrollPane.setActor(root);
    }

    /** Header band of a pack column, drawn with the table header asset. */
    private VisTable headerBand(VisLabel title) {
        VisTable header = new VisTable();
        header.background(VisUI.getSkin().getDrawable(HEADER_BG));
        header.add(title).growX().height(HEADER_HEIGHT).padLeft(8).padRight(8);
        return header;
    }

    /** Header band with a trailing control, e.g. the editor-only toggle. */
    private VisTable headerBand(VisLabel title, Actor trailing) {
        VisTable header = new VisTable();
        header.background(VisUI.getSkin().getDrawable(HEADER_BG));
        header.add(title).growX().height(HEADER_HEIGHT).padLeft(8);
        header.add(trailing).height(HEADER_HEIGHT).padLeft(8).padRight(8);
        return header;
    }

    /** Reflect the selected pack's flag without firing the change notification back. */
    public void setEditorOnly(boolean editorOnly) {
        editorOnlyCheckBox.setChecked(editorOnly);
    }

    public void setEditorOnlyEnabled(boolean enabled) {
        editorOnlyCheckBox.setDisabled(!enabled);
        editorOnlyCheckBox.setColor(1f, 1f, 1f, enabled ? 1f : 0.45f);
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
