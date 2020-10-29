package games.rednblack.editor.plugin.tiled.tools;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.util.OsUtils;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.TransformCommandBuilder;
import games.rednblack.h2d.common.command.UpdateRegionCommandBuilder;
import games.rednblack.h2d.common.view.tools.Tool;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by mariam on 3/29/16.
 */
public class DrawTileTool implements Tool {

    public static final String NAME = "TILE_ADD_TOOL";

    private TiledPlugin tiledPlugin;
    private float gridWidth;
    private float gridHeight;

    public DrawTileTool(TiledPlugin tiledPlugin) {
        this.tiledPlugin = tiledPlugin;
    }

    @Override
    public void initTool() {

    }

    @Override
    public String getShortcut() {
        return OsUtils.getShortcutFor(Input.Keys.CONTROL_LEFT, Input.Keys.B);
    }

    @Override
    public String getTitle() {
        return "Draw Tile Tool";
    }

    @Override
    public boolean stageMouseDown(float x, float y) {
        initGridThings();
        drawImage(x, y);
        return true;
    }

    @Override
    public void stageMouseUp(float x, float y) {
    }

    @Override
    public void stageMouseDragged(float x, float y) {
        drawImage(x, y);
    }

    @Override
    public void stageMouseDoubleClick(float x, float y) {

    }

    @Override
    public void stageMouseScrolled(int amount) {

    }

    @Override
    public boolean itemMouseDown(Entity entity, float x, float y) {
        initGridThings();
        drawOnEntity(entity, x, y);
        return true;
    }

    @Override
    public void itemMouseUp(Entity entity, float x, float y) {
    }

    @Override
    public void itemMouseDragged(Entity entity, float x, float y) {
        drawImage(x, y);
    }

    @Override
    public void itemMouseDoubleClick(Entity entity, float x, float y) {
        if (!tiledPlugin.isOnCurrentSelectedLayer(entity)) return;
        if (entity != null) {
            TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
            if (textureRegionComponent != null && tiledPlugin.isTile(entity)) {
                // there is already other tile under this one
                if (textureRegionComponent.regionName.equals(tiledPlugin.getSelectedTileName())) {
                    //rotate
                    TransformCommandBuilder commandBuilder = new TransformCommandBuilder();
                    commandBuilder.begin(entity);
                    TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
                    if (transformComponent.scaleX > 0 && transformComponent.scaleY > 0) {
                        commandBuilder.setScale(transformComponent.scaleX * -1f, transformComponent.scaleY);
                    } else if (transformComponent.scaleX < 0 && transformComponent.scaleY > 0) {
                        commandBuilder.setScale(transformComponent.scaleX, transformComponent.scaleY * -1f);
                    } else if (transformComponent.scaleX < 0 && transformComponent.scaleY < 0) {
                        commandBuilder.setScale(transformComponent.scaleX * -1f, transformComponent.scaleY);
                    } else if (transformComponent.scaleX > 0 && transformComponent.scaleY < 0) {
                        commandBuilder.setScale(transformComponent.scaleX, transformComponent.scaleY * -1f);
                    }
                    commandBuilder.execute(tiledPlugin.facade);
                }
            }
        }
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
        if(keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
            tiledPlugin.getAPI().toolHotSwap(tiledPlugin.deleteTileTool);
            tiledPlugin.deleteTileTool.setHotSwapped();
        }
    }

    @Override
    public void keyUp(Entity entity, int keycode) {

    }

    private void initGridThings() {
        gridWidth = tiledPlugin.dataToSave.getParameterVO().gridWidth;
        gridHeight = tiledPlugin.dataToSave.getParameterVO().gridHeight;
    }

    private void drawImage(float x, float y) {
        if (tiledPlugin.getSelectedTileName().equals("")) return;

        float newX = MathUtils.floor(x / gridWidth) * gridWidth + tiledPlugin.getSelectedTileGridOffset().x;
        float newY = MathUtils.floor(y / gridHeight) * gridHeight + tiledPlugin.getSelectedTileGridOffset().y;
        int row = MathUtils.floor(newY / gridHeight);
        int column = MathUtils.round(newX / gridWidth);

        Entity underneathTile = tiledPlugin.getPluginEntityWithParams(row, column);
        if (underneathTile != null) {
            drawOnEntity(underneathTile, x, y);
            return;
        }

        Entity imageEntity = tiledPlugin.getAPI().drawImage(tiledPlugin.getSelectedTileName(), new Vector2(newX, newY));
        MainItemComponent mainItemComponent = ComponentRetriever.get(imageEntity, MainItemComponent.class);
        mainItemComponent.tags.add(TiledPlugin.TILE_TAG);

        mainItemComponent.setCustomVars(TiledPlugin.ROW, Integer.toString(row));
        mainItemComponent.setCustomVars(TiledPlugin.COLUMN, Integer.toString(column));
    }

    private void drawOnEntity(Entity entity, float x, float y) {
        if (!tiledPlugin.isOnCurrentSelectedLayer(entity)) return;
        if (entity != null) {
            TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
            if (textureRegionComponent != null && textureRegionComponent.regionName != null
                    && tiledPlugin.isTile(entity)) {
                // there is already other tile under this one
                if(textureRegionComponent.regionName.equals(tiledPlugin.getSelectedTileName())) {
                    return;
                } else {
                    //replace
                    updateRegion(entity, tiledPlugin.getSelectedTileName());
                }
            }
            return;
        }
        drawImage(x, y);
    }

    private void updateRegion(Entity entity, String region) {
        UpdateRegionCommandBuilder builder = new UpdateRegionCommandBuilder();
        builder.begin(entity);
        builder.setRegion(tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region));
        builder.setRegionName(tiledPlugin.getSelectedTileName());
        builder.execute(tiledPlugin.facade);
    }

}
