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
import games.rednblack.puremvc.Facade;

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

        followersUIMediator = Facade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
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
     * sets selection to particular item
     * @param item to select
     * @param removeOthers if set to true this item will become the only selection, otherwise will be added to existing
     */
    public void setSelection(int item, boolean removeOthers) {
        if (currentSelection.contains(item)) return;

        if (removeOthers) clearSelections();

        currentSelection.add(item);

        Facade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
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
            Facade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
            return;
        }

        currentSelection.addAll(items.stream().collect(Collectors.toList()));

        if (alsoShow) {
            Facade.getInstance().sendNotification(MsgAPI.SHOW_SELECTIONS, currentSelection);
        } else {
            Facade.getInstance().sendNotification(MsgAPI.HIDE_SELECTIONS, currentSelection);
        }
        Facade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
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

        Facade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
    }

    /**
     * clears all selections
     */
    public void clearSelections() {
        currentSelection.clear();

        Facade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, currentSelection);
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
        align(relativeTo, false, Axis.X, toHighestX, toHighestX, false);
    }

    public void alignSelectionsByY(int relativeTo, boolean toHighestY) {
        align(relativeTo, false, Axis.Y, toHighestY, toHighestY, false);
    }

    public void alignSelectionsAtLeftEdge(int relativeTo) {
        align(relativeTo, true, Axis.X, false, true, false);
    }

    public void alignSelectionsAtRightEdge(int relativeTo) {
        align(relativeTo, true, Axis.X, true, false, false);
    }

    public void alignSelectionsAtTopEdge(int relativeTo) {
        align(relativeTo, true, Axis.Y, true, false, false);
    }

    public void alignSelectionsAtBottomEdge(int relativeTo) {
        align(relativeTo, true, Axis.Y, false, true, false);
    }

    public void alignSelectionsVerticallyCentered(int relativeTo) {
        align(relativeTo, true, Axis.Y, false, false, true);
    }

    public void alignSelectionsHorizontallyCentered(int relativeTo) {
        align(relativeTo, true, Axis.X, false, false, true);
    }

    private enum Axis { X, Y }

    /**
     * Pure alignment math: returns the target visual low-edge position for an
     * entity, given the reference entity's low edge and size, the entity's size,
     * and the alignment mode. Extracted from the per-method duplicates so it can
     * be unit-tested without the ECS engine.
     *
     * @param refLow               the reference entity's visual low edge (left / bottom)
     * @param refSize              the reference entity's visual size (width / height)
     * @param entSize              the entity's visual size (width / height)
     * @param useHighEdge          align to the reference's high edge (right / top) instead of low
     * @param itemAnchoredHighEdge offset the entity by its own size (anchor its high edge)
     * @param centered             center the entity on the reference instead of edge-aligning
     * @return the target visual low-edge position for the entity
     */
    static float alignTarget(float refLow, float refSize, float entSize,
                             boolean useHighEdge, boolean itemAnchoredHighEdge, boolean centered) {
        if (centered) {
            return refLow + (refSize - entSize) / 2f;
        }
        float refEdge = useHighEdge ? refLow + refSize : refLow;
        return refEdge - (itemAnchoredHighEdge ? entSize : 0f);
    }

    /**
     * Shared alignment loop. Moves every selected entity (optionally excluding
     * the reference) so that its visual low edge lands on the target computed by
     * {@link #alignTarget}, preserving each entity's transform-to-visual offset.
     */
    private void align(int relativeTo, boolean skipReference, Axis axis,
                       boolean useHighEdge, boolean itemAnchoredHighEdge, boolean centered) {
        if (relativeTo == -1) return;

        EntityBounds ref = new EntityBounds(relativeTo);
        float refLow = (axis == Axis.X) ? ref.getVisualX() : ref.getVisualY();
        float refSize = (axis == Axis.X) ? ref.getVisualWidth() : ref.getVisualHeight();

        moveCommandBuilder.clear();
        for (int entity : currentSelection) {
            if (skipReference && entity == relativeTo) continue;
            EntityBounds eb = new EntityBounds(entity);
            float entSize = (axis == Axis.X) ? eb.getVisualWidth() : eb.getVisualHeight();
            float delta = (axis == Axis.X) ? (eb.getX() - eb.getVisualX()) : (eb.getY() - eb.getVisualY());
            float target = alignTarget(refLow, refSize, entSize, useHighEdge, itemAnchoredHighEdge, centered);
            if (axis == Axis.X) {
                moveCommandBuilder.setX(entity, target + delta);
            } else {
                moveCommandBuilder.setY(entity, target + delta);
            }
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
            Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
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
