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

package games.rednblack.editor.view.ui.box.resourcespanel.draggable;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.BoxItemResource;
import games.rednblack.h2d.common.ResourcePayloadObject;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.function.BiFunction;

/**
 * Created by azakhary on 7/3/2014.
 */
public class DraggableResource extends DragAndDrop implements Comparable<DraggableResource> {

    protected final Sandbox sandbox;
    private final DraggableResourceView viewComponent;
    private BiFunction<String, Vector2, Boolean> factoryFunction;

    public DraggableResource(DraggableResourceView viewComponent) {
        this.viewComponent = viewComponent;
        sandbox = Sandbox.getInstance();
    }

    public void initDragDrop() {
        setKeepWithinStage(false);

        addSource(new Source((Actor) viewComponent) {
            @Override
			public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                Payload payload = new Payload();
                Actor dragActor = viewComponent.getDragActor();
                OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();
                dragActor.setScale(1f/runtimeCamera.zoom);

                ResourcePayloadObject payloadData = viewComponent.getPayloadData();
                payloadData.xOffset = dragActor.getWidth() / 2f;
                payloadData.yOffset = dragActor.getHeight() / 2f;
                payload.setDragActor(dragActor);
                payload.setObject(payloadData);
                payload.setInvalidDragActor(null);
                float dragX = dragActor.getWidth() - (dragActor.getWidth() / (runtimeCamera.zoom * 2f));
                float dragY = dragActor.getHeight() / (runtimeCamera.zoom * 2f);
                setDragActorPosition(dragX, -dragY);

                if (viewComponent instanceof BoxItemResource)
                    HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.RESOURCE_BOX_DRAG_START, viewComponent, null);
                return payload;
            }
        });

        addTarget(new Target(sandbox.getUIStage().dummyTarget) {
        	boolean dragging = false;
        	
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
            	if (!dragging) {
            		// we only want to send the notification once and not every tick
            		dragging = true;
            		HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.SANDBOX_DRAG_IMAGE_ENTER, source, null);
            	}
                return true;
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
                Vector2 vector = sandbox.screenToWorld(x + UIStage.SANDBOX_BOTTOM_MARGIN, y + UIStage.SANDBOX_LEFT_MARGIN);
                DraggableResource.this.drop(payload, vector);
            }
            
            @Override
			public void reset(Source source, Payload payload) {
            	// this method is called when we exit the sandbox, and this is also called when we drop the image into the sandbox
            	// so we only need to look here :)
        		dragging = false;
        		HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.SANDBOX_DRAG_IMAGE_EXIT, source, null);
            }
            
        });


        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        UIResourcesBoxMediator resourcesBoxMediator = facade.retrieveMediator(UIResourcesBoxMediator.NAME);
        for (Target t : resourcesBoxMediator.customTargets) {
            addTarget(t);
        }

        if (viewComponent instanceof BoxItemResource) {
            StandardWidgetsFactory.addTooltip((Actor) viewComponent, viewComponent.getPayloadData().name);
        }
    }

    private void drop(Payload payload, Vector2 vector2) {
        ResourcePayloadObject resourcePayloadObject = (ResourcePayloadObject) payload.getObject();
        ResourceManager resourceManager = HyperLap2DFacade.getInstance().retrieveProxy(ResourceManager.NAME);

        vector2.sub(resourcePayloadObject.xOffset/resourceManager.getProjectVO().pixelToWorld, resourcePayloadObject.yOffset/resourceManager.getProjectVO().pixelToWorld);
        factoryFunction.apply(resourcePayloadObject.name, vector2);
    }

    public DraggableResourceView getViewComponent() {
        return viewComponent;
    }

    public void setFactoryFunction(BiFunction<String, Vector2, Boolean> factoryFunction) {
        this.factoryFunction = factoryFunction;
    }

    @Override
    public int compareTo(DraggableResource o) {
        return viewComponent.getPayloadData().name.compareTo(o.viewComponent.getPayloadData().name);
    }
}
