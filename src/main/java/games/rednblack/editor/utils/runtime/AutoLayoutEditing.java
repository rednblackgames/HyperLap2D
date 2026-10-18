package games.rednblack.editor.utils.runtime;

import games.rednblack.editor.proxy.PluginUIBridge;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.components.AutoLayoutItemComponent;
import games.rednblack.editor.renderer.components.AutoLayoutOwnSizeComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;

/** What the editor needs to know about auto layouts: who places what, and how to put things back. */
public final class AutoLayoutEditing {

    private AutoLayoutEditing() {
    }

    /** @return the parent laying the entity out, -1 when its parent has no auto layout or it asked to be left alone */
    public static int layingOut(int entity) {
        ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        if (parentNode == null || parentNode.parentEntity == -1) return -1;
        if (SandboxComponentRetriever.get(parentNode.parentEntity, AutoLayoutComponent.class) == null) return -1;
        AutoLayoutItemComponent item = SandboxComponentRetriever.get(entity, AutoLayoutItemComponent.class);
        return item != null && item.ignore ? -1 : parentNode.parentEntity;
    }

    /** @return the parent, when it has an auto layout, whether it places the entity or not; else -1 */
    public static int layoutParent(int entity) {
        ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        if (parentNode == null || parentNode.parentEntity == -1) return -1;
        return SandboxComponentRetriever.get(parentNode.parentEntity, AutoLayoutComponent.class) != null ? parentNode.parentEntity : -1;
    }

    /**
     * Where the children of a composite are, how large, and what size of their own the layout keeps
     * for them, to put them back on undo.
     */
    public static class ChildrenSnapshot {
        private final Array<String> ids = new Array<>();
        private final Array<float[]> values = new Array<>();

        public ChildrenSnapshot(int composite) {
            NodeComponent node = SandboxComponentRetriever.get(composite, NodeComponent.class);
            if (node == null) return;
            for (int child : node.children) {
                MainItemComponent mainItem = SandboxComponentRetriever.get(child, MainItemComponent.class);
                TransformComponent transform = SandboxComponentRetriever.get(child, TransformComponent.class);
                DimensionsComponent dimensions = SandboxComponentRetriever.get(child, DimensionsComponent.class);
                if (mainItem == null || transform == null || dimensions == null) continue;
                AutoLayoutOwnSizeComponent own = SandboxComponentRetriever.get(child, AutoLayoutOwnSizeComponent.class);
                ids.add(mainItem.uniqueId);
                values.add(new float[]{transform.x, transform.y, dimensions.width, dimensions.height,
                        own != null ? own.width : Float.NaN, own != null ? own.height : Float.NaN});
            }
        }

        public void restore() {
            for (int i = 0; i < ids.size; i++) {
                int child = EntityUtils.getByUniqueId(ids.get(i));
                if (child == -1) continue;
                TransformComponent transform = SandboxComponentRetriever.get(child, TransformComponent.class);
                DimensionsComponent dimensions = SandboxComponentRetriever.get(child, DimensionsComponent.class);
                float[] value = values.get(i);
                transform.x = value[0];
                transform.y = value[1];
                dimensions.width = value[2];
                dimensions.height = value[3];
                if (dimensions.boundBox != null && SandboxComponentRetriever.get(child, NodeComponent.class) != null) {
                    dimensions.boundBox.set(0, 0, value[2], value[3]);
                }

                AutoLayoutOwnSizeComponent own = SandboxComponentRetriever.get(child, AutoLayoutOwnSizeComponent.class);
                if (own == null && (!Float.isNaN(value[4]) || !Float.isNaN(value[5]))) {
                    own = PluginUIBridge.get().getSandbox().getEngine().edit(child).create(AutoLayoutOwnSizeComponent.class);
                }
                if (own != null) {
                    own.width = value[4];
                    own.height = value[5];
                }
            }
        }
    }
}
