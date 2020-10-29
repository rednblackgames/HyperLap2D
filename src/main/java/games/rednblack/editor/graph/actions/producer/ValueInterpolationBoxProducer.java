package games.rednblack.editor.graph.actions.producer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.graph.GraphBox;
import games.rednblack.editor.graph.GraphBoxImpl;
import games.rednblack.editor.graph.GraphBoxOutputConnector;
import games.rednblack.editor.graph.GraphBoxPartImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.ValueGraphBoxProducer;
import games.rednblack.editor.renderer.utils.InterpolationMap;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.Map;

public class ValueInterpolationBoxProducer extends ValueGraphBoxProducer<ActionFieldType> {

    public ValueInterpolationBoxProducer(NodeConfiguration<ActionFieldType> configuration) {
        super(configuration);
    }

    @Override
    public GraphBox<ActionFieldType> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        String name = data.get("interpolation");

        return createGraphBox(skin, id, name);
    }

    @Override
    public GraphBox<ActionFieldType> createDefault(Skin skin, String id) {
        return createGraphBox(skin, id, "linear");
    }

    private GraphBox<ActionFieldType> createGraphBox(Skin skin, String id, String interpolation) {
        GraphBoxImpl<ActionFieldType> end = new GraphBoxImpl<>(id, configuration, skin);
        end.addGraphBoxPart(createValuePart(skin, interpolation));

        return end;
    }

    private GraphBoxPartImpl<ActionFieldType> createValuePart(Skin skin, String interpolation) {
        VisSelectBox<String> selectBox = StandardWidgetsFactory.createSelectBox(String.class);
        Array<String> names = new Array<>();
        InterpolationMap.map.keySet().forEach(names::add);
        selectBox.setItems(names);
        selectBox.setSelected(interpolation);

        GraphBoxPartImpl<ActionFieldType> addPart = new GraphBoxPartImpl<>(selectBox, new GraphBoxPartImpl.Callback() {
            @Override
            public void serialize(Map<String, String> object) {
                object.put("interpolation", selectBox.getSelected());
            }
        });

        addPart.setOutputConnector(GraphBoxOutputConnector.Side.Right, configuration.getNodeOutputs().get("value"));
        return addPart;
    }

    @Override
    public boolean isUnique() {
        return false;
    }
}
