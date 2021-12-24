/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.data.migrations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.data.migrations.migrators.*;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.h2d.common.vo.ProjectVO;
import org.apache.commons.io.FileUtils;

import java.io.IOException;

/**
 * Created by azakhary on 9/28/2014.
 */
public class ProjectVersionMigrator {

	private String projectPath;
	private ProjectVO projectVo;
	private ProjectInfoVO projectInfoVO;

	private int safetyIterator = 0;

	/**
	 * this is the current supported version, change when data format is changed, and add migration script
	 */
	public static String dataFormatVersion = "1.0.0";

	private final Json json = HyperJson.getJson();

	public ProjectVersionMigrator (String projectPath, ProjectVO projectVo) {
		this.projectPath = projectPath;
		this.projectVo = projectVo;
		String prjInfoFilePath = projectPath + "/project.dt";
		FileHandle projectInfoFile = Gdx.files.internal(prjInfoFilePath);
		String projectInfoContents = "{}";
		try {
			projectInfoContents = FileUtils.readFileToString(projectInfoFile.file(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!projectVo.projectVersion.equals("0.2.0"))
			projectInfoVO = json.fromJson(ProjectInfoVO.class, projectInfoContents);

		json.setOutputType(JsonWriter.OutputType.json);
	}

	public void start () {
		if (projectVo.projectVersion == null || projectVo.projectVersion.equals("")) {
			projectVo.projectVersion = "0.0.4";
		}

		migrationIterator();
	}

	private void migrationIterator () {
		if (projectVo.projectVersion.equals(dataFormatVersion)) return;

		if (safetyIterator > 100) {
			System.out.println("Emergency exit from version migration process due to safety lock");
			return;
		}
		safetyIterator++;

		if (projectVo.projectVersion.equals("0.0.4")) {
			VersionMigTo005 vmt = new VersionMigTo005();
			doMigration(vmt, "0.0.5");
		}
		if (projectVo.projectVersion.equals("0.0.5") || projectVo.projectVersion.equals("0.0.6") || projectVo.projectVersion.equals("0.0.7")) {
			DummyMig vmt = new DummyMig();
			doMigration(vmt, "0.0.8");
		}
		if (projectVo.projectVersion.equals("0.0.8")) {
			VersionMigTo009 vmt = new VersionMigTo009();
			doMigration(vmt, "0.0.9");
		}
		if (projectVo.projectVersion.equals("0.0.9")) {
			IVersionMigrator vmt = new DummyMig();
			doMigration(vmt, "0.1");
		}
		if (projectVo.projectVersion.equals("0.1")) {
			IVersionMigrator vmt = new VersionMigTo011();
			doMigration(vmt, "0.1.1");
		}
		if (projectVo.projectVersion.equals("0.1.1")) {
			IVersionMigrator vmt = new VersionMigTo020();
			doMigration(vmt, "0.2.0");
		}
		if (projectVo.projectVersion.equals("0.2.0")) {
			IVersionMigrator vmt = new VersionMigTo100();
			doMigration(vmt, "1.0.0");
		}
	}

	private void doMigration (IVersionMigrator vmt, String nextVersion) {
		vmt.setProject(projectPath, projectVo, projectInfoVO);

		if (vmt.doMigration()) {
			setVersion(nextVersion);
			migrationIterator();
		}
	}

	private void setVersion (String version) {
		projectVo.projectVersion = version;
		String projectVoJson = json.toJson(projectVo, ProjectVO.class);
		try {
			FileUtils.writeStringToFile(new java.io.File(projectPath + "/project.h2d"), projectVoJson, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
