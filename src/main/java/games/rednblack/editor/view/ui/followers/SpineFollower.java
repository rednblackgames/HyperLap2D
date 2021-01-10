package games.rednblack.editor.view.ui.followers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.extention.spine.SpineObjectComponent;

public class SpineFollower extends NormalSelectionFollower {
    private final SpineObjectComponent spineObjectComponent;
    private final SkeletonRendererDebug skeletonRendererDebug;

    private final Matrix4 matrix = new Matrix4();

    public SpineFollower(Entity entity) {
        super(entity);
        spineObjectComponent = ComponentRetriever.get(entity, SpineObjectComponent.class);
        skeletonRendererDebug = new SkeletonRendererDebug();
        float pixelsPerWU = 1f / Sandbox.getInstance().getPixelPerWU();
        skeletonRendererDebug.setScale(pixelsPerWU);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            batch.end();

            matrix.set(transformComponent.worldTransform);

            skeletonRendererDebug.getShapeRenderer().setProjectionMatrix(Sandbox.getInstance().getCamera().combined);
            skeletonRendererDebug.getShapeRenderer().setTransformMatrix(matrix);

            skeletonRendererDebug.draw(spineObjectComponent.skeleton);

            batch.begin();
        }
    }
}
