package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.CompositeTransformComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.editor.view.ui.properties.panels.UIBasicItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.command.TransformCommandBuilder;
import org.puremvc.java.patterns.facade.Facade;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sasun Poghosyan on 4/13/2016.
 */
public class CompositeStrategy extends AbstractTransformStrategy {

    private HashMap<Integer, Vector2> childrenInitialPositions = new HashMap<>();
    private HashMap<Integer, Vector2> childrenFinalPositions = new HashMap<>();
    private final Array<Object[]> payloads = new Array<>();

    private final Vector2 parentInitialPosition = new Vector2();
    private final Vector2 parentInitialSize = new Vector2();

    private final Vector2 parentFinalPosition = new Vector2();
    private final Vector2 parentFinalSize = new Vector2();

    private static final float[] tmp1 = new float[3];
    private static final float[] tmp2 = new float[3];

    private final Facade facade = HyperLap2DFacade.getInstance();

    public void getInitialPositions(int entity) {
        getParentState(entity, parentInitialPosition, parentInitialSize);
        childrenInitialPositions.clear();
        getChildrenPositions(entity, childrenInitialPositions);
    }

    public void swapItemFinalAndInitialStates(int entity) {
        childrenFinalPositions.clear();
        getChildrenPositions(entity, childrenFinalPositions);
        getParentState(entity, parentFinalPosition, parentFinalSize);

        setEntityChildrenAtPositions(childrenInitialPositions);
        setParentState(entity, parentInitialPosition, parentInitialSize);

        sendResizePositionNotification(entity);
    }

    private void sendResizePositionNotification(int entity) {
        payloads.clear();
        payloads.add(parentEntity(entity));
        for (Map.Entry<Integer, Vector2> entrySet : childrenFinalPositions.entrySet()) {
            Object[] payload = new Object[2];
            payload[0] = EntityUtils.getByUniqueId(entrySet.getKey());
            payload[1] = entrySet.getValue();
            payloads.add(payload);
        }
        if (!parentFinalPosition.equals(parentInitialPosition) || !parentFinalSize.equals(parentInitialSize))
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_ITEM_AND_CHILDREN_TO, payloads);
    }

    private void setParentState(int entity, Vector2 position, Vector2 size) {
        EntityUtils.setPosition(entity, position);
        EntityUtils.setSize(entity, size);
    }

    private void getParentState(int entity, Vector2 position, Vector2 size) {
        EntityUtils.getPosition(entity, position);
        EntityUtils.getSize(entity, size);
    }

    private Object[] parentEntity(int entity) {
        Object[] obj = new Object[3];
        obj[0] = entity;
        obj[1] = new Vector2(parentFinalPosition);
        obj[2] = new Vector2(parentFinalSize);
        return obj;
    }

    private void getChildrenPositions(int parentEntity, HashMap<Integer, Vector2> entityPos) {
        NodeComponent nodeComponent = SandboxComponentRetriever.get(parentEntity, NodeComponent.class);
        if (nodeComponent != null) {
            for (int entity : nodeComponent.children) {
                TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
                Vector2 currentEntityPos = new Vector2(transformComponent.x, transformComponent.y);
                entityPos.put(EntityUtils.getEntityId(entity), currentEntityPos);
            }
        }
    }

    private void setEntityChildrenAtPositions(HashMap<Integer, Vector2> posMap) {
        for (Map.Entry<Integer, Vector2> entrySet : posMap.entrySet()) {
            Integer id = entrySet.getKey();
            Vector2 position = entrySet.getValue();
            int entity = EntityUtils.getByUniqueId(id);
            EntityUtils.setPosition(entity, position);
        }
    }

    @Override
    public void calculate(float mouseDx, float mouseDy, int anchor, int entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePointStage, float lastTransformAngle, float lastEntityAngle) {
        CompositeTransformComponent component = SandboxComponentRetriever.get(entity, CompositeTransformComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        float[] horizontal = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation, tmp1);
        float[] vertical = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation + 90, tmp2);
        float deltaW = horizontal[0] / transformComponent.scaleX;
        float deltaH = vertical[0] / transformComponent.scaleY;

        if (!component.automaticResize) {
            DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

            float newWidth = dimensionsComponent.width;
            float newHeight = dimensionsComponent.height;

            switch (anchor) {
                case NormalSelectionFollower.L:
                    float x = horizontal[1];
                    float y = horizontal[2];
                    move(entity, -deltaW, 0);
                    if (isShiftPressed()) {
                        deltaW *= 2;
                    }
                    newWidth = dimensionsComponent.width - deltaW;
                    transformComponent.x += x;
                    transformComponent.y += y;
                    break;
                case NormalSelectionFollower.R:
                    if (isShiftPressed()) {
                        move(entity, deltaW, 0);
                        deltaW *= 2;
                        transformComponent.x -= horizontal[1];
                        transformComponent.y -= horizontal[2];
                    }
                    newWidth = dimensionsComponent.width + deltaW;
                    break;
                case NormalSelectionFollower.B:
                    float x1 = vertical[1];
                    float y1 = vertical[2];
                    move(entity, 0, -deltaH);
                    if (isShiftPressed()) {
                        deltaH *= 2;
                    }
                    newHeight = dimensionsComponent.height - deltaH;
                    transformComponent.x += x1;
                    transformComponent.y += y1;
                    break;
                case NormalSelectionFollower.T:
                    if (isShiftPressed()) {
                        move(entity, 0, deltaH);
                        deltaH *= 2;
                        transformComponent.x -= vertical[1];
                        transformComponent.y -= vertical[2];
                    }
                    newHeight = dimensionsComponent.height + deltaH;
                    break;
                case NormalSelectionFollower.LT:
                    if (isShiftPressed()) {
                        move(entity, -deltaW, deltaH);
                        deltaW *= 2;
                        deltaH *= 2;
                        transformComponent.x -= vertical[1];
                        transformComponent.y -= vertical[2];
                    } else {
                        move(entity, -deltaW, 0);
                    }
                    newWidth = dimensionsComponent.width - deltaW;
                    newHeight = dimensionsComponent.height + deltaH;
                    transformComponent.x += horizontal[1];
                    transformComponent.y += horizontal[2];

                    break;
                case NormalSelectionFollower.RT:
                    if (isShiftPressed()) {
                        move(entity, deltaW, deltaH);
                        deltaH *= 2;
                        deltaW *= 2;
                        transformComponent.x -= horizontal[1];
                        transformComponent.y -= horizontal[2];
                        transformComponent.x -= vertical[1];
                        transformComponent.y -= vertical[2];
                    }
                    newWidth = dimensionsComponent.width + deltaW;
                    newHeight = dimensionsComponent.height + deltaH;
                    break;
                case NormalSelectionFollower.RB:
                    if (isShiftPressed()) {
                        move(entity, deltaW, -deltaH);
                        deltaW *= 2;
                        deltaH *= 2;
                        transformComponent.x -= horizontal[1];
                        transformComponent.y -= horizontal[2];
                    } else {
                        move(entity, 0, -deltaH);
                    }
                    newWidth = dimensionsComponent.width + deltaW;
                    newHeight = dimensionsComponent.height - deltaH;
                    transformComponent.x += vertical[1];
                    transformComponent.y += vertical[2];
                    break;
                case NormalSelectionFollower.LB:
                    move(entity, -deltaW, -deltaH);
                    if (isShiftPressed()) {
                        deltaW *= 2;
                        deltaH *= 2;
                    }
                    newWidth = dimensionsComponent.width - deltaW;
                    newHeight = dimensionsComponent.height - deltaH;
                    transformComponent.x += horizontal[1];
                    transformComponent.y += horizontal[2];
                    transformComponent.x += vertical[1];
                    transformComponent.y += vertical[2];
                    break;
            }
            dimensionsComponent.width = newWidth;
            dimensionsComponent.height = newHeight;
            dimensionsComponent.boundBox.width = newWidth;
            dimensionsComponent.boundBox.height = newHeight;
        }

        // Origin
        origin(deltaW, deltaH, anchor, transformComponent, transformCommandBuilder);

        // Rotating
        rotating(anchor, transformCommandBuilder, mousePointStage, lastTransformAngle, lastEntityAngle, transformComponent);
    }

    private boolean isShiftPressed() {
        UIBasicItemPropertiesMediator mediator = facade.retrieveMediator(UIBasicItemPropertiesMediator.NAME);
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
                || (mediator != null && mediator.isXYScaleLinked());
    }

    private void move(int node, float x, float y) {
        SnapshotArray<Integer> nodeEntity = SandboxComponentRetriever.get(node, NodeComponent.class).children;
        for (int child : nodeEntity) {
            TransformComponent transformComponent = SandboxComponentRetriever.get(child, TransformComponent.class);
            transformComponent.x += x;
            transformComponent.y += y;
        }
    }
}
