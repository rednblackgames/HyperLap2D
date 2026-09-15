package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.data.FrameRange;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.renderer.data.TenPatchVO;
import games.rednblack.editor.renderer.tenpatch.TenPatchDrawable;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.h2d.common.view.ui.widget.TintButton;
import games.rednblack.puremvc.Facade;

/**
 * The 9-patch editor: the canvas with its rulers on the left, and on the right the property grid of
 * everything a ten patch can do (stretch areas, tiling with scrolling tiles, crush mode, gradient) with a
 * live preview.
 *
 * Created by azakhary on 8/18/2015.
 */
public class MainPanel extends H2DDialog {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.ninepatch.MainPanel";

    public static final String SAVE_CLICKED = CLASS_NAME + ".SAVE_CLICKED";

    /** Sized so the dialog still fits the editor's minimum window (920x720). */
    private static final int CANVAS_WIDTH = 540;
    private static final int CANVAS_HEIGHT = 520;
    private static final int SIDE_WIDTH = 350;
    private static final int PREVIEW_HEIGHT = 170;
    private static final int TINT_WIDTH = 34;
    private static final int TINT_HEIGHT = 20;

    private static final String[] CRUSH_MODES = {"Shrink", "Crop", "Crop reversed", "None"};
    private static final int[] CRUSH_MODE_VALUES = {
            TenPatchDrawable.CrushMode.SHRINK,
            TenPatchDrawable.CrushMode.CROP,
            TenPatchDrawable.CrushMode.CROP_REVERSED,
            TenPatchDrawable.CrushMode.NONE
    };
    private static final String CRUSH_TOOLTIP = "What happens when the patch is drawn smaller than its fixed parts: "
            + "shrink them, crop from one side or the other, or overflow the bounds.";
    private static final String OFFSET_TOOLTIP = "Shift of the tiles inside the stretch areas, in pixels.";
    private static final String SPEED_TOOLTIP = "Pixels per second the tiles scroll. Animated at runtime and in the preview.";
    private static final String GRADIENT_TOOLTIP = "Tints every patch with a colour per corner.";

    /** Corner order of TenPatch colours: lower left, upper left, upper right, lower right. */
    private static final int LOWER_LEFT = 0, UPPER_LEFT = 1, UPPER_RIGHT = 2, LOWER_RIGHT = 3;
    private static final String[] CORNER_NAMES = {"Bottom left", "Top left", "Top right", "Bottom right"};

    private final Facade facade;

    private TextureRegion texture;
    private boolean lockUpdates;

    /** Frames and playback of an animated 9-patch, frames null for a still one. */
    private Array<TextureAtlas.AtlasRegion> frames;
    private FrameRange frameRange;
    private int fps;
    private int playMode;
    private final VisLabel animationInfo;

    private final EditingZone editingZone;
    private final PreviewWidget previewWidget;

    private final VisLabel zoomLabel;
    private final VisLabel statusLabel;
    private final VisCheckBox gridSwitch;

    private final VisLabel horizontalCount;
    private final VisLabel verticalCount;
    private final VisLabel selectedLabel;
    private final Spinner startSpinner;
    private final Spinner endSpinner;

    private final VisCheckBox tilingSwitch;
    private final Spinner offsetXSpinner;
    private final Spinner offsetYSpinner;
    private final Spinner speedXSpinner;
    private final Spinner speedYSpinner;
    private final VisSelectBox<String> crushModeSelectBox;

    private final VisCheckBox gradientSwitch;
    private final TintButton[] cornerButtons = new TintButton[4];
    private final Color[] cornerColors = {new Color(Color.WHITE), new Color(Color.WHITE), new Color(Color.WHITE), new Color(Color.WHITE)};
    private final ColorPicker colorPicker = new HyperLapColorPicker();

    private final VisSlider previewWidthSlider;
    private final VisSlider previewHeightSlider;
    private final VisLabel previewWidthValue;
    private final VisLabel previewHeightValue;

    public MainPanel(Facade facade) {
        super("Nine Patch", false);
        addCloseButton();
        closeOnEscape();

        this.facade = facade;

        editingZone = new EditingZone();
        // sized up front so the first fit() has a canvas to fit into, before the dialog is laid out
        editingZone.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        previewWidget = new PreviewWidget();

        // ---- canvas column
        VisTable canvasColumn = new VisTable();
        canvasColumn.add(editingZone).size(CANVAS_WIDTH, CANVAS_HEIGHT).row();

        VisTable toolbar = new VisTable();
        VisTextButton zoomOut = StandardWidgetsFactory.createTextButton("-");
        VisTextButton zoomIn = StandardWidgetsFactory.createTextButton("+");
        VisTextButton fit = StandardWidgetsFactory.createTextButton("Fit");
        VisTextButton actual = StandardWidgetsFactory.createTextButton("1:1");
        zoomLabel = PropertyGrid.value("100%");
        zoomLabel.setAlignment(Align.center);
        gridSwitch = StandardWidgetsFactory.createSwitch("Pixel grid");
        gridSwitch.setChecked(true);
        statusLabel = PropertyGrid.value("");
        statusLabel.setAlignment(Align.right);
        statusLabel.setEllipsis(true);

        toolbar.add(zoomOut).width(26).padRight(2);
        toolbar.add(zoomLabel).width(48);
        toolbar.add(zoomIn).width(26).padLeft(2).padRight(8);
        toolbar.add(fit).width(36).padRight(2);
        toolbar.add(actual).width(36).padRight(12);
        toolbar.add(gridSwitch).padRight(12);
        toolbar.add(statusLabel).growX().minWidth(0);
        canvasColumn.add(toolbar).growX().padTop(6);

        zoomOut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                editingZone.zoomBy(1f / 1.25f);
            }
        });
        zoomIn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                editingZone.zoomBy(1.25f);
            }
        });
        fit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                editingZone.fit();
            }
        });
        actual.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                editingZone.resetZoom();
            }
        });
        gridSwitch.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                editingZone.setShowGrid(gridSwitch.isChecked());
            }
        });
        StandardWidgetsFactory.addTooltip(zoomOut, "Zoom out (mouse wheel)");
        StandardWidgetsFactory.addTooltip(zoomIn, "Zoom in (mouse wheel)");
        StandardWidgetsFactory.addTooltip(fit, "Fit the graphic in the canvas");
        StandardWidgetsFactory.addTooltip(actual, "One graphic pixel per screen pixel");

        // ---- side panel
        VisTable side = new VisTable();
        PropertyGrid grid = PropertyGrid.on(side).padPanel();

        grid.section("Stretch areas");
        horizontalCount = PropertyGrid.value("0");
        verticalCount = PropertyGrid.value("0");
        grid.rowCompact("Horizontal", areaButtons(EditingZone.HORIZONTAL, horizontalCount));
        grid.rowCompact("Vertical", areaButtons(EditingZone.VERTICAL, verticalCount));
        grid.tooltipLastRow("Drag on a ruler to create an area, drag its guides to resize it, right click a guide to remove it.");

        selectedLabel = PropertyGrid.value("-");
        startSpinner = StandardWidgetsFactory.createNumberSelector("default", 0, 0, 99999, 1);
        endSpinner = StandardWidgetsFactory.createNumberSelector("default", 0, 0, 99999, 1);
        VisTable selectedRow = new VisTable();
        selectedRow.add(selectedLabel).width(24).left().padRight(PropertyGrid.LABEL_GAP);
        selectedRow.add(PropertyGrid.subLabel("from")).padRight(PropertyGrid.SUB_LABEL_GAP);
        selectedRow.add(startSpinner).width(PropertyGrid.NUMBER_WIDTH).height(PropertyGrid.FIELD_HEIGHT).padRight(PropertyGrid.PAIR_GAP);
        selectedRow.add(PropertyGrid.subLabel("to")).padRight(PropertyGrid.SUB_LABEL_GAP);
        selectedRow.add(endSpinner).width(PropertyGrid.NUMBER_WIDTH).height(PropertyGrid.FIELD_HEIGHT);
        grid.rowCompact("Selected", selectedRow);
        grid.tooltipLastRow("First and last pixel of the selected area, inclusive. Click a band or a ruler bar to select it.");
        ChangeListener selectedAreaListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (lockUpdates) return;
                int start = ((IntSpinnerModel) startSpinner.getModel()).getValue();
                int end = ((IntSpinnerModel) endSpinner.getModel()).getValue();
                editingZone.setSelectedArea(start, end);
            }
        };
        startSpinner.addListener(selectedAreaListener);
        endSpinner.addListener(selectedAreaListener);

        tilingSwitch = StandardWidgetsFactory.createSwitch();
        grid.section("Tiling", tilingSwitch);
        offsetXSpinner = StandardWidgetsFactory.createNumberSelector("default", 0f, -100000f, 100000f, 1f);
        offsetYSpinner = StandardWidgetsFactory.createNumberSelector("default", 0f, -100000f, 100000f, 1f);
        speedXSpinner = StandardWidgetsFactory.createNumberSelector("default", 0f, -100000f, 100000f, 1f);
        speedYSpinner = StandardWidgetsFactory.createNumberSelector("default", 0f, -100000f, 100000f, 1f);
        grid.pair("Offset", "X", offsetXSpinner, "Y", offsetYSpinner);
        grid.tooltipLastRow(OFFSET_TOOLTIP);
        grid.pair("Speed", "X", speedXSpinner, "Y", speedYSpinner);
        grid.tooltipLastRow(SPEED_TOOLTIP);

        grid.section("Crush");
        crushModeSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        crushModeSelectBox.setItems(CRUSH_MODES);
        grid.row("Below min size", crushModeSelectBox);
        grid.tooltipLastRow(CRUSH_TOOLTIP);

        gradientSwitch = StandardWidgetsFactory.createSwitch();
        grid.section("Gradient", gradientSwitch);
        StandardWidgetsFactory.addTooltip(gradientSwitch, GRADIENT_TOOLTIP);
        for (int i = 0; i < 4; i++) {
            cornerButtons[i] = StandardWidgetsFactory.createTintButton(TINT_WIDTH, TINT_HEIGHT);
            cornerButtons[i].setColorValue(cornerColors[i]);
            StandardWidgetsFactory.addTooltip(cornerButtons[i], CORNER_NAMES[i]);
            final int corner = i;
            cornerButtons[i].addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    openColorPicker(corner);
                }
            });
        }
        grid.rowCompact("Top", cornerPair(cornerButtons[UPPER_LEFT], cornerButtons[UPPER_RIGHT]));
        grid.rowCompact("Bottom", cornerPair(cornerButtons[LOWER_LEFT], cornerButtons[LOWER_RIGHT]));

        grid.section("Preview");
        animationInfo = PropertyGrid.value("Still image");
        grid.rowCompact("Frames", animationInfo);
        grid.tooltipLastRow("Animated 9-patches play a sprite animation: fps, play mode and ranges are edited in the item's properties.");
        grid.wideFill(previewWidget, PREVIEW_HEIGHT);
        previewWidthSlider = StandardWidgetsFactory.createSlider(5, 100, 1);
        previewHeightSlider = StandardWidgetsFactory.createSlider(5, 100, 1);
        previewWidthSlider.setValue(100);
        previewHeightSlider.setValue(100);
        previewWidthValue = PropertyGrid.value("100%");
        previewHeightValue = PropertyGrid.value("100%");
        grid.sliderRow("Box width", previewWidthSlider, previewWidthValue);
        grid.sliderRow("Box height", previewHeightSlider, previewHeightValue);
        grid.tooltipLastRow("Size of the free box of the preview. Shrink it below the fixed parts to see the crush mode.");
        ChangeListener previewSizeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                previewWidthValue.setText(Math.round(previewWidthSlider.getValue()) + "%");
                previewHeightValue.setText(Math.round(previewHeightSlider.getValue()) + "%");
                previewWidget.setCustomSize(previewWidthSlider.getValue() / 100f, previewHeightSlider.getValue() / 100f);
            }
        };
        previewWidthSlider.addListener(previewSizeListener);
        previewHeightSlider.addListener(previewSizeListener);

        ChangeListener optionsListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (lockUpdates) return;
                updateEnabledState();
                updatePreview();
            }
        };
        tilingSwitch.addListener(optionsListener);
        offsetXSpinner.addListener(optionsListener);
        offsetYSpinner.addListener(optionsListener);
        speedXSpinner.addListener(optionsListener);
        speedYSpinner.addListener(optionsListener);
        crushModeSelectBox.addListener(optionsListener);
        gradientSwitch.addListener(optionsListener);

        // ---- dialog
        VisTable content = new VisTable();
        content.add(canvasColumn).top();
        content.add(side).width(SIDE_WIDTH).top().padLeft(PropertyGrid.PANEL_PAD * 2).fillY();
        getContentTable().add(content).grow().padTop(8);

        VisLabel note = PropertyGrid.text("Saving rewrites the .9.png and reloads the scene.");
        VisTextButton saveBtn = StandardWidgetsFactory.createTextButton("Apply and save", "accent");
        saveBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(SAVE_CLICKED);
            }
        });
        getButtonsTable().add(note).left().expandX().padLeft(PropertyGrid.PANEL_PAD);
        getButtonsTable().add(saveBtn).width(150).pad(2);
        getCell(getButtonsTable()).growX();

        editingZone.setListener(new EditingZone.Listener() {
            @Override
            public void changed() {
                updateAreaCounts();
                updateSelection();
                updatePreview();
            }

            @Override
            public void selectionChanged() {
                updateSelection();
            }

            @Override
            public void viewChanged() {
                zoomLabel.setText(Math.round(editingZone.getZoom() * 100f) + "%");
            }

            @Override
            public void statusChanged(String status) {
                statusLabel.setText(status);
            }
        });
    }

    private VisTable areaButtons(final int axis, VisLabel count) {
        VisTable table = new VisTable();
        VisTextButton add = StandardWidgetsFactory.createTextButton("+");
        VisTextButton remove = StandardWidgetsFactory.createTextButton("-");
        table.add(add).width(26).padRight(2);
        table.add(remove).width(26).padRight(PropertyGrid.LABEL_GAP);
        table.add(count).width(60).left();
        add.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                editingZone.addArea(axis);
            }
        });
        remove.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                editingZone.removeArea(axis);
            }
        });
        StandardWidgetsFactory.addTooltip(add, "Add an area in the widest fixed part");
        StandardWidgetsFactory.addTooltip(remove, "Remove the selected area, or the last one");
        return table;
    }

    private VisTable cornerPair(TintButton left, TintButton right) {
        VisTable table = new VisTable();
        table.add(left).padRight(PropertyGrid.PAIR_GAP);
        table.add(right);
        return table;
    }

    private void openColorPicker(final int corner) {
        if (!gradientSwitch.isChecked()) return;
        colorPicker.setListener(new ColorPickerAdapter() {
            @Override
            public void changed(Color newColor) {
                setCornerColor(corner, newColor);
            }

            @Override
            public void finished(Color newColor) {
                setCornerColor(corner, newColor);
            }
        });
        colorPicker.setColor(cornerColors[corner]);
        getStage().addActor(colorPicker.fadeIn());
    }

    private void setCornerColor(int corner, Color color) {
        cornerColors[corner].set(color);
        cornerButtons[corner].setColorValue(color);
        updatePreview();
    }

    private void updateEnabledState() {
        boolean tiling = tilingSwitch.isChecked();
        setEnabled(offsetXSpinner, tiling);
        setEnabled(offsetYSpinner, tiling);
        setEnabled(speedXSpinner, tiling);
        setEnabled(speedYSpinner, tiling);

        boolean gradient = gradientSwitch.isChecked();
        for (TintButton button : cornerButtons) {
            button.setTouchable(gradient ? Touchable.enabled : Touchable.disabled);
            button.getColor().a = gradient ? 1f : 0.35f;
        }
    }

    private static void setEnabled(Spinner spinner, boolean enabled) {
        spinner.setDisabled(!enabled);
        spinner.getColor().a = enabled ? 1f : 0.5f;
    }

    private void updateAreaCounts() {
        horizontalCount.setText(countText(editingZone.getAreaCount(EditingZone.HORIZONTAL)));
        verticalCount.setText(countText(editingZone.getAreaCount(EditingZone.VERTICAL)));
    }

    private static String countText(int count) {
        return count == 1 ? "1 area" : count + " areas";
    }

    private void updateSelection() {
        boolean wasLocked = lockUpdates;
        lockUpdates = true;
        int[] area = editingZone.getSelectedArea();
        int[] limits = editingZone.getSelectedAreaLimits();
        if (area == null || limits == null) {
            selectedLabel.setText("-");
            setEnabled(startSpinner, false);
            setEnabled(endSpinner, false);
        } else {
            selectedLabel.setText(EditingZone.areaName(editingZone.getSelectedAxis(), editingZone.getSelectedPair()));
            IntSpinnerModel startModel = (IntSpinnerModel) startSpinner.getModel();
            IntSpinnerModel endModel = (IntSpinnerModel) endSpinner.getModel();
            startModel.setMin(limits[0]);
            startModel.setMax(limits[1]);
            endModel.setMin(limits[0]);
            endModel.setMax(limits[1]);
            startModel.setValue(area[0], false);
            endModel.setValue(area[1], false);
            setEnabled(startSpinner, true);
            setEnabled(endSpinner, true);
        }
        lockUpdates = wasLocked;
    }

    private void updatePreview() {
        if (lockUpdates || texture == null) return;
        previewWidget.update((TextureAtlas.AtlasRegion) texture, getTenPatchVO(), frames, frameRange, fps, playMode);
    }

    private static int crushModeIndex(int crushMode) {
        for (int i = 0; i < CRUSH_MODE_VALUES.length; i++) {
            if (CRUSH_MODE_VALUES[i] == crushMode) return i;
        }
        return 0;
    }

    /**
     * @param texture region to edit
     * @param vo      configuration to start from, stretch areas and offsets in pixels of {@code texture}
     */
    public void setTexture(TextureRegion texture, TenPatchVO vo) {
        setTexture(texture, vo, null, null, 24, TenPatchDrawable.PlayMode.LOOP);
    }

    /**
     * @param texture region to edit, the first frame for an animated 9-patch
     * @param vo      configuration to start from, stretch areas and offsets in pixels of {@code texture}
     * @param frames  every frame of the animation in order, null for a still 9-patch
     * @param range   frames the preview plays, null for all of them
     */
    public void setTexture(TextureRegion texture, TenPatchVO vo, Array<TextureAtlas.AtlasRegion> frames, FrameRange range, int fps, int playMode) {
        this.texture = texture;
        this.frames = frames;
        this.frameRange = range;
        this.fps = fps;
        this.playMode = playMode;
        lockUpdates = true;

        if (frames == null) {
            animationInfo.setText("Still image");
        } else {
            String rangeText = range == null ? "all" : range.name + " (" + range.startFrame + "-" + range.endFrame + ")";
            animationInfo.setText(frames.size + " frames, " + fps + " fps, range " + rangeText);
        }

        editingZone.setTexture(texture, vo);
        zoomLabel.setText(Math.round(editingZone.getZoom() * 100f) + "%");
        statusLabel.setText("");

        tilingSwitch.setChecked(vo.tiling);
        ((SimpleFloatSpinnerModel) offsetXSpinner.getModel()).setValue(vo.offsetX, false);
        ((SimpleFloatSpinnerModel) offsetYSpinner.getModel()).setValue(vo.offsetY, false);
        ((SimpleFloatSpinnerModel) speedXSpinner.getModel()).setValue(vo.offsetXspeed, false);
        ((SimpleFloatSpinnerModel) speedYSpinner.getModel()).setValue(vo.offsetYspeed, false);
        crushModeSelectBox.setSelectedIndex(crushModeIndex(vo.crushMode));

        gradientSwitch.setChecked(vo.hasGradient());
        setCornerFromVO(LOWER_LEFT, vo.color1);
        setCornerFromVO(UPPER_LEFT, vo.color2);
        setCornerFromVO(UPPER_RIGHT, vo.color3);
        setCornerFromVO(LOWER_RIGHT, vo.color4);

        previewWidthSlider.setValue(100);
        previewHeightSlider.setValue(100);

        updateAreaCounts();
        updateSelection();
        updateEnabledState();
        lockUpdates = false;
        updatePreview();
    }

    private void setCornerFromVO(int corner, float[] rgba) {
        if (rgba != null && rgba.length >= 4) cornerColors[corner].set(rgba[0], rgba[1], rgba[2], rgba[3]);
        else cornerColors[corner].set(Color.WHITE);
        cornerButtons[corner].setColorValue(cornerColors[corner]);
    }

    private float[] cornerToVO(int corner) {
        if (!gradientSwitch.isChecked()) return null;
        Color c = cornerColors[corner];
        return new float[]{c.r, c.g, c.b, c.a};
    }

    /** Current configuration, stretch areas and offsets in pixels of the edited region. */
    public TenPatchVO getTenPatchVO() {
        TenPatchVO vo = new TenPatchVO(editingZone.getHorizontalStretchAreas(), editingZone.getVerticalStretchAreas());
        vo.tiling = tilingSwitch.isChecked();
        vo.offsetX = ((SimpleFloatSpinnerModel) offsetXSpinner.getModel()).getValue();
        vo.offsetY = ((SimpleFloatSpinnerModel) offsetYSpinner.getModel()).getValue();
        vo.offsetXspeed = ((SimpleFloatSpinnerModel) speedXSpinner.getModel()).getValue();
        vo.offsetYspeed = ((SimpleFloatSpinnerModel) speedYSpinner.getModel()).getValue();
        vo.crushMode = CRUSH_MODE_VALUES[Math.max(0, crushModeSelectBox.getSelectedIndex())];
        vo.color1 = cornerToVO(LOWER_LEFT);
        vo.color2 = cornerToVO(UPPER_LEFT);
        vo.color3 = cornerToVO(UPPER_RIGHT);
        vo.color4 = cornerToVO(LOWER_RIGHT);
        return vo;
    }
}
