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

package games.rednblack.editor.view.ui.box.bottom;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

/**
 * Created by azakhary on 4/15/2015.
 */
public class UIGridBoxMediator extends Mediator<UIGridBox> {
    private static final String TAG = UIGridBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIGridBoxMediator() {
        super(NAME, new UIGridBox());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ProjectManager.PROJECT_OPENED,
                MsgAPI.GRID_SIZE_CHANGED,
                MsgAPI.LOCK_LINES_CHANGED,
                UIGridBox.GRID_SIZE_TEXT_FIELD_UPDATED,
                UIGridBox.LOCK_LINES_CHECKBOX_FIELD_UPDATED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();

        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
                viewComponent.update();
                viewComponent.setGridSize(sandbox.getGridSize());
                viewComponent.setLockLines(sandbox.getLockLines());
                break;
            case MsgAPI.GRID_SIZE_CHANGED:
                viewComponent.setGridSize(sandbox.getGridSize());
                break;
            case MsgAPI.LOCK_LINES_CHANGED:
                Boolean lockLinesChanged = notification.getBody();
                viewComponent.setLockLines(lockLinesChanged);
                break;
            case UIGridBox.GRID_SIZE_TEXT_FIELD_UPDATED:
                String body = notification.getBody();
                sandbox.setGridSize(Float.parseFloat(body));
                break;
            case UIGridBox.LOCK_LINES_CHECKBOX_FIELD_UPDATED:
                Boolean lockLines = notification.getBody();
                sandbox.setLockLines(lockLines);
                break;
            default:
                break;
        }
    }
}
