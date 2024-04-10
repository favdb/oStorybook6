/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

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
package storybook.db.status;

import javax.swing.JLabel;
import i18n.I18N;
import storybook.db.status.Status.STATUS;
import storybook.db.scene.SceneStatus;
import storybook.ui.interfaces.IRefreshable;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StatusLabel extends JLabel implements IRefreshable {

	private SceneStatus state = SceneStatus.getDefault();
	private boolean iconOnly;

	public StatusLabel(Integer status) {
		this(status, false);
	}

	public StatusLabel(Integer status, boolean iconOnly) {
		super(STATUS.values()[status].getLabel());
		setIcon(STATUS.values()[status].getIcon());
		this.iconOnly = iconOnly;
		if (iconOnly) {
			setText("");
			setToolTipText(STATUS.values()[status].getLabel());
		}
	}

	public StatusLabel(SceneStatus state) {
		this(state, false);
	}

	public StatusLabel(SceneStatus state, boolean iconOnly) {
		this.state = state;
		this.iconOnly = iconOnly;
		refresh();
	}

	@Override
	public void refresh() {
		if (!iconOnly) {
			setText(state.toString());
		}
		if (state == null) {
			return;
		}
		setIcon(state.getIcon());
		setToolTipText(I18N.getColonMsg("status") + " " + state);
	}

	public SceneStatus getState() {
		return state;
	}

	public void setState(SceneStatus state) {
		this.state = state;
	}

	public void setStatus(Integer state) {
		this.state = new SceneStatus(state);
	}
}
