package games.rednblack.editor.plugin.tiled.view.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators.FloatValidator;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.AlternativeAutoTileVO;
import games.rednblack.editor.plugin.tiled.data.AutoTileVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * The dialog that handles the alternatives.
 * 
 * @author Jan-Thierry Wegener
 */
public class AlternativeAutoTileDialog extends UIDraggablePanel {

	public AutoTileVO openingAutoTileVO;
	
	private TiledPlugin tiledPlugin;
	
	public VisSelectBox<String>[] alternativeSelectBoxArray;
	public VisValidatableTextField[] alternativePercentTextFieldArray;
	
	public AlternativeAutoTileDialog(TiledPlugin tiledPlugin) {
		super("Setup alternatives");
		
		this.tiledPlugin = tiledPlugin;
	}
	
	/**
	 * Initiates the view of the dialog.
	 */
	public void initView() {
		clear();
		
		String[] allAutoTileVOArray = new String[tiledPlugin.dataToSave.getAutoTiles().size];
		int index = 0;
		// add a "none" to the drop down list
		allAutoTileVOArray[index++] = "";
		for (int i = 0; i < tiledPlugin.dataToSave.getAutoTiles().size; i++) {
			AutoTileVO next = tiledPlugin.dataToSave.getAutoTiles().get(i);
			if (!openingAutoTileVO.equals(next)) {
				allAutoTileVOArray[index++] = next.regionName;
			}
		}
		
		alternativeSelectBoxArray = new VisSelectBox[allAutoTileVOArray.length - 1];
		alternativePercentTextFieldArray = new VisValidatableTextField[allAutoTileVOArray.length + 0];

		VisTable table = new VisTable();
		table.row().padTop(20);
		table.add(getVisImageButton(openingAutoTileVO.regionName)).maxHeight(32);
		table.add(StandardWidgetsFactory.createLabel(openingAutoTileVO.regionName, Align.left)).padLeft(5).left();//.width(115);
		alternativePercentTextFieldArray[0] = StandardWidgetsFactory.createValidableTextField(new FloatValidator());
		alternativePercentTextFieldArray[0].setText(openingAutoTileVO.alternativeAutoTileList.get(0, new AlternativeAutoTileVO()).percent.toString());
		table.add(alternativePercentTextFieldArray[0]).padLeft(5).padRight(5).fillX().left();
		table.row();
		for (int i = 0; i < allAutoTileVOArray.length - 1; i++) {
			String region = openingAutoTileVO.alternativeAutoTileList.get(i + 1, new AlternativeAutoTileVO()).region;
			
			VisImageButton imgButton = getVisImageButton(region);
			table.add(imgButton).maxHeight(32);
	        alternativeSelectBoxArray[i] = StandardWidgetsFactory.createSelectBox(String.class);
	        alternativeSelectBoxArray[i].setItems(allAutoTileVOArray);
	        alternativeSelectBoxArray[i].setSelected(openingAutoTileVO.alternativeAutoTileList.get(i + 1, new AlternativeAutoTileVO()).region);
	        alternativeSelectBoxArray[i].addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					updateVisImageButton(imgButton, ((SelectBox<String>) actor).getSelected());
					pack();
				}
	        });
	        table.add(alternativeSelectBoxArray[i]).padLeft(5).left();
			
			alternativePercentTextFieldArray[i + 1] = StandardWidgetsFactory.createValidableTextField(new FloatValidator());
			alternativePercentTextFieldArray[i + 1].setText(openingAutoTileVO.alternativeAutoTileList.get(i + 1, new AlternativeAutoTileVO()).percent.toString());
			table.add(alternativePercentTextFieldArray[i + 1]).padLeft(5).padRight(5).fillX().left();
			
			table.row().padTop(5).padBottom(5);
		}

		VisTextButton saveButton = StandardWidgetsFactory.createTextButton("Save");
		saveButton.addListener(new ClickListener() {
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				tiledPlugin.facade.sendNotification(TiledPlugin.ACTION_SAVE_ALTERNATIVES_AUTO_TILE);
				hide();
			}
		});
		
		VisTextButton cancelButton = StandardWidgetsFactory.createTextButton("Cancel");
		cancelButton.addListener(new ClickListener() {
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				hide();
			}
			
		});
		
		row();
		add(table);
		row();
		add(saveButton).center().padLeft(5).padRight(5);
		add(cancelButton).center().padLeft(5).padRight(5);
		row();
        
        pack();
    }

	/**
	 * Returns a button with the given region as image.
	 * 
	 * @param regionName The name of the image.
	 * 
	 * @return An image button.
	 */
	private VisImageButton getVisImageButton(String regionName) {
		VisImageButton.VisImageButtonStyle imageBoxStyle = new VisImageButton.VisImageButtonStyle();
		Drawable tileDrawable = null;
		if (!"".equals(regionName)) {   
			tileDrawable = new TextureRegionDrawable(tiledPlugin.pluginRM.getTextureRegion(regionName, EntityFactory.IMAGE_TYPE));
		}
		imageBoxStyle.imageUp = tileDrawable;
        imageBoxStyle.imageDown = tileDrawable;
        imageBoxStyle.imageChecked = tileDrawable;
        imageBoxStyle.imageOver = tileDrawable;

        VisImageButton ct = new VisImageButton(imageBoxStyle);
		return ct;
	}

	/**
	 * Updates the given button with the given image.
	 * 
	 * @param button The button to update.
	 * @param regionName The name of the new image.
	 */
	private void updateVisImageButton(VisImageButton button, String regionName) {
		VisImageButton.VisImageButtonStyle imageBoxStyle = button.getStyle();
		Drawable tileDrawable = null;
		if (!"".equals(regionName)) {   
			tileDrawable = new TextureRegionDrawable(tiledPlugin.pluginRM.getTextureRegion(regionName, EntityFactory.IMAGE_TYPE));
		}
		imageBoxStyle.imageUp = tileDrawable;
        imageBoxStyle.imageDown = tileDrawable;
        imageBoxStyle.imageChecked = tileDrawable;
        imageBoxStyle.imageOver = tileDrawable;
        
        button.setStyle(imageBoxStyle);
	}

	/**
	 * Returns the currently set auto-tile.
	 * 
	 * @return The currently set auto-tile.
	 */
	public AutoTileVO getOpeningAutoTileVO() {
		return openingAutoTileVO;
	}

	/**
	 * Sets the current auto-tile.
	 * 
	 * @param openingAutoTileVO The new auto-tile.
	 */
	public void setOpeningAutoTileVO(AutoTileVO openingAutoTileVO) {
		this.openingAutoTileVO = openingAutoTileVO;
	}

}
