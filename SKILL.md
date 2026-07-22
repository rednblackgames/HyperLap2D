---
name: hyperlap2d-mcp
description: Drive the HyperLap2D 2D scene editor from an MCP client (create/modify entities & components, list assets, screenshot, scene settings, shaders, z-ordering). Use whenever the running editor's MCP server is connected and the task is to build or inspect a HyperLap2D scene.
---

# HyperLap2D MCP Server — Tool Guide

The HyperLap2D editor exposes an MCP server (Streamable HTTP) that lets an external client drive it: create and modify entities and components, list assets, capture screenshots, change scenes and scene settings, create shaders, and control z-ordering. This skill documents every tool, the editor's coordinate/layer model, and the edge cases that bite when authoring a scene blind.

## Prerequisites & connection

1. The editor is running (`./gradlew runHyperLap2D`) with a project open and a scene loaded.
2. **Settings → Plugins → "MCP Server"** is enabled (and the port set). The server starts/stops live on toggle.
3. The MCP client connects to `http://127.0.0.1:<port>/mcp` (Streamable HTTP). Configure it like:
   ```json
   { "mcpServers": { "hyperlap2d": { "url": "http://127.0.0.1:8765/mcp" } } }
   ```
4. If a tool returns "no scene loaded" / "no current scene", open a project and scene in the editor first. If `list_*` tools return empty, no project/scene is loaded.

All tool calls run on the editor's render thread via an internal bridge; they are synchronous with a timeout (≈5s, 15s for screenshots). Keep the editor open while calling.

## Core model (read this before placing anything)

- **World units, not pixels.** Entity `x`, `y`, `width`, `height` are in **world units**. One world unit = `pixelsPerWU` pixels. A value like `300` can be enormous or tiny depending on `pixelsPerWU` — **always read `pixelsPerWU` first** (`get_scene_settings` or `get_asset_dimensions`) before choosing coordinates.
- **Asset world size = pixel size / pixelsPerWU.** Use `get_asset_dimensions` to get each region's pixel + world width/height so you can place tiles the right distance apart.
- **Never guess an asset's appearance from its name.** Use `get_asset_preview` to actually see the source PNG before placing it. Names like `walls3` or `plant1` tell you nothing reliable about content or scale.
- **Coordinate origin.** The editor scene camera is y-down (origin top-left, +y downward). Entity `(x, y)` is the entity's position **in its parent composite's local space**, never in absolute scene space.
- **Layers + z-index.** Every entity belongs to a **layer** (named, e.g. `Default`) and has a **z-index** that is *local to that layer*. Lower z-index draws behind, higher draws in front. The runtime auto-adjusts z-indices into a linear progression, so setting an absolute integer is the intended usage (`set_z_index`). Read current order from `list_entities` (`zIndex` + `layer` fields).
- **Composites — everything lives in one.** A composite is a container entity that holds children. **The scene root is itself a composite**, so *every* entity has a parent composite and *every* `(x, y)` is local to that parent. A child's coordinates are an offset from its composite's origin — **not** a scene-absolute position. Creating a child at `(5.5, 0)` inside a composite places it 5.5 units from *that composite's* origin, wherever the composite sits. See **Building a composite correctly** below — getting this backwards puts everything in the wrong place.
- **Validation is sacred.** Component add/edit goes through the *same properties panels* the UI uses. Invalid values or disallowed components are rejected with the same messages the panels produce. Do not try to "work around" a rejected value — it's rejected because the editor considers it invalid.

## Golden rules for scene authoring

1. `get_scene_settings` → note `pixelsPerWU`.
2. `list_assets` → know what's available.
3. `get_asset_dimensions` → learn each region's world size.
4. `get_asset_preview` → *see* the assets you plan to use.
5. `create_entity` / `create_entities` at correct world coordinates (computed from pixelsPerWU and asset world sizes).
6. `set_z_index` to order layers/depth.
7. `screenshot` (mode `whole` or `region`) to let the user verify.

Do not place entities blindly; do not invent asset semantics. The user judges all visual results — your job is correct, validated structure.

---

## Tool reference

22 tools. Grouped by purpose.

### Discovery (read-only)

#### `list_scenes`
No args. Returns `[{ "name": ... }]` — all scenes in the open project.

#### `list_entities`
No args. Returns a flat array of every entity in the current scene:
`{ uniqueId, parentId, name, typeId, type, depth, zIndex, layer }`
- `uniqueId` — the stable id you pass to all mutation tools.
- `parentId` — null for direct children of the scene root; otherwise the parent composite's uniqueId. Use with `depth` to rebuild the tree.
- `type` — display name (covers core + Spine/Talos/TinyVG).
- `zIndex`, `layer` — the per-layer z-index and layer name (mirror of `set_z_index`).

#### `list_assets`
No args. Returns loaded project assets grouped by category:
`imageRegion`, `ninePatchRegion`, `spineAnimation`, `spriteAnimation`, `particleEffect`, `talosEffect`, `font`, `tinyvg`, `shader`.
Use region names for `create_entity` (image/9patch) and shader names for `update_component`/`update_scene_settings`.

#### `get_asset_dimensions`
No args. Returns `{ pixelsPerWU, regions: [{ name, pixelWidth, pixelHeight, worldWidth, worldHeight, ninePatch }] }` for all image/nine-patch regions. `worldWidth = pixelWidth / pixelsPerWU`. Use to plan placement spacing.

#### `get_asset_preview`
Args: `name` (required), `folder` (optional, default `<projectPath>/assets/orig/images`). Returns the source `<name>.png` as MCP **image content** plus text with `pixelWidth`/`pixelHeight`/`path`. Use to see an asset before placing it. If the PNG isn't in the default folder, pass `folder` to point elsewhere.

#### `get_scene_settings`
No args. Returns `{ sceneName, pixelsPerWU, shader, physicsEnabled, gravityX, gravityY, sleepVelocity, lightsEnabled, pseudo3d, blurNum, lightMapScale, lightType, directionalRays, directionalDegree, directionalHeight, ambientColor, directionalColor }`. Colors are `[r,g,b,a]` lists.

#### `get_editable_components`
Args: `entityId`. Returns `{ editable: [...], addable: [...] }` — the componentKeys whose panel currently applies to that entity (for `update_component`) and the components it can still have added (for `add_component`). **Use this when unsure** what an entity accepts; it's the single source of truth shared with the validation path.

#### `list_actions`
No args. Returns the library action-graph names in the project (the node-graph actions attachable to entities). (Actions are out of scope for creation via MCP — listed only.)

### Mutation (validated, undoable unless noted)

#### `create_entity`
Args: `type` (required), `name`, `x`, `y`, `width`, `height`, `fontFamily`, `fontSize`, `lightType`, `parentUniqueId`.
- `type`: `image | spriteAnimation | spineAnimation | libraryItem | 9patch | tinyvg | particle | talos | primitive | composite | label | light`.
- Name-based types (`image`, `spriteAnimation`, `spineAnimation`, `libraryItem`, `9patch`, `tinyvg`, `particle`, `talos`) require `name` from `list_assets`.
- `primitive`: `width`/`height` (default 100) define the rect at creation.
- `label`: optional `fontFamily` (must be a loaded font) / `fontSize` (default 20).
- `light`: `lightType` `POINT` (default) or `CONE`.
- `parentUniqueId`: create **inside** that composite; `x`/`y` then become **local to the composite** (offsets from its origin, not scene-absolute — see Edge cases → *Building a composite correctly*, and lay children out around `(0,0)`, not at their intended world positions). The composite must be a **direct child of the current viewing entity**. To target a nested composite, enter its container in the editor first.
- Returns the new entity's `uniqueId`. Creation is undoable (Ctrl+Z).

#### `create_entities` (bulk)
Args: `entities` (array of specs with the same fields as `create_entity`). Creates each sequentially via the same validated/undoable path. Returns a JSON array of `{ ok, uniqueId, error }` in input order. Use for placing many tiles/objects at once instead of many `create_entity` round-trips. Some entries can fail without aborting the rest.

#### `update_transform`
Args: `entityId`, `fields` (object). Fields: `x`, `y`, `width`, `height`, `scaleX`, `scaleY`, `rotation`, `flipX`, `flipY`, `id`, `tint`. Pass only what you want to change. Drives the basic properties panel (validated, undoable).
- **`width`/`height` are editable only for label, composite, and 9-patch entities.** For images/sprites/spine/etc. size comes from the asset — use `scaleX`/`scaleY`. For **primitives**, see Edge cases (width/height are *not* the size; the polygon vertices are).

#### `update_component`
Args: `entityId`, `componentKey`, `fields` (object). Drives that component's properties panel (validated, undoable). Pass only the fields to change. `componentKey` selects the panel and the accepted fields:

| componentKey | applies to | fields |
|---|---|---|
| `basic` | all entities | x, y, width, height, scaleX, scaleY, rotation, flipX, flipY, id, tint |
| `label` | label entities | text, fontFamily, bitmapFont, fontSize, align, wrap, mono |
| `particle` | particle effects | matrixTransform, autoStart |
| `image` | images | renderMode, spriteType |
| `composite` | composites | scissorsEnabled, renderToFBO, automaticResize |
| `sprite` | sprite animations | fps, animation, playMode |
| `spine` | spine animations | animation, skin |
| `talos` | talos effects | matrixTransform, autoStart |
| `lightItem` | light entities | type, rayCount, radius, angle, distance, direction, intensity, height, softnessLength, falloff, isStatic, isXRay, isSoft, isActive |
| `shader` | entities with a Shader component | shaderName, renderingLayer |
| `physics` | entities with Physics | mass, density, friction, restitution, damping, angularDamping, gravityScale, height, centerOfMassX, centerOfMassY, rotationalInertia, bodyType, shapeType, allowSleep, awake, bullet, sensor, fineBoundBox, fixedRotation |
| `light` | entities with a Light component | rays, distance, intensity, constantFalloff, linearFalloff, quadraticFalloff, softnessLength, height, direction, isStatic, isXRay, isSoft, isActive, color |
| `circle` | entities with Circle Shape | radius |
| `polygon` | entities with Polygon Shape | verticesCount, openPath |
| `sensor` | entities with Sensor | sensorTop/Bottom/Left/Right, spanPercentTop/Bottom/Left/Right, heightPercentTop/Bottom, widthPercentLeft/Right |
| `layout` | entities with Layout | leftMargin/rightMargin/bottomMargin/topMargin, horizontalBias/verticalBias, leftEnabled/rightEnabled/bottomEnabled/topEnabled, leftTarget/rightTarget/bottomTarget/topTarget, leftSide/rightSide/bottomSide/topSide, matchWidth/matchHeight |

For shape/physics/light/etc. components, the entity must already have that component — `add_component` it first (or check `get_editable_components`).

#### `add_component`
Args: `entityId`, `componentKey`. `componentKey` ∈ `Polygon Shape | Circle Shape | Physics | Physics Sensors | Shader | Light | Typing Label | Layout`. Allowed set is enforced editor-side (Light/Shader can't be added to light entities; Typing Label only to labels; already-present rejected). Undoable.

#### `remove_component`
Args: `entityId`, `componentKey` (same keys). Only the removable components above. Undoable.

#### `set_z_index`
Args: `entityId`, `zIndex` (integer). Sets the entity's z-index to an absolute value **local to its layer**. Lower draws behind, higher in front; the runtime auto-adjusts z-indices into a linear progression. Undoable (Ctrl+Z). Read the result back via `list_entities` (`zIndex`, `layer`).

#### `delete_entity`
Args: `entityId`. Selects then runs the standard delete command. Undoable.

#### `delete_entities` (bulk)
Args: `entityIds` (array of strings). Deletes each via the standard path. Returns `[{ entityId, ok, error }]` in input order. Some can fail (e.g. not found) without aborting the rest.

#### `open_scene`
Args: `sceneName` (from `list_scenes`). Routes through the editor's unsaved-changes check (may prompt/save in the UI), then loads the scene. Timeout 30s; if the user cancels a save dialog the request times out.

#### `update_scene_settings`
Args: `fields` (object). Drives the scene properties panel (validated, undoable). Fields: `physicsEnabled`, `gravityX`, `gravityY`, `sleepVelocity`, `lightsEnabled`, `pseudo3d`, `blurNum`, `lightMapScale`, `lightType` (`DIFFUSE`/`DIRECTIONAL`/`BRIGHT`), `directionalRays`, `directionalDegree`, `directionalHeight` (only when `lightType=DIRECTIONAL`), `ambientColor`, `directionalColor`, `shader`. Pass only what you want to change.

#### `create_shader`
Args: `name` (required, unique), `templateType` (0=Default Array, 1=Distance Field, 2=Screen Reading), optional `vertex` + `fragment` (custom GLSL, overrides template). Writes `.vert`/`.frag`, compile-checks first (a failed compile leaves no files), registers the shader. Then apply it with `update_component` (`shaderName`) on an entity, or `update_scene_settings` (`shader`) for the scene shader.

#### `save_project`
No args. Saves the current project (all scenes and assets) to disk.

### Visual

#### `screenshot`
Args: `mode` (`whole` | `view` | `region`, default `whole`); for `region` also `x`, `y`, `width`, `height` (world units). Returns a PNG as MCP **image content**.
- `whole` — all entities regardless of viewport (recommended for verifying a scene).
- `region` — a world-space rectangle (good for inspecting a specific area).
- `view` — the current editor camera view. **Known issue:** `view` can render upside-down on an oversized canvas; prefer `whole` or `region` (the user is fixing `view` separately).
- Screenshots currently do **not** render lights (known limitation).
- Huge scenes are capped to a safe max texture size per side, preserving aspect.

---

## Edge cases & gotchas

### Primitives: width/height are NOT the size
A color primitive's visual size is its **polygon vertices** (`PolygonShapeComponent`), not `DimensionsComponent.width/height` — those are a derived bounding box. Consequences:
- `update_transform` **refuses** `width`/`height` on primitives (the basic panel disables them for primitives, images, sprites, etc. — only label/composite/9-patch allow width/height). This is by design, not a bug.
- At **creation** time, `create_entity` with `type: "primitive"` does accept `width`/`height` and builds the rect polygon from them (`PolygonShapeVO.createRect(width, height)`).
- To **resize an existing primitive**, use `scaleX`/`scaleY` (via `update_transform`), or edit the polygon shape (`update_component` with `componentKey: "polygon"` controls `verticesCount`/`openPath`, not direct coordinates). For an exact new rectangle size, recreate the primitive.

### pixelsPerWU dominates coordinate choice
`x`, `y`, `width`, `height` are world units. With `pixelsPerWU = 100`, an entity at `x = 300` is 300 world units from origin — possibly far off-screen if the scene is small. Always read `pixelsPerWU` (`get_scene_settings` / `get_asset_dimensions`) first, and compute placement from asset **world** dimensions (`worldWidth = pixelWidth / pixelsPerWU`).

### Colors
Color fields (`tint`, `color`, `ambientColor`, `directionalColor`) accept any of:
- `"r,g,b,a"` (e.g. `"1,0,0,1"`)
- a JSON array `[r,g,b,a]`
- `"#RRGGBBAA"` hex (e.g. `"#FF0000FF"`)

### Label align is case-insensitive
`update_component` (`label`, `align`) matches the allowed names case-insensitively: `"top left"`, `"Center"`, `"BOTTOM RIGHT"` all work. Allowed values: `Top Left, Top, Top Right, Left, Center, Right, Bottom Left, Bottom, Bottom Right`.

### Building a composite correctly (READ THIS — the #1 mistake)

A composite has its own local coordinate space, and by default `automaticResize = true`: **the composite's size and origin are recomputed from its children.** You do **not** know a composite's final position/size until its children exist. This dictates the workflow.

**The correct sequence — build the group in local space, then place the whole group:**

1. **Create the composite** (`create_entity` type `composite`). Its `(x, y)` at this point is provisional — you'll set the real position in step 3. Create it at `(0, 0)`.
2. **Add all children with `parentUniqueId`, in LOCAL coordinates.** Lay the group out around its own origin — a child at local `(0, 0)` sits at the composite's origin, a child at `(2, 1)` is 2 units right / 1 down *within the group*. Do **not** use scene-absolute coordinates here (that was the mistake: children given world positions like `x=5.5` inside a composite meant to sit at the scene origin). Design the group as if it were its own little scene starting at `(0,0)`.
3. **Now position the composite in its parent.** After the children exist, `automaticResize` has computed the composite's dimensions. `update_transform` the composite's `(x, y)` to drop the finished group where it belongs in the parent. Everything inside moves with it, rigidly — you place the container once, not each child.

**Why "just give every child its final world position" fails:** with `automaticResize`, adding/moving a child re-derives the composite's bounding box and origin, so absolute-looking child coordinates don't land where you expect, and a later `update_transform` on one child shifts the whole group's frame. Build in local space (step 2), place once (step 3), and this never bites.

**Nesting / other rules:**
- The parent must be a **direct child of the current viewing entity** (normally the scene root). The editor's enter-composite flow only supports entering one level at a time. If you get *"parent composite must be a direct child of the current viewing entity; double-click its container in the editor to enter it first"*, enter the outer composite in the editor UI, then target the inner composite's uniqueId.
- Passing a non-composite uniqueId (an entity with no children slot) errors with *"parent is not a composite/container"*.
- The enter/exit happens within one render frame (no visible camera jump) and is **not** pushed to the undo stack — only the entity creation is undoable.

### Z-index is per-layer and absolute
`set_z_index` sets an absolute integer scoped to the entity's **layer**. The runtime re-linearizes z-indices, so don't try to compute "relative" offsets — just set the order you want (e.g. background=0, mid=5, foreground=10). Retrieve current values from `list_entities`.

### Component edits are panel-validated
Every `add_component` / `update_component` / `remove_component` / `update_transform` / `update_scene_settings` drives the real UI panel off-stage and runs its VisUI validators. If a value is rejected, the error message is the panel's. Use `get_editable_components` to discover what's allowed for a given entity before editing. Never assume a field exists for a type.

### Bulk operations
`create_entities` and `delete_entities` process entries sequentially and return per-entry results; a failure in one entry does **not** abort the others. Check the returned array for partial failures.

### Undoability
`create_entity`/`create_entities`, `update_transform`, `update_component`, `add_component`, `remove_component`, `set_z_index`, `delete_entity`/`delete_entities`, `update_scene_settings` are all undoable in the editor (Ctrl+Z). Composite enter/exit for creation is intentionally not on the undo stack. `create_shader` writes resource files (not a scene edit).

### Visual verification is the user's job
Never judge a screenshot as "looks good" on the user's behalf. Capture the screenshot, hand it over, and let the user decide. Your responsibility is correct, validated structure and correct coordinates/scale.

---

## Recommended workflow: design a scene from scratch

1. `open_scene` (or confirm the current scene) → `get_scene_settings` (note `pixelsPerWU`).
2. `list_assets` → `get_asset_dimensions` (world sizes) → `get_asset_preview` for the regions you'll use (see them).
3. Plan placement in world units using `worldWidth`/`worldHeight` (tile spacing = region world size; positions relative to origin). Group related entities into composites (e.g. `ground`, `path`, `wall`, `props`) — it keeps the scene tree navigable and lets you order/move whole groups at once.
4. For each group: create the composite, then `create_entities` (bulk) with `parentUniqueId` set and children laid out in **local** coordinates around `(0,0)` (see *Building a composite correctly*); then `update_transform` the composite to its final position. Capture the returned `uniqueId`s.
5. `set_z_index` to order the composites/entities background→foreground.
6. Add components as needed: `get_editable_components` → `add_component` → `update_component`.
7. `screenshot` (mode `whole`) → return it to the user for visual verification.
8. Iterate based on user feedback; `save_project` when satisfied.

If something is rejected, read the error — it reflects the editor's real validation. Don't retry the same invalid value.