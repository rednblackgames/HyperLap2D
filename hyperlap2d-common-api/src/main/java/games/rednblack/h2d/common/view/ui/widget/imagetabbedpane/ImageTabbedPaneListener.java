package games.rednblack.h2d.common.view.ui.widget.imagetabbedpane;


public interface ImageTabbedPaneListener {
    /**
     * Called when TabbedPane switched to new tab.
     * @param tab that TabbedPane switched to. May be null if all tabs were disabled or if {@link ImageTabbedPane#setAllowTabDeselect(boolean)} was set to
     * true and all tabs were deselected.
     */
    void switchedTab (ImageTab tab);

    /**
     * Called when Tab was removed TabbedPane.
     * @param tab that was removed.
     */
    void removedTab (ImageTab tab);

    /** Called when all tabs were removed from TabbedPane. */
    void removedAllTabs ();
}
