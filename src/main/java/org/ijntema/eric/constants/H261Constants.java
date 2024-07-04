package org.ijntema.eric.constants;

import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;

public class H261Constants {

    public static final int                                                QUANT                        = 31;
    public static final int                                                PICTURE_WIDTH                = 352;
    public static final int                                                PICTURE_HEIGHT               = 288;
    public static final int                                                GOB_ROWS                     = 6;
    public static final int                                                GOB_COLUMNS                  = 2;
    public static final int                                                MACROBLOCK_ROWS              = 3;
    public static final int                                                MACROBLOCK_COLUMNS           = 11;
    public static final int                                                Y_BLOCKS_AMOUNT              = 4;
    public static final int                                                CB_BLOCKS_AMOUNT             = 1;
    public static final int                                                CR_BLOCKS_AMOUNT             = 1;
    // YCbCr 4:2:0
    public static final int                                                TOTAL_BLOCKS                 = Y_BLOCKS_AMOUNT + CB_BLOCKS_AMOUNT + CR_BLOCKS_AMOUNT;
    public static final int                                                BLOCK_SIZE                   = 8;
    public static final Map<Integer, Pair<Integer, Integer>>               VLC_TABLE_MACROBLOCK_ADDRESS = new HashMap<>();
    public static final Map<Integer, Map<Integer, Pair<Integer, Integer>>> VLC_TABLE_TCOEFF;
    public static final int[][]                                            ZIGZAG_ORDER                 =
            {
                    { 0,  1,  5,  6, 14, 15, 27, 28},
                    { 2,  4,  7, 13, 16, 26, 29, 42},
                    { 3,  8, 12, 17, 25, 30, 41, 43},
                    { 9, 11, 18, 24, 31, 40, 44, 53},
                    {10, 19, 23, 32, 39, 45, 52, 54},
                    {20, 22, 33, 38, 46, 51, 55, 60},
                    {21, 34, 37, 47, 50, 56, 59, 61},
                    {35, 36, 48, 49, 57, 58, 62, 63}
            };

    public static final int[] TEST_MATRIX =
            {
                    254, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    };

    static {

        VLC_TABLE_MACROBLOCK_ADDRESS.put(1, new Pair<>(0b1, 1));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(2, new Pair<>(0b011, 3));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(3, new Pair<>(0b010, 3));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(4, new Pair<>(0b011, 4));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(5, new Pair<>(0b010, 4));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(6, new Pair<>(0b00011, 5));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(7, new Pair<>(0b00010, 5));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(8, new Pair<>(0b0000_111, 7));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(9, new Pair<>(0b0000_110, 7));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(10, new Pair<>(0b0000_1011, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(11, new Pair<>(0b0000_1010, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(12, new Pair<>(0b0000_1001, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(13, new Pair<>(0b0000_1000, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(14, new Pair<>(0b0000_0111, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(15, new Pair<>(0b0000_0110, 8));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(16, new Pair<>(0b0000_0101_11, 10));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(17, new Pair<>(0b0000_0101_10, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(18, new Pair<>(0b0000_0101_01, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(19, new Pair<>(0b0000_0101_00, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(20, new Pair<>(0b0000_0100_11, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(21, new Pair<>(0b0000_0100_10, 10));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(22, new Pair<>(0b0000_0100_011, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(23, new Pair<>(0b0000_0100_010, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(24, new Pair<>(0b0000_0100_001, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(25, new Pair<>(0b0000_0100_000, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(26, new Pair<>(0b0000_0011_111, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(27, new Pair<>(0b0000_0011_110, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(28, new Pair<>(0b0000_0011_101, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(29, new Pair<>(0b0000_0011_100, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(30, new Pair<>(0b0000_0011_011, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(31, new Pair<>(0b0000_0011_010, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(32, new Pair<>(0b0000_0011_001, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(33, new Pair<>(0b0000_0011_000, 11));


        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff0 = new HashMap<>();
//        vlcTableTcoeff0.put(-1, new Pair<>(0b1, 1)); // Not used yet
        vlcTableTcoeff0.put(-1, new Pair<>(0b11, 2));
        vlcTableTcoeff0.put(2, new Pair<>(0b0100, 4));
        vlcTableTcoeff0.put(-3, new Pair<>(0b0010_1, 5));
        vlcTableTcoeff0.put(4, new Pair<>(0b0000_110, 7));
        vlcTableTcoeff0.put(5, new Pair<>(0b0010_0110, 8));
        vlcTableTcoeff0.put(-6, new Pair<>(0b0010_0001, 8));
        vlcTableTcoeff0.put(7, new Pair<>(0b0000_0010_10, 10));
        vlcTableTcoeff0.put(-8, new Pair<>(0b0000_0001_1101, 12));
        vlcTableTcoeff0.put(9, new Pair<>(0b0000_0001_1000, 12));
        vlcTableTcoeff0.put(-10, new Pair<>(0b0000_0001_0011, 12));
        vlcTableTcoeff0.put(11, new Pair<>(0b0000_0001_0000, 12));
        vlcTableTcoeff0.put(12, new Pair<>(0b0000_0000_1101_0, 13));
        vlcTableTcoeff0.put(-13, new Pair<>(0b0000_0000_1100_1, 13));
        vlcTableTcoeff0.put(14, new Pair<>(0b0000_0000_1100_0, 13));
        vlcTableTcoeff0.put(-15, new Pair<>(0b0000_0000_1011_1, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff1 = new HashMap<>();
        vlcTableTcoeff1.put(-1, new Pair<>(0b011, 3));
        vlcTableTcoeff1.put(2, new Pair<>(0b0001_10, 6));
        vlcTableTcoeff1.put(-3, new Pair<>(0b0010_0101, 8));
        vlcTableTcoeff1.put(4, new Pair<>(0b0000_0011_00, 10));
        vlcTableTcoeff1.put(-5, new Pair<>(0b0000_0001_1011, 12));
        vlcTableTcoeff1.put(6, new Pair<>(0b0000_0000_1011_0, 13));
        vlcTableTcoeff1.put(-7, new Pair<>(0b0000_0000_1010_1, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff2 = new HashMap<>();
        vlcTableTcoeff2.put(-1, new Pair<>(0b0101, 4));
        vlcTableTcoeff2.put(2, new Pair<>(0b0000_100, 7));
        vlcTableTcoeff2.put(-3, new Pair<>(0b0000_0010_11, 10));
        vlcTableTcoeff2.put(4, new Pair<>(0b0000_0001_0100, 12));
        vlcTableTcoeff2.put(5, new Pair<>(0b0000_0000_1010_0, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff3 = new HashMap<>();
        vlcTableTcoeff3.put(-1, new Pair<>(0b0011_1, 5));
        vlcTableTcoeff3.put(2, new Pair<>(0b0010_0100, 8));
        vlcTableTcoeff3.put(3, new Pair<>(0b0000_0001_1100, 12));
        vlcTableTcoeff3.put(-4, new Pair<>(0b0000_0000_1001_1, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff4 = new HashMap<>();
        vlcTableTcoeff4.put(1, new Pair<>(0b0011_0, 5));
        vlcTableTcoeff4.put(-2, new Pair<>(0b0000_0011_11, 10));
        vlcTableTcoeff4.put(3, new Pair<>(0b0000_0001_0010, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff5 = new HashMap<>();
        vlcTableTcoeff5.put(-1, new Pair<>(0b0001_11, 6));
        vlcTableTcoeff5.put(-2, new Pair<>(0b0000_0010_01, 10));
        vlcTableTcoeff5.put(3, new Pair<>(0b0000_0000_1001_0, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff6 = new HashMap<>();
        vlcTableTcoeff6.put(-1, new Pair<>(0b0001_01, 6));
        vlcTableTcoeff6.put(2, new Pair<>(0b0000_0001_1110, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff7 = new HashMap<>();
        vlcTableTcoeff7.put(1, new Pair<>(0b0001_00, 6));
        vlcTableTcoeff7.put(-2, new Pair<>(0b0000_0001_0101, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff8 = new HashMap<>();
        vlcTableTcoeff8.put(-1, new Pair<>(0b0000_111, 7));
        vlcTableTcoeff8.put(-2, new Pair<>(0b0000_0001_0001, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff9 = new HashMap<>();
        vlcTableTcoeff9.put(-1, new Pair<>(0b0000_101, 7));
        vlcTableTcoeff9.put(-2, new Pair<>(0b0000_0000_1000_1, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff10 = new HashMap<>();
        vlcTableTcoeff10.put(-1, new Pair<>(0b0010_0111, 8));
        vlcTableTcoeff10.put(2, new Pair<>(0b0000_0000_1000_0, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff11 = new HashMap<>();
        vlcTableTcoeff11.put(-1, new Pair<>(0b0010_0011, 8));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff12 = new HashMap<>();
        vlcTableTcoeff12.put(1, new Pair<>(0b0010_0010, 8));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff13 = new HashMap<>();
        vlcTableTcoeff13.put(1, new Pair<>(0b0010_0000, 8));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff14 = new HashMap<>();
        vlcTableTcoeff14.put(1, new Pair<>(0b0000_0011_10, 10));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff15 = new HashMap<>();
        vlcTableTcoeff15.put(-1, new Pair<>(0b0000_0011_01, 10));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff16 = new HashMap<>();
        vlcTableTcoeff16.put(1, new Pair<>(0b0000_0010_00, 10));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff17 = new HashMap<>();
        vlcTableTcoeff17.put(-1, new Pair<>(0b0000_0001_1111, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff18 = new HashMap<>();
        vlcTableTcoeff18.put(1, new Pair<>(0b0000_0001_1010, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff19 = new HashMap<>();
        vlcTableTcoeff19.put(-1, new Pair<>(0b0000_0001_1001, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff20 = new HashMap<>();
        vlcTableTcoeff20.put(-1, new Pair<>(0b0000_0001_0111, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff21 = new HashMap<>();
        vlcTableTcoeff21.put(-1, new Pair<>(0b0000_0001_0110, 12));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff22 = new HashMap<>();
        vlcTableTcoeff22.put(-1, new Pair<>(0b0000_0000_1111_1, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff23 = new HashMap<>();
        vlcTableTcoeff23.put(1, new Pair<>(0b0000_0000_1111_0, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff24 = new HashMap<>();
        vlcTableTcoeff24.put(-1, new Pair<>(0b0000_0000_1110_1, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff25 = new HashMap<>();
        vlcTableTcoeff25.put(1, new Pair<>(0b0000_0000_1110_0, 13));

        Map<Integer, Pair<Integer, Integer>> vlcTableTcoeff26 = new HashMap<>();
        vlcTableTcoeff26.put(-1, new Pair<>(0b0000_0000_1101_1, 13));

        VLC_TABLE_TCOEFF = new HashMap<>();
        VLC_TABLE_TCOEFF.put(0, vlcTableTcoeff0);
        VLC_TABLE_TCOEFF.put(1, vlcTableTcoeff1);
        VLC_TABLE_TCOEFF.put(2, vlcTableTcoeff2);
        VLC_TABLE_TCOEFF.put(3, vlcTableTcoeff3);
        VLC_TABLE_TCOEFF.put(4, vlcTableTcoeff4);
        VLC_TABLE_TCOEFF.put(5, vlcTableTcoeff5);
        VLC_TABLE_TCOEFF.put(6, vlcTableTcoeff6);
        VLC_TABLE_TCOEFF.put(7, vlcTableTcoeff7);
        VLC_TABLE_TCOEFF.put(8, vlcTableTcoeff8);
        VLC_TABLE_TCOEFF.put(9, vlcTableTcoeff9);
        VLC_TABLE_TCOEFF.put(10, vlcTableTcoeff10);
        VLC_TABLE_TCOEFF.put(11, vlcTableTcoeff11);
        VLC_TABLE_TCOEFF.put(12, vlcTableTcoeff12);
        VLC_TABLE_TCOEFF.put(13, vlcTableTcoeff13);
        VLC_TABLE_TCOEFF.put(14, vlcTableTcoeff14);
        VLC_TABLE_TCOEFF.put(15, vlcTableTcoeff15);
        VLC_TABLE_TCOEFF.put(16, vlcTableTcoeff16);
        VLC_TABLE_TCOEFF.put(17, vlcTableTcoeff17);
        VLC_TABLE_TCOEFF.put(18, vlcTableTcoeff18);
        VLC_TABLE_TCOEFF.put(19, vlcTableTcoeff19);
        VLC_TABLE_TCOEFF.put(20, vlcTableTcoeff20);
        VLC_TABLE_TCOEFF.put(21, vlcTableTcoeff21);
        VLC_TABLE_TCOEFF.put(22, vlcTableTcoeff22);
        VLC_TABLE_TCOEFF.put(23, vlcTableTcoeff23);
        VLC_TABLE_TCOEFF.put(24, vlcTableTcoeff24);
        VLC_TABLE_TCOEFF.put(25, vlcTableTcoeff25);
        VLC_TABLE_TCOEFF.put(26, vlcTableTcoeff26);
    }
}
