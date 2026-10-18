package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.data.AutoLayoutItemVO;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * How a child takes part in the auto layout of its parent, under a picture of that layout with the
 * child pointed out. In a grid the cells decide size and alignment, so only leaving out applies.
 */
public class UIAutoLayoutItemProperties extends UIItemCollapsibleProperties {

    private static final int PREVIEW_HEIGHT = 100;
    private static final String AUTO = "As the layout";

    private final AutoLayoutPreviewData previewData = new AutoLayoutPreviewData();
    private AutoLayoutPreview preview;

    private VisValidatableTextField growField;
    private VisSelectBox<String> alignBox;
    private VisCheckBox ignoreSwitch;

    private AutoLayoutComponent.Mode shownMode;

    public UIAutoLayoutItemProperties() {
        super("Auto Layout Item");
        initView();
        initListeners();
    }

    private void initView() {
        preview = new AutoLayoutPreview(previewData);
        growField = StandardWidgetsFactory.createValidableTextField(new Validators.FloatValidator());
        alignBox = StandardWidgetsFactory.createSelectBox(String.class);
        ignoreSwitch = StandardWidgetsFactory.createSwitch();

        PropertyGrid.on(mainTable)
                .wideFill(preview, PREVIEW_HEIGHT)
                .row("Grow", growField).tooltipLastRow("Share of the room left, against the other children that grow; 0 keeps its own length")
                .row("Align", alignBox).tooltipLastRow("How it sits across, instead of the way the layout says")
                .toggle("Left out", ignoreSwitch).tooltipLastRow("Stays where it is put and takes no room");

        showMode(AutoLayoutComponent.Mode.ROW);
    }

    private void initListeners() {
        growField.addListener(new KeyboardListener(getUpdateEventName()));
        alignBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        ignoreSwitch.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                facade.sendNotification(getUpdateEventName());
            }
        });
    }

    private void showMode(AutoLayoutComponent.Mode mode) {
        if (mode == shownMode) return;
        shownMode = mode;
        int selected = Math.max(0, alignBox.getSelectedIndex());
        Array<String> names = new Array<>();
        names.add(AUTO);
        names.addAll(UIAutoLayoutProperties.alignNames(mode == AutoLayoutComponent.Mode.COLUMN));
        alignBox.setItems(names);
        alignBox.setSelectedIndex(selected);
    }

    public void setData(AutoLayoutComponent.Mode parentMode, AutoLayoutItemVO vo) {
        showMode(parentMode);
        boolean grid = parentMode == AutoLayoutComponent.Mode.GRID;
        growField.setText(UIAutoLayoutProperties.number(vo.grow));
        alignBox.setSelectedIndex(vo.alignSelf == null ? 0 : vo.alignSelf.ordinal() + 1);
        ignoreSwitch.setChecked(vo.ignore);

        boolean placed = !vo.ignore;
        growField.setDisabled(grid || !placed);
        alignBox.setDisabled(grid || !placed);
    }

    public AutoLayoutItemVO getData(AutoLayoutItemVO current) {
        AutoLayoutItemVO vo = new AutoLayoutItemVO(current);
        vo.grow = Math.max(0, NumberUtils.toFloat(growField.getText(), current.grow));
        int align = alignBox.getSelectedIndex();
        vo.alignSelf = align <= 0 ? null : AutoLayoutComponent.Align.values()[align - 1];
        vo.ignore = ignoreSwitch.isChecked();
        return vo;
    }

    public AutoLayoutPreviewData getPreviewData() {
        return previewData;
    }

    public void setPreviewRefresher(Runnable refresher) {
        preview.setRefresher(refresher);
    }
}
