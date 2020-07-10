package games.rednblack.editor.plugin.skincomposer;

import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

@Implementation(authors = "fgnm", version = "0.0.1")
public class SkinComposerPlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.skincomposer";

    public static final String PANEL_OPEN = CLASS_NAME + ".PANEL_OPEN";
    public static final String WINDOWS_MENU = "games.rednblack.editor.view.HyperLap2DMenuBar.WINDOW_MENU";

    private final SkinComposerMediator skinComposerMediator;

    public SkinComposerPlugin() {
        skinComposerMediator = new SkinComposerMediator(this);
    }

    @Override
    public void initPlugin() {
        facade.registerMediator(skinComposerMediator);
        pluginAPI.addMenuItem(WINDOWS_MENU, "Skin Composer", PANEL_OPEN);
    }
}
