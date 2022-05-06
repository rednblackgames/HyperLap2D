package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.SkeletonJson;
import com.kotcrab.vis.ui.VisUI;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;
import com.talosvfx.talos.runtime.utils.VectorField;
import dev.lyze.gdxtinyvg.TinyVG;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.resources.FontSizePair;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.utils.*;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.extension.spine.ResourceRetrieverAttachmentLoader;
import games.rednblack.h2d.extension.spine.SpineDataObject;
import games.rednblack.h2d.extension.spine.SpineDrawableLogic;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.ResourceRetrieverAssetProvider;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.puremvc.java.patterns.proxy.Proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by azakhary on 4/26/2015.
 */
public class ResourceManager extends Proxy implements IResourceRetriever {

    public String packResolutionName = "orig";

    private static final String TAG = ResourceManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private final HashMap<String, ParticleEffectPool> particleEffects = new HashMap<>(1);
    private final HashMap<String, ParticleEffectDescriptor> talosVFXs = new HashMap<>(1);
    private HashMap<String, TextureAtlas> currentProjectAtlas = new HashMap<>(1);

    private final HashMap<String, SpineDataObject> spineAnimAtlases = new HashMap<>();
    private final HashMap<String, Array<TextureAtlas.AtlasRegion>> spriteAnimAtlases = new HashMap<>();
    private final HashMap<FontSizePair, BitmapFont> fonts = new HashMap<>();
    private final HashMap<String, BitmapFont> bitmapFonts = new HashMap<>();
    private final HashMap<String, ShaderProgram> shaderPrograms = new HashMap<>(1);
    private final HashMap<String, TinyVG> tinyVGs = new HashMap<>(1);
    private final HashMap<String, TinyVG> originalTinyVGs = new HashMap<>(1);

    private TextureAtlas.AtlasRegion defaultRegion;

    private ResolutionManager resolutionManager;
    private SettingsManager settingsManager;
    private PixmapPacker fontPacker;

    public ResourceManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        TextureArrayPolygonSpriteBatch.getMaxTextureUnits();

        WhitePixel.initializeShared();

        PixmapPacker packer = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.SkylineStrategy());
        packer.setTransparentColor(Color.WHITE);
        packer.getTransparentColor().a = 0;

        FreeTypeFontGenerator monoGenerator = new FreeTypeFontGenerator(Gdx.files.internal("freetypefonts/FiraCode-Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.characters += "⌘⇧⌥\u25CF\u2022";
        parameter.kerning = true;
        parameter.renderCount = 3;
        parameter.packer = packer;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        BitmapFont defaultMono = monoGenerator.generateFont(parameter);
        defaultMono.setFixedWidthGlyphs(parameter.characters);

        monoGenerator.dispose();

        TextureRegion dejavuRegion = new TextureRegion(new Texture(Gdx.files.internal("style/default-font-32.png")));
        ShadedDistanceFieldFont smallDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        smallDistanceField.setDistanceFieldSmoothing(6);
        smallDistanceField.getData().setScale(0.35f);
        ShadedDistanceFieldFont defaultDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        defaultDistanceField.setDistanceFieldSmoothing(6);
        defaultDistanceField.getData().setScale(0.4f);
        ShadedDistanceFieldFont bigDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        bigDistanceField.setDistanceFieldSmoothing(6);
        bigDistanceField.getData().setScale(0.5f);
        /* Create the ObjectMap and add the fonts to it */
        ObjectMap<String, Object> fontMap = new ObjectMap<>();
        fontMap.put("small-font", smallDistanceField);
        fontMap.put("default-font", defaultDistanceField);
        fontMap.put("big-font", bigDistanceField);
        fontMap.put("default-mono-font", defaultMono);

        SkinLoader.SkinParameter skinParameter = new SkinLoader.SkinParameter(fontMap);

        AssetManager assetManager = new AssetManager();
        assetManager.setLoader(Skin.class, new H2DSkinLoader(assetManager.getFileHandleResolver()));
        assetManager.load("style/uiskin.json", Skin.class, skinParameter);

        assetManager.finishLoading();
        Skin skin = assetManager.get("style/uiskin.json");

        VisUI.load(skin);
        VisUI.setDefaultTitleAlign(Align.center);

        defaultRegion = VisUI.getSkin().getAtlas().findRegion("missing-image");

        fontPacker = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.SkylineStrategy());
        fontPacker.setTransparentColor(Color.WHITE);
        fontPacker.getTransparentColor().a = 0;
    }

    @Override
    public TextureRegion getTextureRegion(String name) {
        for (TextureAtlas atlas : currentProjectAtlas.values()) {
            TextureRegion region = atlas.findRegion(name);
            if (region != null)
                return region;
        }
        return defaultRegion;
    }

    @Override
    public TextureAtlas getTextureAtlas(String atlasName) {
        return currentProjectAtlas.get(atlasName);
    }

    @Override
    public ParticleEffect getParticleEffect(String name) {
        return particleEffects.get(name).obtain();
    }

    /**
     * Sets working resolution, please set before doing any loading
     * @param resolution String resolution name, default is "orig" later use resolution names created in editor
     */
    public void setWorkingResolution(String resolution) {
        ResolutionEntryVO resolutionObject = getProjectVO().getResolution("resolutionName");
        if(resolutionObject != null) {
            packResolutionName = resolution;
        }
    }

    @Override
    public Object getExternalItemType(int itemType, String name) {
        switch (itemType) {
            case SpineItemType.SPINE_TYPE:
                return spineAnimAtlases.get(name);
            case TalosItemType.TALOS_TYPE:
                return talosVFXs.get(name);
            case TinyVGItemType.TINYVG_TYPE:
                return tinyVGs.get(name);
            default:
                return null;
        }
    }

    public TinyVG getOriginalTinyVG(String name) {
        return originalTinyVGs.get(name);
    }

    @Override
    public Array<TextureAtlas.AtlasRegion> getSpriteAnimation(String animationName) {
        return spriteAnimAtlases.get(animationName);
    }

    @Override
    public BitmapFont getFont(String fontName, int fontSize, boolean mono) {
        FontSizePair pair = new FontSizePair(fontName, fontSize, mono);
        return fonts.get(pair);
    }

    @Override
    public BitmapFont getBitmapFont(String fontName) {
        return bitmapFonts.get(fontName);
    }

    @Override
    public boolean hasTextureRegion(String regionName) {
        for (TextureAtlas atlas : currentProjectAtlas.values()) {
            if (atlas.findRegion(regionName) != null)
                return true;
        }
        return false;
    }

    @Override
    public ProjectInfoVO getProjectVO() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectInfoVO();
    }

    @Override
    public SceneVO getSceneVO(String name) {
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        // TODO: this should be cached
        FileHandle file = Gdx.files.internal(sceneDataManager.getCurrProjectScenePathByName(name));
        Json json = HyperJson.getJson();
        return json.fromJson(SceneVO.class, file.readString());
    }

    public void loadCurrentProjectData(String projectPath, String curResolution) {
        packResolutionName = curResolution;
        loadCurrentProjectAssets(projectPath + "/assets/" + curResolution + "/pack");
        loadCurrentProjectParticles(projectPath + File.separator + ProjectManager.PARTICLE_DIR_PATH);
        loadCurrentProjectTalosVFXs(projectPath + File.separator + ProjectManager.TALOS_VFX_DIR_PATH);
        loadCurrentProjectSpineAnimations(projectPath + File.separator + ProjectManager.SPINE_DIR_PATH);
        loadCurrentProjectSpriteAnimations(projectPath + File.separator + ProjectManager.SPRITE_DIR_PATH);
        loadCurrentProjectBitmapFonts(projectPath + File.separator + ProjectManager.BITMAP_FONTS_DIR_PATH);
        loadCurrentProjectTinyVGs(projectPath + File.separator + ProjectManager.TINY_VG_DIR_PATH);
        loadCurrentProjectFonts();
        loadCurrentProjectShaders(projectPath + File.separator + ProjectManager.SHADER_DIR_PATH);

        removeInvalidResourceReferences();
    }

    public void loadCurrentProjectBitmapFonts(String path) {
        bitmapFonts.clear();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store")) continue;

            Array<TextureRegion> pages = new Array<>();
            BitmapFont.BitmapFontData bitmapFontData = new BitmapFont.BitmapFontData(Gdx.files.internal(file.getAbsolutePath()), false);
            for (String page : bitmapFontData.imagePaths) {
                pages.add(getTextureRegion(FilenameUtils.getBaseName(page)));
            }
            BitmapFont bitmapFont = new BitmapFont(bitmapFontData, pages, false);
            bitmapFonts.put(bitmapFont.getData().name, bitmapFont);
        }
    }

    public void loadCurrentProjectTinyVGs(String path) {
        tinyVGs.clear();
        originalTinyVGs.clear();

        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store")) continue;

            tinyVGs.put(entry.nameWithoutExtension(), TinyVGUtils.load(entry));
            originalTinyVGs.put(entry.nameWithoutExtension(), TinyVGUtils.load(entry));
        }
    }

    private void loadCurrentProjectParticles(String path) {
        particleEffects.clear();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store")) continue;

            ParticleEffect particleEffect = new ParticleEffect();
            particleEffect.loadEmitters(Gdx.files.internal(file.getAbsolutePath()));
            for (TextureAtlas atlas : currentProjectAtlas.values()) {
                try {
                    particleEffect.loadEmitterImages(atlas, "");
                    break;
                } catch (Exception ignore) { }
            }
            ParticleEffectPool effectPool = new ParticleEffectPool(particleEffect, 1, games.rednblack.editor.renderer.resources.ResourceManager.PARTICLE_POOL_SIZE);
            particleEffects.put(filename, effectPool);
        }
    }

    private void loadCurrentProjectTalosVFXs(String path) {
        talosVFXs.clear();
        talosResPath = path;
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store") || !filename.endsWith("p")) continue;

            ResourceRetrieverAssetProvider assetProvider = new ResourceRetrieverAssetProvider(this);
            assetProvider.setAssetHandler(ShaderDescriptor.class, this::findShaderDescriptorOnLoad);
            assetProvider.setAssetHandler(VectorField.class, this::findVectorFieldDescriptorOnLoad);
            ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor();
            effectDescriptor.setAssetProvider(assetProvider);
            effectDescriptor.load(Gdx.files.internal(file.getAbsolutePath()));
            talosVFXs.put(filename, effectDescriptor);
        }
    }

    private ObjectMap<String, ShaderDescriptor> shaderDescriptorObjectMap = new ObjectMap<>();
    private String talosResPath;
    private ShaderDescriptor findShaderDescriptorOnLoad (String assetName) {
        ShaderDescriptor asset = shaderDescriptorObjectMap.get(assetName);
        if (asset == null) {
            //Look in all paths, and hopefully load the requested asset, or fail (crash)
            final FileHandle file = new FileHandle(talosResPath + File.separator + assetName);

            asset = new ShaderDescriptor();
            if (file.exists()) {
                asset.setData(file.readString());
            }
        }
        return asset;
    }

    private ObjectMap<String, VectorField> vectorFieldDescriptorObjectMap = new ObjectMap<>();
    private VectorField findVectorFieldDescriptorOnLoad (String assetName) {
        VectorField asset = vectorFieldDescriptorObjectMap.get(assetName);
        if (asset == null) {
            final FileHandle file = new FileHandle(talosResPath + File.separator + assetName + ".fga");

            if (file.exists()) {
                asset = new VectorField(file);
            } else {
                asset = new VectorField();
            }
        }
        return asset;
    }

    private void loadCurrentProjectSpineAnimations(String path) {
        spineAnimAtlases.clear();
        FileHandle sourceDir = new FileHandle(path);
        SpineDrawableLogic spineDrawableLogic = (SpineDrawableLogic) Sandbox.getInstance().sceneControl.sceneLoader.getExternalItemType(SpineItemType.SPINE_TYPE).getDrawable();
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());
                FileHandle animJsonFile = Gdx.files.internal(entry.file().getAbsolutePath() + File.separator + animName + ".json");

                SpineDataObject spineDataObject = new SpineDataObject();
                spineDataObject.skeletonJson = new SkeletonJson(new ResourceRetrieverAttachmentLoader(animName, this, spineDrawableLogic));
                spineDataObject.skeletonData = spineDataObject.skeletonJson.readSkeletonData(animJsonFile);

                spineAnimAtlases.put(animName, spineDataObject);
            }
        }

    }

    private void loadCurrentProjectSpriteAnimations(String path) {
        spriteAnimAtlases.clear();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());
                Array<TextureAtlas.AtlasRegion> regions = null;
                for (TextureAtlas atlas : currentProjectAtlas.values()) {
                    regions = atlas.findRegions(animName);
                    if (regions.size > 0)
                        break;
                }
                if (regions != null)
                    spriteAnimAtlases.put(animName, regions);
            }
        }
    }

    public void loadCurrentProjectAssets(String packFolderPath) {
        FileHandle folder = new FileHandle(packFolderPath);
        for (FileHandle file : folder.list()) {
            if (file.extension().equals("atlas")) {
                String name = file.nameWithoutExtension().equals("pack") ? "main" : file.nameWithoutExtension();
                currentProjectAtlas.put(name, new TextureAtlas(file));
            }
        }
    }

    public ArrayList<FontSizePair> getProjectRequiredFontsList() {
        ObjectSet<FontSizePair> fontsToLoad = new ObjectSet<>();

        for (int i = 0; i < getProjectVO().scenes.size(); i++) {
            SceneVO scene = getSceneVO(getProjectVO().scenes.get(i).sceneName);
            CompositeItemVO composite = scene.composite;
            if (composite == null) {
                continue;
            }
            Array<FontSizePair> fonts = composite.getRecursiveFontList();
            for (CompositeItemVO library : getProjectVO().libraryItems.values()) {
                Array<FontSizePair>  libFonts = library.getRecursiveFontList();
                fontsToLoad.addAll(libFonts);
            }
            fontsToLoad.addAll(fonts);
        }

        ArrayList<FontSizePair> result = new ArrayList<>();
        for (FontSizePair fontSizePair : fontsToLoad)
            result.add(fontSizePair);
        return result;
    }

    public void loadCurrentProjectFonts() {
        fonts.clear();

        ArrayList<FontSizePair> requiredFonts = getProjectRequiredFontsList();
        for (int i = 0; i < requiredFonts.size(); i++) {
            FontSizePair pair = requiredFonts.get(i);
            FileHandle fontFile;
            try {
                fontFile = getTTFSafely(pair.fontName);
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = Math.round(pair.fontSize * resolutionManager.getCurrentMul());
                parameter.packer = fontPacker;
                BitmapFont font = generator.generateFont(parameter);
                font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                font.setUseIntegerPositions(false);
                fonts.put(pair, font);
                generator.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loadCurrentProjectShaders(String path) {
    	Iterator<Entry<String, ShaderProgram>> it = shaderPrograms.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, ShaderProgram> pair = it.next();
    		pair.getValue().dispose();
    		it.remove(); 
    	}
        shaderPrograms.clear();
        path += File.separator;
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName().replace(".vert", "").replace(".frag", "");
            if (file.isDirectory() || filename.endsWith(".DS_Store") || shaderPrograms.containsKey(filename)) continue;
            // check if pair exists.
            if(Gdx.files.internal(path + filename + ".vert").exists() && Gdx.files.internal(path + filename + ".frag").exists()) {
                ShaderProgram shaderProgram = ShaderCompiler.compileShader(Gdx.files.internal(path + filename + ".vert"), Gdx.files.internal(path + filename + ".frag"));
                if (!shaderProgram.isCompiled()) {
                    System.out.println("Error compiling shader: " + shaderProgram.getLog());
                }
                shaderPrograms.put(filename, shaderProgram);
            }
        }

    }

    public void reloadShader(String shaderName) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String shader = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName;

        if(Gdx.files.internal(shader + ".vert").exists() && Gdx.files.internal(shader + ".frag").exists()) {
            ShaderProgram shaderProgram = ShaderCompiler.compileShader(Gdx.files.internal(shader + ".vert"), Gdx.files.internal(shader + ".frag"));
            if (shaderProgram.isCompiled()) {
                shaderPrograms.remove(shaderName);
                shaderPrograms.put(shaderName, shaderProgram);
            } else {
                System.out.println("Error compiling shader: " + shaderProgram.getLog());
            }
        }
    }

    public FileHandle getTTFSafely(String fontName) throws IOException {
        FontManager fontManager = facade.retrieveProxy(FontManager.NAME);

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String expectedPath = projectManager.getFreeTypeFontPath() + File.separator + fontName + ".ttf";
        FileHandle expectedFile = Gdx.files.internal(expectedPath);
        if (!expectedFile.exists()) {
            // let's check if system fonts fot it
            HashMap<String, String> fonts = fontManager.getFontsMap();
            if (fonts.containsKey(fontName)) {
                File source = new File(fonts.get(fontName));
                FileUtils.copyFile(source, expectedFile.file());
                expectedFile = Gdx.files.internal(expectedPath);
            } else {
                throw new FileNotFoundException();
            }
        }

        return expectedFile;
    }

    public void flushAllUnusedFonts() {
        //List of fonts that are required to be in memory
        ArrayList<FontSizePair> requiredFonts = getProjectRequiredFontsList();
        ArrayList<FontSizePair> fontsInMemory = new ArrayList<>(fonts.keySet());

        for (FontSizePair font : fontsInMemory) {
            if (!requiredFonts.contains(font)) {
                fonts.remove(font);
            }
        }
    }

    public boolean isFontLoaded(String shortName, int fontSize, boolean mono) {
        return fonts.containsKey(new FontSizePair(shortName, fontSize, mono));
    }

    public void prepareEmbeddingFont(String fontfamily, int fontSize, boolean mono) {
        flushAllUnusedFonts();

        if (isFontLoaded(fontfamily, fontSize, mono)) {
            return;
        }

        FontManager fontManager = facade.retrieveProxy(FontManager.NAME);

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.packer = fontPacker;
        parameter.mono = mono;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontManager.getTTFByName(fontfamily));
        BitmapFont font = generator.generateFont(parameter);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
        if (mono)
            font.setFixedWidthGlyphs(FreeTypeFontGenerator.DEFAULT_CHARS);
        fonts.put(new FontSizePair(fontfamily, parameter.size, mono), font);
        generator.dispose();
    }

    public HashMap<String, SpineDataObject> getProjectSpineAnimationsList() {
        return spineAnimAtlases;
    }

    public HashMap<String, Array<TextureAtlas.AtlasRegion>> getProjectSpriteAnimationsList() {
        return spriteAnimAtlases;
    }

    public HashMap<String, ParticleEffectPool> getProjectParticleList() {
        return particleEffects;
    }

    public HashMap<String, ParticleEffectDescriptor> getProjectTalosList() {
        return talosVFXs;
    }

    public HashMap<String, BitmapFont> getBitmapFontList() {
        return bitmapFonts;
    }

    public HashMap<String, TinyVG> getTinyVGList() {
        return tinyVGs;
    }

    @Override
    public ResolutionEntryVO getLoadedResolution() {
        if(packResolutionName.equals("orig")) {
            return getProjectVO().originalResolution;
        }
        return getProjectVO().getResolution(packResolutionName);
    }

	@Override
	public ShaderProgram getShaderProgram(String shaderName) {
		return shaderPrograms.get(shaderName);
	}

    public void addShaderProgram(String name, ShaderProgram shaderProgram) {
        shaderPrograms.put(name, shaderProgram);
    }

    public void removeShaderProgram(String shaderName) {
        shaderPrograms.remove(shaderName);
    }

    public HashMap<String, ShaderProgram> getShaders() {
        return shaderPrograms;
    }

    public void removeInvalidResourceReferences() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        HashSet<String> invalidImages = new HashSet<>();

        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.imagesPacks.values()) {
            invalidImages.clear();
            for (String region : packVO.regions) {
                if (!hasTextureRegion(region))
                    invalidImages.add(region);
            }
            if (invalidImages.size() > 0)
                packVO.regions.removeAll(invalidImages);
        }

        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.animationsPacks.values()) {
            invalidImages.clear();
            for (String region : packVO.regions) {
                if (!hasTextureRegion(region))
                    invalidImages.add(region);
            }
            if (invalidImages.size() > 0)
                packVO.regions.removeAll(invalidImages);
        }
    }
}
