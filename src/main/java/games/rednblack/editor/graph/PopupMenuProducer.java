package games.rednblack.editor.graph;

import games.rednblack.h2d.common.view.ui.widget.H2DPopupMenu;

public interface PopupMenuProducer {
    H2DPopupMenu createPopupMenu(float x, float y);
}
