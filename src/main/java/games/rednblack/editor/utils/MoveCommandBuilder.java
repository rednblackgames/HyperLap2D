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

package games.rednblack.editor.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by azakhary on 6/5/2015.
 */
public class MoveCommandBuilder {

    Array<Object[]> payload;

    public MoveCommandBuilder() {
        payload =  new Array<>();
    }

    public void setX(int entity, float x) {
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class, Sandbox.getInstance().getEngine());
        setXY(entity, x, transformComponent.y);
    }

    public void setY(int entity, float y) {
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class, Sandbox.getInstance().getEngine());
        setXY(entity, transformComponent.x, y);
    }

    public void setXY(int entity, float x, float y) {
        Object[] data = new Object[2];
        data[0] = entity;
        data[1] = new Vector2(x, y);
        payload.add(data);
    }

    public void clear() {
        payload = new Array<>();
    }

    public void execute() {
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_ITEMS_MOVE_TO, payload);
    }

}
