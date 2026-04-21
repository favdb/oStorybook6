/*
 * Copyright (C) 2026 favdb
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

import storybook.tools.xml.XmlUtil;

/**
 * manage save/restore filters for tables
 *
 * @author favdb
 */
public class BookParamFilters extends BookParamAbstract {

	private static final String TT = "ProjectParam.";
	//parts no filter
	//chapters no filter
	//scenes
	private int scenesStatus = -1, scenesStrand = -1, scenesNarrator = -1;
	//plots
	private int plotsCat = -1;
	//items
	private int itemsCat = -1;
	//events
	private int eventsCat = -1;
	//tags
	private int tagsCat = -1;
	//ideas
	private int ideasCat = -1;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamFilters(BookParam param) {
		super(param, "export");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("filters");
			init();
		}
	}

	public int getScenesStatus() {
		return scenesStatus;
	}

	public void setScenesStatus(int value) {
		this.scenesStatus = value;
	}

	public int getScenesStrand() {
		return scenesStrand;
	}

	public void setScenesStrand(int value) {
		this.scenesStrand = value;
	}

	public int getScenesNarrator() {
		return scenesNarrator;
	}

	public void setScenesNarrator(int value) {
		this.scenesNarrator = value;
	}

	public int getPlotsCat() {
		return plotsCat;
	}

	public void setPlotsCat(int value) {
		this.plotsCat = value;
	}

	public int getItemsCat() {
		return itemsCat;
	}

	public void setItemsCat(int value) {
		this.itemsCat = value;
	}

	public int getEventsCat() {
		return eventsCat;
	}

	public void setEventsCat(int value) {
		this.eventsCat = value;
	}

	public int getTagsCat() {
		return tagsCat;
	}

	public void setTagsCat(int value) {
		this.tagsCat = value;
	}

	public int getIdeasCat() {
		return ideasCat;
	}

	public void setIdeasCat(int value) {
		this.ideasCat = value;
	}

	@Override
	protected void init() {
		//init scenes filter
		if (node == null) {
			return;
		}
		initChapters();
		initEvents();
		initIdeas();
		initItems();
		initPlots();
		initScenes();
		initTags();
	}

	private void initChapters() {
		//empty
	}

	private void initEvents() {
		String str = XmlUtil.getString(node, "events");
		if (!str.isEmpty()) {
			String sp[] = str.split(",");
			if (sp.length > 0) {
				eventsCat = Integer.getInteger(sp[0]);
			}
		}
	}

	private void initIdeas() {
		String str = XmlUtil.getString(node, "ideas");
		if (!str.isEmpty()) {
			String sp[] = str.split(",");
			if (sp.length > 0) {
				ideasCat = Integer.getInteger(sp[0]);
			}
		}
	}

	private void initItems() {
		String str = XmlUtil.getString(node, "items");
		if (!str.isEmpty()) {
			String sp[] = str.split(",");
			if (sp.length > 0) {
				itemsCat = Integer.getInteger(sp[0]);
			}
		}
	}

	private void initPlots() {
		String str = XmlUtil.getString(node, "plots");
		if (!str.isEmpty()) {
			String sp[] = str.split(",");
			if (sp.length > 0) {
				plotsCat = Integer.getInteger(sp[0]);
			}
		}
	}

	private void initTags() {
		String str = XmlUtil.getString(node, "tags");
		if (!str.isEmpty()) {
			String sp[] = str.split(" ");
			if (sp.length > 0) {
				tagsCat = Integer.getInteger(sp[0]);
			}
		}
	}

	private void initScenes() {
		String str = XmlUtil.getString(node, "scenes");
		if (!str.isEmpty()) {
			String sp[] = str.trim().split(",");
			if (sp.length > 0) {
				scenesStatus = Integer.parseInt(sp[0].trim());
			}
			if (sp.length > 1) {
				scenesStrand = Integer.parseInt(sp[1].trim());
			}
			if (sp.length > 2) {
				scenesNarrator = Integer.parseInt(sp[2].trim());
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder("        <filters \n");
		StringBuilder v = new StringBuilder("           scenes=\"");
		v.append(String.format("%d,%d,%d", scenesStatus, scenesStrand, scenesNarrator));
		v.append("\"\n");
		b.append(v.toString());
		b.append("        />\n");
		return b.toString();
	}

}
