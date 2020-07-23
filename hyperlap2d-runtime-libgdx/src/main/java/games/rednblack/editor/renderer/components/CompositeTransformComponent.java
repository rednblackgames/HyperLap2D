package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

public class CompositeTransformComponent implements BaseComponent {
	public boolean automaticResize = true;
	public boolean scissorsEnabled = false;
	public boolean transform = false;
	public final Affine2 worldTransform = new Affine2();
	public final Matrix4 computedTransform = new Matrix4();
	public final Matrix4 oldTransform = new Matrix4();
	public final Rectangle scissors = new Rectangle();
	public final Rectangle clipBounds = new Rectangle();

	@Override
	public void reset() {
		automaticResize = true;
		scissorsEnabled = false;
		transform = false;

		worldTransform.idt();
		computedTransform.idt();
		oldTransform.idt();
		scissors.set(0, 0, 0, 0);
		clipBounds.set(0, 0, 0, 0);
	}
}
