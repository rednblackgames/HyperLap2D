package games.rednblack.editor.view.ui.box.resourcespanel.draggable.box;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.ui.box.resourcespanel.UIImagesTabMediator;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.ResourcePayloadObject;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Folder of the images tab, drawn as a card stack: a preview of what the pack contains, its name
 * underneath and a couple of sheets peeking out from behind, so folders can be told apart at a
 * glance instead of waiting for the tooltip.
 * <p>
 * Uses the same rounded card of {@link games.rednblack.editor.utils.ResourceGridAdapter} rather
 * than the wireframe box of the sprites around it, which also keeps folders visually apart from
 * the images they hold.
 */
public class AtlasResource extends BoxItemResource {

    private static final String CARD = "table-card";
    private static final String CARD_OVER = "table-card-over";

    /** Amount of sheets peeking out from behind the card. */
    private static final int STACK_SHEETS = 2;
    /** Vertical room taken by the whole stack, shortening the card by as much. */
    private static final float STACK_HEIGHT = 5f;
    /** How much narrower each sheet gets, on both sides. */
    private static final float SHEET_INSET = 3f;
    /** Sheets only show their top edge, the rest stays hidden behind the card. */
    private static final float SHEET_HEIGHT = 14f;
    /** Brightness of the sheet right behind the card, the ones further back get darker. */
    private static final float SHEET_TINT = 0.75f;

    /** Height of the name strip at the bottom of the card. */
    private static final float CAPTION_HEIGHT = 13f;
    /** Inset of the card content, keeping it clear of the rounded corners. */
    private static final float CARD_PADDING = 4f;

    /** Maximum amount of images composing the content preview. */
    private static final int PREVIEW_TILES = 4;
    /** Padding around each preview tile. */
    private static final float TILE_PADDING = 1.5f;

    private final ResourcePayloadObject payload;
    /** Card and sheets, swapped together so the whole stack lights up on mouse over. */
    private final Array<Image> stack = new Array<>(STACK_SHEETS + 1);

    public AtlasResource(TextureAtlas atlas, String atlasName) {
        this(atlas, atlasName, null);
    }

    /**
     * Creates a new folder card for the given atlas.
     *
     * @param atlas The atlas backing the folder, used to render the content preview.
     * @param atlasName The name of the atlas, {@code main} identifies the "go back" card.
     * @param regionNames Names of the regions actually owned by this pack, {@code null} to preview every region of the atlas.
     */
    public AtlasResource(TextureAtlas atlas, String atlasName, Collection<String> regionNames) {
        super(true);
        // the rounded card replaces the wireframe box of the base class
        rc.setVisible(false);

        boolean backFolder = atlasName.equals("main");
        // the "go back" card holds nothing, so it carries no stack either
        float cardHeight = backFolder ? getHeight() : getHeight() - STACK_HEIGHT;

        if (!backFolder) addStack(cardHeight);
        addCard(cardHeight);

        if (backFolder) {
            addCenteredIcon("icon-atlas-back-folder", cardHeight);
        } else {
            addContentPreview(atlas, regionNames, cardHeight);
        }

        addCaption(backFolder ? "Main" : atlasName);

        payload = new ResourcePayloadObject();
        payload.name = atlasName;

        setClickEvent(UIImagesTabMediator.CHANGE_FOLDER, atlasName, null, null);
    }

    /** Sheets peeking above the card, drawn back to front and dimmer the further back they are. */
    private void addStack(float cardHeight) {
        for (int sheet = STACK_SHEETS; sheet > 0; sheet--) {
            float inset = SHEET_INSET * sheet;
            float top = cardHeight + (STACK_HEIGHT / STACK_SHEETS) * sheet;
            float tint = (float) Math.pow(SHEET_TINT, sheet);

            Image image = new Image(VisUI.getSkin().getDrawable(CARD));
            image.setBounds(inset, top - SHEET_HEIGHT, getWidth() - inset * 2f, SHEET_HEIGHT);
            image.setColor(tint, tint, tint, 1f);
            addActor(image);
            stack.add(image);
        }
    }

    private void addCard(float cardHeight) {
        Image card = new Image(VisUI.getSkin().getDrawable(CARD));
        card.setBounds(0, 0, getWidth(), cardHeight);
        addActor(card);
        stack.add(card);
    }

    /**
     * Renders up to {@link #PREVIEW_TILES} images of the folder as a mosaic.
     *
     * @return <code>false</code> when the atlas has nothing to show, leaving the card empty.
     */
    private boolean addContentPreview(TextureAtlas atlas, Collection<String> regionNames, float cardHeight) {
        if (atlas == null) return false;

        Array<TextureRegion> tiles = pickPreviewTiles(atlas, regionNames);
        if (tiles.size == 0) return false;

        float areaX = CARD_PADDING;
        float areaY = CAPTION_HEIGHT + 1f;
        float areaWidth = getWidth() - CARD_PADDING * 2f;
        float areaHeight = cardHeight - areaY - CARD_PADDING;

        int columns = tiles.size == 1 ? 1 : 2;
        int rows = tiles.size <= 2 ? 1 : 2;
        // cells are not squared on purpose: a tile is scaled to fit anyway, so the wider the cell
        // the more room wide sprites get, and nothing is lost by the taller ones
        float cellWidth = areaWidth / columns;
        float cellHeight = areaHeight / rows;

        for (int i = 0; i < tiles.size; i++) {
            int column = i % columns;
            // first tiles go on the top row
            int row = rows - 1 - i / columns;
            addFittedImage(tiles.get(i),
                    areaX + column * cellWidth + TILE_PADDING,
                    areaY + row * cellHeight + TILE_PADDING,
                    cellWidth - TILE_PADDING * 2f,
                    cellHeight - TILE_PADDING * 2f);
        }

        return true;
    }

    /**
     * Picks the images used by the preview, sorted by name so that a folder always looks the same between sessions.
     */
    private Array<TextureRegion> pickPreviewTiles(TextureAtlas atlas, Collection<String> regionNames) {
        List<TextureAtlas.AtlasRegion> candidates = new ArrayList<>();
        for (TextureAtlas.AtlasRegion region : new Array.ArrayIterator<>(atlas.getRegions())) {
            if (region.name.equals("white-pixel")) continue;
            if (regionNames != null && !regionNames.contains(region.name)) continue;
            candidates.add(region);
        }
        candidates.sort((first, second) -> first.name.compareTo(second.name));

        Array<TextureRegion> tiles = new Array<>(PREVIEW_TILES);
        for (int i = 0; i < Math.min(PREVIEW_TILES, candidates.size()); i++) {
            tiles.add(candidates.get(i));
        }
        return tiles;
    }

    private void addFittedImage(TextureRegion region, float x, float y, float width, float height) {
        Image image = new Image(region);
        if (image.getWidth() <= 0 || image.getHeight() <= 0) return;

        float scale = Math.min(1f, Math.min(width / image.getWidth(), height / image.getHeight()));
        image.setScale(scale);
        image.setPosition(x + (width - image.getWidth() * scale) / 2f, y + (height - image.getHeight() * scale) / 2f);
        addActor(image);
    }

    private void addCenteredIcon(String drawableName, float cardHeight) {
        Image icon = new Image(VisUI.getSkin().getDrawable(drawableName));
        icon.setX((getWidth() - icon.getWidth()) / 2);
        icon.setY(CAPTION_HEIGHT + (cardHeight - CAPTION_HEIGHT - icon.getHeight()) / 2);
        addActor(icon);
    }

    private void addCaption(String name) {
        Image seat = new Image(WhitePixel.sharedInstance.drawable);
        seat.setColor(0f, 0f, 0f, 0.25f);
        seat.setBounds(CARD_PADDING, 3f, getWidth() - CARD_PADDING * 2f, CAPTION_HEIGHT - 3f);
        addActor(seat);

        Image separator = new Image(WhitePixel.sharedInstance.drawable);
        separator.setColor(1f, 1f, 1f, 0.1f);
        separator.setBounds(CARD_PADDING + 2f, CAPTION_HEIGHT, getWidth() - (CARD_PADDING + 2f) * 2f, 1f);
        addActor(separator);

        VisLabel label = StandardWidgetsFactory.createLabel(name, "small", Align.center, true);
        label.setBounds(CARD_PADDING, 2f, getWidth() - CARD_PADDING * 2f, CAPTION_HEIGHT - 2f);
        addActor(label);
    }

    @Override
    public void switchToMouseOverColor() {
        setStackDrawable(CARD_OVER);
    }

    @Override
    public void switchToStandardColor() {
        setStackDrawable(CARD);
    }

    private void setStackDrawable(String drawableName) {
        Drawable drawable = VisUI.getSkin().getDrawable(drawableName);
        for (Image image : new Array.ArrayIterator<>(stack)) {
            image.setDrawable(drawable);
        }
    }

    @Override
    public Actor getDragActor() {
        return null;
    }

    @Override
    public ResourcePayloadObject getPayloadData() {
        return payload;
    }
}
