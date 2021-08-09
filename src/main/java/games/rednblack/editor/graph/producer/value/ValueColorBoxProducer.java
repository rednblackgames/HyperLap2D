package games.rednblack.editor.graph.producer.value;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.graph.*;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.ValueGraphBoxProducer;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.h2d.common.view.ui.widget.TintButton;

import java.util.Map;

public class ValueColorBoxProducer<T extends FieldType> extends ValueGraphBoxProducer<T> {
    public ValueColorBoxProducer(NodeConfiguration<T> configuration) {
        super(configuration);
    }

    @Override
    public GraphBox<T> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        String value = data.get("color");

        return createGraphBox(skin, id, value);
    }

    @Override
    public GraphBox<T> createDefault(Skin skin, String id) {
        return createGraphBox(skin, id, "FFFFFFFF");
    }

    private GraphBox<T> createGraphBox(Skin skin, String id, String value) {
        GraphBoxImpl<T> end = new GraphBoxImpl<T>(id, configuration, skin);
        end.addGraphBoxPart(createValuePart(skin, value));

        return end;
    }

    private GraphBoxPartImpl<T> createValuePart(Skin skin, String value) {
        Color color = Color.valueOf(value);

        TintButton tintButton = StandardWidgetsFactory.createTintButton();
        tintButton.setColorValue(color);

        ColorPicker picker = new HyperLapColorPicker(new ColorPickerAdapter() {
            @Override
            public void finished(Color newColor) {
                tintButton.setColorValue(newColor);
                tintButton.fire(new GraphChangedEvent(false, true));
            }

            @Override
            public void changed(Color newColor) {
                tintButton.setColorValue(newColor);
                tintButton.fire(new GraphChangedEvent(false, true));
            }
        });
        picker.setColor(color);

        tintButton.addListener(
                new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        //displaying picker with fade in animation
                        Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());
                    }
                });

        Table table = new Table();
        table.add(new Label("Color", skin)).growX();
        table.add(tintButton);
        table.row();

        GraphBoxPartImpl<T> colorPart = new GraphBoxPartImpl<T>(table,
                new GraphBoxPartImpl.Callback() {
                    @Override
                    public void serialize(Map<String, String> object) {
                        object.put("color", tintButton.getColorValue().toString());
                    }
                }) {
            @Override
            public void dispose() {
                picker.dispose();
            }
        };
        colorPart.setOutputConnector(GraphBoxOutputConnector.Side.Right, configuration.getNodeOutputs().get("value"));
        return colorPart;
    }

    @Override
    public boolean isUnique() {
        return false;
    }
}
