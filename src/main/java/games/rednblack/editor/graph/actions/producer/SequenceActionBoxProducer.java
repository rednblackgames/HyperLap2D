package games.rednblack.editor.graph.actions.producer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.graph.GraphBoxImpl;
import games.rednblack.editor.graph.GraphBoxPartImpl;
import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.actions.config.SequenceActionNodeConfiguration;
import games.rednblack.editor.graph.data.GraphNodeInput;
import games.rednblack.editor.graph.producer.GraphBoxProducerImpl;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.json.simple.JSONObject;

import java.util.Map;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;

public class SequenceActionBoxProducer extends GraphBoxProducerImpl<ActionFieldType> {

    public SequenceActionBoxProducer() {
        super(new SequenceActionNodeConfiguration());
    }

    @Override
    public GraphBoxImpl<ActionFieldType> createPipelineGraphBox(Skin skin, String id, JSONObject data) {
        GraphBoxImpl<ActionFieldType> graphBox = createPipelineGraphBoxConfig(skin, id, new SequenceActionNodeConfiguration());
        addPart(skin, graphBox);
        return graphBox;
    }

    @Override
    public GraphBoxImpl<ActionFieldType> createDefault(Skin skin, String id) {
        return createPipelineGraphBox(skin, id, null);
    }

    private void addPart(Skin skin, GraphBoxImpl<ActionFieldType> graphBox) {
        VisTextButton addButton = StandardWidgetsFactory.createTextButton("+ Add");
        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Map<String, GraphNodeInput<ActionFieldType>> inputs = graphBox.getConfiguration().getNodeInputs();
                GraphNodeInputImpl<ActionFieldType> n = new GraphNodeInputImpl<>("action" + inputs.size(), "Action " + inputs.size(), true, Action);
                inputs.put(n.getFieldId(), n);
                graphBox.addInputGraphPart(skin, n);
                graphBox.invalidate();
            }
        });
        addButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });

        GraphBoxPartImpl<ActionFieldType> colorPart = new GraphBoxPartImpl<>(addButton, null);
        graphBox.addHeaderGraphBoxPart(colorPart);
    }
}
