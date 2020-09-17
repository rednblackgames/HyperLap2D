package games.rednblack.h2d.common.view.ui.widget.imagetabbedpane;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.Sizes;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.i18n.BundleText;
import com.kotcrab.vis.ui.layout.DragPane;
import com.kotcrab.vis.ui.layout.HorizontalFlowGroup;
import com.kotcrab.vis.ui.layout.VerticalFlowGroup;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter;
import com.kotcrab.vis.ui.widget.*;

/**
 * Modified VisUI TabbedPane to support Images as tab content
 */

public class ImageTabbedPane {
    private static final Vector2 tmpVector = new Vector2();
    private static final Rectangle tmpRect = new Rectangle();

    private TabbedPaneStyle style;
    private Sizes sizes;
    private VisImageButton.VisImageButtonStyle sharedCloseActiveButtonStyle;

    private DragPane tabsPane;
    private TabbedPaneTable mainTable;

    private Array<ImageTab> tabs;
    private IdentityMap<ImageTab, TabButtonTable> tabsButtonMap;
    private ButtonGroup<Button> group;

    private ImageTab activeTab;

    private Array<ImageTabbedPaneListener> listeners;
    private boolean allowTabDeselect;

    public ImageTabbedPane () {
        this(VisUI.getSkin().get(TabbedPaneStyle.class));
    }

    public ImageTabbedPane (String styleName) {
        this(VisUI.getSkin().get(TabbedPaneStyle.class));
    }

    public ImageTabbedPane (TabbedPaneStyle style) {
        this(style, VisUI.getSizes());
    }

    public ImageTabbedPane (TabbedPaneStyle style, Sizes sizes) {
        this.style = style;
        this.sizes = sizes;
        listeners = new Array<ImageTabbedPaneListener>();

        sharedCloseActiveButtonStyle = VisUI.getSkin().get("close-active-tab", VisImageButton.VisImageButtonStyle.class);

        group = new ButtonGroup<Button>();

        mainTable = new TabbedPaneTable(this);
        tabsPane = new DragPane(style.vertical ? new VerticalFlowGroup() : new HorizontalFlowGroup());
        configureDragPane(style);

        mainTable.setBackground(style.background);

        tabs = new Array<ImageTab>();
        tabsButtonMap = new IdentityMap<ImageTab, TabButtonTable>();

        Cell<DragPane> tabsPaneCell = mainTable.add(tabsPane);
        Cell<Image> separatorCell = null;

        if (style.vertical) {
            tabsPaneCell.top().growY().minSize(0, 0);
        } else {
            tabsPaneCell.left().growX().minSize(0, 0);
        }

        //note: if separatorBar height/width is not set explicitly it may sometimes disappear
        if (style.separatorBar != null) {
            if (style.vertical) {
                separatorCell = mainTable.add(new Image(style.separatorBar)).growY().width(style.separatorBar.getMinWidth());
            } else {
                mainTable.row();
                separatorCell = mainTable.add(new Image(style.separatorBar)).growX().height(style.separatorBar.getMinHeight());
            }
        } else {
            //make sure that tab will fill available space even when there is no separatorBar image set
            if (style.vertical) {
                mainTable.add().growY();
            } else {
                mainTable.row();
                mainTable.add().growX();
            }
        }

        mainTable.setPaneCells(tabsPaneCell, separatorCell);
    }

    private void configureDragPane (TabbedPaneStyle style) {
        tabsPane.setTouchable(Touchable.childrenOnly);
        tabsPane.setListener(new DragPane.DragPaneListener.AcceptOwnChildren());
        if (style.draggable) {
            Draggable draggable = new Draggable();
            draggable.setInvisibleWhenDragged(true);
            draggable.setKeepWithinParent(true);
            draggable.setBlockInput(true);
            draggable.setFadingTime(0f);
            draggable.setListener(new DragPane.DefaultDragListener() {
                public boolean dragged;

                @Override
                public boolean onStart (Draggable draggable, Actor actor, float stageX, float stageY) {
                    dragged = false;
                    if (actor instanceof TabButtonTable) {
                        if (((TabButtonTable) actor).closeButton.isOver()) return CANCEL;
                    }
                    return super.onStart(draggable, actor, stageX, stageY);
                }

                @Override
                public void onDrag (Draggable draggable, Actor actor, float stageX, float stageY) {
                    super.onDrag(draggable, actor, stageX, stageY);
                    dragged = true;
                }

                @Override
                public boolean onEnd (Draggable draggable, Actor actor, float stageX, float stageY) {
                    boolean result = super.onEnd(draggable, actor, stageX, stageY);
                    if (result == APPROVE) return APPROVE;
                    if (dragged == false) return CANCEL;

                    // check if any actor corner is over some other tab
                    tabsPane.stageToLocalCoordinates(tmpVector.set(stageX, stageY));
                    if (tabsPane.hit(tmpVector.x, tmpVector.y, true) != null) return CANCEL;
                    if (tabsPane.hit(tmpVector.x + actor.getWidth(), tmpVector.y, true) != null) return CANCEL;
                    if (tabsPane.hit(tmpVector.x, tmpVector.y - actor.getHeight(), true) != null) return CANCEL;
                    if (tabsPane.hit(tmpVector.x + actor.getWidth(), tmpVector.y - actor.getHeight(), true) != null)
                        return CANCEL;

                    Vector2 stagePos = tabsPane.localToStageCoordinates(tmpVector.setZero());
                    tmpRect.set(stagePos.x, stagePos.y, tabsPane.getGroup().getWidth(), tabsPane.getGroup().getHeight());
                    if (tmpRect.contains(stageX, stageY) == false) return CANCEL;
                    if (tabsPane.isHorizontalFlow() || tabsPane.isVerticalFlow()) {
                        DRAG_POSITION.set(stageX, stageY);
                        tabsPane.addActor(actor);
                        return APPROVE;
                    }
                    return CANCEL;
                }
            });
            tabsPane.setDraggable(draggable);
        }
    }

    /** @return a direct reference to internal {@link DragPane}. Allows to manage {@link Draggable} settings. */
    public DragPane getTabsPane () {
        return tabsPane;
    }

    /**
     * @param allowTabDeselect if true user may deselect tab, meaning that there won't be any active tab. Allows to create similar
     * behaviour like in Intellij IDEA bottom quick access bar
     */
    public void setAllowTabDeselect (boolean allowTabDeselect) {
        this.allowTabDeselect = allowTabDeselect;
        if (allowTabDeselect) {
            group.setMinCheckCount(0);
        } else {
            group.setMinCheckCount(1);
        }
    }

    public boolean isAllowTabDeselect () {
        return allowTabDeselect;
    }

    public void add (ImageTab tab) {
        tab.setPane(this);
        tabs.add(tab);

        addTab(tab, tabsPane.getChildren().size);
        switchTab(tab);
    }

    public void insert (int index, ImageTab tab) {
        tab.setPane(this);
        tabs.insert(index, tab);
        addTab(tab, index);
    }

    /**
     * @param tab will be added in the selected index.
     * @param index index of the tab, starting from zero.
     */
    protected void addTab (ImageTab tab, int index) {
        TabButtonTable buttonTable = tabsButtonMap.get(tab);
        if (buttonTable == null) {
            buttonTable = new TabButtonTable(tab);
            tabsButtonMap.put(tab, buttonTable);
        }

        buttonTable.setTouchable(Touchable.enabled);
        if (index >= tabsPane.getChildren().size) {
            tabsPane.addActor(buttonTable);
        } else {
            tabsPane.addActorAt(index, buttonTable);
        }
        group.add(buttonTable.button);

        if (tabs.size == 1 && activeTab != null) {
            buttonTable.select();
            notifyListenersSwitched(tab);
        } else if (tab == activeTab) {
            buttonTable.select(); // maintains currently selected tab while rebuilding
        }
    }

    /**
     * Disables or enables given tab.
     * <p>
     * When disabling, if tab is currently selected, TabbedPane will switch to first available enabled Tab. If there is no any
     * other enabled Tab, listener {@link ImageTabbedPaneListener#switchedTab(ImageTab)} with null Tab will be called.
     * <p>
     * When enabling Tab and there isn't any others Tab enabled and {@link #setAllowTabDeselect(boolean)} was set to false, passed
     * Tab will be selected. If {@link #setAllowTabDeselect(boolean)} is set to true nothing will be selected, all tabs will remain
     * unselected.
     * @param tab tab to change its state
     * @param disable controls whether to disable or enable this tab
     * @throws IllegalArgumentException if tab does not belong to this TabbedPane
     */
    public void disableTab (ImageTab tab, boolean disable) {
        checkIfTabsBelongsToThisPane(tab);

        TabButtonTable buttonTable = tabsButtonMap.get(tab);
        buttonTable.button.setDisabled(disable);

        if (activeTab == tab && disable) {
            if (selectFirstEnabledTab()) {
                return;
            }

            // there isn't any tab we can switch to
            activeTab = null;
            notifyListenersSwitched(null);
        }

        if (activeTab == null && allowTabDeselect == false) {
            selectFirstEnabledTab();
        }
    }

    public boolean isTabDisabled (ImageTab tab) {
        TabButtonTable table = tabsButtonMap.get(tab);
        if (table == null) {
            throwNotBelongingTabException(tab);
        }
        return table.button.isDisabled();
    }

    private boolean selectFirstEnabledTab () {
        for (ObjectMap.Entry<ImageTab, TabButtonTable> entry : tabsButtonMap) {
            if (entry.value.button.isDisabled() == false) {
                switchTab(entry.key);
                return true;
            }
        }

        return false;
    }

    private void checkIfTabsBelongsToThisPane (ImageTab tab) {
        if (tabs.contains(tab, true) == false) {
            throwNotBelongingTabException(tab);
        }
    }

    protected void throwNotBelongingTabException (ImageTab tab) {
        throw new IllegalArgumentException("Tab '" + tab.getTabTitle() + "' does not belong to this TabbedPane");
    }

    /**
     * Removes tab from pane, if tab is dirty this won't cause to display "Unsaved changes" dialog!
     * @param tab to be removed
     * @return true if tab was removed, false if that tab wasn't added to this pane
     */
    public boolean remove (ImageTab tab) {
        return remove(tab, true);
    }

    /**
     * Removes tab from pane, if tab is dirty and 'ignoreTabDirty == false' this will cause to display "Unsaved changes" dialog!
     * @return true if tab was removed, false if that tab wasn't added to this pane or "Unsaved changes" dialog was started
     */
    public boolean remove (final ImageTab tab, boolean ignoreTabDirty) {
        checkIfTabsBelongsToThisPane(tab);

        if (ignoreTabDirty) {
            return removeTab(tab);
        }

        if (tab.isDirty() && mainTable.getStage() != null) {
            Dialogs.showOptionDialog(mainTable.getStage(), Text.UNSAVED_DIALOG_TITLE.get(), Text.UNSAVED_DIALOG_TEXT.get(),
                    Dialogs.OptionDialogType.YES_NO_CANCEL, new OptionDialogAdapter() {
                        @Override
                        public void yes () {
                            tab.save();
                            removeTab(tab);
                        }

                        @Override
                        public void no () {
                            removeTab(tab);
                        }
                    });
        } else {
            return removeTab(tab);
        }

        return false;
    }

    private boolean removeTab (ImageTab tab) {
        int index = tabs.indexOf(tab, true);
        boolean success = tabs.removeValue(tab, true);

        if (success) {
            TabButtonTable buttonTable = tabsButtonMap.get(tab);
            tabsPane.removeActor(buttonTable, true);
            tabsPane.invalidateHierarchy();
            tabsButtonMap.remove(tab);
            group.remove(buttonTable.button);

            tab.setPane(null);
            tab.onHide();
            tab.dispose();
            notifyListenersRemoved(tab);

            if (tabs.size == 0) {
                // all tabs were removed so notify listener
                activeTab = null;
                notifyListenersRemovedAll();
            } else if (activeTab == tab) {
                if (index > 0) {
                    // switch to previous tab
                    switchTab(--index);
                } else {
                    // Switching to the next tab, currently having our removed tab index.
                    switchTab(index);
                }
            }
        }

        return success;
    }

    /** Removes all tabs, ignores if tab is dirty */
    public void removeAll () {
        for (ImageTab tab : tabs) {
            tab.setPane(null);
            tab.onHide();
            tab.dispose();
        }

        tabs.clear();
        tabsButtonMap.clear();
        tabsPane.clear();
        activeTab = null;

        notifyListenersRemovedAll();
    }

    public void switchTab (int index) {
        tabsButtonMap.get(tabs.get(index)).select();
    }

    public void switchTab (ImageTab tab) {
        TabButtonTable table = tabsButtonMap.get(tab);
        if (table == null) {
            throwNotBelongingTabException(tab);
        }
        table.select();
    }

    /**
     * Must be called when you want to update tab title. If tab is dirty an '*' is added before title. This is called automatically
     * if using {@link ImageTab#setDirty(boolean)}
     * @param tab that title will be updated
     */
    public void updateTabTitle (ImageTab tab) {
        TabButtonTable table = tabsButtonMap.get(tab);
        if (table == null) {
            throwNotBelongingTabException(tab);
        }
        Tooltip.removeTooltip(table.button);
        new Tooltip.Builder(getTabTitle(tab)).target(table.button).build();
    }

    protected String getTabTitle (ImageTab tab) {
        return tab.isDirty() ? "*" + tab.getTabTitle() : tab.getTabTitle();
    }

    public TabbedPaneTable getTable () {
        return mainTable;
    }

    /** @return active tab or null if no tab is selected. */
    public ImageTab getActiveTab () {
        return activeTab;
    }

    public void addListener (ImageTabbedPaneListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener (ImageTabbedPaneListener listener) {
        return listeners.removeValue(listener, true);
    }

    private void notifyListenersSwitched (ImageTab tab) {
        for (ImageTabbedPaneListener listener : listeners) {
            listener.switchedTab(tab);
        }
    }

    private void notifyListenersRemoved (ImageTab tab) {
        for (ImageTabbedPaneListener listener : listeners) {
            listener.removedTab(tab);
        }
    }

    private void notifyListenersRemovedAll () {
        for (ImageTabbedPaneListener listener : listeners) {
            listener.removedAllTabs();
        }
    }

    /** Returns tabs in order in which they are stored in tabbed pane, sorted by their index and ignoring their order in UI. */
    public Array<ImageTab> getTabs () {
        return tabs;
    }

    /**
     * Returns tabs in order in which they are displayed in the UI - user may drag and move tabs which DOES NOT affect
     * their index. Use {@link #getTabs()} if you don't care about UI order. This creates new array every time it's called!
     */
    public Array<ImageTab> getUIOrderedTabs () {
        Array<ImageTab> tabs = new Array<ImageTab>();
        for (Actor actor : getTabsPane().getChildren()) {
            if (actor instanceof TabButtonTable) {
                tabs.add(((TabButtonTable) actor).tab);
            }
        }
        return tabs;
    }

    public static class TabbedPaneStyle {
        public Drawable background;
        public VisImageButton.VisImageButtonStyle buttonStyle;
        /** Optional. */
        public Drawable separatorBar;
        /** Optional, defaults to false. */
        public boolean vertical = false;
        /** Optional, defaults to true. */
        public boolean draggable = true;
        /** Optional, defaults 5. */
        public float tabPadding = 5;

        public TabbedPaneStyle () {
        }

        public TabbedPaneStyle (TabbedPaneStyle style) {
            this.background = style.background;
            this.buttonStyle = style.buttonStyle;
            this.separatorBar = style.separatorBar;
            this.vertical = style.vertical;
            this.draggable = style.draggable;
            this.tabPadding = style.tabPadding;
        }

        public TabbedPaneStyle (Drawable background, Drawable separatorBar, VisImageButton.VisImageButtonStyle buttonStyle) {
            this.background = background;
            this.separatorBar = separatorBar;
            this.buttonStyle = buttonStyle;
        }

        public TabbedPaneStyle (Drawable separatorBar, Drawable background, VisImageButton.VisImageButtonStyle buttonStyle, boolean vertical, boolean draggable, float tabPadding) {
            this.separatorBar = separatorBar;
            this.background = background;
            this.buttonStyle = buttonStyle;
            this.vertical = vertical;
            this.draggable = draggable;
            this.tabPadding = tabPadding;
        }
    }

    public static class TabbedPaneTable extends VisTable {
        private ImageTabbedPane tabbedPane;
        private Cell<DragPane> tabsPaneCell;
        private Cell<Image> separatorCell;

        public TabbedPaneTable (ImageTabbedPane tabbedPane) {
            this.tabbedPane = tabbedPane;
        }

        private void setPaneCells (Cell<DragPane> tabsPaneCell, Cell<Image> separatorCell) {
            this.tabsPaneCell = tabsPaneCell;
            this.separatorCell = separatorCell;
        }

        public Cell<DragPane> getTabsPaneCell () {
            return tabsPaneCell;
        }

        /** @return separator cell or null if separator is not used */
        public Cell<Image> getSeparatorCell () {
            return separatorCell;
        }

        public ImageTabbedPane getTabbedPane () {
            return tabbedPane;
        }
    }

    private class TabButtonTable extends VisTable {
        public VisImageButton button;
        public VisImageButton closeButton;
        private ImageTab tab;

        private VisImageButton.VisImageButtonStyle buttonStyle;
        private VisImageButton.VisImageButtonStyle closeButtonStyle;
        private Drawable up;

        public TabButtonTable (ImageTab tab) {
            this.tab = tab;

            buttonStyle = new VisImageButton.VisImageButtonStyle(style.buttonStyle);
            if (tab.getTabIconStyle() != null) {
                VisImageButton.VisImageButtonStyle tabIconStyle = VisUI.getSkin().get(tab.getTabIconStyle(), VisImageButton.VisImageButtonStyle.class);
                buttonStyle.imageUp = tabIconStyle.imageUp;
                buttonStyle.imageOver = tabIconStyle.imageOver;
                buttonStyle.imageDown = tabIconStyle.imageDown;
            }
            button = new VisImageButton(buttonStyle) {
                @Override
                public void setDisabled (boolean isDisabled) {
                    super.setDisabled(isDisabled);
                    closeButton.setDisabled(isDisabled);
                    deselect();
                }
            };
            new Tooltip.Builder(getTabTitle(tab)).target(button).build();
            button.setFocusBorderEnabled(false);
            button.setProgrammaticChangeEvents(false);

            closeButtonStyle = new VisImageButton.VisImageButtonStyle(VisUI.getSkin().get("close", VisImageButton.VisImageButtonStyle.class));

            closeButton = new VisImageButton(closeButtonStyle);
            closeButton.setGenerateDisabledImage(true);
            closeButton.getImage().setScaling(Scaling.fill);
            closeButton.getImage().setColor(Color.RED);

            addListeners();

            closeButtonStyle = closeButton.getStyle();
            up = buttonStyle.up;

            add(button).width(button.getWidth() + style.tabPadding);
            if (tab.isCloseableByUser()) {
                add(closeButton).size(14 * sizes.scaleFactor, button.getHeight());
            }
        }

        private void addListeners () {
            closeButton.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    closeTabAsUser();
                }
            });

            button.addListener(new InputListener() {
                private boolean isDown;

                @Override
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int mouseButton) {
                    if (button.isDisabled()) {
                        return false;
                    }

                    isDown = true;
                    if (UIUtils.left()) {
                        setDraggedUpImage();
                    }

                    if (mouseButton == Input.Buttons.MIDDLE) {
                        closeTabAsUser();
                    }

                    return true;
                }

                @Override
                public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                    setDefaultUpImage();
                    isDown = false;
                }

                @Override
                public boolean mouseMoved (InputEvent event, float x, float y) {
                    if (!button.isDisabled() && activeTab != tab) {
                        setCloseButtonOnMouseMove();
                    }
                    return false;
                }

                @Override
                public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (!button.isDisabled() && !isDown && activeTab != tab && pointer == -1) {
                        setDefaultUpImage();
                    }
                }

                @Override
                public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (!button.isDisabled() && activeTab != tab && Gdx.input.justTouched() == false && pointer == -1) {
                        setCloseButtonOnMouseMove();
                    }
                }

                private void setCloseButtonOnMouseMove () {
                    if (isDown) {
                        closeButtonStyle.up = buttonStyle.down;
                    } else {
                        closeButtonStyle.up = buttonStyle.over;
                    }
                }

                private void setDraggedUpImage () {
                    closeButtonStyle.up = buttonStyle.down;
                    buttonStyle.up = buttonStyle.down;
                }

                private void setDefaultUpImage () {
                    closeButtonStyle.up = up;
                    buttonStyle.up = up;
                }
            });

            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    switchToNewTab();
                }
            });
        }

        private void switchToNewTab () {
            // there was some previous tab, deselect it
            if (activeTab != null && activeTab != tab) {
                TabButtonTable table = tabsButtonMap.get(activeTab);
                // table may no longer exists if tab was removed, no big deal since this only changes
                // button style, tab.onHide() will be already called by remove() method
                if (table != null) {
                    table.deselect();
                    activeTab.onHide();
                }
            }

            if (button.isChecked() && tab != activeTab) { // switch to new tab
                activeTab = tab;
                notifyListenersSwitched(tab);
                tab.onShow();
                closeButton.setStyle(sharedCloseActiveButtonStyle);
            } else if (group.getCheckedIndex() == -1) { // no tab selected (allowTabDeselect == true)
                activeTab = null;
                notifyListenersSwitched(null);
            }

        }

        /** Closes tab, does nothing if Tab is not closeable by user */
        private void closeTabAsUser () {
            if (tab.isCloseableByUser()) {
                ImageTabbedPane.this.remove(tab, false);
            }
        }

        private void select () {
            button.setChecked(true);
            switchToNewTab();
        }

        private void deselect () {
            closeButton.setStyle(closeButtonStyle);
        }
    }

    private enum Text implements BundleText {
        UNSAVED_DIALOG_TITLE("unsavedDialogTitle"), UNSAVED_DIALOG_TEXT("unsavedDialogText");

        private final String name;

        Text (final String name) {
            this.name = name;
        }

        private static I18NBundle getBundle () {
            return Locales.getTabbedPaneBundle();
        }

        @Override
        public final String getName () {
            return name;
        }

        @Override
        public final String get () {
            return getBundle().get(name);
        }

        @Override
        public final String format () {
            return getBundle().format(name);
        }

        @Override
        public final String format (final Object... arguments) {
            return getBundle().format(name, arguments);
        }

        @Override
        public final String toString () {
            return get();
        }
    }
}

