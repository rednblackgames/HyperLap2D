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

package games.rednblack.editor.view;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.ui.widget.actors.basic.SandboxBackUI;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

/**
 * Created by sargis on 3/30/15.
 */
public class HyperLap2DScreenMediator extends Mediator<HyperLap2DScreen> {
    private static final String TAG = HyperLap2DScreenMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public HyperLap2DScreenMediator() {
        super(NAME, null);
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.CREATE,
                MsgAPI.PAUSE,
                MsgAPI.RESUME,
                MsgAPI.RENDER);
        interests.add(MsgAPI.RESIZE,
                MsgAPI.DISPOSE,
                MsgAPI.SCENE_LOADED,
                MsgAPI.SAVE_EDITOR_CONFIG);
        interests.add(MsgAPI.SHOW_BLACK_OVERLAY,
                MsgAPI.HIDE_BLACK_OVERLAY);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.CREATE:
            	setViewComponent(new HyperLap2DScreen());
            	//TODO this must be changed to Command
            	SandboxMediator sandboxMediator = facade.retrieveMediator(SandboxMediator.NAME);

                com.artemis.World engine = sandboxMediator.getViewComponent().getEngine();

            	getViewComponent().setEngine(engine);
                viewComponent.show();
                break;
            case MsgAPI.SCENE_LOADED:
                SandboxBackUI sandboxBackUI = new SandboxBackUI(Sandbox.getInstance().getUIStage().getBatch());
                getViewComponent().setBackUI(sandboxBackUI);
                getViewComponent().disableDrawingBgLogo();
                break;
            case MsgAPI.PAUSE:
                viewComponent.pause();
                break;
            case MsgAPI.RESUME:
                viewComponent.resume();
                break;
            case MsgAPI.RENDER:
                float[] delta = notification.getBody();
                viewComponent.render(delta[0]);
                break;
            case MsgAPI.RESIZE:
                int[] data = notification.getBody();
                viewComponent.resize(data[0], data[1]);
                break;
            case MsgAPI.DISPOSE:
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                projectManager.stopFileWatcher();
                break;
            case MsgAPI.SAVE_EDITOR_CONFIG:
                SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
                settingsManager.saveEditorConfig();
                getViewComponent().updateActorSize();
                break;
            case MsgAPI.SHOW_BLACK_OVERLAY:
                viewComponent.showBlackOverlay();
                break;
            case MsgAPI.HIDE_BLACK_OVERLAY:
                viewComponent.hideBlackOverlay();
                break;
        }
    }
}
