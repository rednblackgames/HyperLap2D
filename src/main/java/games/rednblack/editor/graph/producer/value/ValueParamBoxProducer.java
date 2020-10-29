package games.rednblack.editor.graph.producer.value;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.graph.*;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.ValueGraphBoxProducer;
import games.rednblack.editor.view.ui.validator.StringNameValidator;

import java.util.Map;

public class ValueParamBoxProducer<T extends FieldType> extends ValueGraphBoxProducer<T> {
    public ValueParamBoxProducer(NodeConfiguration<T> configuration) {
        super(configuration);
    }

    @Override
    public GraphBox<T> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        String name = data.get("v");

        return createGraphBox(skin, id, name);
    }

    @Override
    public GraphBox<T> createDefault(Skin skin, String id) {
        return createGraphBox(skin, id, "");
    }

    private GraphBox<T> createGraphBox(Skin skin, String id, String name) {
        GraphBoxImpl<T> end = new GraphBoxImpl<T>(id, configuration, skin);
        end.addGraphBoxPart(createValuePart(skin, name));

        return end;
    }

    private GraphBoxPartImpl<T> createValuePart(Skin skin, String name) {
        final VisValidatableTextField v1Input = new VisValidatableTextField(new StringNameValidator()) {
            @Override
            public float getPrefWidth() {
                return 80;
            }
        };
        v1Input.setText(name);
        v1Input.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        v1Input.fire(new GraphChangedEvent(false, true));
                    }
                });

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(new Label("Name: ", skin));
        horizontalGroup.addActor(v1Input);

        GraphBoxPartImpl<T> colorPart = new GraphBoxPartImpl<T>(horizontalGroup,
                new GraphBoxPartImpl.Callback() {
                    @Override
                    public void serialize(Map<String, String> object) {
                        object.put("v", v1Input.getText());
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
