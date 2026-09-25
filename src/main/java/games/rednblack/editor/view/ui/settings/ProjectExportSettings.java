package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.files.FileHandle;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.TexturePackerVO;
import games.rednblack.puremvc.Facade;

public class ProjectExportSettings extends SettingsNodeValue<ProjectVO> {

    private final InputFileWidget exportSettingsInputFileWidget;
    private final VisCheckBox duplicateCheckBox;
    private final VisCheckBox forceSquareCheckBox;
    private final VisCheckBox fastCheckBox;
    private final VisCheckBox legacyCheckBox;
    private final VisSelectBox<Integer> widthSelectBox;
    private final VisSelectBox<Integer> heightSelectBox;
    private final VisSelectBox<String> filterMagSelectBox;
    private final VisSelectBox<String> filterMinSelectBox;

    public ProjectExportSettings(Facade facade) {
        super("Project Export", facade);

        duplicateCheckBox = StandardWidgetsFactory.createSwitch();
        forceSquareCheckBox = StandardWidgetsFactory.createSwitch();
        legacyCheckBox = StandardWidgetsFactory.createSwitch();
        fastCheckBox = StandardWidgetsFactory.createSwitch();
        exportSettingsInputFileWidget = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES, false);
        widthSelectBox = StandardWidgetsFactory.createSelectBox(Integer.class);
        heightSelectBox = StandardWidgetsFactory.createSelectBox(Integer.class);
        filterMagSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        filterMinSelectBox = StandardWidgetsFactory.createSelectBox(String.class);

        Integer[] sizes = {512, 1024, 2048, 4096, 8192};
        widthSelectBox.setItems(sizes);
        heightSelectBox.setItems(sizes);

        String[] filters = {"Linear", "Nearest", "MipMap", "MipMapNearestNearest", "MipMapLinearNearest",
                "MipMapNearestLinear", "MipMapLinearLinear"};
        filterMagSelectBox.setItems(filters);
        filterMinSelectBox.setItems(filters);

        PropertyGrid grid = PropertyGrid.on(getContentTable()).dialogScale().sectionPad(SECTION_PAD_TOP, SECTION_PAD_BOTTOM);

        grid.section("Export");
        grid.row("Export folder", exportSettingsInputFileWidget);

        grid.section("Texture packer");
        grid.pair("Atlas max size", "W", widthSelectBox, "H", heightSelectBox);
        grid.row("Filter mag", filterMagSelectBox);
        grid.row("Filter min", filterMinSelectBox);
        grid.toggleWide("Fast packing", fastCheckBox,
                "Packs your atlas faster, but result may not be efficient and produce extra atlas pages. Do not use for production export.");
        grid.toggleWide("Duplicate edge pixels in atlas", duplicateCheckBox);
        grid.toggleWide("Legacy libGDX format", legacyCheckBox);
        grid.toggleWide("Force square atlas", forceSquareCheckBox);
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
        fastCheckBox.setChecked(vo.fast);
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
        vo.fast = fastCheckBox.isChecked();

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

    @Override
    public boolean requireRestart() {
        return false;
    }
}
