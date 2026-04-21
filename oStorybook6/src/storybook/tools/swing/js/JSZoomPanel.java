/*
 * Copyright (C) 2025 favdb
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
package storybook.tools.swing.js;

import api.mig.swing.MigLayout;
import i18n.I18N;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import resources.icons.ICONS;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.panels.chrono.Chrono;
import storybook.ui.panels.manage.Manage;

/**
 *
 * @author favdb
 */
public class JSZoomPanel extends JPanel {

	private static final String TT = "JSZoomPanel.";

	private final JComponent caller;
	int zoom;
	private JLabel lbZoom;
	private JButton btZoomout, btZoomin;
	private final int zoommin, zoommax;

	public JSZoomPanel(JComponent caller, String name, int zoommin, int zoommax, int zoom) {
		super();
		this.caller = caller;
		this.setName(name);
		this.zoommin = zoommin;
		this.zoommax = zoommax;
		this.zoom = zoom;
		initialize();
	}

	private void initialize() {
		this.setLayout(new MigLayout(MIG.get(MIG.INS1)));
		this.setBorder(SwingUtil.getBorderDefault());
		this.setToolTipText(I18N.getColonMsg(getName() + "_tips"));
		JLabel lb = new JLabel(I18N.getColonMsg(getName()));
		this.add(lb);
		this.add(btZoomout = SwingUtil.createButton("", ICONS.K.MINUS, "zoom.out", e -> zoomAdjust(-1)));
		this.add(lbZoom = new JLabel("" + zoom));
		this.add(btZoomin = SwingUtil.createButton("", ICONS.K.PLUS, "zoom.in", e -> zoomAdjust(1)));
	}

	public void setZoom(int z) {
		zoom = z;
		if (z < zoommin) {
			zoom = zoommin;
		}
		if (z > zoommax) {
			zoom = zoommax;
		}
		lbZoom.setText("" + zoom);
	}

	public int getZoom() {
		return zoom;
	}

	private void zoomAdjust(int n) {
		//LOG.trace(TT + "zoomAdjust(n=" + n + ")");
		zoom += n;
		if (zoom < zoommin) {
			zoom = zoommin;
		}
		if (zoom > zoommax) {
			zoom = zoommax;
		}
		btZoomout.setEnabled(zoom > zoommin);
		btZoomin.setEnabled(zoom < zoommax);
		lbZoom.setText("" + zoom);
		if (caller instanceof Manage) {
			((Manage) caller).zoomSave(zoom);
		} else if (caller instanceof Chrono) {
			((Chrono) caller).zoomSave(zoom);
		}
		repaint();
	}

}
