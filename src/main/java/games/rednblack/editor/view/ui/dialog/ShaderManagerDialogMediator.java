package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.code.syntax.GLSLSyntax;
import games.rednblack.editor.controller.commands.resource.DeleteShaderCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ShaderManagerDialogMediator extends Mediator<ShaderManagerDialog> {

    private static final String TAG = ShaderManagerDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private final GLSLSyntax glslSyntax = new GLSLSyntax();

    public ShaderManagerDialogMediator() {
        super(NAME, new ShaderManagerDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ResourcesMenu.OPEN_SHADER_MANAGER,
                ProjectManager.PROJECT_OPENED,
                ProjectManager.PROJECT_DATA_UPDATED,
                ShaderManagerDialog.EDIT_FRAGMENT_SHADER_DONE,
                ShaderManagerDialog.EDIT_VERTEX_SHADER_DONE,
                ShaderManagerDialog.EDIT_FRAGMENT_SHADER,
                ShaderManagerDialog.EDIT_VERTEX_SHADER,
                DeleteShaderCommand.DONE
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        ResourceManager rm = facade.retrieveProxy(ResourceManager.NAME);
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        File shader;
        Object[] payload;
        String shaderName;

        switch (notification.getName()) {
            case ResourcesMenu.OPEN_SHADER_MANAGER:
                viewComponent.show(uiStage);
                break;
            case ProjectManager.PROJECT_DATA_UPDATED:
            case DeleteShaderCommand.DONE:
            case ProjectManager.PROJECT_OPENED:
                viewComponent.updateShaderList(rm.getShaders().keySet());
                break;
            case ShaderManagerDialog.EDIT_FRAGMENT_SHADER:
                shaderName = notification.getBody();
                shader = new File(projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName + ".frag");
                payload = CodeEditorDialogMediator.openCodeEditorPayload(glslSyntax, "", ShaderManagerDialog.EDIT_FRAGMENT_SHADER_DONE, shader, shaderName);
                facade.sendNotification(MsgAPI.OPEN_CODE_EDITOR, payload);
                break;
            case ShaderManagerDialog.EDIT_VERTEX_SHADER:
                shaderName = notification.getBody();
                shader = new File(projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName + ".vert");
                payload = CodeEditorDialogMediator.openCodeEditorPayload(glslSyntax, "", ShaderManagerDialog.EDIT_VERTEX_SHADER_DONE, shader, shaderName);
                facade.sendNotification(MsgAPI.OPEN_CODE_EDITOR, payload);
                break;
            case ShaderManagerDialog.EDIT_FRAGMENT_SHADER_DONE:
                shader = new File(projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SHADER_DIR_PATH + File.separator + notification.getType() + ".frag");
                try {
                    Files.writeString(shader.toPath(), notification.getBody());
                    rm.reloadShader(notification.getType());
                    //TODO Refresh entities with this shader
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ShaderManagerDialog.EDIT_VERTEX_SHADER_DONE:
                shader = new File(projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SHADER_DIR_PATH + File.separator + notification.getType() + ".vert");
                try {
                    Files.writeString(shader.toPath(), notification.getBody());
                    rm.reloadShader(notification.getType());
                    //TODO Refresh entities with this shader
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
