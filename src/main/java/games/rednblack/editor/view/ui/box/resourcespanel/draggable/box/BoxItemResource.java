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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Null;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResourceView;
import games.rednblack.editor.view.ui.widget.actors.basic.PixelRect;

/**
 * Created by sargis on 5/6/15.
 */
public abstract class BoxItemResource extends Group implements DraggableResourceView {
    protected final Sandbox sandbox;
    protected float thumbnailSize = 50;
    protected PixelRect rc;

    /**
     * The color to fill the background of the image. Also the color of the background when the mouse is not over the image.
     */
    private final Color fillColor = new Color(1, 1, 1, 0.2f);
    /**
     * The standard color of the border. Also the color of the border when the mouse is not over the image.
     */
    private final Color borderColor = new Color(1, 1, 1, 0.4f);
    /**
     * The color of the border when the mouse hovers over the image.
     */
    private final Color borderMouseOverColor = new Color(1f, 94f / 255f, 0f / 255f, 1f);
    /**
     * The color to fill the background of the image when the mouse hovers over the image.
     */
    private final Color fillMouseOverColor = new Color(200f / 255f, 200f / 255f, 200f / 255f, 0.2f);
    /**
     * Whether to change the border color when the mouse hovers over the image.
     * Only used if the the parameter <code>highlightWhenMouseOver</code> is set to <code>true</code>.
     */
    private boolean highlightWhenMouseOver;

    /**
     * The standard thickness of the border. Also the thickness of the border when the mouse is not over the image.
     */
    private float borderThickness = 1f;
    /**
     * The thickness of the border when the mouse hovers the image.
     */
    private float borderMouseOverThickness = 2f;

    public BoxItemResource() {
    	this(false);
    }
    
    /**
     * Creates a new box item resource with the given colors.
     *
     * @param highlightWhenMouseOver Whether to change the border color when the mouse hovers over the image.
     */
    public BoxItemResource(boolean highlightWhenMouseOver) {
        sandbox = Sandbox.getInstance();
        rc = new PixelRect(thumbnailSize, thumbnailSize);
        rc.setFillColor(fillColor);
        rc.setBorderColor(borderColor);
        rc.setThickness(borderThickness);
        addActor(rc);
        setWidth(thumbnailSize);
        setHeight(thumbnailSize);

        thumbnailSize -= Math.max(borderThickness, borderMouseOverThickness);

        this.highlightWhenMouseOver = highlightWhenMouseOver;
    }

    /**
     * Sets the right-click event. Should not be used with {@link #setClickEvent(String, Object, String, Object)}.
     * 
     * @param eventName The event name in case of a right-click.
     * @param payload The payload for the right-click.
     */
    public void setRightClickEvent(String eventName, String payload) {
    	setClickEvent(null, null, eventName, payload);
    }

    /**
     * Sets the left/right-click event. Should not be used with {@link #setRightClickEvent(String, String)}.
     *
     * Will be fired {@link UIResourcesBoxMediator#RESOURCE_BOX_LEFT_CLICK} and {@link UIResourcesBoxMediator#RESOURCE_BOX_RIGHT_CLICK}.
     * 
     * @param leftClickEventName The event name in case of a left-click.
     * @param leftClickPayload The payload for the left-click.
     * @param rightClickEventName The event name in case of a right-click.
     * @param rightClickPayload The payload for the right-click.
     */
    public void setClickEvent(String leftClickEventName, Object leftClickPayload, String rightClickEventName, Object rightClickPayload) {
        addListener(new InputListener() {
        	private boolean isOver;
            @Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            	// we only care for the event if the mouse is still in this resource
            	if (isOver && !event.isTouchFocusCancel()) {
                    String eventType = UIResourcesBoxMediator.NORMAL_CLICK_EVENT_TYPE;
                    if (UIUtils.shift() && UIUtils.ctrl()) {
                        eventType = UIResourcesBoxMediator.SHIFT_CTRL_EVENT_TYPE;
                    } else if (UIUtils.shift()) {
                        eventType = UIResourcesBoxMediator.SHIFT_EVENT_TYPE;
                    } else if (UIUtils.ctrl()) {
                        eventType = UIResourcesBoxMediator.CTRL_EVENT_TYPE;
                    }

	            	if(button == Input.Buttons.LEFT) {
	            		HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.RESOURCE_BOX_LEFT_CLICK, BoxItemResource.this, eventType);

	            		if (leftClickEventName != null)
                            HyperLap2DFacade.getInstance().sendNotification(leftClickEventName, leftClickPayload, eventType);
	            	}

	                if(button == Input.Buttons.RIGHT) {
	                    HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.RESOURCE_BOX_RIGHT_CLICK, BoxItemResource.this, eventType);

                        if (rightClickEventName != null)
                            HyperLap2DFacade.getInstance().sendNotification(rightClickEventName, rightClickPayload, eventType);
	                }
            	}
            }
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
            	// mouse is in the resource
            	isOver = true;
            	// check if we have to change the color
            	if (highlightWhenMouseOver) {
            		switchToMouseOverColor();
            	}
            }
            @Override
            public void exit (InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
            	// mouse no longer in the resource
            	isOver = false;
            	// check if we have to revert the color
            	if (highlightWhenMouseOver) {
                    switchToStandardColor();
            	}
            }
        });
    }
    
    public void setHighlightWhenMouseOver(boolean highlightWhenMouseOver) {
    	this.highlightWhenMouseOver = highlightWhenMouseOver;
    }
    
    public void switchToMouseOverColor() {
        rc.setFillColor(fillMouseOverColor);
        rc.setBorderColor(borderMouseOverColor);
        rc.setThickness(borderMouseOverThickness);
    }
    
    public void switchToStandardColor() {
        rc.setFillColor(fillColor);
        rc.setBorderColor(borderColor);
        rc.setThickness(borderThickness);
    }

    public void setFillColor(Color color) {
        fillColor.set(color);
    }

    public void setFillColor(float r, float g, float b, float a) {
        fillColor.set(r, g, b, a);
    }

    public void setBorderColor(Color color) {
        borderColor.set(color);
    }

    public void setBorderColorColor(float r, float g, float b, float a) {
        borderColor.set(r, g, b, a);
    }

    public void setHighlightFillColor(Color color) {
        fillMouseOverColor.set(color);
    }

    public void setHighlightFillColor(float r, float g, float b, float a) {
        fillMouseOverColor.set(r, g, b, a);
    }

    public void setHighlightBorderColor(Color color) {
        borderMouseOverColor.set(color);
    }

    public void setHighlightBorderColorColor(float r, float g, float b, float a) {
        borderMouseOverColor.set(r, g, b, a);
    }

    public void setBorderThickness(float borderThickness) {
        this.borderThickness = borderThickness;
    }

    public void setHighlightBorderThickness(float borderThickness) {
        this.borderMouseOverThickness = borderThickness;
    }
}
