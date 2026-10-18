package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.data.AutoLayoutVO;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The auto layout of a composite: a live picture of it on top, then how it places its children.
 * Rows that do not apply to the chosen mode are taken away, and the names of the alignments follow
 * the axis they are on.
 */
public class UIAutoLayoutProperties extends UIRemovableProperties {

    public static final String PREFIX = "games.rednblack.editor.view.ui.properties.panels.UIAutoLayoutProperties";
    public static final String CLOSE_CLICKED = PREFIX + ".CLOSE_CLICKED";

    private static final int PREVIEW_HEIGHT = 140;

    static final String[] MODES = {"Row", "Column", "Grid"};
    static final String[] FILL = {"Rows first", "Columns first"};

    private final AutoLayoutPreviewData previewData = new AutoLayoutPreviewData();
    private AutoLayoutPreview preview;
    private VisLabel caption;

    private VisSelectBox<String> modeBox;

    private VisSelectBox<String> justifyBox;
    private VisSelectBox<String> alignBox;
    private VisValidatableTextField gapField;
    private VisCheckBox reverseSwitch;

    private VisValidatableTextField columnsField;
    private VisValidatableTextField cellWidthField, cellHeightField;
    private VisSelectBox<String> cellAlignXBox, cellAlignYBox;
    private VisValidatableTextField columnGapField, rowGapField;
    private VisSelectBox<String> fillBox;

    private VisValidatableTextField padLeftField, padRightField, padTopField, padBottomField;
    private VisCheckBox fitWidthSwitch, fitHeightSwitch;
    private VisCheckBox skipInvisibleSwitch;

    private PropertyGrid.CollapsibleRow linearRows, gridRows;
    private AutoLayoutComponent.Mode shownMode;

    public UIAutoLayoutProperties() {
        super("Auto Layout");
        initView();
        initListeners();
    }

    private void initView() {
        Validators.FloatValidator floatValidator = new Validators.FloatValidator();
        Validators.IntegerValidator integerValidator = new Validators.IntegerValidator();

        preview = new AutoLayoutPreview(previewData);
        caption = PropertyGrid.text("");
        caption.setAlignment(Align.center);
        caption.setEllipsis(true);

        modeBox = StandardWidgetsFactory.createSelectBox(String.class);
        modeBox.setItems(MODES);

        justifyBox = StandardWidgetsFactory.createSelectBox(String.class);
        alignBox = StandardWidgetsFactory.createSelectBox(String.class);
        gapField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        reverseSwitch = StandardWidgetsFactory.createSwitch();

        columnsField = StandardWidgetsFactory.createValidableTextField(integerValidator);
        cellWidthField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        cellHeightField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        cellAlignXBox = StandardWidgetsFactory.createSelectBox(String.class);
        cellAlignXBox.setItems(alignNames(true));
        cellAlignYBox = StandardWidgetsFactory.createSelectBox(String.class);
        cellAlignYBox.setItems(alignNames(false));
        columnGapField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        rowGapField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        fillBox = StandardWidgetsFactory.createSelectBox(String.class);
        fillBox.setItems(FILL);

        padLeftField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        padRightField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        padTopField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        padBottomField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        fitWidthSwitch = StandardWidgetsFactory.createSwitch();
        fitHeightSwitch = StandardWidgetsFactory.createSwitch();
        skipInvisibleSwitch = StandardWidgetsFactory.createSwitch();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.wideFill(preview, PREVIEW_HEIGHT);
        grid.wideFill(caption);
        grid.row("Mode", modeBox);

        VisTable linear = new VisTable();
        PropertyGrid.on(linear, grid)
                .row("Justify", justifyBox).tooltipLastRow("Where the children go when there is room left")
                .row("Align", alignBox).tooltipLastRow("How the children sit across; Stretch makes them fill it")
                .row("Gap", gapField)
                .toggle("Reverse", reverseSwitch);
        linearRows = grid.collapsibleSubGrid(linear);

        VisTable cells = new VisTable();
        PropertyGrid.on(cells, grid)
                .row("Columns", columnsField)
                .pair("Cell size", "W", cellWidthField, "H", cellHeightField).tooltipLastRow("0 makes the cells as large as the largest child")
                .row("Align X", cellAlignXBox)
                .row("Align Y", cellAlignYBox)
                .pair("Gap", "X", columnGapField, "Y", rowGapField)
                .row("Fill", fillBox);
        gridRows = grid.collapsibleSubGrid(cells);

        grid.section("Padding");
        grid.pair("Sides", "L", padLeftField, "R", padRightField);
        grid.pair("", "T", padTopField, "B", padBottomField);
        grid.togglePair("Fit content", "W", fitWidthSwitch, "H", fitHeightSwitch)
                .tooltipLastRow("Makes the composite as large as its children plus padding");
        grid.toggle("Skip hidden", skipInvisibleSwitch).tooltipLastRow("Hidden children take no room");

        showMode(AutoLayoutComponent.Mode.ROW);
    }

    private void initListeners() {
        ChangeListener switchListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                facade.sendNotification(getUpdateEventName());
            }
        };
        modeBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        justifyBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        alignBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        cellAlignXBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        cellAlignYBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        fillBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));

        for (VisValidatableTextField field : new VisValidatableTextField[]{gapField, columnsField, cellWidthField, cellHeightField,
                columnGapField, rowGapField, padLeftField, padRightField, padTopField, padBottomField}) {
            field.addListener(new KeyboardListener(getUpdateEventName()));
        }

        reverseSwitch.addListener(switchListener);
        fitWidthSwitch.addListener(switchListener);
        fitHeightSwitch.addListener(switchListener);
        skipInvisibleSwitch.addListener(switchListener);
    }

    /** Names of the four alignments, said the way they read on that axis. */
    static String[] alignNames(boolean horizontal) {
        return horizontal ? new String[]{"Left", "Center", "Right", "Stretch"} : new String[]{"Top", "Center", "Bottom", "Stretch"};
    }

    static String[] justifyNames(boolean row) {
        return row ? new String[]{"Left", "Center", "Right", "Space between"} : new String[]{"Top", "Center", "Bottom", "Space between"};
    }

    /** Puts up the rows of the mode and names the alignments after its axes. */
    private void showMode(AutoLayoutComponent.Mode mode) {
        if (mode == shownMode) return;
        shownMode = mode;
        boolean grid = mode == AutoLayoutComponent.Mode.GRID;
        linearRows.setContentVisible(!grid);
        gridRows.setContentVisible(grid);
        if (!grid) {
            boolean row = mode == AutoLayoutComponent.Mode.ROW;
            int justify = Math.max(0, justifyBox.getSelectedIndex());
            int align = Math.max(0, alignBox.getSelectedIndex());
            justifyBox.setItems(justifyNames(row));
            alignBox.setItems(alignNames(!row));
            justifyBox.setSelectedIndex(justify);
            alignBox.setSelectedIndex(align);
        }
    }

    public void setData(AutoLayoutVO vo) {
        showMode(vo.mode);
        modeBox.setSelectedIndex(vo.mode.ordinal());
        justifyBox.setSelectedIndex(vo.justify.ordinal());
        alignBox.setSelectedIndex(vo.align.ordinal());
        gapField.setText(number(vo.gap));
        reverseSwitch.setChecked(vo.reverse);

        columnsField.setText(String.valueOf(vo.columns));
        cellWidthField.setText(number(vo.cellWidth));
        cellHeightField.setText(number(vo.cellHeight));
        cellAlignXBox.setSelectedIndex(vo.cellAlignX.ordinal());
        cellAlignYBox.setSelectedIndex(vo.cellAlignY.ordinal());
        columnGapField.setText(number(vo.gap));
        rowGapField.setText(number(vo.rowGap));
        fillBox.setSelectedIndex(vo.columnMajor ? 1 : 0);

        padLeftField.setText(number(vo.padLeft));
        padRightField.setText(number(vo.padRight));
        padTopField.setText(number(vo.padTop));
        padBottomField.setText(number(vo.padBottom));
        fitWidthSwitch.setChecked(vo.fitWidth);
        fitHeightSwitch.setChecked(vo.fitHeight);
        skipInvisibleSwitch.setChecked(vo.skipInvisible);
    }

    /** What the fields say, on top of what the composite has, so a field left half typed changes nothing. */
    public AutoLayoutVO getData(AutoLayoutVO current) {
        AutoLayoutVO vo = new AutoLayoutVO(current);
        vo.mode = AutoLayoutComponent.Mode.values()[Math.max(0, modeBox.getSelectedIndex())];
        // the fields of the other mode still show the old mode's values: only read the ones on screen
        if (current.mode != AutoLayoutComponent.Mode.GRID) {
            vo.justify = AutoLayoutComponent.Justify.values()[Math.max(0, justifyBox.getSelectedIndex())];
            vo.align = AutoLayoutComponent.Align.values()[Math.max(0, alignBox.getSelectedIndex())];
            vo.gap = NumberUtils.toFloat(gapField.getText(), current.gap);
            vo.reverse = reverseSwitch.isChecked();
        } else {
            vo.columns = Math.max(1, NumberUtils.toInt(columnsField.getText(), current.columns));
            vo.cellWidth = Math.max(0, NumberUtils.toFloat(cellWidthField.getText(), current.cellWidth));
            vo.cellHeight = Math.max(0, NumberUtils.toFloat(cellHeightField.getText(), current.cellHeight));
            vo.cellAlignX = AutoLayoutComponent.Align.values()[Math.max(0, cellAlignXBox.getSelectedIndex())];
            vo.cellAlignY = AutoLayoutComponent.Align.values()[Math.max(0, cellAlignYBox.getSelectedIndex())];
            vo.gap = NumberUtils.toFloat(columnGapField.getText(), current.gap);
            vo.rowGap = NumberUtils.toFloat(rowGapField.getText(), current.rowGap);
            vo.columnMajor = fillBox.getSelectedIndex() == 1;
        }
        vo.padLeft = NumberUtils.toFloat(padLeftField.getText(), current.padLeft);
        vo.padRight = NumberUtils.toFloat(padRightField.getText(), current.padRight);
        vo.padTop = NumberUtils.toFloat(padTopField.getText(), current.padTop);
        vo.padBottom = NumberUtils.toFloat(padBottomField.getText(), current.padBottom);
        vo.fitWidth = fitWidthSwitch.isChecked();
        vo.fitHeight = fitHeightSwitch.isChecked();
        vo.skipInvisible = skipInvisibleSwitch.isChecked();
        return vo;
    }

    public AutoLayoutPreviewData getPreviewData() {
        return previewData;
    }

    public void setPreviewRefresher(Runnable refresher) {
        preview.setRefresher(refresher);
    }

    public void setCaption(String text) {
        if (!text.contentEquals(caption.getText())) caption.setText(text);
    }

    static String number(float value) {
        return value == (int) value ? String.valueOf((int) value) : String.valueOf(value);
    }

    @Override
    public void onRemove() {
        facade.sendNotification(CLOSE_CLICKED);
    }
}
