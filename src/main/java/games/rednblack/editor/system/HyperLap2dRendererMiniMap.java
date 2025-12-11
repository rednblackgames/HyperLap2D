package games.rednblack.editor.system;

import com.artemis.annotations.All;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
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

    public HyperLap2dRendererMiniMap(Batch batch, boolean hasStencilBuffer, int samples) {
        super(batch, hasStencilBuffer, samples);
    }

    public Texture getMiniMapTexture(int rootEntity) {
        Camera oldCamera = camera;
        camera = minimapCamera;
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

        minimapCamera.setToOrtho(true, bounds.width, bounds.height);
        minimapCamera.position.set(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2,0);
        if (bounds.width == 0 || bounds.height == 0) return null;

        Gdx.gl.glClearColor(0.318f, 0.318f, 0.318f, 1);
        frameBufferManager.createIfNotExists("minimap", (int) (bounds.width * pixelsPerWU), (int) (bounds.height * pixelsPerWU), false, hasStencilBuffer);
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
        frameBufferManager.endCurrent();
        drawableLogicMapper.endPipeline();

        camera = oldCamera;

        return frameBufferManager.getColorBufferTexture("minimap");
    }

    public Rectangle getMiniMapBounds() {
        return bounds;
    }
}
