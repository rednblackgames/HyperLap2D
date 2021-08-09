package games.rednblack.editor.graph.producer.value;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.graph.*;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.ValueGraphBoxProducer;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.Map;

public class ValueBooleanBoxProducer<T extends FieldType> extends ValueGraphBoxProducer<T> {
    public ValueBooleanBoxProducer(NodeConfiguration<T> configuration) {
        super(configuration);
    }

    @Override
    public GraphBox<T> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        boolean v = data.get("v") != null;

        return createGraphBox(skin, id, v);
    }

    @Override
    public GraphBox<T> createDefault(Skin skin, String id) {
        return createGraphBox(skin, id, false);
    }

    private GraphBox<T> createGraphBox(Skin skin, String id, boolean v) {
        GraphBoxImpl<T> end = new GraphBoxImpl<T>(id, configuration, skin);
        end.addGraphBoxPart(createValuePart(skin, v));

        return end;
    }

    private GraphBoxPartImpl<T> createValuePart(Skin skin, boolean v) {
        HorizontalGroup horizontalGroup = new HorizontalGroup();
        final VisCheckBox checkBox = StandardWidgetsFactory.createCheckBox("Value");
        checkBox.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        checkBox.fire(new GraphChangedEvent(false, true));
                    }
                });
        checkBox.setChecked(v);
        horizontalGroup.addActor(checkBox);

        GraphBoxPartImpl<T> colorPart = new GraphBoxPartImpl<T>(horizontalGroup,
                new GraphBoxPartImpl.Callback() {
                    @Override
                    public void serialize(Map<String, String> object) {
                        if (checkBox.isChecked())
                            object.put("v", "t");
                        else
                            object.remove("v");
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
