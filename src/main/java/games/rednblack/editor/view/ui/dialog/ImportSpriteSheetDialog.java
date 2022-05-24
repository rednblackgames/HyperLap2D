package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import org.puremvc.java.interfaces.IFacade;

public class ImportSpriteSheetDialog extends H2DDialog {
    private static final String prefix = "games.rednblack.editor.view.ui.dialog.ImportSpriteSheetDialog";
    public static final String IMPORT_SPRITE_SHEET = prefix + ".IMPORT_SPRITE_SHEET";

    private final VisValidatableTextField width, height;
    private final InputFileWidget imagePathField;
    private final VisTextButton importButton;
    private final IFacade facade;

    public ImportSpriteSheetDialog() {
        super("Import Sprite Sheet Animation");
        addCloseButton();

        this.facade = HyperLap2DFacade.getInstance();

        setModal(true);
        VisTable fileTable = new VisTable();
        fileTable.pad(6);
        //
        fileTable.add(new VisLabel("Sprite Sheet:")).right().padRight(5);
        imagePathField = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, false);
        imagePathField.setTextFieldWidth(156);
        fileTable.add(imagePathField);
        getContentTable().add(fileTable);
        //
        getContentTable().row().padTop(10);
        //

        Validators.IntegerValidator validator = new Validators.IntegerValidator();

        width = StandardWidgetsFactory.createValidableTextField(validator);
        height = StandardWidgetsFactory.createValidableTextField(validator);

        VisTable sizeTable = new VisTable();
        sizeTable.add("Sprite Width:").padRight(3);
        sizeTable.add(width).width(60);
        sizeTable.add("px").padLeft(5);
        sizeTable.row().padTop(5);
        sizeTable.add("Sprite Height:").padRight(3);
        sizeTable.add(height).width(60);
        sizeTable.add("px").padLeft(5);
        getContentTable().add(sizeTable);

        importButton = StandardWidgetsFactory.createTextButton("Import");
        getButtonsTable().add(importButton);
        pack();

        setListeners();
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
