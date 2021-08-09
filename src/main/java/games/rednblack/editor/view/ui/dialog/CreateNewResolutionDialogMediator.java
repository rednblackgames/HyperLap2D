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

package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.box.bottom.UIResolutionBox;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

/**
 * Created by sargis on 4/9/15.
 */
public class CreateNewResolutionDialogMediator extends Mediator<CreateNewResolutionDialog> {
    private static final String TAG = CreateNewResolutionDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public CreateNewResolutionDialogMediator() {
        super(NAME, new CreateNewResolutionDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                UIResolutionBox.CREATE_NEW_RESOLUTION_BTN_CLICKED,
                CreateNewResolutionDialog.CREATE_BTN_CLICKED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case UIResolutionBox.CREATE_NEW_RESOLUTION_BTN_CLICKED:
                viewComponent.show(uiStage);
                break;
            case CreateNewResolutionDialog.CREATE_BTN_CLICKED:
                ResolutionEntryVO resolutionEntryVO = notification.getBody();
                ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
                resolutionManager.createNewResolution(resolutionEntryVO);
                viewComponent.hide();
                break;
        }
    }
}
