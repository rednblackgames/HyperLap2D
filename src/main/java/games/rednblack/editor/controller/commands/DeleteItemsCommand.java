package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/28/2015.
 */
public class DeleteItemsCommand extends EntityModifyRevertibleCommand {

    private String backup;
    private Array<String> entityIdsToDelete;

    private void backup() {
        Set<Integer> entitySet = new HashSet<>();
        if(entityIdsToDelete == null) {
            entityIdsToDelete = new Array<>();
            entitySet = sandbox.getSelector().getSelectedItems();
            for(int entity: entitySet) {
                entityIdsToDelete.add(EntityUtils.getEntityId(entity));
            }
        } else {
            for(String entityId: entityIdsToDelete) {
                entitySet.add(EntityUtils.getByUniqueId(entityId));
            }
        }

        backup = EntityUtils.getJsonStringFromEntities(entitySet);
    }

    @Override
    public void doAction() {
        backup();

        FollowersUIMediator followersUIMediator = Facade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        for (String entityId : entityIdsToDelete) {
            int item = EntityUtils.getByUniqueId(entityId);
            followersUIMediator.removeFollower(item);
            sandbox.getEngine().delete(item);
        }
        sandbox.getEngine().process();

        sandbox.getSelector().getCurrentSelection().clear();

        facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
    }

    @Override
    public void undoAction() {
        Json json = HyperJson.getJson();
        CompositeItemVO compositeVO = json.fromJson(CompositeItemVO.class, backup);
        Set<Integer> newEntitiesList = PasteItemsCommand.createEntitiesFromVO(compositeVO);

        sandbox.getEngine().process();
        for (int entity : newEntitiesList) {
            Facade.getInstance().sendNotification(MsgAPI.NEW_ITEM_ADDED, entity);
        }

        facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, newEntitiesList);
    }

    public void setItemsToDelete(Set<Integer> entities) {
        entityIdsToDelete = new Array<>();
        for(int entity: entities) {
            entityIdsToDelete.add(EntityUtils.getEntityId(entity));
        }
    }
}
