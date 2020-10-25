package games.rednblack.editor.renderer.factory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.renderer.systems.action.Actions;
import games.rednblack.editor.renderer.systems.action.data.*;
import games.rednblack.editor.renderer.utils.ArrayUtils;
import games.rednblack.editor.renderer.utils.InterpolationMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class ActionFactory {
    private final Map<String, String> actionsLibrary;

    public ActionFactory(Map<String, String> actions) {
        actionsLibrary = actions;
    }

    public ActionData loadFromLibrary(String actionName) {
        return loadFromLibrary(actionName, true, null);
    }

    public ActionData loadFromLibrary(String actionName, ObjectMap<String, Object> params) {
        return loadFromLibrary(actionName, true, params);
    }

    public ActionData loadFromLibrary(String actionName, boolean autoPoolable, ObjectMap<String, Object> params) {
        if (actionsLibrary.get(actionName) == null)
            throw new IllegalArgumentException("The action '" + actionName + "' does not exists.");

        JSONParser parser = new JSONParser();
        ActionData data = null;
        try {
            JSONObject actionGraph = (JSONObject) parser.parse(actionsLibrary.get(actionName));
            data = parseGraph(actionGraph, autoPoolable, params);
        } catch (ParseException e) {
            throw new RuntimeException("The action '" + actionName + "' has not a valid format.");
        }
        return data;
    }

    private ActionData parseGraph(JSONObject actionGraph, boolean autoPoolable, ObjectMap<String, Object> params) {
        Map<String, GraphNode> nodes = new HashMap<>();
        for (JSONObject object : (List<JSONObject>) actionGraph.get("nodes")) {
            String type = (String) object.get("type");
            String id = (String) object.get("id");
            JSONObject data = (JSONObject) object.get("data");

            nodes.put(id, new GraphNode(id, type, data));
        }

        String actionNode = "";
        Map<String, List<GraphConnection>> toNodeConnections = new HashMap<>();
        for (JSONObject connection : (List<JSONObject>) actionGraph.get("connections")) {
            String toNode = (String) connection.get("toNode");
            String fromNode = (String) connection.get("fromNode");
            String fromField = (String) connection.get("fromField");
            String toField = (String) connection.get("toField");

            toNodeConnections.computeIfAbsent(toNode, k -> new ArrayList<>());
            toNodeConnections.get(toNode).add(new GraphConnection(fromNode, fromField, toField));
            Collections.sort(toNodeConnections.get(toNode));

            if (toNode.equals("end") && toField.equals("action")) {
                actionNode = fromNode;
            }
        }

        return getActionData(nodes.get(actionNode), toNodeConnections, nodes, autoPoolable, params);
    }

    private ActionData getActionData(GraphNode node, Map<String, List<GraphConnection>> toNodeConnections,
                                     Map<String, GraphNode> nodes, boolean autoPoolable, ObjectMap<String, Object> params) {
        ActionData actionData = mapTypeToActionData(node.type, autoPoolable);

        for (GraphConnection inConnection : toNodeConnections.get(node.id)) {
            if (inConnection.toField.contains("action")) {
                ActionData subAction = getActionData(nodes.get(inConnection.fromNode), toNodeConnections, nodes, autoPoolable, params);
                addSubAction(actionData, subAction);
            }

            addActionDataParameter(actionData, nodes, toNodeConnections.get(node.id), params);
        }
        return actionData;
    }

    private void addSubAction(ActionData actionData, ActionData subAction) {
        if (actionData instanceof DelegateData) {
            ((DelegateData) actionData).setDelegatedAction(subAction);
        }

        if (actionData instanceof ParallelData) {
            if (((ParallelData) actionData).actionsData == null) {
                ((ParallelData) actionData).actionsData = new ActionData[0];
            }
            ((ParallelData) actionData).actionsData = ArrayUtils.add(((ParallelData) actionData).actionsData, subAction);
        }
    }

    private void addActionDataParameter(ActionData actionData, Map<String, GraphNode> nodes, List<GraphConnection> connections, ObjectMap<String, Object> params) {
        if (actionData instanceof TemporalData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "duration":
                        ((TemporalData) actionData).setDuration((Float) getValue(nodes.get(connection.fromNode), params));
                        break;
                    case "interpolation":
                        ((TemporalData) actionData).setInterpolation((Interpolation) getValue(nodes.get(connection.fromNode), params));
                        break;
                }
            }
        }

        if (actionData instanceof MoveToData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "position":
                        Vector2 pos = (Vector2) getValue(nodes.get(connection.fromNode), params);
                        ((MoveToData) actionData).setEndX(pos.x);
                        ((MoveToData) actionData).setEndY(pos.y);
                        break;
                }
            }
        }

        if (actionData instanceof MoveByData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "position":
                        Vector2 pos = (Vector2) getValue(nodes.get(connection.fromNode), params);
                        ((MoveByData) actionData).setAmountX(pos.x);
                        ((MoveByData) actionData).setAmountY(pos.y);
                        break;
                }
            }
        }

        if (actionData instanceof RotateToData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "degree":
                        ((RotateToData) actionData).setEnd((Float) getValue(nodes.get(connection.fromNode), params));
                        break;
                }
            }
        }

        if (actionData instanceof RotateByData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "degree":
                        ((RotateByData) actionData).setAmount((Float) getValue(nodes.get(connection.fromNode), params));
                        break;
                }
            }
        }

        if (actionData instanceof SizeToData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "size":
                        Vector2 pos = (Vector2) getValue(nodes.get(connection.fromNode), params);
                        ((SizeToData) actionData).setEndWidth(pos.x);
                        ((SizeToData) actionData).setEndHeight(pos.y);
                        break;
                }
            }
        }

        if (actionData instanceof SizeByData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "size":
                        Vector2 pos = (Vector2) getValue(nodes.get(connection.fromNode), params);
                        ((SizeByData) actionData).setAmountWidth(pos.x);
                        ((SizeByData) actionData).setAmountHeight(pos.y);
                        break;
                }
            }
        }

        if (actionData instanceof ScaleToData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "scale":
                        Vector2 pos = (Vector2) getValue(nodes.get(connection.fromNode), params);
                        ((ScaleToData) actionData).setEndX(pos.x);
                        ((ScaleToData) actionData).setEndY(pos.y);
                        break;
                }
            }
        }

        if (actionData instanceof ScaleByData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "scale":
                        Vector2 pos = (Vector2) getValue(nodes.get(connection.fromNode), params);
                        ((ScaleByData) actionData).setAmountX(pos.x);
                        ((ScaleByData) actionData).setAmountY(pos.y);
                        break;
                }
            }
        }

        if (actionData instanceof ColorData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "color":
                        ((ColorData) actionData).setEndColor((Color) getValue(nodes.get(connection.fromNode), params));
                        break;
                }
            }
        }

        if (actionData instanceof AlphaData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "alpha":
                        ((AlphaData) actionData).setEnd((Float) getValue(nodes.get(connection.fromNode), params));
                        break;
                }
            }
        }

        if (actionData instanceof DelayData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "delay":
                        ((DelayData) actionData).setDuration((Float) getValue(nodes.get(connection.fromNode), params));
                        break;
                }
            }
        }

        if (actionData instanceof RepeatData) {
            for (GraphConnection connection : connections) {
                switch (connection.toField) {
                    case "count":
                        int count = (int) getValue(nodes.get(connection.fromNode), params);
                        ((RepeatData) actionData).setRepeatCount(count);
                        break;
                }
            }
        }
    }

    private Object getValue(GraphNode node, ObjectMap<String, Object> params) {
        switch (node.type) {
            case "ValueBoolean":
                return node.data.get("v");
            case "ValueColor":
                return Color.valueOf((String) node.data.get("color"));
            case "ValueFloat":
                return ((Number) node.data.get("v1")).floatValue();
            case "ValueVector2":
                return new Vector2(((Number) node.data.get("v1")).floatValue(), ((Number) node.data.get("v2")).floatValue());
            case "ValueInterpolation":
                return InterpolationMap.map.get((String) node.data.get("interpolation"));
            case "ValueParam":
                if (params == null || params.get((String) node.data.get("v")) == null)
                    throw new IllegalArgumentException("Custom parameter '" + node.data.get("v") + "' not found.");
                return params.get((String) node.data.get("v"));
            default:
                return null;
        }
    }

    private ActionData mapTypeToActionData(String nodeType, boolean autoPoolable) {
        switch (nodeType) {
            case "AlphaAction":
                return Actions.actionData(AlphaData.class, autoPoolable);
            case "ColorAction":
                return Actions.actionData(ColorData.class, autoPoolable);
            case "DelayAction":
                return Actions.actionData(DelayData.class, autoPoolable);
            case "FadeInAction":
                AlphaData fadeIn = Actions.actionData(AlphaData.class, autoPoolable);
                fadeIn.setEnd(1);
                return fadeIn;
            case "FadeOutAction":
                AlphaData fadeOut = Actions.actionData(AlphaData.class, autoPoolable);
                fadeOut.setEnd(0);
                return fadeOut;
            case "ForeverAction":
                RepeatData actionData = Actions.actionData(RepeatData.class, autoPoolable);
                actionData.setRepeatCount(RepeatData.FOREVER);
                return actionData;
            case "MoveByAction":
                return Actions.actionData(MoveByData.class, autoPoolable);
            case "MoveToAction":
                return Actions.actionData(MoveToData.class, autoPoolable);
            case "ParallelAction":
                return Actions.actionData(ParallelData.class, autoPoolable);
            case "RepeatAction":
                return Actions.actionData(RepeatData.class, autoPoolable);
            case "RotateByAction":
                return Actions.actionData(RotateByData.class, autoPoolable);
            case "RotateToAction":
                return Actions.actionData(RotateToData.class, autoPoolable);
            case "ScaleByAction":
                return Actions.actionData(ScaleByData.class, autoPoolable);
            case "SequenceAction":
                return Actions.actionData(SequenceData.class, autoPoolable);
            case "SizeByAction":
                return Actions.actionData(SizeByData.class, autoPoolable);
            case "SizeToAction":
                return Actions.actionData(SizeToData.class, autoPoolable);
            case "ScaleToAction":
                return Actions.actionData(ScaleToData.class, autoPoolable);
            default:
                return null;
        }
    }

    private static class GraphNode {
        String id;
        String type;
        JSONObject data;

        public GraphNode(String id, String type, JSONObject data) {
            this.id = id;
            this.data = data;
            this.type = type;
        }
    }

    private static class GraphConnection implements Comparable<GraphConnection> {
        String fromNode;
        String fromField;
        String toField;

        public GraphConnection(String fromNode, String fromField, String toField) {
            this.fromField = fromField;
            this.fromNode = fromNode;
            this.toField = toField;
        }

        @Override
        public int compareTo(GraphConnection o) {
            return toField.compareTo(o.toField);
        }
    }
}
