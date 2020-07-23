package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import games.rednblack.editor.renderer.scripts.IScript;

import java.util.Iterator;

/**
 * Created by azakhary on 6/19/2015.
 */
public class ScriptComponent implements BaseComponent {

    public Array<IScript> scripts = new Array<>();

    public void addScript(IScript script) {
        scripts.add(script);
    }

    public void addScript(String className) {
        try {
            IScript script = (IScript) ClassReflection.newInstance(ClassReflection.forName(className));
            addScript(script);
        } catch (ReflectionException ignored) {

        }
    }

    public void removeScript(Class className) {
        Iterator<IScript> i = scripts.iterator();
        while (i.hasNext()) {
            IScript s = i.next();
            if(s.getClass().getName().equals(className)) {
                i.remove();
            }
        }
    }

    @Override
    public void reset() {
        scripts.clear();
    }
}
