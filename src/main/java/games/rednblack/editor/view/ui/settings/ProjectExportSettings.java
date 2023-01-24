package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.TexturePackerVO;

public class ProjectExportSettings extends SettingsNodeValue<ProjectVO> {

    private final InputFileWidget exportSettingsInputFileWidget;
    private final VisCheckBox duplicateCheckBox;
    private final VisCheckBox forceSquareCheckBox;
    private final VisCheckBox legacyCheckBox;
    private final VisSelectBox<Integer> widthSelectBox;
    private final VisSelectBox<Integer> heightSelectBox;
    private final VisSelectBox<String> filterMagSelectBox;
    private final VisSelectBox<String> filterMinSelectBox;

    public ProjectExportSettings() {
        super("Project Export", HyperLap2DFacade.getInstance());
        duplicateCheckBox = StandardWidgetsFactory.createCheckBox("Duplicate edge pixels in atlas");
        forceSquareCheckBox = StandardWidgetsFactory.createCheckBox("Force Square");
        legacyCheckBox = StandardWidgetsFactory.createCheckBox("Legacy libGDX format");
        exportSettingsInputFileWidget = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES, false);
        widthSelectBox = StandardWidgetsFactory.createSelectBox(Integer.class);
        heightSelectBox = StandardWidgetsFactory.createSelectBox(Integer.class);
        filterMagSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        filterMinSelectBox = StandardWidgetsFactory.createSelectBox(String.class);

        VisTable assetsTable = new VisTable();
        assetsTable.add("Export folder:").padRight(15).left();
        assetsTable.add(exportSettingsInputFileWidget).growX().row();
        getContentTable().add(assetsTable).padTop(5).fillX().expandX().growX().row();

        getContentTable().add("Texture Packer Settings").padTop(15).left().row();
        getContentTable().addSeparator().row();
        VisTable texturePackerTable = new VisTable();
        texturePackerTable.left();
        texturePackerTable.add("Atlas Max Size:").left().top().padRight(5).padTop(10);
        texturePackerTable.add(getDimensionsTable()).padTop(10).left();
        texturePackerTable.row().padTop(10);
        texturePackerTable.add("Atlas Filter:").left().top().padRight(5).padTop(10);
        texturePackerTable.add(getFilterTable()).padTop(10).left();
        texturePackerTable.row().padTop(10);
        texturePackerTable.add(duplicateCheckBox).left().colspan(2).row();
        texturePackerTable.add(legacyCheckBox).left().colspan(2).row();
        texturePackerTable.add(forceSquareCheckBox).left().row();
        texturePackerTable.row().padTop(23);

        getContentTable().add(texturePackerTable).padLeft(8).fillX().expandX().growX().row();
    }

    private Table getDimensionsTable() {
        Integer[] data = {512, 1024, 2048, 4096, 8192};
        VisTable dimensionsTable = new VisTable();

        widthSelectBox.setItems(data);
        dimensionsTable.add(new VisLabel("Width:")).left().padRight(3);
        dimensionsTable.add(widthSelectBox).width(85).height(21).padRight(3);
        dimensionsTable.row().padTop(10);
        heightSelectBox.setItems(data);
        dimensionsTable.add(new VisLabel("Height:")).left().padRight(3);
        dimensionsTable.add(heightSelectBox).width(85).height(21).left();
        return dimensionsTable;
    }

    private Table getFilterTable() {
        String[] data = {"Linear", "Nearest", "MipMap", "MipMapNearestNearest", "MipMapLinearNearest",
                "MipMapNearestLinear", "MipMapLinearLinear"};
        VisTable filterTable = new VisTable();

        filterMagSelectBox.setItems(data);
        filterTable.add(new VisLabel("Mag:")).left().padRight(3);
        filterTable.add(filterMagSelectBox).width(85).height(21).padRight(3);
        filterTable.row().padTop(10);
        filterMinSelectBox.setItems(data);
        filterTable.add(new VisLabel("Min:")).left().padRight(3);
        filterTable.add(filterMinSelectBox).width(85).height(21).left();
        return filterTable;
    }

    @Override
    public void translateSettingsToView() {
        ProjectVO projectVO = getSettings();
        String exportPath = "";
        if (projectVO.projectMainExportPath != null) {
            exportPath = projectVO.projectMainExportPath;
        }
        exportSettingsInputFileWidget.setValue(new FileHandle(exportPath));

        TexturePackerVO vo = projectVO.texturePackerVO;

        widthSelectBox.setSelected(Integer.parseInt(vo.maxWidth));
        heightSelectBox.setSelected(Integer.parseInt(vo.maxHeight));

        duplicateCheckBox.setChecked(vo.duplicate);
        legacyCheckBox.setChecked(vo.legacy);
        forceSquareCheckBox.setChecked(vo.square);

        filterMagSelectBox.setSelected(vo.filterMag);
        filterMinSelectBox.setSelected(vo.filterMin);
    }

    @Override
    public void translateViewToSettings() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        TexturePackerVO backup = new TexturePackerVO(projectManager.currentProjectVO.texturePackerVO);

        TexturePackerVO vo = new TexturePackerVO();
        vo.maxWidth = String.valueOf(widthSelectBox.getSelected());
        vo.maxHeight = String.valueOf(heightSelectBox.getSelected());
        vo.duplicate = duplicateCheckBox.isChecked();
        vo.legacy = legacyCheckBox.isChecked();
        vo.square = forceSquareCheckBox.isChecked();
        vo.filterMag = filterMagSelectBox.getSelected();
        vo.filterMin = filterMinSelectBox.getSelected();

        boolean packerModified = !vo.equals(backup);
        projectManager.setTexturePackerVO(vo);

        if (packerModified) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(false, new ResolutionManager.RepackCallback() {
                @Override
                public void onRepack(boolean success) {
                    if (!success) {
                        facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "Invalid properties selected, revert settings");
                        projectManager.setTexturePackerVO(backup);
                        resolutionManager.rePackProjectImagesForAllResolutions(false);
                        translateSettingsToView();
                    }
                }
            });
        }

        facade.sendNotification(MsgAPI.SAVE_EXPORT_PATH, exportSettingsInputFileWidget.getValue().file().getAbsolutePath());
    }

    @Override
    public boolean validateSettings() {
        return exportSettingsInputFileWidget.getValue() != null;
    }
}
