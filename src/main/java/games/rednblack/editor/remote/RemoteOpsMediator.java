package games.rednblack.editor.remote;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.SelectionPayload;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.renderer.utils.DefaultShaders;
import games.rednblack.editor.renderer.utils.ShaderCompiler;
import org.apache.commons.io.FileUtils;
import games.rednblack.editor.proxy.EntityMetadata;
import games.rednblack.editor.proxy.PluginUIBridge;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.editor.renderer.data.LightVO;
import games.rednblack.editor.renderer.data.LightsPropertiesVO;
import games.rednblack.editor.renderer.data.PolygonShapeVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.system.HyperLap2dRendererMiniMap;
import games.rednblack.editor.utils.runtime.AddableComponents;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.UIAbstractEntityPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIBasicItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UILabelItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UILightBodyPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIParticlePropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIPhysicsPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIScenePropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIShaderPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UICircleShapePropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UICompositeItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIImageItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UILayoutPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UILightItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UIPolygonComponentPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UISensorPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UISpineAnimationItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UISpriteAnimationItemPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.UITalosPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.remote.RemoteAssetsRequest;
import games.rednblack.h2d.common.remote.RemoteAssetsResult;
import games.rednblack.h2d.common.remote.RemoteAssetDimensionsRequest;
import games.rednblack.h2d.common.remote.RemoteAssetDimensionsResult;
import games.rednblack.h2d.common.remote.RemoteCreateEntityRequest;
import games.rednblack.h2d.common.remote.RemoteCreateEntityResult;
import games.rednblack.h2d.common.remote.RemoteCreateShaderRequest;
import games.rednblack.h2d.common.remote.RemoteCreateShaderResult;
import games.rednblack.h2d.common.remote.RemoteDeleteRequest;
import games.rednblack.h2d.common.remote.RemoteEditableComponentsRequest;
import games.rednblack.h2d.common.remote.RemoteEditableComponentsResult;
import games.rednblack.h2d.common.remote.RemoteEditRequest;
import games.rednblack.h2d.common.remote.RemoteEditResult;
import games.rednblack.h2d.common.remote.RemoteHandle;
import games.rednblack.h2d.common.remote.RemoteOpenSceneRequest;
import games.rednblack.h2d.common.remote.RemoteZIndexRequest;
import games.rednblack.h2d.common.remote.RemoteSceneSettingsRequest;
import games.rednblack.h2d.common.remote.RemoteSceneSettingsResult;
import games.rednblack.h2d.common.remote.RemoteScreenshotRequest;
import games.rednblack.h2d.common.remote.RemoteScreenshotResult;
import games.rednblack.h2d.common.remote.RemoteTypeNamesRequest;
import games.rednblack.h2d.common.remote.RemoteTypeNamesResult;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Editor-core bridge that fulfills plugin "remote operation" requests on the libGDX
 * render thread. The MCP plugin (sandboxed to common-api + runtime) cannot reach the
 * renderer, ResourceManager, or the entity-type-name map directly, so it sends
 * {@code MsgAPI.ACTION_REMOTE_*} notifications carrying a {@link RemoteHandle}; this
 * mediator does the work and completes the handle. GL/editor state access happens
 * inside {@code Gdx.app.postRunnable}; the static type-name map is read directly.
 *
 * Asset and type-name enumeration reuses the exact data sources the resource panel and
 * items tree use (ProjectInfoVO.imagesPacks, ResourceManager getters,
 * EntityMetadata.itemTypeNameMap) — no new access points are added.
 */
public class RemoteOpsMediator extends Mediator<Object> {
    public static final String NAME = RemoteOpsMediator.class.getCanonicalName();

    public RemoteOpsMediator() {
        super(NAME, new Object());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.ACTION_REMOTE_SCREENSHOT,
                MsgAPI.ACTION_REMOTE_LIST_ASSETS,
                MsgAPI.ACTION_REMOTE_TYPE_NAMES);
        interests.add(MsgAPI.ACTION_REMOTE_EDIT,
                MsgAPI.ACTION_REMOTE_DELETE,
                MsgAPI.ACTION_REMOTE_OPEN_SCENE);
        interests.add(MsgAPI.ACTION_REMOTE_SCENE_SETTINGS);
        interests.add(MsgAPI.ACTION_REMOTE_CREATE_ENTITY);
        interests.add(MsgAPI.ACTION_REMOTE_EDITABLE_COMPONENTS);
        interests.add(MsgAPI.ACTION_REMOTE_CREATE_SHADER);
        interests.add(MsgAPI.ACTION_REMOTE_ASSET_DIMENSIONS);
        interests.add(MsgAPI.ACTION_REMOTE_SET_Z_INDEX);
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case MsgAPI.ACTION_REMOTE_SCREENSHOT: {
                RemoteScreenshotRequest req = (RemoteScreenshotRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> captureScreenshot(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_LIST_ASSETS: {
                RemoteAssetsRequest req = (RemoteAssetsRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> listAssets(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_TYPE_NAMES: {
                RemoteTypeNamesRequest req = (RemoteTypeNamesRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                typeNames(req); // static map, safe to read on any thread
                break;
            }
            case MsgAPI.ACTION_REMOTE_EDIT: {
                RemoteEditRequest req = (RemoteEditRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> doEdit(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_DELETE: {
                RemoteDeleteRequest req = (RemoteDeleteRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> doDelete(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_OPEN_SCENE: {
                RemoteOpenSceneRequest req = (RemoteOpenSceneRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> openScene(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_SCENE_SETTINGS: {
                RemoteSceneSettingsRequest req = (RemoteSceneSettingsRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> getSceneSettings(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_CREATE_ENTITY: {
                RemoteCreateEntityRequest req = (RemoteCreateEntityRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> createEntity(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_EDITABLE_COMPONENTS: {
                RemoteEditableComponentsRequest req = (RemoteEditableComponentsRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> editableComponents(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_CREATE_SHADER: {
                RemoteCreateShaderRequest req = (RemoteCreateShaderRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> createShader(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_ASSET_DIMENSIONS: {
                RemoteAssetDimensionsRequest req = (RemoteAssetDimensionsRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> getAssetDimensions(req));
                break;
            }
            case MsgAPI.ACTION_REMOTE_SET_Z_INDEX: {
                RemoteZIndexRequest req = (RemoteZIndexRequest) notification.getBody();
                if (req == null || req.handle == null) return;
                Gdx.app.postRunnable(() -> setZIndex(req));
                break;
            }
        }
    }

    private void captureScreenshot(RemoteScreenshotRequest req) {
        RemoteScreenshotResult result = new RemoteScreenshotResult();
        try {
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            HyperLap2dRendererMiniMap renderer = sandbox.getEngine().getSystem(HyperLap2dRendererMiniMap.class);
            if (renderer == null) {
                result.ok = false;
                result.error = "minimap renderer not available";
                req.handle.complete(result);
                return;
            }
            int rootEntity = sandbox.getSceneControl().getRootEntity();
            if (rootEntity < 0) {
                result.ok = false;
                result.error = "no scene loaded (open a project first)";
                req.handle.complete(result);
                return;
            }

            Pixmap pixmap;
            switch (req.mode) {
                case WHOLE:
                    pixmap = renderer.getMiniMapPixmap(rootEntity);
                    break;
                case VIEW:
                    pixmap = renderer.getMainScreenPixmap();
                    break;
                case REGION:
                    pixmap = renderer.getRegionPixmap(rootEntity, req.x, req.y, req.width, req.height);
                    break;
                default:
                    pixmap = renderer.getMiniMapPixmap(rootEntity);
            }
            if (pixmap == null) {
                result.ok = false;
                result.error = (req.mode == RemoteScreenshotRequest.Mode.VIEW)
                        ? "current view not available (no scene loaded?)"
                        : "scene is empty or region has zero size (nothing to capture)";
                req.handle.complete(result);
                return;
            }
            try {
                result.width = pixmap.getWidth();
                result.height = pixmap.getHeight();
                result.pngBytes = pngBytes(pixmap, result.width, result.height);
                result.ok = true;
            } finally {
                pixmap.dispose();
            }
        } catch (Throwable t) {
            result.ok = false;
            result.error = "screenshot failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private static byte[] pngBytes(Pixmap pixmap, int w, int h) {
        FileHandle tmp = Gdx.files.absolute(System.getProperty("java.io.tmpdir", "/tmp")
                + "/h2d-mcp-shot-" + System.nanoTime() + ".png");
        PixmapIO.writePNG(tmp, pixmap);
        byte[] bytes = tmp.readBytes();
        tmp.delete();
        return bytes;
    }

    private void listAssets(RemoteAssetsRequest req) {
        RemoteAssetsResult result = new RemoteAssetsResult();
        try {
            Facade facade = Facade.getInstance();
            ResourceManager rm = facade.retrieveProxy(ResourceManager.NAME);
            ProjectManager pm = facade.retrieveProxy(ProjectManager.NAME);

            // Loaded runtime resources (same getters the resource-panel tabs use).
            addAll(result, "spineAnimation", rm.getProjectSpineAnimationsList().keySet());
            addAll(result, "spriteAnimation", rm.getProjectSpriteAnimationsList().keySet());
            addAll(result, "particleEffect", rm.getProjectParticleList().keySet());
            addAll(result, "talosEffect", rm.getProjectTalosList().keySet());
            addAll(result, "font", rm.getBitmapFontList().keySet());
            addAll(result, "tinyvg", rm.getTinyVGList().keySet());
            addAll(result, "shader", rm.getShaders().keySet());

            // Image / 9-patch regions — same source and detection as UIImagesTabMediator:
            // ProjectInfoVO.imagesPacks (atlas -> region names) + getTextureAtlas + findValue("split").
            ProjectInfoVO pi = pm.getCurrentProjectInfoVO();
            if (pi != null && pi.imagesPacks != null) {
                for (Map.Entry<String, TexturePackVO> entry : pi.imagesPacks.entrySet()) {
                    String atlasName = entry.getKey();
                    TexturePackVO pack = entry.getValue();
                    if (pack == null || pack.regions == null) continue;
                    TextureAtlas atlas = rm.getTextureAtlas(atlasName);
                    if (atlas != null) {
                        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
                            if (!pack.regions.contains(region.name)) continue;
                            if ("white-pixel".equals(region.name)) continue;
                            boolean ninePatch = region.findValue("split") != null;
                            result.add(ninePatch ? "ninePatchRegion" : "imageRegion", region.name);
                        }
                    } else {
                        for (String regionName : pack.regions) {
                            if ("white-pixel".equals(regionName)) continue;
                            result.add("imageRegion", regionName);
                        }
                    }
                }
            }
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "list assets failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private void typeNames(RemoteTypeNamesRequest req) {
        RemoteTypeNamesResult result = new RemoteTypeNamesResult();
        try {
            result.names.putAll(EntityMetadata.itemTypeNameMap); // same map the items tree uses
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "type names failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    /** Per-region pixel + world dimensions (world = pixel / pixelsPerWU) for layout planning. */
    private void getAssetDimensions(RemoteAssetDimensionsRequest req) {
        RemoteAssetDimensionsResult result = new RemoteAssetDimensionsResult();
        try {
            Facade facade = Facade.getInstance();
            ResourceManager rm = facade.retrieveProxy(ResourceManager.NAME);
            ProjectManager pm = facade.retrieveProxy(ProjectManager.NAME);
            Sandbox sandbox = PluginUIBridge.get(facade).getSandbox();
            int ppwu = sandbox.getPixelPerWU();
            result.pixelsPerWU = ppwu;
            ProjectInfoVO pi = pm.getCurrentProjectInfoVO();
            if (pi != null && pi.imagesPacks != null) {
                for (Map.Entry<String, TexturePackVO> entry : pi.imagesPacks.entrySet()) {
                    TexturePackVO pack = entry.getValue();
                    if (pack == null || pack.regions == null) continue;
                    TextureAtlas atlas = rm.getTextureAtlas(entry.getKey());
                    if (atlas == null) continue;
                    for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
                        if (!pack.regions.contains(region.name) || "white-pixel".equals(region.name)) continue;
                        int pw = region.getRegionWidth();
                        int ph = region.getRegionHeight();
                        boolean ninePatch = region.findValue("split") != null;
                        float ww = ppwu > 0 ? pw / (float) ppwu : 0f;
                        float wh = ppwu > 0 ? ph / (float) ppwu : 0f;
                        result.regions.add(new RemoteAssetDimensionsResult.RegionDim(region.name, pw, ph, ww, wh, ninePatch));
                    }
                }
            }
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "asset dimensions failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private static void addAll(RemoteAssetsResult result, String category, java.util.Collection<String> names) {
        if (names == null) return;
        for (String n : names) result.add(category, n);
    }

    /** Set an entity's z-index (local to its layer) via the undoable command. */
    private void setZIndex(RemoteZIndexRequest req) {
        RemoteEditResult result = new RemoteEditResult();
        try {
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            Engine engine = sandbox.getEngine();
            int entity = EntityUtils.getByUniqueId(req.entityId);
            if (entity < 0 || !engine.getEntityManager().isActive(entity)
                    || ComponentRetriever.get(entity, MainItemComponent.class, engine) == null) {
                result.ok = false;
                result.error = "entity not found or invalid: " + req.entityId;
                req.handle.complete(result);
                return;
            }
            // Route through the undoable command (same path the UI's z-index actions use).
            Facade.getInstance().sendNotification(MsgAPI.ACTION_SET_ENTITY_Z_INDEX,
                    new Object[]{entity, req.zIndex});
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "set z-index failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    // ---- Validated edits (drive the existing properties panels off-stage) ----

    private void doEdit(RemoteEditRequest req) {
        RemoteEditResult result = new RemoteEditResult();
        try {
            if (req.op == null) {
                result.ok = false;
                result.error = "missing op";
                req.handle.complete(result);
                return;
            }
            // Scene edits have no entity — handle before entity resolution (which would throw on null entityId).
            if (req.op == RemoteEditRequest.Op.SET_FIELDS && "scene".equals(req.componentKey)) {
                setSceneFields(req.fields, result);
                req.handle.complete(result);
                return;
            }
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            Engine engine = sandbox.getEngine();
            int entity = EntityUtils.getByUniqueId(req.entityId);
            if (entity < 0 || !engine.getEntityManager().isActive(entity)
                    || ComponentRetriever.get(entity, MainItemComponent.class, engine) == null) {
                result.ok = false;
                result.error = "entity not found or invalid: " + req.entityId;
                req.handle.complete(result);
                return;
            }

            switch (req.op) {
                case ADD_COMPONENT: {
                    Class<? extends Component> cls = AddableComponents.classForKey(req.componentKey);
                    if (cls == null) {
                        result.ok = false;
                        result.error = "unknown component key: " + req.componentKey;
                        break;
                    }
                    if (!AddableComponents.isAddable(entity, req.componentKey, engine)) {
                        result.ok = false;
                        result.error = "component '" + req.componentKey
                                + "' is not addable to this entity (not allowed for its type, or already present)";
                        break;
                    }
                    Facade.getInstance().sendNotification(MsgAPI.ACTION_ADD_COMPONENT,
                            AddComponentToItemCommand.payload(entity, cls));
                    result.ok = true;
                    break;
                }
                case REMOVE_COMPONENT: {
                    Class<? extends Component> cls = AddableComponents.classForKey(req.componentKey);
                    if (cls == null) {
                        result.ok = false;
                        result.error = "unknown component key: " + req.componentKey;
                        break;
                    }
                    if (ComponentRetriever.get(entity, cls, engine) == null) {
                        result.ok = false;
                        result.error = "entity does not have component '" + req.componentKey + "'";
                        break;
                    }
                    Facade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT,
                            RemoveComponentFromItemCommand.payload(entity, cls));
                    result.ok = true;
                    break;
                }
                case SET_FIELDS:
                    setFields(engine, entity, req.componentKey, req.fields, result);
                    break;
            }
        } catch (Throwable t) {
            result.ok = false;
            result.error = "edit failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    /**
     * Drive the properties panel for {@code componentKey} off-stage: instantiate a transient
     * (unregistered) panel+mediator, bind it to the entity, set the requested field values on
     * the panel's widgets, run the same VisUI validators the user-typing path runs, and only
     * then apply via the mediator's view-to-data translation (which sends the update command,
     * undoable). No notification/event is fired, so the live UI's mediators are untouched.
     */
    private void setFields(Engine engine, int entity, String componentKey,
                           Map<String, Object> fields, RemoteEditResult result) {
        String key = componentKey == null ? "" : componentKey;
        UIAbstractEntityPropertiesMediator<?> mediator;

        MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, engine);
        int type = main != null ? main.entityType : -1;

        // Applicability check — shared with editableComponents discovery so the two can't drift.
        String appErr = panelApplicabilityError(key, entity, type, engine);
        if (appErr != null) {
            result.ok = false;
            result.error = appErr;
            return;
        }

        switch (key) {
            case "basic":
            case "transform":
                mediator = new UIBasicItemPropertiesMediator(); break;
            case "label": mediator = new UILabelItemPropertiesMediator(); break;
            case "particle": mediator = new UIParticlePropertiesMediator(); break;
            case "image": mediator = new UIImageItemPropertiesMediator(); break;
            case "composite": mediator = new UICompositeItemPropertiesMediator(); break;
            case "sprite": mediator = new UISpriteAnimationItemPropertiesMediator(); break;
            case "spine": mediator = new UISpineAnimationItemPropertiesMediator(); break;
            case "talos": mediator = new UITalosPropertiesMediator(); break;
            case "lightItem": mediator = new UILightItemPropertiesMediator(); break;
            case "shader": mediator = new UIShaderPropertiesMediator(); break;
            case "physics": mediator = new UIPhysicsPropertiesMediator(); break;
            case "light": mediator = new UILightBodyPropertiesMediator(); break;
            case "circle": mediator = new UICircleShapePropertiesMediator(); break;
            case "polygon": mediator = new UIPolygonComponentPropertiesMediator(); break;
            case "sensor": mediator = new UISensorPropertiesMediator(); break;
            case "layout": mediator = new UILayoutPropertiesMediator(); break;
            default:
                result.ok = false;
                result.error = "unsupported component key for SET_FIELDS: " + componentKey
                        + " (supported: basic/transform, label, particle, image, composite, sprite, spine, talos, "
                        + "lightItem, shader, physics, light, circle, polygon, sensor, layout)";
                return;
        }

        RemoteEditablePanel panel = (RemoteEditablePanel) mediator.getViewComponent();

        // Run onRegister manually (the instance is not facade-registered) so panels that do
        // setup there — e.g. UILabelItemPropertiesMediator populates font lists — are ready.
        // For panels with no onRegister override this is a no-op.
        mediator.onRegister();

        // Bind to the entity — populates the widgets with current values (unset fields keep current).
        mediator.setItem(entity);

        // Apply requested field values via the panel's own setters.
        List<String> applyErrors = new ArrayList<>();
        if (fields != null) {
            for (Map.Entry<String, Object> e : fields.entrySet()) {
                try {
                    panel.setFieldValue(e.getKey(), e.getValue());
                } catch (RuntimeException ex) {
                    applyErrors.add(e.getKey() + ": " + ex.getMessage());
                }
            }
        }
        if (!applyErrors.isEmpty()) {
            result.ok = false;
            result.validationErrors.addAll(applyErrors);
            return;
        }

        // Run the same widget validators the UI runs.
        List<String> validationErrors = panel.validateFieldValues();
        if (!validationErrors.isEmpty()) {
            result.ok = false;
            result.validationErrors.addAll(validationErrors);
            return;
        }

        // Apply — same path as the user typing in the panel (sends the update command, undoable).
        mediator.applyViewToItemData();
        result.ok = true;
    }

    /**
     * Single source of truth for whether a componentKey's panel applies to an entity.
     * Returns null if applicable, else an error message. Used by both setFields (editing) and
     * editableComponents (discovery) so the two can't drift.
     */
    private static String panelApplicabilityError(String key, int entity, int type, Engine engine) {
        switch (key) {
            case "basic": case "transform": return null;
            case "label": return type == EntityFactory.LABEL_TYPE ? null : "entity is not a label";
            case "particle": return type == EntityFactory.PARTICLE_TYPE ? null : "entity is not a particle effect";
            case "image": return type == EntityFactory.IMAGE_TYPE ? null : "entity is not an image";
            case "composite": return type == EntityFactory.COMPOSITE_TYPE ? null : "entity is not a composite";
            case "sprite": return type == EntityFactory.SPRITE_TYPE ? null : "entity is not a sprite animation";
            case "spine": return type == SpineItemType.SPINE_TYPE ? null : "entity is not a spine animation";
            case "talos": return type == TalosItemType.TALOS_TYPE ? null : "entity is not a talos effect";
            case "lightItem": return type == EntityFactory.LIGHT_TYPE ? null : "entity is not a light item";
            case "shader": return ComponentRetriever.get(entity, ShaderComponent.class, engine) != null ? null : "entity has no Shader component; add it first (add_component Shader)";
            case "physics": return ComponentRetriever.get(entity, PhysicsBodyComponent.class, engine) != null ? null : "entity has no Physics component; add it first (add_component Physics)";
            case "light": return ComponentRetriever.get(entity, LightBodyComponent.class, engine) != null ? null : "entity has no Light component; add it first (add_component Light)";
            case "circle": return ComponentRetriever.get(entity, CircleShapeComponent.class, engine) != null ? null : "entity has no Circle Shape component; add it first (add_component 'Circle Shape')";
            case "polygon": return ComponentRetriever.get(entity, PolygonShapeComponent.class, engine) != null ? null : "entity has no Polygon Shape component; add it first (add_component 'Polygon Shape')";
            case "sensor": return ComponentRetriever.get(entity, SensorComponent.class, engine) != null ? null : "entity has no Sensor component; add it first (add_component 'Physics Sensors')";
            case "layout": return ComponentRetriever.get(entity, LayoutComponent.class, engine) != null ? null : "entity has no Layout component; add it first (add_component Layout)";
            default: return "unsupported component key: " + key;
        }
    }

    private static final String[] ALL_PANEL_KEYS = {
            "basic", "label", "particle", "image", "composite", "sprite", "spine", "talos", "lightItem",
            "shader", "physics", "light", "circle", "polygon", "sensor", "layout"
    };

    /** Discovery: which componentKeys are editable on the entity + which components can be added. */
    private void editableComponents(RemoteEditableComponentsRequest req) {
        RemoteEditableComponentsResult result = new RemoteEditableComponentsResult();
        try {
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            Engine engine = sandbox.getEngine();
            int entity = EntityUtils.getByUniqueId(req.entityId);
            if (entity < 0 || !engine.getEntityManager().isActive(entity)
                    || ComponentRetriever.get(entity, MainItemComponent.class, engine) == null) {
                result.ok = false;
                result.error = "entity not found or invalid: " + req.entityId;
                req.handle.complete(result);
                return;
            }
            MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, engine);
            int type = main != null ? main.entityType : -1;
            for (String k : ALL_PANEL_KEYS) {
                if (panelApplicabilityError(k, entity, type, engine) == null) result.editable.add(k);
            }
            for (String k : AddableComponents.addableForEntity(entity, engine).keySet()) result.addable.add(k);
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "editable components failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private void doDelete(RemoteDeleteRequest req) {
        RemoteEditResult result = new RemoteEditResult();
        try {
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            Engine engine = sandbox.getEngine();
            int entity = EntityUtils.getByUniqueId(req.entityId);
            if (entity < 0 || !engine.getEntityManager().isActive(entity)) {
                result.ok = false;
                result.error = "entity not found: " + req.entityId;
                req.handle.complete(result);
                return;
            }
            // Standard delete path: select the entity, then run the delete command (undoable).
            Facade.getInstance().sendNotification(MsgAPI.ACTION_SET_SELECTION, SelectionPayload.single(entity));
            Facade.getInstance().sendNotification(MsgAPI.ACTION_DELETE);
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "delete failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private void openScene(RemoteOpenSceneRequest req) {
        RemoteEditResult result = new RemoteEditResult();
        try {
            if (req.sceneName == null || req.sceneName.isEmpty()) {
                result.ok = false;
                result.error = "missing sceneName";
                req.handle.complete(result);
                return;
            }
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            // Route through CHECK_EDITS_ACTION — the same path the UI uses — so unsaved changes are
            // prompted (and saved) before the scene switches. The runnable runs immediately if there
            // are no unsaved changes, or after the user confirms the save dialog. The handle is
            // completed inside the runnable; if the user cancels the dialog, the request times out.
            Facade.getInstance().sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> {
                RemoteEditResult r = new RemoteEditResult();
                try {
                    sandbox.loadScene(req.sceneName);
                    r.ok = true;
                } catch (Throwable t) {
                    r.ok = false;
                    r.error = "open scene failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
                }
                req.handle.complete(r);
            });
        } catch (Throwable t) {
            result.ok = false;
            result.error = "open scene failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
            req.handle.complete(result);
        }
    }

    /** Drive the scene properties panel off-stage (SceneVO observable) — validated, undoable. */
    private void setSceneFields(Map<String, Object> fields, RemoteEditResult result) {
        try {
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
            if (sceneVO == null) {
                result.ok = false;
                result.error = "no current scene";
                return;
            }
            UIScenePropertiesMediator mediator = new UIScenePropertiesMediator();
            RemoteEditablePanel panel = (RemoteEditablePanel) mediator.getViewComponent();
            // Suppress select-box change events so the always-registered live scene mediator
            // is not triggered mid-drive.
            panel.setProgrammaticSelectBoxes(true);
            mediator.onRegister();
            mediator.setItem(sceneVO);
            panel.refreshDisabledState(); // recompute directional disabled from the loaded lightType

            List<String> applyErrors = new ArrayList<>();
            if (fields != null) {
                // Apply lightType first — it toggles the directional fields' enabled state.
                if (fields.containsKey("lightType")) {
                    try {
                        panel.setFieldValue("lightType", fields.get("lightType"));
                        panel.refreshDisabledState();
                    } catch (RuntimeException ex) {
                        applyErrors.add("lightType: " + ex.getMessage());
                    }
                }
                for (Map.Entry<String, Object> e : fields.entrySet()) {
                    if ("lightType".equals(e.getKey())) continue;
                    try {
                        panel.setFieldValue(e.getKey(), e.getValue());
                    } catch (RuntimeException ex) {
                        applyErrors.add(e.getKey() + ": " + ex.getMessage());
                    }
                }
            }
            if (!applyErrors.isEmpty()) {
                result.ok = false;
                result.validationErrors.addAll(applyErrors);
                return;
            }
            List<String> validationErrors = panel.validateFieldValues();
            if (!validationErrors.isEmpty()) {
                result.ok = false;
                result.validationErrors.addAll(validationErrors);
                return;
            }
            mediator.applyViewToItemData();
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            StringBuilder sb = new StringBuilder("scene edit failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            for (StackTraceElement e : t.getStackTrace()) {
                sb.append("\n  at ").append(e);
                if (sb.length() > 600) { sb.append(" ..."); break; }
            }
            result.error = sb.toString();
        }
    }

    private void getSceneSettings(RemoteSceneSettingsRequest req) {
        RemoteSceneSettingsResult result = new RemoteSceneSettingsResult();
        try {
            Sandbox sandbox = PluginUIBridge.get(Facade.getInstance()).getSandbox();
            SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
            if (sceneVO == null) {
                result.ok = false;
                result.error = "no current scene";
                req.handle.complete(result);
                return;
            }
            result.sceneName = sceneVO.sceneName;
            result.settings.put("pixelsPerWU", sandbox.getPixelPerWU());
            result.settings.put("shader", sceneVO.shaderVO != null ? sceneVO.shaderVO.shaderName : "");

            result.settings.put("physicsEnabled", sceneVO.physicsPropertiesVO.enabled);
            result.settings.put("gravityX", sceneVO.physicsPropertiesVO.gravityX);
            result.settings.put("gravityY", sceneVO.physicsPropertiesVO.gravityY);
            result.settings.put("sleepVelocity", sceneVO.physicsPropertiesVO.sleepVelocity);

            LightsPropertiesVO lights = sceneVO.lightsPropertiesVO;
            result.settings.put("lightsEnabled", lights.enabled);
            result.settings.put("pseudo3d", lights.pseudo3d);
            result.settings.put("blurNum", lights.blurNum);
            result.settings.put("lightMapScale", lights.lightMapScale);
            result.settings.put("lightType", lights.lightType);
            result.settings.put("directionalRays", lights.directionalRays);
            result.settings.put("directionalDegree", lights.directionalDegree);
            result.settings.put("directionalHeight", lights.directionalHeight);
            result.settings.put("ambientColor", toRgbaList(lights.ambientColor));
            result.settings.put("directionalColor", toRgbaList(lights.directionalColor));
            result.ok = true;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "read scene settings failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private static java.util.List<Float> toRgbaList(float[] rgba) {
        java.util.List<Float> list = new java.util.ArrayList<>(4);
        if (rgba != null) for (float v : rgba) list.add(v);
        return list;
    }

    /** Create a new entity via the editor's ItemFactory (the same path as dragging an asset). */
    private void createEntity(RemoteCreateEntityRequest req) {
        RemoteCreateEntityResult result = new RemoteCreateEntityResult();
        try {
            if (req.type == null || req.type.isEmpty()) {
                result.ok = false;
                result.error = "missing type";
                req.handle.complete(result);
                return;
            }
            Facade facade = Facade.getInstance();
            ItemFactory factory = ItemFactory.get();
            Vector2 pos = new Vector2(req.x, req.y);

            // Validate name-based assets exist (don't create broken entities).
            String missing = assetMissing(facade, req.type, req.name);
            if (missing != null) {
                result.ok = false;
                result.error = missing;
                req.handle.complete(result);
                return;
            }

            int entity;
            switch (req.type) {
                case "image": factory.createSimpleImage(req.name, pos); entity = factory.getCreatedEntity(); break;
                case "spriteAnimation": factory.createSpriteAnimation(req.name, pos); entity = factory.getCreatedEntity(); break;
                case "spineAnimation": factory.createSpineAnimation(req.name, pos); entity = factory.getCreatedEntity(); break;
                case "libraryItem": factory.createItemFromLibrary(req.name, pos); entity = factory.getCreatedEntity(); break;
                case "9patch": factory.create9Patch(req.name, pos); entity = factory.getCreatedEntity(); break;
                case "tinyvg": entity = factory.createTinyVGItem(req.name, pos); break;
                case "particle": entity = factory.createParticleItem(req.name, pos); break;
                case "talos": entity = factory.createTalosItem(req.name, pos); break;
                case "primitive":
                    factory.createPrimitive(pos, PolygonShapeVO.createRect(req.width, req.height));
                    entity = factory.getCreatedEntity(); break;
                case "composite": entity = factory.createCompositeItem(pos); break;
                case "label": {
                    games.rednblack.editor.proxy.FontManager fontManager = facade.retrieveProxy(games.rednblack.editor.proxy.FontManager.NAME);
                    com.badlogic.gdx.utils.Array<String> fonts = fontManager.getFontNamesFromMap();
                    String family = (req.fontFamily == null || req.fontFamily.isEmpty()) ? null : req.fontFamily;
                    if (family == null) {
                        if (fonts.size == 0) {
                            result.ok = false;
                            result.error = "no fonts loaded in the project; add a font or pass a loaded fontFamily";
                            req.handle.complete(result);
                            return;
                        }
                        family = fonts.first();
                    } else if (!contains(fonts, family)) {
                        result.ok = false;
                        result.error = "fontFamily '" + family + "' not loaded; available: " + fonts;
                        req.handle.complete(result);
                        return;
                    }
                    TextTool tt = new TextTool();
                    tt.setFontFamily(family);
                    if (req.fontSize > 0) tt.setFontSize(req.fontSize);
                    entity = factory.createLabel(tt, pos); break;
                }
                case "light": {
                    LightVO vo = new LightVO();
                    vo.type = "CONE".equalsIgnoreCase(req.lightType)
                            ? LightObjectComponent.LightType.CONE
                            : LightObjectComponent.LightType.POINT;
                    entity = factory.createLightItem(vo, pos); break;
                }
                default:
                    result.ok = false;
                    result.error = "unknown type: " + req.type
                            + " (image, spriteAnimation, spineAnimation, libraryItem, 9patch, tinyvg, particle, talos, primitive, composite, label, light)";
                    req.handle.complete(result);
                    return;
            }
            if (entity < 0) {
                result.ok = false;
                result.error = "create failed (no scene loaded?)";
            } else {
                Engine engine = PluginUIBridge.get(facade).getSandbox().getEngine();
                MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, engine);
                result.uniqueId = (main != null && main.uniqueId != null) ? main.uniqueId : String.valueOf(entity);
                result.ok = true;
            }
        } catch (Throwable t) {
            result.ok = false;
            result.error = "create failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    /** Create a shader resource (write .vert/.frag + register) — same path as the shader manager dialog. */
    private void createShader(RemoteCreateShaderRequest req) {
        RemoteCreateShaderResult result = new RemoteCreateShaderResult();
        try {
            if (req.name == null || req.name.isEmpty()) {
                result.ok = false;
                result.error = "missing shader name";
                req.handle.complete(result);
                return;
            }
            Facade facade = Facade.getInstance();
            ResourceManager rm = facade.retrieveProxy(ResourceManager.NAME);
            ProjectManager pm = facade.retrieveProxy(ProjectManager.NAME);
            if (rm.getShaders().containsKey(req.name)) {
                result.ok = false;
                result.error = "shader '" + req.name + "' already exists";
                req.handle.complete(result);
                return;
            }
            String vertex, fragment;
            if (req.vertex != null && req.fragment != null) {
                vertex = req.vertex;
                fragment = req.fragment;
            } else {
                switch (req.templateType) {
                    case 1:
                        vertex = DefaultShaders.DISTANCE_FIELD_VERTEX_SHADER;
                        fragment = DefaultShaders.DISTANCE_FIELD_FRAGMENT_SHADER;
                        break;
                    case 2:
                        vertex = DefaultShaders.DEFAULT_SCREE_READING_VERTEX_SHADER;
                        fragment = DefaultShaders.DEFAULT_SCREE_READING_FRAGMENT_SHADER;
                        break;
                    default:
                        vertex = DefaultShaders.DEFAULT_ARRAY_VERTEX_SHADER;
                        fragment = DefaultShaders.DEFAULT_ARRAY_FRAGMENT_SHADER;
                        break;
                }
            }
            String dir = pm.getCurrentProjectPath() + File.separator + ProjectManager.SHADER_DIR_PATH + File.separator;
            // Compile-check from the source first so a failed compile leaves no stray files.
            com.badlogic.gdx.graphics.glutils.ShaderProgram sp = ShaderCompiler.compileShader(vertex, fragment);
            if (!sp.isCompiled()) {
                result.ok = false;
                result.error = "shader compile failed: " + sp.getLog();
                req.handle.complete(result);
                return;
            }
            FileHandle vert = new FileHandle(dir + req.name + ".vert");
            FileHandle frag = new FileHandle(dir + req.name + ".frag");
            FileUtils.writeStringToFile(vert.file(), vertex, "utf-8");
            FileUtils.writeStringToFile(frag.file(), fragment, "utf-8");
            rm.addShaderProgram(req.name, sp);
            result.ok = true;
            result.name = req.name;
        } catch (Throwable t) {
            result.ok = false;
            result.error = "create shader failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    /** @return an error message if the name-based asset for {@code type} is missing, else null. */
    private static String assetMissing(Facade facade, String type, String name) {
        ResourceManager rm = facade.retrieveProxy(ResourceManager.NAME);
        ProjectManager pm = facade.retrieveProxy(ProjectManager.NAME);
        ProjectInfoVO pi = pm.getCurrentProjectInfoVO();
        switch (type) {
            case "image":
            case "9patch":
                if (pi == null || pi.imagesPacks == null) return "no project loaded";
                for (TexturePackVO pack : pi.imagesPacks.values()) {
                    if (pack != null && pack.regions != null && pack.regions.contains(name)) return null;
                }
                return "image region '" + name + "' not found; check list_assets 'imageRegion'/'ninePatchRegion'";
            case "spriteAnimation":
                return rm.getProjectSpriteAnimationsList().containsKey(name) ? null
                        : "sprite animation '" + name + "' not found; check list_assets 'spriteAnimation'";
            case "spineAnimation":
                return rm.getProjectSpineAnimationsList().containsKey(name) ? null
                        : "spine animation '" + name + "' not found; check list_assets 'spineAnimation'";
            case "tinyvg":
                return rm.getTinyVGList().containsKey(name) ? null
                        : "tinyvg '" + name + "' not found; check list_assets 'tinyvg'";
            case "particle":
                return rm.getProjectParticleList().containsKey(name) ? null
                        : "particle effect '" + name + "' not found; check list_assets 'particleEffect'";
            case "talos":
                return rm.getProjectTalosList().containsKey(name) ? null
                        : "talos effect '" + name + "' not found; check list_assets 'talosEffect'";
            case "libraryItem":
                return (pi != null && pi.libraryItems.containsKey(name)) ? null
                        : "library item '" + name + "' not found; check the library";
            default:
                return null; // primitive/composite/label/light don't need an asset name
        }
    }

    private static boolean contains(com.badlogic.gdx.utils.Array<String> arr, String v) {
        for (int i = 0; i < arr.size; i++) if (arr.get(i).equals(v)) return true;
        return false;
    }
}