package games.rednblack.editor.renderer.commons;

import com.badlogic.ashley.core.Entity;

public abstract class RefreshableObject {
    protected boolean needsRefresh = false;

    public void scheduleRefresh() {
        needsRefresh = true;
    }

    public void executeRefresh(Entity entity) {
        if (needsRefresh) {
            refresh(entity);
            needsRefresh = false;
        }
    }

    protected abstract void refresh(Entity entity);
}
