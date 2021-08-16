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

package games.rednblack.editor.view.stage;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.Constants;
import games.rednblack.editor.utils.EntityBounds;
import games.rednblack.editor.utils.MoveCommandBuilder;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.SceneControlMediator;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.h2d.common.MsgAPI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Managing item selections, selecting by criteria and so on
 *
 * @author azakhary
 */
public class ItemSelector {

    /** commands reference */
    private Sandbox sandbox;

    private SceneControlMediator sceneControl;

    /** list of current selected panels */
    private Set<Integer> currentSelection = new HashSet<>();

    private FollowersUIMediator followersUIMediator;

    private MoveCommandBuilder moveCommandBuilder = new MoveCommandBuilder();

    public ItemSelector(Sandbox sandbox) {
        this.sandbox = sandbox;
        sceneControl = sandbox.sceneControl;

        followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
    }

    /***************************** Getters *********************************/

    /**
     * @return HashMap of selection rectangles that contain panels
     */
    public Set<Integer> getCurrentSelection() {
        return currentSelection;
    }

    /**
     * @return one selected item
     */
    public int getSelectedItem() {
        if(currentSelection.size() > 0) {
            return currentSelection.iterator().next();
        }

        return -1;
    }

    /**
    public SelectionRectangle getSelectedItemSelectionRectangle() {
        ArrayList<SelectionRectangle> items = new ArrayList<SelectionRectangle>();
        for (SelectionRectangle value : currentSelection.values()) {
            items.add(value);
            break;
        }
        if(items.size() > 0) {
            return items.get(0);
        }

        return null;
    }
     */

    /**
     * @return list of currently selected panels
     */
    public Set<Integer> getSelectedItems() {
        return currentSelection;
    }


    public BiConsumer<Integer, AccContainer> broadestItem = (i, acc) -> {
        if (acc.carryVal == null) acc.carryVal = Constants.FLOAT_MIN;
        EntityBounds bounds = new EntityBounds(i);
        final float width = bounds.getVisualWidth();
        if (width > acc.carryVal) {
            acc.carryVal = width;
            acc.carry = i;
        }
    };

    public BiConsumer<Integer, AccContainer> highestItem = (i, acc) -> {
        if (acc.carryVal == null) acc.carryVal = Constants.FLOAT_MIN;
        EntityBounds bounds = new EntityBounds(i);
        final float height = bounds.getVisualHeight();
        if (height > acc.carryVal) {
            acc.carryVal = height;
            acc.carry = i;
        }
    };

    public BiConsumer<Integer, AccContainer> rightmostItem = (i, acc) -> {
        if (acc.carryVal == null) acc.carryVal = Constants.FLOAT_MIN;
        EntityBounds bounds = new EntityBounds(i);
        final float x = bounds.getVisualRightX();
        if (x > acc.carryVal) {
            acc.carryVal = x;
            acc.carry = i;
        }
    };

    public BiConsumer<Integer, AccContainer> leftmostItem = (i, acc) -> {
        if (acc.carryVal == null) acc.carryVal = Constants.FLOAT_MAX;
        EntityBounds bounds = new EntityBounds(i);
        final float x = bounds.getVisualX();
        if (x < acc.carryVal) {
            acc.carryVal = x;
            acc.carry = i;
        }
    };

    public BiConsumer<Integer, AccContainer> topmostItem = (i, acc) -> {
        if (acc.carryVal == null) acc.carryVal = Constants.FLOAT_MIN;
        EntityBounds bounds = new EntityBounds(i);
        final float y = bounds.getVisualTopY();
        if (y > acc.carryVal) {
            acc.carryVal = y;
            acc.carry = i;
        }
    };
    public BiConsumer<Integer, AccContainer> bottommostItem = (i, acc) -> {
        if (acc.carryVal == null) acc.carryVal = Constants.FLOAT_MAX;
        EntityBounds bounds = new EntityBounds(i);
        final float y = bounds.getVisualY();
        if (y < acc.carryVal) {
            acc.carryVal = y;
            acc.carry = i;
        }
    };


    /**
     * used as accumulator container
     */
    private static class AccContainer {
        public Float carryVal = null;
        public int carry = -1;
    }


    public int get(BiConsumer<Integer, AccContainer> checkSelection) {
        final AccContainer acc = new AccContainer();

        for (int entity : currentSelection) {
            checkSelection.accept(entity, acc);
        }
        return acc.carry;
    }


     /**
     * Finds all panels that are on particular layer and selects them
     * @param name of the layer
     */
    public void selectItemsByLayerName(String name) {
    	//TODO fix and uncomment
//        ArrayList<Entity> itemsArr = new ArrayList<Entity>();
//        for (int i = 0; i < sceneControl.getCurrentScene().getItems().size(); i++) {
//            if (sceneControl.getCurrentScene().getItems().get(i).getDataVO().layerName.equals(name)) {
//                itemsArr.add(sceneControl.getCurrentScene().getItems().get(i));
//            }
//        }
//
//        setSelections(itemsArr, true);
    }

    /**
     * sets selection to particular item
     * @param item to select
     * @param removeOthers if set to true this item will become the only selection, otherwise will be added to existing
     */
    public void setSelection(int item, boolean removeOthers) {
        if (currentSelection.contains(item)) return;

        if (removeOthers) clearSelections();

        currentSelection.add(item);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
    }

    /**
     * adds to selection a list of items
     * @param items list of panels to select
     */
    public void addSelections(Set<Integer> items) {
        for (int item : items) {
            setSelection(item, false);
        }
    }

    public boolean isSelected(int entity) {
        return currentSelection.contains(entity);
    }

    /**
     * set selection to a list of items
     * @param items list of panels to select
     * @param alsoShow if false, selection will remain hidden at this moment
     */
    public void setSelections(Set<Integer> items, boolean alsoShow) {
        currentSelection.clear();

        if(items == null) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
            return;
        }

        currentSelection.addAll(items.stream().collect(Collectors.toList()));

        if (alsoShow) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.SHOW_SELECTIONS, currentSelection);
        } else {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.HIDE_SELECTIONS, currentSelection);
        }
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
    }

    /**
     * remove selection to a list of items
     * @param items list of panels to remove selection
     */
    public void releaseSelections(Set<Integer> items) {
        for (int item : items) {
            releaseSelection(item);
        }
    }

    /**
     * Un-selects item
     * @param item to un-select
     */
    public void releaseSelection(int item) {
        currentSelection.remove(item);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
    }

    /**
     * clears all selections
     */
    public void clearSelections() {
        currentSelection.clear();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
    }


    /**
     * Selects all panels on currently active scene
     */
    public HashSet<Integer> getAllFreeItems() {
    	NodeComponent nodeComponent = SandboxComponentRetriever.get(sandbox.getCurrentViewingEntity(), NodeComponent.class);
		SnapshotArray<Integer> childrenEntities = nodeComponent.children;

        Integer[] array = childrenEntities.toArray();
        HashSet<Integer> result = new HashSet<>(Arrays.asList(array));

        for (Iterator<Integer> i = result.iterator(); i.hasNext();) {
            int element = i.next();
            LayerItemVO layerItemVO = EntityUtils.getEntityLayer(element);
            if(layerItemVO != null && layerItemVO.isLocked) {
                i.remove();
            }
        }

        return result;
    }



    /************************ Manipulate selected panels  ******************************/

    /**
     * removes all selected panels from the scene
     */
    public void removeCurrentSelectedItems() {
        for (int item : currentSelection) {
            followersUIMediator.removeFollower(item);
            sandbox.getEngine().delete(item);
        }

        currentSelection.clear();
        sandbox.getEngine().process();
    }

    public void alignSelectionsByX(int relativeTo, boolean toHighestX) {
    	if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToX = (toHighestX)? (bounds.getVisualRightX()) : bounds.getVisualX();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            EntityBounds entityBounds = new EntityBounds(entity);
            final float deltaX = entityBounds.getX() - entityBounds.getVisualX();
            final float visualX = relativeToX - ((toHighestX)? 1 : 0) * entityBounds.getVisualWidth();

            moveCommandBuilder.setX(entity, visualX + deltaX);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsByY(int relativeTo, boolean toHighestY) {
    	if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToY = (toHighestY)? bounds.getVisualTopY() : bounds.getVisualY();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            EntityBounds entityBounds = new EntityBounds(entity);
            final float deltaY = entityBounds.getY() - entityBounds.getVisualY();
            final float visualY = relativeToY - ((toHighestY)? 1 : 0) * entityBounds.getVisualHeight();

            moveCommandBuilder.setY(entity, visualY + deltaY);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsAtLeftEdge(int relativeTo) {
        if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToX = bounds.getVisualX();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (entity == relativeTo) continue;
            EntityBounds entityBounds = new EntityBounds(entity);

            final float deltaX = entityBounds.getX() - entityBounds.getVisualX();
            final float visualX = relativeToX - entityBounds.getVisualWidth();

            moveCommandBuilder.setX(entity, visualX + deltaX);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsAtRightEdge(int relativeTo) {
        if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToRightX = bounds.getVisualRightX();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (entity == relativeTo) continue;
            EntityBounds entityBounds = new EntityBounds(entity);

            final float deltaX = entityBounds.getX() - entityBounds.getVisualX();

            moveCommandBuilder.setX(entity, relativeToRightX + deltaX);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsAtTopEdge(int relativeTo) {
        if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToTopY = bounds.getVisualTopY();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (entity == relativeTo) continue;
            EntityBounds entityBounds = new EntityBounds(entity);

            final float deltaY = entityBounds.getY() - entityBounds.getVisualY();

            moveCommandBuilder.setY(entity, relativeToTopY + deltaY);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsAtBottomEdge(int relativeTo) {
        if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToY = bounds.getVisualY();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (entity == relativeTo) continue;
            EntityBounds entityBounds = new EntityBounds(entity);

            final float deltaY = entityBounds.getY() - entityBounds.getVisualY();
            final float visualY = relativeToY - entityBounds.getVisualHeight();

             moveCommandBuilder.setY(entity, visualY + deltaY);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsVerticallyCentered(int relativeTo) {
        if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToY = bounds.getVisualY();
        final float relativeToHeight = bounds.getVisualHeight();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (entity == relativeTo) continue;
            EntityBounds entityBounds = new EntityBounds(entity);

            final float deltaY = entityBounds.getY() - entityBounds.getVisualY();
            final float visualY = relativeToY + (relativeToHeight - entityBounds.getVisualHeight()) / 2;

            moveCommandBuilder.setY(entity, visualY + deltaY);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelectionsHorizontallyCentered(int relativeTo) {
        if (relativeTo == -1) return;

        EntityBounds bounds = new EntityBounds(relativeTo);
        final float relativeToX = bounds.getVisualX();
        final float relativeToWidth = bounds.getVisualWidth();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (entity == relativeTo) continue;
            EntityBounds entityBounds = new EntityBounds(entity);

            final float deltaX = entityBounds.getX() - entityBounds.getVisualX();
            final float visualX = relativeToX + (relativeToWidth - entityBounds.getVisualWidth()) / 2;

            moveCommandBuilder.setX(entity, visualX + deltaX);
        }
        moveCommandBuilder.execute();
    }

    public void alignSelections(int align) {
        //ResolutionEntryVO resolutionEntryVO = dataManager.getCurrentProjectInfoVO().getResolution(dataManager.currentResolutionName);
        switch (align) {
            case Align.top:
                alignSelectionsByY(get(topmostItem), true);
                break;
            case Align.left:
                alignSelectionsByX(get(leftmostItem), false);
                break;
            case Align.bottom:
                alignSelectionsByY(get(bottommostItem), false);
                break;
            case Align.right:
                alignSelectionsByX(get(rightmostItem), true);
                break;
            case Align.center | Align.left: //horizontal
                alignSelectionsHorizontallyCentered(get(broadestItem));
                break;
            case Align.center | Align.bottom: //vertical
                alignSelectionsVerticallyCentered(get(highestItem));
                break;
        }
    }

    public void alignSelectionsAtEdge(int align) {
        switch (align) {
            case Align.top:
                alignSelectionsAtTopEdge(get(bottommostItem));
                break;
            case Align.left:
                alignSelectionsAtLeftEdge(get(rightmostItem));
                break;
            case Align.bottom:
                alignSelectionsAtBottomEdge(get(topmostItem));
                break;
            case Align.right:
                alignSelectionsAtRightEdge(get(leftmostItem));
                break;
        }
    }

    /**
     * Moves selected panels by specified values in both directions
     * @param x
     * @param y
     */
    public void moveSelectedItemsBy(float x, float y) {
        for (int entity : currentSelection) {
            sandbox.itemControl.moveItemBy(entity, x, y);
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
        }
    }

    public boolean selectionIsOneItem() {
        return getCurrentSelection().size() == 1;
    }

    public boolean selectionIsComposite() {

        if(currentSelection.isEmpty()) return false;

        int entity = currentSelection.stream().findFirst().get();
        NodeComponent nodeComponent = SandboxComponentRetriever.get(entity, NodeComponent.class);

        return nodeComponent != null;
    }
}
