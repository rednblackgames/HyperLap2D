package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.editor.view.ui.widget.InputFileWidget;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ProjectVO;

public class ProjectExportSettings extends SettingsDialog.SettingsNodeValue<ProjectVO> {

    private final InputFileWidget exportSettingsInputFileWidget;
    private final VisCheckBox duplicateCheckBox;
    private VisSelectBox<Integer> widthSelectBox;
    private VisSelectBox<Integer> heightSelectBox;

    public ProjectExportSettings() {
        super("Project Export");
        VisTable mainTable = new VisTable();
        mainTable.padTop(6).padBottom(6);
        mainTable.add("Assets folder:").right().padRight(5);
        exportSettingsInputFileWidget = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES, false);
        mainTable.add(exportSettingsInputFileWidget);
        mainTable.row().padTop(10);
        mainTable.add("Atlas Max Size:").right().top().padRight(5);
        mainTable.add(getDimensionsTable()).left();
        mainTable.row().padTop(10);
        duplicateCheckBox = StandardWidgetsFactory.createCheckBox("Duplicate edge pixels in atlas");
        mainTable.add(duplicateCheckBox).colspan(2);
        mainTable.row().padTop(23);

        getContentTable().add(mainTable).expandX().fillX();
    }

    private Table getDimensionsTable() {
        Integer[] data = {512, 1024, 2048, 4096};
        VisTable dimensionsTable = new VisTable();
        widthSelectBox = StandardWidgetsFactory.createSelectBox(Integer.class);
        widthSelectBox.setItems(data);
        dimensionsTable.add(new VisLabel("Width:")).left().padRight(3);
        dimensionsTable.add(widthSelectBox).width(85).height(21).padRight(3);
        dimensionsTable.row().padTop(10);

        heightSelectBox = StandardWidgetsFactory.createSelectBox(Integer.class);
        heightSelectBox.setItems(data);
        dimensionsTable.add(new VisLabel("Height:")).left().padRight(3);
        dimensionsTable.add(heightSelectBox).width(85).height(21).left();
        return dimensionsTable;
    }

    @Override
    public void translateSettingsToView() {
        ProjectVO projectVO = getSettings();
        String exportPath = "";
        if (projectVO.projectMainExportPath != null) {
            exportPath = projectVO.projectMainExportPath;
        }
        exportSettingsInputFileWidget.setValue(new FileHandle(exportPath));

        widthSelectBox.setSelected(Integer.parseInt(projectVO.texturepackerWidth));
        heightSelectBox.setSelected(Integer.parseInt(projectVO.texturepackerHeight));

        duplicateCheckBox.setChecked(projectVO.texturepackerDuplicate);
    }

    @Override
    public void translateViewToSettings() {
        boolean packerModified = false;
        if (getSettings().texturepackerDuplicate != duplicateCheckBox.isChecked() ||
                !getSettings().texturepackerWidth.equals(String.valueOf(widthSelectBox.getSelected())) ||
                !getSettings().texturepackerHeight.equals(String.valueOf(heightSelectBox.getSelected())))  {
            packerModified = true;
        }
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        projectManager.setTexturePackerSizes(widthSelectBox.getSelected(), heightSelectBox.getSelected());
        projectManager.setTexturePackerDuplicate(duplicateCheckBox.isChecked());

        facade.sendNotification(MsgAPI.SAVE_EXPORT_PATH, exportSettingsInputFileWidget.getValue().file().getAbsolutePath());

        if (packerModified) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions();
        }
    }

    @Override
    public boolean validateSettings() {
        return exportSettingsInputFileWidget.getValue() != null;
    }
}
