/*
 * Copyright (C) 2017 favdb
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
package storybook.dialog.options;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import storybook.App;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panels.AbstractOptions;
import storybook.ui.panels.AbstractPanel;
import storybook.ui.panels.chrono.Chrono;

/**
 *
 * @author favdb
 */
public class OptsChrono extends AbstractOptions {

	private static final String CK_DIRECTION = "ckDirection", CK_NODATES = "ckNodates", SL_ZOOM = "ZoomSlider";
	private boolean vertical, nodates;
	private JSlider sl_zoom;
	private JCheckBox ckDirection, ckNodates;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public OptsChrono(MainFrame m) {
		super(m);
		init();
		initUi();
	}

	@Override
	public void init() {
		setZoomMin(Chrono.ZOOM_MIN);
		setZoomMax(Chrono.ZOOM_MAX);
		zoomValue = AbstractPanel.setMinMax(App.preferences.chronoGetZoom(), Chrono.ZOOM_MIN, Chrono.ZOOM_MAX);
		vertical = App.preferences.chronoGetLayoutDirection();
		nodates = App.preferences.chronoGetLayoutNodates();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		// layout direction
		ckDirection = Ui.initCheckBox(this,
				CK_DIRECTION, "vertical", vertical, null, this);
		ckDirection.addItemListener(e -> changeDirection());
		ckDirection.setToolTipText(I18N.getColonMsg("view.chrono.direction"));
		add(ckDirection);
		// show nodates
		ckNodates = Ui.initCheckBox(this,
				CK_NODATES, "view.chrono.nodates", nodates, null, this);
		ckNodates.addItemListener(e -> changeNodates());
		add(ckNodates);
		sl_zoom = new JSlider(JSlider.HORIZONTAL, Chrono.ZOOM_MIN, Chrono.ZOOM_MAX, zoomValue);
		sl_zoom.setName(SL_ZOOM);
		sl_zoom.setMajorTickSpacing(5);
		sl_zoom.setMinorTickSpacing(1);
		sl_zoom.setOpaque(false);
		sl_zoom.setPaintTicks(true);
		sl_zoom.addChangeListener(e -> changeZoom());
		add(sl_zoom);

	}

	private void changeZoom() {
		int val = sl_zoom.getValue();
		App.preferences.chronoSetZoom(val);
		mainFrame.getBookController().chronoSetZoom(val);
	}

	public void changeDirection() {
		boolean val = ckDirection.isSelected();
		App.preferences.chronoSetLayoutDirection(val);
		mainFrame.getBookController().chronoSetLayoutDirection(val);
	}

	public void changeNodates() {
		boolean val = ckNodates.isSelected();
		App.preferences.chronoSetLayoutNodates(val);
		mainFrame.getBookController().chronoSetLayoutNodates(val);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
