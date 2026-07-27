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
import games.rednblack.h2d.extension.bvb.BVBDataObject;
import games.rednblack.talos.runtime.ParticleEffectDescriptor;
import games.rednblack.talos.runtime.ParticleEffectInstancePool;
import games.rednblack.talos.runtime.bvb.BVB;
import games.rednblack.talos.runtime.utils.ShaderDescriptor;
import games.rednblack.talos.runtime.utils.VectorField;
import dev.lyze.gdxtinyvg.TinyVG;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.resources.FontSizePair;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.utils.*;
import games.rednblack.editor.utils.AsyncAtlasLoader;
import games.rednblack.editor.utils.FrameStepRunner;
import games.rednblack.editor.view.ui.dialog.LoadingBarDialog;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.spine.ResourceRetrieverAttachmentLoader;
import games.rednblack.h2d.extension.spine.SpineDrawableLogic;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.ResourceRetrieverAssetProvider;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGUtils;
import games.rednblack.puremvc.Proxy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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

    /** How the loading bar is split between the three phases; the atlases dominate the wait. */
    private static final float TEXTURES_LOAD_SHARE = 0.7f;
    private static final float READ_LOAD_SHARE = 0.2f;
    private static final float INSTALL_LOAD_SHARE = 0.1f;

    /** Mirrors the fallback size of {@link games.rednblack.editor.renderer.factory.component.LabelComponentFactory}. */
    private static final int LABEL_DEFAULT_SIZE = 12;

    public static final String PHASE_ATLASES = "Texture atlases";
    public static final String PHASE_READ = "Reading resources";
    public static final String PHASE_INSTALL = "Installing";
    /** The checklist of a project load, for whoever opens the loading dialog ahead of one. */
    public static final String[] LOAD_PHASES = {PHASE_ATLASES, PHASE_READ, PHASE_INSTALL};

    private final HashMap<String, ParticleEffectPool> particleEffects = new HashMap<>(1);
    private final HashMap<String, ParticleEffectInstancePool> talosVFXs = new HashMap<>(1);
    private final HashMap<String, TextureAtlas> currentProjectAtlas = new HashMap<>(1);
    /** Atlases of the load being replaced, kept drawable until the new ones are in use. */
    private final HashMap<String, TextureAtlas> previousProjectAtlas = new HashMap<>(1);

    private final HashMap<String, BVBDataObject> spineAnimAtlases = new HashMap<>();
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

    private boolean loading;
    private Runnable pendingLoad;

    public ResourceManager() {
        super(NAME, null);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        TextureArrayPolygonSpriteBatch.getMaxTextureUnits();

        PixmapPacker packer = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.SkylineStrategy());
        packer.setTransparentColor(Color.WHITE);
        packer.getTransparentColor().a = 0;

        FreeTypeFontGenerator dejaVuSansGenerator = new FreeTypeFontGenerator(Gdx.files.internal("freetypefonts/DejaVuSans.ttf")) /*{
            @Override
            protected BitmapFont newBitmapFont(BitmapFont.BitmapFontData data, Array<TextureRegion> pageRegions, boolean integer) {
                return new ThreadSafeBitmapFont(data, pageRegions, integer);
            }
        }*/;
        FreeTypeFontGenerator monoGenerator = new FreeTypeFontGenerator(Gdx.files.internal("freetypefonts/FiraCode-Regular.ttf"))/*{
            @Override
            protected BitmapFont newBitmapFont(BitmapFont.BitmapFontData data, Array<TextureRegion> pageRegions, boolean integer) {
                return new ThreadSafeBitmapFont(data, pageRegions, integer);
            }
        }*/;

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.characters += "⌘⇧⌥\u25CF\u2022";
        parameter.kerning = true;
        parameter.renderCount = 1;
        parameter.packer = packer;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.gamma = 0.6f;
        parameter.hinting = FreeTypeFontGenerator.Hinting.None;

        parameter.size = (int) (16 / settingsManager.editorConfigVO.uiScaleDensity);
        BitmapFont defaultMono = monoGenerator.generateFont(parameter);
        defaultMono.setFixedWidthGlyphs(parameter.characters);
        defaultMono.setUseIntegerPositions(false);
        defaultMono.getData().setScale(settingsManager.editorConfigVO.uiScaleDensity);

        monoGenerator.dispose();

        parameter.size = (int) (11 / settingsManager.editorConfigVO.uiScaleDensity);
        BitmapFont small = dejaVuSansGenerator.generateFont(parameter);
        small.setUseIntegerPositions(false);
        small.getData().setScale(settingsManager.editorConfigVO.uiScaleDensity);

        parameter.size = (int) (13 / settingsManager.editorConfigVO.uiScaleDensity);
        BitmapFont defaultFont = dejaVuSansGenerator.generateFont(parameter);
        defaultFont.setUseIntegerPositions(false);
        defaultFont.getData().setScale(settingsManager.editorConfigVO.uiScaleDensity);

        parameter.size = (int) (16 / settingsManager.editorConfigVO.uiScaleDensity);
        BitmapFont big = dejaVuSansGenerator.generateFont(parameter);
        big.setUseIntegerPositions(false);
        big.getData().setScale(settingsManager.editorConfigVO.uiScaleDensity);

        dejaVuSansGenerator.dispose();

        /*TextureRegion dejavuRegion = new TextureRegion(new Texture(Gdx.files.internal("style/default-font-32.png")));
        ShadedDistanceFieldFont smallDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        smallDistanceField.setDistanceFieldSmoothing(6);
        smallDistanceField.getData().setScale(0.35f);
        ShadedDistanceFieldFont defaultDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        defaultDistanceField.setDistanceFieldSmoothing(6);
        defaultDistanceField.getData().setScale(0.4f);
        ShadedDistanceFieldFont bigDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        bigDistanceField.setDistanceFieldSmoothing(6);
        bigDistanceField.getData().setScale(0.5f);*/
        /* Create the ObjectMap and add the fonts to it */
        ObjectMap<String, Object> fontMap = new ObjectMap<>();
        fontMap.put("small-font", small);
        fontMap.put("default-font", defaultFont);
        fontMap.put("big-font", big);
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

    public String getPackFromRegionName(String regionName) {
        for (Entry<String, TextureAtlas> entry : currentProjectAtlas.entrySet()) {
            if (entry.getValue().findRegion(regionName) != null)
                return entry.getKey().equals("main") ? "pack" : entry.getKey();
        }
        return "pack";
    }

    @Override
    public ProjectInfoVO getProjectVO() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectInfoVO();
    }

    @Override
    public SceneVO getSceneVO(String name) {
        return getSceneVO(name, HyperJson.getJson());
    }

    /**
     * @param json the parser to read with — off the render thread this has to be an instance of its
     *             own, since the shared one caches type information as it reads.
     */
    private SceneVO getSceneVO(String name, Json json) {
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        // TODO: this should be cached
        FileHandle file = Gdx.files.internal(sceneDataManager.getCurrProjectScenePathByName(name));
        return json.fromJson(SceneVO.class, file.readString());
    }

    /**
     * Reloads every project resource for the given resolution.
     * <p>
     * The load runs in three phases: texture pages are decoded off the render thread, then every
     * resource that is pure parsing is read on a worker thread, and only the parts that need a GL
     * context — Talos VFX, shader compilation, font pages — run on the render thread, one step per
     * frame. So the editor keeps drawing and the loading dialog keeps moving throughout.
     * <p>
     * {@code onComplete} runs on the render thread once everything is in, and is where callers put
     * whatever used to follow this call — the scene reload, notifications, and so on.
     */
    public void loadCurrentProjectData(String projectPath, String curResolution, Runnable onComplete) {
        if (loading) {
            // Every load reloads everything, so only the most recent request is worth keeping.
            pendingLoad = () -> loadCurrentProjectData(projectPath, curResolution, onComplete);
            return;
        }
        loading = true;
        packResolutionName = curResolution;

        facade.sendNotification(MsgAPI.SHOW_LOADING_DIALOG);
        facade.sendNotification(LoadingBarDialog.SET_PHASES, LOAD_PHASES);
        facade.sendNotification(LoadingBarDialog.SET_PHASE, PHASE_ATLASES);
        facade.sendNotification(LoadingBarDialog.SET_PROGRESS, 0f);

        new AsyncAtlasLoader(projectPath + "/assets/" + curResolution + "/pack", new AsyncAtlasLoader.Listener() {
            @Override
            public void onProgress(float progress, int pages) {
                facade.sendNotification(LoadingBarDialog.SET_DETAIL, pages + (pages == 1 ? " page" : " pages"));
                facade.sendNotification(LoadingBarDialog.SET_PROGRESS, progress * TEXTURES_LOAD_SHARE);
            }

            @Override
            public void onFinished(Map<String, TextureAtlas> atlases) {
                installProjectAtlases(atlases);
                readProjectData(projectPath, onComplete);
            }

            @Override
            public void onFailed(Throwable error) {
                error.printStackTrace();
                facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "ERROR: Unable to load project textures!");
                // Keep going with the atlases already in memory: better a stale editor than a stuck one.
                readProjectData(projectPath, onComplete);
            }
        }).start();
    }

    /**
     * Swaps in the atlases that just finished loading. The previous ones stay on the GPU until the
     * load is over: the editor keeps drawing meanwhile, and everything on screen still holds regions
     * pointing at them.
     */
    private void installProjectAtlases(Map<String, TextureAtlas> atlases) {
        previousProjectAtlas.putAll(currentProjectAtlas);
        currentProjectAtlas.clear();
        for (Entry<String, TextureAtlas> entry : atlases.entrySet()) {
            String name = entry.getKey().equals("pack") ? "main" : entry.getKey();
            currentProjectAtlas.put(name, entry.getValue());
        }
    }

    /**
     * Second phase: everything that is only parsing runs on a worker thread, into collections of its
     * own. Nothing here touches the live resource maps, so the render thread keeps drawing the scene
     * with the resources it already has until the install phase swaps them in.
     */
    private void readProjectData(String projectPath, Runnable onComplete) {
        // Look up on the render thread what the readers need but must not reach for themselves.
        SpineDrawableLogic spineDrawableLogic = null;
        try {
            spineDrawableLogic = (SpineDrawableLogic) PluginUIBridge.get(facade).getSceneLoader()
                    .getExternalItemType(SpineItemType.SPINE_TYPE).getDrawable();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        SpineDrawableLogic spineDrawable = spineDrawableLogic;
        float fontScaleMul = resolutionManager.getCurrentMul();

        facade.sendNotification(LoadingBarDialog.SET_PHASE, PHASE_READ);

        LoadedProjectData data = new LoadedProjectData();
        Thread worker = new Thread(() -> {
            long startedAt = System.nanoTime();
            try {
                // Confined to this thread: the shared parser is in use by the render thread.
                Json json = HyperJson.newJson();

                readStep("particle effects", 0f, () ->
                        data.particleEffects = readParticles(projectPath + File.separator + ProjectManager.PARTICLE_DIR_PATH));
                readStep("Spine animations", 0.15f, () ->
                        data.spineAnimations = readSpineAnimations(projectPath + File.separator + ProjectManager.SPINE_DIR_PATH, spineDrawable, json));
                readStep("sprite animations", 0.3f, () ->
                        data.spriteAnimations = readSpriteAnimations(projectPath + File.separator + ProjectManager.SPRITE_DIR_PATH));
                readStep("bitmap fonts", 0.4f, () ->
                        data.bitmapFonts = readBitmapFonts(projectPath + File.separator + ProjectManager.BITMAP_FONTS_DIR_PATH));
                readStep("TinyVG assets", 0.5f, () ->
                        data.tinyVGs = readTinyVGs(projectPath + File.separator + ProjectManager.TINY_VG_DIR_PATH));
                readStep("fonts", 0.6f, () ->
                        data.fontData = readFonts(fontScaleMul, json));
                readStep("shaders", 0.85f, () ->
                        data.shaderSources = readShaderSources(projectPath + File.separator + ProjectManager.SHADER_DIR_PATH));
                readStep("resource references", 0.95f, () ->
                        data.regionNames = collectRegionNames());

                System.out.println("Read project resources in " + (System.nanoTime() - startedAt) / 1_000_000L + "ms");
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                // Whatever happened, hand back to the render thread: a load that never finishes would
                // leave the editor behind the loading dialog for good.
                Gdx.app.postRunnable(() -> installProjectData(projectPath, data, onComplete));
            }
        }, "ProjectDataLoader");
        worker.setDaemon(true);
        worker.start();
    }

    /** Announces a read step and runs it; a resource that fails to parse must not stall the load. */
    private void readStep(String detail, float progress, Runnable action) {
        Gdx.app.postRunnable(() -> {
            facade.sendNotification(LoadingBarDialog.SET_DETAIL, detail);
            facade.sendNotification(LoadingBarDialog.SET_PROGRESS, TEXTURES_LOAD_SHARE + READ_LOAD_SHARE * progress);
        });
        try {
            action.run();
        } catch (Throwable t) {
            // The step's resources stay as they were, which is quiet enough to be missed: say it.
            t.printStackTrace();
            Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.SHOW_NOTIFICATION,
                    "ERROR: failed to load " + detail));
        }
    }

    /**
     * Third phase: swap in what the worker read, and run the parts that need a GL context. Each step
     * is a frame of its own, but they are all short — the long work already happened off-thread.
     */
    private void installProjectData(String projectPath, LoadedProjectData data, Runnable onComplete) {
        facade.sendNotification(LoadingBarDialog.SET_PHASE, PHASE_INSTALL);

        new FrameStepRunner()
                .add("Talos VFX", () -> loadCurrentProjectTalosVFXs(projectPath + File.separator + ProjectManager.TALOS_VFX_DIR_PATH))
                .add("shaders", () -> installShaders(data.shaderSources))
                .add("fonts", () -> installFonts(data.fontData))
                .add("resource references", () -> {
                    swap(particleEffects, data.particleEffects);
                    swap(spineAnimAtlases, data.spineAnimations);
                    swap(spriteAnimAtlases, data.spriteAnimations);
                    swap(bitmapFonts, data.bitmapFonts);
                    if (data.tinyVGs != null) {
                        swap(tinyVGs, data.tinyVGs.working);
                        swap(originalTinyVGs, data.tinyVGs.original);
                    }
                    removeInvalidResourceReferences(data.regionNames);
                })
                .run((name, progress) -> {
                    facade.sendNotification(LoadingBarDialog.SET_DETAIL, name);
                    facade.sendNotification(LoadingBarDialog.SET_PROGRESS, TEXTURES_LOAD_SHARE + READ_LOAD_SHARE + INSTALL_LOAD_SHARE * progress);
                }, () -> finishProjectDataLoad(onComplete));
    }

    /** A step that failed to read leaves its map alone rather than emptying it. */
    private static <K, V> void swap(HashMap<K, V> target, HashMap<K, V> loaded) {
        if (loaded == null) return;
        target.clear();
        target.putAll(loaded);
    }

    private void finishProjectDataLoad(Runnable onComplete) {
        // Before the callback, so the checklist is complete for the frames the dialog spends fading.
        facade.sendNotification(LoadingBarDialog.SET_COMPLETE);
        try {
            if (onComplete != null) onComplete.run();
        } finally {
            // The callback has rebuilt the scene against the new atlases, so nothing points here anymore.
            for (TextureAtlas atlas : previousProjectAtlas.values())
                atlas.dispose();
            previousProjectAtlas.clear();

            facade.sendNotification(MsgAPI.HIDE_LOADING_DIALOG);
            loading = false;

            Runnable pending = pendingLoad;
            pendingLoad = null;
            if (pending != null) pending.run();
        }
    }

    private HashMap<String, BitmapFont> readBitmapFonts(String path) {
        HashMap<String, BitmapFont> loaded = new HashMap<>();
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
            // Pages come from the atlases, so nothing here creates a texture.
            BitmapFont bitmapFont = new BitmapFont(bitmapFontData, pages, false);
            loaded.put(bitmapFont.getData().name, bitmapFont);
        }
        return loaded;
    }

    private TinyVGSet readTinyVGs(String path) {
        TinyVGSet loaded = new TinyVGSet();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store")) continue;

            loaded.working.put(entry.nameWithoutExtension(), TinyVGUtils.load(entry));
            loaded.original.put(entry.nameWithoutExtension(), TinyVGUtils.load(entry));
        }
        return loaded;
    }

    private HashMap<String, ParticleEffectPool> readParticles(String path) {
        HashMap<String, ParticleEffectPool> loaded = new HashMap<>();
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
            loaded.put(filename, effectPool);
        }
        return loaded;
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
            talosVFXs.put(filename, new ParticleEffectInstancePool(effectDescriptor, 1, games.rednblack.editor.renderer.resources.ResourceManager.PARTICLE_POOL_SIZE));
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

    private HashMap<String, BVBDataObject> readSpineAnimations(String path, SpineDrawableLogic spineDrawableLogic, Json json) {
        HashMap<String, BVBDataObject> loaded = new HashMap<>();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());
                FileHandle animJsonFile = Gdx.files.internal(entry.file().getAbsolutePath() + File.separator + animName + ".json");

                BVBDataObject spineDataObject = new BVBDataObject();
                spineDataObject.skeletonJson = new SkeletonJson(new ResourceRetrieverAttachmentLoader(animName, this, spineDrawableLogic));
                spineDataObject.skeletonData = spineDataObject.skeletonJson.readSkeletonData(animJsonFile);

                FileHandle bvb = Gdx.files.internal(entry.file().getAbsolutePath() + File.separator + animName + "-bvb.json");
                if (bvb.exists())
                    spineDataObject.bvbData = json.fromJson(BVB.class, bvb);

                loaded.put(animName, spineDataObject);
            }
        }

        return loaded;
    }

    private HashMap<String, Array<TextureAtlas.AtlasRegion>> readSpriteAnimations(String path) {
        HashMap<String, Array<TextureAtlas.AtlasRegion>> loaded = new HashMap<>();
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
                    loaded.put(animName, regions);
            }
        }
        return loaded;
    }

    public ArrayList<FontSizePair> getProjectRequiredFontsList() {
        return getProjectRequiredFontsList(HyperJson.getJson());
    }

    private ArrayList<FontSizePair> getProjectRequiredFontsList(Json json) {
        ObjectSet<FontSizePair> fontsToLoad = new ObjectSet<>();

        // Library items are shared by every scene, so they are collected once and not per scene.
        for (CompositeItemVO library : getProjectVO().libraryItems.values())
            fontsToLoad.addAll(library.getRecursiveFontList());

        for (int i = 0; i < getProjectVO().scenes.size(); i++) {
            SceneVO scene = getSceneVO(getProjectVO().scenes.get(i).sceneName, json);
            CompositeItemVO composite = scene.composite;
            if (composite == null) {
                continue;
            }
            fontsToLoad.addAll(composite.getRecursiveFontList());
        }

        ArrayList<FontSizePair> result = new ArrayList<>();
        for (FontSizePair fontSizePair : fontsToLoad)
            result.add(fontSizePair);
        return result;
    }

    /**
     * Rasterizes every font the project needs. Only the glyph packing happens here — turning the
     * packer's pages into textures needs a GL context and is left to {@link #installFonts}.
     */
    private HashMap<FontSizePair, PreparedFont> readFonts(float scaleMul, Json json) {
        HashMap<FontSizePair, PreparedFont> loaded = new HashMap<>();

        ArrayList<FontSizePair> requiredFonts = getProjectRequiredFontsList(json);
        for (int i = 0; i < requiredFonts.size(); i++) {
            FontSizePair pair = requiredFonts.get(i);
            FileHandle fontFile;
            try {
                fontFile = getTTFSafely(pair.fontName);
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = Math.round(pair.fontSize * scaleMul);
                parameter.packer = fontPacker;

                FreeTypeFontGenerator.FreeTypeBitmapFontData data = new FreeTypeFontGenerator.FreeTypeBitmapFontData();
                data.regions = new Array<>();
                generator.generateData(parameter, data);
                loaded.put(pair, new PreparedFont(data, parameter));

                generator.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return loaded;
    }

    private void installFonts(HashMap<FontSizePair, PreparedFont> loaded) {
        if (loaded == null) return;

        fonts.clear();
        for (Entry<FontSizePair, PreparedFont> entry : loaded.entrySet()) {
            PreparedFont prepared = entry.getValue();
            fontPacker.updateTextureRegions(prepared.data.regions, prepared.parameter.minFilter,
                    prepared.parameter.magFilter, prepared.parameter.genMipMaps);
            if (prepared.data.regions.isEmpty()) {
                System.err.println("No texture regions generated for font: " + entry.getKey().fontName);
                continue;
            }

            BitmapFont font = new BitmapFont(prepared.data, prepared.data.regions, true);
            font.setOwnsTexture(false);
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            font.setUseIntegerPositions(false);
            fonts.put(entry.getKey(), font);
        }
    }

    /** Shader sources by name; compiling them is a GL job, see {@link #installShaders}. */
    private HashMap<String, String[]> readShaderSources(String path) {
        HashMap<String, String[]> loaded = new HashMap<>();
        path += File.separator;
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName().replace(".vert", "").replace(".frag", "");
            if (file.isDirectory() || filename.endsWith(".DS_Store") || loaded.containsKey(filename)) continue;
            // check if pair exists.
            FileHandle vertex = Gdx.files.internal(path + filename + ".vert");
            FileHandle fragment = Gdx.files.internal(path + filename + ".frag");
            if (vertex.exists() && fragment.exists())
                loaded.put(filename, new String[]{vertex.readString(), fragment.readString()});
        }
        return loaded;
    }

    private void installShaders(HashMap<String, String[]> loaded) {
        if (loaded == null) return;

        for (ShaderProgram shaderProgram : shaderPrograms.values())
            shaderProgram.dispose();
        shaderPrograms.clear();

        for (Entry<String, String[]> entry : loaded.entrySet()) {
            ShaderProgram shaderProgram = ShaderCompiler.compileShader(entry.getValue()[0], entry.getValue()[1]);
            if (!shaderProgram.isCompiled()) {
                System.out.println("Error compiling shader: " + shaderProgram.getLog());
            }
            shaderPrograms.put(entry.getKey(), shaderProgram);
        }
    }

    public void reloadShader(String shaderName) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String shader = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName;

        if(Gdx.files.internal(shader + ".vert").exists() && Gdx.files.internal(shader + ".frag").exists()) {
            ShaderProgram shaderProgram = ShaderCompiler.compileShader(Gdx.files.internal(shader + ".vert"), Gdx.files.internal(shader + ".frag"));
            if (shaderProgram.isCompiled()) {
                shaderPrograms.remove(shaderName).dispose();
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
                throw new FileNotFoundException(fontName);
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
        embedFont(fontfamily, fontSize, mono);
    }

    /**
     * Rasterizes every font the given composite needs and that is not in memory yet.
     * <p>
     * Items restored from a VO — pasted, or brought back by an undo — may carry fonts the loaded
     * project never asked for: a label added to a scene that was never saved, or a copy coming from
     * another project. The label factories read the font straight out of the map and blow up on a
     * miss, so it has to be filled in before the entities are built. Unlike
     * {@link #prepareEmbeddingFont} this does not flush, or each font would evict the previous one.
     */
    public void prepareEmbeddingFonts(CompositeItemVO compositeVO) {
        for (MainItemVO item : compositeVO.getAllItems()) {
            if (!(item instanceof LabelVO)) continue;

            LabelVO label = (LabelVO) item;
            // Bitmap fonts come from the project's font folder, they are not rasterized here.
            if (label.bitmapFont != null || label.style == null || label.style.isEmpty()) continue;
            // Same fallback the label factories apply when they ask for the font.
            embedFont(label.style, label.size == 0 ? LABEL_DEFAULT_SIZE : label.size, label.monoSpace);
        }
    }

    private void embedFont(String fontfamily, int fontSize, boolean mono) {
        if (isFontLoaded(fontfamily, fontSize, mono)) {
            return;
        }

        FileHandle fontFile;
        try {
            fontFile = getTTFSafely(fontfamily);
        } catch (IOException e) {
            System.err.println("Unable to find font file for: " + fontfamily);
            return;
        }

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.packer = fontPacker;
        parameter.mono = mono;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        BitmapFont font = generator.generateFont(parameter);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
        if (mono)
            font.setFixedWidthGlyphs(FreeTypeFontGenerator.DEFAULT_CHARS);
        fonts.put(new FontSizePair(fontfamily, parameter.size, mono), font);
        generator.dispose();
    }

    public HashMap<String, BVBDataObject> getProjectSpineAnimationsList() {
        return spineAnimAtlases;
    }

    public HashMap<String, Array<TextureAtlas.AtlasRegion>> getProjectSpriteAnimationsList() {
        return spriteAnimAtlases;
    }

    public HashMap<String, ParticleEffectPool> getProjectParticleList() {
        return particleEffects;
    }

    public HashMap<String, ParticleEffectInstancePool> getProjectTalosList() {
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
        removeInvalidResourceReferences(collectRegionNames());
    }

    /**
     * Same sweep, against a set of region names collected up front. {@link #hasTextureRegion} walks
     * every atlas linearly, which on a project with thousands of regions turns this into a quadratic
     * scan; the set makes each check a lookup.
     */
    private void removeInvalidResourceReferences(HashSet<String> regionNames) {
        if (regionNames == null) return;

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        HashSet<String> invalidImages = new HashSet<>();

        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.imagesPacks.values()) {
            invalidImages.clear();
            for (String region : packVO.regions) {
                if (!regionNames.contains(region))
                    invalidImages.add(region);
            }
            if (invalidImages.size() > 0)
                packVO.regions.removeAll(invalidImages);
        }

        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.animationsPacks.values()) {
            invalidImages.clear();
            for (String region : packVO.regions) {
                if (!regionNames.contains(region))
                    invalidImages.add(region);
            }
            if (invalidImages.size() > 0)
                packVO.regions.removeAll(invalidImages);
        }
    }

    private HashSet<String> collectRegionNames() {
        HashSet<String> names = new HashSet<>();
        for (TextureAtlas atlas : currentProjectAtlas.values()) {
            for (TextureAtlas.AtlasRegion region : atlas.getRegions())
                names.add(region.name);
        }
        return names;
    }

    /** What the background phase reads, waiting to be installed on the render thread. */
    private static class LoadedProjectData {
        HashMap<String, ParticleEffectPool> particleEffects;
        HashMap<String, BVBDataObject> spineAnimations;
        HashMap<String, Array<TextureAtlas.AtlasRegion>> spriteAnimations;
        HashMap<String, BitmapFont> bitmapFonts;
        TinyVGSet tinyVGs;
        HashMap<FontSizePair, PreparedFont> fontData;
        HashMap<String, String[]> shaderSources;
        HashSet<String> regionNames;
    }

    /** TinyVGs are parsed twice on purpose: the editor edits one copy and compares against the other. */
    private static class TinyVGSet {
        final HashMap<String, TinyVG> working = new HashMap<>();
        final HashMap<String, TinyVG> original = new HashMap<>();
    }

    private static class PreparedFont {
        final FreeTypeFontGenerator.FreeTypeBitmapFontData data;
        final FreeTypeFontGenerator.FreeTypeFontParameter parameter;

        PreparedFont(FreeTypeFontGenerator.FreeTypeBitmapFontData data, FreeTypeFontGenerator.FreeTypeFontParameter parameter) {
            this.data = data;
            this.parameter = parameter;
        }
    }
}
