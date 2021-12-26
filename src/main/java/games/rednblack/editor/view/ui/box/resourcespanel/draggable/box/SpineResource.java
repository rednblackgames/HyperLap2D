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

package games.rednblack.editor.view.ui.box.resourcespanel.draggable.box;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.widget.actors.SpineActor;
import games.rednblack.h2d.common.ResourcePayloadObject;

/**
 * Created by azakhary on 7/3/2014.
 */
public class SpineResource extends BoxItemResource {

    private final SpineActor payloadActor;
    private final SpineActor animThumb;
    private final ResourcePayloadObject payload;

    private boolean isMouseInside = false;

    public SpineResource(String animationName) {
        // this is not changing the behavior of the former constructor
        // as long as the colors of the super class are not changed
        this(animationName, false);
    }

    /**
     * Creates a new spine resource from the given animation name.
     *
     * @param animationName          The of the animation for the spine resource.
     * @param highlightWhenMouseOver Whether to change the border color when the mouse hovers over the image.
     */
    public SpineResource(String animationName, boolean highlightWhenMouseOver) {
        super(highlightWhenMouseOver);
        SpineVO vo = new SpineVO();
        vo.animationName = animationName;

        animThumb = new SpineActor(animationName, sandbox.getSceneControl().sceneLoader.getRm());

        if (animThumb.getWidth() > thumbnailSize || animThumb.getHeight() > thumbnailSize) {
            // resizing is needed
            float scaleFactor = 1.0f;
            if (animThumb.getWidth() > animThumb.getHeight()) {
                //scale by width
                scaleFactor = 1.0f / (animThumb.getWidth() / thumbnailSize);
            } else {
                scaleFactor = 1.0f / (animThumb.getHeight() / thumbnailSize);
            }
            animThumb.setScale(scaleFactor);

            animThumb.setX((getWidth() - animThumb.getWidth()) / 2);
            animThumb.setY((getHeight() - animThumb.getHeight()) / 2);
        } else {
            // put it in middle
            animThumb.setX((getWidth() - animThumb.getWidth()) / 2);
            animThumb.setY((getHeight() - animThumb.getHeight()) / 2);
        }

        animThumb.setAnimation(animThumb.skeletonData.getAnimations().get(0).getName());
        animThumb.getState().setTimeScale(0);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isMouseInside = true;
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                isMouseInside = false;
                super.enter(event, x, y, pointer, toActor);
            }
        });


        addActor(animThumb);

        payloadActor = new SpineActor(animationName, sandbox.getSceneControl().sceneLoader.getRm());

        payload = new ResourcePayloadObject();
        payload.name = animationName;
        payload.className = getClass().getName();

        super.act(1f);
        super.act(Gdx.graphics.getDeltaTime());

        setRightClickEvent(UIResourcesBoxMediator.SPINE_ANIMATION_RIGHT_CLICK, payload.name);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animThumb.getState().setTimeScale(isMouseInside ? 1f : 0f);
    }

    @Override
    public Actor getDragActor() {
        return payloadActor;
    }

    @Override
    public ResourcePayloadObject getPayloadData() {
        return payload;
    }
}
