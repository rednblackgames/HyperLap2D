package games.rednblack.editor.plugin.tiled.tools;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import org.puremvc.java.interfaces.INotification;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mariam on 4/5/16.
 */
public class DeleteTileTool implements Tool {

    public static final String NAME = "TILE_DELETE_TOOL";

    private TiledPlugin tiledPlugin;

    private boolean isHotswapped = false;

    public DeleteTileTool(TiledPlugin tiledPlugin) {
        this.tiledPlugin = tiledPlugin;
    }

    @Override
    public void initTool() {
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
    public boolean itemMouseDown(Entity entity, float x, float y) {
        deleteEntityWithCoordinate(x, y);
        return true;
    }

    @Override
    public void itemMouseUp(Entity entity, float x, float y) {

    }

    @Override
    public void itemMouseDragged(Entity entity, float x, float y) {
        deleteEntityWithCoordinate(x, y);
    }

    @Override
    public void itemMouseDoubleClick(Entity entity, float x, float y) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void handleNotification(INotification notification) {

    }

    @Override
    public void keyDown(Entity entity, int keycode) {

    }

    @Override
    public void keyUp(Entity entity, int keycode) {
        if(isHotswapped) {
            if(keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
                isHotswapped = false;
                tiledPlugin.getAPI().toolHotSwapBack();
            }
        }
    }

    Set<Entity> items = new HashSet<>();
    private void deleteEntity(Entity entity) {
        if (tiledPlugin.isTile(entity) && tiledPlugin.isOnCurrentSelectedLayer(entity)) {
            items.clear();
            items.add(entity);
            tiledPlugin.facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, items);
            tiledPlugin.facade.sendNotification(MsgAPI.ACTION_DELETE);
        }
    }

    private void deleteEntityWithCoordinate (float x, float y) {
        Entity entity = tiledPlugin.getPluginEntityWithCoords(x, y);
        if (entity != null) {
            deleteEntity(entity);
        }
    }

    public void setHotSwapped() {
        isHotswapped = true;
    }
}
