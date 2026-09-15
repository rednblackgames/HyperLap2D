package games.rednblack.editor.plugin.tiled.view.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import games.rednblack.puremvc.Facade;

public class ImportTileSetDialog extends H2DDialog {
    private static final String prefix = "games.rednblack.editor.plugin.tiled.view.dialog.ImportTileSetDialog";
    public static final String IMPORT_TILESET = prefix + ".IMPORT_TILESET";

    private static final int MIN_WIDTH = 400;

    private final VisValidatableTextField width, height;
    private final InputFileWidget imagePathField;
    private final VisTextButton importButton;
    private final Facade facade;
    private final VisCheckBox removeBlankTileCheck;

    public ImportTileSetDialog(Facade facade) {
        super("Import Tile Set");
        this.facade = facade;

        setModal(true);
        addCloseButton();
        closeOnEscape();

        imagePathField = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, false);
        imagePathField.setTextFieldWidth(PropertyGrid.FIELD_WIDTH);

        Validators.IntegerValidator validator = new Validators.IntegerValidator();
        width = StandardWidgetsFactory.createValidableTextField(validator);
        height = StandardWidgetsFactory.createValidableTextField(validator);
        removeBlankTileCheck = StandardWidgetsFactory.createCheckBox("Skip blank tiles");

        VisTable body = new VisTable();
        getContentTable().add(body).growX();
        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("Tile set");
        grid.rowCompact("Image", imagePathField);
        grid.section("Tile size");
        grid.rowUnit("Width", width, "px");
        grid.rowUnit("Height", height, "px");
        grid.toggleWide("Skip tiles that are fully transparent", removeBlankTileCheck);

        importButton = StandardWidgetsFactory.createTextButton("Import", "accent");
        getButtonsTable().add(importButton).pad(2);
        getCell(getButtonsTable()).right();
        pack();

        setListeners();
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }

    public int getTileWidth() {
        return Integer.parseInt(width.getText());
    }

    public int getTileHeight() {
        return Integer.parseInt(height.getText());
    }

    public Boolean getBlankTileOption(){ return removeBlankTileCheck.isChecked(); }

    private void setListeners() {
        importButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (width.isInputValid() && height.isInputValid() && imagePathField.getValue() != null) {
                    facade.sendNotification(IMPORT_TILESET, imagePathField.getValue());
                }
            }
        });
    }
}
