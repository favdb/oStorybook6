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
package storybook.db.scene;

import storybook.db.status.AbstractStatusModel;
import i18n.I18N;
import storybook.db.status.Status.STATUS;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * @author martin
 *
 */
public class SceneStatusModel extends AbstractStatusModel {

	public enum State {
		OUTLINE, DRAFT, EDIT1, EDIT2, DONE, IN_PROGRESS, ALL
	}

	public SceneStatusModel() {
		this(true);
	}

	public SceneStatusModel(boolean addPseudoStates) {
		super();
		states.add(new SceneStatus(STATUS.OUTLINE));
		states.add(new SceneStatus(STATUS.DRAFT));
		states.add(new SceneStatus(STATUS.EDIT1));
		states.add(new SceneStatus(STATUS.EDIT2));
		states.add(new SceneStatus(STATUS.DONE));
		if (addPseudoStates) {
			states.add(new SceneStatus(State.IN_PROGRESS.ordinal(),
					I18N.getMsg("status.in.progress"), IconUtil.getIconSmall(ICONS.K.STATUS_INPROGRESS)));
			states.add(new SceneStatus(State.ALL.ordinal(),
					I18N.getMsg("status.all"), IconUtil.getIconSmall(ICONS.K.EMPTY)));
		}
	}

}
