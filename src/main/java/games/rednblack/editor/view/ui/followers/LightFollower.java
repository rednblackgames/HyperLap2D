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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.CircleActor;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Created by azakhary on 5/20/2015.
 */
public class LightFollower extends BasicFollower {
    protected LightObjectComponent lightObjectComponent;
    private CircleActor circleActor;
    private Image icon;
    OrthographicCamera camera;
    int pixelPerWU;

    public LightFollower(int entity) {
        super(entity);
        lightObjectComponent = SandboxComponentRetriever.get(entity, LightObjectComponent.class);
    }

    @Override
    public void create() {
        icon = new Image(VisUI.getSkin().getDrawable("tool-sphericlight"));
        icon.setTouchable(Touchable.disabled);
        addActor(icon);
        pixelPerWU = Sandbox.getInstance().sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        camera = Sandbox.getInstance().getCamera();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            ShapeDrawer shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
            float radius = lightObjectComponent.distance * pixelPerWU / camera.zoom;
            circleActor = new CircleActor(shapeDrawer, radius);
            circleActor.setColor( 1, 1, 1, 0.5f);
            addActor(circleActor);
        }
    }

    @Override
    public void act(float delta) {
        setVisible(!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT));
        super.act(delta);
        float radius = lightObjectComponent.distance * pixelPerWU / camera.zoom;
        circleActor.setRadius(radius);
        circleActor.setPosition(getWidth() / 2, getHeight() / 2);
    }

    @Override
    public void update() {
        super.update();
        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);

        setWidth ( pixelPerWU * dimensionsComponent.width * scaleX / camera.zoom );
        setHeight( pixelPerWU * dimensionsComponent.height * scaleY / camera.zoom );

        setX(getX() - getWidth() / 2f);
        setY(getY() - getHeight() / 2f);

        icon.setX((getWidth() - icon.getWidth()) / 2);
        icon.setY((getHeight() - icon.getHeight()) / 2);
        setRotation(0);
    }

    @Override
    public void hide() {
        // you cannot hide light folower
        icon.setColor(Color.WHITE);
    }

    @Override
    public void show() {
        super.show();
        icon.setColor(Color.ORANGE);
    }
}
