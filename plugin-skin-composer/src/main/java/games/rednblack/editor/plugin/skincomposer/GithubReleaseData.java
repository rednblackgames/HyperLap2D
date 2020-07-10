package games.rednblack.editor.plugin.skincomposer;

import com.badlogic.gdx.utils.Array;

public class GithubReleaseData {
    public String tag_name;
    public String name;
    public Array<GithubReleaseAssetData> assets;

    public static class GithubReleaseAssetData {
        String name;
        String browser_download_url;
    }
}
