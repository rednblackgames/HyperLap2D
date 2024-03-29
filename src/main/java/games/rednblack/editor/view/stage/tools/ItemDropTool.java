package games.rednblack.editor.view.stage.tools;

import games.rednblack.editor.utils.runtime.EntityUtils;

/**
 * Created by CyberJoe on 6/24/2015.
 */
public abstract class ItemDropTool extends SelectionTool {

    public ItemDropTool() {

    }

    @Override
    public boolean stageMouseDown(float x, float y) {
        int entity = putItem(x, y);

        return false;
    }

    @Override
    public void stageMouseUp(float x, float y) {

    }

    @Override
    public boolean itemMouseDown(int entity, float x, float y) {
        if(checkFilter(entity)) {
            return super.itemMouseDown(entity, x, y);
        } else {
            stageMouseDown(x, y);
        }

        return false;
    }

    @Override
    public void itemMouseDragged(int entity, float x, float y) {
        if(checkFilter(entity)) {
            super.itemMouseDragged(entity, x, y);
        }

    }

    @Override
    public void itemMouseUp(int entity, float x, float y) {
        if(checkFilter(entity)) {
            super.itemMouseUp(entity, x, y);
        }
    }

    protected boolean checkFilter(int entity) {
        int[] itemTypes = listItemFilters();
        for(int i = 0; i < itemTypes.length; i++) {
            if(itemTypes[i] == EntityUtils.getType(entity)) {
                return true;
            }
        }

        return false;
    }

    public abstract int putItem(float x, float y);

    public abstract int[] listItemFilters();
}
