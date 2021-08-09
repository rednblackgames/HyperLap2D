package games.rednblack.editor.graph;

import com.google.common.base.Function;
import games.rednblack.editor.graph.data.FieldType;

import java.util.Map;

public class SameTypeOutputTypeFunction<T extends FieldType> implements Function<Map<String, T>, T> {
    private String[] inputs;

    public SameTypeOutputTypeFunction(String... input) {
        this.inputs = input;
    }

    @Override
    public T apply(Map<String, T> map) {
        T resolvedType = null;
        for (String input : inputs) {
            T type = map.get(input);
            if (type == null)
                return null;
            if (resolvedType != null && resolvedType != type)
                return null;
            resolvedType = type;
        }

        return resolvedType;
    }
}
