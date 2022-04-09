package games.rednblack.editor.view.ui.box.resourcespanel.draggable.box;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import dev.lyze.gdxtinyvg.TinyVG;
import dev.lyze.gdxtinyvg.TinyVGIO;
import dev.lyze.gdxtinyvg.drawers.TinyVGShapeDrawer;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.h2d.common.ResourcePayloadObject;

public class TinyVGResource extends BoxItemResource {
    private final Image payloadImg;
    private final ResourcePayloadObject payload;

    public TinyVGResource(String name, TinyVG origTvg, TinyVGShapeDrawer shapeDrawer) {
        super(true);

        TextureRegion region = TinyVGIO.toTextureRegion(origTvg, shapeDrawer, 2);
        Image img = new Image(region);
        if (img.getWidth() > thumbnailSize || img.getHeight() > thumbnailSize) {
            // resizing is needed
            float scaleFactor = 1.0f;
            if (img.getWidth() > img.getHeight()) {
                //scale by width
                scaleFactor = 1.0f / (img.getWidth() / thumbnailSize);
            } else {
                scaleFactor = 1.0f / (img.getHeight() / thumbnailSize);
            }
            img.setScale(scaleFactor);

            img.setX((getWidth() - img.getWidth() * img.getScaleX()) / 2);
            img.setY((getHeight() - img.getHeight() * img.getScaleY()) / 2);
        } else {
            // put it in middle
            img.setX((getWidth() - img.getWidth()) / 2);
            img.setY((getHeight() - img.getHeight()) / 2);
        }

        addActor(img);

        setRightClickEvent(UIResourcesBoxMediator.TINY_VG_RIGHT_CLICK, name);

        payloadImg = new Image(region);
        payload = new ResourcePayloadObject();
        payload.name = name;
        payload.className = getClass().getName();
    }

    @Override
    public Actor getDragActor() {
        return payloadImg;
    }

    @Override
    public ResourcePayloadObject getPayloadData() {
        return payload;
    }
}
