package games.rednblack.editor.data.migrations.migrators;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.data.migrations.IVersionMigrator;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.h2d.common.vo.ProjectVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class VersionMigTo020 implements IVersionMigrator {
    private final Json json = new Json();

    private String projectPath;
    private ProjectVO projectVO;
    private ProjectInfoVO projectInfoVO;

    @Override
    public void setProject(String path, ProjectVO vo, ProjectInfoVO projectInfoVO) {
        projectPath = path;
        projectVO = vo;
        this.projectInfoVO = projectInfoVO;
        json.setOutputType(JsonWriter.OutputType.json);
    }

    @Override
    public boolean doMigration() {
        TexturePackVO mainPack = new TexturePackVO();
        mainPack.name = "main";
        projectInfoVO.imagesPacks.put("main", mainPack);

        String res = projectVO.lastOpenResolution.isEmpty() ? "orig" : projectVO.lastOpenResolution;
        FileHandle pack = new FileHandle(projectPath + "/assets/" + res + "/pack/pack.atlas");
        TextureAtlas.TextureAtlasData mainAtlas = new TextureAtlas.TextureAtlasData(pack, pack.parent(), false);

        for (TextureAtlas.TextureAtlasData.Region region : new Array.ArrayIterator<>(mainAtlas.getRegions())) {
            projectInfoVO.imagesPacks.get("main").regions.add(region.name);
        }

        TexturePackVO mainAnimPack = new TexturePackVO();
        mainAnimPack.name = "main";
        projectInfoVO.animationsPacks.put("main", mainAnimPack);

        String spriteAnimationsPath = projectPath + File.separator + "assets/orig" + File.separator + "sprite-animations";
        FileHandle sourceDir = new FileHandle(spriteAnimationsPath);
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());

                FileHandle atlasTargetPath = new FileHandle(new File(spriteAnimationsPath + File.separator + animName + File.separator + animName + ".atlas"));
                TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(atlasTargetPath, atlasTargetPath.parent(), false);

                for (TextureAtlas.TextureAtlasData.Region region : new Array.ArrayIterator<>(atlas.getRegions())) {
                    projectInfoVO.animationsPacks.get("main").regions.add(region.name);
                }

                try {
                    AssetsUtils.unpackAtlasIntoTmpFolder(atlasTargetPath.file(), null,projectPath + File.separator + ProjectManager.IMAGE_DIR_PATH);
                } catch (Exception ignore) {
                }
            }
        }

        String spineAnimationsPath = projectPath + File.separator  + "assets/orig" + File.separator + "spine-animations";
        FileHandle spineDir = new FileHandle(spineAnimationsPath);
        for (FileHandle entry : spineDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());

                FileHandle atlasTargetPath = new FileHandle(new File(spineAnimationsPath + File.separator + animName + File.separator + animName + ".atlas"));
                TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(atlasTargetPath, atlasTargetPath.parent(), false);

                for (TextureAtlas.TextureAtlasData.Region region : new Array.ArrayIterator<>(atlas.getRegions())) {
                    projectInfoVO.animationsPacks.get("main").regions.add(animName+region.name);
                }

                try {
                    AssetsUtils.unpackAtlasIntoTmpFolder(atlasTargetPath.file(), animName, projectPath + File.separator + ProjectManager.IMAGE_DIR_PATH);
                } catch (Exception ignore) {
                }
            }
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        FileHandle whitePixel = new FileHandle(projectPath + File.separator + "assets/orig/images" + File.separator + "white-pixel.png");
        PixmapIO.writePNG(whitePixel, pixmap);

        projectInfoVO.imagesPacks.get("main").regions.add("white-pixel");
        try {
            FileUtils.writeStringToFile(new File(projectPath + "/project.dt"), projectInfoVO.constructJsonString(), "utf-8");
        } catch (IOException e) {
            return false;
        }

        pack.delete();

        return true;
    }
}
