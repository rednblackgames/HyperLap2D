package games.rednblack.editor.view.ui.box.resourcespanel.draggable.box;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.view.ui.box.resourcespanel.UIImagesTabMediator;
import games.rednblack.h2d.common.ResourcePayloadObject;

public class AtlasResource extends BoxItemResource {

    private final ResourcePayloadObject payload;

    public AtlasResource(TextureAtlas atlas, String atlasName) {
        super(true);

        Image folderIcon = new Image(VisUI.getSkin().getDrawable(atlasName.equals("main") ? "icon-atlas-back-folder" : "icon-atlas-folder"));
        addActor(folderIcon);
        folderIcon.setX((getWidth() - folderIcon.getWidth()) / 2);
        folderIcon.setY((getHeight() - folderIcon.getHeight()) / 2);

        payload = new ResourcePayloadObject();
        payload.name = atlasName;

        setClickEvent(UIImagesTabMediator.CHANGE_FOLDER, atlasName, null, null);
    }

    @Override
    public Actor getDragActor() {
        return null;
    }

    @Override
    public ResourcePayloadObject getPayloadData() {
        return payload;
    }
}
