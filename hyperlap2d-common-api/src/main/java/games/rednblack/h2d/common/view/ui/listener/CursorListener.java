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

package games.rednblack.h2d.common.view.ui.listener;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.puremvc.patterns.facade.Facade;
import games.rednblack.h2d.common.proxy.CursorManager;
import games.rednblack.h2d.common.vo.CursorData;

public class CursorListener extends InputListener {
    private final CursorData cursor;
    private final CursorManager cursorManager;

    public CursorListener(CursorData cursor, Facade facade) {
        this.cursor = cursor;
        cursorManager = facade.retrieveProxy(CursorManager.NAME);
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer == -1) cursorManager.setOverrideCursor(cursor);
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer == -1) cursorManager.removeOverrideCursor();
    }
}