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

package games.rednblack.editor.utils;

import com.badlogic.gdx.utils.IntArray;
import games.rednblack.editor.renderer.data.TenPatchVO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

/**
 * Reads and writes the 1px border of Android style {@code .9.png} files. Unlike libGDX's
 * {@code TexturePacker}, every black segment of the top and left border is kept, so a file can
 * describe multiple stretch areas per axis (the TenPatch format).
 *
 * Created by sargis on 8/29/14.
 */
public class NinePatchUtils {

    private static final int BLACK = 0xFF000000;

    /**
     * Stretch segments of a {@code .9.png}, in pixels of the content (border stripped).
     * Every array holds pairs of inclusive indexes in ascending order.
     */
    public static class Patches {
        /** Pairs from the left of the graphic. */
        public int[] horizontal;
        /** Pairs from the top of the graphic, image coordinates. */
        public int[] vertical;

        public Patches(int[] horizontal, int[] vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }

    /**
     * Reads the stretch segments from the top row and left column of a {@code .9.png} image.
     * A border without any black pixel means the whole axis stretches.
     */
    public static Patches findPatches(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] row = getPixels(image, 0, 0, width, 1, new int[width]);
        int[] column = getPixels(image, 0, 0, 1, height, new int[height]);

        return new Patches(getSegments(row), getSegments(column));
    }

    /** Converts border segments to a {@link TenPatchVO}, flipping the vertical axis to be bottom based. */
    public static TenPatchVO toTenPatchVO(Patches patches, int contentHeight) {
        int[] vertical = new int[patches.vertical.length];
        for (int i = 0; i + 1 < patches.vertical.length; i += 2) {
            int start = patches.vertical[i];
            int end = patches.vertical[i + 1];
            // last pair in image coordinates becomes first pair in bottom based coordinates
            int target = patches.vertical.length - 2 - i;
            vertical[target] = contentHeight - 1 - end;
            vertical[target + 1] = contentHeight - 1 - start;
        }
        return new TenPatchVO(patches.horizontal.clone(), vertical);
    }

    /** Converts a {@link TenPatchVO} (bottom based vertical areas) to border segments (top based). */
    public static Patches fromTenPatchVO(TenPatchVO vo, int contentHeight) {
        int[] source = vo.verticalStretchAreas == null ? new int[0] : vo.verticalStretchAreas;
        int[] vertical = new int[source.length];
        for (int i = 0; i + 1 < source.length; i += 2) {
            int start = source[i];
            int end = source[i + 1];
            int target = source.length - 2 - i;
            vertical[target] = contentHeight - 1 - end;
            vertical[target + 1] = contentHeight - 1 - start;
        }
        int[] horizontal = vo.horizontalStretchAreas == null ? new int[0] : vo.horizontalStretchAreas.clone();
        return new Patches(horizontal, vertical);
    }

    /**
     * Reads a {@code .9.png} file and returns its stretch areas as a {@link TenPatchVO}.
     *
     * @return null when the file cannot be read
     */
    public static TenPatchVO readTenPatchVO(File ninePatchFile) {
        try {
            BufferedImage image = ImageIO.read(ninePatchFile);
            if (image == null || image.getWidth() < 3 || image.getHeight() < 3) return null;
            return toTenPatchVO(findPatches(image), image.getHeight() - 2);
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage removePatches(BufferedImage image) {
        BufferedImage buffer = createTranslucentCompatibleImage(
                image.getWidth() - 2, image.getHeight() - 2);

        Graphics2D g2 = buffer.createGraphics();
        g2.drawImage(image, -1, -1, null);
        g2.dispose();
        return buffer;
    }

    /**
     * Wraps content in a 1px border and draws the given segments scaled by {@code ratio}.
     * Top and left borders carry the stretch areas, bottom and right borders the padding, which spans
     * from the first to the last stretch area.
     */
    public static BufferedImage convertTo9Patch(BufferedImage image, Patches patches, float ratio) {
        BufferedImage buffer = createTranslucentCompatibleImage(image.getWidth() + 2, image.getHeight() + 2);
        Graphics2D g2 = buffer.createGraphics();
        g2.drawImage(image, 1, 1, null);
        g2.dispose();
        draw9Patch(buffer, patches, ratio);
        return buffer;
    }

    /** Draws the 9-patch border on {@code image} (which already includes the 1px border). */
    public static void draw9Patch(BufferedImage image, Patches patches, float ratio) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] horizontal = scaleSegments(patches.horizontal, ratio, width - 2);
        int[] vertical = scaleSegments(patches.vertical, ratio, height - 2);

        for (int i = 0; i + 1 < horizontal.length; i += 2) {
            for (int x = horizontal[i]; x <= horizontal[i + 1]; x++) {
                image.setRGB(x + 1, 0, BLACK);
            }
        }
        if (horizontal.length >= 2) {
            for (int x = horizontal[0]; x <= horizontal[horizontal.length - 1]; x++) {
                image.setRGB(x + 1, height - 1, BLACK);
            }
        }

        for (int i = 0; i + 1 < vertical.length; i += 2) {
            for (int y = vertical[i]; y <= vertical[i + 1]; y++) {
                image.setRGB(0, y + 1, BLACK);
            }
        }
        if (vertical.length >= 2) {
            for (int y = vertical[0]; y <= vertical[vertical.length - 1]; y++) {
                image.setRGB(width - 1, y + 1, BLACK);
            }
        }
    }

    /**
     * Scales segment pairs keeping them inside {@code [0, size - 1]}, ordered and non overlapping.
     * Segments collapsed by the scaling are dropped.
     */
    public static int[] scaleSegments(int[] segments, float ratio, int size) {
        if (segments == null) return new int[0];
        IntArray result = new IntArray(segments.length);
        int previousEnd = -1;
        for (int i = 0; i + 1 < segments.length; i += 2) {
            int start = Math.round(segments[i] * ratio);
            int end = Math.round((segments[i + 1] + 1) * ratio) - 1;
            start = Math.max(start, previousEnd + 1);
            end = Math.min(Math.max(end, start), size - 1);
            if (start > size - 1 || start > end) continue;
            result.add(start);
            result.add(end);
            previousEnd = end;
        }
        return result.toArray();
    }

    private static GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return environment.getDefaultScreenDevice().getDefaultConfiguration();
    }

    private static BufferedImage createTranslucentCompatibleImage(int width, int height) {
        return getGraphicsConfiguration().createCompatibleImage(width, height,
                Transparency.TRANSLUCENT);
    }

    private static int[] getPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
        if (w == 0 || h == 0) {
            return new int[0];
        }

        if (pixels == null) {
            pixels = new int[w * h];
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("Pixels array must have a length >= w * h");
        }

        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
            Raster raster = img.getRaster();
            return (int[]) raster.getDataElements(x, y, w, h, pixels);
        }

        // Unmanages the image
        return img.getRGB(x, y, w, h, pixels, 0, w);
    }

    /**
     * Runs of black pixels along a border line (corners excluded), converted to content coordinates.
     * When the line has no black pixel at all, the whole content stretches.
     */
    private static int[] getSegments(int[] pixels) {
        IntArray segments = new IntArray();
        int start = -1;
        for (int i = 1; i < pixels.length - 1; i++) {
            boolean black = pixels[i] == BLACK;
            if (black && start < 0) {
                start = i;
            } else if (!black && start >= 0) {
                segments.add(start - 1);
                segments.add(i - 2);
                start = -1;
            }
        }
        if (start >= 0) {
            segments.add(start - 1);
            segments.add(pixels.length - 3);
        }

        if (segments.size == 0 && pixels.length > 2) {
            segments.add(0);
            segments.add(pixels.length - 3);
        }
        return segments.toArray();
    }
}
