package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.data.SensorDataVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
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
        Entity entity = (Entity) payload[0];
        SensorDataVO vo = (SensorDataVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        SensorComponent sensorComponent = ComponentRetriever.get(entity, SensorComponent.class);

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

        sensorComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);
        SensorComponent sensorComponent = ComponentRetriever.get(entity, SensorComponent.class);

        sensorComponent.bottom = backup.bottom;
        sensorComponent.left = backup.left;
        sensorComponent.right = backup.right;
        sensorComponent.top = backup.top;

        sensorComponent.bottomSpanPercent = backup.bottomSpanPercent;
        sensorComponent.leftSpanPercent = backup.leftSpanPercent;
        sensorComponent.rightSpanPercent = backup.rightSpanPercent;
        sensorComponent.topSpanPercent = backup.topSpanPercent;
        
        sensorComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(Entity entity, SensorDataVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
