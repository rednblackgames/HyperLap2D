package games.rednblack.editor.plugin.tiled.tools;

import java.util.HashSet;
import java.util.Set;

import org.puremvc.java.interfaces.INotification;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.h2d.common.vo.CursorData;

/**
 * Created by mariam on 4/5/16.
 */
public class DeleteTileTool implements Tool {
    private static final CursorData CURSOR = new CursorData("tile-eraser-cursor", 14, 14);
    public static final String NAME = "TILE_DELETE_TOOL";

    private TiledPlugin tiledPlugin;

    private boolean isHotswapped = false;

    public DeleteTileTool(TiledPlugin tiledPlugin) {
        this.tiledPlugin = tiledPlugin;
    }

    @Override
    public void initTool() {
        Texture cursorTexture = tiledPlugin.pluginRM.getTexture(CURSOR.region);
        tiledPlugin.getAPI().setCursor(CURSOR, new TextureRegion(cursorTexture));
    }

    @Override
    public String getShortcut() {
        return null;
    }

    @Override
    public String getTitle() {
        return "Delete Tile Tool";
    }

    @Override
    public boolean stageMouseDown(float x, float y) {
        return true;
    }

    @Override
    public void stageMouseUp(float x, float y) {
    	tiledPlugin.facade.sendNotification(TiledPlugin.AUTO_FILL_TILES);
    }

    @Override
    public void stageMouseDragged(float x, float y) {
        deleteEntityWithCoordinate(x, y);
    }

    @Override
    public void stageMouseDoubleClick(float x, float y) {

    }

    @Override
    public boolean stageMouseScrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean itemMouseDown(int entity, float x, float y) {
        deleteEntityWithCoordinate(x, y);
        tiledPlugin.facade.sendNotification(TiledPlugin.AUTO_FILL_TILES);
        return true;
    }

    @Override
    public void itemMouseUp(int entity, float x, float y) {
    	tiledPlugin.facade.sendNotification(TiledPlugin.AUTO_FILL_TILES);
    }

    @Override
    public void itemMouseDragged(int entity, float x, float y) {
        deleteEntityWithCoordinate(x, y);
    }

    @Override
    public void itemMouseDoubleClick(int entity, float x, float y) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void handleNotification(INotification notification) {

    }

    @Override
    public void keyDown(int entity, int keycode) {

    }

    @Override
    public void keyUp(int entity, int keycode) {
        if(isHotswapped) {
            if(keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
                isHotswapped = false;
                tiledPlugin.getAPI().toolHotSwapBack();
            }
        }
    }

    Set<Integer> items = new HashSet<>();
    private void deleteEntity(int entity) {
        if (tiledPlugin.isTile(entity) && tiledPlugin.isOnCurrentSelectedLayer(entity)) {
            items.clear();
            items.add(entity);
            tiledPlugin.facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, items);
            tiledPlugin.facade.sendNotification(MsgAPI.ACTION_DELETE);
        }
    }

    private void deleteEntityWithCoordinate (float x, float y) {
        int entity = tiledPlugin.getPluginEntityWithCoords(x, y);
        if (entity != -1) {
            deleteEntity(entity);
        }
    }

    public void setHotSwapped() {
        isHotswapped = true;
    }
}
