package games.rednblack.editor.system;

import games.rednblack.editor.renderer.ecs.annotations.All;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import games.rednblack.editor.renderer.components.BoundingBoxComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.systems.render.HyperLap2dRenderer;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogic;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

@All(ViewPortComponent.class)
public class HyperLap2dRendererMiniMap extends HyperLap2dRenderer {

    private final Rectangle bounds = new Rectangle();

    private final OrthographicCamera minimapCamera = new OrthographicCamera();
    private Camera minimapOldCamera;

    public HyperLap2dRendererMiniMap(Batch batch, boolean hasStencilBuffer, int samples) {
        super(batch, hasStencilBuffer, samples);
    }

    /**
     * Frames the given world-space rectangle, renders the whole scene into the "minimap" FBO
     * (with culling off so everything is drawn), and leaves the FBO bound. Returns the pixel
     * size, or null if the rectangle is empty.
     */
    private int[] beginMiniMap(int rootEntity, Rectangle worldBounds) {
        minimapOldCamera = camera;
        camera = minimapCamera;
        minimapCamera.setToOrtho(true, worldBounds.width, worldBounds.height);
        minimapCamera.position.set(worldBounds.x + worldBounds.width / 2, worldBounds.y + worldBounds.height / 2, 0);
        if (worldBounds.width == 0 || worldBounds.height == 0) {
            camera = minimapOldCamera;
            return null;
        }

        int w = (int) (worldBounds.width * pixelsPerWU);
        int h = (int) (worldBounds.height * pixelsPerWU);

        // Cap the FBO to a safe max per side (the GL_MAX_TEXTURE_SIZE cap alone can be far larger
        // than is actually allocatable — a 40000px region crashed native gdx2d_clear). Preserve aspect.
        int maxDim = 8192;
        if (w > maxDim || h > maxDim) {
            float s = Math.min(maxDim / (float) w, maxDim / (float) h);
            w = (int) (w * s);
            h = (int) (h * s);
        }
        if (w < 1) w = 1;
        if (h < 1) h = 1;

        Gdx.gl.glClearColor(0.318f, 0.318f, 0.318f, 1);
        frameBufferManager.createIfNotExists("minimap", w, h, false, hasStencilBuffer);
        frameBufferManager.begin("minimap");
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        minimapCamera.update();
        batch.setProjectionMatrix(minimapCamera.combined);

        drawableLogicMapper.beginPipeline();
        batch.begin();
        enableCull = false;
        drawRecursively(rootEntity, 1f, DrawableLogic.RenderingType.TEXTURE);
        enableCull = true;
        batch.end();

        return new int[]{w, h};
    }

    private void endMiniMap() {
        frameBufferManager.endCurrent();
        drawableLogicMapper.endPipeline();
        camera = minimapOldCamera;
    }

    /** Whole-scene bounds (union of the root's direct children bounding boxes). */
    private Rectangle computeWholeSceneBounds(int rootEntity) {
        NodeComponent nodeComponent = nodeMapper.get(rootEntity);
        bounds.set(0, 0, 0, 0);
        Integer[] children = nodeComponent.children.begin();
        for (int i = 0, n = nodeComponent.children.size; i < n; i++) {
            int child = children[i];
            BoundingBoxComponent boundingBoxComponent = SandboxComponentRetriever.get(child, BoundingBoxComponent.class);
            if (boundingBoxComponent != null && boundingBoxComponent.rectangle != null)
                bounds.merge(boundingBoxComponent.rectangle);
        }
        nodeComponent.children.end();
        return bounds;
    }

    public Texture getMiniMapTexture(int rootEntity) {
        int[] size = beginMiniMap(rootEntity, computeWholeSceneBounds(rootEntity));
        if (size == null) return null;
        endMiniMap();
        return frameBufferManager.getColorBufferTexture("minimap");
    }

    /** Whole scene → PNG-ready pixmap (render thread). null if empty. Caller disposes. */
    public Pixmap getMiniMapPixmap(int rootEntity) {
        int[] size = beginMiniMap(rootEntity, computeWholeSceneBounds(rootEntity));
        if (size == null) return null;
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, size[0], size[1]);
        endMiniMap();
        return pixmap;
    }

    /** Custom world-space rectangle → PNG-ready pixmap (render thread). null if region is empty. Caller disposes. */
    public Pixmap getRegionPixmap(int rootEntity, float x, float y, float width, float height) {
        Rectangle region = new Rectangle(x, y, width, height);
        int[] size = beginMiniMap(rootEntity, region);
        if (size == null) return null;
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, size[0], size[1]);
        endMiniMap();
        return pixmap;
    }

    /**
     * The current editor view (the "main" FBO rendered each frame, scene only — no UI) → PNG-ready
     * pixmap (render thread). null if the main FBO isn't available. Caller disposes.
     */
    public Pixmap getMainScreenPixmap() {
        Texture main = frameBufferManager.getColorBufferTexture("main");
        if (main == null) return null;
        int w = main.getWidth();
        int h = main.getHeight();
        frameBufferManager.begin("main");
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, w, h);
        frameBufferManager.endCurrent();
        return pixmap;
    }

    public Rectangle getMiniMapBounds() {
        return bounds;
    }
}