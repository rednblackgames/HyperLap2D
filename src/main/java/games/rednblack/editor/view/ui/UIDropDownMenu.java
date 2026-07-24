package games.rednblack.editor.view.ui;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.editor.utils.MenuIcons;
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
    /** action constant -> skin region used as its context-menu icon (shared with the top menu bar). */
    private final HashMap<String, String> actionIcons = new HashMap<>();

    public UIDropDownMenu() {
        actionNames.put(MsgAPI.ACTION_GROUP_ITEMS, "Wrap into composite");
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

        actionIcons.put(MsgAPI.ACTION_GROUP_ITEMS, "icon-menu-composite");
        actionIcons.put(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, "icon-menu-edit-composite");
        actionIcons.put(MsgAPI.ACTION_CONVERT_TO_BUTTON, "icon-menu-button");
        actionIcons.put(MsgAPI.ACTION_CUT, "icon-menu-cut");
        actionIcons.put(MsgAPI.ACTION_COPY, "icon-menu-copy");
        actionIcons.put(MsgAPI.ACTION_PASTE, "icon-menu-paste");
        actionIcons.put(MsgAPI.ACTION_DELETE, "icon-menu-delete");
        actionIcons.put(MsgAPI.SHOW_ADD_LIBRARY_DIALOG, "icon-menu-library-add");
        actionIcons.put(MsgAPI.ACTION_CREATE_PRIMITIVE, "icon-menu-primitive");
        actionIcons.put(MsgAPI.ACTION_CREATE_STICKY_NOTE, "icon-menu-sticky-note");

        actionIcons.put(MsgAPI.ACTION_EXPORT_LIBRARY_ITEM, "icon-menu-export");
        actionIcons.put(MsgAPI.ACTION_EXPORT_ACTION_ITEM, "icon-menu-export");
        actionIcons.put(MsgAPI.ACTION_RENAME_ACTION_ITEM, "icon-menu-rename");
        actionIcons.put(MsgAPI.ACTION_DELETE_IMAGE_RESOURCE, "icon-menu-delete");
        actionIcons.put(MsgAPI.ACTION_DELETE_TINY_VG_RESOURCE, "icon-menu-delete");
        actionIcons.put(MsgAPI.ACTION_DELETE_LIBRARY_ITEM, "icon-menu-delete");
        actionIcons.put(MsgAPI.ACTION_DELETE_PARTICLE_EFFECT, "icon-menu-delete");
        actionIcons.put(MsgAPI.ACTION_DELETE_TALOS_VFX, "icon-menu-delete");
        actionIcons.put(MsgAPI.ACTION_DELETE_SPRITE_ANIMATION_RESOURCE, "icon-menu-delete");
        actionIcons.put(MsgAPI.ACTION_DELETE_SPINE_ANIMATION_RESOURCE, "icon-menu-delete");

        actionIcons.put(MsgAPI.ACTION_DELETE_MULTIPLE_RESOURCE, "icon-menu-delete-all");

        actionIcons.put(MsgAPI.ACTION_UPDATE_RULER_POSITION, "icon-menu-ruler");

        actionIcons.put(MsgAPI.ACTION_CHANGE_POLYGON_VERTEX_POSITION, "icon-menu-vertex");
        actionIcons.put(MsgAPI.ACTION_DELETE_POLYGON_VERTEX, "icon-menu-vertex-delete");

        actionIcons.put(MsgAPI.ACTION_CHANGE_ORIGIN_POSITION, "icon-menu-origin");
        actionIcons.put(MsgAPI.ACTION_CENTER_ORIGIN_POSITION, "icon-menu-origin-center");

        actionIcons.put(MsgAPI.ACTION_DUPLICATE_LIBRARY_ACTION, "icon-menu-duplicate");
        actionIcons.put(MsgAPI.ACTION_DELETE_LIBRARY_ACTION, "icon-menu-delete");
    }

    private static Drawable icon(String region) {
        return MenuIcons.get(region);
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
            String action = currentActionList.get(i);
            String itemName = actionNames.get(action);
            Drawable itemIcon = icon(actionIcons.get(action));
            MenuItem menuItem = itemIcon != null
                    ? new MenuItem(itemName, itemIcon, new MenuItemListener(ITEM_CLICKED, action))
                    : new MenuItem(itemName, new MenuItemListener(ITEM_CLICKED, action));
            menuItem.getImageCell().pad(5);
            addItem(menuItem);
        }
    }
}
