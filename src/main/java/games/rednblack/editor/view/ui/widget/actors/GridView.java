package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.stage.Sandbox;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GridView extends Actor {

    private final ShapeDrawer shapeDrawer;
    private final Label zeroLabel;

    private static final Color BASE_COLOR = new Color(0.6f, 0.6f, 0.6f, 1);

    private static final float ALPHA_AXIS = 0.3f;
    private static final float ALPHA_MAJOR = 0.15f;
    private static final float ALPHA_MINOR = 0.075f;

    private static final int SUBDIVISIONS = 2;

    private final Color tmpColor = new Color();
    private static final int separatorsCount = 20;
    private float gridMeasuringSizeInWorld;
    private final Vector2 tmpPoint = new Vector2();

    public GridView(ShapeDrawer shapeDrawer) {
        this.shapeDrawer = shapeDrawer;
        zeroLabel = new VisLabel("0,0");
        zeroLabel.setColor(new Color(1, 1, 1, 0.4f));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        OrthographicCamera uiCamera = (OrthographicCamera) Sandbox.getInstance().getUIStage().getCamera();
        batch.setProjectionMatrix(uiCamera.combined);

        float screenWidth = Sandbox.getInstance().getUIStage().getWidth();
        float screenHeight = Sandbox.getInstance().getUIStage().getHeight();

        calculateGridSize();

        drawDynamicGrid(screenWidth, screenHeight, parentAlpha);
        drawZeroLabel(batch, parentAlpha);
    }

    private void calculateGridSize() {
        float viewMeasurableWidth = Sandbox.getInstance().getViewport().getWorldWidth() * Sandbox.getInstance().getCameraZoomTarget();
        float gridMeasuringSize = viewMeasurableWidth / separatorsCount;

        if (gridMeasuringSize <= 0.5) {
            gridMeasuringSize = MathUtils.round(gridMeasuringSize * 10f) / 10f;
        } else if (gridMeasuringSize <= 10) {
            gridMeasuringSize = MathUtils.round(gridMeasuringSize);
        } else if (gridMeasuringSize > 10 && gridMeasuringSize <= 20) {
            gridMeasuringSize = MathUtils.round(gridMeasuringSize / 5) * 5;
        } else {
            gridMeasuringSize = MathUtils.round(gridMeasuringSize / 10) * 10;
        }

        gridMeasuringSizeInWorld = gridMeasuringSize;
    }

    private void drawDynamicGrid(float screenWidth, float screenHeight, float parentAlpha) {
        OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();

        float subStep = gridMeasuringSizeInWorld / SUBDIVISIONS;

        float viewportHalfWidth = (Sandbox.getInstance().getViewport().getWorldWidth() * Sandbox.getInstance().getCameraZoomTarget()) / 2f;
        float viewportHalfHeight = (Sandbox.getInstance().getViewport().getWorldHeight() * Sandbox.getInstance().getCameraZoomTarget()) / 2f;

        float worldLeft = runtimeCamera.position.x - viewportHalfWidth;
        float worldRight = runtimeCamera.position.x + viewportHalfWidth;
        float worldBottom = runtimeCamera.position.y - viewportHalfHeight;
        float worldTop = runtimeCamera.position.y + viewportHalfHeight;

        float startX = (float) Math.floor(worldLeft / subStep) * subStep;
        float startY = (float) Math.floor(worldBottom / subStep) * subStep;

        for (float x = startX; x <= worldRight + subStep; x += subStep) {
            LineType type = getLineType(x);

            tmpPoint.set(x, 0);
            Vector2 screenPos = Sandbox.getInstance().worldToScreen(tmpPoint);

            setupLineStyle(type, parentAlpha);
            float thickness = (type == LineType.AXIS) ? 2f : 1f;
            shapeDrawer.line(screenPos.x, 0, screenPos.x, screenHeight, thickness);
        }

        for (float y = startY; y <= worldTop + subStep; y += subStep) {
            LineType type = getLineType(y);

            tmpPoint.set(0, y);
            Vector2 screenPos = Sandbox.getInstance().worldToScreen(tmpPoint);

            setupLineStyle(type, parentAlpha);
            float thickness = (type == LineType.AXIS) ? 2f : 1f;
            shapeDrawer.line(0, screenPos.y, screenWidth, screenPos.y, thickness);
        }
    }

    private enum LineType {
        AXIS,
        MAJOR,
        MINOR
    }

    private LineType getLineType(float coordinate) {
        if (Math.abs(coordinate) < 0.001f) return LineType.AXIS;
        long index = Math.round(coordinate / (gridMeasuringSizeInWorld / SUBDIVISIONS));

        if (index % SUBDIVISIONS == 0) {
            return LineType.MAJOR;
        } else {
            return LineType.MINOR;
        }
    }

    private void setupLineStyle(LineType type, float parentAlpha) {
        tmpColor.set(BASE_COLOR);

        switch (type) {
            case AXIS:
                tmpColor.a = ALPHA_AXIS;
                break;
            case MAJOR:
                tmpColor.a = ALPHA_MAJOR;
                break;
            case MINOR:
                tmpColor.a = ALPHA_MINOR;
                break;
        }

        tmpColor.a *= parentAlpha;
        shapeDrawer.setColor(tmpColor);
    }

    private void drawZeroLabel(Batch batch, float parentAlpha) {
        tmpPoint.set(0, 0);
        Vector2 screenOrigin = Sandbox.getInstance().worldToScreen(tmpPoint);

        zeroLabel.setColor(1, 1, 1, 0.5f * parentAlpha);
        zeroLabel.setPosition(screenOrigin.x + 6, screenOrigin.y + 6);
        zeroLabel.draw(batch, parentAlpha);
    }
}