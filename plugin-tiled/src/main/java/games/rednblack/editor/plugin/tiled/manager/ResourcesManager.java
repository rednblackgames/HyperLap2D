package games.rednblack.editor.plugin.tiled.manager;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.google.common.io.ByteStreams;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.view.SpineDrawable;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.extension.spine.SpineDataObject;
import games.rednblack.h2d.extension.spine.SpineItemType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mariam on 4/21/16.
 */
public class ResourcesManager {

    private final String RESOURCES_FILE_NAME = "tiled";

    private TiledPlugin tiledPlugin;
    private TextureAtlas textureAtlas;

    private final SkeletonRenderer skeletonRenderer = new SkeletonRenderer();

    private final ObjectMap<String, Texture> textureCache = new ObjectMap<>();
    private final ObjectMap<String, SpineDrawable> spineDrawableCache = new ObjectMap<>();

    public ResourcesManager(TiledPlugin tiledPlugin) {
        this.tiledPlugin = tiledPlugin;
        skeletonRenderer.setPremultipliedAlpha(true);

        init();
    }

    private void init() {
        FileHandle atlasTempFile = getResourceFileFromJar(RESOURCES_FILE_NAME + ".atlas");
        FileHandle pngTempFile = getResourceFileFromJar(RESOURCES_FILE_NAME + ".png");
        textureAtlas = new TextureAtlas(atlasTempFile);
        atlasTempFile.file().deleteOnExit();
        pngTempFile.file().deleteOnExit();

        loadTexture("tile-cursor");
        loadTexture("tile-eraser-cursor");
    }

    private void loadTexture(String name) {
        FileHandle file = getResourceFileFromJar(name + ".png");
        file.file().deleteOnExit();
        textureCache.put(name, new Texture(file));
    }

    private FileHandle getResourceFileFromJar(String fileName) {
        File tempFile = new File(tiledPlugin.getAPI().getCacheDir()+ File.separator + fileName);

        try {
            InputStream in = getClass().getResourceAsStream("/"+fileName);
            FileOutputStream out = new FileOutputStream(tempFile);
            ByteStreams.copy(in, out);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileHandle(tempFile);
    }

    public Texture getTexture(String name) {
        return textureCache.get(name);
    }

    public TextureRegion getTextureRegion(String name, int type) {
        TextureRegion region = textureAtlas.findRegion(name); // try to get region from plugin assets
        if (region == null) { // take the region from hyperlap assets
            switch (type) {
                case EntityFactory.IMAGE_TYPE:
                    region = tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(name);
                    break;
                case EntityFactory.SPRITE_TYPE:
                    region = tiledPlugin.getAPI().getSceneLoader().getRm().getSpriteAnimation(name).get(0);
                    break;
            }
        }
        return region;
    }

    public SpineDrawable getSpineDrawable(String name) {
        if (spineDrawableCache.get(name) == null) {
            SpineDataObject spineDataObject = (SpineDataObject) tiledPlugin.getAPI().getSceneLoader().getRm().getExternalItemType(SpineItemType.SPINE_TYPE, name);
            SkeletonData skeletonData = spineDataObject.skeletonData;
            Skeleton skeleton = new Skeleton(skeletonData);

            spineDrawableCache.put(name, new SpineDrawable(skeleton, skeletonRenderer));
        }

        return spineDrawableCache.get(name);
    }

    public NinePatch getPluginNinePatch(String name) {
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(name);
        if(region == null) return null;
        int[] splits = region.findValue("split");
        return new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
    }
}
