package games.rednblack.editor.graph.actions.producer;

import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.data.NodeConfiguration;
import games.rednblack.editor.graph.producer.GraphBoxProducerImpl;

public class EntityBoxProducer extends GraphBoxProducerImpl<ActionFieldType> {

    public EntityBoxProducer(NodeConfiguration<ActionFieldType> configuration) {
        super(configuration);
    }
}
