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

package games.rednblack.editor.utils.poly.tracer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.utils.poly.TextureUtils;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Tracer {
	public static Vector2[][] trace(TextureRegion texture, float hullTolerance, int alphaTolerance, boolean multiPartDetection, boolean holeDetection) {
		Pixmap pixmap = TextureUtils.getPOTPixmap(texture);

		Blending blending = pixmap.getBlending();
		pixmap.setBlending(Blending.None);


		int w = pixmap.getWidth();
		int h = pixmap.getHeight();

		int size = w * h;
		int[] array = new int[size];

		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				int color = pixmap.getPixel(x, y);
				array[x + y*w] = color;
			}
		}

		pixmap.setBlending(blending);
		pixmap.dispose();

		Array<Array<Vector2>> outlines;
		try {
			outlines = TextureConverter.createPolygon(array, w, h, hullTolerance, alphaTolerance, multiPartDetection, holeDetection);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		float th = texture.getRegionHeight();

		Vector2[][] polygons = new Vector2[outlines.size][];

		for (int i=0; i<outlines.size; i++) {
			Array<Vector2> outline = outlines.get(i);
			polygons[i] = new Vector2[outline.size];
			for (int ii=0; ii<outline.size; ii++) {
				polygons[i][ii] = outline.get(ii);
				polygons[i][ii].y = th - polygons[i][ii].y;
				polygons[i][ii].x /= Sandbox.getInstance().getPixelPerWU();
				polygons[i][ii].y /= Sandbox.getInstance().getPixelPerWU();
			}
		}

		return polygons;
	}
}
