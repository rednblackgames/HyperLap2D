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

package games.rednblack.editor.view.ui.box;

import java.util.Comparator;
import java.util.Set;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;

public class UIItemsTreeBox extends UICollapsibleBox {
    public static final String ITEMS_SELECTED = "games.rednblack.editor.view.ui.box.UIItemsTreeBox." + ".ITEMS_SELECTED";
    private final HyperLap2DFacade facade;
    private final VisTable treeTable;
    private Tree<UIItemsTreeNode, UIItemsTreeValue> tree;

    private UIItemsTreeNode rootNode;
    private Set<Entity> lastSelection;

    private final VisImageButton zUp, zDown;

    Sandbox sandbox;

    public UIItemsTreeBox() {
        super("Items Tree", 180);
        setMovable(false);
        facade = HyperLap2DFacade.getInstance();
        treeTable = new VisTable();
        treeTable.left();
        zUp = StandardWidgetsFactory.createImageButton("arrow-button");
        StandardWidgetsFactory.addTooltip(zUp, "Move Z-Index Up");
        zDown = StandardWidgetsFactory.createImageButton("arrow-button");
        zDown.setTransform(true);
        zDown.setRotation(180);
        zDown.setOrigin(zUp.getPrefWidth() / 2, zUp.getPrefHeight() / 2);
        StandardWidgetsFactory.addTooltip(zDown, "Move Z-Index Down");

        createCollapsibleWidget(treeTable);

        zUp.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sandbox.itemControl.itemZIndexChange(sandbox.getSelector().getCurrentSelection(), true);
                facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, sandbox.getSelector().getCurrentSelection());
            }
        });

        zDown.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sandbox.itemControl.itemZIndexChange(sandbox.getSelector().getCurrentSelection(), false);
                facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, sandbox.getSelector().getCurrentSelection());
            }
        });
    }

    UIItemsTreeNode rootTreeNode;

    public void init(Entity rootScene) {
        sandbox = Sandbox.getInstance();

        treeTable.clear();
        tree = new VisTree<>();
        VisScrollPane scroller = StandardWidgetsFactory.createScrollPane(tree);
        scroller.setFlickScroll(false);
        treeTable.add(scroller).width(177).maxHeight(570).colspan(2);
        //
        rootTreeNode = addTreeRoot(rootScene, null);
        rootTreeNode.setExpanded(true);
        tree.addListener(new TreeChangeListener());

        sortTree();

        if (lastSelection != null)
            setSelection(lastSelection);

        treeTable.row().padTop(5);
        treeTable.add(new Separator("tool")).colspan(2).padTop(2).padBottom(2).fill().expand().row();

        treeTable.add(zUp).padLeft(zUp.getPrefWidth() + 10).left();
        treeTable.add(zDown).padRight(zDown.getPrefWidth() + 10).right();
    }

    public void sortTree() {
        rootTreeNode.getChildren().sort(new ZIndexComparator());
        rootTreeNode.updateChildren();
        tree.updateRootNodes();
    }

    private UIItemsTreeNode addTreeRoot(Entity entity, UIItemsTreeNode parentNode) {  // was like this addTreeRoot(CompositeItem compoiteItem, Node parentNode)
        UIItemsTreeNode node = addTreeNode(entity, parentNode);
        if (parentNode == null) rootNode = node;

        NodeComponent nodeComponent = ComponentRetriever.get(entity, NodeComponent.class);

        if(nodeComponent != null) {
            for (Entity item : nodeComponent.children) {
                if (EntityUtils.getType(entity) == EntityFactory.COMPOSITE_TYPE) {
                    addTreeRoot(item, node);
                } else {
                    addTreeNode(item, node);
                }
            }
        }
        return node;
    }

    private UIItemsTreeNode addTreeNode(Entity item, UIItemsTreeNode parentNode) {
        String name, style;
        ParentNodeComponent parentNodeComponent = ComponentRetriever.get(item, ParentNodeComponent.class);
        MainItemComponent mainItemComponent = ComponentRetriever.get(item, MainItemComponent.class);

        if (parentNodeComponent == null) {
            name = Sandbox.getInstance().sceneControl.getCurrentSceneVO().sceneName;
            style = "default";
        } else if (mainItemComponent.itemIdentifier != null && !mainItemComponent.itemIdentifier.isEmpty()) {
            name = mainItemComponent.itemIdentifier;
            style = "default";
        } else {
            style = "greyed";
            int type = EntityUtils.getType(item);
            name = EntityUtils.itemTypeNameMap.get(type);
            if (name == null)
                name = EntityUtils.itemTypeNameMap.get(EntityFactory.UNKNOWN_TYPE);
        }

        VisTable label = new VisTable();
        Cell<VisLabel> lblCell = label.add(new VisLabel(name, style));
        UIItemsTreeNode node = new UIItemsTreeNode(label);
        ZIndexComponent zIndexComponent = ComponentRetriever.get(item, ZIndexComponent.class);

        node.setValue(new UIItemsTreeValue(mainItemComponent.uniqueId, zIndexComponent.getGlobalZIndex()));
        if (mainItemComponent.entityType != EntityFactory.COMPOSITE_TYPE)
            lblCell.padBottom(4);
        else {
            lblCell.padTop(4);
            lblCell.padLeft(3);
            lblCell.padBottom(4);
        }
        node.setIcon(EntityUtils.getItemIcon(item));
        if (parentNode != null) {
            parentNode.add(node);
        } else {
            tree.add(node);
        }
        return node;
    }

    public void setSelection(Set<Entity> selection) {
        lastSelection = selection;

        if (tree == null) return;
        tree.getSelection().clear();
        if (selection == null) return;
        addToSelection(selection);
    }

    public void addToSelection(Set<Entity> selection) {
        if (lastSelection != null && selection != null)
            lastSelection.addAll(selection);

        if (tree == null || selection == null) return;
        Array<UIItemsTreeNode> allSceneRootNodes = tree.getNodes().get(0).getChildren();

        for (int entityId : EntityUtils.getEntityId(selection)) {
            for (UIItemsTreeNode n : allSceneRootNodes) {
                if(n.getValue().entityId == entityId) {
                    tree.getSelection().add(n);
                    break;
                }
            }
        }
    }

    public void removeFromSelection(Set<Entity> selection) {
        if (lastSelection != null && selection != null)
            lastSelection.removeAll(selection);

        if (tree == null || selection == null) return;
        Array<UIItemsTreeNode> allSceneRootNodes = tree.getNodes().get(0).getChildren();

        for (int entityId : EntityUtils.getEntityId(selection)) {
            for (UIItemsTreeNode n : allSceneRootNodes) {
                if(n.getValue().entityId == entityId) {
                    tree.getSelection().remove(n);
                    break;
                }
            }
        }
    }


    private class TreeChangeListener extends ClickListener {
        public void clicked (InputEvent event, float x, float y) {
            Selection<UIItemsTreeNode> selection = tree.getSelection();
            selection.remove(rootNode);
            facade.sendNotification(ITEMS_SELECTED, selection);
        }
    }

    public static class ZIndexComparator implements Comparator<UIItemsTreeNode> {
        @Override
        public int compare(UIItemsTreeNode o1, UIItemsTreeNode o2) {
            return Integer.compare(o2.getValue().zIndex, o1.getValue().zIndex);
        }
    }
}
