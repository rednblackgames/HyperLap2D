package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

public interface IDrawStrategy {
    void drawTile(float x, float y, int row, int column);
    void updateTile(int entity);
}
