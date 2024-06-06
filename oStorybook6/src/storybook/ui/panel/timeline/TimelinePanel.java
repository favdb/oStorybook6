/*
 * Copyright (C) 2020 favdb
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

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import static storybook.ctrl.Ctrl.PROPS.PRINT;
import static storybook.ctrl.Ctrl.PROPS.REFRESH;
import static storybook.ctrl.Ctrl.PROPS.SHOWOPTIONS;
import static storybook.ctrl.Ctrl.PROPS.TIMELINE_OPTIONS;
import static storybook.ctrl.Ctrl.PROPS.TIMELINE_ZOOM;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.dialog.OptionsDlg;
import storybook.tools.print.ComponentPrinter;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class TimelinePanel extends AbstractPanel implements ChangeListener {

    private static final String TT = "TimelinePanel.";

    public static final int ZOOM_MIN = 2, ZOOM_MAX = 10;
    private JSlider sl_zoom;

    public enum ACT {
	CK_EVENTS, CK_PERSONS, CK_SCENES, NONE;
    }

    private JPanel panel;
    private Date begin, end;
    private final List<TimelineEntity> tlEntities = new ArrayList<>();
    private TimelineScale scale;
    public int zoomValue;
    private JCheckBox ckEvents,
	    ckPersons,
	    ckScenes;
    public boolean bEvents,
	    bPersons,
	    bScenes;

    public TimelinePanel(MainFrame mainFrame) {
	super(mainFrame);
    }

    @Override
    public void init() {
	//LOG.trace(TT + ".init()");
	this.withPart = false;
	zoomValue = getZoom();
	String s = App.preferences.timelineGetOptions();
	bScenes = (s.charAt(0) == '1');
	bPersons = (s.charAt(1) == '1');
	bEvents = (s.charAt(2) == '1');
	if (!tlEntities.isEmpty()) {
	    tlEntities.clear();
	}
    }

    @Override
    public void initUi() {
	//LOG.trace(TT + ".initUi()");
	setLayout(new MigLayout("ins 0"));
	if (!LaF.isDark()) {
	    setBackground(Color.white);
	}
	initToolbar();
	add(toolbar, MIG.get(MIG.SPAN, MIG.GROWX));
	panel = new JPanel(new MigLayout(MIG.INS0));
	if (!LaF.isDark()) {
	    panel.setBackground(Color.white);
	}
	refresh();
	if (scale != null) {
	    begin = scale.getDateBegin();
	    end = scale.getDateEnd();
	}
	JScrollPane scroll = new JScrollPane(panel);
	SwingUtil.setUnitIncrement(scroll);
	SwingUtil.setMaxPreferredSize(scroll);
	add(scroll, MIG.GROW);
    }

    @Override
    public JToolBar initToolbar() {
	//LOG.trace(TT + ".initToolbar()");
	//no part to select
	super.initToolbar();
	//options size
	toolbar.add(new JLabel(I18N.getColonMsg("size")));
	int max = SwingUtil.getScreenSize().width;
	sl_zoom = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, zoomValue / 320);
	sl_zoom.setName("zoom");
	sl_zoom.setOpaque(false);
	//sl_zoom.setPaintTicks(false);
	if (zoomValue > max) {
	    zoomValue = max;
	}
	sl_zoom.setValue(zoomValue / 320);
	sl_zoom.addChangeListener(this);
	toolbar.add(sl_zoom);
	//options for Scenes, Events and Persons
	ckScenes = Ui.initCheckBox(this, ACT.CK_SCENES.name(), "scenes", bScenes, BNONE);
	ckScenes.addActionListener(this);
	toolbar.add(ckScenes);
	ckPersons = Ui.initCheckBox(this, ACT.CK_PERSONS.name(), "persons", bPersons, BNONE);
	ckPersons.addActionListener(this);
	toolbar.add(ckPersons);
	ckEvents = Ui.initCheckBox(this, ACT.CK_EVENTS.name(), "events", bEvents, BNONE);
	ckEvents.addActionListener(this);
	toolbar.add(ckEvents);
	return toolbar;
    }

    public Date getDateBegin() {
	return begin;
    }

    public Date getDateEnd() {
	return end;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
	//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ")");
	String propName = evt.getPropertyName();
	if (propName == null
		|| "SHOWINFO".equalsIgnoreCase(propName)
		|| "SCENE_INIT".equalsIgnoreCase(propName)) {
	    return;
	}
	Object newValue = evt.getNewValue();
	SbView sbView = (SbView) getParent().getParent();
	if (newValue instanceof AbstractEntity) {
	    switch (ActKey.getType(evt)) {
		case EVENT:
		case PERSON:
		case SCENE:
		    if (evt.getPropertyName().toLowerCase().contains("update")) {
			refresh();
		    }
		    break;
		default:
		    break;
	    }
	} else if (newValue instanceof View) {
	    View view = (View) evt.getNewValue();
	    if (!view.getName().equals(SbView.VIEWNAME.READING.toString())) {
		switch (Ctrl.getPROPS(evt.getPropertyName())) {
		    case PRINT:
			if (sbView.getName().equals(((SbView) newValue).getName())) {
			    printAction();
			}
			return;
		    case REFRESH:
			refresh();
			return;
		    case TIMELINE_OPTIONS:
			String s = App.preferences.timelineGetOptions();
			while (s.length() < 3) {
			    s += "0";
			}
			bScenes = s.charAt(0) == '1';
			bPersons = s.charAt(1) == '1';
			bEvents = s.charAt(2) == '1';
			ckScenes.setSelected(bScenes);
			ckPersons.setSelected(bPersons);
			ckEvents.setSelected(bEvents);
			refresh();
			break;
		    case TIMELINE_ZOOM:
			sl_zoom.setValue((Integer) newValue);
			break;
		    case SHOWOPTIONS:
			if (sbView.getName().equals(((SbView) newValue).getName())) {
			    OptionsDlg.show(mainFrame, sbView.getName());
			}
			break;
		}
	    }
	}
    }

    @Override
    public void actionPerformed(ActionEvent event
    ) {
	//LOG.trace(TT + ".actionPerformed(event=" + event.toString() + ")");
	if (event.getSource() instanceof JCheckBox) {
	    bScenes = ckScenes.isSelected();
	    bEvents = ckEvents.isSelected();
	    bPersons = ckPersons.isSelected();
	    saveOptions();
	    refresh();
	}
    }

    @Override
    public void refresh() {
	//LOG.trace(TT + "refresh()");
	refreshData();
	panel.removeAll();
	if (tlEntities.isEmpty()) {
	    panel.add(new JSLabel(I18N.getMsg("timeline.empty")), MIG.WRAP);
	    return;
	} else {
	    setScale();
	}
	int lineScene = 0, lineEvent = 0, linePerson = 0;
	int r = 0;
	for (TimelineEntity tle : tlEntities) {
	    tle.setColorSize(scale);
	    int line = lineScene;
	    switch (tle.getEntity().getObjType()) {
		case EVENT:
		    if (!bEvents) {
			continue;
		    }
		    r = (bScenes ? 1 : 0);
		    line = lineEvent;
		    lineEvent++;
		    break;
		case PERSON:
		    if (!bPersons) {
			continue;
		    }
		    r = (bScenes ? 1 : 0) + (bEvents ? 1 : 0);
		    line = linePerson;
		    linePerson++;
		    break;
		case SCENE:
		    if (!bScenes) {
			continue;
		    }
		    r = 0;
		    line = lineScene;
		    lineScene++;
		    break;
		default:
		    break;
	    }
	    scale.setEntityTo(panel, tle, (line % 5) + (r * 5));
	    tle.setTooltips(scale.isSameDay());
	}
	scale.setEntityTo(panel, null, 9);
	panel.revalidate();
    }

    private void refreshData() {
	//LOG.trace(TT + "refreshData()");
	if (!tlEntities.isEmpty()) {
	    tlEntities.clear();
	}
	//load scenes
	List<Scene> scenes = Scenes.find(mainFrame);
	int nb = 0;
	for (Scene scene : scenes) {
	    if (scene.hasScenets()) {
		nb++;
	    }
	}
	if (nb < 2) {
	    return;
	}
	scenes = mainFrame.project.scenes.getWithDates();
	for (Scene scene : scenes) {
	    tlEntities.add(new TimelineEntity(mainFrame, (AbstractEntity) scene));
	}
	//load events
	for (Object obj : (List) mainFrame.project.getList(Book.TYPE.EVENT)) {
	    if (((AbstractEntity) obj).hasDate()) {
		tlEntities.add(new TimelineEntity(mainFrame, (AbstractEntity) obj));
	    }
	}
	//load persons
	for (Object obj : (List) mainFrame.project.getList(Book.TYPE.PERSON)) {
	    Person person = (Person) obj;
	    if (!person.getBirthdayToString().isEmpty()
		    && begin.before(person.getBirthday())
		    && end.after(person.getBirthday())) {
		tlEntities.add(new TimelineEntity(mainFrame, (AbstractEntity) person, person.getBirthday()));
	    }
	    if (!person.getDayofdeathToString().isEmpty()
		    && begin.before(person.getDayofdeath())
		    && end.after(person.getDayofdeath())) {
		tlEntities.add(new TimelineEntity(mainFrame, (AbstractEntity) person, person.getDayofdeath()));
	    }
	}
	begin = computeBegin();
	end = computeEnd();
    }

    private Date computeBegin() {
	//LOG.trace(TT + ".computeBegin()");
	Date r = null;
	for (TimelineEntity t : tlEntities) {
	    if (r == null) {
		r = t.getDate();
	    }
	    if (t.getDate() != null && t.getDate().before(r)) {
		r = t.getDate();
	    }
	}
	return r;
    }

    private Date computeEnd() {
	//LOG.trace(TT + ".computeEnd()");
	Date r = null;
	for (TimelineEntity t : tlEntities) {
	    if (r == null) {
		r = t.getDate();
	    }
	    if (t.getDate() != null && t.getDate().after(r)) {
		r = t.getDate();
	    }
	}
	return r;
    }

    private void setScale() {
	/*LOG.trace(TT + ".setScale()"
				+ " begin date=" + DateUtil.dateToString(begin)
				+ ", ending date=" + DateUtil.dateToString(end));*/
	scale = new TimelineScale(zoomValue, begin, end);
	panel.add(scale, "pos 0 0");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
	//LOG.trace(TT + ".stateChanged(e=" + e.toString() + ")");
	if (e.getSource() instanceof JSlider) {
	    JSlider s = (JSlider) e.getSource();
	    zoomValue = s.getValue() * 320;
	    App.preferences.timelineSetZoom(zoomValue);
	    refresh();
	}
    }

    private void saveOptions() {
	String s = "";
	s += (ckScenes.isSelected() ? "1" : "0");
	s += (ckPersons.isSelected() ? "1" : "0");
	s += (ckEvents.isSelected() ? "1" : "0");
	App.preferences.timelineSetOptions(s);
    }

    protected void setZoom(int val) {
	App.preferences.timelineSetZoom(val);
	mainFrame.getBookController().timelineSetZoom(val);
    }

    protected int getZoom() {
	return App.preferences.timelineGetZoom();
    }

    private void printAction() {
	ComponentPrinter.pr(mainFrame, panel, mainFrame.getBook().getTitle(), "");
    }
}
