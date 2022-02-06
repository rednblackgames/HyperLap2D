package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.data.SensorDataVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Update command for the sensor data.
 * 
 * @author Jan-Thierry Wegener
 */
public class UpdateSensorDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private SensorDataVO backup;
    
    public UpdateSensorDataCommand() {
    }

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        SensorDataVO vo = (SensorDataVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        SensorComponent sensorComponent = SandboxComponentRetriever.get(entity, SensorComponent.class);

        backup = new SensorDataVO();
        backup.loadFromComponent(sensorComponent);

        sensorComponent.bottom = vo.bottom;
        sensorComponent.left = vo.left;
        sensorComponent.right = vo.right;
        sensorComponent.top = vo.top;

        sensorComponent.bottomSpanPercent = vo.bottomSpanPercent;
        sensorComponent.leftSpanPercent = vo.leftSpanPercent;
        sensorComponent.rightSpanPercent = vo.rightSpanPercent;
        sensorComponent.topSpanPercent = vo.topSpanPercent;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        SensorComponent sensorComponent = SandboxComponentRetriever.get(entity, SensorComponent.class);

        sensorComponent.bottom = backup.bottom;
        sensorComponent.left = backup.left;
        sensorComponent.right = backup.right;
        sensorComponent.top = backup.top;

        sensorComponent.bottomSpanPercent = backup.bottomSpanPercent;
        sensorComponent.leftSpanPercent = backup.leftSpanPercent;
        sensorComponent.rightSpanPercent = backup.rightSpanPercent;
        sensorComponent.topSpanPercent = backup.topSpanPercent;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, SensorDataVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
