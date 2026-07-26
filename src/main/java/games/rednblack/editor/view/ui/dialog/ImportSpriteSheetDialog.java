package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import games.rednblack.puremvc.Facade;

public class ImportSpriteSheetDialog extends H2DDialog {
    private static final String prefix = "games.rednblack.editor.view.ui.dialog.ImportSpriteSheetDialog";
    public static final String IMPORT_SPRITE_SHEET = prefix + ".IMPORT_SPRITE_SHEET";

    private final VisValidatableTextField width, height;
    private final InputFileWidget imagePathField;
    private final VisTextButton importButton;
    private final Facade facade;

    private static final int MIN_WIDTH = 460;
    /** What the two numbers are for: the sheet is cut into frames of exactly this size. */
    private static final String SIZE_TOOLTIP =
            "The size of one frame. The sheet is cut into a grid of frames this big, left to right "
                    + "and top to bottom.";

    public ImportSpriteSheetDialog() {
        super("Import Sprite Sheet Animation");
        addCloseButton();
        closeOnEscape();

        this.facade = Facade.getInstance();

        setModal(true);

        Validators.IntegerValidator validator = new Validators.IntegerValidator();
        imagePathField = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, false);
        width = StandardWidgetsFactory.createValidableTextField(validator);
        height = StandardWidgetsFactory.createValidableTextField(validator);

        VisTable body = new VisTable();
        getContentTable().add(body).growX();
        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("Sprite sheet");
        grid.row("File", imagePathField);

        grid.section("Frame");
        grid.rowUnit("Width", width, "px");
        grid.tooltipLastRow(SIZE_TOOLTIP);
        grid.rowUnit("Height", height, "px");
        grid.tooltipLastRow(SIZE_TOOLTIP);

        importButton = StandardWidgetsFactory.createTextButton("Import", "accent");
        getButtonsTable().add(importButton).pad(2);
        getCell(getButtonsTable()).right();

        setListeners();
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }

    public int getSpriteWidth() {
        return Integer.parseInt(width.getText());
    }

    public int getSpriteHeight() {
        return Integer.parseInt(height.getText());
    }

    private void setListeners() {
        importButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (width.isInputValid() && height.isInputValid() && imagePathField.getValue() != null) {
                    facade.sendNotification(IMPORT_SPRITE_SHEET, imagePathField.getValue());
                }
            }
        });
    }
}
