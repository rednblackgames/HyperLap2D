package games.rednblack.editor.view.stage;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

/**
 * Owns the editor grid + lock-line settings, extracted from {@link Sandbox}
 * (Phase 2 decomposition). Reads/writes {@code ProjectManager.currentProjectVO}
 * (persisting on change) and broadcasts the {@code GRID_SIZE_CHANGED} /
 * {@code LOCK_LINES_CHANGED} notifications. {@code Sandbox} holds an instance
 * and delegates to it.
 */
public class GridService {

    private final ProjectManager projectManager;
    private final Facade facade;

    public GridService(ProjectManager projectManager, Facade facade) {
        this.projectManager = projectManager;
        this.facade = facade;
    }

    public float getGridSize() {
        return projectManager.currentProjectVO.gridSize;
    }

    public void setGridSize(float gridSize) {
        projectManager.currentProjectVO.gridSize = gridSize;
        projectManager.saveCurrentProject();
        facade.sendNotification(MsgAPI.GRID_SIZE_CHANGED, gridSize);
    }

    public boolean getLockLines() {
        return projectManager.currentProjectVO.lockLines;
    }

    public void setLockLines(boolean lockLines) {
        projectManager.currentProjectVO.lockLines = lockLines;
        projectManager.saveCurrentProject();
        facade.sendNotification(MsgAPI.LOCK_LINES_CHANGED, lockLines);
    }
}