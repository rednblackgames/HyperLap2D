package games.rednblack.editor.plugin.tiled.data;

public class AlternativeAutoTileVO {
	
	public String region = "";
	public Float percent = Float.valueOf(0.0f);
	
	public AlternativeAutoTileVO() {
	}
	
	public AlternativeAutoTileVO(String region, Float percent) {
		this.region = region;
		this.percent = percent;
	}
	
	public AlternativeAutoTileVO(String region, String percent) {
		this.region = region;
		this.percent = Float.valueOf(percent);
	}
	
}
