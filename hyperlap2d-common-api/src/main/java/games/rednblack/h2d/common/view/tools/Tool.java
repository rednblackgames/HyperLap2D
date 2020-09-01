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

package games.rednblack.h2d.common.view.tools;

import com.badlogic.ashley.core.Entity;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 4/30/2015.
 */
public interface Tool {
    void initTool();
    boolean stageMouseDown(float x, float y);
    void stageMouseUp(float x, float y);
    void stageMouseDragged(float x, float y);
    void stageMouseDoubleClick(float x, float y);
    void stageMouseScrolled(int amount);
    boolean itemMouseDown(Entity entity, float x, float y);
    void itemMouseUp(Entity entity, float x, float y);
    void itemMouseDragged(Entity entity, float x, float y);
    void itemMouseDoubleClick(Entity entity, float x, float y);
    String getName();
    String getTitle();
    String getShortcut();
    void handleNotification(INotification notification);
    void keyDown(Entity entity, int keycode);
    void keyUp(Entity entity, int keycode);
}
