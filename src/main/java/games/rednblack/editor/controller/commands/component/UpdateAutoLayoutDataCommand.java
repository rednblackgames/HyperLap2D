package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.data.AutoLayoutVO;
import games.rednblack.editor.utils.runtime.AutoLayoutEditing;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

public class UpdateAutoLayoutDataCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private AutoLayoutVO backup;
    private AutoLayoutEditing.ChildrenSnapshot children;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        AutoLayoutVO vo = (AutoLayoutVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        AutoLayoutComponent component = SandboxComponentRetriever.get(entity, AutoLayoutComponent.class);
        backup = new AutoLayoutVO();
        backup.loadFromComponent(component);
        children = new AutoLayoutEditing.ChildrenSnapshot(entity);

        vo.applyToComponent(component);
        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        AutoLayoutComponent component = SandboxComponentRetriever.get(entity, AutoLayoutComponent.class);
        if (component == null) return;

        children.restore();
        backup.applyToComponent(component);
        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, AutoLayoutVO vo) {
        return new Object[]{entity, vo};
    }
}
