package games.rednblack.editor;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationGLESFix;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import games.rednblack.editor.proxy.SettingsManager;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] argv) {
        if (restartStartOnFirstThread()) {
            return;
        }

        Thread.currentThread().setUncaughtExceptionHandler(new CustomExceptionHandler());
        //Increase default lwjgl stack size
        System.setProperty("org.lwjgl.system.stackSize", "256");

        HyperLap2DFacade.getInstance();

        Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode();

        SettingsManager settingsManager = new SettingsManager();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(467,415);
        config.setResizable(false);
        config.setDecorated(false);
        config.setInitialVisible(false);
        config.setIdleFPS(60);
        config.setForegroundFPS(settingsManager.editorConfigVO.fpsLimit);
        config.useVsync(false);
        config.setTitle("HyperLap2D");
        config.setWindowIcon("hyperlap_icon_96.png");
        if (settingsManager.editorConfigVO.useANGLEGLES2)
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 3, 2);
        config.setBackBufferConfig(8,8,8,8,16,8, settingsManager.editorConfigVO.msaaSamples);

        new Lwjgl3ApplicationGLESFix(HyperLap2DApp.initInstance(dm.width, dm.height, settingsManager), config);
    }

    public static String getJarContainingFolder(Class aclass) {
        CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

        File jarFile;

        if (codeSource.getLocation() != null) {
            try {
                jarFile = new File(codeSource.getLocation().toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return "";
            }
        }
        else {
            String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
            String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
            try {
                jarFilePath = URLDecoder.decode(jarFilePath, String.valueOf(StandardCharsets.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            jarFile = new File(jarFilePath);
        }
        return jarFile.getParentFile().getAbsolutePath();
    }

    public static boolean restartStartOnFirstThread() {
        // if not a mac return false
        if (!SystemUtils.IS_OS_MAC_OSX && !SystemUtils.IS_OS_MAC) {
            return false;
        }

        // get current jvm process pid
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        // get environment variable on whether XstartOnFirstThread is enabled
        String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);

        // if environment variable is "1" then XstartOnFirstThread is enabled
        if (env != null && env.equals("1")) {
            return false;
        }

        // restart jvm with -XstartOnFirstThread
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        String jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        ArrayList<String> jvmArgs = new ArrayList<>();

        jvmArgs.add(jvmPath);
        jvmArgs.add("-XstartOnFirstThread");
        jvmArgs.add("-Djava.awt.headless=true");
        jvmArgs.addAll(inputArguments);
        jvmArgs.add("-cp");
        jvmArgs.add(classpath);
        jvmArgs.add(mainClass);

        // if you don't need console output, just enable these two lines
        // and delete bits after it. This JVM will then terminate.
        //ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
        //processBuilder.start();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
