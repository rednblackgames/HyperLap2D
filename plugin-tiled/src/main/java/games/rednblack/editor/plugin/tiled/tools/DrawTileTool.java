package games.rednblack.editor.plugin.tiled.tools;

import games.rednblack.h2d.extension.spine.SpineItemType;
import org.puremvc.java.interfaces.INotification;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.kotcrab.vis.ui.util.OsUtils;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.tools.drawStrategy.AutoTileDrawStrategy;
import games.rednblack.editor.plugin.tiled.tools.drawStrategy.IDrawStrategy;
import games.rednblack.editor.plugin.tiled.tools.drawStrategy.ImageDrawStrategy;
import games.rednblack.editor.plugin.tiled.tools.drawStrategy.SpineDrawStrategy;
import games.rednblack.editor.plugin.tiled.tools.drawStrategy.SpriteDrawStrategy;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.TransformCommandBuilder;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.h2d.common.vo.CursorData;

/**
 * Created by mariam on 3/29/16.
 */
public class DrawTileTool implements Tool {
    private static final CursorData CURSOR = new CursorData("tile-cursor", 14, 14);
    public static final String NAME = "TILE_ADD_TOOL";

    private TiledPlugin tiledPlugin;
    private float gridWidth;
    private float gridHeight;

    private final ImageDrawStrategy imageDrawStrategy;
    private final SpriteDrawStrategy spriteDrawStrategy;
    private final SpineDrawStrategy spineDrawStrategy;
    private final AutoTileDrawStrategy autoTileDrawStrategy;
    private IDrawStrategy currentDrawStrategy;

    public DrawTileTool(TiledPlugin tiledPlugin) {
        this.tiledPlugin = tiledPlugin;
        imageDrawStrategy = new ImageDrawStrategy(tiledPlugin);
        spriteDrawStrategy = new SpriteDrawStrategy(tiledPlugin);
        spineDrawStrategy = new SpineDrawStrategy(tiledPlugin);
        autoTileDrawStrategy = new AutoTileDrawStrategy(tiledPlugin);
    }

    @Override
    public void initTool() {
        Texture cursorTexture = tiledPlugin.pluginRM.getTexture(CURSOR.region);
        tiledPlugin.getAPI().setCursor(CURSOR, new TextureRegion(cursorTexture));
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
        drawTile(x, y);
        return true;
    }

    @Override
    public void stageMouseUp(float x, float y) {
    	tiledPlugin.facade.sendNotification(TiledPlugin.AUTO_FILL_TILES);
    }

    @Override
    public void stageMouseDragged(float x, float y) {
        drawTile(x, y);
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
        initGridThings();
        if (entity == -1)
            drawTile(x, y);
        else
            drawOnEntity(entity);
        return true;
    }

    @Override
    public void itemMouseUp(int entity, float x, float y) {
    	tiledPlugin.facade.sendNotification(TiledPlugin.AUTO_FILL_TILES);
    }

    @Override
    public void itemMouseDragged(int entity, float x, float y) {
        drawTile(x, y);
    }

    @Override
    public void itemMouseDoubleClick(int entity, float x, float y) {
        if (!tiledPlugin.isOnCurrentSelectedLayer(entity)) return;
        if (entity != -1 && tiledPlugin.isTile(entity)) {
            //rotate
            TransformCommandBuilder commandBuilder = new TransformCommandBuilder();
            commandBuilder.begin(entity, tiledPlugin.getAPI().getEngine());
            TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class, tiledPlugin.getAPI().getEngine());
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

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void handleNotification(INotification notification) {

    }

    @Override
    public void keyDown(int entity, int keycode) {
        if(keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
            tiledPlugin.getAPI().toolHotSwap(tiledPlugin.deleteTileTool);
            tiledPlugin.deleteTileTool.setHotSwapped();
        }
    }

    @Override
    public void keyUp(int entity, int keycode) {

    }

    private void initGridThings() {
        gridWidth = tiledPlugin.dataToSave.getParameterVO().gridWidth;
        gridHeight = tiledPlugin.dataToSave.getParameterVO().gridHeight;
    }

    private void chooseDrawStrategy() {
    	if (tiledPlugin.isAutoGridTilesTabSelected()) {
    		currentDrawStrategy = autoTileDrawStrategy;
    	} else {
	        switch (tiledPlugin.getSelectedTileType()) {
	            case EntityFactory.IMAGE_TYPE:
	                currentDrawStrategy = imageDrawStrategy;
	                break;
	            case EntityFactory.SPRITE_TYPE:
	                currentDrawStrategy = spriteDrawStrategy;
	                break;
	            case SpineItemType.SPINE_TYPE:
	                currentDrawStrategy = spineDrawStrategy;
	                break;
	            default:
	                currentDrawStrategy = null;
	        }
    	}
    }

    private void drawTile(float x, float y) {
        if (tiledPlugin.getSelectedTileName().equals("")) return;

        float newX = MathUtils.floor(x / gridWidth) * gridWidth + tiledPlugin.getSelectedTileGridOffset().x;
        float newY = MathUtils.floor(y / gridHeight) * gridHeight + tiledPlugin.getSelectedTileGridOffset().y;
        int row = MathUtils.floor(newY / gridHeight);
        int column = MathUtils.round(newX / gridWidth);

        chooseDrawStrategy();
        currentDrawStrategy.drawTile(newX, newY, row, column);
    }

    private void drawOnEntity(int entity) {
        chooseDrawStrategy();
        currentDrawStrategy.updateTile(entity);
    }
}
