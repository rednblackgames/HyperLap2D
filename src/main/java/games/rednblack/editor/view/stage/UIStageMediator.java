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

package games.rednblack.editor.view.stage;

import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

/**
 * Created by sargis on 4/20/15.
 */
public class UIStageMediator extends Mediator<UIStage> {
    private static final String TAG = UIStageMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIStageMediator() {
        super(NAME, new UIStage());
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SHOW_ADD_LIBRARY_DIALOG,
                MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case MsgAPI.SHOW_ADD_LIBRARY_DIALOG:
                Sandbox sandbox = Sandbox.getInstance();

                int item = notification.getBody();

                MainItemComponent mainItemComponent = SandboxComponentRetriever.get(item, MainItemComponent.class);

                H2DDialogs.showInputDialog(sandbox.getUIStage(), "New Library Item", "Unique Name", false, new InputDialogListener() {
                    @Override
                    public void finished(String input) {
                        Object[] payload = new Object[2];
                        payload[0] = item;
                        payload[1] = input;
                        facade.sendNotification(MsgAPI.ACTION_ADD_TO_LIBRARY, payload);
                    }

                    @Override
                    public void canceled() {

                    }
                }).setText(mainItemComponent.libraryLink, true).pack();
                break;
            case MsgAPI.SAVE_EDITOR_CONFIG:
                Gdx.app.postRunnable(() -> getViewComponent().updateViewportDensity());
                break;
        }
    }
}
