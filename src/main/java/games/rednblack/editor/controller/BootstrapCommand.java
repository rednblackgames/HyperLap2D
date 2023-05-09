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

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.*;
import games.rednblack.editor.controller.commands.component.*;
import games.rednblack.editor.controller.commands.resource.*;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

/**
 * Created by azakhary on 4/28/2015.
 */
public class BootstrapCommand extends SimpleCommand {

    @Override
	public void execute(INotification notification) {
        super.execute(notification);
        facade = HyperLap2DFacade.getInstance();
        facade.sendNotification(SplashScreenAdapter.UPDATE_SPLASH, "Loading Commands...");

        facade.registerCommand(MsgAPI.ACTION_CUT, CutItemsCommand::new);
        facade.registerCommand(MsgAPI.ACTION_COPY, CopyItemsCommand::new);
        facade.registerCommand(MsgAPI.ACTION_PASTE, PasteItemsCommand::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE, DeleteItemsCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CREATE_ITEM, CreateItemCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, CompositeCameraChangeCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CREATE_PRIMITIVE, CreatePrimitiveCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CREATE_STICKY_NOTE, CreateStickyNoteCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CREATE_STICKY_NOTE, CreateStickyNoteCommand::new);
        facade.registerCommand(MsgAPI.ACTION_REMOVE_STICKY_NOTE, RemoveStickyNoteCommand::new);
        facade.registerCommand(MsgAPI.ACTION_MODIFY_STICKY_NOTE, ModifyStickyNoteCommand::new);

        facade.registerCommand(MsgAPI.ACTION_DELETE_LAYER, DeleteLayerCommand::new);
        facade.registerCommand(MsgAPI.ACTION_NEW_LAYER, NewLayerCommand::new);
        facade.registerCommand(MsgAPI.ACTION_SWAP_LAYERS, LayerSwapCommand::new);
        facade.registerCommand(MsgAPI.ACTION_JUMP_LAYERS, LayerJumpCommand::new);
        facade.registerCommand(MsgAPI.ACTION_RENAME_LAYER, RenameLayerCommand::new);

        facade.registerCommand(MsgAPI.ACTION_ADD_COMPONENT, AddComponentToItemCommand::new);
        facade.registerCommand(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand::new);
        facade.registerCommand(MsgAPI.CUSTOM_VARIABLE_MODIFY, CustomVariableModifyCommand::new);

        facade.registerCommand(MsgAPI.ACTION_ITEMS_MOVE_TO, ItemsMoveCommand::new);

        facade.registerCommand(MsgAPI.ACTION_ITEM_AND_CHILDREN_TO, ItemChildrenTransformCommand::new);

        facade.registerCommand(MsgAPI.ACTION_ITEM_TRANSFORM_TO, ItemTransformCommand::new);
        facade.registerCommand(MsgAPI.ACTION_REPLACE_REGION_DATA, ReplaceRegionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_REPLACE_SPRITE_ANIMATION_DATA, ReplaceSpriteAnimationCommand::new);
        facade.registerCommand(MsgAPI.ACTION_REPLACE_SPINE_ANIMATION_DATA, ReplaceSpineCommand::new);
        facade.registerCommand(MsgAPI.ACTION_ADD_TO_LIBRARY, AddToLibraryCommand::new);
        facade.registerCommand(MsgAPI.ACTION_ADD_TO_LIBRARY_ACTION, AddToLibraryAction::new);
        facade.registerCommand(MsgAPI.ACTION_CHANGE_LIBRARY_ACTION, ChangeLibraryActionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CONVERT_TO_BUTTON, ConvertToButtonCommand::new);
        facade.registerCommand(MsgAPI.ACTION_GROUP_ITEMS, ConvertToCompositeCommand::new);

        facade.registerCommand(MsgAPI.ACTION_SET_SELECTION, SetSelectionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_ADD_SELECTION, AddSelectionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_RELEASE_SELECTION, ReleaseSelectionCommand::new);

        facade.registerCommand(MsgAPI.ACTION_UPDATE_RULER_POSITION, ChangeRulerPositionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CHANGE_POLYGON_VERTEX_POSITION, ChangePolygonVertexPositionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_POLYGON_VERTEX, DeletePolygonVertexCommand::new);
        facade.registerCommand(MsgAPI.ACTION_CHANGE_ORIGIN_POSITION, ChangeOriginPointPosition::new);
        facade.registerCommand(MsgAPI.ACTION_CENTER_ORIGIN_POSITION, CenterOriginPointCommand::new);

        // DATA MODIFY by components
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SCENE_DATA, UpdateSceneDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_ITEM_DATA, UpdateEntityComponentsCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_LABEL_DATA, UpdateLabelDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_LIGHT_DATA, UpdateLightDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_COMPOSITE_DATA, UpdateCompositeDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_PARTICLE_DATA, UpdateParticleDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_TALOS_DATA, UpdateTalosDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_BODY_LIGHT_DATA, UpdateLightBodyDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_CIRCLE_SHAPE, UpdateCircleShapeCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_PHYSICS_BODY_DATA, UpdatePhysicsDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SENSOR_DATA, UpdateSensorDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SHADER_DATA, UpdateShaderDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_IMAGE_ITEM_DATA, UpdateImageItemDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SPRITE_ANIMATION_DATA, UpdateSpriteAnimationDataCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_SPINE_ANIMATION_DATA, UpdateSpineDataCommand::new);

        facade.registerCommand(MsgAPI.ACTION_UPDATE_MESH_DATA, UpdatePolygonVerticesCommand::new);
        facade.registerCommand(MsgAPI.ACTION_UPDATE_POLYGON_DATA, UpdatePolygonDataCommand::new);

        facade.registerCommand(MsgAPI.ACTION_EXPORT_PROJECT, ExportProjectCommand::new);
        facade.registerCommand(MsgAPI.SAVE_EXPORT_PATH, SaveExportPathCommand::new);

        facade.registerCommand(MsgAPI.ACTION_PLUGIN_PROXY_COMMAND, PluginItemCommand::new);

        // Resources
        facade.registerCommand(MsgAPI.ACTION_DELETE_IMAGE_RESOURCE, DeleteImageResource::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_TINY_VG_RESOURCE, DeleteTinyVGResource::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_SHADER, DeleteShaderCommand::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_LIBRARY_ITEM, DeleteLibraryItem::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_LIBRARY_ACTION, DeleteLibraryAction::new);
        facade.registerCommand(MsgAPI.ACTION_DUPLICATE_LIBRARY_ACTION, DuplicateLibraryAction::new);
        facade.registerCommand(MsgAPI.ACTION_EXPORT_LIBRARY_ITEM, ExportLibraryItemCommand::new);
        facade.registerCommand(MsgAPI.ACTION_EXPORT_ACTION_ITEM, ExportActionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_RENAME_ACTION_ITEM, RenameLibraryActionCommand::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_PARTICLE_EFFECT, DeleteParticleEffect::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_TALOS_VFX, DeleteTalosVFX::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_SPINE_ANIMATION_RESOURCE, DeleteSpineAnimation::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_SPRITE_ANIMATION_RESOURCE, DeleteSpriteAnimation::new);
        facade.registerCommand(MsgAPI.ACTION_DELETE_MULTIPLE_RESOURCE, DeleteMultipleResources::new);

        facade.registerCommand(MsgAPI.SHOW_NOTIFICATION, ShowNotificationCommand::new);
    }
}
