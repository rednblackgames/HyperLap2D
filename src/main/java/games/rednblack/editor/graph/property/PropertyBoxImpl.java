package games.rednblack.editor.graph.property;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import games.rednblack.editor.graph.GraphBox;
import games.rednblack.editor.graph.GraphBoxImpl;
import games.rednblack.editor.graph.GraphChangedEvent;
import games.rednblack.editor.graph.ValueGraphNodeOutput;
import games.rednblack.editor.graph.config.PropertyNodeConfiguration;
import games.rednblack.editor.graph.data.FieldType;

import java.util.HashMap;
import java.util.Map;

public class PropertyBoxImpl<T extends FieldType> extends Table implements PropertyBox<T> {
    private T propertyType;
    private PropertyDefaultBox propertyDefaultBox;
    private TextField textField;

    public PropertyBoxImpl(Skin skin, String name, T propertyType,
                           PropertyDefaultBox propertyDefaultBox) {
        super(skin);
        this.propertyType = propertyType;
        this.propertyDefaultBox = propertyDefaultBox;

        textField = new TextField(name, skin);
        Table headerTable = new Table(skin);
        headerTable.add(new Label("Name: ", skin));
        headerTable.add(textField).growX();
        textField.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        fire(new GraphChangedEvent(true, true));
                    }
                });
        headerTable.row();
        add(headerTable).growX().row();
        if (propertyDefaultBox != null)
            add(propertyDefaultBox.getActor()).growX().row();
    }

    @Override
    public T getType() {
        return propertyType;
    }

    @Override
    public String getName() {
        return textField.getText();
    }

    @Override
    public Map<String, Object> getData() {
        if (propertyDefaultBox != null) {
            Map<String, Object> data = propertyDefaultBox.serializeData();
            if (data == null)
                return null;
            return data;
        } else {
            return null;
        }
    }

    @Override
    public Actor getActor() {
        return this;
    }

    @Override
    public GraphBox<T> createPropertyBox(Skin skin, String id, float x, float y) {
        final String name = getName();
        GraphBoxImpl<T> result = new GraphBoxImpl<T>(id, new PropertyNodeConfiguration<T>(name, propertyType), skin) {
            @Override
            public HashMap<String, String> getData() {
                HashMap<String, String> result = new HashMap<>();
                result.put("name", name);
                result.put("type", propertyType.name());
                return result;
            }
        };
        result.addOutputGraphPart(skin, new ValueGraphNodeOutput<T>(name, propertyType));
        return result;
    }

    @Override
    public void dispose() {

    }
}
