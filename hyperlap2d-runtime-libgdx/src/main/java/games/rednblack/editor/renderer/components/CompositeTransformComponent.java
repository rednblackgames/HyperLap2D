package games.rednblack.editor.renderer.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

public class CompositeTransformComponent implements Component {
	public boolean automaticResize = true;
	public boolean scissorsEnabled = false;
	public boolean transform = false;
	public final Affine2 worldTransform = new Affine2();
	public final Matrix4 computedTransform = new Matrix4();
	public final Matrix4 oldTransform = new Matrix4();
	public final Rectangle scissors = new Rectangle();
	public final Rectangle clipBounds = new Rectangle();
}
