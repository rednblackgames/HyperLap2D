package games.rednblack.editor.view.ui.panel;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.menu.WindowMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.properties.panels.UIShaderProperties;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.Set;

public class ShaderUniformsPanelMediator extends Mediator<ShaderUniformsPanel> {
    private static final String TAG = ShaderUniformsPanelMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private Entity observable = null;

    public ShaderUniformsPanelMediator() {
        super(NAME, new ShaderUniformsPanel());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        viewComponent.setEmpty();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.ITEM_SELECTION_CHANGED,
                MsgAPI.EMPTY_SPACE_CLICKED,
                UIShaderProperties.UNIFORMS_BUTTON_CLICKED,
                WindowMenu.SHADER_UNIFORMS_EDITOR_OPEN
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case WindowMenu.SHADER_UNIFORMS_EDITOR_OPEN:
            case UIShaderProperties.UNIFORMS_BUTTON_CLICKED:
                viewComponent.show(uiStage);
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                Set<Entity> selection = notification.getBody();
                if(selection.size() == 1) {
                    setObservable(selection.iterator().next());
                }
                break;
            case MsgAPI.EMPTY_SPACE_CLICKED:
                setObservable(null);
                break;
        }
    }

    private void setObservable(Entity item) {
        observable = item;
        updateView();
    }

    private void updateView() {
        if(observable == null) {
            viewComponent.setEmpty();
        } else {
            //Update view component
        }
    }
}
