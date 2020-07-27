package games.rednblack.editor.plugin.skincomposer;

import java.util.Map;

public class SkinComposerVO {
    public boolean alwaysCheckUpdates = true;

    public void fromStorage(Map<String, Object> settings) {
        alwaysCheckUpdates = (boolean) settings.getOrDefault("alwaysCheckUpdates", true);
    }

    public void toStorage(Map<String, Object> settings) {
        settings.put("alwaysCheckUpdates", alwaysCheckUpdates);
    }
}
