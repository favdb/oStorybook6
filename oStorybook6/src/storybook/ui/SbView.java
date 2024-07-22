/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun, 2015 FaVdB

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
package storybook.ui;

import api.infonode.docking.View;
import java.awt.Component;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class SbView extends View {

	public enum VIEWNAME {
		//views
		ATTRIBUTESLIST("ViewAttributesList"),
		BOOK("ViewBook"),
		CHRONO("ViewChrono"),
		CHRONONEW("ViewChronoNew"),
		INFO("ViewInfo"),
		TIMELINE("ViewTimeline"),
		STORY_THREE("ViewClassic"),
		STORY_FREYTAG("ViewFreytag"),
		STORY_VOGLER("ViewVogler"),
		MANAGE("ViewManage"),
		MEMORIA("ViewMemoria"),
		//NAVIGATION("ViewNavigation"),
		PLANNING("ViewPlanning"),
		READING("ViewReading"),
		SCENARIO("ViewScenario"),
		STORYBOARD("ViewStoryboard"),
		STORYMAP("ViewStorymap"),
		TREE("ViewTree"),
		TYPIST("ViewTypist"),
		//charts
		CHART_GANTT("ChartGantt"),
		CHART_OCCURRENCE_OF_ITEMS("ChartOccurrenceOfItems"),
		CHART_OCCURRENCE_OF_LOCATIONS("ChartOccurrenceOfLocations"),
		CHART_OCCURRENCE_OF_PERSONS("ChartOccurrenceOfPersons"),
		CHART_PERSONS_BY_DATE("ChartPersonsByDate"),
		CHART_PERSONS_BY_SCENE("ChartPersonsByScene"),
		CHART_STRANDS_BY_DATE("ChartStrandsByDate"),
		CHART_WIWW("ChartWiWW"),
		//tables
		ATTRIBUTES("Attributes"),
		CATEGORIES("Categories"),
		CHAPTERS("Chapters"),
		EVENTS("Events"),
		ENDNOTES("Endnotes"),
		EPISODES("Episodes"),
		GENDERS("Genders"),
		IDEAS("Ideas"),
		INTERNALS("Internals"),
		ITEMS("Items"),
		ITEMLINKS("ItemLinks"),
		LOCATIONS("Locations"),
		MEMOS("Memos"),
		PARTS("Parts"),
		PERSONS("Persons"),
		PLOTS("Plots"),
		RELATIONS("Relations"),
		SCENES("Scenes"),
		STRANDS("Strands"),
		TAGS("Tags"),
		TAGLINKS("TagLinks"),
		NONE("None");
		final private String text;

		private VIEWNAME(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}

		public boolean compare(View view) {
			return text.equals(view.getName());
		}
	}

	public static VIEWNAME getVIEW(View view) {
		for (VIEWNAME v : VIEWNAME.values()) {
			if (v.toString().toLowerCase().equals(view.getName().toLowerCase())) {
				return (v);
			}
		}
		return (VIEWNAME.NONE);
	}

	public static VIEWNAME getVIEW(String str) {
		for (VIEWNAME v : VIEWNAME.values()) {
			if (v.toString().equals(str)) {
				return (v);
			}
		}
		return (VIEWNAME.NONE);
	}

	private static int counter = -1;
	private boolean loaded;
	private Integer number;

	public SbView(String title) {
		this(title, null, null);
	}

	public SbView(String title, Icon icon) {
		this(title, icon, null);
	}

	public SbView(String title, Component comp) {
		this(title, null, comp);
	}

	public SbView(String title, Icon icon, Component comp) {
		super(title, icon, comp);
		loaded = comp != null;
		number = counter++;
	}

	public void load(JComponent comp) {
		super.setComponent(comp);
		loaded = true;
	}

	public void unload() {
		super.setComponent(null);
		loaded = false;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public boolean isWindowShowing() {
		return getRootWindow() != null;
	}

	public void cleverRestoreFocus() {
		setVisible(true);
		if (!isMinimized()) {
			restore();
		}
		restoreFocus();
	}

	@Override
	public String toString() {
		return "View " + number + ": " + getTitle();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = hash * 31 + number.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		SbView test = (SbView) obj;
		return Objects.equals(number, test.number);
	}
}
