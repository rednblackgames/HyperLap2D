package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NinePatchComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.data.TenPatchVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.tenpatch.TenPatchDrawable;
import games.rednblack.editor.renderer.tenpatch.TenPatchUtils;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by azakhary on 8/18/2015.
 */
public class MainPanelMediator extends Mediator<MainPanel> {
    private static final String TAG = MainPanelMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private NinePatchPlugin plugin;

    private ImageUtils imageUtils = new ImageUtils();

    public MainPanelMediator(NinePatchPlugin plugin) {
        super(NAME, new MainPanel(plugin.facade));
        this.plugin = plugin;
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(NinePatchPlugin.EDIT_NINE_PATCH,
                NinePatchPlugin.CONVERT_TO_NINE_PATCH,
                MainPanel.SAVE_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case NinePatchPlugin.EDIT_NINE_PATCH:
                loadNinePatch();
                break;
            case NinePatchPlugin.CONVERT_TO_NINE_PATCH:
                convertImageToNinePatch();
                break;
            case MainPanel.SAVE_CLICKED:
                int entity = plugin.currEditingEntity;
                NinePatchComponent ninePatchComponent = ComponentRetriever.get(entity, NinePatchComponent.class, plugin.getAPI().getEngine());
                TextureAtlas.AtlasRegion region = plugin.getAPI().getProjectTextureRegion(ninePatchComponent.textureRegionName);
                TenPatchVO vo = toOriginalResolution(viewComponent.getTenPatchVO(), region);
                applyNewTenPatch(ninePatchComponent.textureRegionName, vo);
                viewComponent.hide();
                break;
        }
    }

    private void convertImageToNinePatch() {
        int entity = plugin.currEditingEntity;
        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, plugin.getAPI().getEngine());
        mainItemComponent.entityType = EntityFactory.NINE_PATCH;
        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class, plugin.getAPI().getEngine());
        String regionName = textureRegionComponent.regionName;
        NinePatchComponent ninePatchComponent = plugin.getAPI().getEngine().edit(entity).create(NinePatchComponent.class);
        ninePatchComponent.textureRegionName = regionName;
        TextureAtlas.AtlasRegion newRegion = (TextureAtlas.AtlasRegion) textureRegionComponent.region;
        int[] splits = {0, 0, 0, 0};
        int[] pad = {0, 0, 0, 0};
        newRegion.names = new String[] {"split", "pad"};
        newRegion.values = new int[][] {splits, pad};
        TenPatchVO vo = TenPatchUtils.fromSplits(splits, newRegion.originalWidth, newRegion.originalHeight);
        ninePatchComponent.tenPatch = new TenPatchDrawable(vo.horizontalStretchAreas, vo.verticalStretchAreas, false, newRegion);

        //remove original image
        File originalImg = new File(plugin.getAPI().getProjectPath() + "/assets/orig/images/"+regionName+".png");
        originalImg.delete();

        //save project
        plugin.getAPI().saveProject();

        //save split data
        addSplitsToImageInAtlas(regionName, splits);
        applyNewTenPatch(regionName, toOriginalResolution(vo, newRegion));
    }

    private void loadNinePatch() {
        int entity = plugin.currEditingEntity;
        NinePatchComponent ninePatchComponent = ComponentRetriever.get(entity, NinePatchComponent.class, plugin.getAPI().getEngine());
        loadRegion(ninePatchComponent.textureRegionName);
        viewComponent.show(plugin.getAPI().getUIStage());
    }

    /**
     * Ratio between the pixels of the resolution loaded in the editor and the original resolution. Stretch
     * areas are stored in original resolution pixels while the plugin edits the loaded region.
     */
    private float loadedResolutionRatio() {
        ProjectInfoVO projectInfo = plugin.getAPI().getCurrentProjectInfoVO();
        String resolutionName = plugin.getAPI().getCurrentProjectVO().lastOpenResolution;
        if (resolutionName == null || resolutionName.isEmpty() || resolutionName.equals("orig")) return 1f;
        ResolutionEntryVO resolution = projectInfo.getResolution(resolutionName);
        if (resolution == null) return 1f;
        float multiplier = resolution.getMultiplier(projectInfo.originalResolution);
        return multiplier == 0 ? 1f : 1f / multiplier;
    }

    /** Configuration of a region in pixels of the loaded resolution. */
    private TenPatchVO toLoadedResolution(TenPatchVO vo, TextureAtlas.AtlasRegion region) {
        return TenPatchUtils.scale(vo, loadedResolutionRatio(), region.originalWidth, region.originalHeight);
    }

    /** Configuration of a region in pixels of the original resolution. */
    private TenPatchVO toOriginalResolution(TenPatchVO vo, TextureAtlas.AtlasRegion region) {
        float ratio = loadedResolutionRatio();
        int width = Math.round(region.originalWidth / ratio);
        int height = Math.round(region.originalHeight / ratio);
        return TenPatchUtils.scale(vo, 1f / ratio, width, height);
    }

    private void addSplitsToImageInAtlas(String textureRegionName, int[] splits) {
        String atlasName = plugin.getAPI().getPackNameFromRegion(textureRegionName) + ".atlas";
        FileHandle packAtlas = Gdx.files.internal(plugin.getAPI().getProjectPath() + "/assets/orig/pack/" + atlasName);
        String content = packAtlas.readString();
        if (plugin.getAPI().getCurrentProjectVO().texturePackerVO.legacy) {
            int regionIndex = content.indexOf(textureRegionName);
            int splitEnd = content.indexOf("orig: ", regionIndex);
            String splitStr = "split: "+splits[0]+", "+splits[1]+", "+splits[2]+", "+splits[3]+"\n  ";
            String newContent = content.substring(0, splitEnd) + splitStr + content.substring(splitEnd);
            File test = new File(plugin.getAPI().getProjectPath() + "/assets/orig/pack/" + atlasName);
            writeFile(newContent, test);
        } else {
            int regionIndex = content.indexOf(textureRegionName);
            String splitStr = "split: " + splits[0] + ", " + splits[1] + ", " + splits[2] + ", " + splits[3] + "\n\t";
            int splitEnd = content.indexOf("bounds: ", regionIndex);
            String newContent = content.substring(0, splitEnd) + splitStr + content.substring(splitEnd);
            File test = new File(plugin.getAPI().getProjectPath() + "/assets/orig/pack/" + atlasName);
            writeFile(newContent, test);
        }
    }

    /**
     * Saves a configuration expressed in original resolution pixels: rewrites the {@code .9.png} of the
     * original resolution, updates the atlas {@code split} entry with the outer stretch areas, stores the
     * configuration in the project and reloads it.
     */
    private void applyNewTenPatch(String textureRegionName, TenPatchVO vo) {
        String atlasName = plugin.getAPI().getPackNameFromRegion(textureRegionName) + ".atlas";
        // first need to modify original image
        FileHandle packAtlas = Gdx.files.internal(plugin.getAPI().getProjectPath() + "/assets/orig/pack/" + atlasName);
        FileHandle imagesDir = Gdx.files.internal(plugin.getAPI().getProjectPath() + "/assets/orig/pack/");
        TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(packAtlas, imagesDir, false);
        BufferedImage finalImage = imageUtils.extractImage(atlas, textureRegionName, vo);
        if (finalImage != null) {
            imageUtils.saveImage(finalImage, plugin.getAPI().getProjectPath() + "/assets/orig/images/" + textureRegionName + ".9.png");
        }

        // now need to modify the pack
        int[] splits = TenPatchUtils.toSplits(vo, finalImage == null ? 0 : finalImage.getWidth() - 2, finalImage == null ? 0 : finalImage.getHeight() - 2);
        String content = packAtlas.readString();
        int regionIndex = content.indexOf(textureRegionName);
        int splitStart = content.indexOf("split: ", regionIndex) + "split: ".length();

        if (plugin.getAPI().getCurrentProjectVO().texturePackerVO.legacy) {
            int splitEnd = content.indexOf("orig: ", splitStart);
            String splitStr = splits[0]+", "+splits[1]+", "+splits[2]+", "+splits[3]+"\n  ";
            String newContent = content.substring(0, splitStart) + splitStr + content.substring(splitEnd);
            File test = new File(plugin.getAPI().getProjectPath() + "/assets/orig/pack/" + atlasName);
            writeFile(newContent, test);
        } else {
            String splitStr = splits[0] + ", " + splits[1] + ", " + splits[2] + ", " + splits[3];
            int splitEnd = content.indexOf("\n", splitStart);
            String newContent = content.substring(0, splitStart) + splitStr;
            if (splitEnd != -1)
                newContent = newContent + content.substring(splitEnd);
            File test = new File(plugin.getAPI().getProjectPath() + "/assets/orig/pack/" + atlasName);
            writeFile(newContent, test);
        }

        // store the ten patch configuration in the project, it has to be on disk before reloading
        ProjectInfoVO projectInfo = plugin.getAPI().getCurrentProjectInfoVO();
        if (projectInfo.tenPatches == null) projectInfo.tenPatches = new HashMap<>();
        projectInfo.tenPatches.put(textureRegionName, new TenPatchVO(vo));
        plugin.getAPI().saveProject();

        // reload
        plugin.getAPI().reLoadProject();
    }

    private void writeFile(String content, File file) {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(content);
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadRegion(String name) {
        TextureAtlas.AtlasRegion region = plugin.getAPI().getProjectTextureRegion(name);
        validateNinePatchTextureRegion(region);
        TenPatchVO vo = TenPatchUtils.getConfiguration(plugin.getAPI().getCurrentProjectInfoVO(), region);
        if (plugin.getAPI().getCurrentProjectInfoVO().tenPatches != null
                && plugin.getAPI().getCurrentProjectInfoVO().tenPatches.containsKey(name)) {
            vo = toLoadedResolution(vo, region);
        }
        viewComponent.setTexture(region, vo);
    }

    private void validateNinePatchTextureRegion(TextureAtlas.AtlasRegion texture) {
        int[] s = texture.findValue("split");
        if (s == null) {
            // Add splits to the atlasRegion if they are missing
            fixNinePatch(texture);
        }
    }

    private void fixNinePatch(TextureAtlas.AtlasRegion texture) {
        int[] splits = {0, 0, 0, 0};
        int[] pad = {0, 0, 0, 0};
        texture.names = new String[] {"split", "pad"};
        texture.values = new int[][] {splits, pad};

        //remove original image
        File originalImg = new File(plugin.getAPI().getProjectPath() + "/assets/orig/images/"+texture.name+".png");
        originalImg.delete();

        //save project
        plugin.getAPI().saveProject();

        //save split data
        addSplitsToImageInAtlas(texture.name, splits);
    }
}
