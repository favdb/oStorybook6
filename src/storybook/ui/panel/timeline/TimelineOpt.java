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
package storybook.ui.panel.timeline;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import storybook.App;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.panel.AbstractOptions;

/**
 *
 * @author favdb
 */
public class TimelineOpt extends AbstractOptions implements ActionListener, ChangeListener {

	private JCheckBox ckScenes;
	private JCheckBox ckPersons;
	private JCheckBox ckEvents;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TimelineOpt(MainFrame m) {
		super(m);
		init();
		initUi();
	}

	@Override
	public void init() {
		setZoomMin(TimelinePanel.ZOOM_MIN);
		setZoomMax(TimelinePanel.ZOOM_MAX);
		setZoomValue(App.preferences.timelineGetZoom());
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		add(new JLabel(I18N.getColonMsg("size")), MIG.SPLIT2);
		int max = SwingUtil.getScreenSize().width;
		JSlider slider = new JSlider(JSlider.HORIZONTAL, getZoomMin(), getZoomMax(), getZoomValue() / 320);
		slider.setName("zoom");
		slider.setOpaque(false);
		slider.setPaintTicks(false);
		if (getZoomValue() > max) {
			setZoomValue(max);
		}
		slider.setValue(getZoomValue() / 320);
		slider.addChangeListener(this);
		add(slider);
		//options for Scenes, Events and Persons
		String s = App.preferences.timelineGetOptions();
		while (s.length() < 3) {
			s += "0";
		}
		boolean bScenes = s.charAt(0) == '1';
		boolean bPersons = s.charAt(1) == '1';
		boolean bEvents = s.charAt(2) == '1';
		ckScenes = Ui.initCheckBox(this, TimelinePanel.ACT.CK_SCENES.name(), "scenes", bScenes, BNONE);
		ckScenes.addActionListener(this);
		add(ckScenes);
		ckPersons = Ui.initCheckBox(this, TimelinePanel.ACT.CK_PERSONS.name(), "persons", bPersons, BNONE);
		ckPersons.addActionListener(this);
		add(ckPersons);
		ckEvents = Ui.initCheckBox(this, TimelinePanel.ACT.CK_EVENTS.name(), "events", bEvents, BNONE);
		ckEvents.addActionListener(this);
		add(ckEvents);
	}

	@Override
	public void setZoomValue(int val) {
		super.setZoomValue(val);
		App.preferences.timelineSetZoom(val);
		mainFrame.getBookController().timelineSetZoom(val);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Component comp = (Component) e.getSource();
		if ("zoom".equals(comp.getName())) {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				int val = slider.getValue();
				setZoomValue(val);
			}
		}
	}

	private void saveOptions() {
		String s = "";
		s += (ckScenes.isSelected() ? "1" : "0");
		s += (ckPersons.isSelected() ? "1" : "0");
		s += (ckEvents.isSelected() ? "1" : "0");
		App.preferences.timelineSetOptions(s);
		mainFrame.getBookController().timelineSetOptions(s);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			saveOptions();
		}
	}

}
