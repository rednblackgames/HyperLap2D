package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import games.rednblack.editor.renderer.data.TenPatchVO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Extracts a region from an atlas page and writes it back as an Android style {@code .9.png}, with one
 * black segment per stretch area on the top and left border and the padding on the bottom and right border.
 *
 * Created by various artists on 8/18/2015.
 */
public class ImageUtils {
    private static final int NINEPATCH_PADDING = 1;
    private static final String OUTPUT_TYPE = "png";

    /**
     * @param vo stretch areas in pixels of the region, horizontal from the left and vertical from the bottom
     * @return the region wrapped in a 9-patch border, or null when the region is not in the atlas
     */
    public BufferedImage extractImage(TextureAtlas.TextureAtlasData atlas, String regionName, TenPatchVO vo) {
        for (TextureAtlas.TextureAtlasData.Region region : atlas.getRegions()) {
            if(region.name.equals(regionName)) {
                TextureAtlas.TextureAtlasData.Page page = region.page;
                BufferedImage img = null;
                try {
                    img = ImageIO.read(page.textureFile.file());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (img == null) return null;
                return extractNinePatch(img, region, vo);
            }
        }
        return null;
    }

    private BufferedImage extractImage (BufferedImage page, TextureAtlas.TextureAtlasData.Region region, int padding) {
        BufferedImage splitImage = null;

        // get the needed part of the page and rotate if needed
        if (region.rotate) {
            BufferedImage srcImage = page.getSubimage(region.left, region.top, region.height, region.width);
            splitImage = new BufferedImage(region.width, region.height, page.getType());

            AffineTransform transform = new AffineTransform();
            transform.rotate(Math.toRadians(90.0));
            transform.translate(0, -region.width);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            op.filter(srcImage, splitImage);
        } else {
            splitImage = page.getSubimage(region.left, region.top, region.width, region.height);
        }

        // draw the image to a bigger one if padding is needed
        if (padding > 0) {
            BufferedImage paddedImage = new BufferedImage(splitImage.getWidth() + padding * 2, splitImage.getHeight() + padding * 2,
                    page.getType());
            Graphics2D g2 = paddedImage.createGraphics();
            g2.drawImage(splitImage, padding, padding, null);
            g2.dispose();
            return paddedImage;
        } else {
            return splitImage;
        }
    }

    private BufferedImage extractNinePatch (BufferedImage page, TextureAtlas.TextureAtlasData.Region region, TenPatchVO vo) {
        BufferedImage splitImage = extractImage(page, region, NINEPATCH_PADDING);
        int width = splitImage.getWidth();
        int height = splitImage.getHeight();

        int[] horizontal = vo.horizontalStretchAreas == null ? new int[0] : vo.horizontalStretchAreas;
        int[] vertical = vo.verticalStretchAreas == null ? new int[0] : vo.verticalStretchAreas;

        // top border: horizontal stretch areas, bottom border: horizontal padding
        for (int i = 0; i + 1 < horizontal.length; i += 2) {
            for (int x = horizontal[i]; x <= horizontal[i + 1]; x++) {
                setBlack(splitImage, x + NINEPATCH_PADDING, 0);
            }
        }
        if (horizontal.length >= 2) {
            for (int x = horizontal[0]; x <= horizontal[horizontal.length - 1]; x++) {
                setBlack(splitImage, x + NINEPATCH_PADDING, height - 1);
            }
        }

        // left border: vertical stretch areas, right border: vertical padding.
        // Areas are counted from the bottom, image rows from the top.
        int contentHeight = region.height;
        for (int i = 0; i + 1 < vertical.length; i += 2) {
            for (int row = vertical[i]; row <= vertical[i + 1]; row++) {
                setBlack(splitImage, 0, contentHeight - 1 - row + NINEPATCH_PADDING);
            }
        }
        if (vertical.length >= 2) {
            for (int row = vertical[0]; row <= vertical[vertical.length - 1]; row++) {
                setBlack(splitImage, width - 1, contentHeight - 1 - row + NINEPATCH_PADDING);
            }
        }

        return splitImage;
    }

    private static void setBlack(BufferedImage image, int x, int y) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return;
        image.setRGB(x, y, 0xFF000000);
    }

    public void saveImage(BufferedImage image, String path) {
        try {
            ImageIO.write(image, OUTPUT_TYPE, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
