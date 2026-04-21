/*
 * Copyright (C) 2023 favdb
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
package storybook.db.location;

import i18n.I18N;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JCheckBox;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.project.Project;
import storybook.tools.ListUtil;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class Locations extends AbsEntitys {

	private final List<Location> locations = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Locations(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Location p : locations) {
			if (n < p.getId()) {
				n = p.getId();
			}
		}
		return n;
	}

	@Override
	public void save(AbstractEntity entity) {
		if (entity.getId() < 0) {
			entity.setId(getLast() + 1);
			locations.add((Location) entity);
		} else {
			locations.set(getIdx(entity.getId()), (Location) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Location p : locations) {
			if (p.getId().equals(id)) {
				return locations.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Location get(Long id) {
		for (Location p : locations) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		locations.add((Location) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			locations.remove((Location) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (locations.isEmpty()) {
			return null;
		}
		return locations.get(0);
	}

	@Override
	public List getList() {
		return locations;
	}

	/**
	 * sort by Id
	 */
	@Override
	public void sortById() {
		Collections.sort(locations, (Location r1, Location r2) -> r1.getId().compareTo(r2.getId()));
	}

	@Override
	public int getCount() {
		return locations.size();
	}

	/**
	 * find all chapters sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Location> ls = new ArrayList<>();
		for (Location p : locations) {
			ls.add(p);
		}
		Collections.sort(ls, (Location r1, Location r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	/**
	 * find Chapter for the given name
	 *
	 * @param name
	 * @return
	 */
	public Location findName(String name) {
		for (Location p : locations) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllInList() {
		List<String> ls = new ArrayList<>();
		for (Location s : locations) {
			ls.add(s.getName());
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<String> findCountries() {
		List<String> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCountry() && !ls.contains(s.getCountry())) {
				ls.add(s.getCountry());
			}
		}
		return (ls);
	}

	@SuppressWarnings("unchecked")
	public List<String> findCities() {
		List<String> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCity() && !ls.contains(s.getCity())) {
				ls.add(s.getCity());
			}
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<String> findCitiesByCountry(String country) {
		List<String> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCountry()
					&& s.getCountry().equals(country)
					&& !ls.contains(s.getCity())) {
				ls.add(s.getCity());
			}
		}
		return (ls);
	}

	@SuppressWarnings("unchecked")
	public List<Location> findCitiesByCountries(List<String> countries) {
		List<Location> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCountry()
					&& countries.contains(s.getCountry())
					&& !ls.contains(s)) {
				ls.add(s);
			}
		}
		return (ls);
	}

	@SuppressWarnings("unchecked")
	public List<Location> findByCountry(String country) {
		List<Location> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCountry()
					&& country.equals(s.getCountry())) {
				ls.add(s);
			}
		}
		return (ls);
	}

	@SuppressWarnings("unchecked")
	public List<Location> findByCity(String city) {
		List<Location> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCity()
					&& city.equals(s.getCity())) {
				ls.add(s);
			}
		}
		return (ls);
	}

	@SuppressWarnings("unchecked")
	public List<Location> findByContryCity(String country, String city) {
		List<Location> ls = new ArrayList<>();
		for (Location s : locations) {
			if (s.hasCountry() && country.equals(s.getCountry())
					&& s.hasCity() && city.equals(s.getCity())) {
				ls.add(s);
			}
		}
		return (ls);
	}

	public List<Location> findByCountries(List<String> selectedCountries) {
		List<Location> ls = new ArrayList<>();
		for (Location l : locations) {
			if (selectedCountries.contains(l.getCountry())) {
				ls.add(l);
			}
		}
		return ls;
	}

	public List<Location> findOrdered() {
		@SuppressWarnings("unchecked")
		List<Location> ls = getList();
		Collections.sort(ls, (Location it1, Location it2)
				-> it1.getCountryCity().compareTo(it2.getCountryCity()));
		return ls;
	}

	/**
	 * find Locations linked to the given Chapter
	 *
	 * @param mainFrame
	 * @param chapter
	 * @return
	 */
	public static List find(MainFrame mainFrame, Chapter chapter) {
		List<Location> locations = new ArrayList<>();
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		for (Scene s : scenes) {
			for (Location l : s.getLocations()) {
				locations.add(l);
			}
		}
		return ListUtil.setUnique(locations);
	}

	/**
	 * get a JCheckBox for countries
	 *
	 * @param comp
	 * @return
	 */
	public List<JCheckBox> cbCountry(ActionListener comp) {
		List<JCheckBox> list = new ArrayList<>();
		List<String> countries = findCountries();
		for (String country : countries) {
			JCheckBox chb = new JCheckBox(country);
			chb.setOpaque(false);
			chb.addActionListener(comp);
			chb.setSelected(true);
			list.add(chb);
		}
		return list;
	}

	/**
	 * get the tool tip of the given Location
	 *
	 * @param buf
	 * @param location
	 */
	public static void tooltip(StringBuilder buf, Location location) {
		if (location == null) {
			return;
		}
		if (!location.getCity().isEmpty()) {
			buf.append(I18N.getColonMsg("location.city"));
			buf.append(" ");
			buf.append(location.getCity());
			buf.append(Html.BR);
		}
		if (!location.getCity().isEmpty()) {
			buf.append(I18N.getColonMsg("location.country"));
			buf.append(" ");
			buf.append(location.getCountry());
		}
		buf.append(Html.intoP(TextUtil.ellipsize(location.getDescription()), "\"margin-top:5px\""));
	}

	@Override
	public void setLinks() {
		for (Location p : locations) {
			if (p.getSiteId() != -1L) {
				p.setSite(get(p.getSiteId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Location p : locations) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Location p : locations) {
			p.changeHtmlLinks(path);
		}
	}

}
