package games.rednblack.editor.view.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.resourcespanel.UIImagesTab;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.ImageResource;
import games.rednblack.h2d.common.MsgAPI;

/**
 * This mediator is part of the behavior that allows multiple images to be selected and dropped to other panels (like the GridTilesTab from the tiles plugin).
 * 
 * @author Jan-Thierry Wegener
 */
public class ImageResourceSelectionUIMediator extends Mediator<ImageResource>  {
	
	public static final String NAME = ImageResourceSelectionUIMediator.class.getCanonicalName();
	
	private final SortedSet<String> imageResourceSelectedSet = new TreeSet<>();

	/**
	 * The image table from {@link UIImagesTab}. This is the table we add our selection behavior to.
	 */
    private VisTable imagesTable;
    
    private ImageResource imageResourcePreviousClick;

    public ImageResourceSelectionUIMediator() {
        super(NAME);
    }
    
    @Override
    public void onRegister() {
    	super.onRegister();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
        		UIResourcesBoxMediator.IMAGE_LEFT_CLICK,
        		MsgAPI.IMAGE_BUNDLE_DROP,
        		UIResourcesBoxMediator.IMAGE_TABLE_UPDATED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
    	super.handleNotification(notification);
    	
        switch (notification.getName()) {
	        case UIResourcesBoxMediator.IMAGE_LEFT_CLICK:
	        	ImageResource imageResource = notification.getBody();
	        	switch (notification.getType()) {
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
	        	
	        	imageResourcePreviousClick = imageResource;
	        	break;
	        case MsgAPI.IMAGE_BUNDLE_DROP:
	        	Set<String> nameSet = new HashSet<>(imageResourceSelectedSet);
	        	// remove the dropped one, so that it is not added twice
	        	Object[] payloadBody = notification.getBody();
	        	nameSet.remove(payloadBody[0]);
	        	for (String name : nameSet) {
	        		HyperLap2DFacade.getInstance().sendNotification(MsgAPI.IMAGE_BUNDLE_DROP_SINGLE, new Object[] {name, payloadBody[1]});
	        	}
	        	break;
	        case UIResourcesBoxMediator.IMAGE_TABLE_UPDATED:
	        	imagesTable = notification.getBody();
	        	break;
	        default:
	        	System.err.println("Unknown notification: " + notification);
        }
    	
    }

    /**
     * Handles a shift-click on the panel. This means that the selection is cleared, and all resources between the
     * previously clicked and currently clicked resources are selected / unselected.
     * 
     * @param imageResource The clicked image resource.
     */
    private void handleShiftClick(ImageResource imageResource) {
    	boolean removed = imageResourceSelectedSet.remove(imageResource.getPayloadData().name);

		clearSelection();
		
    	if (removed) {
    		setSelectionBetween(imageResourcePreviousClick, imageResource, false);
    	} else {
    		setSelectionBetween(imageResourcePreviousClick, imageResource, true);
    	}
    }

    /**
     * Handles a shift-ctrl-click on the panel. This means that the selection is kept, and all resources between the
     * previously clicked and currently clicked resources are selected / unselected.
     * 
     * @param imageResource The clicked image resource.
     */
    private void handleShiftCtrlClick(ImageResource imageResource) {
    	if (imageResourceSelectedSet.remove(imageResource.getPayloadData().name)) {
    		setSelectionBetween(imageResourcePreviousClick, imageResource, false);
    	} else {
    		setSelectionBetween(imageResourcePreviousClick, imageResource, true);
    	}
    }

    /**
     * Handles a ctrl-click on the panel. This means that the selection is kept, and the currently clicked resource is selected / unselected.
     * 
     * @param imageResource The clicked image resource.
     */
	private void handleCtrlClick(ImageResource imageResource) {
    	if (imageResourceSelectedSet.remove(imageResource.getPayloadData().name)) {
    		// we unselected the cell
    		setSelected(imageResource, false);
    	} else {
    		// we selected the cell
    		setSelected(imageResource, true);
    	}
    }

    /**
     * Handles a click on the panel. This means that the selection is cleared, and the resource currently clicked resources are selected.
     * 
     * @param imageResource The clicked image resource.
     */
    private void handleNormalClick(ImageResource imageResource) {
		clearSelection();
		
		// we selected the cell
		setSelected(imageResource, true);
    }

    /**
     * Selects or unselets the given image resource.
     * 
     * @param imageResource The clicked image resource.
     * @param isSelected Whether to select (true) or unselect (false) the given resource.
     */
    private void setSelected(ImageResource imageResource, boolean isSelected) {
    	if (isSelected) {
			imageResource.switchToMouseOverColor();
    		imageResource.setHighlightWhenMouseOver(false);
			imageResourceSelectedSet.add(imageResource.getPayloadData().name);
    	} else {
    		imageResource.switchToStandardColor();
    		imageResource.setHighlightWhenMouseOver(true);
			imageResourceSelectedSet.remove(imageResource.getPayloadData().name);
    	}
    }

    /**
     * Clears the selection.
     */
    private void clearSelection() {
    	for (Cell<ImageResource> cell : imagesTable.getCells()) {
			ImageResource imgResource = cell.getActor();
			setSelected(imgResource, false);
		}
    }
    
    private int getCellIndex(ImageResource imageResource, int defaultIndex) {
    	Cell<ImageResource> cell = imagesTable.getCell(imageResource);
    	int index = defaultIndex;
    	if (cell != null) {
    		// compute the indixes, should be faster than iterating over the array of cells
    		index = cell.getRow() * imagesTable.getColumns() + cell.getColumn();
    	}
    	return index;
    }

    private void setSelectionBetween(ImageResource imageResourceStart, ImageResource imageResourceEnd, boolean selected) {
    	int startIndex = getCellIndex(imageResourceStart, 0);
    	int endIndex = getCellIndex(imageResourceEnd, imagesTable.getCells().size - 1);

    	// we want to start with start :)
    	if (endIndex < startIndex) {
    		int tmp = startIndex;
    		startIndex = endIndex;
    		endIndex = tmp;
    	}

    	for (int i = startIndex; i <= endIndex; i++) {
    		Cell<ImageResource> cell = imagesTable.getCells().get(i);
    		ImageResource imageResource = cell.getActor();
    		setSelected(imageResource, selected);
    	}
    }
    
}
