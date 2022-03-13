package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class HyperLap2DInternalLibraryAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("lib");
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            Json json = HyperJson.getJson();
            String projectInfoContents = null;
            try {
                projectInfoContents = FileUtils.readFileToString(handle.file(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            CompositeItemVO voInfo = json.fromJson(CompositeItemVO.class, projectInfoContents);
            adjustPPWCoordinates(voInfo);

            String fileNameAndExtension = handle.name();
            String fileName = FilenameUtils.removeExtension(fileNameAndExtension);
            projectManager.getCurrentProjectInfoVO().libraryItems.put(fileName, voInfo);
            projectManager.saveCurrentProject();
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        return false;
    }

    private void adjustPPWCoordinates(CompositeItemVO compositeItemVO) {
        int ppwu = projectManager.getCurrentProjectInfoVO().pixelToWorld;
        compositeItemVO.width /= ppwu;
        compositeItemVO.height /= ppwu;

        for (MainItemVO item : compositeItemVO.getAllItems()) {
            item.originX = item.originX / ppwu;
            item.originY = item.originY / ppwu;
            item.x = item.x / ppwu;
            item.y = item.y / ppwu;

            if (item.shape != null) {
                PolygonShapeVO shapeVO = item.shape;
                if (shapeVO.vertices != null) {
                    for (Vector2 vector2 : shapeVO.vertices) {
                        vector2.x /= ppwu;
                        vector2.y /= ppwu;
                    }
                }

                if (shapeVO.polygonizedVertices != null) {
                    for (Vector2[] array : shapeVO.polygonizedVertices) {
                        for (Vector2 vector2 : array) {
                            vector2.x /= ppwu;
                            vector2.y /= ppwu;
                        }
                    }
                }
            }

            if (item.physics != null) {
                PhysicsBodyDataVO physicsBodyDataVO = item.physics;
                physicsBodyDataVO.centerOfMass.x /= ppwu;
                physicsBodyDataVO.centerOfMass.y /= ppwu;
            }

            if (item.light != null) {
                LightBodyDataVO lightBodyDataVO = item.light;
                lightBodyDataVO.distance /= ppwu;
            }

            if (item.circle != null) {
                item.circle.x /= ppwu;
                item.circle.y /= ppwu;
                item.circle.radius /= ppwu;
            }

            if (item instanceof CompositeItemVO) {
                ((CompositeItemVO) item).width = ((CompositeItemVO) item).width / ppwu;
                ((CompositeItemVO) item).height = ((CompositeItemVO) item).height / ppwu;
            }

            if (item instanceof Image9patchVO) {
                ((Image9patchVO) item).width = ((Image9patchVO) item).width / ppwu;
                ((Image9patchVO) item).height = ((Image9patchVO) item).height / ppwu;
            }

            if (item instanceof LabelVO) {
                ((LabelVO) item).width = ((LabelVO) item).width / ppwu;
                ((LabelVO) item).height = ((LabelVO) item).height / ppwu;
            }

            if (item instanceof LightVO) {
                ((LightVO) item).distance /= ppwu;
            }
        }
    }
}
