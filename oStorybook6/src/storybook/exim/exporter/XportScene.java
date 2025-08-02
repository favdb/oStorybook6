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
import storybook.db.scene.Scene;

/**
 *
 * @author favdb
 */
public class XportScene extends XportAbs {

	public XportScene(XportBook2Html m) {
		super(m);
	}

	@Override
	public void set(AbstractEntity scene) {
		super.set(scene);
		lev = 3;
	}

	@Override
	public String begin(AbstractEntity entity) {
		set(entity);
		if (xport.layout.getSceneTitle()
				&& xport.getNbScenes((Scene) entity) > 1) {
			return getTitle();
		}
		return "";
	}

	@Override
	public String content() {
		//empty
		return "";
	}

	@Override
	public String end() {
		//empty
		return "";
	}

}
