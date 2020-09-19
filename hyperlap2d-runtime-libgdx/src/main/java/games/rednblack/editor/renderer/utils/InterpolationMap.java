package games.rednblack.editor.renderer.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InterpolationMap {
    public static final Map<String, Interpolation> map = new HashMap<>();

    static {
        Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);
        for (Field field : interpolationFields) {
            try {
                map.put(field.getName(), (Interpolation) field.get(null));
            } catch (ReflectionException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getName(Interpolation interpolation) {
        for (Map.Entry<String, Interpolation> entry : map.entrySet()) {
            if (Objects.equals(interpolation, entry.getValue())) {
                return entry.getKey();
            }
        }
        return "linear";
    }
}
