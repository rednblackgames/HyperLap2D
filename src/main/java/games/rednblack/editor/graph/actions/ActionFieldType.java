package games.rednblack.editor.graph.actions;

import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.renderer.systems.action.data.*;

public enum ActionFieldType implements FieldType {
    Entity, Boolean, Float, Color, Action, Delay, Alpha, Parallel, Sequence;

    @Override
    public boolean accepts(Object value) {
        switch (this) {
            case Action:
                return value instanceof ActionData;
            case Parallel:
                return value instanceof ParallelData;
            case Sequence:
                return value instanceof SequenceData;
            case Alpha:
                return value instanceof AlphaData;
            case Delay:
                return value instanceof DelayData;
            case Entity:
                return value instanceof com.badlogic.ashley.core.Entity;
            case Color:
                return value instanceof com.badlogic.gdx.graphics.Color;
            case Boolean:
                return value instanceof Boolean;
            case Float:
                return value instanceof Float;
        }

        return false;
    }

    @Override
    public Object convert(Object value) {
        return value;
    }
}
