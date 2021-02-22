package games.rednblack.editor.plugin.performance;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.UIDraggablePanel;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class PerformancePanel extends UIDraggablePanel {

    private final VisTable mainTable;

    private VisLabel entitiesCount, memoryLabel, fpsLbl, glCalls, drawCalls, shaderSwitch, textureBind, vertexCount;

    private Engine engine;
    private final GLProfiler profiler;

    public PerformancePanel() {
        super("Performance");
        addCloseButton();

        mainTable = new VisTable();

        getContentTable().add(mainTable).left().width(250).pad(5);

        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();
    }

    public void initView() {
        mainTable.clear();

        entitiesCount = new VisLabel();
        fpsLbl = new VisLabel();
        memoryLabel = new VisLabel();
        glCalls = new VisLabel();
        drawCalls = new VisLabel();
        shaderSwitch = new VisLabel();
        textureBind = new VisLabel();
        vertexCount = new VisLabel();

        mainTable.add(new VisLabel("Entity count: ")).right();
        mainTable.add(entitiesCount).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("FPS: ")).right();
        mainTable.add(fpsLbl).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("Memory: ")).right();
        mainTable.add(memoryLabel).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("GL Calls: ")).right();
        mainTable.add(glCalls).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("Draw calls: ")).right();
        mainTable.add(drawCalls).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("Shader switches: ")).right();
        mainTable.add(shaderSwitch).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("Texture bindings: ")).right();
        mainTable.add(textureBind).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("Vertex count: ")).right();
        mainTable.add(vertexCount).left().padLeft(4);
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
        entitiesCount.setText(engine.getEntities().size());
        fpsLbl.setText(Gdx.graphics.getFramesPerSecond());
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMemory = memoryUsage.getUsed() / (1024 * 1024);
        long allocatedMemory = memoryUsage.getCommitted() / (1024 * 1024);
        memoryLabel.getText().clear();
        memoryLabel.getText().append(usedMemory);
        memoryLabel.getText().append(" of ");
        memoryLabel.getText().append(allocatedMemory);
        memoryLabel.getText().append(" MB");
        glCalls.setText(profiler.getCalls());
        drawCalls.setText(profiler.getDrawCalls());
        shaderSwitch.setText(profiler.getShaderSwitches());
        textureBind.setText(profiler.getTextureBindings());
        vertexCount.setText((int) profiler.getVertexCount().total);
    }


    public void render() {
        profiler.reset();
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}