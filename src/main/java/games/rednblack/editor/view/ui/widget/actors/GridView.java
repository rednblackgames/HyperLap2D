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

package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.stage.Sandbox;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GridView extends Actor {

    private final Array<Segment> lines = new Array<>();
    private final ShapeDrawer shapeDrawer;

    private final Label zeroLabel;

    private int pixelsPerWU = 1;

    private int gridSize;
    int gridLinesCount;

    private final Color tmpColor = new Color();

    public GridView(ShapeDrawer shapeDrawer) {
        this.shapeDrawer = shapeDrawer;
        gridSize = 50;
        gridLinesCount = 80;

        pixelsPerWU = Sandbox.getInstance().getPixelPerWU();

        for (int i = 0; i < gridLinesCount; i++) {
            Segment tmp = new Segment(i * gridSize - (gridLinesCount / 2f - 1) * gridSize,
                    -(gridLinesCount / 2f - 1) * gridSize, i * gridSize - (gridLinesCount / 2f - 1) * gridSize,
                    gridSize * gridLinesCount - (gridLinesCount / 2f - 1) * gridSize);
            lines.add(tmp);
        }

        for (int i = 0; i < gridLinesCount; i++) {
            Segment tmp = new Segment(-(gridLinesCount / 2f - 1) * gridSize,
                    i * gridSize - (gridLinesCount / 2f - 1) * gridSize,
                    gridSize * gridLinesCount - (gridLinesCount / 2f - 1) * gridSize,
                    i * gridSize - (gridLinesCount / 2f - 1) * gridSize);
            lines.add(tmp);
        }

        this.setWidth(gridSize * gridLinesCount);
        this.setHeight(gridSize * gridLinesCount);

        zeroLabel = new VisLabel("0.0");
        zeroLabel.setColor(new Color(1, 1, 1, 0.4f));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        OrthographicCamera uiCamera = (OrthographicCamera) Sandbox.getInstance().getUIStage().getCamera();
        batch.setProjectionMatrix(uiCamera.projection);

        drawLines();

        OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();
        zeroLabel.draw(batch, parentAlpha);
        zeroLabel.setX(-(runtimeCamera.position.x * pixelsPerWU) / runtimeCamera.zoom - 5 - zeroLabel.getWidth());
        zeroLabel.setY(-(runtimeCamera.position.y * pixelsPerWU) / runtimeCamera.zoom - zeroLabel.getHeight());
    }

    private void drawLines() {
        OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();
        float offsetX = (runtimeCamera.position.x * pixelsPerWU) / runtimeCamera.zoom % gridSize;
        float offsetY = (runtimeCamera.position.y * pixelsPerWU) / runtimeCamera.zoom % gridSize;

        for (int i = 0; i < lines.size; i++) {
            shapeDrawer.setColor(getLineColor(i));
            shapeDrawer.line(lines.get(i).start.x - offsetX,
                    lines.get(i).start.y - offsetY,
                    lines.get(i).end.x - offsetX,
                    lines.get(i).end.y - offsetY,
                    1.0f);
        }
    }

    private Color getLineColor(int i) {
        OrthographicCamera runtimeCamera = Sandbox.getInstance().getCamera();

        float offsetTmp = ((runtimeCamera.position.x * pixelsPerWU) / runtimeCamera.zoom) / gridSize;
        if (i >= gridLinesCount) {
            i -= gridLinesCount;
            offsetTmp = ((runtimeCamera.position.y * pixelsPerWU) / runtimeCamera.zoom) / gridSize;
        }

        // offset
        int offset = 0;
        if (offsetTmp > 0) offset = (int) Math.floor(offsetTmp);
        if (offsetTmp < 0) offset = (int) -Math.floor(-offsetTmp);
        i += offset;

        Color color = tmpColor.set(Color.WHITE);

        if ((gridLinesCount / 2 - i - 1) % 4 == 0) {
            color.a = 0.1f;
        } else if ((gridLinesCount / 2 - i - 1) % 2 == 0) {
            color.a = 0.05f;
        } else {
            color.a = 0.02f;
        }

        return color;
    }

    public static class Segment {
        public Vector2 start;
        public Vector2 end;

        public Segment(float startX, float startY, float endX, float endY) {
            start = new Vector2(startX, startY);
            end = new Vector2(endX, endY);
        }

        public Segment(Vector2 start, Vector2 end) {
            this.start = start;
            this.end = end;
        }
    }
}
