package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.data.WidgetVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateWidgetDataCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private WidgetVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        WidgetVO vo = (WidgetVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        WidgetComponent widgetComponent = SandboxComponentRetriever.get(entity, WidgetComponent.class);
        if (widgetComponent == null) {
            cancel();
            return;
        }

        backup = new WidgetVO();
        backup.loadFromComponent(widgetComponent);

        vo.applyToComponent(widgetComponent);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        WidgetComponent widgetComponent = SandboxComponentRetriever.get(entity, WidgetComponent.class);
        if (widgetComponent == null) return;

        backup.applyToComponent(widgetComponent);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, WidgetVO vo) {
        return new Object[]{entity, vo};
    }
}
