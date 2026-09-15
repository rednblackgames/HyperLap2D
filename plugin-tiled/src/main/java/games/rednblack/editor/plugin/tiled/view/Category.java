package games.rednblack.editor.plugin.tiled.view;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.plugin.tiled.data.AttributeVO;
import games.rednblack.editor.plugin.tiled.data.CategoryVO;
import games.rednblack.h2d.common.view.ui.PropertyGrid;

/**
 * A titled group of numeric attributes laid out as a property grid: the title as a section rule,
 * one label + number field row per attribute. An empty title skips the section.
 *
 * Created by mariam on 2/5/16.
 */
public class Category extends VisTable {

    private final CategoryVO categoryVO;
    private Array<AttributeVO> attributes;

    public Category(CategoryVO categoryVO) {
        this.categoryVO = categoryVO;
        reInitView(categoryVO.attributes);
    }

    public void reInitView(Array<AttributeVO> attributes) {
        this.attributes = attributes;
        clear();

        PropertyGrid grid = PropertyGrid.on(this);
        String title = displayName(categoryVO.title);
        if (!title.isEmpty()) grid.section(title);
        for (AttributeVO attributeVO : attributes) {
            // a number: sized for its digits like PropertyGrid#rowUnit, not for the whole field column
            VisTable holder = new VisTable();
            holder.add(Attribute.createField(attributeVO)).width(PropertyGrid.NUMBER_WIDTH).height(PropertyGrid.FIELD_HEIGHT);
            grid.rowCompact(displayName(attributeVO.title), holder);
        }
        pack();
    }

    public AttributeVO getAttributeVO(String title) {
        for (AttributeVO attributeVO : attributes) {
            if (attributeVO.title.equals(title)) {
                return attributeVO;
            }
        }
        return new AttributeVO();
    }

    /** VO titles carry a trailing ": " from the old label layout; the grid draws its own labels. */
    private static String displayName(String title) {
        if (title == null) return "";
        String trimmed = title.trim();
        return trimmed.endsWith(":") ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;
    }
}
