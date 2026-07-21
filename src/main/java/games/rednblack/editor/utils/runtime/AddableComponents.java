package games.rednblack.editor.utils.runtime;

import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.extension.typinglabel.TypingLabelComponent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The set of components the "Add Component" dropdown can attach, plus the per-entity-type
 * restrictions, extracted from {@code UIBasicItemPropertiesMediator} so both the live UI
 * and the MCP RemoteOps path share one source of truth (no validation bypass).
 *
 * 7 of the 8 component classes live in {@code hyperlap2d-runtime-libgdx}; only
 * {@link TypingLabelComponent} is in the typinglabel extension (which the editor, but not
 * the sandboxed plugin, depends on). This helper is editor-core, so all 8 are reachable.
 */
public final class AddableComponents {
    private AddableComponents() {}

    public static final String POLYGON_COMPONENT_KEY = "Polygon Shape";
    public static final String CIRCLE_SHAPE_COMPONENT_KEY = "Circle Shape";
    public static final String PHYSICS_COMPONENT_KEY = "Physics";
    public static final String SENSOR_COMPONENT_KEY = "Physics Sensors";
    public static final String SHADER_COMPONENT_KEY = "Shader";
    public static final String LIGHT_COMPONENT_KEY = "Light";
    public static final String TYPING_LABEL_COMPONENT_KEY = "Typing Label";
    public static final String LAYOUT_COMPONENT_KEY = "Layout";

    /** All addable component keys -> component classes (immutable). */
    public static final Map<String, Class<? extends Component>> COMPONENT_CLASS_MAP;
    static {
        Map<String, Class<? extends Component>> m = new LinkedHashMap<>();
        m.put(POLYGON_COMPONENT_KEY, PolygonShapeComponent.class);
        m.put(CIRCLE_SHAPE_COMPONENT_KEY, CircleShapeComponent.class);
        m.put(PHYSICS_COMPONENT_KEY, PhysicsBodyComponent.class);
        m.put(SENSOR_COMPONENT_KEY, SensorComponent.class);
        m.put(SHADER_COMPONENT_KEY, ShaderComponent.class);
        m.put(LIGHT_COMPONENT_KEY, LightBodyComponent.class);
        m.put(TYPING_LABEL_COMPONENT_KEY, TypingLabelComponent.class);
        m.put(LAYOUT_COMPONENT_KEY, LayoutComponent.class);
        COMPONENT_CLASS_MAP = Collections.unmodifiableMap(m);
    }

    public static Class<? extends Component> classForKey(String key) {
        return COMPONENT_CLASS_MAP.get(key);
    }

    /** Components allowed for the given entity type (per-type filtering applied), as a fresh map. */
    public static Map<String, Class<? extends Component>> forEntityType(int entityType) {
        Map<String, Class<? extends Component>> addable = new LinkedHashMap<>(COMPONENT_CLASS_MAP);
        if (entityType == EntityFactory.LIGHT_TYPE) {
            addable.remove(LIGHT_COMPONENT_KEY);
            addable.remove(SHADER_COMPONENT_KEY);
        }
        if (entityType != EntityFactory.LABEL_TYPE) {
            addable.remove(TYPING_LABEL_COMPONENT_KEY);
        }
        return addable;
    }

    /** Components that can still be added to {@code entity} (allowed for its type and not already present). */
    public static Map<String, Class<? extends Component>> addableForEntity(int entity, Engine engine) {
        MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, engine);
        int type = main != null ? main.entityType : -1;
        Map<String, Class<? extends Component>> addable = forEntityType(type);
        addable.entrySet().removeIf(e -> ComponentRetriever.get(entity, e.getValue(), engine) != null);
        return addable;
    }

    /** True if {@code componentKey} is a valid addable component for {@code entity} (allowed + not present). */
    public static boolean isAddable(int entity, String componentKey, Engine engine) {
        Class<? extends Component> cls = classForKey(componentKey);
        if (cls == null) return false;
        return addableForEntity(entity, engine).containsKey(componentKey);
    }
}