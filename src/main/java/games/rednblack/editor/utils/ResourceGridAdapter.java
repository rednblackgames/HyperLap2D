package games.rednblack.editor.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.proxy.ResourceManager;
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

    public ResourceGridAdapter(Array<String> array) {
        super(array);
    }

    @Override
    protected VisTable createView(String item) {
        ResourceManager rm = Facade.getInstance().retrieveProxy(ResourceManager.NAME);

        VisTable table = new VisTable();
        table.center();
        Image icon = new Image(new TextureRegionDrawable(rm.getTextureRegion(item)), Scaling.contain, Align.center);
        // the thumbnail grows with the cell and stays square: its height tracks the width the cell
        // gets from the 3-way split, so wider dialog -> bigger previews
        table.add(new SquareBox(icon)).height(100).growX().pad(4).row();
        // ellipsis keeps long region names from stretching the column
        VisLabel label = StandardWidgetsFactory.createLabel(item, "default", Align.center, true);
        table.add(label).growX().padBottom(4).row();
        return table;
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
            table.add(view).growX().uniformX().top().pad(2);
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
