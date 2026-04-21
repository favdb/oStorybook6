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
package storybook.ui.panels;

import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public abstract class AbstractOptions extends JPanel implements ActionListener, ChangeListener {

	protected MainFrame mainFrame;
	private boolean zoom;
	protected int zoomValue;
	protected int zoomMin;
	protected int zoomMax;

	public AbstractOptions(MainFrame m) {
		mainFrame = m;
	}

	public void initAll() {
		init();
		initUi();
	}

	public void init() {

	}

	public void initUi() {

	}

	public int getZoomMin() {
		return this.zoomMin;
	}

	public void setZoomMin(int value) {
		this.zoomMin = value;
	}

	public int getZoomMax() {
		return this.zoomMax;
	}

	public void setZoomMax(int value) {
		this.zoomMax = value;
	}

	public boolean isZoom() {
		return this.zoom;
	}

	public void setZoom(boolean zoom) {
		this.zoom = zoom;
	}

	public void setZoomValue(int zoomValue) {
		this.zoomValue = zoomValue;
	}

	public int getZoomValue() {
		return zoomValue;
	}

	protected void zoom(int val) {
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSlider) {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				int val = slider.getValue();
				setZoomValue(val);
			}
		}
	}

}
