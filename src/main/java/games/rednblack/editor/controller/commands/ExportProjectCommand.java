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

package games.rednblack.editor.controller.commands;

import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.interfaces.INotification;

public class ExportProjectCommand extends SandboxCommand {

    @Override
    public void execute(INotification notification) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        if (projectManager.currentProjectVO.texturePackerVO.fast) {
            H2DDialogs.showConfirmDialog(sandbox.getUIStage(),
                    "Warning, not optimized Atlas!", "While a fast packing can speed up the development time,\nfor production you should disable it:\n\nFile -> Settings -> Project Export -> Fast Packing",
                    new String[]{"Cancel", "Yes, export as is"}, new Integer[]{0, 1}, r -> {
                        if (r == 1) {
                            projectManager.exportProject();
                            facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "Project successfully exported");
                        }
                    }).padBottom(20).pack();
        } else {
            projectManager.exportProject();
            facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "Project successfully exported");
        }
    }
}
