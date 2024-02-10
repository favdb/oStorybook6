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
package storybook.ui.panel.chrono;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import storybook.App;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractOptions;
import storybook.ui.panel.AbstractPanel;
import static storybook.ui.panel.chrono.ChronoPanel.ZOOM_MAX;
import static storybook.ui.panel.chrono.ChronoPanel.ZOOM_MIN;

/**
 *
 * @author favdb
 */
public class ChronoOpt extends AbstractOptions {

	private static final String CN_LAYOUT_DIRECTION = "CbLayoutDirection", SL_ZOOM = "ZoomSlider";
	private boolean layoutDirection;
	private JSlider sl_zoom;
	private JCheckBox cbDirection;

	public ChronoOpt(MainFrame m) {
		super(m);
		init();
		initUi();
	}

	@Override
	public void init() {
		setZoomMin(ChronoPanel.ZOOM_MIN);
		setZoomMax(ChronoPanel.ZOOM_MAX);
		zoomValue = AbstractPanel.setMinMax(ChronoPanel.ZOOM_MIN, ChronoPanel.ZOOM_MAX,
		   App.preferences.chronoGetZoom());
		layoutDirection = App.preferences.chronoGetLayoutDirection();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		// layout direction
		cbDirection = new JCheckBox();
		cbDirection.setName(CN_LAYOUT_DIRECTION);
		cbDirection.addItemListener(e -> changeDirection());
		cbDirection.setText(I18N.getMsg("vertical"));
		cbDirection.setOpaque(false);
		cbDirection.setSelected(layoutDirection);
		cbDirection.setToolTipText(I18N.getColonMsg("statusbar.change.layout.direction"));
		add(cbDirection);
		sl_zoom = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, zoomValue);
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
		boolean val = cbDirection.isSelected();
		App.preferences.chronoSetLayoutDirection(val);
		mainFrame.getBookController().chronoSetLayoutDirection(val);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
