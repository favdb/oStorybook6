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
package storybook.db.book;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import storybook.App;
import storybook.Pref;
import storybook.tools.LOG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractOptions;
import storybook.ui.panel.book.BookPanel;

/**
 *
 * @author favdb
 */
public class OptBook extends AbstractOptions {

	private final String ZOOM = "ZoomSlider";

	public OptBook(MainFrame m) {
		super(m);
		init();
		initUi();
	}

	@Override
	public void init() {
		setZoomMin(BookPanel.ZOOM_MIN);
		setZoomMax(BookPanel.ZOOM_MAX);
		try {
			zoomValue = App.preferences.bookGetZoom();
		} catch (Exception e) {
			LOG.err(Arrays.toString(e.getStackTrace()));
			zoomValue = Pref.KEY.BOOK_ZOOM.getInteger();
		}
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout());
		add(new JLabel(I18N.getColonMsg("zoom")));
		JSlider slider = new JSlider(JSlider.HORIZONTAL, getZoomMin(), getZoomMax(), zoomValue);
		slider.setName(ZOOM);
		slider.setMajorTickSpacing(5);
		slider.setMinorTickSpacing(1);
		slider.setOpaque(false);
		slider.setPaintTicks(true);
		slider.addChangeListener(this);
		add(slider, "growx");
	}

	@Override
	protected void zoom(int val) {
		App.preferences.bookSetZoom(val);
		mainFrame.getBookController().bookSetZoom(val);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Component comp = (Component) e.getSource();
		if (ZOOM.equals(comp.getName())) {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				int val = slider.getValue();
				zoom(val);
				return;
			}
		}
		super.stateChanged(e);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}
}
