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
package storybook.exim.exporter;

import storybook.db.abs.AbstractEntity;
import storybook.exim.EXIM;
import storybook.tools.html.Html;

/**
 *
 * @author favdb
 */
abstract class XportAbs {

	public AbstractEntity entity;
	public final XportBook2Html xport;
	public int lev;

	public XportAbs(XportBook2Html x) {
		this.xport = x;
	}

	public void set(AbstractEntity entity) {
		this.entity = entity;
		this.lev = 1;
	}

	/**
	 * set the current entity and get the begining context String
	 *
	 * @param entity
	 * @return
	 */
	abstract String begin(AbstractEntity entity);

	/**
	 * get the title String for the current entity
	 *
	 * @return
	 */
	public String getTitle() {
		if (EXIM.getTitle(entity.getName()).isEmpty()) {
			return "";
		}
		if (lev == 0) {
			return Html.intoA(entity.getName(), "", "");
		}
		return Html.intoH(lev, Html.intoA(entity.getName(), "", EXIM.getTitle(entity.getName())));
	}

	/**
	 * get the content String for the current entity
	 *
	 * @return
	 */
	public abstract String content();

	/**
	 * get the ending context String for the current entity
	 *
	 * @return
	 */
	public abstract String end();

}
