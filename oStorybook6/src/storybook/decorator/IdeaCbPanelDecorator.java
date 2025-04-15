/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2015 - FaVdB

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
package storybook.decorator;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import storybook.db.abs.AbstractEntity;
import storybook.db.idea.Idea;
import storybook.tools.StringUtil;
import storybook.tools.swing.FontUtil;

/**
 * @author FaVdB
 *
 */
public class IdeaCbPanelDecorator extends CbPanelDecorator {

	private String oldCat = "";

	public IdeaCbPanelDecorator() {
		// empty
	}

	@Override
	public void decorateBeforeFirstEntity() {
		// empty
	}

	@Override
	public void decorateBeforeEntity(AbstractEntity entity) {
		Idea p = (Idea) entity;
		String cat = StringUtil.capitalize(p.getCategory());
		if (!oldCat.equals(cat)) {
			JLabel lb = new JLabel(cat);
			lb.setFont(FontUtil.getBold());
			panel.add(lb, "span");
			oldCat = cat;
		}
	}

	@Override
	public void decorateEntity(JCheckBox cb, AbstractEntity entity) {
		Idea p = (Idea) entity;
		JLabel lbIcon = new JLabel(p.getIcon());
		panel.add(lbIcon, "split 2");
		panel.add(cb);
	}

	@Override
	public void decorateAfterEntity(AbstractEntity entity) {
		// empty
	}
}
