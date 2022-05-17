package games.rednblack.editor.plugin.tiled.view.dialog;

import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.AlternativeAutoTileVO;
import games.rednblack.editor.plugin.tiled.data.AutoTileVO;

/**
 * The mediator for messages for the alternatives.
 * 
 * @author Jan-Thierry Wegener
 */
public class AlternativeAutoTileDialogMediator extends Mediator<AlternativeAutoTileDialog> {

	public static final String NAME = AlternativeAutoTileDialogMediator.class.getName();

	private TiledPlugin tiledPlugin;

    public AlternativeAutoTileDialogMediator(TiledPlugin tiledPlugin) {
        super(NAME, new AlternativeAutoTileDialog(tiledPlugin));
        this.tiledPlugin = tiledPlugin;
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[] {
                TiledPlugin.ACTION_SETUP_ALTERNATIVES_AUTO_TILE,
                TiledPlugin.ACTION_SAVE_ALTERNATIVES_AUTO_TILE,
                TiledPlugin.ACTION_RECALC_PERCENT_ALTERNATIVES_AUTO_TILE
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case TiledPlugin.ACTION_SETUP_ALTERNATIVES_AUTO_TILE:
            	AutoTileVO autoTileVO = tiledPlugin.dataToSave.getAutoTile(notification.getBody());
                viewComponent.setOpeningAutoTileVO(autoTileVO);
                viewComponent.initView();
                viewComponent.show(tiledPlugin.getAPI().getUIStage());
            	break;
            case TiledPlugin.ACTION_SAVE_ALTERNATIVES_AUTO_TILE:
				viewComponent.openingAutoTileVO.alternativeAutoTileList.clear();
				
				AlternativeAutoTileVO alternativeAutoTileVO = new AlternativeAutoTileVO(viewComponent.openingAutoTileVO.regionName, viewComponent.alternativePercentTextFieldArray[0].getText());
				viewComponent.openingAutoTileVO.alternativeAutoTileList.add(alternativeAutoTileVO);
				for (int i = 0; i < viewComponent.alternativeSelectBoxArray.length; i++) {
					String region = viewComponent.alternativeSelectBoxArray[i].getSelected();
					Float percent = Float.valueOf(viewComponent.alternativePercentTextFieldArray[i + 1].getText());
					viewComponent.openingAutoTileVO.alternativeAutoTileList.add(new AlternativeAutoTileVO(region, percent));
				}

				tiledPlugin.facade.sendNotification(TiledPlugin.ACTION_RECALC_PERCENT_ALTERNATIVES_AUTO_TILE, viewComponent.openingAutoTileVO.regionName);
            	break;
            case TiledPlugin.ACTION_RECALC_PERCENT_ALTERNATIVES_AUTO_TILE:
            	String deletedAutoTileRegion = notification.getBody();
            	if (deletedAutoTileRegion == null) {
	            	for (AutoTileVO at : tiledPlugin.dataToSave.getAutoTiles()) {
	                	recalculatePercent(at);
	            	}
            	} else {
            		recalculatePercent(tiledPlugin.dataToSave.getAutoTile(deletedAutoTileRegion));
            	}

				tiledPlugin.saveDataManager.save();
            	break;
        }
    }

    /**
     * Recalculates the probabilities of the alternatives of all or the given auto-tile.
     * 
     * @param at The auto-tile to recalculate, or if <code>null</code> then all auto-tiles.
     */
    private void recalculatePercent(AutoTileVO at) {
		if (at.alternativeAutoTileList.size() == 0) return;

    	float total = 0;
    	for (AlternativeAutoTileVO aat : at.alternativeAutoTileList) {
			if (!"".equals(aat.region)) {
				total += Math.abs(aat.percent);
			}
    	}
    	
    	if (total < 0.00001f) {
    		// kind of 0
    		at.alternativeAutoTileList.get(0).percent = 1f;
    	} else {
			for (AlternativeAutoTileVO aat : at.alternativeAutoTileList) {
				if ("".equals(aat.region)) {
					aat.percent = 0f;
				} else {
					aat.percent = Math.abs(aat.percent) / total;
				}
			}
    	}
    }

}
