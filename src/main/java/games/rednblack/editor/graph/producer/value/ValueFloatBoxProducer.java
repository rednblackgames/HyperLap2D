package games.rednblack.editor.graph.producer.value;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.graph.*;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.ValueGraphBoxProducer;

import java.util.Map;

public class ValueFloatBoxProducer<T extends FieldType> extends ValueGraphBoxProducer<T> {
    public ValueFloatBoxProducer(NodeConfiguration<T> configuration) {
        super(configuration);
    }

    @Override
    public GraphBox<T> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        float v1 = Float.parseFloat(data.get("v1"));

        return createGraphBox(skin, id, v1);
    }

    @Override
    public GraphBox<T> createDefault(Skin skin, String id) {
        return createGraphBox(skin, id, 0);
    }

    private GraphBox<T> createGraphBox(Skin skin, String id, float v1) {
        GraphBoxImpl<T> end = new GraphBoxImpl<T>(id, configuration, skin);
        end.addGraphBoxPart(createValuePart(skin, v1));

        return end;
    }

    private GraphBoxPartImpl<T> createValuePart(Skin skin, float v1) {
        final VisValidatableTextField v1Input = new VisValidatableTextField(Validators.FLOATS) {
            @Override
            public float getPrefWidth() {
                return 80;
            }
        };
        v1Input.setText(String.valueOf(v1));
        v1Input.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        v1Input.fire(new GraphChangedEvent(false, true));
                    }
                });

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(new Label("Value: ", skin));
        horizontalGroup.addActor(v1Input);

        GraphBoxPartImpl<T> colorPart = new GraphBoxPartImpl<T>(horizontalGroup,
                new GraphBoxPartImpl.Callback() {
                    @Override
                    public void serialize(Map<String, String> object) {
                        object.put("v1", v1Input.getText());
                    }
                });
        colorPart.setOutputConnector(GraphBoxOutputConnector.Side.Right, configuration.getNodeOutputs().get("value"));
        return colorPart;
    }

    @Override
    public boolean isUnique() {
        return false;
    }
}
