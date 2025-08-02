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
package storybook.db.abs;

import java.util.List;
import storybook.project.Project;

/**
 * Abstract for entities lists and methods
 *
 * @author favdb
 */
public abstract class AbsEntitys {

	public final Project project;

	public AbsEntitys(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	public abstract AbstractEntity getFirst();

	public abstract Long getLast();

	public abstract void add(AbstractEntity entity);

	public abstract void save(AbstractEntity entity);

	public abstract void delete(AbstractEntity entity);

	public abstract AbstractEntity get(Long id);

	public abstract int getIdx(Long id);

	public abstract List getList();

	public abstract int getCount();

	public abstract void setLinks();

	public abstract String toXml();

	public abstract void changeHtmlLinks(String path);

}
