package games.rednblack.editor.view.ui.panel;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.UIDraggablePanel;

public class ShaderUniformsPanel extends UIDraggablePanel {

    private final HyperLap2DFacade facade;

    public ShaderUniformsPanel() {
        super("Shader Uniforms");
        addCloseButton();

        facade = HyperLap2DFacade.getInstance();

        getContentTable().add("Test");
    }

    public void setEmpty() {
        getContentTable().add("Empty");
    }
}
