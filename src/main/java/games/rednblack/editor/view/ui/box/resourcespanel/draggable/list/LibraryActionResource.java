package games.rednblack.editor.view.ui.box.resourcespanel.draggable.list;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ResourcePayloadObject;

public class LibraryActionResource extends ListItemResource {

    private final Image payloadImg;
    private final ResourcePayloadObject payload;
    private String key;

    public LibraryActionResource(String key) {
        super(key, "library");
        this.key = key;
        payloadImg = new Image(getStyle().resourceOver);
        payloadImg.setScale(2);
        payloadImg.getColor().a = .85f;
        payload = new ResourcePayloadObject();
        payload.name = key;
        payload.className = getClass().getName();

        setRightClickEvent(UIResourcesBoxMediator.LIBRARY_ACTION_RIGHT_CLICK, payload.name);
        setDoubleClickEvent(MsgAPI.OPEN_NODE_EDITOR, payload.name);
    }

    public String getKey() {
        return key;
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
