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

import static storybook.exim.exporter.AbstractExport.*;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author favdb
 */
public class BookParamWeb extends BookParamAbstract {

	private WEB_SUMMARY summary;

	public enum KW {
		BANNER, SUMMARY, AUTHOR, COPYRIGHT;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private boolean banner = false, bAuthor = false, bCopyright = false;
	private String bannerImg = "";

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamWeb(BookParam param) {
		super(param, "web");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("web");
		}
		init();
	}

	@Override
	protected void init() {
		if (node != null) {
			String x[] = getString(node, KW.BANNER.toString()).split(",");
			setBanner("1".equals(x[0]));
			if (x.length > 1) {
				setBannerImg(x[1]);
			}
			setSummary(new WEB_SUMMARY(node));
			bAuthor = getBoolean(node, KW.AUTHOR.toString());
			bCopyright = getBoolean(node, KW.COPYRIGHT.toString());
		} else {
			summary = new WEB_SUMMARY();
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder("        <web ");
		// banner
		StringBuilder x = new StringBuilder((getBanner() ? "1" : "0"));
		if (getBanner()) {
			x.append(",").append(getBannerImg());
		}
		b.append(stringAttribute(0, KW.BANNER.toString(), x.toString()));
		b.append(stringAttribute(0, KW.AUTHOR.toString(), (bAuthor ? "1" : "0")));
		b.append(stringAttribute(0, KW.COPYRIGHT.toString(), (bCopyright ? "1" : "0")));
		// summary
		b.append(summary.toXml());
		b.append(" />\n");
		return b.toString();
	}

	public boolean getBanner() {
		return banner;
	}

	public void setBanner(boolean b) {
		banner = b;
	}

	public String getBannerImg() {
		return bannerImg;
	}

	public void setBannerImg(String s) {
		bannerImg = s;
	}

	public void setSummary(WEB_SUMMARY b) {
		summary = b;
	}

	public WEB_SUMMARY getSummary() {
		return summary;
	}

	public void setAuthor(boolean value) {
		this.bAuthor = value;
	}

	public boolean getAuthor() {
		return bAuthor;
	}

	public void setCopyright(boolean value) {
		this.bCopyright = value;
	}

	public boolean getCopyright() {
		return bCopyright;
	}

}
