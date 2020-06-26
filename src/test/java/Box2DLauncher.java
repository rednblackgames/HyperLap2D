import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Box2DLauncher {

    public static void main(String[] argv) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("box2d lights test");
        config.setWindowedMode(800, 400);

        new Lwjgl3Application(new Box2dLightTest(), config);
    }

}