package games.rednblack.editor.plugin.tiled.data;

import com.badlogic.gdx.math.Vector2;

import games.rednblack.editor.renderer.factory.EntityFactory;

public class AutoTileVO implements TextureRegionVO {

	public String regionName = "";
	public Vector2 gridOffset;
	public int entityType = EntityFactory.IMAGE_TYPE;

	/**
	 * The list of alternative auto-tiles.
	 */
	public final DefaultValueList<AlternativeAutoTileVO> alternativeAutoTileList = new DefaultValueList<>();

	public AutoTileVO() {
		gridOffset = new Vector2();
	}

	public AutoTileVO(String regionName) {
		this.regionName = regionName;
		gridOffset = new Vector2();
	}

	public AutoTileVO(String regionName, Vector2 offset) {
		this.regionName = regionName;
		this.gridOffset = offset;
	}

	@Override
	public String getRegionName() {
		return regionName;
	}

	@Override
	public int getEntityType() {
		return entityType;
	}

}
