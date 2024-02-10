/*
 * Copyright (C) 2019 favdb
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
package storybook.db.event;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import storybook.Const;
import storybook.db.DB;
import storybook.db.abs.AbstractEntity;
import storybook.dialog.chooser.ColorPicker;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.LOG;
import storybook.tools.SbDuration;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSDateChooser;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class EventEdit extends AbstractEditor {

	private JComboBox cbCategory;
	private JSDateChooser dDate;
	private JTextField fDuration;
	//private ColorChooserPanel tfColor;
	private ColorPicker cbColor;
	private static final String INITIALES = I18N.getMsg("duration.initiales");
	private JTextField tfTimestep;
	private JButton btColor;

	public EventEdit(Editor m, AbstractEntity e) {
		super(m, e, "010");
		initAll();
	}

	@Override
	public void initUpper() {
		Event ev = (Event) entity;
		cbCategory = Ui.initCategory(pUpper, mainFrame.project.events.findCategories(), ev.getCategory(), "101");
		SwingUtil.setCBsize(cbCategory);
		JPanel p = new JPanel(new MigLayout(MIG.INS0));
		cbColor = new ColorPicker(ev.getJColor());
		p.add(cbColor);
		btColor = Ui.initButton("btColor", " ", ICONS.K.COLOR, "color.other", e -> changeColor());
		btColor.setHorizontalAlignment(SwingConstants.LEFT);
		p.add(btColor);
		if (cbColor.getSelectedIndex() < 1) {
			btColor.setBackground(ev.getJColor());
		}
		Ui.addField(pUpper, "color", "", p, null, BMANDATORY);
		dDate = initSbDate(pUpper, "date", ev.getEventTime(), BMANDATORY, 0);
		String tDuration = "";
		if (!ev.getDuration().isEmpty()) {
			SbDuration dur = new SbDuration(ev.getDuration());
			tDuration = dur.toText(INITIALES);
		}
		fDuration = Ui.initStringField(pUpper, "duration", 16, tDuration, "");
		fDuration.setToolTipText(I18N.getMsg("duration.format"));
		tfTimestep = initIntegerField(pUpper, DB.DATA.EVENT_TIMESTEP, 5, ev.getTimeStep(), "");
		tfTimestep.setToolTipText(I18N.getMsg("event.timestep_tip"));
	}

	@Override
	public boolean verifier() {
		JTextField tf = new JTextField();
		resetError();
		dDate.setBorder(tf.getBorder());
		if (dDate.hasError() || dDate.getDate() == null) {
			errorMsg(dDate, Const.ERROR_MISSING);
		}
		if (!fDuration.getText().isEmpty()) {
			String str = SbDuration.check(fDuration.getText(), INITIALES);
			if (!str.isEmpty()) {
				errorString(fDuration, str);
			}
		}
		/*if (!tfTimestep.getText().isEmpty() && !StringUtil.isNumeric(tfTimestep.getText())) {
			errorMsg(tfTimestep, Const.ERROR_NOTNUMERIC);
		}*/
		if (Html.htmlToText(hNotes.getText()).isEmpty()) {
			errorMsg(hNotes, Const.ERROR_MISSING);
		}
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Event event = (Event) entity;
		event.setCategory((String) cbCategory.getSelectedItem());
		event.setEventTime(dDate.getTimestamp());
		SbDuration dur = SbDuration.getFromText(fDuration.getText(), INITIALES);
		event.setDuration(dur.toString());
		/*if (!tfTimestep.getText().isEmpty()) {
			event.setTimeStep(Integer.parseInt(tfTimestep.getText()));
		} else {
			event.setTimeStep(0);
		}*/
		dur = SbDuration.getFromText(tfTimestep.getText());
		event.setTimeStep(dur.toMinutes());
		if (cbColor.getSelectedIndex() == -1) {
			event.setJColor(btColor.getBackground());
		} else {
			event.setJColor((Color) cbColor.getSelectedItem());
		}
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	private void changeColor() {
		LOG.trace("changeColor()");
		Color color = JColorChooser.showDialog(this, I18N.getMsg("color.choose"), ((Event) entity).getJColor());
		btColor.setBackground(color);
	}

}
