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
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.TexturePackVO;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class VersionMigTo020 implements IVersionMigrator {
    private final Json json = new Json();

    private String projectPath;
    private ProjectVO projectVO;

    @Override
    public void setProject(String path, ProjectVO vo) {
        projectPath = path;
        projectVO = vo;
        json.setOutputType(JsonWriter.OutputType.json);
    }

    @Override
    public boolean doMigration() {
        TexturePackVO mainPack = new TexturePackVO();
        mainPack.name = "main";
        projectVO.imagesPacks.put("main", mainPack);

        TexturePackVO mainAnimPack = new TexturePackVO();
        mainAnimPack.name = "main";
        projectVO.animationsPacks.put("main", mainAnimPack);

        String spriteAnimationsPath = projectPath + File.separator + "assets/orig" + File.separator + "sprite-animations";
        FileHandle sourceDir = new FileHandle(spriteAnimationsPath);
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());

                FileHandle atlasTargetPath = new FileHandle(new File(spriteAnimationsPath + File.separator + animName + File.separator + animName + ".atlas"));
                TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(atlasTargetPath, atlasTargetPath.parent(), false);

                for (TextureAtlas.TextureAtlasData.Region region : new Array.ArrayIterator<>(atlas.getRegions())) {
                    projectVO.animationsPacks.get("main").regions.add(region.name);
                }

                try {
                    ImportUtils.unpackAtlasIntoTmpFolder(atlasTargetPath.file(), null,projectPath + File.separator + ProjectManager.IMAGE_DIR_PATH);
                } catch (Exception ignore) {
                }

                for (TextureAtlas.TextureAtlasData.Page page : new Array.ArrayIterator<>(atlas.getPages())) {
                    if (page.textureFile != null)
                        page.textureFile.delete();
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
                    projectVO.animationsPacks.get("main").regions.add(animName+region.name);
                }

                try {
                    ImportUtils.unpackAtlasIntoTmpFolder(atlasTargetPath.file(), animName, projectPath + File.separator + ProjectManager.IMAGE_DIR_PATH);
                } catch (Exception ignore) {
                }

                for (TextureAtlas.TextureAtlasData.Page page : new Array.ArrayIterator<>(atlas.getPages())) {
                    if (page.textureFile != null)
                        page.textureFile.delete();
                }
            }
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        FileHandle whitePixel = new FileHandle(projectPath + File.separator + "assets/orig/images" + File.separator + "white-pixel.png");
        PixmapIO.writePNG(whitePixel, pixmap);

        projectVO.imagesPacks.get("main").regions.add("white-pixel");

        String res = projectVO.lastOpenResolution.isEmpty() ? "orig" : projectVO.lastOpenResolution;
        File pack = new File(projectPath + "/assets/" + res + "/pack/pack.atlas");
        pack.delete();

        return true;
    }
}
