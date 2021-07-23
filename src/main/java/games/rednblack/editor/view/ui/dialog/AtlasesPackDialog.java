package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPane;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPaneListener;

import java.util.Set;

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

    private String mainSelected = null, currentSelected = null;

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
                                facade.sendNotification(removeNotification, tab.getTabTitle());
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
                currentSelectedPackLabel.setText(tab.getTabTitle());
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
        addNewPackTable.add(newPackName).growX();
        addNewPackTable.add(newPackButton).width(80);

        mainPackAdapter = new SimpleListAdapter<>(mainList);
        mainPackAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);
        mainPackAdapter.getSelectionManager().setProgrammaticChangeEvents(false);

        currentPackAdapter = new SimpleListAdapter<>(currentList);
        currentPackAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);
        currentPackAdapter.getSelectionManager().setProgrammaticChangeEvents(false);

        ListView<String> mainPackList = new ListView<>(mainPackAdapter);
        mainPackList.getScrollPane().addListener(new ScrollFocusListener());
        ListView<String> currentPackList = new ListView<>(currentPackAdapter);
        currentPackList.getScrollPane().addListener(new ScrollFocusListener());

        mainPackList.setItemClickListener(this::selectMainItem);
        currentPackList.setItemClickListener(this::selectCurrentItem);

        getContentTable().add(addNewPackTable).growX().row();
        getContentTable().add(tabbedPane.getTable()).height(30).growX().padTop(4).padBottom(4).row();

        insertButton = StandardWidgetsFactory.createTextButton(">");
        insertButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!insertButton.isDisabled())
                    facade.sendNotification(moveRegionNotification);
            }
        });
        removeButton = StandardWidgetsFactory.createTextButton("<");
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!removeButton.isDisabled())
                    facade.sendNotification(moveRegionNotification);
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
        NinePatchDrawable bg = new NinePatchDrawable((NinePatchDrawable) VisUI.getSkin().getDrawable("sticky-note"));
        bg.getPatch().setColor(Color.DARK_GRAY);
        mainPackList.getMainTable().background(bg);
        opTable.add(mainPackList.getMainTable()).uniformX().grow();
        opTable.add(opButtonsContainer).growY();
        currentPackList.getMainTable().background(bg);
        opTable.add(currentPackList.getMainTable()).uniformX().grow().row();

        getContentTable().add(opTable).grow().row();
    }

    private void selectCurrentItem(String item) {
        mainPackAdapter.getSelectionManager().deselectAll();
        mainSelected = null;
        currentSelected = item;

        updateOpButtons();
    }

    private void selectMainItem(String item) {
        currentPackAdapter.getSelectionManager().deselectAll();
        currentSelected = null;
        mainSelected = item;

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
            currentList.addAll(item);
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
            mainList.addAll(item);
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
        return tabbedPane.getActiveTab().getTabTitle();
    }

    private void updateOpButtons() {
       insertButton.setDisabled(tabbedPane.getActiveTab() == null || mainSelected == null);
       removeButton.setDisabled(tabbedPane.getActiveTab() == null || currentSelected == null);
    }

    public String getCurrentSelected() {
        return currentSelected;
    }

    public String getMainSelected() {
        return mainSelected;
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
}
