package games.rednblack.editor.view.ui.box.resourcespanel.filter;

import games.rednblack.h2d.common.filters.IAbstractResourceFilter;

public class NormalMapFilter extends IAbstractResourceFilter {

    public NormalMapFilter() {
        super("Filter Normal Maps", "normal-maps");
    }

    @Override
    public boolean filterResource(String resName, int entityType) {
        return resName.endsWith(".normal");
    }
}
