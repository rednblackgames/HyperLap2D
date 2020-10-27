package games.rednblack.h2d.common.network.model;

import com.badlogic.gdx.utils.Array;

public class GithubReleaseData {
    public String tag_name;
    public String name;
    public Array<GithubReleaseAssetData> assets;

    public static class GithubReleaseAssetData {
        public String name;
        public String browser_download_url;
    }
}
