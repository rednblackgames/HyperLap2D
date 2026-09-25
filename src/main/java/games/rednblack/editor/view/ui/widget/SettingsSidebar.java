package games.rednblack.editor.view.ui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.h2d.common.view.SettingsNodeValue;

/**
 * The list of settings categories down the left of the settings dialog: one row each, the one being
 * read marked, and the panels a plugin adds indented under the category they belong to.
 *
 * It takes the place of a tree because the dialog is only ever two levels deep, where an expand
 * arrow hides things for nothing. Selecting a row sends a {@link ChangeListener.ChangeEvent}, which
 * is what the dialog listens to in order to swap the panel on the right.
 */
public class SettingsSidebar extends VisTable {

    /** Wide enough for the longest category name plus its icon, and fixed: nothing to drag. */
    public static final float WIDTH = 180f;

    /** What a settings surface is made of: the rows here, and the panel beside them. */
    public static final Color SURFACE = Color.valueOf("363639");

    /** The rows are their own colour, the way a button is: the bar itself is nothing. */
    private static final Color ROW_UP = SURFACE;
    private static final Color ROW_OVER = Color.valueOf("45454A");
    private static final Color ROW_OPEN = Color.valueOf("1BA1E2");
    private static final Color ROW_OPEN_OVER = Color.valueOf("31AFEC");
    private static final Color ROW_OPEN_DOWN = Color.valueOf("1790CA");

    /** Room above and below the name, which is what decides how tall a row is. */
    private static final float ROW_PAD_Y = 9f;
    /** The rows touch: a row draws the line above it, the last one closes the list underneath. */
    private static final float ROW_GAP = 0f;
    private static final float ROW_PAD_LEFT = 10f;
    private static final float ROW_PAD_RIGHT = 8f;
    private static final float CHILD_INDENT = 16f;
    private static final float ICON_SIZE = 16f;
    private static final float ICON_GAP = 8f;

    private final Array<SettingsDialog.SettingsNode> rootNodes = new Array<>();

    private SettingsDialog.SettingsNode selected;
    /** One row is always the open one: a button of its own would let a second click turn it off. */
    private final ButtonGroup<Button> rows = new ButtonGroup<>();

    public SettingsSidebar() {
        top().left();
    }

    public Array<SettingsDialog.SettingsNode> getRootNodes() {
        return rootNodes;
    }

    public void addNode(SettingsDialog.SettingsNode node) {
        rootNodes.add(node);
        rebuild();
    }

    public void addChildNode(SettingsDialog.SettingsNode parent, SettingsDialog.SettingsNode child) {
        parent.add(child);
        rebuild();
    }

    public SettingsDialog.SettingsNode getSelectedNode() {
        return selected;
    }

    public SettingsNodeValue<?> getSelectedValue() {
        return selected == null ? null : selected.getValue();
    }

    /** Opens a category, telling whoever is listening so the panel beside it follows. */
    public void select(SettingsDialog.SettingsNode node) {
        if (node == null || node == selected) return;

        selected = node;
        rebuild();

        ChangeListener.ChangeEvent event = Pools.obtain(ChangeListener.ChangeEvent.class);
        fire(event);
        Pools.free(event);
    }

    /** Whether anything is open yet: the dialog opens the first category when nothing is. */
    public boolean hasSelection() {
        return selected != null;
    }

    private void rebuild() {
        clearChildren();
        rows.clear();
        //while the list is being built nothing is checked yet, so the rule comes after it
        rows.setMinCheckCount(0);
        rows.setMaxCheckCount(1);

        Array<SettingsDialog.SettingsNode> nodes = new Array<>();
        Array<Boolean> indented = new Array<>();
        for (SettingsDialog.SettingsNode node : rootNodes) {
            nodes.add(node);
            indented.add(false);
            for (SettingsDialog.SettingsNode child : node.getChildren()) {
                nodes.add(child);
                indented.add(true);
            }
        }

        for (int i = 0; i < nodes.size; i++) {
            addRow(nodes.get(i), indented.get(i), shapeOf(i, nodes.size));
        }

        if (selected != null) rows.setMinCheckCount(1);
    }

    /** Only the ends of the list are rounded: what is between them is a plain rectangle. */
    private String shapeOf(int index, int count) {
        if (count == 1) return "settings-row-single";
        if (index == 0) return "settings-row-top";
        if (index == count - 1) return "settings-row-bottom";
        return "settings-row-middle";
    }

    private void addRow(SettingsDialog.SettingsNode node, boolean child, String shape) {
        boolean open = node == selected;
        Color tint = VisUI.getSkin().getColor(open ? "white" : "hyperlap2d-menuitem-grey");

        Button row = new Button(rowStyle(shape));
        row.left();
        row.pad(ROW_PAD_Y, ROW_PAD_LEFT + (child ? CHILD_INDENT : 0), ROW_PAD_Y, ROW_PAD_RIGHT);

        Drawable icon = node.getIcon();
        if (icon != null) {
            Image image = new Image(icon);
            image.setScaling(Scaling.fit);
            image.setColor(tint);
            row.add(image).size(ICON_SIZE).padRight(ICON_GAP);
        } else {
            //an icon-less panel, a plugin's own, keeps its name in line with the others
            row.add().size(ICON_SIZE).padRight(ICON_GAP);
        }
        VisLabel name = new VisLabel(node.getName());
        name.setColor(tint);
        row.add(name).expandX().left();

        rows.add(row);
        row.setChecked(open);
        row.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                select(node);
            }
        });

        add(row).growX().padTop(ROW_GAP).row();
    }

    /**
     * One shape tinted four ways: the list is drawn by its rows, and where a row sits in it decides
     * which of its corners are round.
     */
    private Button.ButtonStyle rowStyle(String shape) {
        Skin skin = VisUI.getSkin();
        NinePatchDrawable background = new NinePatchDrawable(skin.getPatch(shape));

        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = background.tint(ROW_UP);
        style.over = background.tint(ROW_OVER);
        style.checked = background.tint(ROW_OPEN);
        style.checkedOver = background.tint(ROW_OPEN_OVER);
        style.down = background.tint(ROW_OPEN_DOWN);
        return style;
    }
}
