package games.rednblack.editor.data.migrations.migrators;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.data.migrations.IVersionMigrator;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.h2d.common.vo.ProjectVO;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class VersionMigTo011 implements IVersionMigrator {

    private final Json json = new Json();
    private final JsonReader jsonReader = new JsonReader();

    private String projectPath;

    @Override
    public void setProject(String path, ProjectVO vo, ProjectInfoVO projectInfoVO) {
        projectPath = path;
        json.setOutputType(JsonWriter.OutputType.json);
    }

    @Override
    public boolean doMigration() {
        String prjInfoFilePath = projectPath + "/project.dt";
        File projectFile = new File(prjInfoFilePath);
        if (projectFile.exists()) {
            try {
                String content = FileUtils.readFileToString(projectFile, "utf-8");
                JsonValue value = jsonReader.parse(content);
                JsonValue oldActions = value.get("libraryActions");
                JsonValue newActions = new JsonValue(JsonValue.ValueType.object);
                newActions.name = "libraryActions";
                if (oldActions != null) {
                    for (JsonValue action : oldActions) {
                        String stringAction = action.prettyPrint(JsonWriter.OutputType.minimal, 0).replace("\"", "");
                        GraphVO graphVOAction = json.fromJson(GraphVO.class, stringAction.replace("\\", ""));
                        JsonValue newAction = jsonReader.parse(json.toJson(graphVOAction));;
                        newAction.name = action.name;
                        newActions.addChild(newAction);
                    }
                }
                setNewKeyToJson(value, "libraryActions", "libraryActions", newActions);
                content = value.prettyPrint(JsonWriter.OutputType.json, 1);
                FileUtils.writeStringToFile(projectFile, content, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void setNewKeyToJson(JsonValue container, String newKey, String oldKey, JsonValue newVal) {
        JsonValue oldVal = container.get(oldKey);
        if (oldVal == null)
            return;
        if(oldVal.prev != null) oldVal.prev.setNext(newVal);
        if(oldVal.next != null)  oldVal.next.setPrev(newVal);
        newVal.setPrev(oldVal.prev);
        newVal.setNext(oldVal.next);
        newVal.name = newKey;
    }
}
