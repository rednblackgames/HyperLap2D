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

package games.rednblack.editor.controller;

import com.puremvc.patterns.command.SimpleCommand;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.menu.HyperLap2DMenuBarMediator;
import games.rednblack.editor.view.HyperLap2DScreenMediator;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.stage.UIStageMediator;
import games.rednblack.editor.view.ui.RulersUIMediator;
import games.rednblack.editor.view.ui.UIDropDownMenuMediator;
import games.rednblack.editor.view.ui.box.*;
import games.rednblack.editor.view.ui.dialog.*;
import games.rednblack.editor.view.ui.panel.CustomVariablesPanelMediator;
import games.rednblack.editor.view.ui.panel.EditSpriteAnimationPanelMediator;
import games.rednblack.editor.view.ui.panel.TagsPanelMediator;

/**
 * Created by sargis on 4/1/15.
 */
public class BootstrapViewCommand extends SimpleCommand {
    @Override
    public void execute(Notification notification) {
        super.execute(notification);
        facade = HyperLap2DFacade.getInstance();
        facade.sendNotification(SplashScreenAdapter.UPDATE_SPLASH, "Loading Views...");

        facade.registerMediator(new HyperLap2DScreenMediator());
        facade.registerMediator(new HyperLap2DMenuBarMediator());
        facade.registerMediator(new UICompositeHierarchyMediator());
        facade.registerMediator(new UIGridBoxMediator());
        facade.registerMediator(new UIResolutionBoxMediator());
        facade.registerMediator(new UIZoomBoxMediator());
        facade.registerMediator(new UIToolBoxMediator());
		facade.registerMediator(new UILivePreviewBoxMediator());

        // Dialogs
        facade.registerMediator(new NewProjectDialogMediator());
        facade.registerMediator(new ImportDialogMediator());
        facade.registerMediator(new ExportSettingsDialogMediator());
        facade.registerMediator(new CreateNewResolutionDialogMediator());
        facade.registerMediator(new CustomVariablesPanelMediator());
        facade.registerMediator(new TagsPanelMediator());
        facade.registerMediator(new EditSpriteAnimationPanelMediator());
        facade.registerMediator(new AboutDialogMediator());

        facade.registerMediator(new RulersUIMediator());
        facade.registerMediator(new FollowersUIMediator());

        facade.registerMediator(new UIAlignBoxMediator());
        facade.registerMediator(new UIItemsTreeBoxMediator());
        facade.registerMediator(new UIMultiPropertyBoxMediator());
        facade.registerMediator(new UILayerBoxMediator());
        facade.registerMediator(new UIResourcesBoxMediator());
        facade.registerMediator(new UIStageMediator());
        facade.registerMediator(new SandboxMediator());
        facade.registerMediator(new UIDropDownMenuMediator());

        facade.registerMediator(new SaveDocumentDialogMediator());
    }
}
