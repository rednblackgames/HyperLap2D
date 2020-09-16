package games.rednblack.editor.graph.actions;

import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.renderer.systems.action.data.AlphaData;
import games.rednblack.editor.renderer.systems.action.data.DelayData;

public enum ActionFieldType implements FieldType {
    Entity, Delay, Alpha;

    @Override
    public boolean accepts(Object value) {
        switch (this) {
            case Alpha:
                return value instanceof AlphaData;
            case Delay:
                return value instanceof DelayData;
            case Entity:
                return value instanceof com.badlogic.ashley.core.Entity;
        }

        return false;
    }

    @Override
    public Object convert(Object value) {
        return value;
    }
}
