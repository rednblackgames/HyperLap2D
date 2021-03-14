package games.rednblack.editor.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import games.rednblack.editor.renderer.systems.render.HyperLap2dRenderer;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.h2d.common.HyperLog;
import org.apache.commons.lang3.SystemUtils;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

public class BootstrapInfoCommand extends SimpleCommand {

    @Override
    public void execute(INotification notification) {
        HyperLog.infoUnderline("HyperLap2D " + AppConfig.getInstance().versionString);
        HyperLog.info(SystemUtils.OS_NAME + " [" + SystemUtils.OS_VERSION + " - " + SystemUtils.OS_ARCH + "]");
        GL20 gl20 = Gdx.graphics.getGL20();
        String gpu = gl20.glGetString(GL20.GL_RENDERER);
        if (gpu.toLowerCase().contains("hd graphics")) {
            /*
            TODO the most weird bug I've ever found!
            clearColor inside FBO freeze rendering on some Intel HD Graphics GPU
            unless custom Fragment Shader is provided see @DefaultShaders#DEFAULT_FRAGMENT_SHADER
             */
            HyperLap2dRenderer.clearColor = new Color(0.000001f, 0, 0, 0);
        }
        HyperLog.info(gl20.glGetString(GL20.GL_VENDOR) + " - " + gpu);
        HyperLog.info("Screen size " + Gdx.graphics.getBackBufferWidth() + " x " + Gdx.graphics.getBackBufferHeight());
        HyperLog.info("GL version " + gl20.glGetString(GL20.GL_VERSION));
        HyperLog.info("Shaders version " + gl20.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
    }
}
