package au.net.immortius.wardrobe.util;

import com.google.common.base.Preconditions;

/**
 * Utility for dealing with color hex codes
 */
public final class ColorUtil {
    private ColorUtil() {

    }

    /**
     * Converts from hex (e.g. #ffOOOO) to rgb (three channel 0-255)
     * @param hex The hex string
     * @return int[3] with values between 0 and 255 inclusive
     */
    public static int[] hexToRgb(String hex) {
        Preconditions.checkArgument(hex.length() == 7);
        int[] result = new int[3];
        result[0] =  Integer.valueOf( hex.substring( 1, 3 ), 16 );
        result[1] =  Integer.valueOf( hex.substring( 3, 5 ), 16 );
        result[2] =  Integer.valueOf( hex.substring( 5, 7 ), 16 );
        return result;
    }

    /**
     * Converts from rgb (three channel 0-255) to hex (e.g. #ff00ff)
     * @param rgb int[3] with values between 0 and 255 inclusive
     * @return A lower case hex code prefixed with '#'
     */
    public static String rgbToHex(int[] rgb) {
        Preconditions.checkArgument(rgb.length >= 3);
        return "#" + (colorChannelCode(rgb[0]) + colorChannelCode(rgb[1]) + colorChannelCode(rgb[2]));
    }

    private static String colorChannelCode(int colorByte) {
        String fullHex = "0" + Integer.toHexString(colorByte);
        return fullHex.substring(fullHex.length() - 2);
    }
}
