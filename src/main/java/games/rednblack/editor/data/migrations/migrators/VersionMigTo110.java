package games.rednblack.editor.data.migrations.migrators;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.data.migrations.IVersionMigrator;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.renderer.widget.handlers.CoreStateOverrides;
import games.rednblack.h2d.common.vo.ProjectVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns the buttons of the old system into widgets.
 *
 * A button used to be a composite tagged {@code button} whose children lived on two layers: the
 * ones on {@code normal} were shown while the button was idle, the ones on {@code pressed} while it
 * was held down or checked. The look of a state now belongs to the children themselves, as
 * overrides, so the two layers have nothing left to say and are merged away.
 *
 * What the old system did, stated as overrides of the states a button widget declares:
 * <ul>
 *     <li>a child of the {@code normal} layer is hidden in {@code pressed} and {@code checked}</li>
 *     <li>a child of the {@code pressed} layer is hidden in every other state</li>
 *     <li>a child of any other layer was never touched, and still is not</li>
 * </ul>
 *
 * Children keep their drawing order to the letter: they are re-homed to the layer that sat right
 * behind the ones being removed and renumbered in the order they were drawn in.
 */
public class VersionMigTo110 implements IVersionMigrator {

    private static final String NORMAL_LAYER = "normal";
    private static final String PRESSED_LAYER = "pressed";
    private static final String BUTTON_TAG = "button";
    private static final String DEFAULT_LAYER = "Default";

    private String projectPath;
    private ProjectVO projectVO;
    private ProjectInfoVO projectInfoVO;

    @Override
    public void setProject(String path, ProjectVO vo, ProjectInfoVO projectInfoVO) {
        projectPath = path;
        projectVO = vo;
        this.projectInfoVO = projectInfoVO;
    }

    @Override
    public boolean doMigration() {
        try {
            for (SceneVO sceneVO : projectInfoVO.scenes) {
                migrateFile(new File(projectPath + File.separator + "scenes" + File.separator + sceneVO.sceneName + ".dt"));
            }
            // library items are buttons too, and they live in the project file
            migrateFile(new File(projectPath + File.separator + "project.dt"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void migrateFile(File file) throws IOException {
        if (!file.exists()) return;

        JsonValue root = new JsonReader().parse(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        if (root == null || !migrate(root)) return;

        FileUtils.writeStringToFile(file, root.toJson(JsonWriter.OutputType.json), StandardCharsets.UTF_8);
    }

    /**
     * Converts every old button in the tree, wherever it sits: a scene, a library item, or inside
     * another composite.
     *
     * @return true if anything was converted
     */
    public static boolean migrate(JsonValue node) {
        boolean converted = false;
        for (JsonValue child = node.child; child != null; child = child.next) {
            converted |= migrate(child);
        }

        if (node.isObject() && isLegacyButton(node)) {
            convert(node);
            converted = true;
        }
        return converted;
    }

    private static boolean isLegacyButton(JsonValue node) {
        // one already holding a widget has been through here before
        if (node.get("widget") != null) return false;

        JsonValue tags = node.get("tags");
        if (tags == null || !tags.isArray()) return false;

        for (JsonValue tag = tags.child; tag != null; tag = tag.next) {
            if (BUTTON_TAG.equals(tag.asString())) return true;
        }
        return false;
    }

    private static void convert(JsonValue button) {
        removeTag(button);
        button.addChild("widget", widgetOf(WidgetTypes.get(WidgetTypes.BUTTON)));
        mergeStateLayers(button);
    }

    private static void removeTag(JsonValue button) {
        JsonValue tags = button.get("tags");
        for (JsonValue tag = tags.child; tag != null; tag = tag.next) {
            if (BUTTON_TAG.equals(tag.asString())) {
                tags.remove(indexOf(tags, tag));
                return;
            }
        }
    }

    private static int indexOf(JsonValue array, JsonValue value) {
        int index = 0;
        for (JsonValue entry = array.child; entry != null; entry = entry.next, index++) {
            if (entry == value) return index;
        }
        return -1;
    }

    /** The widget block of a type, as the editor would write it. */
    private static JsonValue widgetOf(WidgetType type) {
        JsonValue widget = new JsonValue(JsonValue.ValueType.object);
        widget.addChild("widgetType", new JsonValue(type.name));

        JsonValue states = new JsonValue(JsonValue.ValueType.array);
        for (String state : type.states) states.addChild(new JsonValue(state));
        widget.addChild("states", states);

        return widget;
    }

    /**
     * Writes what the two layers used to mean as state overrides, moves their children onto a layer
     * that stays, and takes the layers away.
     */
    private static void mergeStateLayers(JsonValue button) {
        Array<String> layers = layerNames(button);
        if (!layers.contains(NORMAL_LAYER, false) && !layers.contains(PRESSED_LAYER, false)) return;

        List<JsonValue> children = childrenOf(button);
        if (children.isEmpty()) {
            removeLayers(button, layers);
            return;
        }

        // the order they are drawn in: by layer first, then by index inside it
        children.sort((a, b) -> {
            int layerA = layers.indexOf(layerOf(a), false);
            int layerB = layers.indexOf(layerOf(b), false);
            if (layerA != layerB) return Integer.compare(Math.max(layerA, 0), Math.max(layerB, 0));
            return Integer.compare(a.getInt("zIndex", 0), b.getInt("zIndex", 0));
        });

        String survivor = survivingLayer(layers);
        int zIndex = 0;
        for (JsonValue child : children) {
            String layer = layerOf(child);

            if (NORMAL_LAYER.equals(layer)) {
                hideIn(child, WidgetTypes.STATE_PRESSED, WidgetTypes.STATE_CHECKED);
            } else if (PRESSED_LAYER.equals(layer)) {
                hideIn(child, WidgetTypes.STATE_NORMAL, WidgetTypes.STATE_HOVER, WidgetTypes.STATE_DISABLED);
            }

            if (NORMAL_LAYER.equals(layer) || PRESSED_LAYER.equals(layer)) set(child, "layerName", survivor);
            setInt(child, "zIndex", zIndex++);
        }

        removeLayers(button, layers);
        ensureLayer(button, survivor);
    }

    /**
     * @return the layer the orphaned children move to: the one right behind the ones going away, so
     * that they stay in front of everything they were in front of. The one in front of them if they
     * were the backmost, and a fresh default layer if the button had no other layer at all.
     */
    private static String survivingLayer(Array<String> layers) {
        int firstRemoved = layers.size;
        for (int i = 0; i < layers.size; i++) {
            if (isStateLayer(layers.get(i))) {
                firstRemoved = i;
                break;
            }
        }

        for (int i = firstRemoved - 1; i >= 0; i--) {
            if (!isStateLayer(layers.get(i))) return layers.get(i);
        }
        for (int i = firstRemoved + 1; i < layers.size; i++) {
            if (!isStateLayer(layers.get(i))) return layers.get(i);
        }
        return DEFAULT_LAYER;
    }

    private static boolean isStateLayer(String layer) {
        return NORMAL_LAYER.equals(layer) || PRESSED_LAYER.equals(layer);
    }

    private static void hideIn(JsonValue child, String... states) {
        JsonValue widgetPart = childObject(child, "widgetPart");
        JsonValue overrides = childObject(widgetPart, "overrides");

        for (String state : states) {
            JsonValue values = childObject(childObject(overrides, state), "values");
            if (values.get(CoreStateOverrides.VISIBLE) == null) {
                values.addChild(CoreStateOverrides.VISIBLE, new JsonValue("false"));
            }
        }
    }

    private static JsonValue childObject(JsonValue parent, String name) {
        JsonValue child = parent.get(name);
        if (child == null) {
            child = new JsonValue(JsonValue.ValueType.object);
            parent.addChild(name, child);
        }
        return child;
    }

    private static Array<String> layerNames(JsonValue composite) {
        Array<String> names = new Array<>();
        JsonValue layers = composite.get("layers");
        if (layers == null) return names;

        for (JsonValue layer = layers.child; layer != null; layer = layer.next) {
            names.add(layer.getString("layerName", ""));
        }
        return names;
    }

    /** Direct children, whatever type they are: {@code content} groups them by class name. */
    private static List<JsonValue> childrenOf(JsonValue composite) {
        List<JsonValue> children = new ArrayList<>();
        JsonValue content = composite.get("content");
        if (content == null) return children;

        for (JsonValue group = content.child; group != null; group = group.next) {
            for (JsonValue child = group.child; child != null; child = child.next) children.add(child);
        }
        return children;
    }

    private static String layerOf(JsonValue child) {
        return child.getString("layerName", "");
    }

    private static void removeLayers(JsonValue composite, Array<String> layers) {
        JsonValue layerArray = composite.get("layers");
        if (layerArray == null) return;

        for (JsonValue layer = layerArray.child; layer != null; layer = layer.next) {
            if (isStateLayer(layer.getString("layerName", ""))) layerArray.remove(indexOf(layerArray, layer));
        }
        layers.removeValue(NORMAL_LAYER, false);
        layers.removeValue(PRESSED_LAYER, false);
    }

    private static void ensureLayer(JsonValue composite, String name) {
        JsonValue layers = childArray(composite, "layers");
        for (JsonValue layer = layers.child; layer != null; layer = layer.next) {
            if (name.equals(layer.getString("layerName", ""))) return;
        }

        JsonValue layer = new JsonValue(JsonValue.ValueType.object);
        layer.addChild("layerName", new JsonValue(name));
        layer.addChild("isVisible", new JsonValue(true));
        layers.addChild(layer);
    }

    private static JsonValue childArray(JsonValue parent, String name) {
        JsonValue child = parent.get(name);
        if (child == null) {
            child = new JsonValue(JsonValue.ValueType.array);
            parent.addChild(name, child);
        }
        return child;
    }

    private static void set(JsonValue node, String name, String value) {
        JsonValue field = node.get(name);
        if (field == null) node.addChild(name, new JsonValue(value));
        else field.set(value);
    }

    private static void setInt(JsonValue node, String name, int value) {
        JsonValue field = node.get(name);
        if (field == null) node.addChild(name, new JsonValue(value));
        else field.set(value, null);
    }
}
