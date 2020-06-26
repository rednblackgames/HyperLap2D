package games.rednblack.editor.renderer.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.utils.CustomVariables;

import java.util.HashSet;
import java.util.Set;

public class MainItemComponent implements Component {
    public int uniqueId = 0;
	public String itemIdentifier = "";
	public String libraryLink = "";
    public Set<String> tags = new HashSet<String>();
    private String customVars = "";
    public CustomVariables customVariables = new CustomVariables();
	public int entityType;
	public boolean visible = true;

	public void setCustomVars(String key, String value) {
		customVariables.setVariable(key, value);
		setCustomVarString(customVariables.saveAsString());
	}

	public void removeCustomVars(String key) {
		customVariables.removeVariable(key);
		setCustomVarString(customVariables.saveAsString());
	}

	public String getCustomVarString() {
		return customVars;
	}

	public void setCustomVarString(String vars) {
		customVars = vars;
		if (customVariables.getCount() == 0) {
			customVariables.loadFromString(customVars);
		}
	}
}
