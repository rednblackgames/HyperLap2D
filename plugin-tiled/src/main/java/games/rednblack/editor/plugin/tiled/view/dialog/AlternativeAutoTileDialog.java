package games.rednblack.editor.plugin.tiled.view.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.util.Validators.FloatValidator;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.AlternativeAutoTileVO;
import games.rednblack.editor.plugin.tiled.data.AutoTileVO;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.extension.spine.SpineItemType;

/**
 * Lets the user pick, for one auto-tile, which other auto-tiles may be painted in its place and how
 * often. Alternatives are added by clicking a tile in the strip of remaining auto-tiles and removed
 * per row. Weights are relative; every row shows the share it currently amounts to, and the mediator
 * stores the normalised values on save.
 *
 * @author Jan-Thierry Wegener
 */
public class AlternativeAutoTileDialog extends H2DDialog {

	private static final int MIN_WIDTH = 440;
	private static final int PREVIEW_SIZE = 32;
	private static final int STRIP_TILE_SIZE = 40;
	private static final int STRIP_HEIGHT = STRIP_TILE_SIZE + 12;
	private static final int PERCENT_WIDTH = 40;

	public AutoTileVO openingAutoTileVO;

	private final TiledPlugin tiledPlugin;

	/** Weight of the tile itself, first entry of the stored list. */
	private VisValidatableTextField baseWeight;
	private VisLabel basePercent;
	private final Array<Row> rows = new Array<>();

	private VisTable listTable;
	private VisTable stripTable;
	private VisLabel stripHint;

	/** One chosen alternative: its region, the weight the user types, and the share it amounts to. */
	private static final class Row {
		final String region;
		final VisValidatableTextField weight;
		final VisLabel percent;

		Row(String region, VisValidatableTextField weight, VisLabel percent) {
			this.region = region;
			this.weight = weight;
			this.percent = percent;
		}
	}

	public AlternativeAutoTileDialog(TiledPlugin tiledPlugin) {
		super("Setup alternatives");
		this.tiledPlugin = tiledPlugin;
		addCloseButton();
		closeOnEscape();
	}

	/**
	 * Initiates the view of the dialog.
	 */
	public void initView() {
		getContentTable().clear();
		getButtonsTable().clear();
		rows.clear();

		VisTable body = new VisTable();
		getContentTable().add(body).growX();
		PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();

		// the tile the alternatives are set up for
		grid.section("Tile");
		AlternativeAutoTileVO base = openingAutoTileVO.alternativeAutoTileList.get(0, new AlternativeAutoTileVO());
		baseWeight = weightField(base.percent);
		basePercent = percentLabel();
		VisTable baseRow = new VisTable();
		baseRow.add(previewBox(openingAutoTileVO.regionName, openingAutoTileVO.entityType)).padRight(PropertyGrid.LABEL_GAP);
		baseRow.add(PropertyGrid.value(openingAutoTileVO.regionName)).left().growX();
		baseRow.add(PropertyGrid.label("Weight")).padRight(PropertyGrid.LABEL_GAP);
		baseRow.add(baseWeight).width(PropertyGrid.NUMBER_WIDTH).padRight(PropertyGrid.LABEL_GAP);
		baseRow.add(basePercent).width(PERCENT_WIDTH).right();
		grid.wideFill(baseRow);

		// alternatives already chosen, one row each
		grid.section("Alternatives");
		listTable = new VisTable();
		grid.wideFill(listTable);
		for (int i = 1; i < openingAutoTileVO.alternativeAutoTileList.size(); i++) {
			AlternativeAutoTileVO alternative = openingAutoTileVO.alternativeAutoTileList.get(i);
			if (alternative.region == null || alternative.region.isEmpty()) continue;
			if (findRow(alternative.region) != null) continue;
			rows.add(new Row(alternative.region, weightField(alternative.percent), percentLabel()));
		}

		// remaining auto-tiles: click one to add it
		grid.section("Add alternative");
		stripHint = PropertyGrid.text("");
		grid.wideFill(stripHint);
		stripTable = new VisTable();
		stripTable.left();
		VisScrollPane strip = StandardWidgetsFactory.createScrollPane(stripTable);
		strip.setScrollingDisabled(false, true);
		strip.setFadeScrollBars(false);
		grid.wideFill(strip, STRIP_HEIGHT);

		VisTextButton saveButton = StandardWidgetsFactory.createTextButton("Save", "accent");
		saveButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				tiledPlugin.facade.sendNotification(TiledPlugin.ACTION_SAVE_ALTERNATIVES_AUTO_TILE);
				hide();
			}
		});
		VisTextButton cancelButton = StandardWidgetsFactory.createTextButton("Cancel");
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hide();
			}
		});
		getButtonsTable().add(cancelButton).pad(2);
		getButtonsTable().add(saveButton).pad(2);
		getCell(getButtonsTable()).right();

		rebuild();
	}

	/**
	 * What the dialog currently holds, in the stored order: the tile itself first, then every chosen
	 * alternative with the weight as typed. Normalising is the mediator's job.
	 */
	public Array<AlternativeAutoTileVO> collectAlternatives() {
		Array<AlternativeAutoTileVO> result = new Array<>();
		result.add(new AlternativeAutoTileVO(openingAutoTileVO.regionName, weightOf(baseWeight)));
		for (Row row : rows) {
			result.add(new AlternativeAutoTileVO(row.region, weightOf(row.weight)));
		}
		return result;
	}

	@Override
	public float getPrefWidth() {
		return Math.max(super.getPrefWidth(), MIN_WIDTH);
	}

	/** Re-lays out the list and the strip after an add or remove, then refreshes the shares. */
	private void rebuild() {
		listTable.clear();
		if (rows.size == 0) {
			listTable.add(PropertyGrid.text("No alternatives yet, pick a tile below")).left().growX().row();
		}
		for (Row row : rows) {
			AutoTileVO tile = tiledPlugin.dataToSave.getAutoTile(row.region);
			int entityType = tile == null ? openingAutoTileVO.entityType : tile.entityType;

			VisImageButton remove = new VisImageButton("close-window");
			StandardWidgetsFactory.addTooltip(remove, "Remove this alternative");
			remove.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					rows.removeValue(row, true);
					rebuild();
				}
			});

			listTable.add(previewBox(row.region, entityType)).padRight(PropertyGrid.LABEL_GAP).padBottom(PropertyGrid.ROW_PAD);
			listTable.add(PropertyGrid.valueEllipsized(row.region)).left().growX().minWidth(0).padBottom(PropertyGrid.ROW_PAD);
			listTable.add(row.weight).width(PropertyGrid.NUMBER_WIDTH).padRight(PropertyGrid.LABEL_GAP).padBottom(PropertyGrid.ROW_PAD);
			listTable.add(row.percent).width(PERCENT_WIDTH).right().padRight(PropertyGrid.LABEL_GAP).padBottom(PropertyGrid.ROW_PAD);
			listTable.add(remove).padBottom(PropertyGrid.ROW_PAD);
			listTable.row();
		}

		stripTable.clear();
		int remaining = 0;
		NinePatchDrawable idle = new NinePatchDrawable(tiledPlugin.pluginRM.getPluginNinePatch("tile-box-inactive"));
		NinePatchDrawable over = new NinePatchDrawable(tiledPlugin.pluginRM.getPluginNinePatch("tile-box-over"));
		for (AutoTileVO tile : tiledPlugin.dataToSave.getAutoTiles()) {
			if (tile.regionName.equals(openingAutoTileVO.regionName) || findRow(tile.regionName) != null) continue;
			remaining++;

			VisImageButton.VisImageButtonStyle style = new VisImageButton.VisImageButtonStyle();
			style.up = idle;
			style.over = over;
			style.down = over;
			Drawable drawable = tileDrawable(tile.regionName, tile.entityType);
			style.imageUp = drawable;
			style.imageOver = drawable;
			style.imageDown = drawable;
			VisImageButton button = new VisImageButton(style);
			StandardWidgetsFactory.addTooltip(button, tile.regionName);
			button.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					rows.add(new Row(tile.regionName, weightField(1f), percentLabel()));
					rebuild();
				}
			});
			stripTable.add(button).size(STRIP_TILE_SIZE).pad(3);
		}
		stripHint.setText(remaining == 0
				? (rows.size == 0 ? "Add other auto-tiles to the Auto Tiling tab first" : "Every other auto-tile is already an alternative")
				: "Click a tile to add it as an alternative");

		updatePercents();
		pack();
	}

	/** Share of each entry, from the weights as typed: base plus every alternative sum to 100%. */
	private void updatePercents() {
		float total = Math.abs(weightOf(baseWeight));
		for (Row row : rows) total += Math.abs(weightOf(row.weight));

		if (total < 0.00001f) {
			// nothing weighs anything: the tile itself is what gets painted, like the mediator's rule
			basePercent.setText("100%");
			for (Row row : rows) row.percent.setText("0%");
			return;
		}
		basePercent.setText(percent(weightOf(baseWeight), total));
		for (Row row : rows) row.percent.setText(percent(weightOf(row.weight), total));
	}

	private static String percent(float weight, float total) {
		return Math.round(Math.abs(weight) / total * 100f) + "%";
	}

	private static float weightOf(VisValidatableTextField field) {
		try {
			return Float.parseFloat(field.getText());
		} catch (NumberFormatException e) {
			return 0f;
		}
	}

	private Row findRow(String region) {
		for (Row row : rows) {
			if (row.region.equals(region)) return row;
		}
		return null;
	}

	private VisValidatableTextField weightField(Float weight) {
		VisValidatableTextField field = StandardWidgetsFactory.createValidableTextField(new FloatValidator());
		field.setText(String.valueOf(weight == null ? 0f : weight));
		field.setTextFieldListener((textField, c) -> updatePercents());
		return field;
	}

	private static VisLabel percentLabel() {
		VisLabel label = PropertyGrid.value("");
		label.setAlignment(Align.right);
		return label;
	}

	/** Tile image inside a bordered slot, the same look as the Tiles panel. */
	private VisTable previewBox(String regionName, int entityType) {
		VisImage image = new VisImage();
		image.setScaling(Scaling.fit);
		image.setAlign(Align.center);
		image.setDrawable(tileDrawable(regionName, entityType));
		VisTable box = new VisTable();
		box.setBackground(new NinePatchDrawable(tiledPlugin.pluginRM.getPluginNinePatch("tile-box-inactive")));
		box.add(image).size(PREVIEW_SIZE).pad(2);
		return box;
	}

	private Drawable tileDrawable(String regionName, int entityType) {
		if (regionName == null || regionName.isEmpty()) return null;
		if (entityType == SpineItemType.SPINE_TYPE) {
			return tiledPlugin.pluginRM.getSpineDrawable(regionName);
		}
		return new TextureRegionDrawable(tiledPlugin.pluginRM.getTextureRegion(regionName, entityType));
	}

	public AutoTileVO getOpeningAutoTileVO() {
		return openingAutoTileVO;
	}

	public void setOpeningAutoTileVO(AutoTileVO openingAutoTileVO) {
		this.openingAutoTileVO = openingAutoTileVO;
	}
}
