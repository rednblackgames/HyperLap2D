package games.rednblack.editor.proxy;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.components.LayerMapComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;

import java.util.HashMap;
import java.util.HashSet;

import static games.rednblack.editor.renderer.factory.EntityFactory.*;

/**
 * Read-only metadata accessors for the runtime ECS, exposed to the view layer through
 * {@link EntityDataProxy#metadata()} (Phase 3 decoupling). This is the view-facing
 * replacement for the static {@code games.rednblack.editor.utils.runtime.EntityUtils}
 * metadata methods — the view no longer references {@code EntityUtils.} directly, while
 * non-view/command callers keep using {@code EntityUtils} (unchanged).
 *
 * <p>Built on {@link EntityDataProxy#get(int, Class)} and {@link PluginUIBridge}, the same
 * access path {@code EntityUtils} already uses. Write paths stay in commands — callers must
 * not mutate through {@link #getLayer(int)} (its returned {@link LayerItemVO} is live).</p>
 */
public class EntityMetadata {

    /** Type → display-name map (view-facing copy; {@code EntityUtils} keeps its own for non-view callers). */
    public static final HashMap<Integer, String> itemTypeNameMap = new HashMap<>();
    /** Type → icon-skin-name map (view-facing copy). */
    public static final HashMap<Integer, String> itemTypeIconMap = new HashMap<>();
    static {
        itemTypeNameMap.put(UNKNOWN_TYPE, "Unknown");
        itemTypeNameMap.put(COMPOSITE_TYPE, "Composite Item");
        itemTypeNameMap.put(PARTICLE_TYPE, "Particle Effect");
        itemTypeNameMap.put(LABEL_TYPE, "Label");
        itemTypeNameMap.put(IMAGE_TYPE, "Image");
        itemTypeNameMap.put(NINE_PATCH, "9-Patch Image");
        itemTypeNameMap.put(LIGHT_TYPE, "Light");
        itemTypeNameMap.put(SpineItemType.SPINE_TYPE, "Spine Animation");
        itemTypeNameMap.put(SPRITE_TYPE, "Sprite Animation");
        itemTypeNameMap.put(COLOR_PRIMITIVE, "Primitive");
        itemTypeNameMap.put(TalosItemType.TALOS_TYPE, "Talos VFX");
        itemTypeNameMap.put(TinyVGItemType.TINYVG_TYPE, "TinyVG Image");

        itemTypeIconMap.put(UNKNOWN_TYPE, "icon-unknown");
        itemTypeIconMap.put(COMPOSITE_TYPE, "icon-composite2");
        itemTypeIconMap.put(PARTICLE_TYPE, "icon-particle-white");
        itemTypeIconMap.put(LABEL_TYPE, "icon-label");
        itemTypeIconMap.put(IMAGE_TYPE, "icon-image");
        itemTypeIconMap.put(NINE_PATCH, "icon-image");
        itemTypeIconMap.put(LIGHT_TYPE, "icon-light");
        itemTypeIconMap.put(SpineItemType.SPINE_TYPE, "icon-spine");
        itemTypeIconMap.put(SPRITE_TYPE, "icon-animation");
        itemTypeIconMap.put(COLOR_PRIMITIVE, "icon-image");
        itemTypeIconMap.put(TalosItemType.TALOS_TYPE, "icon-talos");
        itemTypeIconMap.put(TinyVGItemType.TINYVG_TYPE, "icon-tinyvg");
    }

    private final EntityDataProxy entityData;

    EntityMetadata(EntityDataProxy entityData) {
        this.entityData = entityData;
    }

    public int getType(int entity) {
        MainItemComponent mainItemComponent = entityData.get(entity, MainItemComponent.class);
        if (mainItemComponent == null)
            return UNKNOWN_TYPE;
        return mainItemComponent.entityType;
    }

    public String getName(int entity) {
        ParentNodeComponent parentNodeComponent = entityData.get(entity, ParentNodeComponent.class);
        if (parentNodeComponent == null)
            return PluginUIBridge.get().getSandbox().getSceneControl().getCurrentSceneVO().sceneName;

        MainItemComponent mainItemComponent = entityData.get(entity, MainItemComponent.class);
        if (mainItemComponent.itemIdentifier != null && !mainItemComponent.itemIdentifier.isEmpty()) {
            return mainItemComponent.itemIdentifier;
        } else {
            int type = getType(entity);
            String name = itemTypeNameMap.get(type);
            if (name != null)
                return name;
            else
                return itemTypeNameMap.get(UNKNOWN_TYPE);
        }
    }

    public String getUniqueId(int entity) {
        MainItemComponent mainItemComponent = entityData.get(entity, MainItemComponent.class);
        return mainItemComponent.uniqueId;
    }

    public Array<String> getUniqueIds(Iterable<Integer> entities) {
        Array<String> entityIds = new Array<>();
        for (int entity : entities) {
            MainItemComponent mainItemComponent = entityData.get(entity, MainItemComponent.class);
            if (mainItemComponent != null)
                entityIds.add(mainItemComponent.uniqueId);
        }
        return entityIds;
    }

    public int getByUniqueId(String id) {
        return getEntityFactory().getEntityByUniqueId(id);
    }

    public HashSet<Integer> getByUniqueId(Array<String> ids) {
        HashSet<Integer> entities = new HashSet<>();
        for (String id : ids) {
            entities.add(getEntityFactory().getEntityByUniqueId(id));
        }
        return entities;
    }

    /** Returns the entity's layer. <b>Read-only contract</b> — do not mutate the returned {@link LayerItemVO}. */
    public LayerItemVO getLayer(int entity) {
        ZIndexComponent zIndexComponent = entityData.get(entity, ZIndexComponent.class);
        LayerMapComponent layerMapComponent = entityData.get(
                entityData.get(entity, ParentNodeComponent.class).parentEntity, LayerMapComponent.class);
        return layerMapComponent.getLayer(zIndexComponent.layerHash);
    }

    public Drawable getIcon(int entityType) {
        String icon = itemTypeIconMap.get(entityType);
        return VisUI.getSkin().getDrawable(icon);
    }

    public String getTypeName(int entityType) {
        return itemTypeNameMap.get(entityType);
    }

    public String getTypeIconName(int entityType) {
        return itemTypeIconMap.get(entityType);
    }

    private EntityFactory getEntityFactory() {
        return PluginUIBridge.get().getSandbox().getSceneControl().sceneLoader.getEntityFactory();
    }
}