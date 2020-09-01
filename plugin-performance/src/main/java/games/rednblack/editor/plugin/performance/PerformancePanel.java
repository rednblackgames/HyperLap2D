package games.rednblack.editor.plugin.performance;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.UIDraggablePanel;

public class PerformancePanel extends UIDraggablePanel {

    private VisTable mainTable;

    private VisLabel entitiesCount;
    private VisLabel fpsLbl;

    private Engine engine;

    public PerformancePanel() {
        super("Performance");
        addCloseButton();

        mainTable = new VisTable();

        getContentTable().add(mainTable).left().width(150).pad(5);
    }

    public void initView() {
        mainTable.clear();

        entitiesCount = new VisLabel();
        fpsLbl = new VisLabel();


        mainTable.add(new VisLabel("Entity count: ")).right();
        mainTable.add(entitiesCount).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("FPS: ")).right();
        mainTable.add(fpsLbl).left().padLeft(4);
        mainTable.row();
        pack();
    }

    public void initLockView() {
        mainTable.clear();

        mainTable.add(new VisLabel("No project open")).right();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(entitiesCount != null && fpsLbl != null) {
            entitiesCount.setText(engine.getEntities().size() + "");
            fpsLbl.setText(Gdx.graphics.getFramesPerSecond() + "");
        }
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}