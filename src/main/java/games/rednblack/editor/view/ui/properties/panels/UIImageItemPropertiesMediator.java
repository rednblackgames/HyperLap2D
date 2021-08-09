package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.component.UpdateImageItemDataCommand;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UIImageItemPropertiesMediator extends UIItemPropertiesMediator<UIImageItemProperties> {
    private static final String TAG = UIImageItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private TextureRegionComponent textureRegionComponent;

    public UIImageItemPropertiesMediator() {
        super(NAME, new UIImageItemProperties());
    }

    @Override
    protected void translateObservableDataToView(int item) {
        textureRegionComponent = SandboxComponentRetriever.get(item, TextureRegionComponent.class);

        if(textureRegionComponent.isRepeat) {
            viewComponent.setRenderMode("REPEAT");
        } else {
            viewComponent.setRenderMode("SINGLE");
        }

        if(textureRegionComponent.isPolygon) {
            viewComponent.setSpriteType("POLYGON");
        } else {
            viewComponent.setSpriteType("SQUARE");
        }
    }

    @Override
    protected void translateViewToItemData() {
        SimpleImageVO oldPayloadVo = new SimpleImageVO();
        oldPayloadVo.loadFromComponent(textureRegionComponent);

        SimpleImageVO payloadVo = new SimpleImageVO();
        payloadVo.isRepeat = viewComponent.getRenderMode().equals("REPEAT");
        payloadVo.isPolygon = viewComponent.getSpriteType().equals("POLYGON");

        if (oldPayloadVo.isRepeat != payloadVo.isRepeat || oldPayloadVo.isPolygon != payloadVo.isPolygon) {
            Object payload = UpdateImageItemDataCommand.payload(observableReference, payloadVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_IMAGE_ITEM_DATA, payload);
        }
    }
}
