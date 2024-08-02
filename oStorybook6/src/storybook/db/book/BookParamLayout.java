/*
 * Copyright (C) 2020 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import java.util.ArrayList;
import java.util.List;
import storybook.Const;
import static storybook.exim.exporter.AbstractExport.*;
import storybook.tools.ListUtil;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author favdb
 */
public class BookParamLayout extends BookParamAbstract {

	public enum KW {
		PART, CHAPTER, SCENE, REVIEW;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private boolean partTitle;
	private boolean chapterDateLocation;
	private boolean chapterDescription;
	private boolean chapterNumber;
	private boolean chapterRoman;
	private boolean chapterTitle;
	private boolean sceneDidascalie;
	private boolean sceneSeparator;
	private String sceneSeparatorValue = "";
	private boolean sceneTitle;
	private boolean showReview;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamLayout(BookParam param) {
		super(param, "layout");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("layout");
		}
		init();
	}

	public BookParamLayout(String layout) {
		super(null, "layout");
		setBookLayout(layout);
	}

	public boolean getChapterDateLocation() {
		return chapterDateLocation;
	}

	public void setChapterDateLocation(boolean d) {
		chapterDateLocation = d;
	}

	public boolean getChapterDescription() {
		return chapterDescription;
	}

	public void setChapterDescription(boolean d) {
		chapterDescription = d;
	}

	public boolean getChapterNumber() {
		return chapterNumber;
	}

	public void setChapterNumber(boolean d) {
		chapterNumber = d;
	}

	public boolean getChapterRoman() {
		return chapterRoman;
	}

	public void setChapterRoman(boolean d) {
		chapterRoman = d;
	}

	public boolean getChapterTitle() {
		return chapterTitle;
	}

	public void setChapterTitle(boolean d) {
		chapterTitle = d;
	}

	public boolean getPartTitle() {
		return partTitle;
	}

	public void setPartTitle(boolean d) {
		partTitle = d;
	}

	public boolean getSceneDidascalie() {
		return sceneDidascalie;
	}

	public void setSceneDidascalie(boolean d) {
		sceneDidascalie = d;
	}

	public boolean getSceneSeparator() {
		return sceneSeparator;
	}

	public void setSceneSeparator(boolean d) {
		sceneSeparator = d;
	}

	public String getSceneSeparatorValue() {
		if (sceneSeparatorValue.isEmpty()) {
			return Const.SCENE_SEPARATOR;
		}
		return sceneSeparatorValue;
	}

	public void setSceneSeparatorValue(String value) {
		sceneSeparatorValue = value;
	}

	public boolean getShowReview() {
		return showReview;
	}

	public void setShowReview(boolean b) {
		showReview = b;
	}

	public boolean getSceneTitle() {
		return sceneTitle;
	}

	public void setSceneTitle(boolean d) {
		sceneTitle = d;
	}

	public static void setBookLayout(BookParamLayout layout, String in) {
		if (in.isEmpty()) {
			return;
		}
		layout.setPartTitle(in.charAt(0) == '1');
		layout.setChapterDateLocation(in.charAt(1) == '1');
		layout.setChapterDescription(in.charAt(2) == '1');
		layout.setChapterNumber(in.charAt(3) == '1');
		layout.setChapterRoman(in.charAt(4) == '1');
		layout.setChapterTitle(in.charAt(5) == '1');
		layout.setSceneDidascalie(in.charAt(6) == '1');
		layout.setSceneSeparator(in.charAt(7) == '1');
		layout.setSceneTitle(in.charAt(8) == '1');
	}

	public void setBookLayout(String in) {
		setBookLayout(this, in);
	}

	public String getBookLayout() {
		return (getBookLayout(this));
	}

	public static String getBookLayout(BookParamLayout layout) {
		String r = "";
		r += (layout.getPartTitle() ? "1" : "0");
		r += (layout.getChapterDateLocation() ? "1" : "0");
		r += (layout.getChapterDescription() ? "1" : "0");
		r += (layout.getChapterNumber() ? "1" : "0");
		r += (layout.getChapterRoman() ? "1" : "0");
		r += (layout.getChapterTitle() ? "1" : "0");
		r += (layout.getSceneDidascalie() ? "1" : "0");
		r += (layout.getSceneSeparator() ? "1" : "0");
		r += (layout.getSceneTitle() ? "1" : "0");
		return (r);
	}

	@Override
	protected void init() {
		if (node != null) {
			setPartLayout(getString(node, KW.PART.toString()));
			setChapterLayout(getString(node, KW.CHAPTER.toString()));
			setSceneLayout(getString(node, KW.SCENE.toString()));
			setShowReview(getBoolean(node, KW.REVIEW.toString()));
		}
	}

	private String getPartLayout() {
		return getPartTitle() ? "1" : "0";
	}

	private String getChapterLayout() {
		List<String> l = new ArrayList<>();
		l.add(chapterTitle ? "1" : "0");
		l.add(chapterNumber ? "1" : "0");
		l.add(chapterRoman ? "1" : "0");
		l.add(chapterDateLocation ? "1" : "0");
		l.add(chapterDescription ? "1" : "0");
		return ListUtil.join(l, "");
	}

	private String getSceneLayout() {
		List<String> l = new ArrayList<>();
		l.add(sceneTitle ? "1" : "0");
		l.add(sceneDidascalie ? "1" : "0");
		l.add(sceneSeparator ? "1" : "0");
		return ListUtil.join(l, "") + "|" + sceneSeparatorValue;
	}

	private void setPartLayout(String s) {
		setPartTitle(s.equals("1"));
	}

	private void setChapterLayout(String s) {
		if (!s.isEmpty()) {
			setChapterTitle(s.charAt(0) == '1');
			setChapterNumber(s.charAt(1) == '1');
			setChapterRoman(s.charAt(2) == '1');
			setChapterDateLocation(s.charAt(3) == '1');
			setChapterDescription(s.charAt(4) == '1');
		}
	}

	private void setSceneLayout(String s) {
		if (!s.isEmpty()) {
			String x[] = s.split("\\|");
			setSceneTitle(x[0].charAt(0) == '1');
			setSceneDidascalie(x[0].charAt(1) == '1');
			setSceneSeparator(x[0].charAt(2) == '1');
			if (x.length > 1) {
				setSceneSeparatorValue(x[1]);
			}
		}
	}

	@Override
	public String toXml() {
		//LOG.trace(TT + ".toXml(b)");
		StringBuilder b = new StringBuilder("        <layout ");
		b.append(stringAttribute(0, KW.PART.toString(), getPartLayout()));
		b.append(stringAttribute(0, KW.CHAPTER.toString(), getChapterLayout()));
		b.append(stringAttribute(0, KW.SCENE.toString(), getSceneLayout()));
		b.append(stringAttribute(0, KW.REVIEW.toString(), getShowReview()));
		b.append(" />\n");
		return b.toString();
	}

	public int getHashCode() {
		return toXml().hashCode();
	}

	@Override
	public String toString() {
		return toXml();
	}

}
