package games.rednblack.editor.utils.runtime;

import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class TalosExportFormat {

    public Metadata metadata = new Metadata();

    public Array<Emitter> emitters = new Array<>();

    public static class Metadata {
        public Array<String> resources = new Array<>();
    }

    public static class Emitter {
        public Array<Module> modules = new Array<>();
    }

    public static class Module extends HashMap<String, Object> {

    }
}
