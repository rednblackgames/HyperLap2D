package games.rednblack.editor.view.ui.settings;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.vo.EditorConfigVO;

public class SandboxSettings extends SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox disableAmbientComposite;

    public SandboxSettings() {
        super("Sandbox", HyperLap2DFacade.getInstance());

        getContentTable().add("Composites").left().row();
        getContentTable().addSeparator();
        disableAmbientComposite = StandardWidgetsFactory.createCheckBox("Disable Ambient light when viewing Composites");
        getContentTable().add(disableAmbientComposite).left().padTop(5).padLeft(8).row();

        getContentTable().add("Background").left().padTop(10).row();
        getContentTable().addSeparator();
    }

    @Override
    public void translateSettingsToView() {
        disableAmbientComposite.setChecked(getSettings().disableAmbientComposite);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().disableAmbientComposite = disableAmbientComposite.isChecked();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return getSettings().disableAmbientComposite != disableAmbientComposite.isChecked();
    }
}
