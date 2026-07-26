package games.rednblack.editor.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

/**
 * Grid variant of {@link ResourceListAdapter}: instead of one full-width row per region, items are
 * laid out {@value #COLUMNS} per row as a thumbnail with the region name underneath.
 * <p>
 * Only the layout differs — selection, caching and click handling all stay in the VisUI adapter
 * base, so the surrounding {@code ListView} keeps working unchanged.
 */
public class ResourceGridAdapter extends ResourceListAdapter {

    public static final int COLUMNS = 3;

    private static final String CARD_BG = "table-card";
    private static final String CARD_OVER = "table-card-over";
    private static final String CARD_SELECTED = "table-card-selected";

    private static final int THUMBNAIL_HEIGHT = 100;
    private static final int CARD_PAD = 6;
    /** Gap between two cards, so their frames read as separate tiles. */
    private static final int CARD_GAP = 3;

    public ResourceGridAdapter(Array<String> array) {
        super(array);
    }

    /** Card of a region: its thumbnail over its name, framed like the buttons are. */
    @Override
    protected VisTable createView(String item) {
        ResourceManager rm = Facade.getInstance().retrieveProxy(ResourceManager.NAME);

        ResourceView view = new ResourceView(hoverBackground(), selectedBackground());
        view.center();
        Image icon = new Image(new TextureRegionDrawable(rm.getTextureRegion(item)), Scaling.contain, Align.center);
        // the thumbnail grows with the cell and stays square: its height tracks the width the cell
        // gets from the 3-way split, so wider dialog -> bigger previews
        view.add(new SquareBox(icon)).height(THUMBNAIL_HEIGHT).growX().pad(CARD_PAD).row();
        // ellipsis keeps long region names from stretching the column
        VisLabel label = StandardWidgetsFactory.createLabel(item,
                PropertyGrid.style(PropertyGrid.SECTION_STYLE_LARGE), Align.center, true);
        view.add(label).growX().padLeft(CARD_PAD).padRight(CARD_PAD).padBottom(CARD_PAD).row();
        return view;
    }

    /** Cards carry a frame of their own, so they do not stripe like the rows of a list do. */
    @Override
    protected Drawable normalBackground(int index) {
        return drawable(CARD_BG);
    }

    @Override
    protected Drawable hoverBackground() {
        return drawable(CARD_OVER);
    }

    @Override
    protected Drawable selectedBackground() {
        return drawable(CARD_SELECTED);
    }

    @Override
    public void fillTable(VisTable table) {
        // mirrors AbstractListAdapter.fillTable, but breaks the row every COLUMNS cells. Cells use
        // growX + uniformX so the three columns split the full available width evenly instead of
        // sitting at a fixed size; the square thumbnails then grow with them.
        if (getItemsSorter() != null) sort(getItemsSorter());

        int column = 0;
        for (String item : iterable()) {
            VisTable view = getView(item);
            prepareViewBeforeAddingToTable(item, view);
            if (view instanceof ResourceView) {
                ((ResourceView) view).setNormalBackground(normalBackground(column));
            }
            table.add(view).growX().uniformX().top().pad(CARD_GAP);
            if (++column % COLUMNS == 0) {
                table.row();
            }
        }
        // pad the final partial row so the last cells keep their column width instead of stretching
        if (column % COLUMNS != 0) {
            for (int i = column % COLUMNS; i < COLUMNS; i++) {
                table.add().growX().uniformX();
            }
            table.row();
        }
    }

    /**
     * Container that reports a preferred height equal to its width, so a thumbnail placed in a
     * {@code growX} cell renders as a square that scales with the column width. Without this the
     * cell height would come from the region's native texture size and small regions would stay
     * tiny no matter how much width the grid has.
     */
    private static class SquareBox extends VisTable {
        SquareBox(Image image) {
            add(image).grow();
        }

        @Override
        public float getPrefHeight() {
            return getWidth();
        }

        @Override
        public float getMinHeight() {
            return 0;
        }
    }
}
