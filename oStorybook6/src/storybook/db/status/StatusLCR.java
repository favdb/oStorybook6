/*
 * Copyright (C) 2016 favdb
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
package storybook.db.status;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author favdb
 */
public class StatusLCR extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean sel, boolean focus) {
	try {
	    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
	    if (value != null) {
		if (value instanceof Status) {
		    return label;
		} else if (value instanceof String) {
		    label.setText((String) value);
		} else if (value instanceof Integer) {
		    Integer n = (Integer) value;
		    if (n < Status.STATUS.values().length) {
			label.setText(Status.getStatusMsg(n));
			label.setIcon(Status.getStatusIcon(n));
		    }
		}
	    }
	    return label;
	} catch (Exception ex) {
	    return new JLabel("");
	}
    }

}
