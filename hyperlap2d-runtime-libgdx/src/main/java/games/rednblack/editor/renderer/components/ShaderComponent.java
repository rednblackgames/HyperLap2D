package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderComponent implements BaseComponent {
	public String shaderName = "";
	private ShaderProgram shaderProgram = null;

	public void setShader(String name, ShaderProgram program) {
		shaderName = name;
		shaderProgram = program;
	}

	public ShaderProgram getShader() {
		return shaderProgram;
	}

	public void clear() {
		shaderName = "";
		shaderProgram = null;
	}

	@Override
	public void reset() {
		clear();
	}
}
