package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.talos.TalosAnchorConstraintComponent;
import games.rednblack.h2d.extension.talos.TalosAnchorConstraintVO;
import games.rednblack.puremvc.Facade;

public class UpdateTalosAnchorConstraintCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private TalosAnchorConstraintVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        TalosAnchorConstraintVO vo = (TalosAnchorConstraintVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        TalosAnchorConstraintComponent comp = SandboxComponentRetriever.get(entity, TalosAnchorConstraintComponent.class);

        backup = new TalosAnchorConstraintVO();
        backup.loadFromComponent(comp, sandbox.getEngine());

        applyVoToComponent(comp, vo, entity);

        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        TalosAnchorConstraintComponent comp = SandboxComponentRetriever.get(entity, TalosAnchorConstraintComponent.class);

        applyVoToComponent(comp, backup, entity);

        sandbox.getEngine().process();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    private void applyVoToComponent(TalosAnchorConstraintComponent comp, TalosAnchorConstraintVO vo, int entity) {
        comp.bindings.clear();
        comp.checksum = 0;

        if (vo.bindings == null) return;

        for (TalosAnchorConstraintVO.AnchorBindingVO bvo : vo.bindings) {
            TalosAnchorConstraintComponent.AnchorBinding ab = new TalosAnchorConstraintComponent.AnchorBinding();
            ab.scopeKey = bvo.scopeKey;
            ab.horizontalBias = bvo.horizontalBias;
            ab.verticalBias = bvo.verticalBias;
            ab.left = createConstraintData(bvo.left, entity);
            ab.right = createConstraintData(bvo.right, entity);
            ab.top = createConstraintData(bvo.top, entity);
            ab.bottom = createConstraintData(bvo.bottom, entity);
            comp.bindings.add(ab);
        }
    }

    private TalosAnchorConstraintComponent.ConstraintData createConstraintData(
            TalosAnchorConstraintVO.ConstraintDataVO dataVO, int entity) {
        if (dataVO == null) return null;

        TalosAnchorConstraintComponent.ConstraintData data = new TalosAnchorConstraintComponent.ConstraintData();
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

    public static Object payload(int entity, TalosAnchorConstraintVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;
        return payload;
    }
}
