package games.rednblack.h2d.common.view.ui.widget.imagetabbedpane;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;

public abstract class ImageTab implements Disposable {
    private boolean activeTab;
    private ImageTabbedPane pane;

    private boolean closeableByUser = true;
    private boolean savable = false;
    private boolean dirty = false;

    public ImageTab () {
    }

    /** @param savable if true tab can be saved and marked as dirty. */
    public ImageTab (boolean savable) {
        this.savable = savable;
    }

    /**
     * @param savable if true tab can be saved and marked as dirty.
     * @param closeableByUser if true tab can be closed by user from tabbed pane.
     */
    public ImageTab (boolean savable, boolean closeableByUser) {
        this.savable = savable;
        this.closeableByUser = closeableByUser;
    }

    /** @return tab title used by tabbed pane. */
    public abstract String getTabTitle ();

    /** @return tab style name used by image tabbed pane. */
    public abstract String getTabIconStyle();

    /**
     * @return table that contains this tab view, will be passed to tabbed pane listener. Should
     * return same table every time this is called.
     */
    public abstract Table getContentTable ();

    /** Called by pane when this tab becomes shown. Class overriding this should call super.onShow(). */
    public void onShow () {
        activeTab = true;
    }

    /** Called by pane when this tab becomes hidden. Class overriding this should call super.onHide(). */
    public void onHide () {
        activeTab = false;
    }

    /** @return true is this tab is currently active. */
    public boolean isActiveTab () {
        return activeTab;
    }

    /** @return pane that this tab belongs to, or null. */
    public ImageTabbedPane getPane () {
        return pane;
    }

    /** Should be called by TabbedPane only, when tab is added to pane. */
    public void setPane (ImageTabbedPane pane) {
        this.pane = pane;
    }

    public boolean isSavable () {
        return savable;
    }

    public boolean isCloseableByUser () {
        return closeableByUser;
    }

    public boolean isDirty () {
        return dirty;
    }

    public void setDirty (boolean dirty) {
        checkSavable();

        boolean update = (dirty != this.dirty);

        if (update) {
            this.dirty = dirty;
            if (pane != null) getPane().updateTabTitle(this);
        }
    }

    /** Marks this tab as dirty */
    public void dirty () {
        setDirty(true);
    }

    /**
     * Called when this tab should save its own state. After saving setDirty(false) must be called manually to remove dirty state.
     * @return true when save succeeded, false otherwise.
     */
    public boolean save () {
        checkSavable();

        return false;
    }

    private void checkSavable () {
        if (isSavable() == false) throw new IllegalStateException("Tab " + getTabTitle() + " is not savable!");
    }

    /** Removes this tab from pane (if any). */
    public void removeFromTabPane () {
        if (pane != null) pane.remove(this);
    }

    /** Called when tab is being removed from scene. */
    @Override
    public void dispose () {

    }
}
