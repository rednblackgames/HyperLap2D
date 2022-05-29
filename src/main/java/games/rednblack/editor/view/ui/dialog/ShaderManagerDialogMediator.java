package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.files.FileHandle;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.code.syntax.GLSLSyntax;
import games.rednblack.editor.controller.commands.resource.DeleteShaderCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.utils.DefaultShaders;
import games.rednblack.editor.renderer.utils.ShaderCompiler;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.io.FileUtils;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ShaderManagerDialogMediator extends Mediator<ShaderManagerDialog> {

    private static final String TAG = ShaderManagerDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private final GLSLSyntax glslSyntax = new GLSLSyntax();

    private ResourceManager resourceManager;
    private ProjectManager projectManager;

    public ShaderManagerDialogMediator() {
        super(NAME, new ShaderManagerDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();

        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
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
                ShaderManagerDialog.CREATE_NEW_SHADER,
                DeleteShaderCommand.DONE
        };
    }

    @Override
    public void handleNotification(INotification notification) {
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
                viewComponent.updateShaderList(resourceManager.getShaders().keySet());
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
                    resourceManager.reloadShader(notification.getType());
                    updateEntitiesShaders(sandbox.getRootEntity(), notification.getType());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ShaderManagerDialog.EDIT_VERTEX_SHADER_DONE:
                shader = new File(projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SHADER_DIR_PATH + File.separator + notification.getType() + ".vert");
                try {
                    Files.writeString(shader.toPath(), notification.getBody());
                    resourceManager.reloadShader(notification.getType());
                    updateEntitiesShaders(sandbox.getRootEntity(), notification.getType());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ShaderManagerDialog.CREATE_NEW_SHADER:
                payload = notification.getBody();
                createNewShader((String) payload[0], (int) payload[1]);
                break;
        }
    }

    private void updateEntitiesShaders(int root, String shaderName) {
        Consumer<Integer> action = (item) -> {
            ShaderComponent shaderComponent = SandboxComponentRetriever.get(item, ShaderComponent.class);
            if (shaderComponent != null && shaderComponent.shaderName.equals(shaderName)) {
                shaderComponent.setShader(shaderName, resourceManager.getShaderProgram(shaderName));
            }
        };

        EntityUtils.applyActionRecursivelyOnEntities(root, action);
    }

    private void createNewShader(String name, int type) {
        String vertex = null;
        String fragment = null;

        switch (type) {
            case 0:
                vertex = DefaultShaders.DEFAULT_VERTEX_SHADER;
                fragment = DefaultShaders.DEFAULT_FRAGMENT_SHADER;
                break;
            case 1:
                vertex = DefaultShaders.DEFAULT_ARRAY_VERTEX_SHADER;
                fragment = DefaultShaders.DEFAULT_ARRAY_FRAGMENT_SHADER;
                break;
            case 2:
                vertex = DefaultShaders.DISTANCE_FIELD_VERTEX_SHADER;
                fragment = DefaultShaders.DISTANCE_FIELD_FRAGMENT_SHADER;
                break;
            case 3:
                vertex = DefaultShaders.DEFAULT_SCREE_READING_VERTEX_SHADER;
                fragment = DefaultShaders.DEFAULT_SCREE_READING_FRAGMENT_SHADER;
                break;
        }

        if (vertex == null || fragment == null) return;

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        ResourceManager rm = facade.retrieveProxy(ResourceManager.NAME);

        FileHandle vert = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + name + ".vert");
        FileHandle frag = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + name + ".frag");

        try {
            FileUtils.writeStringToFile(vert.file(), vertex, "utf-8");
            FileUtils.writeStringToFile(frag.file(), fragment, "utf-8");
            rm.addShaderProgram(name, ShaderCompiler.compileShader(vertex, fragment));
            viewComponent.updateShaderList(rm.getShaders().keySet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
