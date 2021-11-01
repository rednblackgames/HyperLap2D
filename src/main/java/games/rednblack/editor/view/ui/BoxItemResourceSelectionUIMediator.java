package games.rednblack.editor.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.resourcespanel.UIImagesTab;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.BoxItemResource;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This mediator is part of the behavior that allows multiple images to be selected and dropped to other panels (like the GridTilesTab from the tiles plugin).
 * 
 * @author Jan-Thierry Wegener
 */
public class BoxItemResourceSelectionUIMediator extends Mediator<BoxItemResource>  {
	
	public static final String NAME = BoxItemResourceSelectionUIMediator.class.getCanonicalName();
	
	public final SortedSet<String> boxResourceSelectedSet = new TreeSet<>();

	/**
	 * The image table from {@link UIImagesTab}. This is the table we add our selection behavior to.
	 */
    private ObjectSet<VisTable> boxesTableSet = new ObjectSet<>();
    
    private BoxItemResource boxResourcePreviousClick;

    public BoxItemResourceSelectionUIMediator() {
        super(NAME);
    }
    
    @Override
    public void onRegister() {
    	super.onRegister();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
				UIResourcesBoxMediator.RESOURCE_BOX_RIGHT_CLICK,
        		UIResourcesBoxMediator.RESOURCE_BOX_LEFT_CLICK,
				UIResourcesBoxMediator.RESOURCE_BOX_DRAG_START,
        		MsgAPI.IMAGE_BUNDLE_DROP,
        		UIResourcesBoxMediator.ADD_RESOURCES_BOX_TABLE_SELECTION_MANAGEMENT,
        		UIResourcesBoxMediator.SANDBOX_DRAG_IMAGE_ENTER,
        		UIResourcesBoxMediator.SANDBOX_DRAG_IMAGE_EXIT
        };
    }

    @Override
    public void handleNotification(INotification notification) {
    	super.handleNotification(notification);

		BoxItemResource imageResource;

		switch (notification.getName()) {
	        case UIResourcesBoxMediator.RESOURCE_BOX_LEFT_CLICK:
				handleClickType(notification.getType(), notification.getBody());
	        	break;
			case UIResourcesBoxMediator.RESOURCE_BOX_RIGHT_CLICK:
				imageResource = notification.getBody();
				//Act as left-click when item is not already selected
				if (!boxResourceSelectedSet.contains(imageResource.getPayloadData().name)) {
					handleClickType(notification.getType(), imageResource);
				}
				break;
			case UIResourcesBoxMediator.RESOURCE_BOX_DRAG_START:
				imageResource = notification.getBody();
				//If the dragged resource hasn't been selected previously reset selection
				if (!boxResourceSelectedSet.contains(imageResource.getPayloadData().name)) {
					handleNormalClick(imageResource);
				}
				break;
	        case MsgAPI.IMAGE_BUNDLE_DROP:
	        	Set<String> nameSet = new HashSet<>(boxResourceSelectedSet);
	        	// remove the dropped one, so that it is not added twice
	        	Object[] payloadBody = notification.getBody();
	        	nameSet.remove(payloadBody[0]);
				Array<String> namesOrdered = new Array<>();
				namesOrdered.addAll(nameSet.toArray(String[]::new));
				namesOrdered.sort();
	        	for (String name : namesOrdered) {
	        		HyperLap2DFacade.getInstance().sendNotification(MsgAPI.IMAGE_BUNDLE_DROP_SINGLE, new Object[] {name, payloadBody[1], false});
	        	}
	        	break;
	        case UIResourcesBoxMediator.ADD_RESOURCES_BOX_TABLE_SELECTION_MANAGEMENT:
				boxesTableSet.add(notification.getBody());
	        	break;
	        case UIResourcesBoxMediator.SANDBOX_DRAG_IMAGE_ENTER:
	        	Source sourceEnter = notification.getBody();
	        	setColorExcept(new Color(0f, 0f, 0f, 0.3f), sourceEnter.getActor());
	        	break;
	        case UIResourcesBoxMediator.SANDBOX_DRAG_IMAGE_EXIT:
	        	setColorExcept(new Color(0f, 0f, 0f, 1f), null);
	        	break;
        }
    	
    }

    private void handleClickType(String type, BoxItemResource imageResource) {
		switch (type) {
			case UIResourcesBoxMediator.NORMAL_CLICK_EVENT_TYPE:
				handleNormalClick(imageResource);
				break;
			case UIResourcesBoxMediator.SHIFT_CTRL_EVENT_TYPE:
				handleShiftCtrlClick(imageResource);
				break;
			case UIResourcesBoxMediator.SHIFT_EVENT_TYPE:
				handleShiftClick(imageResource);
				break;
			case UIResourcesBoxMediator.CTRL_EVENT_TYPE:
				handleCtrlClick(imageResource);
				break;
		}

		boxResourcePreviousClick = imageResource;
	}
    
    /**
     * Darkens all the resources except the given one.
     * 
     * @param boxResource The dragged image resource.
     * @param color The color to set to all other resources.
     */
    private void setColorExcept(Color color, Actor boxResource) {
    	for (VisTable boxesTable : boxesTableSet) {
			for (Cell<BoxItemResource> cell : boxesTable.getCells()) {
				Actor imgResource = cell.getActor();
				if (!imgResource.equals(boxResource)) {
					imgResource.setColor(color);
				}
			}
		}
    }

    /**
     * Handles a shift-click on the panel. This means that the selection is cleared, and all resources between the
     * previously clicked and currently clicked resources are selected / unselected.
     * 
     * @param boxResource The clicked image resource.
     */
    private void handleShiftClick(BoxItemResource boxResource) {
		if (boxResourcePreviousClick == null) boxResourcePreviousClick = boxResource;

    	boolean removed = boxResourceSelectedSet.remove(boxResource.getPayloadData().name);

		clearSelection();
		
    	if (removed) {
    		setSelectionBetween(boxResourcePreviousClick, boxResource, false);
    	} else {
    		setSelectionBetween(boxResourcePreviousClick, boxResource, true);
    	}
    }

    /**
     * Handles a shift-ctrl-click on the panel. This means that the selection is kept, and all resources between the
     * previously clicked and currently clicked resources are selected / unselected.
     * 
     * @param boxResource The clicked image resource.
     */
    private void handleShiftCtrlClick(BoxItemResource boxResource) {
		if (boxResourcePreviousClick == null) boxResourcePreviousClick = boxResource;

    	if (boxResourceSelectedSet.remove(boxResource.getPayloadData().name)) {
    		setSelectionBetween(boxResourcePreviousClick, boxResource, false);
    	} else {
    		setSelectionBetween(boxResourcePreviousClick, boxResource, true);
    	}
    }

    /**
     * Handles a ctrl-click on the panel. This means that the selection is kept, and the currently clicked resource is selected / unselected.
     * 
     * @param boxResource The clicked image resource.
     */
	private void handleCtrlClick(BoxItemResource boxResource) {
    	if (boxResourceSelectedSet.remove(boxResource.getPayloadData().name)) {
    		// we unselected the cell
    		setSelected(boxResource, false);
    	} else {
    		// we selected the cell
    		setSelected(boxResource, true);
    	}
    }

    /**
     * Handles a click on the panel. This means that the selection is cleared, and the resource currently clicked resources are selected.
     * 
     * @param boxResource The clicked image resource.
     */
    private void handleNormalClick(BoxItemResource boxResource) {
		clearSelection();
		
		// we selected the cell
		setSelected(boxResource, true);
    }

    /**
     * Selects or unselets the given image resource.
     * 
     * @param boxResource The clicked image resource.
     * @param isSelected Whether to select (true) or unselect (false) the given resource.
     */
    private void setSelected(BoxItemResource boxResource, boolean isSelected) {
    	if (isSelected) {
			boxResource.switchToMouseOverColor();
			boxResource.setHighlightWhenMouseOver(false);
			boxResourceSelectedSet.add(boxResource.getPayloadData().name);
    	} else {
			boxResource.switchToStandardColor();
			boxResource.setHighlightWhenMouseOver(true);
			boxResourceSelectedSet.remove(boxResource.getPayloadData().name);
    	}
    }

    /**
     * Clears the selection.
     */
    private void clearSelection() {
		for (VisTable boxesTable : boxesTableSet) {
			for (Cell<BoxItemResource> cell : boxesTable.getCells()) {
				BoxItemResource imgResource = cell.getActor();
				setSelected(imgResource, false);
			}
		}
		boxResourceSelectedSet.clear();
    }
    
    private int getCellIndex(BoxItemResource boxResource, int defaultIndex, VisTable boxesTable) {
    	Cell<BoxItemResource> cell = boxesTable.getCell(boxResource);
    	int index = defaultIndex;
    	if (cell != null) {
    		// compute the indixes, should be faster than iterating over the array of cells
    		index = cell.getRow() * boxesTable.getColumns() + cell.getColumn();
    	}
    	return index;
    }

    private VisTable getBoxResourceTable(BoxItemResource boxResource) {
		for (VisTable boxesTable : boxesTableSet) {
			Cell<BoxItemResource> cell = boxesTable.getCell(boxResource);
			if (cell != null)
				return boxesTable;
		}
		return null;
	}

    private void setSelectionBetween(BoxItemResource boxResourceStart, BoxItemResource boxResourceEnd, boolean selected) {
		VisTable boxesTable = getBoxResourceTable(boxResourceStart);
		if (boxesTable == null) return;

		int startIndex = getCellIndex(boxResourceStart, 0, boxesTable);
		int endIndex = getCellIndex(boxResourceEnd, boxesTable.getCells().size - 1, boxesTable);

		// we want to start with start :)
		if (endIndex < startIndex) {
			int tmp = startIndex;
			startIndex = endIndex;
			endIndex = tmp;
		}

		for (int i = startIndex; i <= endIndex; i++) {
			Cell<BoxItemResource> cell = boxesTable.getCells().get(i);
			BoxItemResource boxResource = cell.getActor();
			setSelected(boxResource, selected);
		}
    }
    
}
