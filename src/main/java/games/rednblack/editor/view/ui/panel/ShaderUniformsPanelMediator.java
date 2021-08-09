package games.rednblack.editor.view.ui.panel;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.data.ShaderUniformVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
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

    private int observable = -1;

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
                WindowMenu.SHADER_UNIFORMS_EDITOR_OPEN,
                ShaderUniformsPanel.ADD_BUTTON_CLICKED,
                ShaderUniformsPanel.REMOVE_BUTTON_CLICKED
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
                setObservable(observable);
                viewComponent.show(uiStage);
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                Set<Integer> selection = notification.getBody();
                if (selection.size() == 1) {
                    int entity = selection.iterator().next();
                    setObservable(entity);
                } else {
                    setObservable(-1);
                }
                break;
            case MsgAPI.EMPTY_SPACE_CLICKED:
                setObservable(-1);
                break;
            case ShaderUniformsPanel.ADD_BUTTON_CLICKED:
                addNewUniform(notification.getBody());
                break;
            case ShaderUniformsPanel.REMOVE_BUTTON_CLICKED:
                removeUniform(notification.getBody());
                break;
        }
    }

    private void setObservable(int item) {
        observable = item;
        updateView();
    }

    private void updateView() {
        if (observable == -1) {
            viewComponent.setEmpty();
            return;
        }

        ShaderComponent shaderComponent = SandboxComponentRetriever.get(observable, ShaderComponent.class);
        if (shaderComponent != null)
            viewComponent.updateView(shaderComponent.uniforms, shaderComponent.customUniforms);
        else
            viewComponent.setEmpty("Selected item doesn't have a Shader Component");
    }

    private void addNewUniform(Object[] payload) {
        String name = (String) payload[0];
        ShaderUniformVO vo = new ShaderUniformVO();
        switch (payload.length) {
            case 2:
                if (payload[1] instanceof Integer)
                    vo.set((Integer) payload[1]);
                else if (payload[1] instanceof Float)
                    vo.set((Float) payload[1]);
                break;
            case 3:
                vo.set((Float) payload[1], (Float) payload[2]);
                break;
            case 4:
                vo.set((Float) payload[1], (Float) payload[2], (Float) payload[3]);
                break;
            case 5:
                vo.set((Float) payload[1], (Float) payload[2], (Float) payload[3], (Float) payload[4]);
                break;
        }

        ShaderComponent shaderComponent = SandboxComponentRetriever.get(observable, ShaderComponent.class);
        shaderComponent.customUniforms.put(name, vo);

        updateView();
    }

    private void removeUniform(String uniform) {
        ShaderComponent shaderComponent = SandboxComponentRetriever.get(observable, ShaderComponent.class);
        shaderComponent.customUniforms.remove(uniform);

        updateView();
    }
}
