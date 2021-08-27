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

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.BoundingBoxComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.extension.talos.TalosComponent;

/**
 * Created by azakhary on 6/5/2015.
 */
public class EntityBounds extends Rectangle {

    private float scaleX;
    private float scaleY;

    /**
     * points winding order is counterclockwise (p1 -> p2 -> p3 -> p4)
     *       /---p3
     * p4---/    |
     * |         |
     * |         |
     * |      __p2
     * p1 ___/
     */
    private final Vector2 p1 = new Vector2();
    private final Vector2 p2 = new Vector2();
    private final Vector2 p3 = new Vector2();
    private final Vector2 p4 = new Vector2();
    private final Array<Vector2> boundPointList = new Array<>();
    private final float[] boundPoints = new float[8];

    public EntityBounds(int entity) {
        setEntity(entity);
    }

    public EntityBounds() {

    }

    public void setEntity(int entity) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        BoundingBoxComponent boundingBoxComponent = SandboxComponentRetriever.get(entity, BoundingBoxComponent.class);
        x = transformComponent.x;
        y = transformComponent.y;
        scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);
        width = dimensionsComponent.width;
        height = dimensionsComponent.height;

        //Use custom bounding boxes only for lights and particles which doesn't have dimensions
        //Or if the entity has no bounding boxes
        if (boundingBoxComponent == null
                || SandboxComponentRetriever.get(entity, LightObjectComponent.class) != null
                || SandboxComponentRetriever.get(entity, ParticleComponent.class) != null
                || SandboxComponentRetriever.get(entity, TalosComponent.class) != null) {

            if (SandboxComponentRetriever.get(entity, LightObjectComponent.class) != null) {
                x += dimensionsComponent.boundBox.x;
                y += dimensionsComponent.boundBox.y;
                scaleX = 1;
                scaleY = 1;
                width = dimensionsComponent.boundBox.width;
                height = dimensionsComponent.boundBox.height;
            } else if (SandboxComponentRetriever.get(entity, ParticleComponent.class) != null
                    || SandboxComponentRetriever.get(entity, TalosComponent.class) != null) {
                width = dimensionsComponent.boundBox.width;
                height = dimensionsComponent.boundBox.height;
                dimensionsComponent.width = width;
                dimensionsComponent.height = height;
                x += dimensionsComponent.boundBox.x;
                y += dimensionsComponent.boundBox.y;
            }

            Matrix3 transMat = TransformUtils.identity();

            if ((scaleX != 1 || scaleY != 1) && transformComponent.rotation != 0) {
                transMat = TransformUtils.scaleRotMat(transformComponent);
            } else if (scaleX != 1 || scaleY != 1) {
                transMat = TransformUtils.scalingMat(transformComponent);
            } else if (transformComponent.rotation != 0) {
                transMat = TransformUtils.rotationMat(transformComponent);
            }

            p1.set(x, y).mul(transMat);
            p2.set(x + width, y).mul(transMat);
            p3.set(x + width, y + height).mul(transMat);
            p4.set(x, y + height).mul(transMat);
        } else {
            //Otherwise, we already have a neat and solid bounding boxes system, so use it!
            p1.set(boundingBoxComponent.points[0]);
            p2.set(boundingBoxComponent.points[1]);
            p3.set(boundingBoxComponent.points[2]);
            p4.set(boundingBoxComponent.points[3]);
        }

        boundPoints[0] = p1.x;
        boundPoints[1] = p1.y;
        boundPoints[2] = p2.x;
        boundPoints[3] = p2.y;
        boundPoints[4] = p3.x;
        boundPoints[5] = p3.y;
        boundPoints[6] = p4.x;
        boundPoints[7] = p4.y;
    }

    public float getVisualWidth() {
        return Math.abs(getWidth() * scaleX);
    }

    public float getVisualHeight() {
        return Math.abs(getHeight() * scaleY);
    }

    public float getVisualX() {
        return Math.min(getP1x(), getP3x());
    }

    public float getVisualY() {
        return Math.min(getP1y(), getP3y());
    }

    public float getVisualRightX() {
        return Math.max(getP1x(), getP3x());
    }

    public float getVisualTopY() {
        return Math.max(getP1y(), getP3y());
    }

    public float getP1x() {
        return p1.x;
    }

    public float getP1y() {
        return p1.y;
    }

    public float getP2x() {
        return p2.x;
    }

    public float getP2y() {
        return p2.y;
    }

    public float getP3x() {
        return p3.x;
    }

    public float getP3y() {
        return p3.y;
    }

    public float getP4x() {
        return p4.x;
    }

    public float getP4y() {
        return p4.y;
    }

    public float[] getBoundPoints() {
        return boundPoints;
    }

    public float[] getBoundPoints(int entity) {
        setEntity(entity);
        return boundPoints;
    }

    public Array<Vector2> getBoundPointsList() {
        boundPointList.clear();
        boundPointList.add(p1, p2, p3, p4);
        return boundPointList;
    }

    public Array<Vector2> getBoundPointsList(int entity) {
        setEntity(entity);
        return getBoundPointsList();

    }
}
