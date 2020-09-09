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

package games.rednblack.editor.view.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import games.rednblack.h2d.common.MsgAPI;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import games.rednblack.editor.event.MenuItemListener;

import java.util.HashMap;

/**
 * Created by azakhary on 4/20/2015.
 */
public class UIDropDownMenu extends PopupMenu {

    private static final String CLASS_NAME = "games.rednblack.editor.view.ui.UIDropDownMenu";

    public static final String ITEM_CLICKED = CLASS_NAME + ".ACTION_CLICKED";

    private Array<String> currentActionList = new Array<>();

    private HashMap<String, String> actionNames = new HashMap<>();

    public UIDropDownMenu() {
        actionNames.put(MsgAPI.ACTION_GROUP_ITEMS, "Convert into composite");
        actionNames.put(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, "Edit composite");
        actionNames.put(MsgAPI.ACTION_CONVERT_TO_BUTTON, "Convert to button");
        actionNames.put(MsgAPI.ACTION_CUT, "Cut");
        actionNames.put(MsgAPI.ACTION_COPY, "Copy");
        actionNames.put(MsgAPI.ACTION_PASTE, "Paste");
        actionNames.put(MsgAPI.ACTION_DELETE, "Delete");
        actionNames.put(MsgAPI.SHOW_ADD_LIBRARY_DIALOG, "Add to library");
        actionNames.put(MsgAPI.ACTION_CREATE_PRIMITIVE, "Create Primitive");

        actionNames.put(MsgAPI.ACTION_EXPORT_LIBRARY_ITEM, "Export");
        actionNames.put(MsgAPI.ACTION_DELETE_IMAGE_RESOURCE, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_LIBRARY_ITEM, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_PARTICLE_EFFECT, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_SPRITE_ANIMATION_RESOURCE, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_SPRITER_ANIMATION_RESOURCE, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_SPINE_ANIMATION_RESOURCE, "Delete");

        actionNames.put(MsgAPI.ACTION_UPDATE_RULER_POSITION, "Change Ruler Position");

        actionNames.put(MsgAPI.ACTION_CHANGE_POLYGON_VERTEX_POSITION, "Change Vertex Position");
        actionNames.put(MsgAPI.ACTION_DELETE_POLYGON_VERTEX, "Delete Vertex");
    }

    public void setActionName(String action, String name) {
        actionNames.put(action, name);
    }

    public void setActionList(Array<String> actions) {
        currentActionList.clear();
        currentActionList.addAll(actions);

        initView();
        setListeners();
    }

    private void setListeners() {
        clearListeners();
    }

    private void initView() {
        clear();

        for (int i = 0; i < currentActionList.size; i++) {
            String itemName = actionNames.get(currentActionList.get(i));
            MenuItem menuItem = new MenuItem(itemName, new MenuItemListener(ITEM_CLICKED, currentActionList.get(i)));
            addItem(menuItem);
        }
    }

    public void keepWithinStage() {
        Stage stage = getStage();
        if (stage == null) return;
        Camera camera = stage.getCamera();
        if (camera instanceof OrthographicCamera) {
            OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
            float parentWidth = stage.getWidth();
            float parentHeight = stage.getHeight();
            if (getX(Align.right) - camera.position.x > parentWidth / 2 / orthographicCamera.zoom)
                setPosition(camera.position.x + parentWidth / 2 / orthographicCamera.zoom, getY(Align.right), Align.right);
            if (getX(Align.left) - camera.position.x < -parentWidth / 2 / orthographicCamera.zoom)
                setPosition(camera.position.x - parentWidth / 2 / orthographicCamera.zoom, getY(Align.left), Align.left);
            if (getY(Align.top) - camera.position.y > parentHeight / 2 / orthographicCamera.zoom)
                setPosition(getX(Align.top), camera.position.y + parentHeight / 2 / orthographicCamera.zoom, Align.top);
            if (getY(Align.bottom) - camera.position.y < -parentHeight / 2 / orthographicCamera.zoom)
                setPosition(getX(Align.bottom), camera.position.y - parentHeight / 2 / orthographicCamera.zoom, Align.bottom);
        } else if (getParent() == stage.getRoot()) {
            float parentWidth = stage.getWidth();
            float parentHeight = stage.getHeight();
            if (getX() < 0) setX(0);
            if (getRight() > parentWidth) setX(parentWidth - getWidth());
            if (getY() < 0) setY(0);
            if (getTop() > parentHeight) setY(parentHeight - getHeight());
        }
    }
}
