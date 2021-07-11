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
    private final Color fillColor;
    /**
     * The standard color of the border. Also the color of the border when the mouse is not over the image.
     */
    private final Color borderColor;
    /**
     * The color of the border when the mouse hovers over the image.
     */
    private final Color borderMouseOverColor;
    /**
     * The color to fill the background of the image when the mouse hovers over the image.
     */
    private final Color fillMouseOverColor;
    /**
     * Whether to change the border color when the mouse hovers over the image.
     */
    private boolean highlightWhenMouseOver;
    
    public BoxItemResource() {
    	this(new Color(1, 1, 1, 0.2f), new Color(1, 1, 1, 0.4f), Color.BLACK, Color.BLACK, false);
    }
    
    /**
     * Creates a new box item resource with the given colors.
     *
     * @param fillColor The color to fill the background of the image.
     * @param borderColor The standard color of the border. Also used when the mouse is not hovering over the image.
     * @param fillMouseOverColor The color to fill the background of the image when the mouse hovers over the image. Only used if the the parameter <code>highlightWhenMouseOver</code> is set to <code>true</code>.
     * @param borderMouseOverColor The color of the border when the mouse hovers over the image. Only used if the the parameter <code>highlightWhenMouseOver</code> is set to <code>true</code>.
     * @param highlightWhenMouseOver Whether to change the border color when the mouse hovers over the image.
     */
    public BoxItemResource(Color fillColor, Color borderColor, Color fillMouseOverColor, Color borderMouseOverColor, boolean highlightWhenMouseOver) {
        sandbox = Sandbox.getInstance();
        rc = new PixelRect(thumbnailSize, thumbnailSize);
        rc.setFillColor(fillColor);
        rc.setBorderColor(borderColor);
        addActor(rc);
        setWidth(thumbnailSize);
        setHeight(thumbnailSize);
        
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.fillMouseOverColor = fillMouseOverColor;
        this.borderMouseOverColor = borderMouseOverColor;
        this.highlightWhenMouseOver = highlightWhenMouseOver;
    }

    /**
     * Sets the right-click event. Should not be used with {@link #setClickEvent(String, String, Object, Object)}.
     * 
     * @param eventName The event name in case of a right-click.
     * @param payload The payload for the right-click.
     */
    public void setRightClickEvent(String eventName, String payload) {
    	setClickEvent(null, eventName, null, payload);
    }

    /**
     * Sets the left/right-click event. Should not be used with {@link #setRightClickEvent(String, String)}.
     * 
     * @param leftClickEventName The event name in case of a left-click.
     * @param leftClickPayload The payload for the left-click.
     * @param rightClickEventName The event name in case of a right-click.
     * @param rightClickPayload The payload for the right-click.
     */
    public void setClickEvent(String leftClickEventName, String rightClickEventName, Object leftClickPayload, Object rightClickPayload) {
        addListener(new InputListener() {
        	private boolean isOver;
            @Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                return true;
            }
            @Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            	// we only care for the event if the mouse is still in this resource
            	if (isOver && !event.isTouchFocusCancel()) {
	            	if(button == Input.Buttons.LEFT && leftClickEventName != null) {
	            		String eventType = UIResourcesBoxMediator.NORMAL_CLICK_EVENT_TYPE;
	            		if (UIUtils.shift() && UIUtils.ctrl()) {
	            			 eventType = UIResourcesBoxMediator.SHIFT_CTRL_EVENT_TYPE;
	            		} else if (UIUtils.shift()) {
	            			eventType = UIResourcesBoxMediator.SHIFT_EVENT_TYPE;
	            		} else if (UIUtils.ctrl()) {
	            			eventType = UIResourcesBoxMediator.CTRL_EVENT_TYPE;
	            		}
	            		HyperLap2DFacade.getInstance().sendNotification(leftClickEventName, leftClickPayload, eventType);
	            	}
	                if(button == Input.Buttons.RIGHT && rightClickEventName != null) {
	                    HyperLap2DFacade.getInstance().sendNotification(rightClickEventName, rightClickPayload);
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
            		rc.setFillColor(fillColor);
            		rc.setBorderColor(borderColor);
            	}
            }
        });
    }
    
    public void setHighlightWhenMouseOver(boolean highlightWhenMouseOver) {
    	this.highlightWhenMouseOver = highlightWhenMouseOver;
    }
    
    public void switchToMouseOverColor() {
    	if (fillMouseOverColor != null && borderMouseOverColor != null) {
			rc.setFillColor(fillMouseOverColor);
			rc.setBorderColor(borderMouseOverColor);
    	}
    }
    
    public void switchToStandardColor() {
    	if (fillColor != null && borderColor != null) {
			rc.setFillColor(fillColor);
			rc.setBorderColor(borderColor);
    	}
    }
    
}
