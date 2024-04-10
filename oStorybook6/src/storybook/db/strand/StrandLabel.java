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
package storybook.db.strand;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import resources.icons.IconUtil;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.ColorUtil;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StrandLabel extends JLabel {

	public StrandLabel(Strand strand, boolean iconOnly) {
		super(strand.toString());
		setIcon(new ColorIcon(strand.getJColor(), IconUtil.getDefSize()));
		setToolTipText(strand.toString());
		if (iconOnly) {
			setText("");
		}
	}

	public StrandLabel(Strand strand, String prop) {
		super(" " + strand.toString() + " ");
		setMinimumSize(new Dimension(20, getHeight()));
		setBackground(ColorUtil.getPastel(strand.getJColor()));
		setForeground(!ColorUtil.isDark(strand.getJColor()) ? Color.BLACK : Color.WHITE);
		setOpaque(true);
		putClientProperty(prop, strand.getId());
	}
}
