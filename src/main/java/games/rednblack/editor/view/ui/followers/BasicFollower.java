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

package games.rednblack.editor.view.ui.followers;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 5/20/2015.
 */
public abstract class BasicFollower extends Group {

    protected TransformComponent transformComponent;
    protected DimensionsComponent dimensionsComponent;
    protected int entity;

    protected float pointOriginX;
    protected float pointOriginY;

    protected float polygonOffsetX;
    protected float polygonOffsetY;

    private final Array<SubFollower> subFollowers = new Array<>();

    public BasicFollower(int entity) {
        setItem(entity);
        create();
        update();
    }

    private void setItem(int entity) {
        transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        this.entity = entity;
    }

    public void update() {
        if (SandboxComponentRetriever.get(entity, MainItemComponent.class) == null)
            return;

        Sandbox sandbox = Sandbox.getInstance();
        OrthographicCamera camera = Sandbox.getInstance().getCamera();

        int pixelPerWU = sandbox.sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;

    	Vector2 position = Pools.obtain(Vector2.class);

        position.x = 0;
        position.y = 0;

        ComponentMapper<TransformComponent> transformMapper = (ComponentMapper<TransformComponent>) ComponentMapper.getFor(TransformComponent.class, sandbox.getEngine());
        ComponentMapper<ParentNodeComponent> parentNodeMapper = (ComponentMapper<ParentNodeComponent>) ComponentMapper.getFor(ParentNodeComponent.class, sandbox.getEngine());
        TransformMathUtils.localToAscendantCoordinates(sandbox.getCurrentViewingEntity(), entity, position, transformMapper, parentNodeMapper);
        position = Sandbox.getInstance().worldToScreen(position);

        setX( ( int ) ( position.x ) );
        setY( ( int ) ( position.y ) );

        polygonOffsetX = 0;
        polygonOffsetY = 0;

        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);

        if(dimensionsComponent.boundBox != null ) // if we have a composite item ...
        {   // .. we set the width to be the width of the AABB + the starting point of the AABB
            // .. same for the height
            /**
             *       ...........
             *       .         .
             *       .         .
             * .<--->.<------->.
             *       ...........
             */

            setWidth (pixelPerWU * (dimensionsComponent.boundBox.x + dimensionsComponent.boundBox.width) * scaleX / camera.zoom);
            setHeight(pixelPerWU * (dimensionsComponent.boundBox.y + dimensionsComponent.boundBox.height) * scaleY / camera.zoom);
        } else if (dimensionsComponent.polygon != null) {
            Rectangle b = dimensionsComponent.polygon.getBoundingRectangle();
            setWidth (pixelPerWU * (b.width) * scaleX / camera.zoom);
            setHeight(pixelPerWU * (b.height) * scaleY / camera.zoom);

            polygonOffsetX = pixelPerWU * (b.x) * scaleX / camera.zoom;
            polygonOffsetY = pixelPerWU * (b.y) * scaleY / camera.zoom;

            setX(getX() + polygonOffsetX);
            setY(getY() + polygonOffsetY);
        } else {
            setWidth ( pixelPerWU * dimensionsComponent.width * scaleX / camera.zoom );
            setHeight( pixelPerWU * dimensionsComponent.height * scaleY / camera.zoom );
        }

        Pools.free(position);

        pointOriginX = pixelPerWU * transformComponent.originX * scaleX / camera.zoom;
        pointOriginY = pixelPerWU * transformComponent.originY * scaleY / camera.zoom;

        setRotation(transformComponent.rotation);

        for (SubFollower follower : subFollowers) {
            follower.update();
        }
    }

    public void show() {
        setVisible(true);
        update();
    }

    public void hide() {
        setVisible(false);
    }

    public abstract void create();


    public void setFollowerListener(FollowerTransformationListener listener) {

    }

    public void clearFollowerListener() {

    }

    @Override
    public Actor hit (float x, float y, boolean touchable) {
        Actor hitActor = super.hit(x, y, touchable);
        if(hitActor == null) return null;
        if(hitActor.equals(this)) return null;

        return hitActor;
    }

    public void handleNotification(INotification notification) {
        for(SubFollower follower: subFollowers) {
            follower.handleNotification(notification);
        }
    }

    public int getEntity() {
        return entity;
    }

    public void addSubfollower(SubFollower subFollower) {
        subFollowers.add(subFollower);
        subFollower.setParentFollower(this);
        addActor(subFollower);
    }

    public Array<SubFollower> getSubFollowers() {
        return subFollowers;
    }

    public SubFollower getSubFollower(Class<? extends SubFollower> clazz) {
        for(SubFollower subFollower: new Array.ArrayIterator<>(subFollowers)) {
            if(subFollower.getClass() == clazz) {
                return subFollower;
            }
        }

        return null;
    }

    public void removeSubFollower(Class<? extends SubFollower> clazz) {
        SubFollower subFollower = getSubFollower(clazz);
        if(subFollower != null) {
            removeSubFollower(subFollower);
        }
    }

    public void removeSubFollower(SubFollower subFollower) {
        subFollowers.removeValue(subFollower, true);
        subFollower.remove();
    }

    public void clearSubFollowers() {
        for(SubFollower subFollower: subFollowers) {
            subFollower.remove();
        }
        subFollowers.clear();
    }
}
