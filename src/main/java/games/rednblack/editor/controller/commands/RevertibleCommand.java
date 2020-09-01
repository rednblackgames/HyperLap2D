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
import games.rednblack.editor.proxy.CommandManager;
import org.puremvc.java.interfaces.INotification;

public abstract class RevertibleCommand extends SandboxCommand {

    protected CommandManager commandManager;
    protected INotification notification;

    protected boolean isCancelled = false;
    protected boolean stateDone = false;

    @Override
    public void execute(INotification notification) {
        commandManager = facade.retrieveProxy(CommandManager.NAME);
        this.notification = notification;
        callDoAction();
        stateDone = true;
        if(!isCancelled) commandManager.addCommand(this);
    }

    public abstract void doAction();
    public abstract void undoAction();

    public void callDoAction() {
        doAction();
    }

    public void callUndoAction() {
       undoAction();
    }

    public INotification getNotification() {
        return notification;
    }

    public void setStateDone(boolean state) {
        stateDone = state;
    }

    public boolean isStateDone() {
        return stateDone;
    }

    public void cancel() {
        isCancelled = true;
    }
}
