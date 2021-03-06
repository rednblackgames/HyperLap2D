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

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 5/20/2015.
 */
public abstract class BasicFollower extends Group {

    protected TransformComponent transformComponent;
    protected DimensionsComponent dimensionsComponent;
    protected Entity entity;

    protected float pointOriginX;
    protected float pointOriginY;

    private final Array<SubFollower> subFollowers = new Array<>();

    public BasicFollower(Entity entity) {
        setItem(entity);
        create();
        update();
    }

    private void setItem(Entity entity) {
        transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
        dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        this.entity = entity;
    }

    public void update() {
        Sandbox sandbox = Sandbox.getInstance();
        OrthographicCamera camera = Sandbox.getInstance().getCamera();

        int pixelPerWU = sandbox.sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;

    	Vector2 position = Pools.obtain(Vector2.class);

        position.x = 0;
        position.y = 0;

        TransformMathUtils.localToAscendantCoordinates(sandbox.getCurrentViewingEntity(), entity, position);
        position = Sandbox.getInstance().worldToScreen(position);

        setX( ( int ) ( position.x ) );
        setY( ( int ) ( position.y ) );

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
        }
        else {
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

    public Entity getEntity() {
        return entity;
    }

    public void addSubfollower(SubFollower subFollower) {
        subFollowers.add(subFollower);
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
