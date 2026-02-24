package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.data.LayoutConstraintVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

public class UpdateLayoutDataCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private LayoutConstraintVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        LayoutConstraintVO vo = (LayoutConstraintVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        LayoutComponent layoutComponent = SandboxComponentRetriever.get(entity, LayoutComponent.class);

        backup = new LayoutConstraintVO();
        backup.loadFromComponent(layoutComponent, sandbox.getEngine());

        applyVoToComponent(layoutComponent, vo, entity);

        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        LayoutComponent layoutComponent = SandboxComponentRetriever.get(entity, LayoutComponent.class);

        applyVoToComponent(layoutComponent, backup, entity);

        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    private void applyVoToComponent(LayoutComponent comp, LayoutConstraintVO vo, int entity) {
        comp.horizontalBias = vo.horizontalBias;
        comp.verticalBias = vo.verticalBias;

        comp.left = createConstraintData(vo.left, entity);
        comp.right = createConstraintData(vo.right, entity);
        comp.top = createConstraintData(vo.top, entity);
        comp.bottom = createConstraintData(vo.bottom, entity);
    }

    private LayoutComponent.ConstraintData createConstraintData(LayoutConstraintVO.ConstraintDataVO dataVO, int entity) {
        if (dataVO == null) return null;

        LayoutComponent.ConstraintData data = new LayoutComponent.ConstraintData();
        data.targetSide = dataVO.targetSide;
        data.margin = dataVO.margin;

        if (dataVO.targetUniqueId == null) {
            data.targetEntity = -1;
            data.resolved = true;
        } else {
            int target = EntityUtils.getByUniqueId(dataVO.targetUniqueId);
            if (target != -1) {
                data.targetEntity = target;
                data.resolved = true;
            } else {
                data.targetUniqueId = dataVO.targetUniqueId;
                data.targetEntity = -1;
                data.resolved = false;
            }
        }

        return data;
    }

    public static Object payload(int entity, LayoutConstraintVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;
        return payload;
    }
}
