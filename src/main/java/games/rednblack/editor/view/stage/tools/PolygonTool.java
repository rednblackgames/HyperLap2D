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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonVerticesCommand;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.editor.view.ui.followers.PolygonTransformationListener;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;

import java.util.Set;

/**
 * Created by azakhary on 7/2/2015.
 *
 */
public class PolygonTool extends SelectionTool implements PolygonTransformationListener {

    public static final String NAME = "MESH_TOOL";
    public static final String prefix = "games.rednblack.editor.view.stage.tools.PolygonTool";
    public static final String MANUAL_VERTEX_POSITION = prefix + ".MANUAL_VERTEX_POSITION";

    private FollowersUIMediator followersUIMediator;

    private final Vector2 dragLastPoint = new Vector2();

    private Object[] currentCommandPayload;

    private PolygonFollower lastSelectedMeshFollower = null;
    private Vector2[][] polygonizedVerticesBackup = null;
    private Array<Vector2> verticesBackup = null;

    private final IntSet intersectionProblems = new IntSet();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortcut() {
        return null;
    }

    @Override
    public String getTitle() {
        return "Polygon Tool";
    }

    @Override
    public void initTool() {
        super.initTool();

        followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);

        updateSubFollowerList();
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case AddComponentToItemCommand.DONE:
            case RemoveComponentFromItemCommand.DONE:
            case MsgAPI.ITEM_SELECTION_CHANGED:
            case MsgAPI.SCENE_LOADED:
                updateSubFollowerList();
                break;
        }
    }

    @Override
    public boolean itemMouseDown(int entity, float x, float y) {
        lastSelectedMeshFollower = getMeshFollower(entity);
        return super.itemMouseDown(entity, x, y);
    }

    private void setListener(PolygonFollower meshFollower) {
        meshFollower.setListener(this);
    }

    private void updateSubFollowerList() {
        Sandbox sandbox = Sandbox.getInstance();
        Set<Integer> selectedEntities = sandbox.getSelector().getSelectedItems();
        for(int entity: selectedEntities) {
            BasicFollower follower = followersUIMediator.getFollower(entity);
            if (follower == null) continue;
            follower.update();
            follower.removeSubFollower(PolygonFollower.class);
            PolygonFollower meshFollower = new PolygonFollower(entity);
            follower.addSubfollower(meshFollower);
            setListener(meshFollower);
            lastSelectedMeshFollower = meshFollower;
        }
    }

    @Override
    public void vertexUp(PolygonFollower follower, int vertexIndex, float x, float y) {
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);

        Array<Vector2> points = polygonShapeComponent.vertices;

        if (!polygonShapeComponent.openEnded) {
            IntSet intersections = PolygonUtils.checkForIntersection(vertexIndex, points, intersectionProblems);
            if(intersections == null) {
                if(PolygonUtils.isPolygonCCW(points.toArray())){
                    points.reverse();
                }
                polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(points.toArray());
            } else {
                // restore from backup
                polygonShapeComponent.vertices = UpdatePolygonVerticesCommand.cloneData(verticesBackup);
                polygonShapeComponent.polygonizedVertices = UpdatePolygonVerticesCommand.cloneData(polygonizedVerticesBackup);
            }
        }

        follower.setProblems(null);

        UpdatePolygonVerticesCommand.payload(currentCommandPayload, polygonShapeComponent.vertices, polygonShapeComponent.polygonizedVertices);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);
    }

    @Override
    public void vertexDown(PolygonFollower follower, int vertexIndex, float x, float y) {
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);
        currentCommandPayload = UpdatePolygonVerticesCommand.payloadInitialState(follower.getEntity());

        polygonShapeComponent.vertices.insert(vertexIndex, new Vector2(x, y));
        if (!polygonShapeComponent.openEnded)
            polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(polygonShapeComponent.vertices.toArray());
        follower.update();

        follower.draggingAnchorId = vertexIndex;
        dragLastPoint.set(x, y);
        follower.setSelectedAnchor(vertexIndex);
        lastSelectedMeshFollower = follower;

        verticesBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.vertices);
        if (polygonShapeComponent.polygonizedVertices != null) {
            polygonizedVerticesBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.polygonizedVertices);
        }
    }

    @Override
    public void anchorDown(PolygonFollower follower, int anchor, float x, float y) {
        dragLastPoint.set(x, y);
        currentCommandPayload = UpdatePolygonVerticesCommand.payloadInitialState(follower.getEntity());
        follower.setSelectedAnchor(anchor);
        lastSelectedMeshFollower = follower;

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);
        polygonizedVerticesBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.polygonizedVertices);
        verticesBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.vertices);
    }

    @Override
    public void anchorDragged(PolygonFollower follower, int anchor, float x, float y) {
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);

        Array<Vector2> points = polygonShapeComponent.vertices;
        Vector2 diff = dragLastPoint.sub(x, y);
        points.get(anchor).sub(diff);
        dragLastPoint.set(x, y);

        if (!polygonShapeComponent.openEnded) {
            // check if any of near lines intersect
            IntSet intersections = PolygonUtils.checkForIntersection(anchor, points, intersectionProblems);
            if(intersections == null) {
                polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(points.toArray());
                follower.setProblems(null);
            } else {
                follower.setProblems(intersections);
            }
        }

        follower.update();
    }

    @Override
    public void anchorUp(PolygonFollower follower, int anchor, int button, float x, float y) {
        if (button == Input.Buttons.RIGHT && anchor >= 0) {
            follower.setSelectedAnchor(anchor);
            Object[] payload = new Object[2];
            payload[0] = follower;
            payload[1] = anchor;
            HyperLap2DFacade.getInstance().sendNotification(PolygonTool.MANUAL_VERTEX_POSITION, payload);
            return;
        }

        vertexUp(follower, anchor, x, y);
    }

    @Override
    public void keyDown(int entity, int keycode) {
        if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
            if(!deleteSelectedAnchor()) {
                super.keyDown(entity, keycode);
            }
        } else {
            super.keyDown(entity, keycode);
        }
    }

    private PolygonFollower getMeshFollower(int entity) {
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        BasicFollower follower = followersUIMediator.getFollower(entity);

        PolygonFollower meshFollower = (PolygonFollower) (follower).getSubFollower(PolygonFollower.class);
        return meshFollower;
    }

    private boolean deleteSelectedAnchor() {
        PolygonFollower follower = lastSelectedMeshFollower;
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);
        if(follower != null && follower.getSelectedAnchorId() != -1) {
            if(polygonShapeComponent == null || polygonShapeComponent.vertices == null || polygonShapeComponent.vertices.size == 0) return false;
            if( polygonShapeComponent.vertices.size <= 3) return false;

            verticesBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.vertices);
            currentCommandPayload = UpdatePolygonVerticesCommand.payloadInitialState(follower.getEntity());

            polygonShapeComponent.vertices.removeIndex(follower.getSelectedAnchorId());
            follower.setSelectedAnchor(follower.getSelectedAnchorId() - 1);

            if (!polygonShapeComponent.openEnded) {
                polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(polygonShapeComponent.vertices.toArray());

                if(polygonShapeComponent.polygonizedVertices == null) {
                    // restore from backup
                    polygonShapeComponent.vertices = UpdatePolygonVerticesCommand.cloneData(verticesBackup);
                    polygonShapeComponent.polygonizedVertices = UpdatePolygonVerticesCommand.cloneData(polygonizedVerticesBackup);
                    follower.update();
                }
            }

            UpdatePolygonVerticesCommand.payload(currentCommandPayload, polygonShapeComponent.vertices, polygonShapeComponent.polygonizedVertices);
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);

            follower.update();

            return true;
        }

        return false;
    }
}
