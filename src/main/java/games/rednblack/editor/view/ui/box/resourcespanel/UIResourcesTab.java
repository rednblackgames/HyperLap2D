package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.PoolManager;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.view.ui.widget.CullingTable;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.puremvc.Facade;

/**
 * Created by sargis on 5/4/15.
 */
public abstract class UIResourcesTab extends ImageTab {
    protected static PoolManager POOLS = new PoolManager();
    static {
        POOLS.addPool(Vector2.class, Vector2::new);
    }

    protected final VisTable contentTable;

    /**
     * Content of the scroll pane: shows either the items table or the empty placeholder, never
     * both. Keeping the placeholder out of the items table matters because
     * {@link games.rednblack.editor.view.ui.BoxItemResourceSelectionUIMediator} treats every cell
     * of the tables registered for selection management as a resource box.
     */
    private final CullingTable listRoot = new CullingTable();

    private VisTable itemsTable;

    public String searchString = "";

    public UIResourcesTab() {
        super(false, false);
        contentTable = new VisTable();
        contentTable.padTop(8).padLeft(7);
        contentTable.add(StandardWidgetsFactory.createLabel("Search:")).padLeft(1).padBottom(6);
        contentTable.add(createTextField()).padLeft(0).padRight(7).fillX().padBottom(4);
        contentTable.add(createFilterButton()).padRight(7).padTop(-4);
        contentTable.row();

        VisScrollPane scrollPane = crateScrollPane();
        contentTable.add(scrollPane).padTop(4).colspan(3).maxHeight(Gdx.graphics.getHeight() * 0.22f).expandX().fillX();
    }

    protected VisTextField createTextField() {
        VisTextField visTextField = StandardWidgetsFactory.createTextField();
        visTextField.setMessageText(getTabTitle());
        visTextField.setTextFieldListener((textField, c) -> {
            searchString    =   textField.getText();
            Facade facade = Facade.getInstance();
            facade.sendNotification(MsgAPI.UPDATE_RESOURCES_LIST);
        });
        return visTextField;
    }

    protected VisImageButton createFilterButton () {
        VisImageButton button = StandardWidgetsFactory.createImageButton("filter-button");
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Facade facade = Facade.getInstance();
                Vector2 pos = POOLS.obtain(Vector2.class);
                pos.set(0, 0);
                button.localToStageCoordinates(pos);
                Object[] payload = new Object[]{pos.x, pos.y, getTabTitle()};
                facade.sendNotification(UIFilterMenu.SHOW_FILTER_MENU, payload);
                POOLS.free(pos);
            }
        });
        return button;
    }

    @Override
    public Table getContentTable() {
        return contentTable;
    }

    protected abstract VisScrollPane crateScrollPane();

    /** Skin region of the icon shown when this tab has no assets at all. */
    protected abstract String getEmptyIconRegion();

    /** Title shown when this tab has no assets at all, e.g. "No images yet". */
    protected abstract String getEmptyTitle();

    /** One line telling the user how content gets into this tab. */
    protected abstract String getEmptyHint();

    /**
     * Builds the scroll pane every tab shows, wrapping the table that holds the resource boxes so
     * that the empty placeholder can take its place without ever becoming one of its cells.
     */
    protected VisScrollPane createListScrollPane(VisTable itemsTable) {
        this.itemsTable = itemsTable;
        listRoot.left().top();
        listRoot.add(itemsTable).growX();
        //a pack with hundreds of assets draws every one of them otherwise, scrolled out of sight or not
        listRoot.setCullingTarget(itemsTable);
        return StandardWidgetsFactory.createScrollPane(listRoot);
    }

    /**
     * Shows a placeholder in place of the items table.
     * @param filtered true when assets exist but the search text or active filters hide them all
     */
    protected void showEmptyHint(boolean filtered) {
        EmptyResourcesHint hint = filtered
                ? new EmptyResourcesHint("icon-empty-search", "Nothing matches", "Change the search text or the active filters")
                : new EmptyResourcesHint(getEmptyIconRegion(), getEmptyTitle(), getEmptyHint());
        listRoot.clearChildren();
        listRoot.add(hint).growX();
    }

    /** Brings the items table back, dropping any placeholder currently shown. */
    protected void showItemsTable() {
        if (itemsTable.getParent() == listRoot) return;
        listRoot.clearChildren();
        listRoot.add(itemsTable).growX();
    }
}
