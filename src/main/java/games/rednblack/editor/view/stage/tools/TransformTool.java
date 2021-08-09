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
import games.rednblack.editor.HyperLap2DFacade;
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
import org.puremvc.java.interfaces.INotification;

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
    private final ColorPrimitiveStrategy colorPrimitiveStrategy = new ColorPrimitiveStrategy();
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
        cursorManager = HyperLap2DFacade.getInstance().retrieveProxy(CursorManager.NAME);
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
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
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
            HyperLap2DFacade.getInstance().sendNotification(MANUAL_ORIGIN_POSITION, follower.getEntity());
            return;
        }

        commandBuilder.execute(HyperLap2DFacade.getInstance());
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

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED);
    }

    @Override
    public void anchorMouseEnter(NormalSelectionFollower follower, int anchor, float x, float y) {
        if (fixCursor){
            cursorManager.displayCustomCursor();
            return;
        }

        switch (anchor) {
            case NormalSelectionFollower.ROTATION_LB:
                cursorManager.setCursor(Cursors.ROTATION_LB);
                break;
            case NormalSelectionFollower.ROTATION_LT:
                cursorManager.setCursor(Cursors.ROTATION_LT);
                break;
            case NormalSelectionFollower.ROTATION_RT:
                cursorManager.setCursor(Cursors.ROTATION_RT);
                break;
            case NormalSelectionFollower.ROTATION_RB:
                cursorManager.setCursor(Cursors.ROTATION_RB);
                break;
            case NormalSelectionFollower.LB:
                cursorManager.setCursor(Cursors.TRANSFORM_LEFT_RIGHT);
                break;
            case NormalSelectionFollower.L:
                cursorManager.setCursor(Cursors.TRANSFORM_HORIZONTAL);
                break;
            case NormalSelectionFollower.LT:
                cursorManager.setCursor(Cursors.TRANSFORM_RIGHT_LEFT);
                break;
            case NormalSelectionFollower.T:
                cursorManager.setCursor(Cursors.TRANSFORM_VERTICAL);
                break;
            case NormalSelectionFollower.RT:
                cursorManager.setCursor(Cursors.TRANSFORM_LEFT_RIGHT);
                break;
            case NormalSelectionFollower.R:
                cursorManager.setCursor(Cursors.TRANSFORM_HORIZONTAL);
                break;
            case NormalSelectionFollower.RB:
                cursorManager.setCursor(Cursors.TRANSFORM_RIGHT_LEFT);
                break;
            case NormalSelectionFollower.B:
                cursorManager.setCursor(Cursors.TRANSFORM_VERTICAL);
                break;
            default:
                cursorManager.setCursor(Cursors.NORMAL);
                break;
        }
        cursorManager.displayCustomCursor();
    }

    @Override
    public void anchorMouseExit(NormalSelectionFollower follower, int anchor, float x, float y) {
        if (fixCursor)
            return;

        cursorManager.setCursor(Cursors.CROSS);
        cursorManager.displayCustomCursor();
    }
}
