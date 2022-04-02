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

package games.rednblack.editor.proxy;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.HistoricRevertibleCommand;
import games.rednblack.editor.controller.commands.RevertibleCommand;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.patterns.proxy.Proxy;

import java.util.ArrayList;

public class CommandManager extends Proxy {
    private static final String TAG = CommandManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private int cursor = -1;
    private int modifiedCursor = 0;

    final private ArrayList<RevertibleCommand> commands = new ArrayList<>();

    public CommandManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    public void addCommand(RevertibleCommand revertibleCommand) {
        //remove all commands after the cursor
        for(int i = commands.size()-1; i > cursor; i--) {
            commands.remove(i);
        }
        commands.add(revertibleCommand);
        cursor = commands.indexOf(revertibleCommand);
        if (revertibleCommand instanceof HistoricRevertibleCommand) {
            modifiedCursor++;
            autoSave();
        }

        updateWindowTitle();
    }

    public void undoCommand() {
        if(cursor < 0){
            updateWindowTitle();
            return;
        }
        RevertibleCommand command = commands.get(cursor);
        if(command.isStateDone()) {
            command.callUndoAction();
            command.setStateDone(false);
        }
        cursor--;

        if (command instanceof HistoricRevertibleCommand) {
            modifiedCursor--;
            autoSave();
        }
        updateWindowTitle();
    }

    public void saveEvent() {
        modifiedCursor = 0;
        updateWindowTitle();
    }

    public void updateWindowTitle() {
        WindowTitleManager windowTitleManager = facade.retrieveProxy(WindowTitleManager.NAME);
        windowTitleManager.appendSaveHintTitle(isModified());
    }

    public boolean isModified() {
        return Math.abs(modifiedCursor) > 0;
    }

    public void redoCommand() {
        if(cursor + 1 >= commands.size()) return;
        RevertibleCommand command = commands.get(cursor+1);
        if(!command.isStateDone()) {
            cursor++;
            command.callDoAction();
            command.setStateDone(true);

            if (command instanceof HistoricRevertibleCommand) {
                modifiedCursor++;
                autoSave();
            }
            updateWindowTitle();
        }
    }

    public void clearHistory() {
        cursor = -1;
        commands.clear();

        modifiedCursor = 1;
        updateWindowTitle();
    }

    public void initHistory() {
        clearHistory();
        modifiedCursor = 0;
        updateWindowTitle();
    }

    private void autoSave() {
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        if (settingsManager.editorConfigVO.autoSave)
            facade.sendNotification(MsgAPI.AUTO_SAVE_PROJECT);
    }
}
