package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

import java.util.Set;

/**
 * Created by azakhary on 8/18/2015.
 * Plugin to edit Nine Patch data on imported texture region
 */
@Implementation(authors = "azakhary", version = "0.0.1")
public class NinePatchPlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.ninepatch.NinePatchPlugin";

    public static final String EDIT_NINE_PATCH = CLASS_NAME + ".EDIT_NINE_PATCH";
    public static final String CONVERT_TO_NINE_PATCH = CLASS_NAME + ".CONVERT_TO_NINE_PATCH";

    private MainPanelMediator performancePanelMediator;

    public int currEditingEntity;

    public NinePatchPlugin() {
        super(CLASS_NAME);
    }

    @Override
    public void initPlugin() {
        performancePanelMediator = new MainPanelMediator(this);
        facade.registerMediator(performancePanelMediator);
        pluginAPI.setDropDownItemName(EDIT_NINE_PATCH, "Edit NinePatch");
        pluginAPI.setDropDownItemName(CONVERT_TO_NINE_PATCH, "Convert to NinePatch");
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
            if(mainItemComponent.entityType == EntityFactory.IMAGE_TYPE) {
                // it's our guy
                currEditingEntity = entity;
                actionsSet.add(CONVERT_TO_NINE_PATCH);
            }
        }
    }
}
