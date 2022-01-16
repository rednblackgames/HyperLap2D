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

package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonVerticesCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.dialog.AutoTraceDialog;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 7/2/2015.
 */
public class UIPolygonComponentPropertiesMediator extends UIItemPropertiesMediator<UIPolygonComponentProperties> {

    private static final String TAG = UIPolygonComponentPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private PolygonShapeComponent polygonShapeComponent;

    public UIPolygonComponentPropertiesMediator() {
        super(NAME, new UIPolygonComponentProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UIPolygonComponentProperties.ADD_DEFAULT_MESH_BUTTON_CLICKED,
                UIPolygonComponentProperties.COPY_BUTTON_CLICKED,
                UIPolygonComponentProperties.PASTE_BUTTON_CLICKED,
                UIPolygonComponentProperties.CLOSE_CLICKED,
                UIPolygonComponentProperties.ADD_AUTO_TRACE_MESH_BUTTON_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UIPolygonComponentProperties.ADD_DEFAULT_MESH_BUTTON_CLICKED:
                addDefaultMesh();
                break;
            case UIPolygonComponentProperties.ADD_AUTO_TRACE_MESH_BUTTON_CLICKED:
                addAutoTraceMesh();
                break;
            case UIPolygonComponentProperties.COPY_BUTTON_CLICKED:
                copyMesh();
                break;
            case UIPolygonComponentProperties.PASTE_BUTTON_CLICKED:
                pasteMesh();
                break;
            case UIPolygonComponentProperties.CLOSE_CLICKED:
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, PolygonShapeComponent.class));
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(int item) {
        polygonShapeComponent = SandboxComponentRetriever.get(item, PolygonShapeComponent.class);
        if (polygonShapeComponent.vertices == null && polygonShapeComponent.polygonizedVertices == null) {
            viewComponent.initEmptyView();
            return;
        }

        viewComponent.initView();
        viewComponent.setOpenPath(polygonShapeComponent.openEnded);

        if(polygonShapeComponent.polygonizedVertices != null) {
            int verticesCount = 0;
            for(int i = 0; i < polygonShapeComponent.polygonizedVertices.length; i++) {
                for(int j = 0; j < polygonShapeComponent.polygonizedVertices[i].length; j++) {
                    verticesCount++;
                }
            }
            viewComponent.setVerticesCount(verticesCount);
        }
    }

    @Override
    protected void translateViewToItemData() {
        if (polygonShapeComponent.vertices == null && polygonShapeComponent.polygonizedVertices == null) {
            return;
        }

        Object[] payload = new Object[2];
        payload[0] = observableReference;
        payload[1] = viewComponent.isOpenEnded();

        if (viewComponent.isOpenEnded() != polygonShapeComponent.openEnded)
            facade.sendNotification(MsgAPI.ACTION_UPDATE_POLYGON_DATA, payload);
    }

    private void addDefaultMesh() {
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(observableReference, DimensionsComponent.class);
        if(dimensionsComponent.boundBox != null) { // If the bound box is not null we have a Composite Item!
            polygonShapeComponent.makeRectangle( dimensionsComponent.boundBox.x, dimensionsComponent.boundBox.y, dimensionsComponent.boundBox.width, dimensionsComponent.boundBox.height);
        }
        else // Otherwise its a normal item
        {
            polygonShapeComponent.makeRectangle( dimensionsComponent.width, dimensionsComponent.height );
        }

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, observableReference);
    }

    private void addAutoTraceMesh() {
        facade.sendNotification(AutoTraceDialog.OPEN_DIALOG, observableReference);
    }

    private void copyMesh() {
        polygonShapeComponent = SandboxComponentRetriever.get(observableReference, PolygonShapeComponent.class);
        Object[] param = new Object[2];
        param[0] = polygonShapeComponent.polygonizedVertices;
        param[1] = polygonShapeComponent.vertices;
        Sandbox.getInstance().copyToLocalClipboard("meshData", param);
    }

    private void pasteMesh() {
        Object[] param = (Object[]) Sandbox.getInstance().retrieveFromLocalClipboard("meshData");
        Vector2[][] polygonizedVertices = (Vector2[][]) param[0];
        Array<Vector2> vertices = (Array<Vector2>) param[1];
        if(vertices == null) return;
        Object[] payload = UpdatePolygonVerticesCommand.payloadInitialState(observableReference);
        UpdatePolygonVerticesCommand.payload(payload, vertices, polygonizedVertices);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, payload);
    }
}
