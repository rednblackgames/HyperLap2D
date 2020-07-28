package games.rednblack.editor.view.ui.settings;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.vo.ProjectVO;

public class LivePreviewSettings extends SettingsNodeValue<ProjectVO> {

    public LivePreviewSettings() {
        super("Live Preview", HyperLap2DFacade.getInstance());

        getContentTable().add("Window").left().row();
        getContentTable().addSeparator();

        getContentTable().add("Background").left().padTop(10).row();
        getContentTable().addSeparator();
    }

    @Override
    public void translateSettingsToView() {

    }

    @Override
    public void translateViewToSettings() {

    }

    @Override
    public boolean validateSettings() {
        return true;
    }
}
