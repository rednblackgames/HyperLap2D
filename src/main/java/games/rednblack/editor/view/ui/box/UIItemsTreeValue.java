package games.rednblack.editor.view.ui.box;

public class UIItemsTreeValue {
    public String entityId = null;
    public int zIndex = -1;
    /** Layer of the entity — z-index is confined per layer, so drag reorder is only valid within it. */
    public String layerName = "";

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public void reset() {
        entityId = null;
        zIndex = -1;
        layerName = "";
    }
}
