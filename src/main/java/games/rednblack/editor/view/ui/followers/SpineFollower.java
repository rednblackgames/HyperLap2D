package games.rednblack.editor.view.ui.followers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import games.rednblack.editor.utils.SkeletonDrawerDebug;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.extension.spine.SpineComponent;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SpineFollower extends NormalSelectionFollower {
    private ShapeDrawer shapeDrawer;
    private SkeletonDrawerDebug skeletonDrawerDebug;
    private final SpineComponent spineObjectComponent;

    private final Matrix4 oldTransformMatrix = new Matrix4();
    private final Matrix4 oldProjectionMatrix = new Matrix4();
    private final Matrix4 matrix = new Matrix4();

    public SpineFollower(int entity) {
        super(entity);
        spineObjectComponent = SandboxComponentRetriever.get(entity, SpineComponent.class);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null && shapeDrawer == null)
            shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
        if (skeletonDrawerDebug == null && shapeDrawer != null) {
            skeletonDrawerDebug = new SkeletonDrawerDebug(shapeDrawer);
            skeletonDrawerDebug.setScale(1.5f);
            skeletonDrawerDebug.setBoundingBoxes(false);
            skeletonDrawerDebug.setRegionAttachments(false);
            skeletonDrawerDebug.setDrawMeshFill(false);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {

            oldTransformMatrix.set(batch.getTransformMatrix());
            oldProjectionMatrix.set(batch.getProjectionMatrix());

            matrix.set(transformComponent.worldTransform);
            batch.setProjectionMatrix(Sandbox.getInstance().getCamera().combined);
            batch.setTransformMatrix(matrix);

            skeletonDrawerDebug.draw(spineObjectComponent.skeleton);

            batch.setTransformMatrix(oldTransformMatrix);
            batch.setProjectionMatrix(oldProjectionMatrix);
        }
    }
}
