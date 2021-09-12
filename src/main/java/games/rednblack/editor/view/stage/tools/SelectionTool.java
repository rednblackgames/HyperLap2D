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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.util.OsUtils;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.EntityBounds;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.proxy.CursorManager;
import games.rednblack.h2d.common.view.ui.Cursors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/30/2015.
 */ //TODO all the new Vector2 instances should be replaced by tmp instances
public class SelectionTool extends SimpleTool {

    public static final String NAME = "SELECTION_TOOL";

    protected Sandbox sandbox;

    private boolean isDragging = false;
    private boolean currentTouchedItemWasSelected = false;
    private boolean isCastingRectangle = false;

    private Rectangle tmp = new Rectangle();
    private final EntityBounds tmpEntityBounds = new EntityBounds();
    private final float[] draggedRectanglePoints = new float[8];
    private final Polygon tmpPolygon1 = new Polygon();
    private final Polygon tmpPolygon2 = new Polygon();

    private Vector2 directionVector = null;

    private Vector2 dragMouseStartPosition;
    private HashMap<Integer, Vector2> dragStartPositions = new HashMap<>();
    private HashMap<Integer, Vector2> dragTouchDiff = new HashMap<>();

    private TransformComponent transformComponent;

    private DimensionsComponent dimensionsComponent;

    public SelectionTool() {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortcut() {
        return OsUtils.getShortcutFor(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.SELECTION_TOOL));
    }

    @Override
    public String getTitle() {
        return "Selection Tool";
    }

    @Override
    public void initTool() {
        super.initTool();
        sandbox = Sandbox.getInstance();

        // set cursor
        CursorManager cursorManager = HyperLap2DFacade.getInstance().retrieveProxy(CursorManager.NAME);
        cursorManager.setCursor(Cursors.NORMAL);
    }

    @Override
    public boolean stageMouseDown(float x, float y) {
        sandbox = Sandbox.getInstance();

        // transform stage coordinates to screen coordinates
        Vector2 screenCoords = Sandbox.getInstance().worldToScreen(x, y);

        // preparing selection tool rectangle to follow mouse
        sandbox.prepareSelectionRectangle(screenCoords.x, screenCoords.y);
        return true;
    }

    @Override
    public void stageMouseUp(float x, float y) {
        // selection is complete, this will check for what get caught in selection rect, and select 'em
        selectionComplete();

        isCastingRectangle = false;
    }

    @Override
    public void stageMouseDragged(float x, float y) {
        sandbox = Sandbox.getInstance();
        isCastingRectangle = true;

        // transform stage coordinates to screen coordinates
        Vector2 screenCoords = Sandbox.getInstance().worldToScreen(x, y);

        sandbox.selectionRec.setWidth(screenCoords.x - sandbox.selectionRec.getX());
        sandbox.selectionRec.setHeight(screenCoords.y - sandbox.selectionRec.getY());
    }

    @Override
    public void stageMouseDoubleClick(float x, float y) {
        int currentView = sandbox.getCurrentViewingEntity();
        if (currentView == -1)
            return;
        ParentNodeComponent parentNodeComponent = SandboxComponentRetriever.get(currentView, ParentNodeComponent.class);
        if (parentNodeComponent != null) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, parentNodeComponent.parentEntity);
        }
    }

    @Override
    public boolean itemMouseDown(int entity, float x, float y) {
        isItemDown = true;
        sandbox = Sandbox.getInstance();
        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();

        currentTouchedItemWasSelected = sandbox.getSelector().getCurrentSelection().contains(entity);

        // if shift is pressed we are in add/remove selection mode
        if (isShiftPressed()) {
            if (!currentTouchedItemWasSelected) {
                // item was not selected, adding it to selection
                Set<Integer> items = new HashSet<>();
                items.add(entity);
                facade.sendNotification(MsgAPI.ACTION_ADD_SELECTION, items);
            }
        } else {
            if (!currentTouchedItemWasSelected) {
                // get selection, add this item to selection
                Set<Integer> items = new HashSet<>();
                items.add(entity);
                facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, items);
            }
        }

        // remembering local touch position for each of selected boxes, if planning to drag
        dragStartPositions.clear();
        dragTouchDiff.clear();
        for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
            transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);
            if (transformComponent == null)
                continue;

            dragTouchDiff.put(itemInstance, new Vector2(x - transformComponent.x, y - transformComponent.y));
            dragStartPositions.put(itemInstance, new Vector2(transformComponent.x, transformComponent.y));
        }

        dragMouseStartPosition = new Vector2(x, y);

        // pining UI to update current item properties tools
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);

        return true;
    }

    private boolean isItemDown = false;

    @Override
    public void itemMouseDragged(int entity, float x, float y) {
        sandbox = Sandbox.getInstance();

        if (!isDragging && (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))) { // first drag iteration and is copy mode
            // we need to copy/paste the item in place, the set it as selection and draggable, then perform the drag.
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_COPY);
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_PASTE);

            dragStartPositions.clear();
            dragTouchDiff.clear();
            for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
                transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);

                dragTouchDiff.put(itemInstance, new Vector2(x - transformComponent.x, y - transformComponent.y));
                dragStartPositions.put(itemInstance, new Vector2(transformComponent.x, transformComponent.y));
            }

            dragMouseStartPosition = new Vector2(x, y);

            // pining UI to update current item properties tools
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);

        }


        isDragging = true;

        float gridSize = Sandbox.getInstance().getWorldGridSize();

        if (isShiftPressed()) {
            // check if we have a direction vector
            if (directionVector == null) {
                directionVector = new Vector2();
                if (Math.abs(x - dragMouseStartPosition.x) >= Math.abs(y - dragMouseStartPosition.y)) {
                    directionVector.x = 1;
                    directionVector.y = 0;
                } else {
                    directionVector.x = 0;
                    directionVector.y = 1;
                }
            }
        } else {
            directionVector = null;
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            float newX;
            float newY;

            newX = MathUtils.floor(x / gridSize) * gridSize;
            newY = MathUtils.floor(y / gridSize) * gridSize;

            // Selection rectangles should move and follow along
            for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
                transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);

                if (dragTouchDiff.get(itemInstance) == null)
                    continue;
                Vector2 diff = new Vector2(dragTouchDiff.get(itemInstance));
                diff.x = MathUtils.floor(diff.x / gridSize) * gridSize;
                diff.y = MathUtils.floor(diff.y / gridSize) * gridSize;

                if (isShiftPressed()) {
                    if (directionVector.x == 0) {
                        transformComponent.y = (newY - diff.y);
                    } else if (directionVector.y == 0) {
                        transformComponent.x = (newX - diff.x);
                    }
                } else {
                    transformComponent.x = (newX - diff.x);
                    transformComponent.y = (newY - diff.y);
                }
                //value.hide();

                // pining UI to update current item properties tools
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, itemInstance);
            }
        }

    }

    @Override
    public boolean stageMouseScrolled(float amountX, float amountY) {
        if (isItemDown) {
            for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
                transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);

                float degreeAmount = 3;
                if (amountX < 0 || amountY < 0) degreeAmount = -3;
                // And if shift is pressed, the rotation amount is bigger
                if (isShiftPressed()) {
                    degreeAmount = degreeAmount * 15;
                }

                transformComponent.rotation = (transformComponent.rotation + degreeAmount) % 360;
                // pining UI to update current item properties tools
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, itemInstance);
            }
        }

        return isItemDown;
    }

    @Override
    public void itemMouseUp(int entity, float x, float y) {
        isItemDown = false;
        sandbox = Sandbox.getInstance();
        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();

        if (currentTouchedItemWasSelected && !isDragging) {
            // item was selected (and no dragging was performed), so we need to release it
            if (isShiftPressed()) {
                Set<Integer> items = new HashSet<>();
                items.add(entity);
                facade.sendNotification(MsgAPI.ACTION_RELEASE_SELECTION, items);
            }
        }

        // if we were dragging, need to remember new position
        if (isDragging) {
            // sets item position, and puts things into undo-redo que
            Array<Object[]> payloads = new Array<>();
            for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
                transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);
                if (transformComponent == null)
                    continue;
                Vector2 newPosition = new Vector2(transformComponent.x, transformComponent.y);
                Vector2 oldPosition = dragStartPositions.get(itemInstance);

                Object[] payload = new Object[3];
                payload[0] = itemInstance;
                payload[1] = newPosition;
                payload[2] = oldPosition;
                payloads.add(payload);
            }

            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_ITEMS_MOVE_TO, payloads);
        }

        isDragging = false;
        directionVector = null;
    }

    @Override
    public void itemMouseDoubleClick(int item, float x, float y) {
        if (sandbox.getSelector().selectionIsComposite()) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, item);
        }
    }

    private boolean isShiftPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }


    private void selectionComplete() {
        sandbox = Sandbox.getInstance();

        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        OrthographicCamera camera = Sandbox.getInstance().getCamera();
        Viewport viewport = Sandbox.getInstance().getViewport();


        HashSet<Integer> freeItems = sandbox.getSelector().getAllFreeItems();

        // when touch is up, selection process stops, and if any items got "caught" in they should be selected.

        // hiding selection rectangle
        sandbox.selectionRec.setOpacity(0.0f);
        //ArrayList<Entity> curr = new ArrayList<Entity>();
        Set<Integer> curr = new HashSet<>();
        Rectangle sR = sandbox.screenToWorld(sandbox.selectionRec.getRect());

        draggedRectanglePoints[0] = sR.x;
        draggedRectanglePoints[1] = sR.y;
        draggedRectanglePoints[2] = sR.x + sR.width;
        draggedRectanglePoints[3] = sR.y;
        draggedRectanglePoints[4] = sR.x + sR.width;
        draggedRectanglePoints[5] = sR.y + sR.height;
        draggedRectanglePoints[6] = sR.x;
        draggedRectanglePoints[7] = sR.y + sR.height;

        for (int entity : freeItems) {
            transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
            dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

            //if (!freeItems.get(i).isLockedByLayer() && Intersector.overlaps(sR, new Rectangle(entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight()))) {

            tmpPolygon1.setVertices(draggedRectanglePoints);
            tmpPolygon2.setVertices(tmpEntityBounds.getBoundPoints(entity));
            boolean intersects = Intersector.overlapConvexPolygons(tmpPolygon1, tmpPolygon2);
            if (isEntityVisible(entity) && intersects) {
                curr.add(entity);
            }
        }

        if (curr.size() == 0) {
            facade.sendNotification(MsgAPI.EMPTY_SPACE_CLICKED);

            //remove visual selection command
            facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, null);
            return;
        }

        facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, curr);
    }

    private boolean isEntityVisible(int e) {
        LayerItemVO layer = EntityUtils.getEntityLayer(e);

        return (layer == null || layer.isVisible);
    }

    @Override
    public void keyDown(int entity, int keycode) {
        boolean isControlPressed = isControlPressed();

        // the amount of pixels by which to move item if moving
        float deltaMove = 1f / Sandbox.getInstance().getPixelPerWU();

        if (isShiftPressed()) {
            // if shift is pressed, move boxes by 20 pixels instead of one
            deltaMove = 20f / Sandbox.getInstance().getPixelPerWU(); //pixels
        }

        if (sandbox.getGridSize() > 1) {
            deltaMove = sandbox.getWorldGridSize();
            if (isShiftPressed()) {
                // if shift is pressed, move boxes 3 times more then the grid size
                deltaMove *= 3;
            }
        }

        if (!isControlPressed) {
            dragStartPositions.clear();
            for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
                transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);
                if (transformComponent != null)
                    dragStartPositions.put(itemInstance, new Vector2(transformComponent.x, transformComponent.y));
            }

            if (keycode == Input.Keys.UP) {
                // moving UP
                sandbox.getSelector().moveSelectedItemsBy(0, deltaMove);
            }
            if (keycode == Input.Keys.DOWN) {
                // moving down
                sandbox.getSelector().moveSelectedItemsBy(0, -deltaMove);
            }
            if (keycode == Input.Keys.LEFT) {
                // moving left
                sandbox.getSelector().moveSelectedItemsBy(-deltaMove, 0);
            }
            if (keycode == Input.Keys.RIGHT) {
                //moving right
                sandbox.getSelector().moveSelectedItemsBy(deltaMove, 0);
            }

            // sets item position, and puts things into undo-redo que
            Array<Object[]> payloads = new Array<>();
            for (int itemInstance : sandbox.getSelector().getCurrentSelection()) {
                transformComponent = SandboxComponentRetriever.get(itemInstance, TransformComponent.class);
                if (transformComponent == null)
                    continue;
                Vector2 newPosition = new Vector2(transformComponent.x, transformComponent.y);
                Vector2 oldPosition = dragStartPositions.get(itemInstance);

                if (newPosition.equals(oldPosition))
                    continue;

                Object[] payload = new Object[3];
                payload[0] = itemInstance;
                payload[1] = newPosition;
                payload[2] = oldPosition;
                payloads.add(payload);
            }

            if (payloads.size > 0)
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_ITEMS_MOVE_TO, payloads);
        }

        // Delete
        if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_DELETE);
        }
    }

    @Override
    public void keyUp(int entity, int keycode) {

    }

    private boolean isControlPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SYM)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

}
