package games.rednblack.editor.graph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import games.rednblack.editor.graph.data.*;
import games.rednblack.editor.graph.property.PropertyBox;
import games.rednblack.editor.graph.ui.preview.NavigableCanvas;
import games.rednblack.editor.renderer.data.GraphConnectionVO;
import games.rednblack.editor.renderer.data.GraphGroupVO;
import games.rednblack.editor.renderer.data.GraphNodeVO;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.view.ui.widget.H2DPopupMenu;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class GraphContainer<T extends FieldType> extends Table implements NavigableCanvas {
    private static final float CANVAS_GAP = 50f;
    private static final float GROUP_GAP = 10f;
    private static final float GROUP_LABEL_HEIGHT = 20f;
    private static final float CONNECTOR_LENGTH = 10;
    private static final float CONNECTOR_RADIUS = 5;
    private static final int LINE_WEIGHT = 4;

    private static final Color GROUP_BACKGROUND_COLOR = new Color(1f, 1f, 1f, 0.3f);
    private static final Color LINE_COLOR = new Color(0x607d8bb0);
    private static final Color VALID_CONNECTOR_COLOR = Color.WHITE;
    private static final Color INVALID_CONNECTOR_COLOR = Color.RED;

    private static final Color INVALID_LABEL_COLOR = Color.RED;
    private static final Color WARNING_LABEL_COLOR = Color.GOLDENROD;
    private static final Color VALID_LABEL_COLOR = Color.WHITE;
    private static final float NODE_GROUP_PADDING = 4f;

    private float canvasX;
    private float canvasY;
    private float canvasWidth;
    private float canvasHeight;
    private boolean navigating;

    private Group parentWindow;

    private Map<String, GraphBox<T>> graphBoxes = new HashMap<>();
    private Map<String, VisWindow> boxWindows = new HashMap<>();
    private Map<VisWindow, Vector2> windowPositions = new HashMap<>();
    private List<GraphConnection> graphConnections = new LinkedList<>();

    private Map<NodeConnector, Shape> connectionNodeMap = new HashMap<>();
    private Map<GraphConnection, Shape> connections = new HashMap<>();
    private Map<NodeGroupImpl, Rectangle> nodeGroups = new HashMap<>();

    private ShapeDrawer shapeDrawer;
    private final Color shapeDrawerColor = new Color();

    private NodeConnector drawingFromConnector;
    private GraphValidator.ValidationResult<GraphBox<T>, GraphConnection, PropertyBox<T>, T> validationResult = new GraphValidator.ValidationResult<>();

    private Set<String> selectedNodes = new HashSet<>();
    private boolean movingSelected = false;
    private Skin skin;
    private PopupMenuProducer popupMenuProducer;

    private NodeConnector tmpNodeInfo1 = new NodeConnector();
    private NodeConnector tmpNodeInfo2 = new NodeConnector();

    public GraphContainer(Skin skin, final PopupMenuProducer popupMenuProducer) {
        this.skin = skin;
        this.popupMenuProducer = popupMenuProducer;

        setClip(true);
        setTouchable(Touchable.enabled);

        setupListeners();
    }

    private void setupListeners() {
        addListener(
                new ClickListener(Input.Buttons.RIGHT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (!containedInWindow(x, y)) {
                            NodeGroupImpl nodeGroup = null;
                            for (Map.Entry<NodeGroupImpl, Rectangle> nodeGroupEntry : nodeGroups.entrySet()) {
                                Rectangle rectangle = nodeGroupEntry.getValue();
                                if (rectangle.contains(x, y) && y > rectangle.y + rectangle.height - GROUP_LABEL_HEIGHT) {
                                    // Hit the label
                                    nodeGroup = nodeGroupEntry.getKey();
                                    break;
                                }
                            }
                            if (nodeGroup != null) {
                                final NodeGroupImpl finalNodeGroup = nodeGroup;

                                H2DPopupMenu popupMenu = new H2DPopupMenu();
                                MenuItem rename = new MenuItem("Rename group");
                                rename.addListener(
                                        new ClickListener(Input.Buttons.LEFT) {
                                            @Override
                                            public void clicked(InputEvent event, float x, float y) {
                                                Dialogs.showInputDialog(getStage(), "Enter group name", "Name",
                                                        new InputValidator() {
                                                            @Override
                                                            public boolean validateInput(String input) {
                                                                return !input.trim().isEmpty();
                                                            }
                                                        },
                                                        new InputDialogListener() {
                                                            @Override
                                                            public void finished(String input) {
                                                                finalNodeGroup.setName(input.trim());
                                                                fire(new GraphChangedEvent(false, false));
                                                            }

                                                            @Override
                                                            public void canceled() {

                                                            }
                                                        }).setText(finalNodeGroup.getName(), true);
                                            }
                                        });
                                popupMenu.addItem(rename);

                                MenuItem remove = new MenuItem("Remove group");
                                remove.addListener(
                                        new ClickListener(Input.Buttons.LEFT) {
                                            @Override
                                            public void clicked(InputEvent event, float x, float y) {
                                                nodeGroups.remove(finalNodeGroup);
                                                fire(new GraphChangedEvent(false, false));
                                            }
                                        });
                                popupMenu.addItem(remove);

                                showPopupMenu(popupMenu);
                            } else {
                                H2DPopupMenu popupMenu = popupMenuProducer.createPopupMenu(x, y);
                                showPopupMenu(popupMenu);
                            }
                        }
                    }
                });

        addListener(
                new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        processLeftClick(x, y);
                        return super.touchDown(event, x, y, pointer, button);
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        super.touchUp(event, x, y, pointer, button);
                        processLeftClick(x, y);
                    }
                });

        DragListener dragListener = new DragListener() {
            private float canvasXStart;
            private float canvasYStart;
            private NodeGroup dragGroup;
            private float movedByX = 0;
            private float movedByY = 0;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (event.getTarget() == GraphContainer.this) {
                    canvasXStart = canvasX;
                    canvasYStart = canvasY;
                    movedByX = 0;
                    movedByY = 0;
                    dragGroup = null;
                    for (Map.Entry<NodeGroupImpl, Rectangle> nodeGroupEntry : nodeGroups.entrySet()) {
                        Rectangle rectangle = nodeGroupEntry.getValue();
                        if (rectangle.contains(x, y) && y > rectangle.y + rectangle.height - GROUP_LABEL_HEIGHT) {
                            // Hit the label
                            dragGroup = nodeGroupEntry.getKey();
                            break;
                        }
                    }
                }
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (event.getTarget() == GraphContainer.this) {
                    if (dragGroup != null) {
                        movingSelected = true;
                        float moveByX = x - getDragStartX() - movedByX;
                        float moveByY = y - getDragStartY() - movedByY;
                        for (String nodeId : dragGroup.getNodeIds()) {
                            getBoxWindow(nodeId).moveBy(moveByX, moveByY);
                        }
                        movedByX += moveByX;
                        movedByY += moveByY;
                        windowsMoved();
                        updateNodeGroups();
                        movingSelected = false;
                    } else {
                        navigateTo(canvasXStart + getDragStartX() - x, canvasYStart + getDragStartY() - y);
                    }
                }
            }
        };
        dragListener.setTapSquareSize(0f);
        dragListener.setButton(Input.Buttons.MIDDLE);
        addListener(dragListener);
    }

    public void centerCanvas() {
        navigateTo((canvasWidth - getWidth()) / 2f, (canvasHeight - getHeight()) / 2f);
    }

    @Override
    public void getCanvasPosition(Vector2 result) {
        result.set(canvasX, canvasY);
    }

    @Override
    public void getCanvasSize(Vector2 result) {
        result.set(canvasWidth, canvasHeight);
    }

    @Override
    public void getVisibleSize(Vector2 result) {
        result.set(getWidth(), getHeight());
    }

    @Override
    public void navigateTo(float x, float y) {
        if (drawingFromConnector != null)
            return;
        x = MathUtils.round(x);
        y = MathUtils.round(y);

        navigating = true;
        float difX = x - canvasX;
        float difY = y - canvasY;
        for (Actor element : getElements()) {
            element.moveBy(-difX, -difY);
        }
        canvasX = x;
        canvasY = y;
        navigating = false;

        windowsMoved();
    }

    private void showPopupMenu(H2DPopupMenu popupMenu) {
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        popupMenu.showMenu(uiStage, sandbox.getInputX(), uiStage.getHeight() - sandbox.getInputY());
    }

    @Override
    public Iterable<? extends Actor> getElements() {
        return boxWindows.values();
    }

    private void updateCanvas(boolean adjustPosition) {
        if (!navigating) {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;

            Collection<VisWindow> children = boxWindows.values();
            if (children.size() == 0) {
                minX = 0;
                minY = 0;
                maxX = 0;
                maxY = 0;
            } else {
                for (Actor child : children) {
                    float childX = child.getX();
                    float childY = child.getY();
                    float childWidth = child.getWidth();
                    float childHeight = child.getHeight();
                    minX = Math.min(minX, childX);
                    minY = Math.min(minY, childY);
                    maxX = Math.max(maxX, childX + childWidth);
                    maxY = Math.max(maxY, childY + childHeight);
                }
            }

            minX -= CANVAS_GAP;
            minY -= CANVAS_GAP;
            maxX += CANVAS_GAP;
            maxY += CANVAS_GAP;

            canvasWidth = maxX - minX;
            canvasHeight = maxY - minY;

            if (adjustPosition) {
                canvasX = -minX;
                canvasY = -minY;
            }
        }
    }

    public void adjustCanvas() {
        updateCanvas(true);
    }

    public void setValidationResult(GraphValidator.ValidationResult<GraphBox<T>, GraphConnection, PropertyBox<T>, T> validationResult) {
        this.validationResult = validationResult;
        for (GraphBox<T> value : graphBoxes.values()) {
            VisWindow window = boxWindows.get(value.getId());
            if (validationResult.getErrorNodes().contains(value)) {
                window.getTitleLabel().setColor(INVALID_LABEL_COLOR);
            } else if (validationResult.getWarningNodes().contains(value)) {
                window.getTitleLabel().setColor(WARNING_LABEL_COLOR);
            } else {
                window.getTitleLabel().setColor(VALID_LABEL_COLOR);
            }
        }
    }

    public void setParentWindow(Group parentWindow) {
        this.parentWindow = parentWindow;
    }

    public GraphValidator.ValidationResult<GraphBox<T>, GraphConnection, PropertyBox<T>, T> getValidationResult() {
        return validationResult;
    }

    private void processLeftClick(float x, float y) {
        if (containedInWindow(x, y)) {
            drawingFromConnector = null;
            return;
        }

        for (Map.Entry<NodeConnector, Shape> nodeEntry : connectionNodeMap.entrySet()) {
            if (nodeEntry.getValue().contains(x, y)) {
                processNodeClick(nodeEntry.getKey());
                return;
            }
        }

        /*for (Map.Entry<GraphConnection, Shape> connectionEntry : connections.entrySet()) {
            if (connectionEntry.getValue().contains(x, y)) {
                GraphConnection connection = connectionEntry.getKey();
                removeConnection(connection);
                return;
            }
        }*/

        drawingFromConnector = null;
    }

    private boolean containedInWindow(float x, float y) {
        for (Window window : boxWindows.values()) {
            float x1 = window.getX();
            float y1 = window.getY();
            float width = window.getWidth();
            float height = window.getHeight();
            // If window contains it - return
            if (x >= x1 && x < x1 + width
                    && y >= y1 && y < y1 + height)
                return true;
        }
        return false;
    }

    private void removeConnection(GraphConnection connection) {
        graphConnections.remove(connection);
        fire(new GraphChangedEvent(true, false));
        invalidate();
    }

    private void processNodeClick(NodeConnector clickedNodeConnector) {
        GraphBox<T> clickedNode = getGraphBoxById(clickedNodeConnector.getNodeId());
        if (drawingFromConnector != null) {
            if (!drawingFromConnector.equals(clickedNodeConnector)) {
                GraphBox<T> drawingFromNode = getGraphBoxById(drawingFromConnector.getNodeId());

                boolean drawingFromIsInput = drawingFromNode.isInputField(drawingFromConnector.getFieldId());
                if (drawingFromIsInput == clickedNode.isInputField(clickedNodeConnector.getFieldId())) {
                    drawingFromConnector = null;
                } else {
                    NodeConnector connectorFrom = drawingFromIsInput ? clickedNodeConnector : drawingFromConnector;
                    NodeConnector connectorTo = drawingFromIsInput ? drawingFromConnector : clickedNodeConnector;

                    GraphNodeOutput<T> output = getGraphBoxById(connectorFrom.getNodeId()).getConfiguration().getNodeOutputs().get(connectorFrom.getFieldId());
                    GraphNodeInput<T> input = getGraphBoxById(connectorTo.getNodeId()).getConfiguration().getNodeInputs().get(connectorTo.getFieldId());

                    if (!connectorsMatch(input, output)) {
                        // Either input-input, output-output, or different property type
                        drawingFromConnector = null;
                    } else {
                        // Remove conflicting connections if needed
                        for (GraphConnection oldConnection : findNodeConnections(connectorTo)) {
                            removeConnection(oldConnection);
                        }
                        if (!output.supportsMultiple()) {
                            for (GraphConnection oldConnection : findNodeConnections(connectorFrom)) {
                                removeConnection(oldConnection);
                            }
                        }
                        addGraphConnection(connectorFrom.getNodeId(), connectorFrom.getFieldId(),
                                connectorTo.getNodeId(), connectorTo.getFieldId());
                        drawingFromConnector = null;
                    }
                }
            } else {
                // Same node, that started at
                drawingFromConnector = null;
            }
        } else {
            if (clickedNode.isInputField(clickedNodeConnector.getFieldId())
                    || !clickedNode.getConfiguration().getNodeOutputs().get(clickedNodeConnector.getFieldId()).supportsMultiple()) {
                List<GraphConnection> nodeConnections = findNodeConnections(clickedNodeConnector);
                if (nodeConnections.size() > 0) {
                    GraphConnection oldConnection = nodeConnections.get(0);
                    removeConnection(oldConnection);
                    NodeConnector oldNode = getNodeInfo(oldConnection.getNodeFrom(), oldConnection.getFieldFrom());
                    if (oldNode.equals(clickedNodeConnector))
                        drawingFromConnector = getNodeInfo(oldConnection.getNodeTo(), oldConnection.getFieldTo());
                    else
                        drawingFromConnector = oldNode;
                } else {
                    drawingFromConnector = clickedNodeConnector;
                }
            } else {
                drawingFromConnector = clickedNodeConnector;
            }
        }
    }

    private boolean connectorsMatch(GraphNodeInput<T> input, GraphNodeOutput<T> output) {
        Collection<? extends T> producablePropertyTypes = output.getProducableFieldTypes();
        for (T acceptedPropertyType : input.getAcceptedPropertyTypes()) {
            if (producablePropertyTypes.contains(acceptedPropertyType))
                return true;
        }

        return false;
    }

    private List<GraphConnection> findNodeConnections(NodeConnector nodeConnector) {
        String nodeId = nodeConnector.getNodeId();
        String fieldId = nodeConnector.getFieldId();

        List<GraphConnection> result = new LinkedList<>();
        for (GraphConnection graphConnection : graphConnections) {
            if ((graphConnection.getNodeFrom().equals(nodeId) && graphConnection.getFieldFrom().equals(fieldId))
                    || (graphConnection.getNodeTo().equals(nodeId) && graphConnection.getFieldTo().equals(fieldId)))
                result.add(graphConnection);
        }
        return result;
    }

    public void addGraphBox(GraphBox<T> graphBox, String windowTitle, boolean closeable, float x, float y) {
        graphBoxes.put(graphBox.getId(), graphBox);
        GraphBoxWindow window = new GraphBoxWindow(graphBox, windowTitle);
        window.setKeepWithinStage(false);
        if (closeable) {
            window.addCloseButton();
        }
        graphBox.addToWindow(window);
        windowPositions.put(window, new Vector2(x, y));
        window.setPosition(x, y);
        addActor(window);
        window.setSize(Math.max(150, window.getPrefWidth() + 20), window.getPrefHeight() + 20);
        window.setOrigin(Align.center);
        window.addAction(Actions.sequence(
                Actions.scaleTo(0, 0),
                Actions.scaleTo(1, 1, .35f, Interpolation.swingOut)
        ));
        boxWindows.put(graphBox.getId(), window);
        fire(new GraphChangedEvent(true, false));

        updateSelectedVisuals();
    }

    public void addNodeGroup(String name, Set<String> nodeIds) {
        nodeGroups.put(new NodeGroupImpl(name, nodeIds), new Rectangle());
        updateNodeGroups();
        fire(new GraphChangedEvent(false, false));
    }

    private void graphWindowMoved(VisWindow visWindow, String nodeId) {
        if (!movingSelected && !navigating) {
            movingSelected = true;
            Vector2 oldPosition = windowPositions.get(visWindow);
            float movedX = visWindow.getX() - oldPosition.x;
            float movedY = visWindow.getY() - oldPosition.y;
            for (String selectedNode : selectedNodes) {
                if (!selectedNode.equals(nodeId)) {
                    boxWindows.get(selectedNode).moveBy(movedX, movedY);
                }
            }

            windowsMoved();
            movingSelected = false;
        }
        windowPositions.get(visWindow).set(visWindow.getX(), visWindow.getY());
    }

    private void windowsMoved() {
        recreateClickableShapes();
        updateNodeGroups();
        updateCanvas(true);
        fire(new GraphChangedEvent(false, false));
    }

    private void removeFromSelection(String nodeId) {
        selectedNodes.remove(nodeId);
        updateSelectedVisuals();
    }

    private void addToSelection(String nodeId) {
        selectedNodes.add(nodeId);
        updateSelectedVisuals();
    }

    private void setSelection(String nodeId) {
        selectedNodes.clear();
        selectedNodes.add(nodeId);
        updateSelectedVisuals();
    }

    private void updateSelectedVisuals() {
        Window.WindowStyle notSelectedStyle = VisUI.getSkin().get("node", Window.WindowStyle.class);
        Window.WindowStyle selectedStyle = VisUI.getSkin().get("node-selected", Window.WindowStyle.class);

        for (Map.Entry<String, VisWindow> windowEntry : boxWindows.entrySet()) {
            Window.WindowStyle newStyle = selectedNodes.contains(windowEntry.getKey()) ? selectedStyle : notSelectedStyle;
            windowEntry.getValue().setStyle(newStyle);
        }
    }

    private void removeGraphBox(GraphBox<T> graphBox) {
        Iterator<GraphConnection> graphConnectionIterator = graphConnections.iterator();
        String nodeId = graphBox.getId();
        while (graphConnectionIterator.hasNext()) {
            GraphConnection graphConnectionImpl = graphConnectionIterator.next();
            if (graphConnectionImpl.getNodeFrom().equals(nodeId)
                    || graphConnectionImpl.getNodeTo().equals(nodeId))
                graphConnectionIterator.remove();
        }

        boxWindows.remove(nodeId);
        graphBoxes.remove(nodeId);
        selectedNodes.remove(nodeId);
        for (NodeGroupImpl nodeGroupImpl : nodeGroups.keySet()) {
            if (nodeGroupImpl.getNodeIds().remove(nodeId)) {
                if (nodeGroupImpl.getNodeIds().size() == 0) {
                    nodeGroups.remove(nodeGroupImpl);
                }
                break;
            }
        }

        graphBox.dispose();

        fire(new GraphChangedEvent(true, false));
    }

    public void addGraphConnection(String fromNode, String fromField, String toNode, String toField) {
        NodeConnector nodeFrom = getNodeInfo(fromNode, fromField);
        NodeConnector nodeTo = getNodeInfo(toNode, toField);
        if (nodeFrom == null || nodeTo == null)
            throw new IllegalArgumentException("Can't find node: (" + fromNode + ";" + fromField + ") -> (" + toNode + ";" + toField + ")");
        graphConnections.add(new GraphConnectionImpl(fromNode, fromField, toNode, toField));
        fire(new GraphChangedEvent(true, false));
        invalidate();
    }

    @Override
    public void layout() {
        super.layout();
        recreateClickableShapes();
        updateNodeGroups();
        updateCanvas(false);
    }

    private void updateNodeGroups() {
        for (Map.Entry<NodeGroupImpl, Rectangle> nodeGroupEntry : nodeGroups.entrySet()) {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;

            NodeGroupImpl nodeGroupImpl = nodeGroupEntry.getKey();
            for (String nodeId : nodeGroupImpl.getNodeIds()) {
                VisWindow window = boxWindows.get(nodeId);
                float windowX = window.getX();
                float windowY = window.getY();
                float windowWidth = window.getWidth();
                float windowHeight = window.getHeight();
                minX = Math.min(minX, windowX);
                minY = Math.min(minY, windowY);
                maxX = Math.max(maxX, windowX + windowWidth);
                maxY = Math.max(maxY, windowY + windowHeight);
            }

            minX -= GROUP_GAP;
            minY -= GROUP_GAP;
            maxX += GROUP_GAP;
            maxY += GROUP_GAP + GROUP_LABEL_HEIGHT;
            nodeGroupEntry.getValue().set(minX, minY, maxX - minX, maxY - minY);
        }
    }

    private void recreateClickableShapes() {
        connectionNodeMap.clear();
        connections.clear();

        Vector2 from = new Vector2();
        for (Map.Entry<String, VisWindow> windowEntry : boxWindows.entrySet()) {
            String nodeId = windowEntry.getKey();
            Window window = windowEntry.getValue();
            GraphBox<T> graphBox = graphBoxes.get(nodeId);
            float windowX = window.getX();
            float windowY = window.getY() + 14;
            for (GraphBoxInputConnector<T> connector : graphBox.getInputs().values()) {
                switch (connector.getSide()) {
                    case Left:
                        from.set(windowX - CONNECTOR_LENGTH + 7, windowY + connector.getOffset());
                        break;
                    case Top:
                        from.set(windowX + connector.getOffset(), windowY + window.getHeight() + CONNECTOR_LENGTH);
                        break;
                }
                Rectangle2D rectangle = new Rectangle2D.Float(
                        from.x - CONNECTOR_RADIUS, from.y - CONNECTOR_RADIUS,
                        CONNECTOR_RADIUS * 2, CONNECTOR_RADIUS * 2);

                connectionNodeMap.put(new NodeConnector(nodeId, connector.getFieldId()), rectangle);
            }
            for (GraphBoxOutputConnector<T> connector : graphBox.getOutputs().values()) {
                switch (connector.getSide()) {
                    case Right:
                        from.set(windowX + window.getWidth() + CONNECTOR_LENGTH - 7, windowY + connector.getOffset());
                        break;
                    case Bottom:
                        from.set(windowX + connector.getOffset(), windowY - CONNECTOR_LENGTH);
                        break;
                }
                Rectangle2D rectangle = new Rectangle2D.Float(
                        from.x - CONNECTOR_RADIUS, from.y - CONNECTOR_RADIUS,
                        CONNECTOR_RADIUS * 2, CONNECTOR_RADIUS * 2);

                connectionNodeMap.put(new NodeConnector(nodeId, connector.getFieldId()), rectangle);
            }
        }

        //BasicStroke basicStroke = new BasicStroke(7);
        Vector2 to = new Vector2();
        for (GraphConnection graphConnection : graphConnections) {
            NodeConnector fromNode = getNodeInfo(graphConnection.getNodeFrom(), graphConnection.getFieldFrom());
            Window fromWindow = boxWindows.get(fromNode.getNodeId());
            GraphBoxOutputConnector<T> output = getGraphBoxById(fromNode.getNodeId()).getOutputs().get(fromNode.getFieldId());
            calculateConnection(from, fromWindow, output);
            NodeConnector toNode = getNodeInfo(graphConnection.getNodeTo(), graphConnection.getFieldTo());
            Window toWindow = boxWindows.get(toNode.getNodeId());
            GraphBoxInputConnector<T> input = getGraphBoxById(toNode.getNodeId()).getInputs().get(toNode.getFieldId());
            calculateConnection(to, toWindow, input);

            //Shape shape = basicStroke.createStrokedShape(new Line2D.Float(from.x, from.y, to.x, to.y));

            //connections.put(graphConnection, shape);
        }
    }

    private NodeConnector getNodeInfo(String nodeId, String fieldId) {
        GraphBox<T> graphBox = graphBoxes.get(nodeId);
        if (graphBox.getInputs().get(fieldId) != null || graphBox.getOutputs().get(fieldId) != null)
            return new NodeConnector(nodeId, fieldId);
        return null;
    }

    private NodeConnector getNodeInfo(String nodeId, String fieldId, NodeConnector nodeConnector) {
        GraphBox<T> graphBox = graphBoxes.get(nodeId);
        if (graphBox.getInputs().get(fieldId) != null || graphBox.getOutputs().get(fieldId) != null) {
            nodeConnector.setFieldId(fieldId);
            nodeConnector.setNodeId(nodeId);
            return nodeConnector;
        };
        return null;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeDrawer == null) {
            shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
        }
        validate();
        drawShapeGroups(batch, parentAlpha);
        drawShapeConnections(parentAlpha);
        super.draw(batch, parentAlpha);
    }

    private void drawShapeGroups(Batch batch, float parentAlpha) {
        if (!nodeGroups.isEmpty()) {
            float x = getX();
            float y = getY();

            shapeDrawerColor.set(GROUP_BACKGROUND_COLOR);
            shapeDrawerColor.a *= parentAlpha;
            shapeDrawer.setColor(shapeDrawerColor);

            for (Map.Entry<NodeGroupImpl, Rectangle> nodeGroupEntry : nodeGroups.entrySet()) {
                Rectangle rectangle = nodeGroupEntry.getValue();
                shapeDrawer.filledRectangle(x + rectangle.x, y + rectangle.y, rectangle.width, rectangle.height);
            }

            BitmapFont font = skin.getFont("default-font");
            for (Map.Entry<NodeGroupImpl, Rectangle> nodeGroupEntry : nodeGroups.entrySet()) {
                NodeGroupImpl nodeGroupImpl = nodeGroupEntry.getKey();
                Rectangle rectangle = nodeGroupEntry.getValue();
                String name = nodeGroupImpl.getName();
                font.getColor().a *= parentAlpha;
                font.draw(batch, name, x + rectangle.x + NODE_GROUP_PADDING, y + rectangle.y + rectangle.height - NODE_GROUP_PADDING,
                        0, name.length(), rectangle.width - NODE_GROUP_PADDING * 2, Align.center, false, "...");
            }
        }
    }

    private void drawShapeConnections(float parentAlpha) {
        float x = getX();
        float y = getY();

        Vector2 from = PolygonUtils.vector2Pool.obtain();
        Vector2 to = PolygonUtils.vector2Pool.obtain();

        //Connections
        for (GraphConnection graphConnection : graphConnections) {
            NodeConnector fromNode = getNodeInfo(graphConnection.getNodeFrom(), graphConnection.getFieldFrom(), tmpNodeInfo1);
            Window fromWindow = boxWindows.get(fromNode.getNodeId());
            GraphBoxOutputConnector<T> output = getGraphBoxById(fromNode.getNodeId()).getOutputs().get(fromNode.getFieldId());
            calculateConnection(from, fromWindow, output);
            NodeConnector toNode = getNodeInfo(graphConnection.getNodeTo(), graphConnection.getFieldTo(), tmpNodeInfo2);
            Window toWindow = boxWindows.get(toNode.getNodeId());
            GraphBoxInputConnector<T> input = getGraphBoxById(toNode.getNodeId()).getInputs().get(toNode.getFieldId());
            calculateConnection(to, toWindow, input);

            boolean error = validationResult.getErrorConnections().contains(graphConnection);

            from.add(x, y);
            to.add(x, y);

            float xDiff = Math.min(150, Math.abs(from.x - to.x));

            shapeDrawerColor.set(error ? INVALID_CONNECTOR_COLOR : LINE_COLOR);
            shapeDrawerColor.a *= parentAlpha;
            shapeDrawer.setColor(shapeDrawerColor);

            Vector2 center1 = PolygonUtils.vector2Pool.obtain();
            center1.set(from.x + xDiff, from.y);
            Vector2 center2 = PolygonUtils.vector2Pool.obtain();
            center2.set(to.x - xDiff, to.y);

            Array<Vector2> path = PolygonUtils.getCurvedLine(from, to, center1, center2, 50);
            shapeDrawer.path(path, LINE_WEIGHT, JoinType.NONE,true);

            PolygonUtils.vector2Pool.freeAll(path);
            PolygonUtils.vector2Pool.free(center1);
            PolygonUtils.vector2Pool.free(center2);
        }

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        if (drawingFromConnector != null) {
            GraphBox<T> drawingFromNode = getGraphBoxById(drawingFromConnector.getNodeId());
            Window fromWindow = getBoxWindow(drawingFromConnector.getNodeId());
            if (drawingFromNode.isInputField(drawingFromConnector.getFieldId())) {
                GraphBoxInputConnector<T> input = drawingFromNode.getInputs().get(drawingFromConnector.getFieldId());
                calculateConnection(from, fromWindow, input);
                shapeDrawerColor.set(LINE_COLOR);
                shapeDrawerColor.a *= parentAlpha;
                shapeDrawer.setColor(shapeDrawerColor);

                shapeDrawer.line(x + from.x, y + from.y, sandbox.getInputX() - parentWindow.getX(), uiStage.getHeight() - sandbox.getInputY() - parentWindow.getY(), LINE_WEIGHT);
            } else {
                GraphBoxOutputConnector<T> output = drawingFromNode.getOutputs().get(drawingFromConnector.getFieldId());
                calculateConnection(from, fromWindow, output);
                shapeDrawerColor.set(LINE_COLOR);
                shapeDrawerColor.a *= parentAlpha;
                shapeDrawer.setColor(shapeDrawerColor);
                shapeDrawer.line(x + from.x, y + from.y, sandbox.getInputX() - parentWindow.getX(), uiStage.getHeight() - sandbox.getInputY() - parentWindow.getY(), LINE_WEIGHT);
            }
        }

        //Pins
        for (Map.Entry<String, VisWindow> windowEntry : boxWindows.entrySet()) {
            String nodeId = windowEntry.getKey();
            Window window = windowEntry.getValue();
            GraphBox<T> graphBox = graphBoxes.get(nodeId);
            for (GraphNodeInput<T> connector : graphBox.getConfiguration().getNodeInputs().values()) {
                if (!connector.isRequired()) {
                    String fieldId = connector.getFieldId();
                    calculateConnector(from, to, window, graphBox.getInputs().get(fieldId));
                    from.add(x, y);
                    to.add(x, y);

                    shapeDrawerColor.set(VALID_CONNECTOR_COLOR);
                    shapeDrawerColor.a *= parentAlpha;
                    shapeDrawer.setColor(shapeDrawerColor);

                    shapeDrawer.line(from, to);
                    shapeDrawer.circle(from.x, from.y, CONNECTOR_RADIUS);
                }
            }

            for (GraphBoxOutputConnector<T> connector : graphBox.getOutputs().values()) {
                calculateConnector(from, to, window, connector);
                from.add(x, y);
                to.add(x, y);

                shapeDrawerColor.set(VALID_CONNECTOR_COLOR);
                shapeDrawerColor.a *= parentAlpha;
                shapeDrawer.setColor(shapeDrawerColor);

                shapeDrawer.line(from, to);
                shapeDrawer.circle(from.x, from.y, CONNECTOR_RADIUS);
            }
        }

        for (Map.Entry<String, VisWindow> windowEntry : boxWindows.entrySet()) {
            String nodeId = windowEntry.getKey();
            Window window = windowEntry.getValue();
            GraphBox<T> graphBox = graphBoxes.get(nodeId);
            for (GraphNodeInput<T> connector : graphBox.getConfiguration().getNodeInputs().values()) {
                if (connector.isRequired()) {
                    String fieldId = connector.getFieldId();
                    calculateConnector(from, to, window, graphBox.getInputs().get(fieldId));
                    from.add(x, y);
                    to.add(x, y);

                    boolean isErrorous = false;
                    for (NodeConnector errorConnector : validationResult.getErrorConnectors()) {
                        if (errorConnector.getNodeId().equals(nodeId) && errorConnector.getFieldId().equals(connector.getFieldId())) {
                            isErrorous = true;
                            break;
                        }
                    }

                    shapeDrawerColor.set(isErrorous ? INVALID_CONNECTOR_COLOR : VALID_CONNECTOR_COLOR);
                    shapeDrawerColor.a *= parentAlpha;
                    shapeDrawer.setColor(shapeDrawerColor);
                    shapeDrawer.line(from, to);
                    shapeDrawer.filledCircle(from.x, from.y, CONNECTOR_RADIUS);
                }
            }
        }

        PolygonUtils.vector2Pool.free(from);
        PolygonUtils.vector2Pool.free(to);
    }

    private void calculateConnector(Vector2 from, Vector2 to, Window window, GraphBoxOutputConnector<T> connector) {
        float windowX = window.getX() - 7;
        float windowY = window.getY() + 14;
        switch (connector.getSide()) {
            case Right:
                from.set(windowX + window.getWidth() + CONNECTOR_LENGTH, windowY + connector.getOffset());
                to.set(windowX + window.getWidth(), windowY + connector.getOffset());
                break;
            case Bottom:
                from.set(windowX + connector.getOffset(), windowY - CONNECTOR_LENGTH);
                to.set(windowX + connector.getOffset(), windowY);
                break;
        }
    }

    private void calculateConnector(Vector2 from, Vector2 to, Window window, GraphBoxInputConnector<T> connector) {
        float windowX = window.getX() + 7;
        float windowY = window.getY() + 14;
        switch (connector.getSide()) {
            case Left:
                from.set(windowX - CONNECTOR_LENGTH, windowY + connector.getOffset());
                to.set(windowX, windowY + connector.getOffset());
                break;
            case Top:
                from.set(windowX + connector.getOffset(), windowY + window.getHeight() + CONNECTOR_LENGTH);
                to.set(windowX + connector.getOffset(), windowY + window.getHeight());
                break;
        }
    }

    public GraphBox<T> getGraphBoxById(String id) {
        return graphBoxes.get(id);
    }

    public List<GraphConnection> getIncomingConnections(GraphBox<T> graphBox) {
        List<GraphConnection> result = new LinkedList<>();
        for (GraphConnection graphConnection : graphConnections) {
            if (graphConnection.getNodeTo().equals(graphBox.getId()))
                result.add(graphConnection);
        }
        return result;
    }

    public Iterable<GraphBox<T>> getGraphBoxes() {
        return graphBoxes.values();
    }

    public Iterable<? extends GraphConnection> getConnections() {
        return graphConnections;
    }

    public Iterable<? extends NodeGroup> getNodeGroups() {
        return nodeGroups.keySet();
    }

    private void calculateConnection(Vector2 position, Window window, GraphBoxInputConnector<T> connector) {
        float windowX = window.getX() + 7;
        float windowY = window.getY() + 14;
        switch (connector.getSide()) {
            case Left:
                position.set(windowX - CONNECTOR_LENGTH, windowY + connector.getOffset());
                break;
            case Top:
                position.set(windowX + connector.getOffset(), windowY + window.getHeight() + CONNECTOR_LENGTH);
                break;
        }
    }

    private void calculateConnection(Vector2 position, Window window, GraphBoxOutputConnector<T> connector) {
        float windowX = window.getX() - 7;
        float windowY = window.getY() + 14;
        switch (connector.getSide()) {
            case Right:
                position.set(windowX + window.getWidth() + CONNECTOR_LENGTH, windowY + connector.getOffset());
                break;
            case Bottom:
                position.set(windowX + connector.getOffset(), windowY - CONNECTOR_LENGTH);
                break;
        }
    }

    public void dispose() {
        for (GraphBox<T> graphBox : graphBoxes.values()) {
            graphBox.dispose();
        }
    }

    public Window getBoxWindow(String nodeId) {
        return boxWindows.get(nodeId);
    }

    public void createNodeGroup() {
        if (selectedNodes.size() > 0) {
            for (String selectedNode : selectedNodes) {
                if (groupsContain(selectedNode))
                    return;
            }

            Dialogs.showInputDialog(getStage(), "Enter group name", "Name",
                    new InputValidator() {
                        @Override
                        public boolean validateInput(String input) {
                            return !input.trim().isEmpty();
                        }
                    },
                    new InputDialogListener() {
                        @Override
                        public void finished(String input) {
                            addNodeGroup(input.trim(), new HashSet<String>(selectedNodes));
                        }

                        @Override
                        public void canceled() {

                        }
                    });
        }
    }

    private boolean groupsContain(String selectedNode) {
        for (NodeGroupImpl nodeGroupImpl : nodeGroups.keySet()) {
            if (nodeGroupImpl.getNodeIds().contains(selectedNode))
                return true;
        }
        return false;
    }

    private static class NodeGroupImpl implements NodeGroup {
        private String name;
        private Set<String> nodes;

        public NodeGroupImpl(String name, Set<String> nodes) {
            this.name = name;
            this.nodes = nodes;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<String> getNodeIds() {
            return nodes;
        }
    }

    public class GraphBoxWindow extends VisWindow {
        GraphBox<T> graphBox;
        private boolean removeActionRunning;

        public GraphBoxWindow(GraphBox<T> graphBox, String windowTitle) {
            super(windowTitle, false);
            this.graphBox = graphBox;
        }

        @Override
        protected void positionChanged() {
            graphWindowMoved(this, graphBox.getId());
        }

        @Override
        protected void close() {
            removeGraphBox(graphBox);
            windowPositions.remove(this);

            if (removeActionRunning) return;
            removeActionRunning = true;
            final Touchable previousTouchable = getTouchable();
            setTouchable(Touchable.disabled);
            Stage stage = getStage();
            if (stage != null && stage.getKeyboardFocus() != null && stage.getKeyboardFocus().isDescendantOf(this)) {
                FocusManager.resetFocus(stage);
            }
            addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.3f, Interpolation.swingIn), new Action() {
                @Override
                public boolean act (float delta) {
                    remove();
                    setTouchable(previousTouchable);
                    setScale(1);
                    removeActionRunning = false;
                    return true;
                }
            }));
        }

        @Override
        public void toFront() {
            super.toFront();
            String nodeId = graphBox.getId();
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                if (selectedNodes.contains(nodeId))
                    removeFromSelection(nodeId);
                else
                    addToSelection(nodeId);
            } else {
                setSelection(nodeId);
            }
        }

        public void connectorRemoved(String nodeId, String fieldId) {
            List<GraphConnection> toRemove = new ArrayList<>();
            for (GraphConnection graphConnection : graphConnections) {
                if ((graphConnection.getNodeFrom().equals(nodeId) && graphConnection.getFieldFrom().equals(fieldId))
                        || (graphConnection.getNodeTo().equals(nodeId) && graphConnection.getFieldTo().equals(fieldId))) {
                    toRemove.add(graphConnection);
                }
            }

            for (GraphConnection connection : toRemove) {
                removeConnection(connection);
            }
        }

        @Override
        public void addCloseButton() {
            Label titleLabel = getTitleLabel();
            Table titleTable = getTitleTable();

            VisImageButton closeButton = new VisImageButton("close-node-window");
            titleTable.add(closeButton).padRight(-getPadRight() + 1.7f);
            closeButton.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    close();
                }
            });
            closeButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    event.cancel();
                    return true;
                }
            });

            if (titleLabel.getLabelAlign() == Align.center && titleTable.getChildren().size == 2)
                titleTable.getCell(titleLabel).padLeft(closeButton.getWidth() * 2);
            titleTable.padTop(5);
        }
    }

    public GraphVO serializeGraph() {
        GraphVO graph = new GraphVO();
        graph.version = 1;

        Vector2 tmp = new Vector2();
        getCanvasPosition(tmp);
        for (GraphBox<T> graphBox : getGraphBoxes()) {
            Window window = getBoxWindow(graphBox.getId());
            GraphNodeVO object = new GraphNodeVO();
            object.id = graphBox.getId();
            object.type = graphBox.getType();
            object.x = tmp.x + window.getX();
            object.y = tmp.y + window.getY();

            HashMap<String, String> data = graphBox.getData();
            if (data != null)
                object.data = data;

            graph.nodes.add(object);
        }

        for (GraphConnection connection : getConnections()) {
            GraphConnectionVO conn = new GraphConnectionVO();
            conn.fromNode = connection.getNodeFrom();
            conn.fromField = connection.getFieldFrom();
            conn.toNode = connection.getNodeTo();
            conn.toField = connection.getFieldTo();
            graph.connections.add(conn);
        }

        for (NodeGroup nodeGroup : getNodeGroups()) {
            GraphGroupVO group = new GraphGroupVO();
            group.name = nodeGroup.getName();
            group.nodes.addAll(nodeGroup.getNodeIds());
            graph.groups.add(group);
        }

        return graph;
    }

    @Override
    public void clear() {
        super.clear();
        graphBoxes.clear();
        boxWindows.clear();
        windowPositions.clear();
        graphConnections.clear();
        connectionNodeMap.clear();
        connections.clear();
        nodeGroups.clear();
        selectedNodes.clear();

        setupListeners();
    }
}
