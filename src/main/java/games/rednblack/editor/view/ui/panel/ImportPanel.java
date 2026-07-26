package games.rednblack.editor.view.ui.panel;

import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

import java.util.HashMap;

public class ImportPanel extends UIDraggablePanel {
    public static final String CLASS_NAME = "games.rednblack.editor.view.ui.panel.ImportDialog";

    public static final String BROWSE_BTN_CLICKED = CLASS_NAME + ".BROWSE_BTN_CLICKED";

    public static final String IMPORT_FAILED = CLASS_NAME + ".IMPORT_FAILED";

    private static final String DROP_ZONE_BG = "drop-zone";
    private static final String ERROR_BG = "message-error";
    private static final String ERROR_STYLE = "message-error";
    private static final String ERROR_ICON = "icon-alert";
    private static final String DROP_ZONE_BG_OVER = "drop-zone-over";
    private static final String DROP_ICON = "icon-menu-import";

    private static final int MIN_WIDTH = 400;
    private static final int ERROR_PAD = 8;
    /** Left padding of the banner content, clearing the red bar the drawable carries. */
    private static final int ERROR_BAR_PAD = 12;
    private static final int ERROR_ICON_SIZE = 18;
    private static final int DROP_ZONE_HEIGHT = 96;
    private static final String SUPPORTED_TYPES =
            "Images, sprite animations (atlas or image sequence), spine animations, particle effects, "
                    + "fonts, shaders, libraries and actions.";

    private final Facade facade;

    /** The view of the panel, swapped between dropping and importing. */
    private final VisTable body;
    /** Kept out of {@link #body}, so switching view cannot take the error message away with it. */
    private final Cell<VisTable> errorCell;
    private final VisTable errorBanner;
    private final VisLabel errorLabel;

    private VisTable dropZone;
    private VisProgressBar progressBar;

    private final HashMap<Integer, String> typeNames = new HashMap<>();

    ImportPanel() {
        super("Import Resources");
        setMovable(true);
        setModal(false);
        addCloseButton();
        setStyle(VisUI.getSkin().get("box", WindowStyle.class));
        getTitleLabel().setAlignment(Align.left);

        facade = Facade.getInstance();

        fillTypeNames();

        errorLabel = StandardWidgetsFactory.createLabel("", ERROR_STYLE, Align.left);
        errorLabel.setWrap(true);

        errorBanner = new VisTable();
        errorBanner.setBackground(VisUI.getSkin().getDrawable(ERROR_BG));
        errorBanner.setTouchable(Touchable.disabled);
        errorBanner.add(new Image(VisUI.getSkin().getDrawable(ERROR_ICON))).size(ERROR_ICON_SIZE)
                .top().padTop(ERROR_PAD).padLeft(ERROR_BAR_PAD).padRight(ERROR_PAD);
        PropertyGrid.elastic(errorBanner.add(errorLabel)).pad(ERROR_PAD).padLeft(0);

        body = new VisTable();

        VisTable content = new VisTable();
        // the banner only exists while there is something to say, so no space is held for it
        errorCell = content.add((VisTable) null).growX()
                .padLeft(PropertyGrid.PANEL_PAD + PropertyGrid.CONTENT_PAD)
                .padRight(PropertyGrid.PANEL_PAD + PropertyGrid.CONTENT_PAD);
        content.row();
        content.add(body).growX().row();
        getContentTable().add(content).growX();

        setDroppingView();
    }

    private void fillTypeNames() {
        typeNames.clear();

        typeNames.put(AssetsUtils.TYPE_ANIMATION_PNG_SEQUENCE, "PNG Sequence Animation");
        typeNames.put(AssetsUtils.TYPE_BITMAP_FONT, "Bitmap Font");
        typeNames.put(AssetsUtils.TYPE_IMAGE, "Texture");
        typeNames.put(AssetsUtils.TYPE_TEXTURE_ATLAS, "Texture Atlas");
        typeNames.put(AssetsUtils.TYPE_PARTICLE_EFFECT, "Particle Effect");
        typeNames.put(AssetsUtils.TYPE_SPINE_ANIMATION, "Spine Animation");
        typeNames.put(AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS, "Animation Atlas Pack");
        typeNames.put(AssetsUtils.TYPE_TTF_FONT, "TTF Font");
        typeNames.put(AssetsUtils.TYPE_HYPERLAP2D_LIBRARY, "HyperLap2D Library");
        typeNames.put(AssetsUtils.TYPE_HYPERLAP2D_ACTION, "HyperLap2D Action");
        typeNames.put(AssetsUtils.TYPE_SHADER, "Shader");
        typeNames.put(AssetsUtils.TYPE_TALOS_VFX, "Talos VFX");
        typeNames.put(AssetsUtils.TYPE_TINY_VG, "TinyVG Image");
    }

    public static class DropBundle {
        public String[] paths;
        public Vector2 pos;
    }

    public boolean checkDropRegionHit(Vector2 mousePos) {
        Vector2 pos = PluginUIBridge.get().getSandbox().getUIStage().getViewport().unproject(mousePos);
        pos = dropZone.stageToLocalCoordinates(pos);
        if (dropZone.hit(pos.x, pos.y, false) != null) {
            return true;
        }

        dragExit();
        return false;
    }

    public void dragOver() {
        if (dropZone != null) dropZone.setBackground(VisUI.getSkin().getDrawable(DROP_ZONE_BG_OVER));
    }

    public void dragExit() {
        if (dropZone != null) dropZone.setBackground(VisUI.getSkin().getDrawable(DROP_ZONE_BG));
    }

    /** Waiting for files: the drop zone, the way to browse instead, and what can be imported. */
    public void setDroppingView() {
        body.clear();
        hideError();

        VisTextButton browseButton = StandardWidgetsFactory.createTextButton("Browse files");
        browseButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(BROWSE_BTN_CLICKED);
            }
        });

        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("Import");
        grid.wideFill(createDropZone(), DROP_ZONE_HEIGHT);
        grid.buttons(browseButton);

        grid.section("Supported types");
        grid.wideFill(wrappedText(SUPPORTED_TYPES));

        invalidateHeight();
    }

    /** Importing: what is being brought in, and how far along it is. */
    public void setImportingView(int type, int count) {
        body.clear();
        hideError();

        String typeText = typeNames.get(type);
        if (count > 1) typeText += " (" + count + ")";

        progressBar = new VisProgressBar(0, 100, 1, false);
        progressBar.setAnimateDuration(0.5f);

        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("Importing");
        grid.rowCompact("Type", PropertyGrid.value(typeText));
        grid.wideFill(progressBar);

        invalidateHeight();
    }

    private VisTable createDropZone() {
        dropZone = new VisTable();
        dropZone.setBackground(VisUI.getSkin().getDrawable(DROP_ZONE_BG));
        dropZone.add(new Image(VisUI.getSkin().getDrawable(DROP_ICON))).padBottom(6).row();
        dropZone.add(StandardWidgetsFactory.createLabel("Drop files here",
                PropertyGrid.style(PropertyGrid.SECTION_STYLE_LARGE), Align.center)).row();
        return dropZone;
    }

    private VisTable wrappedText(String text) {
        VisLabel label = StandardWidgetsFactory.createLabel(text,
                PropertyGrid.style(PropertyGrid.LABEL_STYLE_LARGE), Align.left);
        label.setWrap(true);
        VisTable wrapper = new VisTable();
        // no preferred width of its own, so the text wraps to the panel instead of widening it
        PropertyGrid.elastic(wrapper.add(label));
        return wrapper;
    }

    /**
     * Reports why an import did not happen. Whatever the panel was showing, it goes back to waiting
     * for files: an import that failed halfway would otherwise leave a progress bar stalled on screen
     * with no way to tell it is over.
     */
    public void showError(int type) {
        String text = switch (type) {
            case AssetsUtils.TYPE_UNSUPPORTED, AssetsUtils.TYPE_UNKNOWN ->
                    "That file cannot be imported. See the supported types below.";
            case AssetsUtils.TYPE_MIXED ->
                    "Those files are of different kinds. Import one kind at a time.";
            case AssetsUtils.TYPE_FAILED ->
                    "The import failed. The console has the details.";
            default -> "The import did not happen.";
        };

        setDroppingView();

        errorLabel.setText(text);
        errorCell.setActor(errorBanner).padTop(PropertyGrid.PANEL_PAD)
                .padBottom(PropertyGrid.DIALOG_ROW_PAD);
        errorBanner.clearActions();
        errorBanner.getColor().a = 0;
        errorBanner.addAction(Actions.fadeIn(0.3f));
        dragExit();
        invalidateHeight();
    }

    private void hideError() {
        errorBanner.clearActions();
        errorLabel.setText("");
        errorCell.setActor(null).padTop(0).padBottom(0);
    }

    public VisProgressBar getProgressBar() {
        return progressBar;
    }

    /** Closing the panel drops the message with it: a stale error must not greet the next open. */
    @Override
    protected void onDismiss() {
        super.onDismiss();
        hideError();
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }
}
