package games.rednblack.editor.plugin.mcp.tools;

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
import games.rednblack.h2d.common.remote.RemoteSceneSettingsRequest;
import games.rednblack.h2d.common.remote.RemoteSceneSettingsResult;
import games.rednblack.h2d.common.remote.RemoteScreenshotRequest;
import games.rednblack.h2d.common.remote.RemoteScreenshotResult;
import games.rednblack.h2d.common.remote.RemoteTypeNamesRequest;
import games.rednblack.h2d.common.remote.RemoteTypeNamesResult;
import games.rednblack.h2d.common.remote.RemoteZIndexRequest;

/**
 * Client-side helper for the editor-core RemoteOps bridge: builds a request + handle,
 * sends the notification, and awaits the result. Used for ops the sandboxed plugin
 * cannot reach directly (screenshot, listing loaded assets, and later validated edits).
 */
public class RemoteOps {
    private final McpContext ctx;

    public RemoteOps(McpContext ctx) {
        this.ctx = ctx;
    }

    public RemoteScreenshotResult screenshot(RemoteScreenshotRequest.Mode mode,
                                             float x, float y, float w, float h, long timeoutMs) {
        RemoteScreenshotRequest req = new RemoteScreenshotRequest();
        req.mode = mode;
        req.x = x; req.y = y; req.width = w; req.height = h;
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_SCREENSHOT, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteScreenshotResult err = new RemoteScreenshotResult();
            err.ok = false;
            err.error = "screenshot request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteAssetsResult listAssets(long timeoutMs) {
        RemoteAssetsRequest req = new RemoteAssetsRequest();
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_LIST_ASSETS, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteAssetsResult err = new RemoteAssetsResult();
            err.ok = false;
            err.error = "list assets request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteTypeNamesResult typeNames(long timeoutMs) {
        RemoteTypeNamesRequest req = new RemoteTypeNamesRequest();
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_TYPE_NAMES, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteTypeNamesResult err = new RemoteTypeNamesResult();
            err.ok = false;
            err.error = "type names request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteEditResult edit(RemoteEditRequest req, long timeoutMs) {
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_EDIT, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteEditResult err = new RemoteEditResult();
            err.ok = false;
            err.error = "edit request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteEditResult delete(String entityId, long timeoutMs) {
        RemoteDeleteRequest req = new RemoteDeleteRequest();
        req.entityId = entityId;
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_DELETE, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteEditResult err = new RemoteEditResult();
            err.ok = false;
            err.error = "delete request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteEditResult openScene(String sceneName, long timeoutMs) {
        RemoteOpenSceneRequest req = new RemoteOpenSceneRequest();
        req.sceneName = sceneName;
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_OPEN_SCENE, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteEditResult err = new RemoteEditResult();
            err.ok = false;
            err.error = "open scene request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteSceneSettingsResult getSceneSettings(long timeoutMs) {
        RemoteSceneSettingsRequest req = new RemoteSceneSettingsRequest();
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_SCENE_SETTINGS, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteSceneSettingsResult err = new RemoteSceneSettingsResult();
            err.ok = false;
            err.error = "scene settings request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteCreateEntityResult createEntity(RemoteCreateEntityRequest req, long timeoutMs) {
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_CREATE_ENTITY, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteCreateEntityResult err = new RemoteCreateEntityResult();
            err.ok = false;
            err.error = "create entity request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteEditableComponentsResult editableComponents(String entityId, long timeoutMs) {
        RemoteEditableComponentsRequest req = new RemoteEditableComponentsRequest();
        req.entityId = entityId;
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_EDITABLE_COMPONENTS, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteEditableComponentsResult err = new RemoteEditableComponentsResult();
            err.ok = false;
            err.error = "editable components request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteCreateShaderResult createShader(RemoteCreateShaderRequest req, long timeoutMs) {
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_CREATE_SHADER, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteCreateShaderResult err = new RemoteCreateShaderResult();
            err.ok = false;
            err.error = "create shader request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteAssetDimensionsResult assetDimensions(long timeoutMs) {
        RemoteAssetDimensionsRequest req = new RemoteAssetDimensionsRequest();
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_ASSET_DIMENSIONS, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteAssetDimensionsResult err = new RemoteAssetDimensionsResult();
            err.ok = false;
            err.error = "asset dimensions request failed: " + e.getMessage();
            return err;
        }
    }

    public RemoteEditResult setZIndex(String entityId, int zIndex, long timeoutMs) {
        RemoteZIndexRequest req = new RemoteZIndexRequest();
        req.entityId = entityId;
        req.zIndex = zIndex;
        req.handle = new RemoteHandle<>();
        ctx.facade().sendNotification(MsgAPI.ACTION_REMOTE_SET_Z_INDEX, req);
        try {
            return req.handle.await(timeoutMs);
        } catch (Exception e) {
            RemoteEditResult err = new RemoteEditResult();
            err.ok = false;
            err.error = "set z-index request failed: " + e.getMessage();
            return err;
        }
    }
}