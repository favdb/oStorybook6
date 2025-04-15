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
package storybook.db.scene;

import java.awt.Color;
import storybook.App;

/**
 *
 * @author favdb
 */
public class Intensity {

	public static final int MIN = 1, MAX = 5;
	private Integer value;

	public Intensity() {
		this.value = MIN;
	}

	public Intensity(Integer value) {
		this.value = value;
	}

	public void set(Integer value) {
		if (value >= 0 && value <= MAX) {
			this.value = value;
		} else {
			this.value = MIN;
		}
	}

	public Integer get() {
		return (value >= MIN && value <= MAX ? value : 0);
	}

	public Color getColor() {
		return App.preferences.intensityGet(get() - 1);
	}

	@Override
	public String toString() {
		return value.toString();
		//return value + " => " + ColorUtil.getHTML(getColor());
	}

}
