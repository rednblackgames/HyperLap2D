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

package games.rednblack.editor.event;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.HyperLap2DFacade;

/**
 * Created by azakhary on 4/15/2015.
 */
public class KeyboardListener implements EventListener {

    private final String eventName;
    private final boolean handleFocus;

    private String lastValue;

    public KeyboardListener(String eventName) {
        this(eventName, true);
    }

    public KeyboardListener(String eventName, boolean focus) {
        this.eventName = eventName;
        this.handleFocus = focus;
    }

    @Override
    public boolean handle(Event event) {
        if (handleFocus && event instanceof FocusListener.FocusEvent) {
            handleFocusListener((FocusListener.FocusEvent) event);
            return true;
        }

        if (event instanceof InputEvent) {
            handleInputListener((InputEvent) event);
            return true;
        }
        return false;
    }

    private void handleInputListener(InputEvent event) {
        switch (event.getType()) {
            case keyUp:
                if (event.getKeyCode() == Input.Keys.ENTER || event.getKeyCode() == Input.Keys.NUMPAD_ENTER) {
                    keyboardHandler((VisTextField) event.getTarget());
                }
                break;
        }
    }

    private void handleFocusListener(FocusListener.FocusEvent event) {
        VisTextField field = (VisTextField) event.getTarget();
        if(event.isFocused()) {
            //it was a focus in event, which is no change
            return;
        }

        switch (event.getType()) {
            case keyboard:
                keyboardHandler(field);
                break;
            case scroll:
                break;
        }
    }

    private void keyboardHandler(VisTextField target) {
        if(!target.isInputValid()) {
            return;
        }

        // check for change
        if(lastValue != null && lastValue.equals(target.getText())) {
            // no change = no event;
            return;
        }

        lastValue = target.getText();

        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        facade.sendNotification(eventName, target.getText());
    }
}