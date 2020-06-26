package games.rednblack.editor.plugin.performance;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.puremvc.patterns.facade.SimpleFacade;
import games.rednblack.h2d.common.UIDraggablePanel;

public class PerformancePanel extends UIDraggablePanel {

    private SimpleFacade facade;

    private VisTable mainTable;

    private VisLabel entitiesCount;
    private VisLabel fpsLbl;

    private Engine engine;

    public PerformancePanel() {
        super("Performance");
        addCloseButton();

        facade = SimpleFacade.getInstance();

        mainTable = new VisTable();

        add(mainTable).width(222);
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

        mainTable.add(new VisLabel("no scenes open")).right();
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