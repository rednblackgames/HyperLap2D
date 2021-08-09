package games.rednblack.editor.view.ui.followers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.PolygonUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.editor.view.ui.widget.actors.polygon.PolyLine;
import games.rednblack.editor.view.ui.widget.actors.polygon.PolyVertex;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PolygonFollower2 extends SubFollower {

    private static final Color innerColor = new Color(200f / 255f, 200f / 255f, 200f / 255f, 0.2f);
    private static final Color outlineColor = new Color(200f / 255f, 156f / 255f, 71f / 255f, 1f);
    private static final Color overColor = new Color(255f / 255f, 94f / 255f, 0f / 255f, 1f);

    private final int pixelsPerWU;
    private final OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();

    private TransformComponent transformComponent;
    private PolygonComponent polygonComponent;
    private DimensionsComponent dimensionsComponent;

    private ShapeDrawer shapeDrawer;

    private final Array<Vector2> originalPoints = new Array<>(true, 0, Vector2.class);
    private final Array<PolyLine> drawingInnerMesh = new Array<>();
    private final Array<PolyLine> drawingLines = new Array<>();
    private final Array<PolyVertex> drawingVertices = new Array<>();

    private PolygonTransformationListener listener;

    public int draggingAnchorId = -1;
    private int selectedAnchorId = -1;

    private int[] intersections = null;

    private final Pool<PolyLine> linePool = new Pool<>() {
        @Override
        protected PolyLine newObject() {
            return new PolyLine(shapeDrawer);
        }
    };
    private final Pool<PolyVertex> vertexPool = new Pool<>() {
        @Override
        protected PolyVertex newObject() {
            return new PolyVertex(shapeDrawer);
        }
    };

    public PolygonFollower2(int entity) {
        super(entity);
        setTouchable(Touchable.enabled);
        pixelsPerWU = Sandbox.getInstance().getPixelPerWU();
    }

    @Override
    public void create() {
        polygonComponent = SandboxComponentRetriever.get(entity, PolygonComponent.class);
        transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null)
            shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion);
        update();
    }

    @Override
    public void update() {
        calculatePoints();
        computeDrawingObjects();
        setSelectedAnchor(selectedAnchorId);
    }

    private void calculatePoints() {
        originalPoints.clear();
        originalPoints.addAll(PolygonUtils.mergeTouchingPolygonsToOne(polygonComponent.vertices));
        originalPoints.shrink();
    }

    public void updateDraw() {
        update();
    }

    private void computeDrawingObjects() {
        linePool.freeAll(drawingInnerMesh);
        vertexPool.freeAll(drawingVertices);
        linePool.freeAll(drawingLines);
        drawingLines.clear();
        drawingVertices.clear();
        drawingInnerMesh.clear();

        if (polygonComponent == null || polygonComponent.vertices == null)
            return;

        float scale = pixelsPerWU / runtimeCamera.zoom;

        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1) * scale;
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1) * scale;

        float pX = -dimensionsComponent.polygon.getBoundingRectangle().x;
        float pY = -dimensionsComponent.polygon.getBoundingRectangle().y;

        //Compute inner mesh
        for (Vector2[] poly : polygonComponent.vertices) {
            for (int i = 1; i < poly.length; i++) {
                PolyLine line = linePool.obtain();
                line.setPoint1(poly[i - 1]);
                line.setPoint2(poly[i]);
                line.offsetPoints(pX, pY);
                line.setColor(innerColor);
                line.setThickness(1.7f);
                line.scalePoints(scaleX, scaleY);
                line.setTouchable(Touchable.disabled);
                drawingInnerMesh.add(line);
                addActor(line);
            }

            if (poly.length > 0) {
                PolyLine line = linePool.obtain();
                line.setPoint1(poly[poly.length - 1]);
                line.setPoint2(poly[0]);
                line.offsetPoints(pX, pY);
                line.setColor(innerColor);
                line.setThickness(1.7f);
                line.scalePoints(scaleX, scaleY);
                line.setTouchable(Touchable.disabled);
                drawingInnerMesh.add(line);
                addActor(line);
            }
        }

        //compute outer mesh
        for (int i = 1; i < originalPoints.size; i++) {
            PolyLine line = linePool.obtain();
            line.setIndex(i);
            line.setPoint1(originalPoints.get(i));
            line.setPoint2(originalPoints.get(i - 1));
            line.offsetPoints(pX, pY);
            line.setColor(outlineColor);
            line.setThickness(1.7f);
            line.scalePoints(scaleX, scaleY);
            line.addListener(new LineClickListener());
            drawingLines.add(line);
            addActor(line);
        }

        PolyLine line = linePool.obtain();
        line.setIndex(0);
        line.setPoint1(originalPoints.get(originalPoints.size - 1));
        line.setPoint2(originalPoints.get(0));
        line.offsetPoints(pX, pY);
        line.setColor(outlineColor);
        line.setThickness(1.7f);
        line.scalePoints(scaleX, scaleY);
        line.addListener(new LineClickListener());
        drawingLines.add(line);
        addActor(line);

        //compute vertices position
        for (int i = 0; i < originalPoints.size; i++) {
            Vector2 point = originalPoints.get(i);
            PolyVertex vertex = vertexPool.obtain();
            vertex.setIndex(i);
            vertex.setPosition((point.x + pX) * scaleX, (point.y + pY) * scaleY, Align.center);
            vertex.addListener(new VertexClickListener());
            drawingVertices.add(vertex);
            addActor(vertex);
        }
    }

    public void setListener(PolygonTransformationListener listener) {
        this.listener = listener;
        clearListeners();
        addListener(new ClickListener() {
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                Vector2 coord = new Vector2(x, y);
                Sandbox.getInstance().screenToWorld(coord);
                int anchorId = draggingAnchorId;
            }
        });
    }

    public int getEntity() {
        return entity;
    }

    public Array<Vector2> getOriginalPoints() {
        return originalPoints;
    }

    public void setProblems(int[] intersections) {
        this.intersections = intersections;
    }

    public void setSelectedAnchor(int anchorId) {
        selectedAnchorId = anchorId;
        for (PolyVertex vertex : drawingVertices) {
            vertex.setSelected(vertex.getIndex() == selectedAnchorId);
        }
    }

    public int getSelectedAnchorId() {
        return selectedAnchorId;
    }

    private static class LineClickListener extends ClickListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            super.enter(event, x, y, pointer, fromActor);
            PolyLine line = (PolyLine) event.getListenerActor();
            line.setColor(overColor);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            super.exit(event, x, y, pointer, toActor);
            PolyLine line = (PolyLine) event.getListenerActor();
            line.setColor(outlineColor);
        }
    }

    private class VertexClickListener extends ClickListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (super.touchDown(event, x, y, pointer, button)) {
                if(button != Input.Buttons.LEFT) return true;

                PolyVertex vertex = (PolyVertex) event.getListenerActor();
                draggingAnchorId = vertex.getIndex();

                Vector2 coord = new Vector2(x, y);
                Sandbox.getInstance().screenToWorld(coord);

                return true;
            } else {
                return false;
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);

            PolyVertex vertex = (PolyVertex) event.getListenerActor();
            draggingAnchorId = vertex.getIndex();

            Vector2 coord = new Vector2(x, y);
            Sandbox.getInstance().screenToWorld(coord);


            draggingAnchorId = -1;
        }
    }
}

