/*
 * Copyright (C) 2022 favdb
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
package storybook.db.scene;

import api.mig.swing.MigLayout;
import assistant.Assistant;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.person.Person;
import storybook.db.status.StatusLCR;
import storybook.db.strand.Strand;
import storybook.tools.swing.CbUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.dialog.AbsDialog;

/**
 *
 * @author favdb
 */
public class ScenesChange extends AbsDialog {

	private static final String TT = "ScenesChange";

	public static boolean show(MainFrame mainFrame, List<AbstractEntity> scenes) {
		//LOG.printInfos(TT + ".show(mainFrame, scenes nb=" + scenes.size() + ")");
		ScenesChange dlg = new ScenesChange(mainFrame, scenes);
		dlg.setVisible(true);
		return dlg.canceled;
	}

	private JComboBox cbStatus;
	private JComboBox cbChapters;
	private final List<AbstractEntity> scenes;
	private JComboBox cbStrands;
	private JComboBox cbNarrator;
	private IntensityPanel intensity;
	private JCheckBox ckIntensity;
	private JRadioButton ckInfo;
	private JRadioButton ckInfoYes;
	private JComboBox cbStage;

	public ScenesChange(MainFrame mainFrame, List<AbstractEntity> scenes) {
		super(mainFrame);
		this.scenes = scenes;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		//LOG.printInfos(TT+".initUi()");
		setLayout(new MigLayout(MIG.WRAP, "[][]"));
		setTitle(I18N.getMsg("scenes.change"));
		// status
		cbStatus = initCb(Book.TYPE.STATUS);
		cbChapters = initCb(Book.TYPE.CHAPTER);
		cbStrands = initCb(Book.TYPE.STRAND);
		if (mainFrame.project.getList(Book.TYPE.STRAND).size() < 2) {
			cbStrands.setEnabled(false);
		}
		cbNarrator = initCb(Book.TYPE.PERSON);
		if (mainFrame.project.getList(Book.TYPE.PERSON).size() < 2) {
			cbNarrator.setEnabled(false);
		}
		intensity = new IntensityPanel(1);
		add(new JLabel(I18N.getColonMsg("intensity")));
		ckIntensity = new JCheckBox(I18N.getMsg("change.no"));
		ckIntensity.setSelected(true);
		add(ckIntensity, MIG.SPLIT2);
		add(intensity, "span");
		add(new JLabel(I18N.getColonMsg("informative")));
		ButtonGroup bg = new ButtonGroup();
		ckInfo = new JRadioButton(I18N.getMsg("change.no"));
		ckInfo.setSelected(true);
		add(ckInfo, MIG.SPLIT + " 3");
		JRadioButton ckInfoNo = new JRadioButton(I18N.getMsg("no"));
		add(ckInfoNo);
		ckInfoYes = new JRadioButton(I18N.getMsg("yes"));
		add(ckInfoYes, MIG.SPLIT2);
		bg.add(ckInfo);
		bg.add(ckInfoNo);
		bg.add(ckInfoYes);
		if (App.getAssistant().isExists(book, "stage")) {
			add(new JLabel(I18N.getColonMsg("scenario.stage")));
			cbStage = new JComboBox();
			cbStage.addItem(I18N.getMsg("change.no"));
			List<String> stages = Assistant.getListOf("scene", "stage");
			for (String st : stages) {
				cbStage.addItem(st);
			}
			add(cbStage);
		}
		// ok and cancel button
		add(getCancelButton(), MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChangeParam param = new ChangeParam(cbStatus, cbChapters, cbStrands, cbNarrator, intensity, ckInfo, ckInfoYes, cbStage);
				//LOG.printInfos("param: " + param.toString());
				if (param.todo()) {
					for (AbstractEntity entity : scenes) {
						changeScene((Scene) entity, param);
					}
					mainFrame.setUpdated();
					canceled = false;
				} else {
					canceled = true;
				}
				dispose();
			}
		};
	}

	private void changeScene(Scene scene, ChangeParam param) {
		//LOG.printInfos(TT + ".changeScene(scene=" + scene.getName() + ",param=[" + param.toString() + "])");
		if (param.getStatus() != -1) {
			scene.setStatus(param.getStatus());
		}
		if (param.isChapter()) {
			scene.setChapter(param.getChapter());
		}
		if (param.isStrand()) {
			scene.setStrand(param.getStrand());
		}
		if (param.isNarrator()) {
			scene.setNarrator(param.getNarrator());
		}
		if (param.getIntensity() != -1) {
			scene.setIntensity(param.getIntensity());
		}
		if (param.isInfo()) {
			scene.setInformative(param.getInfoValue());
		}
		if (param.isStage()) {
			scene.setScenario_stage(cbStage.getSelectedIndex() - 1);
		}
		mainFrame.getBookController().updateEntity(scene);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	@SuppressWarnings("unchecked")
	private JComboBox initCb(Book.TYPE type) {
		int min = 1;
		if (type == Book.TYPE.PERSON) {
			add(new JLabel(I18N.getColonMsg("scene.narrator")));
		} else {
			add(new JLabel(I18N.getColonMsg(type.toString())));
		}
		JComboBox cb = new JComboBox();
		cb.setName("cb_" + type.toString());
		if (type == Book.TYPE.STATUS) {
			cb.setRenderer(new StatusLCR());
			CbUtil.fillCbStatus(cb, -1);
		} else {
			// no change
			cb.addItem(I18N.getMsg("change.no"));
			// unassigned
			if (type == Book.TYPE.CHAPTER) {
				cb.addItem(I18N.getMsg("scenes.unassigned"));
				min = 2;
			}
			for (Object c : (List) mainFrame.project.getList(type)) {
				cb.addItem(c);
			}
		}
		cb.setEnabled(cb.getItemCount() > min);
		add(cb);
		return cb;
	}

	private static class ChangeParam {

		int status = -1, intensity = -1;
		boolean bchapter = false, bstrand = false, bnarrator = false, bstage = false;
		Chapter chapter = null;
		Strand strand = null;
		Person narrator;
		private boolean info;
		private boolean infoValue;
		private JComboBox stage;

		public ChangeParam(JComboBox cbStatus,
		   JComboBox cbChapters,
		   JComboBox cbStrands,
		   JComboBox cbNarrator,
		   IntensityPanel intensity,
		   JRadioButton ckInfo,
		   JRadioButton ckInfoValue,
		   JComboBox cbStage) {
			setStatus(cbStatus.getSelectedIndex() - 1);
			setChapter(cbChapters);
			setStrand(cbStrands);
			setNarrator(cbNarrator);
			setIntensity(intensity);
			setInformative(ckInfo, ckInfoValue);
			setStage(cbStage);
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public void setChapter(JComboBox cb) {
			this.bchapter = cb.getSelectedIndex() > 0;
			if (cb.getSelectedIndex() > 1) {
				this.chapter = (Chapter) cb.getSelectedItem();
			}
		}

		public void setStrand(JComboBox cb) {
			this.bstrand = cb.getSelectedIndex() > 0;
			if (this.bstrand) {
				this.strand = (Strand) cb.getSelectedItem();
			}
		}

		public void setStage(JComboBox cb) {
			if (cb != null) {
				this.bstage = cb.getSelectedIndex() > 0;
				if (this.bstage) {
					this.stage = cb;
				}
			}
		}

		private void setNarrator(JComboBox cb) {
			this.bnarrator = cb.getSelectedIndex() > 0;
			if (this.bnarrator) {
				this.narrator = (Person) cb.getSelectedItem();
			}
		}

		private void setIntensity(IntensityPanel intensity) {
			this.intensity = intensity.getValue();
		}

		private void setInformative(JRadioButton ckInfo, JRadioButton ckInfoValue) {
			this.info = !ckInfo.isSelected();
			this.infoValue = ckInfoValue.isSelected();
		}

		public int getStatus() {
			return status;
		}

		public Chapter getChapter() {
			return chapter;
		}

		public Strand getStrand() {
			return strand;
		}

		public JComboBox getStage() {
			return stage;
		}

		public Person getNarrator() {
			return narrator;
		}

		public int getIntensity() {
			return intensity;
		}

		public boolean isChapter() {
			return bchapter;
		}

		public boolean isStrand() {
			return bstrand;
		}

		public boolean isNarrator() {
			return bstrand;
		}

		public boolean isStage() {
			return bstage;
		}

		public boolean isInfo() {
			return info;
		}

		public boolean getInfoValue() {
			return infoValue;
		}

		public boolean todo() {
			return (status != -1 || bchapter || bstrand || bnarrator || intensity != -1 || info || bstage);
		}

		@Override
		public String toString() {
			return "status=" + status
			   + "\n, chapter=" + (chapter == null ? "null" : chapter.getName())
			   + "\n, strand=" + (strand == null ? "null" : strand.getName())
			   + "\n, narrator=" + (narrator == null ? "null" : narrator.getName())
			   + "\n, intensity=" + intensity
			   + "\n, info=" + (info ? "true" : "false") + ", infoValue=" + (infoValue ? "true" : "false");
		}

	}

}
