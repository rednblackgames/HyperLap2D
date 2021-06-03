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
        		UIResourcesBoxMediator.IMAGE_BUNDLE_DROP,
        		UIResourcesBoxMediator.IMAGE_TABLE_UPDATED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
    	super.handleNotification(notification);
    	
        switch (notification.getName()) {
	        case UIResourcesBoxMediator.IMAGE_LEFT_CLICK:
	        	ImageResource imageResource = notification.getBody();
	        	if (imageResourceSelectedSet.remove(imageResource.getPayloadData().name)) {
	        		setSelected(imageResource, false);
	        		if (UIResourcesBoxMediator.SHIFT_EVENT_TYPE.equals(notification.getType())) {
	        			removeBetween(imageResourcePreviousClick, imageResource);
	        		}
	        	} else {
	        		setSelected(imageResource, true);
	        		if (UIResourcesBoxMediator.SHIFT_EVENT_TYPE.equals(notification.getType())) {
	        			addSelectionBetween(imageResourcePreviousClick, imageResource);
	        		}
	        	}
	        	imageResourcePreviousClick = imageResource;
	        	break;
	        case UIResourcesBoxMediator.IMAGE_BUNDLE_DROP:
	        	Set<String> nameSet = new HashSet<>(imageResourceSelectedSet);
	        	// remove the dropped one, so that it is not added twice
	        	Object[] payloadBody = notification.getBody();
	        	nameSet.remove(payloadBody[0]);
	        	for (String name : nameSet) {
	        		HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.IMAGE_BUNDLE_DROP_SINGLE, new Object[] {name, payloadBody[1]});
	        	}
	        	break;
	        case UIResourcesBoxMediator.IMAGE_TABLE_UPDATED:
	        	imagesTable = notification.getBody();
	        	break;
	        default:
	        	System.err.println("Unknown notification: " + notification);
        }
    	
    }
    
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
    
    private int getCellIndex(ImageResource imageResource, int defaultIndex) {
    	Cell<ImageResource> cell = imagesTable.getCell(imageResource);
    	int index = defaultIndex;
    	if (cell != null) {
    		// compute the indixes, should be faster than iterating over the array of cells
    		index = cell.getRow() * imagesTable.getColumns() + cell.getColumn();
    	}
    	return index;
    }

	private void addSelectionBetween(ImageResource imageResourceStart, ImageResource imageResourceEnd) {
		int startIndex = getCellIndex(imageResourceStart, 0);
		int endIndex = getCellIndex(imageResourceEnd, imagesTable.getCells().size - 1);
		
		// we want to start with start :)
		if (endIndex < startIndex) {
			int tmp = startIndex;
			startIndex = endIndex;
			// add one to include the previously clicked image
			endIndex = tmp;
		}
		
		for (int i = startIndex; i <= endIndex; i++) {
			Cell<ImageResource> cell = imagesTable.getCells().get(i);
			ImageResource imageResource = cell.getActor();
			setSelected(imageResource, true);
		}
	}

	private void removeBetween(ImageResource imageResourceStart, ImageResource imageResourceEnd) {
		int startIndex = getCellIndex(imageResourceStart, 0);
		int endIndex = getCellIndex(imageResourceEnd, imagesTable.getCells().size - 1);
		
		// we want to start with start :)
		if (endIndex < startIndex) {
			int tmp = startIndex;
			startIndex = endIndex;
			// add one to include the previously clicked image
			endIndex = tmp;
		}
		
		for (int i = startIndex; i <= endIndex; i++) {
			Cell<ImageResource> cell = imagesTable.getCells().get(i);
			ImageResource imageResource = cell.getActor();
			setSelected(imageResource, false);
		}
	}
    
    
    
}
