package games.rednblack.editor.view.ui.followers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.extention.spine.SpineObjectComponent;

public class SpineFollower extends NormalSelectionFollower {
    private final SpineObjectComponent spineObjectComponent;
    private final SkeletonRendererDebug skeletonRendererDebug;

    private final Matrix4 matrix = new Matrix4();
    private final Affine2 affine2 = new Affine2();

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

            skeletonRendererDebug.getShapeRenderer().setProjectionMatrix(Sandbox.getInstance().getCamera().combined);

            float originX = transformComponent.originX;
            float originY = transformComponent.originY;
            float x = transformComponent.x;
            float y = transformComponent.y;
            float rotation = transformComponent.rotation;
            float scaleX = transformComponent.scaleX;
            float scaleY = transformComponent.scaleY;

            affine2.setToTrnRotScl(x + originX , y + originY, rotation, scaleX, scaleY);
            if (originX != 0 || originY != 0) affine2.translate(-originX, -originY);
            affine2.translate(-spineObjectComponent.minX, -spineObjectComponent.minY);

            matrix.set(affine2);

            skeletonRendererDebug.getShapeRenderer().setTransformMatrix(matrix);

            skeletonRendererDebug.draw(spineObjectComponent.skeleton);

            batch.begin();
        }
    }
}
