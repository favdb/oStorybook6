/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.db.book;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import storybook.tools.ListUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorUtil;
import static storybook.tools.xml.XmlUtil.getString;

/**
 *
 * @author favdb
 */
public class WEB_SUMMARY {

	public boolean titled = false, chapters = false;
	public String font = "serif";
	public Color color = Color.BLACK;
	public List<WEB_SUMMARY_H> h;

	public WEB_SUMMARY() {
		h = new ArrayList<>();
		h.add(new WEB_SUMMARY_H());
		h.add(new WEB_SUMMARY_H());
		h.add(new WEB_SUMMARY_H());
		h.add(new WEB_SUMMARY_H());
		h.add(new WEB_SUMMARY_H());
		h.add(new WEB_SUMMARY_H());
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public WEB_SUMMARY(Node node) {
		this();
		String x[] = getString(node, "summary").split(",");
		setTitled("1".equals(x[0]));
		setChapters("1".equals(x[1]));
		setColor(x[2]);
		for (int i = 0; i < 6; i++) {
			String z = getString(node, "h" + (i + 1));
			if (!z.isEmpty()) {
				h.set(i, new WEB_SUMMARY_H(z));
			}
		}
	}

	public String getTest() {
		StringBuilder b = new StringBuilder(Html.HTML_B).append(Html.BODY_B);
		for (int i = 0; i < 6; i++) {
			b.append(Html.intoH(i + 1, "Title " + (i + 1), getCss(i, true)));
		}
		b.append(Html.BODY_E).append(Html.HTML_E);
		return b.toString();
	}

	public WEB_SUMMARY_H getH(int n) {
		return h.get(n);
	}

	public void setH(int i, String str) {
		WEB_SUMMARY_H v = new WEB_SUMMARY_H(str);
		setH(i, v);
	}

	public void setH(int n, WEB_SUMMARY_H v) {
		h.set(n, v);
	}

	public static String getAdvancedStyle(BookParamWeb web, boolean coloronly) {
		StringBuilder b = new StringBuilder();
		WEB_SUMMARY_H hcss = web.getSummary().h.get(0);
		if (!coloronly) {
			b.append("font-family: ").append(hcss.getFamily()).append("; ");
			b.append("font-size: ").append(hcss.getSize()).append("%; ");
			if ("bold".equals(hcss.getStyle())) {
				b.append("font-weight: bold; ");
			} else {
				b.append("font-weight: normal; ");
			}
			if ("italic".equals(hcss.getStyle())) {
				b.append("font-style: italic; ");
			}
		} else {
			b.append("font-family: ").append(hcss.getFamily()).append("; ");
		}
		b.append("color: ").append(hcss.getColor()).append("; ");
		b.append("background: ").append(hcss.getBkColor()).append("; ");
		return b.toString();
	}

	public String getCss(int n, boolean margin) {
		StringBuilder b = new StringBuilder();
		WEB_SUMMARY_H hcss = h.get(n);
		b.append("font-family: ").append(hcss.getFamily()).append("; ");
		b.append("font-size: ").append(hcss.getSize()).append("%; ");
		if ("bold".equals(hcss.getStyle())) {
			b.append("font-weight: bold; ");
		} else {
			b.append("font-weight: normal; ");
		}
		if ("italic".equals(hcss.getStyle())) {
			b.append("font-style: italic; ");
		}
		b.append("color: ").append(hcss.getColor()).append("; ");
		b.append("background: ").append(hcss.getBkColor()).append("; ");
		if (margin) {
			if (n == 0) {
				b.append("margin: 0pt; border: 2px solid #f5f5dc; border-radius: 5px;");
			} else {
				Integer v = n * 10;
				b.append("margin: 0pt 0pt 1pt ").append(v.toString()).append("pt; ");
			}
		}
		return b.toString();
	}

	public String getHtml(int n) {
		StringBuilder b = new StringBuilder();
		WEB_SUMMARY_H hcss = h.get(n);
		b.append("font-family: ").append(hcss.getFamily()).append("; ");
		b.append("font-size: ").append(hcss.getSize()).append("%; ");
		if ("bold".equals(hcss.getStyle())) {
			b.append("font-weight: bold; ");
		} else {
			b.append("font-style: ").append(hcss.getStyle()).append("; ");
		}
		b.append("color: ").append(hcss.getColor()).append("; ");
		b.append("background: ").append(hcss.getBkColor()).append("; ");
		String z = hcss.getFamily() + " " + hcss.getSize() + "% " + hcss.getStyle();
		return Html.HTML_B + Html.intoP(z, getCss(n, false)) + Html.HTML_E;
	}

	public boolean getTitled() {
		return titled;
	}

	public void setTitled(boolean v) {
		this.titled = v;
	}

	public boolean getChapters() {
		return chapters;
	}

	public void setChapters(boolean v) {
		this.chapters = v;
	}

	public Color getColor() {
		return (color == null ? Color.LIGHT_GRAY : color);
	}

	public void setColor(String b) {
		color = ColorUtil.fromHexString(b);
	}

	public String getColorStr() {
		return ColorUtil.getHTML(color);
	}

	public void setFont(String v) {
		this.font = v;
	}

	public String getFont() {
		return font;
	}

	private String toXmlAttr(String key, String value) {
		return key + "=\"" + value + "\" ";
	}

	private String toXmlAttr(String key, boolean value) {
		return key + "=\"" + (value ? "1" : "0") + "\" ";
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		List<String> bv = new ArrayList<>();
		bv.add((titled ? "1" : "0"));
		bv.add((chapters ? "1" : "0"));
		bv.add(getColorStr());
		b.append(toXmlAttr(" summary", ListUtil.join(bv)));
		for (int i = 0; i < h.size(); i++) {
			b.append(toXmlAttr(" h" + (i + 1), h.get(i).toXml()));
		}
		return b.toString();
	}

	public static class WEB_SUMMARY_H {

		private String family = "serif",
			size = "100",
			style = "normal",
			color = "#000000",
			bkcolor = "#FFFFFF";

		public WEB_SUMMARY_H() {
		}

		public WEB_SUMMARY_H(String family, String size, String style, String color, String bkcolor) {
			this.family = family;
			this.size = size;
			this.style = style;
			this.color = color;
			this.bkcolor = bkcolor;
		}

		public WEB_SUMMARY_H(String str) {
			String v[] = str.split(",");
			this.family = v[0];
			this.size = v[1];
			this.style = v[2];
			this.color = v[3];
			this.bkcolor = v[4];
		}

		public String getBkColor() {
			return bkcolor;
		}

		public void setBkColor(String value) {
			this.bkcolor = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String value) {
			this.color = value;
		}

		public String getFamily() {
			return family;
		}

		public void setFamily(String v) {
			this.family = v;
		}

		public String getSize() {
			return (size == null ? "100" : size);
		}

		public void setSize(String v) {
			this.size = v;
		}

		public String getStyle() {
			return (style == null ? "normal" : style);
		}

		public void setStyle(String v) {
			this.style = v;
		}

		public String toXml() {
			return getFamily() + "," + getSize() + "," + getStyle() + "," + getColor() + "," + getBkColor();
		}

	}
}
