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

package games.rednblack.editor.renderer.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by azakhary on 7/2/2015.
 */
public class PolygonComponent implements Component {
    public Vector2[][] vertices;

    public void makeRectangle(float width, float height) {
        Vector2[] points = new Vector2[4];
        points[0] = new Vector2(0, 0);
        points[1] = new Vector2(0, height);
        points[2] = new Vector2(width, height);
        points[3] = new Vector2(width, 0);

        vertices = new Vector2[1][4];
        vertices[0] = points;
    }

    public void makeRectangle(float x, float y, float width, float height) // Overloaded Function to enable more flexibility when setting polygon
    {
        Vector2[] points = new Vector2[4];
        points[0] = new Vector2(x, y);
        points[1] = new Vector2(x, y+height);
        points[2] = new Vector2(x + width, y + height);
        points[3] = new Vector2(x + width, y);

        vertices = new Vector2[1][4];
        vertices[0] = points;
    }
}
