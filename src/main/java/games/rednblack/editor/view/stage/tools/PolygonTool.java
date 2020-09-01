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

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonDataCommand;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.editor.view.ui.followers.PolygonTransformationListener;
import org.puremvc.java.interfaces.INotification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 7/2/2015.
 */
public class PolygonTool extends SelectionTool implements PolygonTransformationListener {

    public static final String NAME = "MESH_TOOL";
    public static final String prefix = "games.rednblack.editor.view.stage.tools";
    public static final String MANUAL_VERTEX_POSITION = prefix + ".MANUAL_VERTEX_POSITION";

    private FollowersUIMediator followersUIMediator;

    private Vector2 dragLastPoint;

    private Object[] currentCommandPayload;

    private PolygonFollower lastSelectedMeshFollower = null;
    private Vector2[][] polygonBackup = null;

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
                updateSubFollowerList();
                break;
            case RemoveComponentFromItemCommand.DONE:
                updateSubFollowerList();
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                updateSubFollowerList();
                break;
            case MsgAPI.SCENE_LOADED:
                updateSubFollowerList();
                break;
        }
    }

    @Override
    public boolean itemMouseDown(Entity entity, float x, float y) {
        lastSelectedMeshFollower = getMeshFollower(entity);
        return super.itemMouseDown(entity, x, y);
    }

    private void setListener(PolygonFollower meshFollower) {
        meshFollower.setListener(this);
    }

    private void updateSubFollowerList() {
        Sandbox sandbox = Sandbox.getInstance();
        Set<Entity> selectedEntities = sandbox.getSelector().getSelectedItems();
        for(Entity entity: selectedEntities) {
            BasicFollower follower = followersUIMediator.getFollower(entity);
            follower.removeSubFollower(PolygonFollower.class);
            PolygonFollower meshFollower = new PolygonFollower(entity);
            follower.addSubfollower(meshFollower);
            setListener(meshFollower);
            lastSelectedMeshFollower = meshFollower;
        }
    }

    @Override
    public void vertexUp(PolygonFollower follower, int vertexIndex, float x, float y) {

    }

    @Override
    public void vertexDown(PolygonFollower follower, int vertexIndex, float x, float y) {
        PolygonComponent polygonComponent = ComponentRetriever.get(follower.getEntity(), PolygonComponent.class);
        currentCommandPayload = UpdatePolygonDataCommand.payloadInitialState(follower.getEntity());

        follower.getOriginalPoints().add(vertexIndex, new Vector2(x, y));
        Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);

        polygonComponent.vertices = PolygonUtils.polygonize(points);
        follower.updateDraw();

        follower.draggingAnchorId = vertexIndex;
        dragLastPoint = new Vector2(x, y);
        follower.setSelectedAnchor(vertexIndex);
        lastSelectedMeshFollower = follower;

        if (polygonComponent.vertices != null) {
            polygonBackup = polygonComponent.vertices.clone();
        }
    }

    @Override
    public void VertexMouseOver(PolygonFollower follower, int vertexIndex, float x, float y) {

    }

    @Override
    public void anchorDown(PolygonFollower follower, int anchor, float x, float y) {
        dragLastPoint = new Vector2(x, y);
        currentCommandPayload = UpdatePolygonDataCommand.payloadInitialState(follower.getEntity());
        follower.setSelectedAnchor(anchor);
        lastSelectedMeshFollower = follower;

        PolygonComponent polygonComponent = ComponentRetriever.get(follower.getEntity(), PolygonComponent.class);
        polygonBackup = polygonComponent.vertices.clone();
    }

    @Override
    public void anchorDragged(PolygonFollower follower, int anchor, float x, float y) {
        PolygonComponent polygonComponent = ComponentRetriever.get(follower.getEntity(), PolygonComponent.class);

        Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);
        Vector2 diff = dragLastPoint.sub(x, y);
        points[anchor].sub(diff);
        dragLastPoint = new Vector2(x, y);

        // check if any of near lines intersect
        int[] intersections = PolygonUtils.checkForIntersection(anchor, points);
        if(intersections == null) {
            polygonComponent.vertices = PolygonUtils.polygonize(points);
            follower.setProblems(null);
        } else {
            follower.setProblems(intersections);
        }

        follower.updateDraw();
    }

    @Override
    public void anchorUp(PolygonFollower follower, int anchor, float x, float y) {
        PolygonComponent polygonComponent = ComponentRetriever.get(follower.getEntity(), PolygonComponent.class);

        Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);

        int[] intersections = PolygonUtils.checkForIntersection(anchor, points);
        if(intersections == null) {
            if(PolygonUtils.isPolygonCCW(points)){
                Collections.reverse(follower.getOriginalPoints());
                points = follower.getOriginalPoints().toArray(new Vector2[0]);
            }
            polygonComponent.vertices = PolygonUtils.polygonize(points);
        }

        if(polygonComponent.vertices == null) {
            // restore from backup
            polygonComponent.vertices = polygonBackup.clone();
        } else if(intersections != null) {
            polygonComponent.vertices = polygonBackup.clone();
        }

        follower.setProblems(null);

        currentCommandPayload = UpdatePolygonDataCommand.payload(currentCommandPayload, polygonComponent.vertices);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);
    }

    @Override
    public void keyDown(Entity entity, int keycode) {
        if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
            if(!deleteSelectedAnchor()) {
                super.keyDown(entity, keycode);
            }
        } else {
            super.keyDown(entity, keycode);
        }
    }

    private PolygonFollower getMeshFollower(Entity entity) {
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        BasicFollower follower = followersUIMediator.getFollower(entity);

        PolygonFollower meshFollower = (PolygonFollower) (follower).getSubFollower(PolygonFollower.class);
        return meshFollower;
    }

    private boolean deleteSelectedAnchor() {
        PolygonFollower follower = lastSelectedMeshFollower;
        PolygonComponent polygonComponent = ComponentRetriever.get(follower.getEntity(), PolygonComponent.class);
        if(follower != null) {
            if(polygonComponent == null || polygonComponent.vertices == null || polygonComponent.vertices.length == 0) return false;
            if(follower.getOriginalPoints().size() <= 3) return false;

            polygonBackup = polygonComponent.vertices.clone();
            currentCommandPayload = UpdatePolygonDataCommand.payloadInitialState(follower.getEntity());

            follower.getOriginalPoints().remove(follower.getSelectedAnchorId());
            follower.getSelectedAnchorId(follower.getSelectedAnchorId()-1);
            Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);
            polygonComponent.vertices = PolygonUtils.polygonize(points);

            if(polygonComponent.vertices == null) {
                // restore from backup
                polygonComponent.vertices = polygonBackup.clone();
                follower.update();
            }

            currentCommandPayload = UpdatePolygonDataCommand.payload(currentCommandPayload, polygonComponent.vertices);
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);

            follower.updateDraw();

            return true;
        }

        return false;
    }
}
