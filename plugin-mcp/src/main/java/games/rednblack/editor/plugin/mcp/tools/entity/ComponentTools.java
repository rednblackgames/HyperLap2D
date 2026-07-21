package games.rednblack.editor.plugin.mcp.tools.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.tools.JsonArgs;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.h2d.common.remote.RemoteEditRequest;
import games.rednblack.h2d.common.remote.RemoteEditResult;
import games.rednblack.h2d.common.remote.RemoteEditableComponentsResult;
import games.rednblack.h2d.common.remote.RemoteCreateShaderRequest;
import games.rednblack.h2d.common.remote.RemoteCreateShaderResult;
import games.rednblack.h2d.common.remote.RemoteAssetDimensionsResult;
import games.rednblack.h2d.common.remote.RemoteSceneSettingsResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Mutation tools that route through the editor-core RemoteOps EDIT path, which drives the
 * existing properties panels so validation is never bypassed and changes are undoable.
 *
 * The addable component keys are the editor's stable vocabulary; per-entity-type allowance
 * (e.g. Light/Shader excluded for light entities, Typing Label only for labels) is enforced
 * editor-side via {@code AddableComponents} — the plugin's enum is guidance only.
 */
public final class ComponentTools {
    private ComponentTools() {}

    /** Addable component keys (the editor's "Add Component" dropdown vocabulary). */
    static final String[] ADDABLE_KEYS = {
            "Polygon Shape", "Circle Shape", "Physics", "Physics Sensors",
            "Shader", "Light", "Typing Label", "Layout"
    };

    static McpToolResult format(RemoteEditResult r) {
        if (r.ok) return McpToolResult.text("ok");
        StringBuilder sb = new StringBuilder();
        sb.append("error: ").append(r.error != null ? r.error : "unknown");
        if (r.validationErrors != null && !r.validationErrors.isEmpty()) {
            sb.append(" | validation: ").append(String.join("; ", r.validationErrors));
        }
        return McpToolResult.error(sb.toString());
    }

    private static void writeEntityIdAndComponentKeySchema(JsonWriter w) throws IOException {
        w.object("properties");
        w.object("entityId");
        w.set("type", "string");
        w.set("description", "The entity uniqueId (from list_entities).");
        w.pop();
        w.object("componentKey");
        w.set("type", "string");
        w.array("enum");
        for (String k : ADDABLE_KEYS) w.value(k);
        w.pop();
        w.pop();
        w.pop();
        w.name("required");
        w.array();
        w.value("entityId");
        w.value("componentKey");
        w.pop();
        w.set("additionalProperties", false);
    }

    public static final class AddComponentTool implements Tool {
        private final McpContext ctx;
        public AddComponentTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "add_component"; }
        @Override public String description() {
            return "Add a component to an entity. componentKey must be allowed for the entity's type "
                    + "(validated editor-side): Light/Shader cannot be added to light entities; Typing Label "
                    + "only to label entities; already-present components are rejected.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            writeEntityIdAndComponentKeySchema(w);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteEditRequest req = new RemoteEditRequest();
            req.op = RemoteEditRequest.Op.ADD_COMPONENT;
            req.entityId = args.getString("entityId", "");
            req.componentKey = args.getString("componentKey", "");
            return format(new RemoteOps(ctx).edit(req, 5000));
        }
    }

    public static final class RemoveComponentTool implements Tool {
        private final McpContext ctx;
        public RemoveComponentTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "remove_component"; }
        @Override public String description() {
            return "Remove a component from an entity (only removable components: Polygon Shape, Circle Shape, "
                    + "Physics, Physics Sensors, Shader, Light, Typing Label, Layout). Validated editor-side.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            writeEntityIdAndComponentKeySchema(w);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteEditRequest req = new RemoteEditRequest();
            req.op = RemoteEditRequest.Op.REMOVE_COMPONENT;
            req.entityId = args.getString("entityId", "");
            req.componentKey = args.getString("componentKey", "");
            return format(new RemoteOps(ctx).edit(req, 5000));
        }
    }

    public static final class UpdateTransformTool implements Tool {
        private final McpContext ctx;
        public UpdateTransformTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "update_transform"; }
        @Override public String description() {
            return "Set basic/transform fields on an entity. Pass only the fields to change: x, y, width, height, "
                    + "scaleX, scaleY, rotation (numbers), flipX, flipY (booleans), id (string). Validated through "
                    + "the basic properties panel (width/height must be >= 0). The edit is undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityId");
            w.set("type", "string");
            w.set("description", "The entity uniqueId (from list_entities).");
            w.pop();
            w.object("fields");
            w.set("type", "object");
            w.object("properties");
            w.object("x"); w.set("type", "number"); w.pop();
            w.object("y"); w.set("type", "number"); w.pop();
            w.object("width"); w.set("type", "number"); w.pop();
            w.object("height"); w.set("type", "number"); w.pop();
            w.object("scaleX"); w.set("type", "number"); w.pop();
            w.object("scaleY"); w.set("type", "number"); w.pop();
            w.object("rotation"); w.set("type", "number"); w.pop();
            w.object("flipX"); w.set("type", "boolean"); w.pop();
            w.object("flipY"); w.set("type", "boolean"); w.pop();
            w.object("id"); w.set("type", "string"); w.pop();
            w.pop();
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.value("fields");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteEditRequest req = new RemoteEditRequest();
            req.op = RemoteEditRequest.Op.SET_FIELDS;
            req.componentKey = "basic";
            req.entityId = args.getString("entityId", "");
            Map<String, Object> fields = JsonArgs.toFieldMap(args.get("fields"));
            req.fields = fields;
            return format(new RemoteOps(ctx).edit(req, 5000));
        }
    }

    public static final class DeleteEntityTool implements Tool {
        private final McpContext ctx;
        public DeleteEntityTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "delete_entity"; }
        @Override public String description() {
            return "Delete an entity by uniqueId (selects it then runs the standard delete command, undoable).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityId");
            w.set("type", "string");
            w.set("description", "The entity uniqueId (from list_entities).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            String entityId = args.getString("entityId", "");
            return format(new RemoteOps(ctx).delete(entityId, 5000));
        }
    }

    public static final class DeleteEntitiesTool implements Tool {
        private final McpContext ctx;
        public DeleteEntitiesTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "delete_entities"; }
        @Override public String description() {
            return "Delete multiple entities by uniqueId in one call. Pass an 'entityIds' array; each is "
                    + "selected then deleted via the standard delete command (each undoable). Returns a JSON "
                    + "array of per-id results {entityId, ok, error} in input order. Use this to clean up many "
                    + "entities at once instead of many delete_entity round-trips.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityIds");
            w.set("type", "array");
            w.set("description", "Array of entity uniqueIds (from list_entities) to delete.");
            w.object("items");
            w.set("type", "string");
            w.pop();
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityIds");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            JsonValue ids = args.get("entityIds");
            if (ids == null || !ids.isArray()) {
                return McpToolResult.error("'entityIds' must be an array");
            }
            RemoteOps remote = new RemoteOps(ctx);
            String json = games.rednblack.editor.plugin.mcp.server.McpJson.array(w -> {
                for (JsonValue id = ids.child(); id != null; id = id.next()) {
                    String entityId = id.asString();
                    w.object();
                    w.set("entityId", entityId != null ? entityId : "");
                    try {
                        RemoteEditResult r = remote.delete(entityId == null ? "" : entityId, 5000);
                        if (r.ok) {
                            w.set("ok", true);
                        } else {
                            w.set("ok", false);
                            w.set("error", r.error != null ? r.error : "delete failed");
                        }
                    } catch (Throwable t) {
                        w.set("ok", false);
                        w.set("error", t.getClass().getSimpleName() + ": " + t.getMessage());
                    }
                    w.pop();
                }
            });
            return McpToolResult.text(json);
        }
    }

    public static final class SetZIndexTool implements Tool {
        private final McpContext ctx;
        public SetZIndexTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "set_z_index"; }
        @Override public String description() {
            return "Set an entity's z-index to an absolute integer. z-index is local to the entity's layer; "
                    + "the runtime auto-adjusts z-indices into a linear progression to match the order. Lower "
                    + "draws behind, higher draws in front. The change is undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityId");
            w.set("type", "string");
            w.set("description", "The entity uniqueId (from list_entities).");
            w.pop();
            w.object("zIndex");
            w.set("type", "integer");
            w.set("description", "Target z-index (local to the entity's layer).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.value("zIndex");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            String entityId = args.getString("entityId", "");
            int zIndex = args.getInt("zIndex", 0);
            return format(new RemoteOps(ctx).setZIndex(entityId, zIndex, 5000));
        }
    }

    public static final class OpenSceneTool implements Tool {
        private final McpContext ctx;
        public OpenSceneTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "open_scene"; }
        @Override public String description() {
            return "Open (load) a scene by name in the editor. Use list_scenes to get the available names.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("sceneName");
            w.set("type", "string");
            w.set("description", "The scene name (from list_scenes).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("sceneName");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            String sceneName = args.getString("sceneName", "");
            return format(new RemoteOps(ctx).openScene(sceneName, 30000));
        }
    }

    /**
     * Generic component field editor — drives the properties panel for the given componentKey
     * through the editor-core RemoteOps path (validated, undoable). componentKey determines the
     * panel and which fields are accepted:
     * <ul>
     *   <li><b>basic</b>: x, y, width, height, scaleX, scaleY, rotation, flipX, flipY, id (all entities)</li>
     *   <li><b>label</b>: text, fontFamily, bitmapFont, fontSize, align, wrap, mono (label entities)</li>
     *   <li><b>shader</b>: shaderName, renderingLayer (entities with a Shader component)</li>
     * </ul>
     */
    public static final class UpdateComponentTool implements Tool {
        private final McpContext ctx;
        public UpdateComponentTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "update_component"; }
        @Override public String description() {
            return "Set fields on a component via its properties panel (validated, undoable). componentKey selects "
                    + "the panel: basic (x/y/width/height/scaleX/scaleY/rotation/flipX/flipY/id/tint), "
                    + "label (text/fontFamily/bitmapFont/fontSize/align/wrap/mono), "
                    + "particle (matrixTransform/autoStart), image (renderMode/spriteType), "
                    + "composite (scissorsEnabled/renderToFBO/automaticResize), sprite (fps/animation/playMode), "
                    + "spine (animation/skin), talos (matrixTransform/autoStart), "
                    + "lightItem (type/rayCount/radius/angle/distance/direction/intensity/height/softnessLength/falloff/isStatic/isXRay/isSoft/isActive), "
                    + "shader (shaderName/renderingLayer), "
                    + "physics (mass/density/friction/restitution/damping/angularDamping/gravityScale/height/"
                    + "centerOfMassX/centerOfMassY/rotationalInertia/bodyType/shapeType/allowSleep/awake/bullet/"
                    + "sensor/fineBoundBox/fixedRotation), "
                    + "light (rays/distance/intensity/constantFalloff/linearFalloff/quadraticFalloff/softnessLength/"
                    + "height/direction/isStatic/isXRay/isSoft/isActive/color), "
                    + "circle (radius), polygon (verticesCount/openPath), sensor (sensorTop/Bottom/Left/Right, "
                    + "spanPercentTop/Bottom/Left/Right, heightPercentTop/Bottom, widthPercentLeft/Right), "
                    + "layout (leftMargin/rightMargin/bottomMargin/topMargin, horizontalBias/verticalBias, "
                    + "leftEnabled/rightEnabled/bottomEnabled/topEnabled, leftTarget/rightTarget/bottomTarget/topTarget, "
                    + "leftSide/rightSide/bottomSide/topSide, matchWidth/matchHeight). "
                    + "Colors (tint/color/ambientColor/directionalColor) accept 'r,g,b,a', a [r,g,b,a] list, or '#RRGGBBAA'. "
                    + "Vector3 (falloff) accepts 'x,y,z' or [x,y,z]. Pass only the fields to change. Select fields accept only allowed values.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityId");
            w.set("type", "string");
            w.set("description", "The entity uniqueId (from list_entities).");
            w.pop();
            w.object("componentKey");
            w.set("type", "string");
            w.array("enum");
            w.value("basic");
            w.value("label");
            w.value("particle");
            w.value("image");
            w.value("composite");
            w.value("sprite");
            w.value("spine");
            w.value("talos");
            w.value("lightItem");
            w.value("shader");
            w.value("physics");
            w.value("light");
            w.value("circle");
            w.value("polygon");
            w.value("sensor");
            w.value("layout");
            w.pop();
            w.pop();
            w.object("fields");
            w.set("type", "object");
            w.set("description", "Field name -> value. Numeric fields take numbers; flags take booleans; select fields take one of the allowed values.");
            w.set("additionalProperties", true);
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.value("componentKey");
            w.value("fields");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteEditRequest req = new RemoteEditRequest();
            req.op = RemoteEditRequest.Op.SET_FIELDS;
            req.entityId = args.getString("entityId", "");
            req.componentKey = args.getString("componentKey", "");
            req.fields = JsonArgs.toFieldMap(args.get("fields"));
            return format(new RemoteOps(ctx).edit(req, 5000));
        }
    }

    public static final class GetSceneSettingsTool implements Tool {
        private final McpContext ctx;
        public GetSceneSettingsTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "get_scene_settings"; }
        @Override public String description() {
            return "Read the current scene's settings: physics (physicsEnabled, gravityX, gravityY, sleepVelocity), "
                    + "lights (lightsEnabled, pseudo3d, blurNum, lightMapScale, lightType, directionalRays, "
                    + "directionalDegree, directionalHeight, ambientColor, directionalColor) and scene shader.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteSceneSettingsResult r = new RemoteOps(ctx).getSceneSettings(5000);
            if (!r.ok) return McpToolResult.error(r.error != null ? r.error : "get scene settings failed");
            String json = McpJson.object(w -> {
                w.set("sceneName", r.sceneName == null ? "" : r.sceneName);
                for (Map.Entry<String, Object> e : r.settings.entrySet()) {
                    Object v = e.getValue();
                    if (v instanceof Boolean) w.set(e.getKey(), (Boolean) v);
                    else if (v instanceof Number) w.set(e.getKey(), ((Number) v).floatValue());
                    else if (v instanceof List) {
                        w.name(e.getKey());
                        w.array();
                        for (Object item : (List<?>) v) {
                            if (item instanceof Number) w.value(((Number) item).floatValue());
                            else w.value(item == null ? (Object) null : item.toString());
                        }
                        w.pop();
                    } else {
                        w.set(e.getKey(), v == null ? "" : v.toString());
                    }
                }
            });
            return McpToolResult.text(json);
        }
    }

    public static final class UpdateSceneSettingsTool implements Tool {
        private final McpContext ctx;
        public UpdateSceneSettingsTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "update_scene_settings"; }
        @Override public String description() {
            return "Set the current scene's settings via the scene properties panel (validated, undoable). Fields: "
                    + "physicsEnabled, gravityX, gravityY, sleepVelocity, lightsEnabled, pseudo3d, blurNum, "
                    + "lightMapScale, lightType (DIFFUSE/DIRECTIONAL/BRIGHT), directionalRays, directionalDegree, "
                    + "directionalHeight (only when lightType=DIRECTIONAL), ambientColor, directionalColor, shader. "
                    + "Colors (ambientColor/directionalColor) accept 'r,g,b,a', a [r,g,b,a] list, or '#RRGGBBAA'. "
                    + "Pass only the fields to change.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("fields");
            w.set("type", "object");
            w.set("description", "Field name -> value. Booleans for flags, numbers for numerics, strings for lightType/shader.");
            w.set("additionalProperties", true);
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("fields");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteEditRequest req = new RemoteEditRequest();
            req.op = RemoteEditRequest.Op.SET_FIELDS;
            req.componentKey = "scene";
            req.fields = JsonArgs.toFieldMap(args.get("fields"));
            return format(new RemoteOps(ctx).edit(req, 5000));
        }
    }

    public static final class SaveProjectTool implements Tool {
        private final McpContext ctx;
        public SaveProjectTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "save_project"; }
        @Override public String description() {
            return "Save the current project (all scenes and assets) to disk.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            ctx.api().saveProject();
            return McpToolResult.text("ok");
        }
    }

    public static final class ListActionsTool implements Tool {
        private final McpContext ctx;
        public ListActionsTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "list_actions"; }
        @Override public String description() {
            return "List the library action graph names in the current project (the node-graph actions "
                    + "that can be attached to entities).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            if (ctx.api().getCurrentProjectInfoVO() == null) return McpToolResult.text("[]");
            String json = McpJson.array(w -> {
                for (String name : ctx.api().getCurrentProjectInfoVO().libraryActions.keySet()) w.value(name);
            });
            return McpToolResult.text(json);
        }
    }

    public static final class GetEditableComponentsTool implements Tool {
        private final McpContext ctx;
        public GetEditableComponentsTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "get_editable_components"; }
        @Override public String description() {
            return "For a given entity, list the componentKeys whose properties panel currently applies (editable, "
                    + "for use with update_component) and the components that can be added (addable, for use with "
                    + "add_component). Use this to discover what an entity can have edited/added.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityId");
            w.set("type", "string");
            w.set("description", "The entity uniqueId (from list_entities).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            String entityId = args.getString("entityId", "");
            RemoteEditableComponentsResult r = new RemoteOps(ctx).editableComponents(entityId, 5000);
            if (!r.ok) return McpToolResult.error(r.error != null ? r.error : "failed");
            String json = McpJson.object(w -> {
                w.name("editable");
                w.array();
                for (String k : r.editable) w.value(k);
                w.pop();
                w.name("addable");
                w.array();
                for (String k : r.addable) w.value(k);
                w.pop();
            });
            return McpToolResult.text(json);
        }
    }

    public static final class CreateShaderTool implements Tool {
        private final McpContext ctx;
        public CreateShaderTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "create_shader"; }
        @Override public String description() {
            return "Create a shader resource in the project. Pass a name and either a templateType "
                    + "(0=Default Array, 1=Distance Field, 2=Screen Reading) or custom vertex/fragment GLSL source. "
                    + "Writes the .vert/.frag files and registers the shader (compile-checked). "
                    + "After creation, use update_component (shader) / update_scene_settings (shader) to apply it.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("name");
            w.set("type", "string");
            w.set("description", "Shader resource name (must be unique).");
            w.pop();
            w.object("templateType");
            w.set("type", "integer");
            w.set("default", 0);
            w.set("description", "0=Default Array, 1=Distance Field, 2=Screen Reading. Ignored if vertex/fragment given.");
            w.pop();
            w.object("vertex");
            w.set("type", "string");
            w.set("description", "Custom vertex GLSL (optional; overrides template).");
            w.pop();
            w.object("fragment");
            w.set("type", "string");
            w.set("description", "Custom fragment GLSL (optional; overrides template).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("name");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteCreateShaderRequest req = new RemoteCreateShaderRequest();
            req.name = args.getString("name", "");
            if (args.has("templateType")) req.templateType = args.getInt("templateType", 0);
            if (args.has("vertex")) req.vertex = args.getString("vertex");
            if (args.has("fragment")) req.fragment = args.getString("fragment");
            RemoteCreateShaderResult r = new RemoteOps(ctx).createShader(req, 10000);
            if (!r.ok) return McpToolResult.error(r.error != null ? r.error : "create shader failed");
            return McpToolResult.text(r.name != null ? r.name : "ok");
        }
    }

    public static final class GetAssetDimensionsTool implements Tool {
        private final McpContext ctx;
        public GetAssetDimensionsTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "get_asset_dimensions"; }
        @Override public String description() {
            return "Get pixel + world dimensions (world = pixel / pixelsPerWU) for all image/nine-patch regions, "
                    + "plus pixelsPerWU. Use this to plan entity placement at correct world coordinates "
                    + "(place tiles worldWidth apart, etc.).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteAssetDimensionsResult r = new RemoteOps(ctx).assetDimensions(5000);
            if (!r.ok) return McpToolResult.error(r.error != null ? r.error : "failed");
            String json = McpJson.object(w -> {
                w.set("pixelsPerWU", r.pixelsPerWU);
                w.name("regions");
                w.array();
                for (RemoteAssetDimensionsResult.RegionDim d : r.regions) {
                    w.object();
                    w.set("name", d.name);
                    w.set("pixelWidth", d.pixelWidth);
                    w.set("pixelHeight", d.pixelHeight);
                    w.set("worldWidth", d.worldWidth);
                    w.set("worldHeight", d.worldHeight);
                    w.set("ninePatch", d.ninePatch);
                    w.pop();
                }
                w.pop();
            });
            return McpToolResult.text(json);
        }
    }

    public static final class GetAssetPreviewTool implements Tool {
        private final McpContext ctx;
        public GetAssetPreviewTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "get_asset_preview"; }
        @Override public String description() {
            return "Read the source PNG for an image region and return it as image content, plus its pixel dimensions. "
                    + "Use to see what an asset looks like before placing it. folder defaults to "
                    + "<projectPath>/assets/orig/images; pass folder to point elsewhere.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("name");
            w.set("type", "string");
            w.set("description", "Image region name (from list_assets 'imageRegion'/'ninePatchRegion').");
            w.pop();
            w.object("folder");
            w.set("type", "string");
            w.set("description", "Optional: folder containing the source <name>.png (default <projectPath>/assets/orig/images).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("name");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            String name = args.getString("name", "");
            if (name.isEmpty()) return McpToolResult.error("missing name");
            String folder = args.has("folder") ? args.getString("folder")
                    : ctx.api().getProjectPath() + "/assets/orig/images";
            String path = folder + "/" + name + ".png";
            try {
                java.io.File f = new java.io.File(path);
                if (!f.exists()) return McpToolResult.error("source PNG not found: " + path);
                byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                if (bytes.length < 24 || (bytes[0] & 0xff) != 0x89 || bytes[1] != 'P' || bytes[2] != 'N' || bytes[3] != 'G')
                    return McpToolResult.error("not a PNG: " + path);
                int pw = ((bytes[16] & 0xff) << 24) | ((bytes[17] & 0xff) << 16) | ((bytes[18] & 0xff) << 8) | (bytes[19] & 0xff);
                int ph = ((bytes[20] & 0xff) << 24) | ((bytes[21] & 0xff) << 16) | ((bytes[22] & 0xff) << 8) | (bytes[23] & 0xff);
                return McpToolResult.textAndImage("pixelWidth=" + pw + " pixelHeight=" + ph + " path=" + path, bytes);
            } catch (Exception e) {
                return McpToolResult.error("read failed: " + e.getMessage());
            }
        }
    }
}