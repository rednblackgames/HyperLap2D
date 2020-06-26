package games.rednblack.editor.renderer.systems.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Stack;

public class ShaderManager {
    private final ArrayMap<String, ShaderProgram> shaders;
    private final ArrayMap<String, FrameBuffer> frameBuffers;
    private final Stack<FrameBuffer> activeFrameBuffers;
    private final Camera screenCamera;
    private final Mesh screenMesh;
    private ShaderProgram currentShader;
    private int currentTextureId;

    public ShaderManager() {
        shaders = new ArrayMap<>();

        frameBuffers = new ArrayMap<>();
        activeFrameBuffers = new Stack<>();

        screenCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenMesh = new Mesh(true, 4, 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        Vector3 vec0 = new Vector3(0, 0, 0);
        screenCamera.unproject(vec0);
        Vector3 vec1 = new Vector3(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);
        screenCamera.unproject(vec1);
        screenMesh.setVertices(new float[]{
                vec0.x, vec0.y, 0, 1, 1, 1, 1, 0, 1,
                vec1.x, vec0.y, 0, 1, 1, 1, 1, 1, 1,
                vec1.x, vec1.y, 0, 1, 1, 1, 1, 1, 0,
                vec0.x, vec1.y, 0, 1, 1, 1, 1, 0, 0});
        screenMesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});
        screenCamera.translate(0f, -1f, 0f);
        screenCamera.update();
    }

    public void begin(String shaderName) {
        // check if we have a shader that has not been end()ed
        if (currentShader != null) {
            throw new IllegalArgumentException("Before calling begin() for a new shader please call end() for the current one!");
        }
        // check if we have a program for that name
        ShaderProgram program = shaders.get(shaderName);

        begin(program);
    }

    public void begin(ShaderProgram program) {
        // check if we have a shader that has not been end()ed
        if (currentShader != null) {
            throw new IllegalArgumentException("Before calling begin() for a new shader please call end() for the current one!");
        }

        currentTextureId = 0;

        currentShader = program;
        currentShader.begin();
    }

    public void end() {
        currentShader.end();
        currentShader = null;
    }

    /**
     * Creates a new FrameBuffer with the format RGBA8888 and <code>Gdx.graphics.getWidth()</code> and <code>Gdx.graphics.getHeight()</code>
     *
     * @param frameBufferName name of the new FrameBuffer
     */
    public void createFrameBuffer(String frameBufferName) {
        createFrameBuffer(frameBufferName, Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Creates a new FrameBuffer with the format RGBA8888 and the given width and height
     *
     * @param frameBufferName name of the new FrameBuffer
     * @param width           of the FrameBuffer
     * @param height          of the FrameBuffer
     */
    public void createFrameBuffer(String frameBufferName, int width, int height) {
        createFrameBuffer(frameBufferName, Pixmap.Format.RGBA8888, width, height);
    }

    /**
     * Creates a new FrameBuffer with the format RGBA8888 and the given width and height
     *
     * @param frameBufferName name of the new FrameBuffer
     * @param format          of the FrameBuffer, see Pixmap.Format for valid values
     * @param width           of the FrameBuffer
     * @param height          of the FrameBuffer
     */
    public void createFrameBuffer(String frameBufferName, Pixmap.Format format, int width, int height) {
        if (frameBuffers.containsKey(frameBufferName)) {
            throw new IllegalArgumentException("A framebuffer with the name '" + frameBufferName + "' already exists");
        }
        FrameBuffer frameBuffer = new FrameBuffer(format, width, height, false, false);
        frameBuffers.put(frameBufferName, frameBuffer);
    }

    /**
     * Start rendering into the given FrameBuffer
     *
     * @param frameBufferName name of the FrameBuffer
     */
    public void beginFrameBuffer(String frameBufferName) {
        beginFrameBuffer(frameBufferName, 0f, 0f, 0f, 0f);
    }

    /**
     * Start rendering into the given FrameBuffer with the specified clear color.
     *
     * @param frameBufferName name of the FrameBuffer
     */
    public void beginFrameBuffer(String frameBufferName, float clearColorRed, float clearColorGreen, float clearColorBlue, float clearColorAlpha) {
        if (!frameBuffers.containsKey(frameBufferName)) {
            throw new IllegalArgumentException("A framebuffer with the name '" + frameBufferName + "' has not been created");
        }
        final FrameBuffer frameBuffer = frameBuffers.get(frameBufferName);
        frameBuffer.begin();
        activeFrameBuffers.push(frameBuffer);

        //Gdx.graphics.getGL20().glClearColor(clearColorRed, clearColorGreen, clearColorBlue, clearColorAlpha);
        initInitialFrameBufferState(frameBuffer);
    }

    /**
     * Sets the initial state when a FrameBuffer starts. Override to set your own state.
     *
     * @param frameBuffer the FrameBuffer for which the state is initialized
     */
    protected void initInitialFrameBufferState(FrameBuffer frameBuffer) {
        Gdx.graphics.getGL20().glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
        Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.graphics.getGL20().glEnable(GL20.GL_TEXTURE_2D);
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Stops rendering to the current FrameBuffer
     */
    public void endFrameBuffer() {
        if (activeFrameBuffers.empty()) {
            throw new GdxRuntimeException("There is no active frame buffer that can be ended");
        }
        final FrameBuffer frameBuffer = activeFrameBuffers.pop();
        frameBuffer.end();
    }

    /**
     * Renders the given FrameBuffer to the screen using the current ShaderProgram
     *
     * @param frameBufferName name of the FrameBuffer to render
     */
    public void renderFrameBuffer(String frameBufferName) {
        renderFrameBuffer(frameBufferName, screenMesh);
    }

    /**
     * Renders the given FrameBuffer onto a Mesh using the current ShaderProgram
     *
     * @param frameBufferName name of the FrameBuffer to render
     * @param target          Mesh to render onto
     */
    public void renderFrameBuffer(String frameBufferName, Mesh target) {
        if (currentShader == null) {
            throw new GdxRuntimeException("Rendering the frame buffers needs an active shader");
        }
        FrameBuffer frameBuffer = frameBuffers.get(frameBufferName);
        if (frameBuffer == null) {
            throw new GdxRuntimeException("A framebuffer with the name '" + frameBufferName + "' could not be found");
        }
        frameBuffer.getColorBufferTexture().bind(0);
        currentShader.setUniformMatrix("u_projTrans", screenCamera.combined);
        currentShader.setUniformi("u_texture", 0);
        target.render(currentShader, GL20.GL_TRIANGLES);
    }

    /**
     * Return the FrameBuffer with the given name
     *
     * @param frameBufferName Name of the FrameBuffer to return
     * @return a FrameBuffer or <b>null</b> if there is no such FrameBuffer
     */
    public FrameBuffer getFrameBuffer(String frameBufferName) {
        return frameBuffers.get(frameBufferName);
    }

    /**
     * Returns the currently active shader.
     *
     * @return a valid ShaderProgram or <b>null</b>
     */
    public ShaderProgram getCurrentShader() {
        return currentShader;
    }

    public void setUniformMatrix(String uniformName, Matrix4 matrix) {
        if (currentShader == null) {
            throw new GdxRuntimeException("Please call begin() before setting uniforms");
        }
        currentShader.setUniformMatrix(uniformName, matrix);
    }

    public void setUniformf(String uniformName, float value) {
        if (currentShader == null) {
            throw new GdxRuntimeException("Please call begin() before setting uniforms");
        }
        currentShader.setUniformf(uniformName, value);
    }

    public void setUniform2fv(String uniformName, float[] values) {
        if (currentShader == null) {
            throw new GdxRuntimeException("Please call begin() before setting uniforms");
        }
        currentShader.setUniform2fv(uniformName, values, 0, 2);
    }

    public void setUniformTexture(String uniformName, Texture texture) {
        if (currentShader == null) {
            throw new GdxRuntimeException("Please call begin() before setting uniforms");
        }

        int textureId = ++currentTextureId;
        texture.bind(textureId);
        currentShader.setUniformi(uniformName, textureId);
    }

    /**
     * ShaderManager needs to be disposed if not used anymore.
     */
    public void dispose() {
        for (ShaderProgram program : shaders.values()) {
            program.dispose();
        }
        shaders.clear();

        screenMesh.dispose();
    }

}
