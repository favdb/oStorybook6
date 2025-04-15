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
package storybook.ui.panel.manage;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import resources.icons.ICONS;
import storybook.App;
import storybook.Pref;
import storybook.tools.LOG;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractOptions;
import static storybook.ui.panel.manage.ManagePanel.*;

/**
 *
 * @author favdb
 */
public class ManageOpt extends AbstractOptions {

	private static final String CN_COLUMNS = "ColumnSlider",
			HIDE_UNASSIGNED = "HideUnassigned",
			VERTICAL = "Vertical";
	private int columns;
	private boolean hideUnassigned;
	private JCheckBox ckHide;
	private JCheckBox ckVertical;
	private JSlider slider;
	private JButton btZoomOut;
	private JLabel lbZoom;
	private JButton btZoomIn;

	public ManageOpt(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		setZoomMin(ManagePanel.ZOOM_MIN);
		setZoomMax(ManagePanel.ZOOM_MAX);
		try {
			zoomValue = App.preferences.manageGetZoom();
			columns = App.preferences.manageGetColumns();
			hideUnassigned = App.preferences.manageGetHideUnassigned();
		} catch (Exception e) {
			LOG.err(Arrays.toString(e.getStackTrace()));
			zoomValue = Pref.KEY.MANAGE_ZOOM.getInteger();
			columns = Pref.KEY.MANAGE_COLUMNS.getInteger();
		}
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP)));
		ckHide = new JCheckBox(I18N.getMsg("preferences.manage.hide_unassigned"));
		ckHide.setName(HIDE_UNASSIGNED);
		ckHide.setSelected(hideUnassigned);
		ckHide.addActionListener((ActionEvent evt) -> {
			App.preferences.manageSetHideUnassgned(ckHide.isSelected());
			mainFrame.getBookController().manageSetHideUnassigned((Boolean) ckHide.isSelected());
		});
		add(ckHide);
		ckVertical = new JCheckBox(I18N.getMsg("vertical"));
		ckVertical.setName(VERTICAL);
		ckVertical.setSelected(App.preferences.manageGetVertical());
		ckVertical.addActionListener((ActionEvent evt) -> {
			slider.setEnabled(ckVertical.isSelected());
			App.preferences.manageSetVertical(ckVertical.isSelected());
			mainFrame.getBookController().manageSetVertical((Boolean) ckVertical.isSelected());
		});
		add(ckVertical);
		// columns
		add(new JLabel(I18N.getColonMsg("columns")), MIG.get(MIG.SPLIT2));
		slider = new JSlider(JSlider.HORIZONTAL, 1, 20, columns);
		slider.setName(CN_COLUMNS);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setOpaque(false);
		slider.setPaintTicks(true);
		slider.addChangeListener(this);
		add(slider, MIG.get(MIG.GROWX));
		slider.setEnabled(ckVertical.isSelected());
		JPanel zoomPanel = new JPanel(new MigLayout());
		zoomPanel.setOpaque(false);
		zoomPanel.add(new JLabel(I18N.getColonMsg("zoom")));
		zoomPanel.add(btZoomOut = SwingUtil.createButton("", ICONS.K.MINUS, "zoom.out", e -> zoomAdjust(-1)));
		zoomPanel.add(lbZoom = new JLabel("" + zoomValue));
		zoomPanel.add(btZoomIn = SwingUtil.createButton("", ICONS.K.PLUS, "zoom.in", e -> zoomAdjust(1)));
		add(zoomPanel);
	}

	private void zoomAdjust(int n) {
		zoomValue += n;
		if (zoomValue < ZOOM_MIN) {
			zoomValue = ZOOM_MIN;
		}
		btZoomOut.setEnabled(zoomValue > ZOOM_MIN);
		btZoomIn.setEnabled(zoomValue < ZOOM_MAX);
		lbZoom.setText("" + zoomValue);
		App.preferences.manageSetZoom(zoomValue);
		mainFrame.getBookController().manageSetZoom(zoomValue);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Component comp = (Component) e.getSource();
		if (CN_COLUMNS.equals(comp.getName())) {
			JSlider js = (JSlider) e.getSource();
			if (!js.getValueIsAdjusting()) {
				int val = js.getValue();
				App.preferences.manageSetColumns(val);
				mainFrame.getBookController().manageSetColumns(val);
				return;
			}
		}
		super.stateChanged(e);
	}

	@Override
	protected void zoom(int val) {
		App.preferences.manageSetZoom(val);
		mainFrame.getBookController().manageSetZoom(val);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
