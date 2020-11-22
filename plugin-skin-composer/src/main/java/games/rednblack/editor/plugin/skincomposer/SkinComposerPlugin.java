package games.rednblack.editor.plugin.skincomposer;

import games.rednblack.h2d.common.MenuAPI;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

@Implementation(authors = "fgnm", version = "0.0.1")
public class SkinComposerPlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.skincomposer";

    public static final String PANEL_OPEN = CLASS_NAME + ".PANEL_OPEN";
    public static final String DOWNLOAD_JAR = CLASS_NAME + ".DOWNLOAD_JAR";

    private final SkinComposerMediator skinComposerMediator;
    private final SkinComposerVO settingsVO = new SkinComposerVO();

    public SkinComposerPlugin() {
        super(CLASS_NAME);
        skinComposerMediator = new SkinComposerMediator(this);
    }

    @Override
    public void initPlugin() {
        facade.registerMediator(skinComposerMediator);
        pluginAPI.addMenuItem(MenuAPI.WINDOW_MENU, "Skin Composer", PANEL_OPEN);
        SkinComposerSettings settings = new SkinComposerSettings(facade, this);

        settingsVO.fromStorage(getStorage());
        settings.setSettings(settingsVO);
        facade.sendNotification(MsgAPI.ADD_PLUGIN_SETTINGS, settings);
    }

    public SkinComposerVO getSettingsVO() {
        return settingsVO;
    }
}
