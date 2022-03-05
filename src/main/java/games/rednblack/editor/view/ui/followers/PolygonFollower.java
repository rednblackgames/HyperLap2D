package games.rednblack.editor.view.ui.followers;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.editor.view.ui.widget.actors.polygon.PolyLine;
import games.rednblack.editor.view.ui.widget.actors.polygon.PolyVertex;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PolygonFollower extends SubFollower {

    private static final Color innerColor = new Color(200f / 255f, 200f / 255f, 200f / 255f, 0.2f);
    public static final Color outlineColor = new Color(200f / 255f, 156f / 255f, 71f / 255f, 1f);
    public static final Color overColor = new Color(255f / 255f, 94f / 255f, 0f / 255f, 1f);
    private static final Color problemColor = new Color(200f / 255f, 0f / 255f, 0f / 255f, 1f);

    private final int pixelsPerWU;
    private final OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();

    private ComponentMapper<TransformComponent> transformCM;
    private ComponentMapper<ParentNodeComponent> parentNodeCM;
    private TransformComponent transformComponent;
    private PolygonShapeComponent polygonShapeComponent;
    private DimensionsComponent dimensionsComponent;

    private ShapeDrawer shapeDrawer;

    private final Array<PolyLine> drawingInnerMesh = new Array<>();
    private final Array<PolyLine> drawingLines = new Array<>();
    private final Array<PolyVertex> drawingVertices = new Array<>();

    private PolygonTransformationListener listener;

    public int draggingAnchorId = -1;
    private int selectedAnchorId = -1;

    private IntSet intersections = new IntSet();

    private final Group innerMesh = new Group();
    private final Group outline = new Group();
    private final Group vertices = new Group();

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

    public PolygonFollower(int entity) {
        super(entity);
        setTouchable(Touchable.enabled);
        pixelsPerWU = Sandbox.getInstance().getPixelPerWU();
    }

    @Override
    public void create() {
        polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);
        transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        transformCM = (ComponentMapper<TransformComponent>) SandboxComponentRetriever.getMapper(TransformComponent.class);
        parentNodeCM = (ComponentMapper<ParentNodeComponent>) SandboxComponentRetriever.getMapper(ParentNodeComponent.class);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null)
            shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
        addActor(innerMesh);
        addActor(outline);
        addActor(vertices);
        update();
    }

    @Override
    public void update() {
        computeDrawingObjects();
        setSelectedAnchor(selectedAnchorId);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        for (Actor child : innerMesh.getChildren())
            if (!drawingInnerMesh.contains((PolyLine) child, true))
                child.remove();
        for (Actor child : outline.getChildren())
            if (!drawingLines.contains((PolyLine) child, true))
                child.remove();
        for (Actor child : vertices.getChildren())
            if (!drawingVertices.contains((PolyVertex) child, true))
                child.remove();
    }

    private void computeDrawingObjects() {
        linePool.freeAll(drawingInnerMesh);
        vertexPool.freeAll(drawingVertices);
        linePool.freeAll(drawingLines);
        drawingLines.clear();
        drawingVertices.clear();
        drawingInnerMesh.clear();

        if (polygonShapeComponent == null || polygonShapeComponent.vertices == null)
            return;

        float scale = pixelsPerWU / runtimeCamera.zoom;

        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1) * scale;
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1) * scale;

        float pX = 0, pY = 0;
        if (dimensionsComponent.polygon != null) {
            pX = -dimensionsComponent.polygon.getBoundingRectangle().x;
            pY = -dimensionsComponent.polygon.getBoundingRectangle().y;
        }

        //Compute inner mesh
        if ((intersections == null || intersections.size == 0) && !polygonShapeComponent.openEnded) {
            for (Vector2[] poly : polygonShapeComponent.polygonizedVertices) {
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
                    innerMesh.addActor(line);
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
                    innerMesh.addActor(line);
                }
            }
        }

        //compute outer mesh
        for (int i = 1; i < polygonShapeComponent.vertices.size; i++) {
            PolyLine line = linePool.obtain();
            line.setIndex(i);
            line.setPoint1(polygonShapeComponent.vertices.get(i));
            line.setPoint2(polygonShapeComponent.vertices.get(i - 1));
            line.offsetPoints(pX, pY);
            line.setColor(hasProblems(i) ? problemColor : outlineColor);
            line.setThickness(1.7f);
            line.scalePoints(scaleX, scaleY);
            line.addListener(new LineClickListener());
            drawingLines.add(line);
            outline.addActor(line);
        }

        if (!polygonShapeComponent.openEnded) {
            PolyLine line = linePool.obtain();
            line.setIndex(0);
            line.setPoint1(polygonShapeComponent.vertices.get(polygonShapeComponent.vertices.size - 1));
            line.setPoint2(polygonShapeComponent.vertices.get(0));
            line.offsetPoints(pX, pY);
            line.setColor(hasProblems(0) ? problemColor : outlineColor);
            line.setThickness(1.7f);
            line.scalePoints(scaleX, scaleY);
            line.addListener(new LineClickListener());
            drawingLines.add(line);
            outline.addActor(line);
        }

        //compute vertices position
        for (int i = 0; i < polygonShapeComponent.vertices.size; i++) {
            Vector2 point = polygonShapeComponent.vertices.get(i);
            PolyVertex vertex = vertexPool.obtain();
            vertex.setIndex(i);
            vertex.setPosition((point.x + pX) * scaleX, (point.y + pY) * scaleY, Align.center);
            vertex.addListener(new VertexClickListener());
            drawingVertices.add(vertex);
            vertices.addActor(vertex);
        }
    }

    public void setListener(PolygonTransformationListener listener) {
        this.listener = listener;
        clearListeners();
        addListener(new ClickListener() {
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                Vector2 coord = Pools.obtain(Vector2.class).set(x, y);
                transformActorCoordIntoEntity(PolygonFollower.this, coord);
                int anchorId = draggingAnchorId;

                if (anchorId >= 0) {
                    listener.anchorDragged(PolygonFollower.this, anchorId, coord.x, coord.y);
                }

                Pools.free(coord);
            }
        });
    }

    public int getEntity() {
        return entity;
    }

    public void setProblems(IntSet intersections) {
        this.intersections = intersections;
    }

    private boolean hasProblems(int line) {
        if (intersections == null) return false;
        line = line > 0 ? line - 1 : polygonShapeComponent.vertices.size - 1;
        return intersections.contains(line);
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

    private class LineClickListener extends ClickListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (super.touchDown(event, x, y, pointer, button)) {
                PolyLine line = (PolyLine) event.getListenerActor();

                Vector2 coord = Pools.obtain(Vector2.class).set(x, y);
                transformActorCoordIntoEntity(line, coord);

                listener.vertexDown(PolygonFollower.this, line.getIndex(), coord.x, coord.y);

                Pools.free(coord);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            PolyLine line = (PolyLine) event.getListenerActor();

            Vector2 coord = Pools.obtain(Vector2.class).set(x, y);
            transformActorCoordIntoEntity(line, coord);

            listener.vertexUp(PolygonFollower.this, line.getIndex(), coord.x, coord.y);

            draggingAnchorId = -1;
            Pools.free(coord);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            super.enter(event, x, y, pointer, fromActor);
            if (draggingAnchorId == -1) {
                PolyLine line = (PolyLine) event.getListenerActor();
                line.setColor(overColor);
            }
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            super.exit(event, x, y, pointer, toActor);
            if (draggingAnchorId == -1) {
                PolyLine line = (PolyLine) event.getListenerActor();
                line.setColor(outlineColor);
            }
        }
    }

    private class VertexClickListener extends ClickListener {
        public VertexClickListener() {
            super(-1);
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (super.touchDown(event, x, y, pointer, button)) {
                if(button != Input.Buttons.LEFT) return true;

                PolyVertex vertex = (PolyVertex) event.getListenerActor();
                draggingAnchorId = vertex.getIndex();

                Vector2 coord = Pools.obtain(Vector2.class).set(x, y);
                transformActorCoordIntoEntity(vertex, coord);

                setSelectedAnchor(draggingAnchorId);
                listener.anchorDown(PolygonFollower.this, draggingAnchorId, coord.x, coord.y);

                Pools.free(coord);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);

            PolyVertex vertex = (PolyVertex) event.getListenerActor();
            if(button == Input.Buttons.RIGHT) draggingAnchorId = vertex.getIndex();

            Vector2 coord = Pools.obtain(Vector2.class).set(x, y);
            transformActorCoordIntoEntity(vertex, coord);

            listener.anchorUp(PolygonFollower.this, draggingAnchorId, button,coord.x, coord.y);
            draggingAnchorId = -1;
            Pools.free(coord);
        }
    }

    private void transformActorCoordIntoEntity(Actor actor, Vector2 coord) {
        actor.localToScreenCoordinates(coord);
        Sandbox.getInstance().getViewport().unproject(coord);
        TransformMathUtils.sceneToLocalCoordinates(entity, coord, transformCM, parentNodeCM);
    }
}

