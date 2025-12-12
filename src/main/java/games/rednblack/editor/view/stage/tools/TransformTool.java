/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.stage.tools;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.util.OsUtils;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.tools.transformStrategy.*;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.FollowerTransformationListener;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.command.TransformCommandBuilder;
import games.rednblack.h2d.common.proxy.CursorManager;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.vo.CursorData;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/30/2015.
 */
public class TransformTool extends SelectionTool implements FollowerTransformationListener {

    public static final String NAME = "TRANSFORM_TOOL";
    public static final String prefix = "games.rednblack.editor.view.stage.tools.TransformTool";
    public static final String MANUAL_ORIGIN_POSITION = prefix + ".MANUAL_ORIGIN_POSITION";

    private float lastTransformAngle = 0;
    private float lastEntityAngle = 0;
    private final Vector2 mouseInitialCoordinates = new Vector2();
    private CursorManager cursorManager;
    private TransformCommandBuilder commandBuilder = new TransformCommandBuilder();

    private final BasicStrategy basicStrategy = new BasicStrategy();
    private final CompositeStrategy compositeStrategy = new CompositeStrategy();
    private final NinePatchStrategy ninePatchStrategy = new NinePatchStrategy();
    private final LabelStrategy labelStrategy = new LabelStrategy();
    private ITransformStrategy transformStrategy;

    private boolean fixCursor = false;

    public void execute(Vector2 mouseInitialCoordinates, Vector2 mousePointStage, int anchor, int entity) {
        float mouseDx = mousePointStage.x - mouseInitialCoordinates.x;
        float mouseDy = mousePointStage.y - mouseInitialCoordinates.y;

        transformStrategy.calculate(mouseDx, mouseDy, anchor, entity, commandBuilder, mousePointStage, lastTransformAngle, lastEntityAngle);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortcut() {
        return OsUtils.getShortcutFor(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.TRANSFORM_TOOL));
    }

    @Override
    public String getTitle() {
        return "Transform Tool";
    }

    @Override
    public void initTool() {
        super.initTool();

        if (!sandbox.getSelector().selectionIsOneItem()) {
            sandbox.getSelector().clearSelections();
        }

        updateListeners();

        // set cursor
        cursorManager = Facade.getInstance().retrieveProxy(CursorManager.NAME);
        cursorManager.setCursor(Cursors.CROSS);
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case MsgAPI.NEW_ITEM_ADDED:
                updateListeners((int) notification.getBody());
                break;
        }
    }

    @Override
    public void stageMouseUp(float x, float y) {
        super.stageMouseUp(x, y);
        updateListeners();
    }

    @Override
    public void itemMouseUp(int entity, float x, float y) {
        super.itemMouseUp(entity, x, y);
        updateListeners();
    }

    private void updateListeners() {
        Sandbox sandbox = Sandbox.getInstance();
        Set<Integer> selectedEntities = sandbox.getSelector().getSelectedItems();
        updateListeners(selectedEntities);
    }

    private void updateListeners(int entity) {
        Set<Integer> entities = new HashSet<>();
        entities.add(entity);
        updateListeners(entities);
    }

    private void updateListeners(Set<Integer> entities) {
        FollowersUIMediator followersUIMediator = Facade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        followersUIMediator.clearAllListeners();

        for (int entity : entities) {
            if (followersUIMediator.getFollower(entity) != null)
                followersUIMediator.getFollower(entity).setFollowerListener(this);
        }
    }

    @Override
    public void anchorDown(NormalSelectionFollower follower, int anchor, float x, float y) {
        fixCursor = true;

        Sandbox sandbox = Sandbox.getInstance();

        commandBuilder.begin(follower.getEntity(), Sandbox.getInstance().getEngine());

        TransformComponent transformComponent = SandboxComponentRetriever.get(follower.getEntity(), TransformComponent.class);
        Vector2 mousePoint = sandbox.screenToWorld(x, y);
        mouseInitialCoordinates.set(mousePoint.x, mousePoint.y);

        switch (EntityUtils.getType(follower.getEntity())) {
            case EntityFactory.COMPOSITE_TYPE:
                transformStrategy = compositeStrategy;
                compositeStrategy.getInitialPositions(follower.getEntity());
                break;
            case EntityFactory.NINE_PATCH:
                transformStrategy = ninePatchStrategy;
                break;
            case EntityFactory.LABEL_TYPE:
                transformStrategy = labelStrategy;
                break;
            /*case EntityFactory.COLOR_PRIMITIVE:
                transformStrategy = colorPrimitiveStrategy;
                break;*/
            default:
                transformStrategy = basicStrategy;
                break;
        }

        commandBuilder.begin(follower.getEntity(), sandbox.getEngine());

        if (anchor == NormalSelectionFollower.ROTATION_LT ||
                anchor == NormalSelectionFollower.ROTATION_RT ||
                anchor == NormalSelectionFollower.ROTATION_RB ||
                anchor == NormalSelectionFollower.ROTATION_LB) {

            // get mouse stage coordinates
            Vector2 originPoint = new Vector2(transformComponent.x + transformComponent.originX, transformComponent.y + transformComponent.originY);
            mousePoint.sub(originPoint);
            //origin related
            lastTransformAngle = mousePoint.angleDeg();
            lastEntityAngle = transformComponent.rotation;
        }
    }

    @Override
    public void anchorUp(NormalSelectionFollower follower, int anchor, int button, float x, float y) {
        fixCursor = false;

        if (anchor == NormalSelectionFollower.ORIGIN && button == Input.Buttons.RIGHT) {
            Facade.getInstance().sendNotification(MANUAL_ORIGIN_POSITION, follower.getEntity());
            return;
        }

        commandBuilder.execute(Facade.getInstance());
        if (transformStrategy == compositeStrategy) {
            compositeStrategy.swapItemFinalAndInitialStates(follower.getEntity());
        }

        cursorManager.setCursor(Cursors.CROSS);
        cursorManager.displayCustomCursor();
    }

    @Override
    public void anchorDragged(NormalSelectionFollower follower, int anchor, float x, float y) {
        Sandbox sandbox = Sandbox.getInstance();

        Vector2 mousePointStage = sandbox.screenToWorld(x, y);
        execute(mouseInitialCoordinates, mousePointStage, anchor, follower.getEntity());
        mouseInitialCoordinates.set(mousePointStage.x, mousePointStage.y);

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED);
    }

    @Override
    public void anchorMouseEnter(NormalSelectionFollower follower, int anchor, float x, float y) {
        if (fixCursor) {
            cursorManager.displayCustomCursor();
            return;
        }

        TransformComponent transformComponent = SandboxComponentRetriever.get(follower.getEntity(), TransformComponent.class);
        float entityRotation = transformComponent.rotation;

        // Determine if we are operating on a rotation cursor or a resizing cursor
        boolean isRotationTool = (anchor >= NormalSelectionFollower.ROTATION_LT && anchor <= NormalSelectionFollower.ROTATION_LB);

        // Map the logical anchor (e.g., "Top") to its base angle (e.g., 90 degrees)
        float baseAngle = getBaseAngleForAnchor(anchor);
        CursorData cursorId = Cursors.NORMAL;
        if (baseAngle >= 0) {
            // If Flip X is active, mirror the angle horizontally (across Y-axis): 45 becomes 135.
            float sX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
            float sY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);
            if (sX < 0) {
                baseAngle = 180f - baseAngle;
            }

            // If Flip Y is active, mirror the angle vertically (across X-axis): 45 becomes -45 (315).
            if (sY < 0) {
                baseAngle = -baseAngle;
            }

            // Calculate the final visual angle by adding the entity's rotation
            float visualAngle = baseAngle + entityRotation;
            // Get the correct cursor based on the resulting visual angle
            cursorId = getCursorForAngle(visualAngle, isRotationTool);
        }

        cursorManager.setCursor(cursorId);
        cursorManager.displayCustomCursor();
    }

    /**
     * Converts the anchor ID to its standard polar angle (0 = Right, 90 = Top, etc.)
     */
    private float getBaseAngleForAnchor(int anchor) {
        switch (anchor) {
            case NormalSelectionFollower.R:
                return 0;
            case NormalSelectionFollower.RT:
            case NormalSelectionFollower.ROTATION_RT:
                return 45;
            case NormalSelectionFollower.T:
                return 90;
            case NormalSelectionFollower.LT:
            case NormalSelectionFollower.ROTATION_LT:
                return 135;
            case NormalSelectionFollower.L:
                return 180;
            case NormalSelectionFollower.LB:
            case NormalSelectionFollower.ROTATION_LB:
                return 225;
            case NormalSelectionFollower.B:
                return 270;
            case NormalSelectionFollower.RB:
            case NormalSelectionFollower.ROTATION_RB:
                return 315;
            default:
                return -1;
        }
    }

    /**
     * Calculates which cursor to display based on the actual visual angle on screen.
     */
    private CursorData getCursorForAngle(float angle, boolean isRotation) {
        // Normalize the angle between 0 and 360
        float normalized = angle % 360;
        if (normalized < 0) normalized += 360;

        // Divide the wheel into 8 sectors of 45 degrees each.
        // Add 22.5 to rotate the snapping grid so that 0 degrees is the center of sector 0.
        int sector = (int) ((normalized + 22.5f) / 45f) % 8;

        if (isRotation) {
            // Mapping for rotation cursors (curved icons)
            // Sectors: 0=R, 1=RT, 2=T, 3=LT, 4=L, 5=LB, 6=B, 7=RB
            // We map sectors to the 4 available rotation corner cursors.
            switch (sector) {
                case 0: // Right -> Use RB or RT?
                case 1: return Cursors.ROTATION_RT; // North-East
                case 2: // Top
                case 3: return Cursors.ROTATION_LT; // North-West
                case 4: // Left
                case 5: return Cursors.ROTATION_LB; // South-West
                case 6: // Bottom
                case 7: return Cursors.ROTATION_RB; // South-East
                default: return Cursors.ROTATION_RB;
            }
        } else {
            // Mapping for resizing cursors (arrows)
            switch (sector) {
                case 0: return Cursors.TRANSFORM_HORIZONTAL; // 0 degrees (Right)
                case 1: return Cursors.TRANSFORM_LEFT_RIGHT; // 45 degrees (Top-Right / Bottom-Left) [ / ]
                case 2: return Cursors.TRANSFORM_VERTICAL;   // 90 degrees (Top)
                case 3: return Cursors.TRANSFORM_RIGHT_LEFT; // 135 degrees (Top-Left / Bottom-Right) [ \ ]
                case 4: return Cursors.TRANSFORM_HORIZONTAL; // 180 degrees (Left)
                case 5: return Cursors.TRANSFORM_LEFT_RIGHT; // 225 degrees [ / ]
                case 6: return Cursors.TRANSFORM_VERTICAL;   // 270 degrees (Bottom)
                case 7: return Cursors.TRANSFORM_RIGHT_LEFT; // 315 degrees [ \ ]
                default: return Cursors.NORMAL;
            }
        }
    }

    @Override
    public void anchorMouseExit(NormalSelectionFollower follower, int anchor, float x, float y) {
        if (fixCursor)
            return;

        cursorManager.setCursor(Cursors.CROSS);
        cursorManager.displayCustomCursor();
    }
}
