package games.rednblack.editor.plugin.tiled.view.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.plugins.PluginAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.io.File;

public class ImportTileSetDialogMediator extends Mediator<ImportTileSetDialog> {
    private static final String TAG = ImportTileSetDialogMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final PluginAPI pluginAPI;

    public ImportTileSetDialogMediator(PluginAPI pluginAPI, Facade facade) {
        super(NAME, new ImportTileSetDialog(facade));
        this.pluginAPI = pluginAPI;
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(TiledPlugin.IMPORT_TILESET_PANEL_OPEN,
                ImportTileSetDialog.IMPORT_TILESET);
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case TiledPlugin.IMPORT_TILESET_PANEL_OPEN:
                viewComponent.show(pluginAPI.getUIStage());
                break;
            case ImportTileSetDialog.IMPORT_TILESET:
                FileHandle tileset = notification.getBody();
                importTileset(tileset);
                break;
        }
    }

    private void importTileset(FileHandle tileset) {
        byte[] image = tileset.readBytes();
        Pixmap pixmap = new Pixmap(image, 0, image.length);
        String name = tileset.nameWithoutExtension();

        int tileW = viewComponent.getTileWidth();
        int tileH = viewComponent.getTileHeight();

        TexturePackVO texturePackVO = pluginAPI.getCurrentProjectInfoVO().imagesPacks.get(name + "-tile-set");
        if (texturePackVO == null) {
            texturePackVO = new TexturePackVO();
            texturePackVO.name = name + "-tile-set";

            pluginAPI.getCurrentProjectInfoVO().imagesPacks.put(texturePackVO.name, texturePackVO);
        }

        int i = 0;
        for (int x = 0; x < pixmap.getWidth(); x += tileW) {
            for (int y = 0; y < pixmap.getHeight(); y += tileH) {
                int w = x + tileW <= pixmap.getWidth() ? tileW : pixmap.getWidth() - x;
                int h = y + tileH <= pixmap.getHeight() ? tileH : pixmap.getHeight() - y;
                if( getViewComponent().getBlankTileOption()){
                    if(!isBlankTile(pixmap, x, y, w, h)) { //check if the tile is blank (empty, if not it can be added)
                        createTilePixmap(pixmap,x,y,w,h,i,name,texturePackVO);
                        i++;
                    }
                }else{
                        createTilePixmap(pixmap,x,y,w,h,i,name,texturePackVO);
                        i++;
                }
            }
        }

        pixmap.dispose();

        viewComponent.close();

        facade.sendNotification(MsgAPI.UPDATE_ATLAS_PACK_LIST);
        facade.sendNotification(MsgAPI.ACTION_REPACK);
    }

    private void createTilePixmap(Pixmap pixmap, int x, int y, int w, int h, int i, String name, TexturePackVO texturePackVO){
        Pixmap tilePixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        //insert control

        tilePixmap.drawPixmap(pixmap, 0, 0, x, y, w, h);

        int pix =  pixmap.getPixel(x,y);
        String imagesPath = getCurrentRawImagesPath() + File.separator + name + i + ".png";
        FileHandle path = new FileHandle(imagesPath);
        PixmapIO.writePNG(path, tilePixmap);

        tilePixmap.dispose();
        texturePackVO.regions.add(name + i);
    }

    public String getCurrentRawImagesPath() {
        return pluginAPI.getProjectPath() + File.separator + "assets" + File.separator + "orig" + File.separator + "images";
    }

    private boolean isBlankTile(Pixmap pixmap ,int x, int y, int w, int h){
        for(int cx = x; cx < x + w; cx += 1) {
            for(int cy = y; cy < y + h; cy += 1 ){
                if(pixmap.getPixel(cx, cy)!= 0) return false;
            }
        }
        return true;
    }
}
