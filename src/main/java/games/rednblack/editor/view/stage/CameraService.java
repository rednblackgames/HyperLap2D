package games.rednblack.editor.view.stage;
import games.rednblack.editor.proxy.EntityDataProxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

/**
 * Owns the editor camera: zoom/pan animation, coordinate transforms
 * (screen<->world), input-coordinate scaling, and viewport access. Extracted
 * verbatim from {@link Sandbox} (Phase 2 decomposition). {@code Sandbox} holds
 * an instance and delegates; the service calls back into {@code Sandbox} for
 * the bits that stay there (current viewing entity, UI-stage UI-scale density,
 * scene config, isViewingRootEntity).
 */
public class CameraService {

    private static final float CAMERA_ZOOM_DURATION = 0.65f;
    private static final float CAMERA_PAN_DURATION = 0.45f;

    private final Sandbox sandbox;
    private final Facade facade;

    private static final Vector3 temp = new Vector3();
    private static final Vector2 tmp = new Vector2();
    private float timeToCameraZoomTarget, cameraZoomTarget, cameraZoomOrigin;
    private boolean moveCameraWithZoom = false;

    private float timeToCameraPosTarget;
    private final Vector2 cameraPosTarget = new Vector2();
    private final Vector2 cameraPosOrigin = new Vector2();

    public CameraService(Sandbox sandbox, Facade facade) {
        this.sandbox = sandbox;
        this.facade = facade;
    }

    /**
     * Camera zoom + pan animation, called each frame by {@link Sandbox#render(float)}.
     * (The per-frame culling-debug toggle stays on {@code Sandbox}.)
     */
    public void update(float deltaTime) {
        if (timeToCameraZoomTarget > 0) {
            getCamera().unproject(temp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            float px = temp.x;
            float py = temp.y;

            timeToCameraZoomTarget -= deltaTime;
            float progress = timeToCameraZoomTarget < 0 ? 1 : 1f - timeToCameraZoomTarget / CAMERA_ZOOM_DURATION;
            getCamera().zoom = Interpolation.pow3Out.apply(cameraZoomOrigin, cameraZoomTarget, progress);
            getCamera().update();

            if (moveCameraWithZoom) {
                getCamera().unproject(temp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                getCamera().position.add(px - temp.x, py - temp.y, 0);
                getCamera().update();
            }

            facade.sendNotification(MsgAPI.ZOOM_CHANGED);
        }

        if (timeToCameraPosTarget > 0) {
            timeToCameraPosTarget -= deltaTime;
            float progress = timeToCameraPosTarget < 0 ? 1 : 1f - timeToCameraPosTarget / CAMERA_PAN_DURATION;
            float x = Interpolation.smoother.apply(cameraPosOrigin.x, cameraPosTarget.x, progress);
            float y = Interpolation.smoother.apply(cameraPosOrigin.y, cameraPosTarget.y, progress);
            getCamera().position.set(x, y, 0);

            facade.sendNotification(PanTool.SCENE_PANNED);
        }
    }

    public void adjustCameraInComposites() {
        if (!sandbox.isViewingRootEntity()) {
            panSceneTo(0, 0);
        } else {
            panSceneTo(sandbox.sceneConfigVO.cameraPosition[0], sandbox.sceneConfigVO.cameraPosition[1]);
        }
    }

    public void scenePanned() {
        if (sandbox.isViewingRootEntity() && timeToCameraPosTarget <= 0) {
            sandbox.sceneConfigVO.cameraPosition[0] = getCamera().position.x;
            sandbox.sceneConfigVO.cameraPosition[1] = getCamera().position.y;
        }
    }

    public void panSceneTo(float x, float y) {
        cameraPosOrigin.set(getCamera().position.x, getCamera().position.y);
        cameraPosTarget.set(x, y);
        timeToCameraPosTarget = CAMERA_PAN_DURATION - timeToCameraPosTarget * 0.5f;
    }

    public void panSceneBy(float amountX, float amountY) {
        cameraPosOrigin.set(getCamera().position.x, getCamera().position.y);
        cameraPosTarget.set(cameraPosOrigin.x + amountX, cameraPosOrigin.y + amountY);
        timeToCameraPosTarget = CAMERA_PAN_DURATION - timeToCameraPosTarget * 0.5f;
    }

    public Vector2 getCameraPosTarget() {
        return cameraPosTarget;
    }

    public float getCameraZoomTarget() {
        return cameraZoomTarget;
    }

    public int getZoomPercent() {
        return (int) sandbox.sceneConfigVO.cameraZoom;
    }

    public Vector3 getCameraPosition() {
        return getCamera().position;
    }

    public void setZoomPercent(float percent, boolean moveCamera) {
        sandbox.sceneConfigVO.cameraZoom = percent;

        cameraZoomOrigin = getCamera().zoom;
        cameraZoomTarget = 1f / (sandbox.sceneConfigVO.cameraZoom / 100f);

        timeToCameraZoomTarget = CAMERA_ZOOM_DURATION;
        moveCameraWithZoom = moveCamera;
    }

    public void zoomDivideBy(float amount) {
        float zoomPercent = sandbox.sceneConfigVO.cameraZoom / amount;
        if (zoomPercent < 20) zoomPercent = 20;
        if (zoomPercent > 1000) zoomPercent = 1000;

        setZoomPercent(zoomPercent, false);
    }

    public OrthographicCamera getCamera() {
        return (OrthographicCamera) getViewport().getCamera();
    }

    public ViewPortComponent getViewportComponent() {
        if (sandbox.getCurrentViewingEntity() == -1) return null;
        return EntityDataProxy.get().get(sandbox.getCurrentViewingEntity(), ViewPortComponent.class);
    }

    public Viewport getViewport() {
        ViewPortComponent viewPortComponent = getViewportComponent();
        if (viewPortComponent == null) return null;
        return viewPortComponent.viewPort;
    }

    public Rectangle screenToWorld(Rectangle rect) {
        Vector2 pos = screenToWorld(new Vector2(rect.x, rect.y));
        Vector2 pos2 = screenToWorld(new Vector2(rect.x + rect.width, rect.y + rect.height));
        rect.x = pos.x;
        rect.y = pos.y;
        rect.width = pos2.x - rect.x;
        rect.height = pos2.y - rect.y;
        return rect;
    }

    public Vector2 screenToWorld(Vector2 vector) {
        Viewport viewport = getViewport();
        if (viewport != null) {
            vector.scl(1f / sandbox.getUIStage().getUIScaleDensity());
            vector.y = Gdx.graphics.getHeight() - vector.y;
            vector = viewport.unproject(vector);
        }

        return vector;
    }

    public Vector2 worldToScreen(Vector2 vector) {
        Viewport viewport = getViewport();
        if (viewport != null) {
            vector = viewport.project(vector);
            vector.scl(sandbox.getUIStage().getUIScaleDensity());
        }

        return vector;
    }

    public Vector2 screenToWorld(float x, float y) {
        return screenToWorld(tmp.set(x, y));
    }

    public Vector2 worldToScreen(float x, float y) {
        return worldToScreen(tmp.set(x, y));
    }

    public float getInputX() {
        return getInputX(0);
    }

    public float getInputX(float offset) {
        return (Gdx.input.getX() + offset) * sandbox.getUIStage().getUIScaleDensity();
    }

    public float getInputY() {
        return getInputY(0);
    }

    public float getInputY(float offset) {
        return (Gdx.input.getY() + offset) * sandbox.getUIStage().getUIScaleDensity();
    }
}