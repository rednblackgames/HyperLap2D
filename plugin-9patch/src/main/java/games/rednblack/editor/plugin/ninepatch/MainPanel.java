package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.h2d.common.H2DDialog;
import org.puremvc.java.interfaces.IFacade;

/**
 * Created by azakhary on 8/18/2015.
 */
public class MainPanel extends H2DDialog {
    public static final String CLASS_NAME = "ames.rednblack.editor.plugin.ninepatch.MainPanel";

    public static final String SAVE_CLICKED = CLASS_NAME + ".SAVE_CLICKED";

    private IFacade facade;

    private VisTable mainTable;
    private TextureRegion texture;

    private VisTable editingTable;
    private VisTable previewTable;

    private EditingZone editingZone;
    private PreviewWidget previewWidget;

    public MainPanel(IFacade facade) {
        super("Nine Patch", false);
        addCloseButton();

        this.facade = facade;

        mainTable = new VisTable();
        add(mainTable).width(520).height(310).padBottom(7);
        editingTable = new VisTable();
        previewTable = new VisTable();

        mainTable.add(editingTable).width(310).expandY();
        mainTable.add(previewTable).expandX().expandY();
        mainTable.row();
    }

    private void initView() {
        editingTable.clear();
        editingZone = new EditingZone();
        editingZone.setTexture(texture);
        editingTable.add(editingZone);
        addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                editingZone.zoomBy(amountY);
                return true;
            }
        });

        editingZone.setWidth(310);
        editingZone.setHeight(310);

        editingZone.setListener(new EditingZone.PatchChangeListener() {
            @Override
            public void changed(int[] splits) {
                previewWidget.update((TextureAtlas.AtlasRegion) texture, splits);
            }
        });
    }

    private void initPreView() {
        previewTable.clear();
        previewWidget = new PreviewWidget();
        previewWidget.setHeight(205);
        previewTable.add(previewWidget).width(200).height(205).top();
        previewTable.row();
        previewWidget.update((TextureAtlas.AtlasRegion) texture, ((TextureAtlas.AtlasRegion) texture).findValue("split"));

        VisLabel label = new VisLabel("Note: after saving, your \n scene will reload to \n apply changes.");
        label.setAlignment(Align.center);
        previewTable.add(label).pad(10).fillY().expandY();
        previewTable.row();

        VisTextButton saveBtn = new VisTextButton("apply and save");
        previewTable.add(saveBtn).pad(5);
        previewTable.row();

        saveBtn.addListener(new ClickListener() {
            public void clicked (InputEvent event, float x, float y) {
                facade.sendNotification(SAVE_CLICKED);
            }
        });
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;

        initView();
        initPreView();
    }

    public int[] getSplits() {
        return editingZone.getSplits();
    }

}