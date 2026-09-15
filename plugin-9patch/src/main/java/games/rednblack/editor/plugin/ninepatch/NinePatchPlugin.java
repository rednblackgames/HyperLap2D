package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * Created by azakhary on 8/18/2015.
 * Plugin to edit Nine Patch data on imported texture region: stretch areas (multiple per axis, TenPatch style),
 * tiling with tile offset and scroll speed, crush mode and corner gradient.
 */
@Implementation(authors = "azakhary", version = "0.1.0")
public class NinePatchPlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.ninepatch.NinePatchPlugin";

    public static final String EDIT_NINE_PATCH = CLASS_NAME + ".EDIT_NINE_PATCH";
    public static final String CONVERT_TO_NINE_PATCH = CLASS_NAME + ".CONVERT_TO_NINE_PATCH";

    /** Base name of the atlas packed from {@code assets/textures} and bundled in the plugin jar. */
    private static final String ATLAS_NAME = "ninepatch";

    private MainPanelMediator performancePanelMediator;
    private TextureAtlas pluginAtlas;

    public int currEditingEntity;

    public NinePatchPlugin() {
        super(CLASS_NAME);
    }

    @Override
    public void initPlugin() {
        performancePanelMediator = new MainPanelMediator(this);
        facade.registerMediator(performancePanelMediator);
        loadPluginAtlas();
        pluginAPI.setDropDownItemName(EDIT_NINE_PATCH, "Edit NinePatch", icon("icon-menu-ninepatch"));
        pluginAPI.setDropDownItemName(CONVERT_TO_NINE_PATCH, "Convert to NinePatch", icon("icon-menu-ninepatch-convert"));
    }

    /**
     * The plugin jar is loaded by its own class loader, so libGDX cannot open the atlas as a classpath file:
     * copy atlas + page to the editor cache dir first (same approach as the Tiled plugin).
     */
    private void loadPluginAtlas() {
        try {
            FileHandle atlasFile = extractResource(ATLAS_NAME + ".atlas");
            extractResource(ATLAS_NAME + ".png");
            pluginAtlas = new TextureAtlas(atlasFile);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            pluginAtlas = null;
        }
    }

    private FileHandle extractResource(String fileName) throws IOException {
        File target = new File(pluginAPI.getCacheDir(), fileName);
        try (InputStream in = getClass().getResourceAsStream("/" + fileName)) {
            if (in == null) throw new IOException("Missing plugin resource: " + fileName);
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        target.deleteOnExit();
        return new FileHandle(target);
    }

    /** @return drawable for a region of the plugin atlas, or null (text-only menu item) when unavailable */
    private Drawable icon(String region) {
        if (pluginAtlas == null) return null;
        TextureAtlas.AtlasRegion atlasRegion = pluginAtlas.findRegion(region);
        return atlasRegion == null ? null : new TextureRegionDrawable(atlasRegion);
    }

    @Override
    public void onDropDownOpen(Set<Integer> selectedEntities, Array<String> actionsSet) {
        if(selectedEntities.size() == 1) {
            int entity = selectedEntities.stream().findFirst().get();
            MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, getAPI().getEngine());

            if(mainItemComponent.entityType == EntityFactory.NINE_PATCH) {
                // it's our guy
                currEditingEntity = entity;
                actionsSet.add(EDIT_NINE_PATCH);
            }
            if(mainItemComponent.entityType == EntityFactory.IMAGE_TYPE
                    || mainItemComponent.entityType == EntityFactory.SPRITE_TYPE) {
                // images become still 9-patches, sprite animations become animated ones
                currEditingEntity = entity;
                actionsSet.add(CONVERT_TO_NINE_PATCH);
            }
        }
    }
}
