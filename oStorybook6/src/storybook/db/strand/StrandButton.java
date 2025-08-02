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

import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import resources.icons.IconUtil;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StrandButton extends JButton {

	public StrandButton(Strand strand, ActionListener listener) {
		this(strand, IconUtil.getDefSize(), listener);
	}

	public StrandButton(Strand strand, int sz, ActionListener listener) {
		super(strand.getColorIcon(sz));
		this.setName("btStrand");
		this.setToolTipText(strand.getName());
		this.setMargin(new Insets(0, 0, 0, 0));
		this.addActionListener(listener);
	}

}
