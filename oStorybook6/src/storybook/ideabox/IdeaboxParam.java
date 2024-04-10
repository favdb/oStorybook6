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
package storybook.ideabox;

import java.awt.Dimension;
import java.awt.Point;
import storybook.App;
import storybook.Pref;

/**
 *
 * @author favdb
 */
class IdeaboxParam {

	public Dimension frmSize = new Dimension(1024, 768),
			dlgSize = new Dimension(800, 600);
	public Point frmLoc = null,
			dlgLoc = null;

	public IdeaboxParam() {
		String r = App.preferences.getString(Pref.KEY.IDEABOX_FRAME);
		if (r != null && !r.isEmpty()) {
			String t[] = r.split("\\|");
			if (t.length > 0) {
				String z[] = t[0].split("x");
				frmSize = new Dimension(Integer.parseInt(z[0]), Integer.parseInt(z[1]));
			}
			if (t.length > 1) {
				String z[] = t[1].split(",");
				frmLoc = new Point(Integer.parseInt(z[0]), Integer.parseInt(z[1]));
			}
		}
		r = App.preferences.getString(Pref.KEY.IDEABOX_DIALOG);
		if (r != null && !r.isEmpty()) {
			String t[] = r.split("\\|");
			if (t.length > 0) {
				String z[] = t[0].split("x");
				dlgSize = new Dimension(Integer.parseInt(z[0]), Integer.parseInt(z[1]));
			}
			if (t.length > 1) {
				String z[] = t[1].split(",");
				dlgLoc = new Point(Integer.parseInt(z[0]), Integer.parseInt(z[1]));
			}
		}
	}

	public void setFrm(Dimension dim, Point loc) {
		setFrmSize(dim);
		setFrmLoc(loc);
	}

	public void setFrmSize(Dimension dim) {
		this.frmSize = dim;
	}

	public void setFrmLoc(Point loc) {
		this.frmLoc = loc;
	}

	public void setDlg(Dimension dim, Point loc) {
		setDlgSize(dim);
		setDlgLoc(loc);
	}

	public void setDlgSize(Dimension dim) {
		this.dlgSize = dim;
	}

	public void setDlgLoc(Point loc) {
		this.dlgLoc = loc;
	}

	void saveFrm(IdeaxFrm frm) {
		if (frm == null) {
			return;
		}
		setFrm(frm.getSize(), frm.getLocation());
		save(Pref.KEY.IDEABOX_FRAME);
	}

	void saveDlg(IdeaDialog dlg) {
		setDlg(dlg.getSize(), dlg.getLocation());
		save(Pref.KEY.IDEABOX_DIALOG);
	}

	private void save(Pref.KEY key) {
		Dimension dim = frmSize;
		Point loc = frmLoc;
		if (key == Pref.KEY.IDEABOX_DIALOG) {
			dim = dlgSize;
			loc = dlgLoc;
		}
		dim.width += 10;//because MigLayout reduce this by 10
		dim.height -= 2;//because MigLayout increase this by 2
		String str = String.format("%dx%d|%d,%d",
				dim.width, dim.height,
				loc.x, loc.y);
		App.preferences.setString(key, str);
	}

}
