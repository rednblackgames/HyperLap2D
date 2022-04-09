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

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.widget.H2DPopupMenu;

import java.util.HashMap;

/**
 * Created by azakhary on 4/20/2015.
 */
public class UIDropDownMenu extends H2DPopupMenu {

    private static final String CLASS_NAME = "games.rednblack.editor.view.ui.UIDropDownMenu";

    public static final String ITEM_CLICKED = CLASS_NAME + ".ACTION_CLICKED";

    private final Array<String> currentActionList = new Array<>();
    private final HashMap<String, String> actionNames = new HashMap<>();

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
        actionNames.put(MsgAPI.ACTION_CREATE_STICKY_NOTE, "Create Sticky Note");

        actionNames.put(MsgAPI.ACTION_EXPORT_LIBRARY_ITEM, "Export");
        actionNames.put(MsgAPI.ACTION_EXPORT_ACTION_ITEM, "Export");
        actionNames.put(MsgAPI.ACTION_RENAME_ACTION_ITEM, "Rename");
        actionNames.put(MsgAPI.ACTION_DELETE_IMAGE_RESOURCE, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_TINY_VG_RESOURCE, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_LIBRARY_ITEM, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_PARTICLE_EFFECT, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_TALOS_VFX, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_SPRITE_ANIMATION_RESOURCE, "Delete");
        actionNames.put(MsgAPI.ACTION_DELETE_SPINE_ANIMATION_RESOURCE, "Delete");

        actionNames.put(MsgAPI.ACTION_DELETE_MULTIPLE_RESOURCE, "Delete all selected");

        actionNames.put(MsgAPI.ACTION_UPDATE_RULER_POSITION, "Change Ruler Position");

        actionNames.put(MsgAPI.ACTION_CHANGE_POLYGON_VERTEX_POSITION, "Change Vertex Position");
        actionNames.put(MsgAPI.ACTION_DELETE_POLYGON_VERTEX, "Delete Vertex");

        actionNames.put(MsgAPI.ACTION_CHANGE_ORIGIN_POSITION, "Change Origin Position");
        actionNames.put(MsgAPI.ACTION_CENTER_ORIGIN_POSITION, "Center Origin");

        actionNames.put(MsgAPI.ACTION_DUPLICATE_LIBRARY_ACTION, "Duplicate");
        actionNames.put(MsgAPI.ACTION_DELETE_LIBRARY_ACTION, "Delete");
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
}
