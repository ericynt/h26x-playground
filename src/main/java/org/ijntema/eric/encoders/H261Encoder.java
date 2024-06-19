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
import org.ijntema.eric.model.frame.gob.macroblock.block.Block;

public class H261Encoder {

    private final String[] imagePaths;
    private Picture previousPicture;

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
                        for (int m = 0; m < Macroblock.Y_BLOCKS_AMOUNT; m++) {

                            picture.getGobs()[i][j].getMacroblocks()[k][l].getYBlocks()[m] = new Block();
                        }
                        picture.getGobs()[i][j].getMacroblocks()[k][l].setCbBlock(new Block());
                        picture.getGobs()[i][j].getMacroblocks()[k][l].setCrBlock(new Block());

                        Pair<Integer, Integer> startRowAndColumn = getMarcroblockStartRowAndColumn(i, j, k, l);

                        loadMacroblock(
                                startRowAndColumn,
                                picture.getGobs()[i][j].getMacroblocks()[k][l],
                                this.previousPicture,
                                rgbToYCbCr(bufferedImage)
                        );
                        encodeMacroblock(
                                startRowAndColumn,
                                picture.getGobs()[i][j].getMacroblocks()[k][l],
                                this.previousPicture
                        );
                    }
                }
            }
        }

        this.previousPicture = picture;

        return picture;
    }

    private void loadMacroblock (
            final Pair<Integer, Integer> pixelRowAndColumn,
            final Macroblock macroblock,
            final Picture previousPicture,
            final int[][][] yCbCr
    ) {

    }

    private void encodeMacroblock (
            final Pair<Integer, Integer> pixelRowAndColumn,
            final Macroblock macroblock,
            final Picture previousPicture
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
}
