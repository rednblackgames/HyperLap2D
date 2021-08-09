package games.rednblack.editor.graph.actions.producer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.graph.GraphBoxImpl;
import games.rednblack.editor.graph.GraphBoxPartImpl;
import games.rednblack.editor.graph.GraphChangedEvent;
import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.data.GraphNodeInput;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.GraphBoxProducerImpl;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;

public class ArrayActionBoxProducer extends GraphBoxProducerImpl<ActionFieldType> {

    private Class<? extends NodeConfiguration<ActionFieldType>> configurationType;

    public ArrayActionBoxProducer(Class<? extends NodeConfiguration<ActionFieldType>> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        super(type.getDeclaredConstructor().newInstance());
        configurationType = type;
    }

    @Override
    public GraphBoxImpl<ActionFieldType> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        GraphBoxImpl<ActionFieldType> graphBox = null;
        int pins = Integer.parseInt(data.get("pins"));
        try {
            graphBox = createPipelineGraphBoxConfig(skin, id, configurationType.getDeclaredConstructor().newInstance());
            if (pins != graphBox.getInputs().size()) {
                for (int i = 0; i < pins - 2; i++) {
                    addPin(skin, graphBox);
                }
            }
            addPart(skin, graphBox);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return graphBox;
    }

    @Override
    public GraphBoxImpl<ActionFieldType> createDefault(Skin skin, String id) {
        Map<String, String> data = new HashMap<>();
        data.put("pins", String.valueOf(2));
        return createPipelineGraphBox(skin, id, data);
    }

    private void addPart(Skin skin, GraphBoxImpl<ActionFieldType> graphBox) {
        Map<String, GraphNodeInput<ActionFieldType>> inputs = graphBox.getConfiguration().getNodeInputs();
        VisTextButton addButton = StandardWidgetsFactory.createTextButton("+ Add Pin");
        VisTextButton removeButton = StandardWidgetsFactory.createTextButton("- Remove Pin");
        removeButton.setDisabled(inputs.size() <= 2);

        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                addPin(skin, graphBox);
                addButton.fire(new GraphChangedEvent(true, false));

                if (inputs.size() > 2) {
                    removeButton.setDisabled(false);
                }
            }
        });
        addButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });

        GraphBoxPartImpl<ActionFieldType> addPart = new GraphBoxPartImpl<>(addButton, null);
        graphBox.addHeaderGraphBoxPart(addPart);

        removeButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });
        removeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Map<String, GraphNodeInput<ActionFieldType>> inputs = graphBox.getConfiguration().getNodeInputs();
                GraphNodeInput<ActionFieldType> n = inputs.get("action" + (inputs.size() - 1));
                inputs.remove(n.getFieldId());
                graphBox.removeGraphBoxPart(inputs.size());
                graphBox.invalidate();
                addButton.fire(new GraphChangedEvent(true, false));

                if (inputs.size() == 2) {
                    removeButton.setDisabled(true);
                }
            }
        });

        GraphBoxPartImpl<ActionFieldType> removePart = new GraphBoxPartImpl<>(removeButton, null);
        graphBox.addFooterGraphBoxPart(removePart);

        graphBox.setSerializeCallback(object -> {
            object.put("pins", String.valueOf(inputs.size()));
        });
    }

    private void addPin(Skin skin, GraphBoxImpl<ActionFieldType> graphBox){
        Map<String, GraphNodeInput<ActionFieldType>> inputs = graphBox.getConfiguration().getNodeInputs();
        GraphNodeInputImpl<ActionFieldType> n = new GraphNodeInputImpl<>("action" + inputs.size(), "Action " + inputs.size(), true, Action);
        inputs.put(n.getFieldId(), n);
        graphBox.addInputGraphPart(skin, n);
        graphBox.invalidate();
    }
}
