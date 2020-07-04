package games.rednblack.editor.live;

public class WorldSizeVO {
    public int width;
    public int height;
    public int ppwu;

    public WorldSizeVO(int ppwu, int width, int height) {
        this.height = height;
        this.width = width;
        this.ppwu = ppwu;
    }

    public int getWorldWidth() {
        return width / ppwu;
    }

    public int getWorldHeight() {
        return height / ppwu;
    }
}
