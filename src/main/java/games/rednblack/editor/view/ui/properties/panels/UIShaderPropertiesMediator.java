/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.code.syntax.GLSLSyntax;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateShaderDataCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.dialog.CodeEditorDialogMediator;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by azakhary on 8/12/2015.
 */
public class UIShaderPropertiesMediator extends UIItemPropertiesMediator<UIShaderProperties> {
    private static final String TAG = UIShaderPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final ProjectManager projectManager;
    private final ResourceManager resourceManager;

    private final GLSLSyntax glslSyntax = new GLSLSyntax();

    public UIShaderPropertiesMediator() {
        super(NAME, new UIShaderProperties());

        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        viewComponent.initView(resourceManager.getShaders());

        projectManager = facade.retrieveProxy(ProjectManager.NAME);
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UIShaderProperties.CLOSE_CLICKED,
                UIShaderProperties.EDIT_BUTTON_CLICKED,
                UIShaderProperties.EDIT_SHADER_DONE
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UIShaderProperties.CLOSE_CLICKED:
                facade.sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, ShaderComponent.class));
                break;
            case UIShaderProperties.EDIT_BUTTON_CLICKED:
                if (!viewComponent.getShader().equals("Default")) {
                    File shader = new File(projectManager.getCurrentProjectPath() + File.separator
                            + ProjectManager.SHADER_DIR_PATH + File.separator + viewComponent.getShader() + ".frag");
                    Object[] payload = CodeEditorDialogMediator.openCodeEditorPayload(glslSyntax, "", UIShaderProperties.EDIT_SHADER_DONE, shader, "");
                    facade.sendNotification(MsgAPI.OPEN_CODE_EDITOR, payload);
                }
                break;
            case UIShaderProperties.EDIT_SHADER_DONE:
                File shader = new File(projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SHADER_DIR_PATH + File.separator + viewComponent.getShader() + ".frag");
                try {
                    Files.writeString(shader.toPath(), notification.getBody());
                    resourceManager.reloadShader(viewComponent.getShader());
                    Object payload = UpdateShaderDataCommand.payload(observableReference, viewComponent.getShader(), viewComponent.getRenderingLayer());
                    facade.sendNotification(MsgAPI.ACTION_UPDATE_SHADER_DATA, payload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(int item) {
        ShaderComponent shaderComponent = SandboxComponentRetriever.get(item, ShaderComponent.class);
        String currShaderName = shaderComponent.shaderName;
        viewComponent.setSelected(currShaderName);
        viewComponent.setRenderingLayer(shaderComponent.renderingLayer);
    }

    @Override
    protected void translateViewToItemData() {
        ShaderComponent shaderComponent = SandboxComponentRetriever.get(observableReference, ShaderComponent.class);

        if (!shaderComponent.shaderName.equals(viewComponent.getShader())
                || shaderComponent.renderingLayer != viewComponent.getRenderingLayer()) {
            Object payload = UpdateShaderDataCommand.payload(observableReference,
                    viewComponent.getShader(),
                    viewComponent.getRenderingLayer());
            facade.sendNotification(MsgAPI.ACTION_UPDATE_SHADER_DATA, payload);
        }
    }
}
