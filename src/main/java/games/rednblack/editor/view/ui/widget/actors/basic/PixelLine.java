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

package games.rednblack.editor.view.ui.widget.actors.basic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class PixelLine extends Image {

	 private float thickness = 1f;

	public PixelLine(float x, float y, float toX, float toY) {
		super(WhitePixel.sharedInstance.texture);
		
		setPosition(x, y, toX, toY);
	}
	
	public void setPosition(float x, float y, float toX, float toY) {
		this.setX(x);
		this.setY(y);
		
		double lineLength = Math.sqrt((toX-x)*(toX-x)+(toY-y)*(toY-y));
		this.setScaleX((float) lineLength);
		
		this.setRotation(90-getAngle(x, y, toX, toY));
	}
	
	public void setOpacity(float opacity) {
		Color clr = getColor();
		clr.a = opacity;
		setColor(clr);
	}

	 @Override
	 public void act(float delta) {
		  super.act(delta);
		  this.setScaleY(thickness);
	 }
	
	private float getAngle(float x, float y, float toX, float toY) {
	    float angle = MathUtils.radiansToDegrees * (MathUtils.atan2(toX - x, toY - y));

	    if(angle < 0){
	        angle += 360;
	    }

	    return angle;
	}

	 public void setThickness (float thickness) {
		  this.thickness = thickness;
		 this.setScaleY(thickness);
	 }

	public float getThickness() {
		return thickness;
	}
}
