package games.rednblack.editor.view.ui.box;

public class UIItemsTreeValue {
    public String entityId = null;
    public int zIndex = -1;

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public void reset() {
        entityId = null;
        zIndex = -1;
    }
}
