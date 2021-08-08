package games.rednblack.editor.view.ui.box;

public class UIItemsTreeValue {
    public int entityId = -1;
    public int zIndex = -1;

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public void reset() {
        entityId = -1;
        zIndex = -1;
    }
}
