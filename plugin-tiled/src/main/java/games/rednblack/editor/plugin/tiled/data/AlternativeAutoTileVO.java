package games.rednblack.editor.plugin.tiled.data;

/**
 * This class represents an alternative to an auto-tile. It contains a region to paint when selected and a probabilty to be selected.
 * 
 * @author Jan-Thierry Wegener
 */
public class AlternativeAutoTileVO {
	
	/**
	 * The region to paint when selected.
	 */
	public String region = "";
	/**
	 * The probability to be selected by the auto-tile drawing strategy.
	 */
	public Float percent = Float.valueOf(0.0f);
	
	public AlternativeAutoTileVO() {
	}

	/**
	 * Creates a new alternative from the given region and probability.
	 * 
	 * @param region The region to paint.
	 * @param percent The probability to be selected.
	 */
	public AlternativeAutoTileVO(String region, Float percent) {
		this.region = region;
		this.percent = percent;
	}

	/**
	 * Creates a new alternative from the given region and probability.
	 * 
	 * @param region The region to paint.
	 * @param percent The probability to be selected.
	 */
	public AlternativeAutoTileVO(String region, String percent) {
		this.region = region;
		this.percent = Float.valueOf(percent);
	}

}
