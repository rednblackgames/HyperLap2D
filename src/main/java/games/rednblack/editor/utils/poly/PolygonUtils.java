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
import com.badlogic.gdx.utils.*;
import games.rednblack.editor.utils.Vector2Pool;
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

	public static boolean intersectSegments(Array<Vector2> points, int index1, int index2, int index3, int index4) {
		Vector2 intersectionPoint = Pools.obtain(Vector2.class).set(points.get(index1));
		boolean isIntersecting = Intersector.intersectSegments(points.get(index1), points.get(index2), points.get(index3), points.get(index4), intersectionPoint);
		if(isIntersecting
				&& !isSamePoint(intersectionPoint, points.get(index1))
				&& !isSamePoint(intersectionPoint, points.get(index2))
				&& !isSamePoint(intersectionPoint, points.get(index3))
				&& !isSamePoint(intersectionPoint, points.get(index4))) {
			Pools.free(intersectionPoint);
			return true;
		}
		Pools.free(intersectionPoint);
		return false;
	}

	public static boolean isSamePoint(Vector2 point1, Vector2 point2) {
		int pixelsPerWU = Sandbox.getInstance().getPixelPerWU();
		int precision = 1000 * pixelsPerWU;
		Vector2 pointA = Pools.obtain(Vector2.class).set(point1);
		Vector2 pointB = Pools.obtain(Vector2.class).set(point2);
		pointA.x = Math.round(point1.x * precision) / (float)precision;
		pointA.y = Math.round(point1.y * precision) / (float)precision;
		pointB.x = Math.round(point2.x * precision) / (float)precision;
		pointB.y = Math.round(point2.y * precision) / (float)precision;
		boolean res = pointA.equals(pointB);
		Pools.free(pointA);
		Pools.free(pointB);
		return res;
	}

	public static IntSet checkForIntersection(int anchor, Array<Vector2> points, IntSet problems) {
		problems.clear();
		int leftPointIndex = points.size - 1;
		int rightPointIndex = 0;
		if (anchor > 0) {
			leftPointIndex = anchor - 1;
		}
		if (anchor < points.size - 1) {
			rightPointIndex = anchor + 1;
		}

		for (int i = 0; i < points.size - 1; i++) {
			if (i != leftPointIndex && i != anchor) {
				if(intersectSegments(points, i, i+1, leftPointIndex, anchor)) {
					problems.add(leftPointIndex);
				}

				if(intersectSegments(points, i, i+1, anchor, rightPointIndex)) {
					problems.add(anchor);
				}
			}
		}

		if (anchor != points.size - 1 && leftPointIndex != points.size - 1 && intersectSegments(points, points.size - 1, 0, leftPointIndex, anchor)) {
			problems.add(leftPointIndex);
		}
		if(anchor != points.size - 1 && leftPointIndex != points.size - 1 && intersectSegments(points, points.size - 1, 0, anchor, rightPointIndex)) {
			problems.add(anchor);
		}

		if(problems.size == 0) {
			return null;
		}

		return problems;
	}

	public static Pool<Vector2> vector2Pool = new Vector2Pool(60);
	private static final Array<Vector2> tmpResult = new Array<>();

	public static Array<Vector2> getCurvedLine(Vector2 from, Vector2 to, Vector2 center1, Vector2 center2, int segments) {
		tmpResult.clear();

		float subdiv_step = 1f / segments;
		float subdiv_step2 = subdiv_step * subdiv_step;
		float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;

		float pre1 = 3 * subdiv_step;
		float pre2 = 3 * subdiv_step2;
		float pre4 = 6 * subdiv_step2;
		float pre5 = 6 * subdiv_step3;

		float tmp1x = from.x - center1.x * 2 + center2.x;
		float tmp1y = from.y - center1.y * 2 + center2.y;

		float tmp2x = (center1.x - center2.x) * 3 - from.x + to.x;
		float tmp2y = (center1.y - center2.y) * 3 - from.y + to.y;

		float fx = from.x;
		float fy = from.y;

		float dfx = (center1.x - from.x) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
		float dfy = (center1.y - from.y) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;

		float ddfx = tmp1x * pre4 + tmp2x * pre5;
		float ddfy = tmp1y * pre4 + tmp2y * pre5;

		float dddfx = tmp2x * pre5;
		float dddfy = tmp2y * pre5;

		while (segments-- > 0) {
			tmpResult.add(vector2Pool.obtain().set(fx, fy));
			fx += dfx;
			fy += dfy;
			dfx += ddfx;
			dfy += ddfy;
			ddfx += dddfx;
			ddfy += dddfy;
		}
		tmpResult.add(vector2Pool.obtain().set(fx, fy));
		tmpResult.add(vector2Pool.obtain().set(to.x, to.y));

		return tmpResult;
	}
}
