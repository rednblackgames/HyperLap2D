package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.data.WidgetPartVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Sets the role and the state overrides of a widget part as a whole. The part component is created
 * on demand; the state system shows the result on its next pass.
 */
public class UpdateWidgetPartDataCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private WidgetPartVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        WidgetPartVO vo = (WidgetPartVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        WidgetPartComponent part = SandboxComponentRetriever.get(entity, WidgetPartComponent.class);
        backup = new WidgetPartVO();
        if (part == null) {
            part = sandbox.getEngine().edit(entity).create(WidgetPartComponent.class);
        } else {
            backup.loadFromComponent(part);
        }

        vo.applyToComponent(part);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        WidgetPartComponent part = SandboxComponentRetriever.get(entity, WidgetPartComponent.class);
        if (part == null) return;

        // an empty part is harmless and is not saved, so it is simply left in place
        backup.applyToComponent(part);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, WidgetPartVO vo) {
        return new Object[]{entity, vo};
    }
}
