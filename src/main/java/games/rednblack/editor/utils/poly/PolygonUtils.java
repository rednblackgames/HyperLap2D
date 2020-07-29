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

package games.rednblack.editor.utils.poly;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.view.stage.Sandbox;

import java.util.HashSet;

/**
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class PolygonUtils {
	public static float getPolygonSignedArea(Vector2[] points) {
		if (points.length < 3)
			return 0;

		float sum = 0;
		for (int i = 0; i < points.length; i++) {
			Vector2 p1 = points[i];
			Vector2 p2 = i != points.length-1 ? points[i+1] : points[0];
			sum += (p1.x * p2.y) - (p1.y * p2.x);
		}
		return 0.5f * sum;
	}

	public static float getPolygonArea(Vector2[] points) {
		return Math.abs(getPolygonSignedArea(points));
	}

	public static boolean isPolygonCCW(Vector2[] points) {
		return getPolygonSignedArea(points) > 0;
	}

	public static Vector2[][] polygonize(Vector2[] vertices) {
		return Clipper.polygonize(Clipper.Polygonizer.EWJORDAN, vertices);
	}

	public static boolean intersectSegments(Vector2[] points, int index1, int index2, int index3, int index4) {
		Vector2 intersectionPoint = new Vector2(points[index1]);
		boolean isIntersecting = Intersector.intersectSegments(points[index1], points[index2], points[index3], points[index4], intersectionPoint);
		if(isIntersecting && !isSamePoint(intersectionPoint, points[index1]) && !isSamePoint(intersectionPoint, points[index2]) && !isSamePoint(intersectionPoint, points[index3]) && !isSamePoint(intersectionPoint, points[index4])) {
			return true;
		}

		return false;
	}

	public static boolean isSamePoint(Vector2 point1, Vector2 point2) {
		int pixelsPerWU = Sandbox.getInstance().getPixelPerWU();
		int precision = 10000 * pixelsPerWU;
		Vector2 pointA = new Vector2(point1);
		Vector2 pointB = new Vector2(point2);
		pointA.x = Math.round(point1.x * precision) / (float)precision;
		pointA.y = Math.round(point1.y * precision) / (float)precision;
		pointB.x = Math.round(point2.x * precision) / (float)precision;
		pointB.y = Math.round(point2.y * precision) / (float)precision;

		return pointA.equals(pointB);
	}


	public static int[] checkForIntersection(int anchor, Vector2[] points) {
		int leftPointIndex = points.length-1;
		int rightPointIndex = 0;
		if(anchor > 0) {
			leftPointIndex = anchor-1;
		}
		if(anchor < points.length-1) {
			rightPointIndex =  anchor+1;
		}

		HashSet<Integer> problems = new HashSet<>();

		for(int i = 0; i < points.length-1; i++) {

			if(i != leftPointIndex && i != anchor) {
				if(intersectSegments(points, i, i+1, leftPointIndex, anchor)) {
					problems.add(leftPointIndex);
				}
				if(intersectSegments(points, i, i+1, anchor, rightPointIndex)) {
					problems.add(anchor);
				}
			}
		}
		if(anchor != points.length-1 && leftPointIndex != points.length-1 && intersectSegments(points, points.length-1, 0, leftPointIndex, anchor)) {
			problems.add(leftPointIndex);
		}
		if(anchor != points.length-1 && leftPointIndex != points.length-1 && intersectSegments(points, points.length-1, 0, anchor, rightPointIndex)) {
			problems.add(anchor);
		}

		if(problems.size() == 0) {
			return null;
		}

		int[] result = problems.stream().mapToInt(i->i).toArray();

		return result;
	}
}
