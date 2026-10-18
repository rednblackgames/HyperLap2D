package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.AutoLayoutItemComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.data.AutoLayoutItemVO;
import games.rednblack.editor.utils.runtime.AutoLayoutEditing;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

/** Changes what a child asks of its parent's auto layout, giving it the component the first time. */
public class UpdateAutoLayoutItemDataCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    /** null when the child had no settings of its own before */
    private AutoLayoutItemVO backup;
    private AutoLayoutEditing.ChildrenSnapshot siblings;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        AutoLayoutItemVO vo = (AutoLayoutItemVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        siblings = parentNode != null && parentNode.parentEntity != -1 ? new AutoLayoutEditing.ChildrenSnapshot(parentNode.parentEntity) : null;

        AutoLayoutItemComponent component = SandboxComponentRetriever.get(entity, AutoLayoutItemComponent.class);
        if (component == null) {
            backup = null;
            component = sandbox.getEngine().edit(entity).create(AutoLayoutItemComponent.class);
        } else {
            backup = new AutoLayoutItemVO();
            backup.loadFromComponent(component);
        }

        vo.applyToComponent(component);
        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        if (entity == -1) return;

        if (siblings != null) siblings.restore();
        if (backup == null) {
            sandbox.getEngine().edit(entity).remove(AutoLayoutItemComponent.class);
        } else {
            AutoLayoutItemComponent component = SandboxComponentRetriever.get(entity, AutoLayoutItemComponent.class);
            if (component != null) backup.applyToComponent(component);
        }
        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, AutoLayoutItemVO vo) {
        return new Object[]{entity, vo};
    }
}
