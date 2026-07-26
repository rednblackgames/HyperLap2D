package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.FormRow;
import games.rednblack.h2d.common.view.ui.ListTable;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

import java.util.*;

/**
 * Created by azakhary on 8/1/2015.
 */
public class TagsPanel extends UIDraggablePanel {

    public static final String prefix = "games.rednblack.editor.view.ui.dialog.panels.TagsDialog";
    public static final String ITEM_ADD = prefix + ".ITEM_ADD";
    public static final String ITEM_REMOVED = prefix + ".ITEM_REMOVED";

    private static final int MIN_WIDTH = 380;

    private final Facade facade;

    private final VisTable mainTable;
    private final VisTextField newTagField;
    private final VisTextButton addTagButton;

    private Set<String> tags = new HashSet<>();

    public TagsPanel() {
        super("Tags");
        addCloseButton();

        facade = Facade.getInstance();

        newTagField = StandardWidgetsFactory.createTextField();
        addTagButton = StandardWidgetsFactory.createTextButton("Add");
        addTagButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String tag = newTagField.getText();
                if (!tag.isEmpty() && !tagExists(tag)) {
                    newTagField.setText("");
                    addTag(tag);
                    facade.sendNotification(ITEM_ADD, tag);
                }
            }
        });

        mainTable = new VisTable();
        getContentTable().add(mainTable).growX();
    }

    public void setEmpty() {
        mainTable.clear();
        PropertyGrid.on(mainTable).dialogScale().padPanel()
                .wideCentered(PropertyGrid.value("No item selected"));
        invalidateHeight();
    }

    private void addTag(String tag) {
        tags.add(tag);
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void updateView() {
        mainTable.clear();

        PropertyGrid grid = PropertyGrid.on(mainTable).dialogScale().padPanel();
        grid.section("New tag");
        grid.wideContent(new FormRow().label("Tag").field(newTagField).action(addTagButton));

        grid.section("Tags");
        grid.wideContent(createTagList());

        invalidateHeight();
    }

    private ListTable createTagList() {
        ListTable list = new ListTable("Tag");
        List<String> sorted = new LinkedList<>(tags);
        Collections.sort(sorted);
        if (sorted.isEmpty()) {
            return list.message("This item has no tags");
        }
        for (String tag : sorted) {
            VisImageButton removeButton = StandardWidgetsFactory.createImageButton("trash-button");
            removeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tags.remove(tag);
                    facade.sendNotification(ITEM_REMOVED, tag);
                }
            });
            list.item(tag).action(removeButton);
        }
        return list;
    }

    public Set<String> getTags() {
        return tags;
    }

    private boolean tagExists(String tag) {
        return tags.contains(tag);
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }
}
