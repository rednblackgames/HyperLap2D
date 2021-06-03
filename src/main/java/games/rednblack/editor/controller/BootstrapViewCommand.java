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

import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.view.HyperLap2DScreenMediator;
import games.rednblack.editor.view.menu.HyperLap2DMenuBarMediator;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.stage.UIStageMediator;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.ImageResourceSelectionUIMediator;
import games.rednblack.editor.view.ui.RulersUIMediator;
import games.rednblack.editor.view.ui.StickyNotesUIMediator;
import games.rednblack.editor.view.ui.UIDropDownMenuMediator;
import games.rednblack.editor.view.ui.UIWindowActionMediator;
import games.rednblack.editor.view.ui.UIWindowTitleMediator;
import games.rednblack.editor.view.ui.box.UIAlignBoxMediator;
import games.rednblack.editor.view.ui.box.UICompositeHierarchyMediator;
import games.rednblack.editor.view.ui.box.UIItemsTreeBoxMediator;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.editor.view.ui.box.UIMultiPropertyBoxMediator;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.editor.view.ui.box.bottom.UIGridBoxMediator;
import games.rednblack.editor.view.ui.box.bottom.UILivePreviewBoxMediator;
import games.rednblack.editor.view.ui.box.bottom.UIResolutionBoxMediator;
import games.rednblack.editor.view.ui.box.bottom.UISceneBoxMediator;
import games.rednblack.editor.view.ui.box.bottom.UIZoomBoxMediator;
import games.rednblack.editor.view.ui.dialog.AboutDialogMediator;
import games.rednblack.editor.view.ui.dialog.AutoTraceDialogMediator;
import games.rednblack.editor.view.ui.dialog.CodeEditorDialogMediator;
import games.rednblack.editor.view.ui.dialog.ConsoleDialogMediator;
import games.rednblack.editor.view.ui.dialog.CreateNewResolutionDialogMediator;
import games.rednblack.editor.view.ui.dialog.CreateNoiseDialogMediator;
import games.rednblack.editor.view.ui.dialog.CreatePlaceholderDialogMediator;
import games.rednblack.editor.view.ui.dialog.LoadingBarDialogMediator;
import games.rednblack.editor.view.ui.dialog.NewProjectDialogMediator;
import games.rednblack.editor.view.ui.dialog.NodeEditorDialogMediator;
import games.rednblack.editor.view.ui.dialog.SaveProjectDialogMediator;
import games.rednblack.editor.view.ui.dialog.SettingsDialogMediator;
import games.rednblack.editor.view.ui.panel.CustomVariablesPanelMediator;
import games.rednblack.editor.view.ui.panel.EditSpriteAnimationPanelMediator;
import games.rednblack.editor.view.ui.panel.ImportPanelMediator;
import games.rednblack.editor.view.ui.panel.ShaderUniformsPanelMediator;
import games.rednblack.editor.view.ui.panel.TagsPanelMediator;

/**
 * Created by sargis on 4/1/15.
 */
public class BootstrapViewCommand extends SimpleCommand {
    @Override
    public void execute(INotification notification) {
        super.execute(notification);
        facade = HyperLap2DFacade.getInstance();
        facade.sendNotification(SplashScreenAdapter.UPDATE_SPLASH, "Loading Views...");

        facade.registerMediator(new HyperLap2DScreenMediator());
        facade.registerMediator(new HyperLap2DMenuBarMediator());
        facade.registerMediator(new UIWindowTitleMediator());
        facade.registerMediator(new UIWindowActionMediator());

        facade.registerMediator(new UICompositeHierarchyMediator());
		facade.registerMediator(new UISceneBoxMediator());
        facade.registerMediator(new UIGridBoxMediator());
        facade.registerMediator(new UIResolutionBoxMediator());
        facade.registerMediator(new UIZoomBoxMediator());
        facade.registerMediator(new UIToolBoxMediator());
		facade.registerMediator(new UILivePreviewBoxMediator());

        facade.registerMediator(new RulersUIMediator());
        facade.registerMediator(new FollowersUIMediator());
        facade.registerMediator(new StickyNotesUIMediator());

        facade.registerMediator(new UIAlignBoxMediator());
        facade.registerMediator(new UIItemsTreeBoxMediator());
        facade.registerMediator(new UIMultiPropertyBoxMediator());
        facade.registerMediator(new UILayerBoxMediator());
        facade.registerMediator(new UIResourcesBoxMediator());
        facade.registerMediator(new UIStageMediator());
        facade.registerMediator(new SandboxMediator());
        facade.registerMediator(new UIDropDownMenuMediator());
        
        // improved selection behavior for the image panel
        facade.registerMediator(new ImageResourceSelectionUIMediator());

        //Panels
        facade.registerMediator(new ImportPanelMediator());
        facade.registerMediator(new CustomVariablesPanelMediator());
        facade.registerMediator(new TagsPanelMediator());
        facade.registerMediator(new EditSpriteAnimationPanelMediator());
        facade.registerMediator(new ShaderUniformsPanelMediator());

        // Dialogs
        facade.registerMediator(new LoadingBarDialogMediator());
        facade.registerMediator(new NewProjectDialogMediator());
        facade.registerMediator(new CreateNewResolutionDialogMediator());
        facade.registerMediator(new AboutDialogMediator());
        facade.registerMediator(new SettingsDialogMediator());
        facade.registerMediator(new AutoTraceDialogMediator());
        facade.registerMediator(new CodeEditorDialogMediator());
        facade.registerMediator(new NodeEditorDialogMediator());
        facade.registerMediator(new CreatePlaceholderDialogMediator());
        facade.registerMediator(new CreateNoiseDialogMediator());
        facade.registerMediator(new ConsoleDialogMediator());

        facade.registerMediator(new SaveProjectDialogMediator());
    }
}
