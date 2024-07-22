/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools.swing;

import java.awt.Color;

public class ColorUtil {

    public enum PALETTE {
	WHITE("0xFFFFFF"),
	LIGHT_BLUE("0xEFEFFF"),
	LIGHTSTEEL_BLUE("0xBCD2EE"),
	CORNFLOWER_BLUE("0x6495ED"),
	NAVY_BLUE("0x000080"),
	AZURE("0x007fff"),
	CYAN("0x00ffff"),
	BLUE("0x0000FF"),
	CADET_BLUE("0x5F9EA0"),
	//DARKSLATE_GRAY("0x2F4F4F"),
	GREY("0xBEBEBE"),
	DARK_GREY("0x828282"),
	LIGHT_GREY("0xDBDBDB"),
	//LIGHT_GREEN("0x8DB685"),
	//LIGHT_CYAN("0xE0FFFF"),
	//PALE_GREEN("0x90EE90"),
	YELLOW("0xFFFF00"),
	GOLD("0xFFD700"),
	BURLYWOOD("0xDEB887"),
	SANDY_BROWN("0xF4A460"),
	//ORCHID("0xDA70D6"),
	//ROSY_BROWN("0xEEB4B4"),
	ORANGE("0xFFA500"),
	LIGHT_RED("0xFFEFEF"),
	PEACH_PUFF("0xFFDAB9"),
	SALMON("0xfa8072"),
	PINK("0xffaca8"),
	//DEEP_PINK("0xFF1493"),
	PALE_TURQUOISE("0xAEEEEE"),
	JADE("0x87e990"),
	//NICE_GREEN("0x8FF88F"),
	OLIVE_DRAB("0x6B8E23"),
	STORYBOOK("0X70A167"),
	EMERALD("0x01d758"),
	CHAMPAGNE("0xfbf2b7"),
	SAFRAN("0xf3d617"),
	RUDDY("0xff0921"),
	//MAHOGANY("0x88421d"),
	MOKA("0xa38068"),
	//EGGPLANT("0x55205f"),
	PURPLE("0x9e0e40"),
	BLUE_VIOLET("0x8A2BE2"),
	VIOLET("0xc115e1"),
	FUSCHIA("0xff00ff"),
	LAVANDE("0xe6e6fa"),
	BLACK("0x000000");
	private final int val;

	private PALETTE(String text) {
	    this.val = Integer.decode(text);
	}

	public Color getColor() {
	    return new Color(val);
	}

	public int getRGB() {
	    return getColor().getRGB();
	}

	public Integer getValue() {
	    return (val);
	}

    }

    // some nice colors
    private static final int[] niceColors = {
	0xFFDAB9, // PEACH_PUFF
	0xBCD2EE, // LIGHTSTEEL_BLUE
	0x2F4F4F, // DARKSLATE_GRAY
	0xBEBEBE, // GREY
	0x828282, // GREY51
	0x000080, // NAVY_BLUE
	0x6495ED, // CORNFLOWER_BLUE
	0x5F9EA0, // CADET_BLUE
	0x90EE90, // PALE_GREEN2
	0xFFD700, // GOLD
	0xDEB887, // BURLYWOOD
	0xF4A460, // SANDY_BROWN
	0xDA70D6, // ORCHID
	0x8A2BE2, // BLUE_VIOLET
	0xAEEEEE, // PALE_TURQUOISE2
	0xEEB4B4, // ROSY_BROWN2
	0xFFA500, // ORANGE
	0x6B8E23, // OLIVE_DRAB
	0xFF1493, // DEEP_PINK
	0xFFFF00 // YELLOW
    };

    public static final Color[] intensityColors = {
	PALETTE.GREY.getColor(),
	PALETTE.EMERALD.getColor(),
	Color.YELLOW,
	Color.ORANGE,
	Color.RED
    };

    public static Color getIntensityColor(int i) {
	if (i < 0) {
	    return intensityColors[0];
	}
	if (i > 4) {
	    return intensityColors[4];
	}
	return intensityColors[i];
    }

    public static Color getNiceYellow() {
	return new Color(0xefefe0);
    }

    public static Color getNiceBlue() {
	return new Color(0xefefff);
    }

    public static Color getDarkBlue() {
	return new Color(0xefeff8);
    }

    public static Color getNiceRed() {
	return new Color(0xffefef);
    }

    public static Color getNiceGreen() {
	return new Color(0x8ff88f);
    }

    public static Color getDarkGreen() {
	return new Color(0x6ff06f);
    }

    public static Color getNiceDarkGray() {
	return new Color(0xdbdbdb);
    }

    public static Color getNiceGray() {
	return new Color(0xefefef);
    }

    public static Color[] getNiceColors() {
	Color[] colors = new Color[niceColors.length];
	for (int i = 0; i < niceColors.length; ++i) {
	    colors[i] = new Color(niceColors[i]);
	}
	return colors;
    }

    public static Color getPastel(Color color) {
	return ColorUtil.blend(Color.white, color);
    }

    public static Color getPastel2(Color color) {
	return ColorUtil.blend(Color.white, lighter(color, 0.1));
    }

    public static Color[] getLighterColors(Color[] colors, double fraction) {
	Color[] lightColors = new Color[colors.length];
	int i = 0;
	for (Color color : colors) {
	    lightColors[i] = lighter(color, fraction);
	    ++i;
	}
	return lightColors;
    }

    public static Color[] getDarkColors(Color[] colors, double fraction) {
	Color[] darkColors = new Color[colors.length];
	int i = 0;
	for (Color color : colors) {
	    darkColors[i] = darker(color, fraction);
	    ++i;
	}
	return darkColors;
    }

    public static Color[] getColorArray() {
	return new Color[]{Color.blue, Color.red, Color.green, Color.cyan,
	    Color.magenta, Color.orange, Color.pink, Color.yellow};
    }

    public static String getColorString(Color c) {
	return ("[r=" + c.getRed() + ", v=" + c.getGreen() + ", b=" + c.getBlue() + "]");
    }

    public static String getPaletteString(Color color) {
	for (PALETTE pc : PALETTE.values()) {
	    if (pc.getColor().equals(color)) {
		Color c = pc.getColor();
		return (pc.name() + " [r=" + c.getRed() + ", v=" + c.getGreen() + ", b=" + c.getBlue() + "]");
	    }
	}
	return ("[r=" + color.getRed() + ", v=" + color.getGreen() + ", b=" + color.getBlue() + "]");
    }

    public static Color[] getPastelColors() {
	return new Color[]{
	    new Color(0xFFBFBF), // red
	    new Color(0xFFE6BF), // orange
	    // new Color(0xFFFFBF), // yellow
	    new Color(0xCCFFBF), // green
	    new Color(0xBFCFFF), // blue
	    new Color(0xFFBFEF), // pink
	};
    }

    /**
     * Blend two colors.
     *
     * @param color1 First color to blend.
     * @param color2 Second color to blend.
     * @param ratio Blend ratio. 0.5 will give even blend, 1.0 will return color1, 0.0 will return
     * color2 and so on.
     * @return Blended color.
     */
    public static Color blend(Color color1, Color color2, double ratio) {
	float r = (float) ratio;
	float ir = (float) 1.0 - r;

	float rgb1[] = new float[3];
	float rgb2[] = new float[3];

	color1.getColorComponents(rgb1);
	color2.getColorComponents(rgb2);

	Color color = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r
		+ rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);

	return color;
    }

    /**
     * Make an even blend between two colors.
     *
     * @param color1 First color to blend.
     * @param color2 Second color to blend.
     * @return Blended color.
     */
    public static Color blend(Color color1, Color color2) {
	return ColorUtil.blend(color1, color2, 0.5);
    }

    /**
     * Make a color darker.
     *
     * @param color Color to make darker.
     * @param fraction Darkness fraction.
     * @return Darker color.
     */
    public static Color darker(Color color, double fraction) {
	int red = (int) Math.round(color.getRed() * (1.0 - fraction));
	int green = (int) Math.round(color.getGreen() * (1.0 - fraction));
	int blue = (int) Math.round(color.getBlue() * (1.0 - fraction));

	if (red < 0) {
	    red = 0;
	} else if (red > 255) {
	    red = 255;
	}
	if (green < 0) {
	    green = 0;
	} else if (green > 255) {
	    green = 255;
	}
	if (blue < 0) {
	    blue = 0;
	} else if (blue > 255) {
	    blue = 255;
	}

	int alpha = color.getAlpha();

	return new Color(red, green, blue, alpha);
    }

    /**
     * Make a color lighter.
     *
     * @param color Color to make lighter.
     * @param fraction Darkness fraction.
     * @return Lighter color.
     */
    public static Color lighter(Color color, double fraction) {
	int red = (int) Math.round(color.getRed() * (1.0 + fraction));
	int green = (int) Math.round(color.getGreen() * (1.0 + fraction));
	int blue = (int) Math.round(color.getBlue() * (1.0 + fraction));

	if (red < 0) {
	    red = 0;
	} else if (red > 255) {
	    red = 255;
	}
	if (green < 0) {
	    green = 0;
	} else if (green > 255) {
	    green = 255;
	}
	if (blue < 0) {
	    blue = 0;
	} else if (blue > 255) {
	    blue = 255;
	}

	int alpha = color.getAlpha();
	return new Color(red, green, blue, alpha);
    }

    public static String getHTML(Color color) {
	return "#" + getHexName(color);
    }

    /**
     * Return the hex name of a specified color.
     *
     * @param color Color to get hex name of.
     * @return Hex name of color: "rrggbb".
     */
    public static String getHexName(Color color) {
	if (color == null) {
	    return "ffffff";
	}
	int r = color.getRed();
	int g = color.getGreen();
	int b = color.getBlue();
	String rHex = Integer.toString(r, 16);
	String gHex = Integer.toString(g, 16);
	String bHex = Integer.toString(b, 16);
	return (rHex.length() == 2 ? "" + rHex : "0" + rHex)
		+ (gHex.length() == 2 ? "" + gHex : "0" + gHex)
		+ (bHex.length() == 2 ? "" + bHex : "0" + bHex);
    }

    /**
     * Return the "distance" between two colors. The rgb entries are taken to be coordinates in a 3D
     * space [0.0-1.0], and this method returnes the distance between the coordinates for the first
     * and second color.
     *
     * @param r1
     * @param g1
     * @param b1 First color
     * @param r2
     * @param g2
     * @param b2 Second color.
     * @return Distance bwetween colors.
     */
    public static double colorDistance(double r1, double g1, double b1,
	    double r2, double g2, double b2) {
	double a = r2 - r1;
	double b = g2 - g1;
	double c = b2 - b1;
	return Math.sqrt(a * a + b * b + c * c);
    }

    /**
     * Return the "distance" between two colors.
     *
     * @param color1 First color [r,g,b].
     * @param color2 Second color [r,g,b].
     * @return Distance bwetween colors.
     */
    public static double colorDistance(double[] color1, double[] color2) {
	return ColorUtil.colorDistance(color1[0], color1[1], color1[2],
		color2[0], color2[1], color2[2]);
    }

    /**
     * Return the "distance" between two colors.
     *
     * @param color1 First color.
     * @param color2 Second color.
     * @return Distance between colors.
     */
    public static double colorDistance(Color color1, Color color2) {
	float rgb1[] = new float[3];
	float rgb2[] = new float[3];
	color1.getColorComponents(rgb1);
	color2.getColorComponents(rgb2);
	return ColorUtil.colorDistance(rgb1[0], rgb1[1], rgb1[2], rgb2[0],
		rgb2[1], rgb2[2]);
    }

    /**
     * Check if a color is more dark than light. Useful if an entity of this color is to be labeled:
     * Use white label on a "dark" color and black label on a "light" color.
     *
     * @param r
     * @param g
     * @param b Color to check.
     * @return True if this is a "dark" color, false otherwise.
     */
    public static boolean isDark(double r, double g, double b) {
	// measure distance to white and black respectively
	double dWhite = ColorUtil.colorDistance(r, g, b, 1.0, 1.0, 1.0);
	double dBlack = ColorUtil.colorDistance(r, g, b, 0.0, 0.0, 0.0);
	return dBlack < dWhite;
    }

    /**
     * Check if a color is more dark than light. Useful if an entity of this color is to be labeled:
     * Use white label on a "dark" color and black label on a "light" color.
     *
     * @param color Color to check.
     * @return True if this is a "dark" color, false otherwise.
     */
    public static boolean isDark(Color color) {
	double c = (0.3 * color.getRed()) + (0.59 * color.getGreen()) + (0.11 * color.getBlue());
	return (c < 128);
    }

    public static boolean isLight(Color c) {
	return Math.sqrt(
		c.getRed() * c.getRed() * .241
		+ c.getGreen() * c.getGreen() * .691
		+ c.getBlue() * c.getBlue() * .068) < 130;
    }

    public static Color getFontColor(Color color) {
	double c = (0.3 * color.getRed()) + (0.59 * color.getGreen()) + (0.11 * color.getBlue());
	if (c >= 128) {
	    return Color.BLACK;
	} else {
	    return Color.GRAY;
	}
    }

    /**
     * Creates a {@code String} that represents the supplied color as a hex-value RGB triplet,
     * including the "#". The return value is suitable for use in HTML. The alpha (transparency)
     * channel is neither include nor used in producing the string.
     *
     * @param color the color to convert
     * @return the hex {@code String}
     */
    public static String toHexString(Color color) {
	return "#" + Integer.toHexString(color.getRGB() | 0xFF000000).substring(2);
    }

    public static Color fromHexString(String color) {
	if (color == null || color.isEmpty() || !color.startsWith("#")) {
	    return (Color.BLACK);
	}
	return (Color.decode(color));
    }
}
