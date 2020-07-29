package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateImageItemDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private SimpleImageVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        SimpleImageVO vo = (SimpleImageVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);

        backup = new SimpleImageVO();
        backup.loadFromComponent(textureRegionComponent);

        textureRegionComponent.isRepeat = vo.isRepeat;
        textureRegionComponent.isPolygon = vo.isPolygon;

        updateEntity(entity);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);
        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);

        textureRegionComponent.isRepeat = backup.isRepeat;
        textureRegionComponent.isPolygon = backup.isPolygon;

        updateEntity(entity);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    private void updateEntity(Entity entity) {
        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);

        if (textureRegionComponent.isPolygon) {
            PolygonComponent polygonComponent = ComponentRetriever.get(entity, PolygonComponent.class);
            TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

            if (polygonComponent != null && polygonComponent.vertices != null) {
                float ppwu = dimensionsComponent.width/textureRegionComponent.region.getRegionWidth();
                textureRegionComponent.setPolygonSprite(polygonComponent,1f/ppwu, transformComponent.scaleX, transformComponent.scaleY);
                dimensionsComponent.setPolygon(polygonComponent);
            }
        } else {
            textureRegionComponent.polygonSprite = null;
            dimensionsComponent.polygon = null;
        }
    }

    public static Object payload(Entity entity, SimpleImageVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
