package games.rednblack.editor.renderer.components;

public class ZIndexComponent implements BaseComponent {
    private int zIndex = 0;
    public boolean needReOrder = false;
    public String layerName = "";
    public int layerIndex;

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
        needReOrder = true;
    }

    public int getGlobalZIndex() {
        return layerIndex + zIndex;
    }

    @Override
    public void reset() {
        zIndex = 0;
        needReOrder = false;
        layerName = "";
        layerIndex = 0;
    }
}