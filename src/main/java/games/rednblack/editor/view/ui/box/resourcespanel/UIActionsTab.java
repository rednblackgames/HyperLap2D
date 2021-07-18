package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIActionsTab extends UIResourcesTab {

    private VisTable list;

    public UIActionsTab() {
        VisImageButton newBtn = StandardWidgetsFactory.createImageButton("new-layer-button");
        VisImageButton deleteBtn = StandardWidgetsFactory.createImageButton("trash-button");

        VisTable bottomPane = new VisTable();
        contentTable.row();
        bottomPane.align(Align.left);
        contentTable.add(bottomPane).padTop(5).colspan(3).growX();

        bottomPane.add(newBtn).center().pad(3);
        bottomPane.add(deleteBtn).center().pad(3);

        newBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Dialogs.showInputDialog(Sandbox.getInstance().getUIStage(), "Create New Action", "Action Name : ", false, new StringNameValidator(), new InputDialogListener() {
                    @Override
                    public void finished(String input) {
                        if (input == null || input.equals("")) {
                            return;
                        }
                        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.OPEN_NODE_EDITOR, input);
                    }

                    @Override
                    public void canceled() {

                    }
                });
            }
        });
    }

    @Override
    public String getTabTitle() {
        return "Actions";
    }

    @Override
    public String getTabIconStyle() {
        return "action-button";
    }

    @Override
    protected VisScrollPane crateScrollPane() {
        list = new VisTable();
        return StandardWidgetsFactory.createScrollPane(list);
    }

    public void setItems(Array<DraggableResource> items) {
        list.clear();
        for (DraggableResource box : items) {
            list.add((Actor) box.getViewComponent()).expandX().fillX();
            list.row();
        }
    }
}
