package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntMap;
import games.rednblack.editor.utils.MenuIcons;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;

import static games.rednblack.editor.renderer.factory.EntityFactory.*;

/**
 * The items tree's own icons: all of one size, drawn white and tinted by kind, the way Spine's tree tells a bone
 * from a slot at a glance. Widgets share the theme blue so they stand apart from plain composites.
 */
final class ItemsTreeIcons {

    static final Color WIDGET_COLOR = new Color(0x4FB8ECFF);
    static final Color ROOT_COLOR = new Color(0xD6D6D6FF);

    private static final IntMap<String> REGIONS = new IntMap<>();
    private static final IntMap<Color> COLORS = new IntMap<>();

    static {
        put(UNKNOWN_TYPE, "unknown", 0x9E9E9EFF);
        put(COMPOSITE_TYPE, "composite", 0xE8B84AFF);
        put(LABEL_TYPE, "label", 0xD6D6D6FF);
        put(IMAGE_TYPE, "image", 0x8BC34AFF);
        put(NINE_PATCH, "ninepatch", 0x8BC34AFF);
        put(COLOR_PRIMITIVE, "primitive", 0x4DB6ACFF);
        put(TinyVGItemType.TINYVG_TYPE, "tinyvg", 0x4DB6ACFF);
        put(SPRITE_TYPE, "sprite", 0xBA68C8FF);
        put(SpineItemType.SPINE_TYPE, "spine", 0xFF4000FF);
        put(PARTICLE_TYPE, "particle", 0xFF8A50FF);
        put(TalosItemType.TALOS_TYPE, "talos", 0xC42A5BFF);
        put(LIGHT_TYPE, "light", 0xFFE082FF);
    }

    private ItemsTreeIcons() {
    }

    private static void put(int type, String region, int rgba) {
        REGIONS.put(type, "icon-tree-" + region);
        COLORS.put(type, new Color(rgba));
    }

    static Drawable root() {
        return MenuIcons.get("icon-tree-root");
    }

    static Drawable forType(int entityType) {
        return MenuIcons.get(REGIONS.get(entityType, REGIONS.get(UNKNOWN_TYPE)));
    }

    static Color colorOf(int entityType) {
        return COLORS.get(entityType, COLORS.get(UNKNOWN_TYPE));
    }

    /** Null for a widget type that has no icon of its own yet: it then keeps the composite one. */
    static Drawable forWidget(String widgetType) {
        return MenuIcons.get("icon-tree-widget-" + widgetType);
    }
}
