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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import games.rednblack.editor.HyperLap2DFacade;

public class PixelRect extends Group {

    private final HyperLap2DFacade facade;
    private final PixelLine[] lines = new PixelLine[4];
    private final Image fill;
    private final Rectangle rectangle = new Rectangle();

    public PixelRect() {
        this(0, 0);
    }

    public PixelRect(float width, float height) {
        facade = HyperLap2DFacade.getInstance();
        lines[0] = new PixelLine(0, 0, width, 0);
        lines[1] = new PixelLine(0, 0, 0, height);
        lines[1].setPosition(lines[1].getThickness(), 0, lines[1].getThickness(), height - lines[1].getThickness());
        lines[2] = new PixelLine(width, 0, width, height);
        lines[3] = new PixelLine(0, height, width, height);
        lines[3].setPosition(0, height - lines[3].getThickness(), width, height - lines[3].getThickness());

        fill = new Image(WhitePixel.sharedInstance.texture);
        fill.setColor(new Color(0, 0, 0, 0));

        addActor(fill);

        addActor(lines[0]);
        addActor(lines[1]);
        addActor(lines[2]);
        addActor(lines[3]);

        super.setWidth(width);
        super.setHeight(height);

        setOrigin(getWidth() / 2, getHeight() / 2);
        fill.setScaleX(getWidth());
        fill.setScaleY(getHeight());
    }

    public void setBorderColor(Color clr) {
        lines[0].setColor(clr);
        lines[1].setColor(clr);
        lines[2].setColor(clr);
        lines[3].setColor(clr);
    }

    public void setFillColor(Color clr) {
        fill.setColor(clr);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);

        lines[0].setPosition(0, 0, width, 0);
        lines[2].setPosition(width, 0, width, getHeight());
        lines[3].setPosition(0, getHeight(), width, getHeight());
        fill.setScaleX(getWidth());
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        lines[1].setPosition(0, 0, 0, height);
        lines[2].setPosition(getWidth(), 0, getWidth(), height);
        lines[3].setPosition(0, height, getWidth(), height);
        fill.setScaleY(getHeight());
    }

    public void setOpacity(float opacity) {
        addAction(Actions.alpha(opacity, 0.3f, Interpolation.exp5Out));
    }

    public Rectangle getRect() {
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        if (width < 0) {
            width = -width;
            x = x - width;
        }
        if (height < 0) {
            height = -height;
            y = y - height;
        }

        return rectangle.set(x, y, width, height);
    }

    public void setThickness(float thickness) {
        lines[0].setThickness(thickness);
        lines[1].setThickness(thickness);
        lines[1].setPosition(lines[1].getThickness(), 0, lines[1].getThickness(), getHeight() - lines[1].getThickness());
        lines[2].setThickness(thickness);
        lines[3].setThickness(thickness);

        lines[3].setPosition(0, getHeight() - lines[3].getThickness(), getWidth(), getHeight() - lines[3].getThickness());
    }

}
