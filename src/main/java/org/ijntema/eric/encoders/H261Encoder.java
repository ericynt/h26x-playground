package org.ijntema.eric.encoders;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.util.Pair;
import org.ijntema.eric.model.frame.FrameType;
import org.ijntema.eric.model.frame.Picture;
import org.ijntema.eric.model.frame.gob.GOB;
import org.ijntema.eric.model.frame.gob.macroblock.Macroblock;

public class H261Encoder {

    private final String[] imagePaths;
    private       Picture  previousPicture;

    public H261Encoder (final String[] imagePaths) {

        this.imagePaths = imagePaths;
    }

    public void encode () throws IOException {

        Picture[] pictures = createPictures(this.imagePaths);
    }

    private Picture[] createPictures (final String[] imagePaths) throws IOException {

        Picture[] pictures = new Picture[imagePaths.length];
        for (int i = 0; i < imagePaths.length; i++) {

            pictures[i] = createPicture(imagePaths[i], i);
        }

        return pictures;
    }

    private Picture createPicture (String imagePath, int pictureNumber) throws IOException {

        Picture picture = new Picture();
        picture.setFrameType(pictureNumber == 0 ? FrameType.I_FRAME : FrameType.P_FRAME);
        BufferedImage bufferedImage = loadImage(imagePath);

        for (int i = 0; i < Picture.GOB_ROWS; i++) {

            for (int j = 0; j < Picture.GOB_COLUMNS; j++) {

                picture.getGobs()[i][j] = new GOB();
                for (int k = 0; k < GOB.MACROBLOCK_ROWS; k++) {

                    for (int l = 0; l < GOB.MACROBLOCK_COLUMNS; l++) {

                        picture.getGobs()[i][j].getMacroblocks()[k][l] = new Macroblock();
                        Macroblock macroblock = picture.getGobs()[i][j].getMacroblocks()[k][l];
                        Macroblock previousMacroblock = this.previousPicture == null ? null : this.previousPicture.getGobs()[i][j].getMacroblocks()[k][l];

                        preprocess(
                                getMarcroblockStartRowAndColumn(i, j, k, l),
                                macroblock,
                                previousMacroblock,
                                picture.getFrameType(),
                                rgbToYCbCr(bufferedImage)
                        );
                        encodeMacroblock(
                                macroblock.getBlocks(),
                                previousMacroblock.getBlocks()
                        );
                    }
                }
            }
        }

        this.previousPicture = picture;

        return picture;
    }

    private void preprocess (
            final Pair<Integer, Integer> pixelRowAndColumn,
            final Macroblock macroblock,
            final Macroblock previousMacroblock,
            final FrameType frameType,
            final int[][][] yCbCr
    ) {

        for (int i = pixelRowAndColumn.getKey(); i < pixelRowAndColumn.getKey() + 16; i++) {

            for (int j = pixelRowAndColumn.getValue(); j < pixelRowAndColumn.getValue() + 16; j++) {

                boolean isPFrame = frameType == FrameType.P_FRAME;
                // Y
                int[][][] blocks = macroblock.getBlocks();
                if (i < 8 && j < 8) {

                    blocks[0][i][j] = yCbCr[i][j][0];

                    // Calculate diff. if it's a P-frame
                    if (isPFrame) {

                        blocks[0][i][j] -= previousMacroblock.getBlocks()[0][i][j];
                        updateDiff(macroblock, blocks[0][i][j]);
                    }
                } else if (i < 8) {

                    blocks[1][i][j - 8] = yCbCr[i][j][0];

                    if (isPFrame) {

                        blocks[1][i][j - 8] -= previousMacroblock.getBlocks()[1][i][j - 8];
                        updateDiff(macroblock, blocks[1][i][j - 8]);
                    }
                } else if (j < 8) {

                    blocks[2][i - 8][j] = yCbCr[i][j][0];

                    if (isPFrame) {

                        blocks[2][i - 8][j] -= previousMacroblock.getBlocks()[2][i - 8][j];
                        updateDiff(macroblock, blocks[2][i - 8][j]);
                    }
                } else {

                    blocks[3][i - 8][j - 8] = yCbCr[i][j][0];

                    if (isPFrame) {

                        blocks[3][i - 8][j - 8] -= previousMacroblock.getBlocks()[3][i - 8][j - 8];
                        updateDiff(macroblock, blocks[3][i - 8][j - 8]);
                    }
                }

                // CbCr
                if (i % 2 == 0 && j % 2 == 0) {

                    blocks[4][i / 2][j / 2] = yCbCr[i][j][1];
                    blocks[5][i / 2][j / 2] = yCbCr[i][j][2];

                    if (isPFrame) {

                        blocks[4][i / 2][j / 2] -= previousMacroblock.getBlocks()[4][i / 2][j / 2];
                        blocks[5][i / 2][j / 2] -= previousMacroblock.getBlocks()[5][i / 2][j / 2];
                        updateDiff(macroblock, blocks[4][i / 2][j / 2]);
                        updateDiff(macroblock, blocks[5][i / 2][j / 2]);
                    }
                }
            }
        }
    }

    private void encodeMacroblock (
            int[][][] currentMacroblock,
            int[][][] previousMacroblock
    ) {

    }

    private Pair<Integer, Integer> getMarcroblockStartRowAndColumn (
            final int gobRow,
            final int gobColumn,
            final int macroblockRow,
            final int macroblockColumn
    ) {

        return new Pair<>(
                (gobRow * macroblockRow) + macroblockRow,
                (gobColumn * macroblockColumn) + macroblockColumn
        );
    }

    private BufferedImage loadImage (String imagePath) throws IOException {

        return ImageIO.read(new File(imagePath));
    }

    private int[][][] rgbToYCbCr (BufferedImage image) {

        int[][][] yCbCr = new int[Picture.HEIGHT][Picture.WIDTH][3];
        for (int y = 0; y < Picture.HEIGHT; y++) {

            for (int x = 0; x < Picture.WIDTH; x++) {

                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                yCbCr[y][x][0] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                yCbCr[y][x][1] = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                yCbCr[y][x][2] = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
            }
        }

        return yCbCr;
    }

    private static void updateDiff (final Macroblock macroblock, int value) {

        if (value != 0) {

            macroblock.setDifferent(true);
        }
    }
}
