package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

import java.util.Set;

/**
 * Created by azakhary on 8/18/2015.
 * Plugin to edit Nine Patch data on imported texture region: stretch areas (multiple per axis, TenPatch style),
 * tiling with tile offset and scroll speed, crush mode and corner gradient.
 */
@Implementation(authors = "azakhary", version = "0.1.0")
public class NinePatchPlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.ninepatch.NinePatchPlugin";

    public static final String EDIT_NINE_PATCH = CLASS_NAME + ".EDIT_NINE_PATCH";
    public static final String CONVERT_TO_NINE_PATCH = CLASS_NAME + ".CONVERT_TO_NINE_PATCH";

    /** Base name of the atlas packed from {@code assets/textures} and bundled in the plugin jar. */
    private static final String ATLAS_NAME = "ninepatch";

    private MainPanelMediator performancePanelMediator;

    public int currEditingEntity;

    public NinePatchPlugin() {
        super(CLASS_NAME);
    }

    @Override
    public void initPlugin() {
        performancePanelMediator = new MainPanelMediator(this);
        facade.registerMediator(performancePanelMediator);
        TextureAtlas pluginAtlas = loadPluginAtlas(ATLAS_NAME);
        pluginAPI.setDropDownItemName(EDIT_NINE_PATCH, "Edit NinePatch", atlasDrawable(pluginAtlas, "icon-menu-ninepatch"));
        pluginAPI.setDropDownItemName(CONVERT_TO_NINE_PATCH, "Convert to NinePatch", atlasDrawable(pluginAtlas, "icon-menu-ninepatch-convert"));
    }


    @Override
    public void onDropDownOpen(Set<Integer> selectedEntities, Array<String> actionsSet) {
        if(selectedEntities.size() == 1) {
            int entity = selectedEntities.stream().findFirst().get();
            MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, getAPI().getEngine());

            if(mainItemComponent.entityType == EntityFactory.NINE_PATCH) {
                // it's our guy
                currEditingEntity = entity;
                actionsSet.add(EDIT_NINE_PATCH);
            }
            if(mainItemComponent.entityType == EntityFactory.IMAGE_TYPE
                    || mainItemComponent.entityType == EntityFactory.SPRITE_TYPE) {
                // images become still 9-patches, sprite animations become animated ones
                currEditingEntity = entity;
                actionsSet.add(CONVERT_TO_NINE_PATCH);
            }
        }
    }
}
