package games.rednblack.editor.plugin.performance;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.UIDraggablePanel;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

public class PerformancePanel extends UIDraggablePanel {

    private VisTable mainTable;

    private VisLabel entitiesCount;
    private VisLabel fpsLbl;
    private VisLabel memoryLabel;

    private Engine engine;
    private long time;

    public PerformancePanel() {
        super("Performance");
        addCloseButton();

        mainTable = new VisTable();

        getContentTable().add(mainTable).left().width(250).pad(5);
    }

    public void initView() {
        mainTable.clear();

        entitiesCount = new VisLabel();
        fpsLbl = new VisLabel();
        memoryLabel = new VisLabel();

        mainTable.add(new VisLabel("Entity count: ")).right();
        mainTable.add(entitiesCount).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("FPS: ")).right();
        mainTable.add(fpsLbl).left().padLeft(4);
        mainTable.row();

        mainTable.add(new VisLabel("Memory: ")).right();
        mainTable.add(memoryLabel).left().padLeft(4);
        mainTable.row();
        pack();

        time = TimeUtils.millis();
    }

    public void initLockView() {
        mainTable.clear();

        mainTable.add(new VisLabel("No project open")).right();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TimeUtils.timeSinceMillis(time) > 1000) {
            entitiesCount.setText(engine.getEntities().size() + "");
            fpsLbl.setText(Gdx.graphics.getFramesPerSecond() + "");
            MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            long usedMemory = memoryUsage.getUsed();
            long allocatedMemory = memoryUsage.getCommitted();
            memoryLabel.setText(getFileSizeString(usedMemory) + " of " + getFileSizeString(allocatedMemory));
        }
    }

    public static final long KB = 1024;
    public static final long MB = 1024 * KB;
    public static final long GB = 1024 * MB;
    public static final long PB = 1024 * GB;

    DecimalFormat df = new DecimalFormat("#");

    public String getFileSizeString(long size) {
        int digits = getDigits(size);
        df.applyPattern(digits == 0 ? "#" : "#." + getDigits(digits));
        if (size < KB) {
            return df.format(size) + " " + "KB";
        } else if (size < MB) {
            return df.format((float) size / KB) + " " + "KB";
        } else if (size < GB) {
            return df.format((float) size / MB) + " " + "MB";
        } else {
            return df.format((float) size / GB) + " " + "GB";
        }
    }

    private int getDigits(long size) {
        return size < GB ? 0 : 2;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}