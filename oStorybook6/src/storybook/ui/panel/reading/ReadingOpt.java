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
package storybook.ui.panel.reading;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import storybook.App;
import storybook.Pref;
import storybook.tools.LOG;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractOptions;

/**
 *
 * @author favdb
 */
public class ReadingOpt extends AbstractOptions {

	private static final String CN_FONT_SIZE = "FontSizeSlider";
	private int fontSize;

	public ReadingOpt(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		setZoomMin(10);
		setZoomMax(200);
		try {
			zoomValue = App.preferences.readingGetZoom();
			fontSize = App.preferences.readingGetFontSize();
		} catch (Exception e) {
			LOG.err("OptReading error", e);
			zoomValue = Pref.KEY.READING_ZOOM.getInteger();
			fontSize = Pref.KEY.READING_FONT_SIZE.getInteger();
		}
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap,fill"));
		// font size
		add(new JSLabel(I18N.getColonMsg("font.size")));
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 6, 40, fontSize);
		slider.setName(CN_FONT_SIZE);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setOpaque(false);
		slider.setPaintTicks(true);
		slider.addChangeListener(this);
		add(slider, "growx");
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Component comp = (Component) e.getSource();
		if (CN_FONT_SIZE.equals(comp.getName())) {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				int val = slider.getValue();
				mainFrame.getBookController().readingSetFontSize(val);
				App.preferences.readingSetFontSize(val);
				return;
			}
		}
		super.stateChanged(e);
	}

	@Override
	protected void zoom(int val) {
		App.preferences.readingSetZoom(val);
		mainFrame.getBookController().readingSetZoom(val);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
