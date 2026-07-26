package games.rednblack.editor.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

/**
 * One region per row: its thumbnail, then its name. Rows are striped, light up under the pointer and
 * carry an accent bar once picked, using the same table assets as the panels' data tables.
 */
public class ResourceListAdapter extends SimpleListAdapter<String> {

    protected static final String ROW_BG = "table-row";
    protected static final String ROW_BG_ALT = "table-row-alt";
    protected static final String ROW_OVER = "table-row-over-first";
    protected static final String ROW_SELECTED = "table-row-selected";

    /** Thumbnail of a region in a row, and the padding around it. */
    private static final int THUMBNAIL_SIZE = 42;
    private static final int ROW_PAD = 4;
    private static final int LABEL_PAD = 10;

    public ResourceListAdapter(Array<String> array) {
        super(array);
    }

    public ResourceListAdapter(Array<String> array, String styleName) {
        super(array, styleName);
    }

    public ResourceListAdapter(Array<String> array, SimpleListAdapterStyle style) {
        super(array, style);
    }

    @Override
    protected VisTable createView(String item) {
        ResourceManager rm = Facade.getInstance().retrieveProxy(ResourceManager.NAME);

        ResourceView view = new ResourceView(hoverBackground(), selectedBackground());
        view.left();
        view.add(new Image(new TextureRegionDrawable(rm.getTextureRegion(item)), Scaling.contain, Align.center))
                .size(THUMBNAIL_SIZE).pad(ROW_PAD);
        VisLabel label = StandardWidgetsFactory.createLabel(item,
                PropertyGrid.style(PropertyGrid.SECTION_STYLE_LARGE), Align.left);
        label.setEllipsis(true); // region names are user data: truncate instead of widening the list
        view.add(label).growX().padLeft(LABEL_PAD).padRight(LABEL_PAD).row();
        return view;
    }

    @Override
    public void fillTable(VisTable table) {
        // striping needs the position of an item, which only this pass knows, so the row remembers
        // the tone it was given: it goes back to that tone when it stops being selected
        if (getItemsSorter() != null) sort(getItemsSorter());

        int index = 0;
        for (String item : iterable()) {
            VisTable view = getView(item);
            prepareViewBeforeAddingToTable(item, view);
            if (view instanceof ResourceView) {
                ((ResourceView) view).setNormalBackground(normalBackground(index));
            }
            table.add(view).growX().row();
            index++;
        }
    }

    @Override
    protected void selectView(VisTable view) {
        if (view instanceof ResourceView) ((ResourceView) view).setSelected(true);
        else super.selectView(view);
    }

    @Override
    protected void deselectView(VisTable view) {
        if (view instanceof ResourceView) ((ResourceView) view).setSelected(false);
        else super.deselectView(view);
    }

    /** Alternating row tones. The grid subclass returns a card instead. */
    protected Drawable normalBackground(int index) {
        return drawable(index % 2 == 0 ? ROW_BG : ROW_BG_ALT);
    }

    protected Drawable hoverBackground() {
        return drawable(ROW_OVER);
    }

    protected Drawable selectedBackground() {
        return drawable(ROW_SELECTED);
    }

    protected static Drawable drawable(String name) {
        return VisUI.getSkin().getDrawable(name);
    }

    /** A row that knows its three states, so hovering never overwrites what selection painted. */
    protected static class ResourceView extends VisTable {
        private final Drawable hover;
        private final Drawable selected;
        private Drawable normal;
        private boolean isSelected;

        ResourceView(Drawable hover, Drawable selected) {
            this.hover = hover;
            this.selected = selected;
            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1 && !isSelected) setBackground(ResourceView.this.hover);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1 && !isSelected && !isInView(toActor)) setBackground(normal);
                }

                private boolean isInView(Actor actor) {
                    return actor != null
                            && (actor == ResourceView.this || actor.isDescendantOf(ResourceView.this));
                }
            });
        }

        void setNormalBackground(Drawable normal) {
            this.normal = normal;
            if (!isSelected) setBackground(normal);
        }

        void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            setBackground(isSelected ? selected : normal);
        }
    }
}
