package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntMap;
import games.rednblack.editor.proxy.EntityDataProxy;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.utils.MenuIcons;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;

import static games.rednblack.editor.renderer.factory.EntityFactory.*;

/**
 * The items tree's own icons: all of one size, drawn white and tinted by kind, the way Spine's tree tells a bone
 * from a slot at a glance. Widgets share the theme blue so they stand apart from plain composites. The breadcrumb
 * bar and the properties panel show the same icons, untinted ({@link #forEntity(int)}).
 */
public final class ItemsTreeIcons {

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
        return root(false);
    }

    static Drawable forType(int entityType) {
        return forType(entityType, false);
    }

    static Color colorOf(int entityType) {
        return COLORS.get(entityType, COLORS.get(UNKNOWN_TYPE));
    }

    /** Null for a widget type that has no icon of its own yet: it then keeps the composite one. */
    static Drawable forWidget(String widgetType) {
        return forWidget(widgetType, false);
    }

    /** The icon the tree gives the entity: the scene's for the root, a widget's own, or its kind's. */
    public static Drawable forEntity(int entity) {
        return forEntity(entity, false);
    }

    /** The same, 28 pixels instead of 14 with {@code large}, for the properties panel's title. */
    public static Drawable forEntity(int entity, boolean large) {
        EntityDataProxy entityData = EntityDataProxy.get();
        if (entityData.get(entity, ParentNodeComponent.class) == null) return root(large);
        WidgetComponent widget = entityData.get(entity, WidgetComponent.class);
        Drawable widgetIcon = widget != null ? forWidget(widget.widgetType, large) : null;
        return widgetIcon != null ? widgetIcon : forType(entityData.metadata().getType(entity), large);
    }

    private static Drawable root(boolean large) {
        return sized("icon-tree-root", large);
    }

    private static Drawable forType(int entityType, boolean large) {
        return sized(REGIONS.get(entityType, REGIONS.get(UNKNOWN_TYPE)), large);
    }

    private static Drawable forWidget(String widgetType, boolean large) {
        return sized("icon-tree-widget-" + widgetType, large);
    }

    private static Drawable sized(String region, boolean large) {
        return MenuIcons.get(large ? region + "-large" : region);
    }
}
