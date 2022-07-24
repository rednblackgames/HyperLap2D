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

import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.Comparator;
import java.util.Set;

public class UIItemsTreeBox extends UICollapsibleBox {
    protected ComponentMapper<NodeComponent> nodeComponentMapper;
    protected ComponentMapper<ParentNodeComponent> parentNodeComponentMapper;
    protected ComponentMapper<MainItemComponent> mainItemComponentMapper;
    protected ComponentMapper<ZIndexComponent> zIndexComponentMapper;

    public static final String ITEMS_SELECTED = "games.rednblack.editor.view.ui.box.UIItemsTreeBox." + ".ITEMS_SELECTED";
    private final HyperLap2DFacade facade;
    private final VisTable treeTable;
    private Tree<UIItemsTreeNode, UIItemsTreeValue> tree;
    private VisScrollPane scroller;
    private UIItemsTreeNode rootTreeNode;

    private final Array<UIItemsTreeNode> tmpNodes = new Array<>();
    private UIItemsTreeNode rootNode;
    private Set<Integer> lastSelection;

    private final VisImageButton zUp, zDown;

    private Sandbox sandbox;

    private final ZIndexComparator zIndexComparator = new ZIndexComparator();
    private final Vector2 tmp = new Vector2();

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

    public void init(int rootScene) {
        sandbox = Sandbox.getInstance();
        sandbox.getEngine().inject(this);

        treeTable.clear();
        tree = new VisTree<>();
        scroller = StandardWidgetsFactory.createScrollPane(tree);
        scroller.setFlickScroll(false);
        treeTable.add(scroller).width(170).padTop(5).maxHeight(550).colspan(2);
        //
        rootTreeNode = addTreeRoot(rootScene, null);
        rootTreeNode.setExpanded(true);
        tree.addListener(new TreeChangeListener());

        sortTree();

        lastSelection = null;
        setSelection(null);

        treeTable.row().padTop(5);
        treeTable.add(new Separator("tool")).colspan(2).padTop(2).padBottom(2).fill().expand().row();

        treeTable.add(zUp).padLeft(zUp.getPrefWidth() + 10).left();
        treeTable.add(zDown).padRight(zDown.getPrefWidth() + 10).right();
    }

    public void sortTree() {
        rootTreeNode.getChildren().sort(zIndexComparator);
        rootTreeNode.updateChildren();
        tree.updateRootNodes();
    }

    public void update(int rootScene) {
        Pools.freeAll(tmpNodes);
        tmpNodes.clear();

        tree.clearChildren();
        tree.setOverNode(null);
        tree.getRootNodes().clear();
        tree.getSelection().clear();

        rootTreeNode = addTreeRoot(rootScene, null);
        rootTreeNode.setExpanded(true);

        sortTree();

        if (lastSelection != null)
            setSelection(lastSelection);
    }

    private UIItemsTreeNode addTreeRoot(int entity, UIItemsTreeNode parentNode) {  // was like this addTreeRoot(CompositeItem compoiteItem, Node parentNode)
        UIItemsTreeNode node = addTreeNode(entity, parentNode);
        if (parentNode == null) rootNode = node;

        NodeComponent nodeComponent = nodeComponentMapper.get(entity);
        MainItemComponent mainItemComponent = mainItemComponentMapper.get(entity);

        if(nodeComponent != null) {
            for (int item : nodeComponent.children) {
                if (mainItemComponent.entityType == EntityFactory.COMPOSITE_TYPE) {
                    addTreeRoot(item, node);
                } else {
                    addTreeNode(item, node);
                }
            }
        }
        return node;
    }

    private UIItemsTreeNode addTreeNode(int item, UIItemsTreeNode parentNode) {
        String name;
        ParentNodeComponent parentNodeComponent = parentNodeComponentMapper.get(item);
        MainItemComponent mainItemComponent = mainItemComponentMapper.get(item);

        UIItemsTreeNode node = Pools.get(UIItemsTreeNode.class, 20000).obtain();
        tmpNodes.add(node);

        if (parentNodeComponent == null) {
            node.setColor(Color.WHITE);
            name = Sandbox.getInstance().sceneControl.getCurrentSceneVO().sceneName;
        } else if (mainItemComponent.itemIdentifier != null && !mainItemComponent.itemIdentifier.isEmpty()) {
            node.setColor(Color.WHITE);
            name = mainItemComponent.itemIdentifier;
        } else {
            node.setColor(0.65f, 0.65f, 0.65f, 1f);
            name = EntityUtils.itemTypeNameMap.get(mainItemComponent.entityType);
            if (name == null)
                name = EntityUtils.itemTypeNameMap.get(EntityFactory.UNKNOWN_TYPE);
        }

        node.setName(name);
        ZIndexComponent zIndexComponent = zIndexComponentMapper.get(item);

        node.setNodeValue(mainItemComponent.uniqueId, zIndexComponent.getGlobalZIndex());
        if (mainItemComponent.entityType != EntityFactory.COMPOSITE_TYPE)
            node.setPad(0, 0, 4, 0);
        else
            node.setPad(4, 3, 4, 0);

        if (parentNode != null) {
            node.setIcon(EntityUtils.getItemIcon(mainItemComponent.entityType));
            parentNode.add(node);
        } else {
            node.setIcon(VisUI.getSkin().getDrawable("icon-root"));
            tree.add(node);
        }
        return node;
    }

    public void setSelection(Set<Integer> selection) {
        lastSelection = selection;

        if (tree == null) return;
        tree.getSelection().clear();
        if (selection == null) return;
        addToSelection(selection);
    }

    public void addToSelection(Set<Integer> selection) {
        if (lastSelection != null && selection != null)
            lastSelection.addAll(selection);

        if (tree == null || selection == null) return;
        Array<UIItemsTreeNode> allSceneRootNodes = tree.getRootNodes().get(0).getChildren();

        for (int entityId : EntityUtils.getEntityId(selection)) {
            for (UIItemsTreeNode n : allSceneRootNodes) {
                if(n.getValue().entityId == entityId) {
                    tree.getSelection().add(n);
                    break;
                }
            }
        }

        if (tree.getSelection().size() > 0) {
            tree.validate();
            Actor firstSelected = tree.getSelection().first().getActor();
            tmp.set(0, firstSelected.getHeight());
            firstSelected.localToParentCoordinates(tmp);
            scroller.scrollTo(tmp.x, tmp.y, firstSelected.getWidth(), firstSelected.getHeight());
        }
    }

    public void removeFromSelection(Set<Integer> selection) {
        if (lastSelection != null && selection != null)
            lastSelection.removeAll(selection);

        if (tree == null || selection == null) return;
        Array<UIItemsTreeNode> allSceneRootNodes = tree.getRootNodes().get(0).getChildren();

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
            if (selection.size() == 1 && getTapCount() == 2) {
                UIItemsTreeValue selected = selection.first().getValue();
                int item = EntityUtils.getByUniqueId(selected.entityId);
                if (EntityUtils.getType(item) == EntityFactory.COMPOSITE_TYPE) {
                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, item);
                }
            }
        }
    }

    public static class ZIndexComparator implements Comparator<UIItemsTreeNode> {
        @Override
        public int compare(UIItemsTreeNode o1, UIItemsTreeNode o2) {
            return Integer.compare(o2.getValue().zIndex, o1.getValue().zIndex);
        }
    }
}
