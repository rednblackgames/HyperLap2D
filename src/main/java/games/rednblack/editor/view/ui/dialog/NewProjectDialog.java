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

package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.editor.view.ui.validator.GreaterThanIntegerValidator;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import games.rednblack.puremvc.Facade;
import org.apache.commons.lang3.math.NumberUtils;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;

/**
 * The first thing anyone does in the editor, so it shows what it is about to create: the form on the
 * left, and on the right a card drawing the canvas at its real proportions with the numbers that
 * follow from it. Presets fill the form in one click for the sizes most projects start from.
 */
public class NewProjectDialog extends H2DDialog {
    private static final String prefix = "games.rednblack.editor.view.ui.dialog.NewProjectDialog";
    public static final String CREATE_BTN_CLICKED = prefix + ".CREATE_BTN_CLICKED";
    private static final String DEFAULT_ORIGIN_WIDTH = "1920";
    private static final String DEFAULT_ORIGIN_HEIGHT = "1080";
    private static final String DEFAULT_PPWU = "1";

    /** Sizes offered as one click presets: landscape HD, half of it, and portrait HD. */
    private static final int[][] PRESETS = {{1920, 1080}, {1280, 720}, {1080, 1920}};

    private static final int MIN_WIDTH = 620;
    private static final int NUMBER_WIDTH = 64;
    private static final int CREATE_WIDTH = 100;
    private static final int CONTENT_PAD_TOP = 12;
    private static final int CARD_WIDTH = 220;
    private static final int CARD_PAD = 12;
    private static final int PREVIEW_WIDTH = 184;
    private static final int PREVIEW_HEIGHT = 116;
    private static final String UNKNOWN = "-";
    /** What the number actually decides, which is more than its name suggests. */
    private static final String PPWU_TOOLTIP =
            "How many art pixels make one world unit. At 1 a 100px sprite is 100 units wide, at 100 "
                    + "it is 1 unit. It scales sprites, fonts and composites, and sets the physics "
                    + "scale: Box2D bodies behave best between 0.1 and 10 units.";

    private final InputFileWidget workspacePathField;
    private final VisValidatableTextField projectName;
    private final VisValidatableTextField originWidthTextField;
    private final VisValidatableTextField originHeightTextField;
    private final VisValidatableTextField pixelsPerWorldUnitField;

    private final VisLabel aspectLabel;
    private final VisLabel pixelSizeLabel;
    private final VisLabel worldSizeLabel;

    private String defaultWorkspacePath;

    NewProjectDialog() {
        super("Create New Project");

        setModal(true);
        addCloseButton();
        closeOnEscape();

        VisTable form = new VisTable();
        PropertyGrid grid = PropertyGrid.on(form).dialogScale().padPanel();

        projectName = StandardWidgetsFactory.createValidableTextField(new StringNameValidator());
        workspacePathField = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES, false);

        VisTextField.TextFieldFilter.DigitsOnlyFilter digitsOnly = new VisTextField.TextFieldFilter.DigitsOnlyFilter();
        originWidthTextField = numberField(DEFAULT_ORIGIN_WIDTH, new Validators.IntegerValidator(), digitsOnly);
        originHeightTextField = numberField(DEFAULT_ORIGIN_HEIGHT, new Validators.IntegerValidator(), digitsOnly);
        pixelsPerWorldUnitField = numberField(DEFAULT_PPWU, new GreaterThanIntegerValidator(1, true), digitsOnly);

        aspectLabel = grid.valueLabel(UNKNOWN);
        pixelSizeLabel = StandardWidgetsFactory.createLabel(UNKNOWN,
                PropertyGrid.style(PropertyGrid.LABEL_STYLE_LARGE), Align.left);
        worldSizeLabel = StandardWidgetsFactory.createLabel(UNKNOWN,
                PropertyGrid.style(PropertyGrid.LABEL_STYLE_LARGE), Align.left);

        grid.section("Project");
        grid.row("Name", projectName);
        grid.row("Folder", workspacePathField);

        grid.section("Resolution");
        grid.rowUnit("Width", originWidthTextField, "px");
        grid.rowUnit("Height", originHeightTextField, "px");
        grid.rowUnit("Pixels per unit", pixelsPerWorldUnitField, "px");
        grid.tooltipLastRow(PPWU_TOOLTIP);
        grid.rowCompact("Presets", createPresets());

        VisTable content = new VisTable();
        content.add(form).growX().top();
        content.add(createCanvasCard()).width(CARD_WIDTH).top().padLeft(PropertyGrid.PANEL_PAD);
        getContentTable().add(content).grow().padTop(CONTENT_PAD_TOP);

        VisTextButton createBtn = StandardWidgetsFactory.createTextButton("Create", "accent");
        createBtn.addListener(new BtnClickListener(CREATE_BTN_CLICKED));
        getButtonsTable().add(createBtn).width(CREATE_WIDTH).pad(2);
        getCell(getButtonsTable()).right();

        updateSummary();
    }

    /** The card standing for the project: its canvas drawn to scale, then the numbers behind it. */
    private VisTable createCanvasCard() {
        VisTable card = new VisTable();
        card.setBackground(VisUI.getSkin().getDrawable("table-bg"));
        card.pad(CARD_PAD);
        card.add(new CanvasPreview()).size(PREVIEW_WIDTH, PREVIEW_HEIGHT).row();
        card.add(aspectLabel).left().padTop(CARD_PAD).row();
        card.add(pixelSizeLabel).left().padTop(4).row();
        card.add(worldSizeLabel).left().padTop(2).row();
        return card;
    }

    private VisTable createPresets() {
        VisTable presets = new VisTable();
        for (int[] preset : PRESETS) {
            VisTextButton button = StandardWidgetsFactory.createTextButton(preset[0] + "x" + preset[1]);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    originWidthTextField.setText(String.valueOf(preset[0]));
                    originHeightTextField.setText(String.valueOf(preset[1]));
                    updateSummary();
                }
            });
            presets.add(button).height(PropertyGrid.FIELD_HEIGHT)
                    .padRight(presets.getCells().size < PRESETS.length - 1 ? PropertyGrid.SUB_LABEL_GAP : 0);
        }
        return presets;
    }

    /** A pixel count: digits only, and the card follows whatever is typed. */
    private VisValidatableTextField numberField(String initial, InputValidator validator,
                                               VisTextField.TextFieldFilter filter) {
        VisValidatableTextField field =
                StandardWidgetsFactory.createValidableTextField(initial, "light", validator, filter);
        field.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateSummary();
            }
        });
        return field;
    }

    /** A number field followed by the unit it is measured in. */
    private VisTable withUnit(VisValidatableTextField field) {
        VisTable row = new VisTable();
        row.add(field).width(NUMBER_WIDTH).height(PropertyGrid.FIELD_HEIGHT);
        row.add(StandardWidgetsFactory.createLabel("px",
                PropertyGrid.style(PropertyGrid.LABEL_STYLE_LARGE), Align.left))
                .padLeft(PropertyGrid.LABEL_GAP);
        return row;
    }

    @Override
    public VisDialog show(Stage stage, Action action) {
        originWidthTextField.setText(DEFAULT_ORIGIN_WIDTH);
        originHeightTextField.setText(DEFAULT_ORIGIN_HEIGHT);
        workspacePathField.resetData();
        workspacePathField.setValue(new FileHandle(defaultWorkspacePath));
        updateSummary();
        return super.show(stage, action);
    }

    public String getOriginWidth() {
        return originWidthTextField.getText();
    }

    public String getPixelPerWorldUnit() {
        return pixelsPerWorldUnitField.getText();
    }

    public String getOriginHeight() {
        return originHeightTextField.getText();
    }

    public String getDefaultWorkspacePath() {
        return defaultWorkspacePath;
    }

    public void setDefaultWorkspacePath(String defaultWorkspacePath) {
        this.defaultWorkspacePath = defaultWorkspacePath;
    }

    /**
     * Keeps the card in step with the form. An unusable pixels per unit is already flagged by the
     * field's own error border, so the sizes that depend on it just read as unknown.
     */
    private void updateSummary() {
        int width = NumberUtils.toInt(getOriginWidth());
        int height = NumberUtils.toInt(getOriginHeight());
        int ppwu = NumberUtils.toInt(getPixelPerWorldUnit(), 0);

        aspectLabel.setText(width > 0 && height > 0 ? aspectRatio(width, height) : UNKNOWN);
        pixelSizeLabel.setText(width > 0 && height > 0 ? width + " x " + height + " px" : UNKNOWN);
        worldSizeLabel.setText(width > 0 && height > 0 && ppwu >= 1
                ? width / ppwu + " x " + height / ppwu + " units"
                : UNKNOWN);
    }

    /** "16:9" where the sides reduce to something readable, "1.85 : 1" where they do not. */
    private static String aspectRatio(int width, int height) {
        int divisor = gcd(width, height);
        int w = width / divisor, h = height / divisor;
        if (w <= 32 && h <= 32) return w + ":" + h;
        return String.format("%.2f : 1", width / (float) height);
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }

    /** The canvas at its real proportions, scaled to fit the card. */
    private class CanvasPreview extends Actor {
        private static final float PAD = 4f;
        private static final float BORDER = 1.5f;

        private final Color fill = new Color(27 / 255f, 161 / 255f, 226 / 255f, 0.16f);
        private final Color border = new Color(27 / 255f, 161 / 255f, 226 / 255f, 0.9f);
        private final Color empty = new Color(1f, 1f, 1f, 0.10f);
        /** Scratch colour: the drawn one is the palette colour faded with the dialog. */
        private final Color tint = new Color();

        private ShapeDrawer shapeDrawer;

        @Override
        protected void setStage(Stage stage) {
            super.setStage(stage);
            if (stage != null) {
                shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion);
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (shapeDrawer == null) return;
            shapeDrawer.update();

            // ShapeDrawer paints with colours of its own, so the fade the dialog applies to its
            // children has to be folded in by hand: without this the preview stays opaque while
            // everything around it fades in and out.
            float alpha = parentAlpha * getColor().a;
            if (alpha <= 0f) return;

            int width = NumberUtils.toInt(getOriginWidth());
            int height = NumberUtils.toInt(getOriginHeight());
            if (width <= 0 || height <= 0) {
                shapeDrawer.setColor(faded(empty, alpha));
                shapeDrawer.rectangle(getX() + PAD, getY() + PAD,
                        getWidth() - PAD * 2, getHeight() - PAD * 2, 1f);
                return;
            }

            float scale = Math.min((getWidth() - PAD * 2) / width, (getHeight() - PAD * 2) / height);
            float canvasWidth = width * scale, canvasHeight = height * scale;
            float x = getX() + (getWidth() - canvasWidth) / 2f;
            float y = getY() + (getHeight() - canvasHeight) / 2f;

            shapeDrawer.setColor(faded(fill, alpha));
            shapeDrawer.filledRectangle(x, y, canvasWidth, canvasHeight);
            shapeDrawer.setColor(faded(border, alpha));
            shapeDrawer.rectangle(x, y, canvasWidth, canvasHeight, BORDER);
        }

        private Color faded(Color color, float alpha) {
            return tint.set(color).mul(1f, 1f, 1f, alpha);
        }
    }

    private class BtnClickListener extends ClickListener {
        private final String command;

        public BtnClickListener(String command) {
            this.command = command;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            Facade facade = Facade.getInstance();
            if (projectName.isInputValid() && pixelsPerWorldUnitField.isInputValid()
                    && originHeightTextField.isInputValid() && originWidthTextField.isInputValid()) {
                facade.sendNotification(command,
                        workspacePathField.getValue().path() + File.separator + projectName.getText());
            }
        }
    }
}
