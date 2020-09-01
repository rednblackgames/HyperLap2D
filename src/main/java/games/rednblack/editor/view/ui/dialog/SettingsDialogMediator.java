package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.settings.GeneralSettings;
import games.rednblack.editor.view.ui.settings.PluginsSettings;
import games.rednblack.editor.view.ui.settings.SandboxSettings;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class SettingsDialogMediator extends Mediator<SettingsDialog> {

    private static final String TAG = SettingsDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private SettingsDialog.SettingsNode pluginsSettingsNode;

    public SettingsDialogMediator() {
        super(NAME, new SettingsDialog());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                FileMenu.SETTINGS,
                SettingsDialog.ADD_SETTINGS,
                MsgAPI.ADD_PLUGIN_SETTINGS
        };
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        GeneralSettings generalSettings = new GeneralSettings();
        generalSettings.setSettings(settingsManager.editorConfigVO);
        viewComponent.addSettingsNode(generalSettings);

        SandboxSettings sandboxSettings = new SandboxSettings();
        sandboxSettings.setSettings(settingsManager.editorConfigVO);
        viewComponent.addSettingsNode(sandboxSettings);

        if (settingsManager.editorConfigVO.enablePlugins) {
            PluginsSettings pluginsSettings = new PluginsSettings();
            pluginsSettingsNode = viewComponent.addSettingsNode(pluginsSettings);
        }
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case FileMenu.SETTINGS:
                viewComponent.show(uiStage);
                break;
            case SettingsDialog.ADD_SETTINGS:
                SettingsNodeValue<?> settings = notification.getBody();
                viewComponent.addSettingsNode(settings);
                break;
            case MsgAPI.ADD_PLUGIN_SETTINGS:
                SettingsNodeValue<?> nodeValue = notification.getBody();
                viewComponent.addChildSettingsNode(pluginsSettingsNode, nodeValue);
                break;
        }
    }
}
