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

package games.rednblack.editor.controller;

import games.rednblack.editor.controller.commands.*;
import games.rednblack.editor.controller.commands.component.*;
import games.rednblack.editor.controller.commands.resource.*;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.commands.SimpleCommand;
import games.rednblack.puremvc.interfaces.INotification;

/**
 * Created by azakhary on 4/28/2015.
 */
public class BootstrapCommand extends SimpleCommand {

    @Override
	public void execute(INotification notification) {
        super.execute(notification);

        facade.sendNotification(SplashScreenAdapter.UPDATE_SPLASH, "Loading Commands...");

        facade.registerCommand(MsgAPI.ACTION_CUT, CutItemsCommand.class);
        facade.registerCommand(MsgAPI.ACTION_COPY, CopyItemsCommand.class);
        facade.registerCommand(MsgAPI.ACTION_PASTE, PasteItemsCommand.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE, DeleteItemsCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CREATE_ITEM, CreateItemCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, CompositeCameraChangeCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CREATE_PRIMITIVE, CreatePrimitiveCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CREATE_STICKY_NOTE, CreateStickyNoteCommand.class);
        facade.registerCommand(MsgAPI.ACTION_REMOVE_STICKY_NOTE, RemoveStickyNoteCommand.class);
        facade.registerCommand(MsgAPI.ACTION_MODIFY_STICKY_NOTE, ModifyStickyNoteCommand.class);

        facade.registerCommand(MsgAPI.ACTION_DELETE_LAYER, DeleteLayerCommand.class);
        facade.registerCommand(MsgAPI.ACTION_NEW_LAYER, NewLayerCommand.class);
        facade.registerCommand(MsgAPI.ACTION_SWAP_LAYERS, LayerSwapCommand.class);
        facade.registerCommand(MsgAPI.ACTION_JUMP_LAYERS, LayerJumpCommand.class);
        facade.registerCommand(MsgAPI.ACTION_RENAME_LAYER, RenameLayerCommand.class);

        facade.registerCommand(MsgAPI.ACTION_ADD_COMPONENT, AddComponentToItemCommand.class);
        facade.registerCommand(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.class);
        facade.registerCommand(MsgAPI.CUSTOM_VARIABLE_MODIFY, CustomVariableModifyCommand.class);

        facade.registerCommand(MsgAPI.ACTION_ITEMS_MOVE_TO, ItemsMoveCommand.class);

        facade.registerCommand(MsgAPI.ACTION_ITEM_AND_CHILDREN_TO, ItemChildrenTransformCommand.class);

        facade.registerCommand(MsgAPI.ACTION_ITEM_TRANSFORM_TO, ItemTransformCommand.class);
        facade.registerCommand(MsgAPI.ACTION_REPLACE_REGION_DATA, ReplaceRegionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_REPLACE_SPRITE_ANIMATION_DATA, ReplaceSpriteAnimationCommand.class);
        facade.registerCommand(MsgAPI.ACTION_REPLACE_SPINE_ANIMATION_DATA, ReplaceSpineCommand.class);
        facade.registerCommand(MsgAPI.ACTION_ADD_TO_LIBRARY, AddToLibraryCommand.class);
        facade.registerCommand(MsgAPI.ACTION_ADD_TO_LIBRARY_ACTION, AddToLibraryActionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CHANGE_LIBRARY_ACTION, ChangeLibraryActionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CONVERT_TO_BUTTON, ConvertToButtonCommand.class);
        facade.registerCommand(MsgAPI.ACTION_GROUP_ITEMS, ConvertToCompositeCommand.class);

        facade.registerCommand(MsgAPI.ACTION_SET_SELECTION, SetSelectionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_ADD_SELECTION, AddSelectionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_RELEASE_SELECTION, ReleaseSelectionCommand.class);

        facade.registerCommand(MsgAPI.ACTION_UPDATE_RULER_POSITION, ChangeRulerPositionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CHANGE_POLYGON_VERTEX_POSITION, ChangePolygonVertexPositionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_POLYGON_VERTEX, DeletePolygonVertexCommand.class);
        facade.registerCommand(MsgAPI.ACTION_CHANGE_ORIGIN_POSITION, ChangeOriginPointPosition.class);
        facade.registerCommand(MsgAPI.ACTION_CENTER_ORIGIN_POSITION, CenterOriginPointCommand.class);

        // DATA MODIFY by components
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SCENE_DATA, UpdateSceneDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_ITEM_DATA, UpdateEntityComponentsCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_LABEL_DATA, UpdateLabelDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_LIGHT_DATA, UpdateLightDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_COMPOSITE_DATA, UpdateCompositeDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_PARTICLE_DATA, UpdateParticleDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_TALOS_DATA, UpdateTalosDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_BODY_LIGHT_DATA, UpdateLightBodyDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_CIRCLE_SHAPE, UpdateCircleShapeCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_PHYSICS_BODY_DATA, UpdatePhysicsDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SENSOR_DATA, UpdateSensorDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SHADER_DATA, UpdateShaderDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_IMAGE_ITEM_DATA, UpdateImageItemDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SPRITE_ANIMATION_DATA, UpdateSpriteAnimationDataCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SPINE_ANIMATION_DATA, UpdateSpineDataCommand.class);

        facade.registerCommand(MsgAPI.ACTION_UPDATE_MESH_DATA, UpdatePolygonVerticesCommand.class);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_POLYGON_DATA, UpdatePolygonDataCommand.class);

        facade.registerCommand(MsgAPI.ACTION_EXPORT_PROJECT, ExportProjectCommand.class);
        facade.registerCommand(MsgAPI.SAVE_EXPORT_PATH, SaveExportPathCommand.class);

        facade.registerCommand(MsgAPI.ACTION_PLUGIN_PROXY_COMMAND, PluginItemCommand.class);

        // Resources
        facade.registerCommand(MsgAPI.ACTION_DELETE_IMAGE_RESOURCE, DeleteImageResource.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_TINY_VG_RESOURCE, DeleteTinyVGResource.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_SHADER, DeleteShaderCommand.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_LIBRARY_ITEM, DeleteLibraryItem.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_LIBRARY_ACTION, DeleteLibraryAction.class);
        facade.registerCommand(MsgAPI.ACTION_DUPLICATE_LIBRARY_ACTION, DuplicateLibraryAction.class);
        facade.registerCommand(MsgAPI.ACTION_EXPORT_LIBRARY_ITEM, ExportLibraryItemCommand.class);
        facade.registerCommand(MsgAPI.ACTION_EXPORT_ACTION_ITEM, ExportActionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_RENAME_ACTION_ITEM, RenameLibraryActionCommand.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_PARTICLE_EFFECT, DeleteParticleEffect.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_TALOS_VFX, DeleteTalosVFX.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_SPINE_ANIMATION_RESOURCE, DeleteSpineAnimation.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_SPRITE_ANIMATION_RESOURCE, DeleteSpriteAnimation.class);
        facade.registerCommand(MsgAPI.ACTION_DELETE_MULTIPLE_RESOURCE, DeleteMultipleResources.class);

        facade.registerCommand(MsgAPI.SHOW_NOTIFICATION, ShowNotificationCommand.class);
    }
}
