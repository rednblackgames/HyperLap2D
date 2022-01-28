package games.rednblack.editor.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
        HyperLog.info(gl20.glGetString(GL20.GL_VENDOR) + " - " + gl20.glGetString(GL20.GL_RENDERER));
        HyperLog.info("Screen size " + Gdx.graphics.getBackBufferWidth() + " x " + Gdx.graphics.getBackBufferHeight());
        HyperLog.info("GL version " + gl20.glGetString(GL20.GL_VERSION));
        HyperLog.info("Shaders version " + gl20.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));

        HyperLog.info("JVM Version: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
    }
}
