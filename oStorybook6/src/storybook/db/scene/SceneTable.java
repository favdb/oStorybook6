/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.scene;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import resources.icons.ICONS;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.status.Status;
import storybook.db.status.Status.STATUS;
import storybook.db.status.StatusLCR;
import storybook.db.strand.Strand;
import storybook.exim.exporter.ExportToPhpBB;
import storybook.tools.DateUtil;
import storybook.tools.SbDuration;
import storybook.tools.TextUtil;
import storybook.tools.comparator.ObjectComparator;
import storybook.tools.html.Html;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 * @author martin
 *
 */
public class SceneTable extends AbsTable implements ActionListener {

	private static final String TT = "SceneTable.";

	private JPanel pStrand;
	private JPanel pNarrator;

	public enum ACT {
		CB_STATUS,
		CB_STRAND,
		CB_NARRATOR,
		NONE;
	}

	public ACT getACT(String act) {
		for (ACT a : ACT.values()) {
			if (a.name().equals(act)) {
				return (a);
			}
		}
		return (ACT.NONE);
	}

	private static final String BT_EXTERNAL = "BtEXTERNAL";

	private JComboBox cbStatus;
	private JComboBox cbStrand;
	private JComboBox cbNarrator;

	public SceneTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.SCENE);
	}

	@Override
	public void init() {
		withPart = true;
		//mainFrame.project.scenes.relativeDateInit();
	}

	/**
	 * get all filtered scenes as a list of AbstractEntity, sorted by ???Id??? of unsorted
	 *
	 * @return a list of AbstractEntity
	 */
	@SuppressWarnings({"unchecked"})
	@Override
	public List<AbstractEntity> getAllEntities() {
		//LOG.trace(TT + "getAllEntities()");
		int fStatus = cbStatus.getSelectedIndex();
		Part fPart = null;
		if (cbPartFilter != null && cbPartFilter.getSelectedIndex() > 0) {
			fPart = (Part) cbPartFilter.getSelectedItem();
		}
		Strand fStrand = null;
		if (cbStrand.getSelectedIndex() > 0) {
			fStrand = (Strand) cbStrand.getSelectedItem();
		}
		Person fNarrator = null;
		if (cbNarrator.getSelectedIndex() > 0) {
			fNarrator = (Person) cbNarrator.getSelectedItem();
		}
		List<AbstractEntity> entities = (List) mainFrame.project.getList(getType());
		List<AbstractEntity> scenes = new ArrayList<>();
		for (AbstractEntity e : entities) {
			Scene s = (Scene) e;
			if (fPart != null && s.hasChapter()) {
				Chapter chapter = s.getChapter();
				if (chapter.hasPart() && !chapter.getPart().equals(fPart)) {
					continue;
				}
			}
			boolean b = true;
			if (fStatus < STATUS.values().length && s.getStatus() != fStatus) {
				b = false;
			}
			if (cbNarrator.getSelectedIndex() > 0 && fNarrator != null && !fNarrator.equals(s.getNarrator())) {
				b = false;
			}
			if (cbStrand.getSelectedIndex() > 0) {
				if (fStrand != null && !fStrand.equals(s.getStrand())
						&& (s.getStrands() != null && !s.getStrands().contains(fStrand))) {
					b = false;
				}
			}
			if (b) {
				scenes.add(s);
			}
		}
		//Collections.sort(scenes,
		//		(AbstractEntity r1, AbstractEntity r2) -> r1.getId().compareTo(r2.getId()));
		return scenes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JToolBar initToolbar() {
		//LOG.trace(TT + "initToolbar(withPart=" + (withPart ? "true" : "false") + ")");
		super.initToolbar();
		toolbar.add(new JLabel(I18N.getColonMsg("status")));
		cbStatus = initCbStatus();
		toolbar.add(cbStatus);

		pStrand = new JPanel(new MigLayout());
		pStrand.setOpaque(false);
		pStrand.add(new JLabel(I18N.getColonMsg("strand")));
		cbStrand = new JComboBox();
		cbStrand.setName(ACT.CB_STRAND.toString());
		Ui.fillCB(cbStrand, mainFrame.project.getList(Book.TYPE.STRAND), BALL, this);
		cbStrand.setSelectedIndex(0);
		cbStrand.addActionListener(this);
		pStrand.add(cbStrand);
		toolbar.add(pStrand);

		pNarrator = new JPanel(new MigLayout());
		pNarrator.setOpaque(false);
		pNarrator.add(new JLabel(I18N.getColonMsg("scene.narrator")));
		cbNarrator = new JComboBox();
		cbNarrator.setName(ACT.CB_NARRATOR.toString());
		Ui.fillCB(cbNarrator, mainFrame.project.scenes.findNarrators(), BALL, this);
		cbNarrator.setSelectedIndex(0);
		cbNarrator.addActionListener(this);
		pNarrator.add(cbNarrator);
		toolbar.add(pNarrator);

		return toolbar;
	}

	@SuppressWarnings("unchecked")
	private JComboBox initCbStatus() {
		JComboBox cb = new JComboBox();
		cb.setName("cbStatus");
		for (Status.STATUS st : Status.STATUS.values()) {
			cb.addItem(st.ordinal());
		}
		cb.addItem(I18N.getMsg("status.all"));
		cb.setSelectedIndex(cb.getItemCount() - 1);
		cb.setRenderer(new StatusLCR());
		cb.addActionListener(this);
		return (cb);
	}

	/**
	 * initialize the footer
	 *
	 * @return
	 */
	@Override
	public JToolBar initFooter() {
		JToolBar footer = super.initFooter();
		if (mainFrame.getBook().isXeditorUse()) {
			btExternal = Ui.initButton(BT_EXTERNAL, "", ICONS.K.LIBREOFFICE,
					"", e -> sendSetExternal(table.getSelectedRow()));
			//btExternal.setText(book.getParamEditor().getName());
			btExternal.setToolTipText(I18N.getMsg("xeditor.launching")
					+ " " + book.getParam().getParamEditor().getName());
			btExternal.setEnabled(false);
			footer.add(btExternal);
		}
		return footer;
	}

	@Override
	public void fillTable() {
		super.fillTable();
		if (pStrand != null) {
			pStrand.setVisible(mainFrame.project.getList(Book.TYPE.STRAND).size() > 1);
		}
		if (pNarrator != null) {
			pNarrator.setVisible(mainFrame.project.getList(Book.TYPE.PERSON).size() > 1);
		}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		//LOG.trace(TT + "actionPerformed(" + e.toString() + ")");
		if (e.getSource() instanceof JComboBox) {
			fillTable();
			return;
		}
		super.actionPerformed(e);
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChangeLocal(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		Ctrl.PROPS prop = Ctrl.getPROPS(evt.getPropertyName());
		Object newValue = evt.getNewValue();
		ActKey act = new ActKey(evt);
		try {
			if (isInit(act)) {
				return;
			}
			if (act.isNew() | act.isUpdate() || act.isDelete()) {
				fillTable();
			}
			switch (Book.getTYPE(act.type)) {
				case PART:
					if (isChange(act)) {
						Ui.fillCB(cbPartFilter, mainFrame.project.getList(getType()), "", this);
					}
					break;
				case SCENE:
					if (PROPS.SCENE_FILTER_STATUS.check(propName)) {
						initTableModel(evt);
						if (newValue instanceof SceneStatus) {
							cbStatus.setSelectedItem((SceneStatus) newValue);
						}
					} else if (PROPS.SCENE_FILTER_NARRATOR.check(propName)) {
						initTableModel(evt);
						if (newValue instanceof String) {
							cbNarrator.setSelectedItem((String) newValue);
						}
					} else if (PROPS.SCENE_FILTER_STRAND.check(propName)) {
						initTableModel(evt);
						if (newValue instanceof String) {
							cbStrand.setSelectedItem((String) newValue);
						}
					}
					break;
				case STRAND:
					if (isChange(act)) {
						int nx = cbStrand.getSelectedIndex();
						Strand os = (Strand) cbStrand.getSelectedItem();
						Ui.fillCB(cbStrand, mainFrame.project.getList(Book.TYPE.STRAND), "");
						if (nx != 0 && os != null) {
							cbStrand.setSelectedItem(os);
						}
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Scene) mainFrame.project.get(Book.TYPE.SCENE, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// refresh for chapter, item, location, person, scenario, scene, strand, photo
		switch (Book.getTYPE(entity)) {
			case CHAPTER:
			case ITEM:
			case LOCATION:
			case PLOT:
			//case SCENARIO:
			case SCENE:
				fillTable();
				break;
			case STRAND:
				Ui.fillCB(cbStrand, mainFrame.project.getList(Book.TYPE.STRAND), BALL, this);
				fillTable();
				break;
			case PERSON:
				Ui.fillCB(cbNarrator, mainFrame.project.scenes.findNarrators(), BALL, this);
				fillTable();
			default:
				break;
		}
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);
		AbsColumn col;
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.INFORMATIVE, TCR_HIDE, AL_CENTER, TCR_BOOLEAN));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.NUMBER, NUMERIC, TCR_HIDE, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.STATUS, AL_CENTER, TCR_STATUS));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.INTENSITY, AL_CENTER, TCR_HIDE, TCR_COLOR, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.CCSS, TCR_HIDE, AL_CENTER));
		col = new AbsColumn(mainFrame, cols, DB.DATA.SCENE_CHAPTER, TCR_ENTITY);
		col.setComparator(new ObjectComparator());
		cols.add(col);
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.WORDS, NUMERIC_RENDERER, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.CHARACTERS, NUMERIC_RENDERER, AL_CENTER));
		if (App.getAssistant().isExists(book, "stage")) {
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENARIO_STAGE, TCR_HIDE));
		}
		col = new AbsColumn(mainFrame, cols, DB.DATA.SCENE_SCENETS, TCR_DATE, TCR_HIDE, AL_CENTER);
		col.setMsgKey("date");
		col.setShowDateTime(true);
		cols.add(col);
		col = new AbsColumn(mainFrame, cols, DB.DATA.SCENE_RELATIVESCENEID, TCR_HIDE, AL_CENTER, TCR_ENTITY);
		col.setMsgKey("scene.relativedate.after");
		cols.add(col);
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE_RELATIVETIME, TCR_HIDE, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.DURATION, TCR_HIDE, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE_STRAND, TCR_ENTITY));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.STRANDS, TCR_HIDE, TCR_ENTITIES));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE_NARRATOR, TCR_HIDE, TCR_ENTITY));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE_SUMMARY, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.PERSONS, TCR_HIDE, TCR_ENTITIES));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATIONS, TCR_HIDE, TCR_ENTITIES));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.ITEMS, TCR_HIDE, TCR_ENTITIES));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.PLOTS, TCR_HIDE, TCR_ENTITIES));
		if (book.info.scenarioGet()) {
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENARIO_PITCH, TCR_HIDE));
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENARIO_LOC, AL_CENTER, TCR_HIDE));
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENARIO_MOMENT, AL_CENTER, TCR_HIDE));
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENARIO_START, AL_CENTER, TCR_HIDE));
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENARIO_END, AL_CENTER, TCR_HIDE));
		}
		getColumnsEnd(cols, entity);
		return cols;
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Scene e = (Scene) entity;
		cols.add(e.getInformative());
		cols.add(e.getSceneno());
		cols.add(e.getStatus());
		cols.add(e.getIntensity());
		cols.add(e.getCCSS());
		cols.add(e.getChapter());
		cols.add(e.getWords());
		cols.add(e.getChars());
		if (App.getAssistant().isExists(book, "stage")) {
			cols.add(e.getScenariostage());
		}
		Date date = e.getDateRel();
		if (e.hasScenets()) {// fixed date
			cols.add(date);
			cols.add(-1L);//relative scene
			cols.add(" ");//relative time
		} else if (e.hasRelativescene()) { // relative date
			if (date != null) {
				cols.add("(" + DateUtil.simpleDateTimeToString(date, false) + ")");
			} else {
				cols.add("(?)");
			}
			Scene relScene = mainFrame.project.scenes.get(e.getRelativesceneid());
			cols.add(relScene.getName());
			if (SbDuration.isZero(e.getRelativetime())) {
				cols.add("(" + SbDuration.getFromWords(relScene.getSummary()).toText() + ")");
			} else {
				SbDuration dur = new SbDuration(e.getRelativetime());
				cols.add(dur.toText());
			}
		} else {
			cols.add("");
			cols.add(" ");
			cols.add(" ");
		}
		cols.add(e.getDurationToText());
		cols.add(e.getStrand());
		cols.add(e.getStrands());
		cols.add(e.getNarrator());
		cols.add(TextUtil.ellipsize(Html.htmlToText(e.getSummary()), 128));
		cols.add(e.getPersons());
		cols.add(e.getLocations());
		cols.add(e.getItems());
		cols.add(e.getPlots());
		// scenario data
		if (book.info.scenarioGet()) {
			cols.add(e.getScenariopitch());
			cols.add(e.getScenarioloc());
			cols.add(e.getScenariomoment());
			cols.add(e.getScenariostart());
			cols.add(e.getScenarioend());
		}
		getRowEnd(cols, entity);
		return cols;
	}

	@Override
	protected synchronized void copyToPhpBB(int row) {
		//LOG.trace(TT + ".copyToPhpBB(r=" + row + ")");
		Scene scene = (Scene) getEntityFromRow(row);
		if (scene != null) {
			ExportToPhpBB.getScene(mainFrame, scene);
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg("copied.title"),
					I18N.getMsg(scene.getObjType().toString()), 1);
		}
	}

}
