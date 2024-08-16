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
package storybook.db.scene;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import assistant.Assistant;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.db.DB.DATA;
import storybook.db.EntityCb;
import storybook.db.EntityCbItem;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.Book.TYPE;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.strand.Strand;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.exim.importer.ImportDocument;
import storybook.tools.LOG;
import storybook.tools.Markdown;
import storybook.tools.SbDuration;
import storybook.tools.StringUtil;
import storybook.tools.TextUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.file.TempUtil;
import storybook.tools.file.XEditorFile;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;
import storybook.tools.swing.js.JSDateChooser;
import storybook.tools.zip.ZipXml;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 * panel editor for Scene
 *
 * @author favdb
 */
public class SceneEdit extends AbstractEditor implements CaretListener, ItemListener {

	private static final String TT = "EditScene",
		INITIALES = I18N.getMsg("duration.initiales"),
		BT_STAGE = "btStage",
		RB_DATENONE = "date.none",
		RB_DATEFIX = "date.fixed",
		RB_DATEREL = "date.relative";

	private JPanel pRight, pLeft, pMain, pDate, pRel, datePanel;
	private JTextField tfNumber, tfDuration, fRelTime, xfile;
	private JComboBox cbChapter, cbStrand, cbStage, cbRelScene, cbStatus, cbNarrator;
	private JSDateChooser dcDate;
	private JSCheckList lLocations, lItems, lPersons, lPlots, lStrands;
	private JCheckBox ckInformative;
	private JRadioButton rbDatenone, rbAbs, rbRel;
	private ShefEditor shefEditor;
	private Markdown mkTexte;
	private ScenarioPanel pScenario;
	private Scene scene;
	private JButton btImport, btExternal;
	private IntensityPanel pIntensity;
	private List<Endnote> endnotes;
	private JLabel lbXsize;

	public SceneEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		if (e == null) {
			LOG.err(TT + " : entity is null");
			return;
		}
		initAll();
	}

	public Scene getScene() {
		return scene;
	}

	/**
	 * initialize the upper datas
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initUpper() {
		pMain = new JPanel(
			new MigLayout(MIG.get(MIG.INS1, MIG.WRAP), "[50%][50%]"));
		pLeft = new JPanel(
			new MigLayout(MIG.get(MIG.INS1, MIG.WRAP), "[][grow]"));
		pRight = new JPanel(
			new MigLayout(MIG.get(MIG.INS1, MIG.WRAP), "[][grow]"));
		scene = (Scene) entity;
		pUpper.setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.WRAP, MIG.HIDEMODE2), "[][grow][]"));
		if (scene.getId() == -1 && scene.getChapter() == null) {
			scene.setChapter(mainFrame.lastChapterGet());
		}
		initLeft();
		//date and duration
		initRight();
		pMain.add(pLeft);
		pMain.add(pRight, MIG.TOP);
		pScenario = new ScenarioPanel(this);
		if (book.info.scenarioGet()) {
			JTabbedPane tb0 = new JTabbedPane();
			tb0.add(I18N.getMsg("scenario"), pScenario);
			tb0.add(I18N.getMsg("scene"), pMain);
			pUpper.add(tb0, MIG.get(MIG.GROWX, MIG.SPAN));
		} else {
			pUpper.add(pMain, MIG.get(MIG.SPAN, MIG.GROW));
		}
		//stage
		if (book == null || book.info == null) {
			LOG.err(TT + " book or book.info is null, may crash");
		}
		// end of common
		initSummary();
		initLinks();
	}

	private void initLeft() {
		//number
		Ui.addLabel(pLeft, "number", false, MANDATORY);
		tfNumber = Ui.getStringField(DATA.NUMBER, 5, scene.getSceneno().toString(), BNONE);
		pLeft.add(tfNumber);
		if (scene.getSceneno() < 1) {
			tfNumber.setText("+");
		}
		//status
		cbStatus = Ui.initStatus(pLeft, scene.getStatus(), BMANDATORY);
		//informative
		ckInformative = Ui.initCheckBox(pHead, "ckInformative", "informative",
			scene.getInformative(), BNONE);
		ckInformative.setToolTipText(I18N.getMsg("informative.tip"));
		//intensity
		Ui.addLabelNew(pLeft, "intensity", false, MANDATORY);
		pIntensity = new IntensityPanel(scene.getIntensity());
		pLeft.add(pIntensity, MIG.WRAP);
		//chapter
		if (scene.getName().isEmpty() && scene.getChapter() == null) {
			scene.setChapter(mainFrame.lastChapterGet());
		}
		cbChapter = Ui.getCB(pLeft, this, TYPE.CHAPTER, scene.getChapter(),
			null, BNEW + BEMPTY);
		SwingUtil.setCBsize(cbChapter);
		//strand
		cbStrand = Ui.getCB(pLeft, this, TYPE.STRAND, scene.getStrand(),
			null, BMANDATORY + BNEW + BEMPTY);
		if (scene.getStrand() == null) {
			cbStrand.setSelectedIndex(1);
		}
		cbStrand.addItemListener(this);
	}

	private void initRight() {
		datePanel = initDatePanel();
		pRight.add(datePanel, MIG.get(MIG.CENTER, MIG.SPAN));
		//stage
		JPanel px = new JPanel(new MigLayout(MIG.INS1));
		if (App.getAssistant().isExists("book")) {
			cbStage = Ui.initCbStrings(pRight, this, "scenario.stage",
				Assistant.getListOf("book", "step"),
				scene.getScenariostage(), BEMPTY);
			cbStage.setToolTipText(I18N.getMsg("scenario.stage_tips"));
		}
		//pRight.add(px, MIG.SPAN);
		//narrator
		if (!mainFrame.project.getList(TYPE.PERSON).isEmpty()) {
			cbNarrator = Ui.initCbEntities(pRight, this, "scene.narrator", TYPE.PERSON,
				scene.getNarrator(),
				null, BNEW + BEMPTY);
		}
	}

	private JPanel initDatePanel() {
		//LOG.trace(TT + ".initDatePanel() date type=" + scene.getDateType());
		JPanel gpDate = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.INS0)));
		gpDate.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("date")));
		rbDatenone = Ui.initRadioButton(gpDate, RB_DATENONE, false, BNONE,
			MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		rbAbs = Ui.initRadioButton(gpDate, RB_DATEFIX, false, BNONE);
		rbRel = Ui.initRadioButton(gpDate, RB_DATEREL, false, BNONE, MIG.WRAP);
		//fixed date
		pDate = new JPanel(new MigLayout());
		dcDate = initSbDate(pDate, "date", scene.getScenets(), BNONE, 0);
		gpDate.add(pDate, MIG.SPAN);
		//relative date
		pRel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[][]"));
		cbRelScene = new EntityCb(mainFrame, TYPE.SCENE, scene.getRelativesceneid(), scene, "10");
		SwingUtil.setCBsize(cbRelScene);
		pRel.add(new JLabel(I18N.getColonMsg("scene.relative")));
		pRel.add(cbRelScene);
		//gap with format yy-MM-dd_hh:mm:ss
		String t = "";
		if (!scene.getRelativetime().isEmpty()) {
			SbDuration dur = new SbDuration(scene.getRelativetime());
			t = dur.toText(INITIALES);
		}
		fRelTime = Ui.initStringField(pRel, "scene.relativetime", 16, t, BNONE);
		fRelTime.setToolTipText(I18N.getMsg("duration.format")
			+ "\n" + I18N.getMsg("duration.use"));

		gpDate.add(pRel, MIG.SPAN);
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbDatenone);
		bg.add(rbAbs);
		bg.add(rbRel);
		setGroupDate();
		rbDatenone.addActionListener(this);
		rbAbs.addActionListener(this);
		rbRel.addActionListener(this);
		//duration
		String tDuration = "";
		if (!scene.getDuration().isEmpty() || !scene.getDuration().equals("00-00-00_00:00:00")) {
			SbDuration dur = new SbDuration(scene.getDuration());
			tDuration = dur.toText(INITIALES);
		}
		tfDuration = Ui.initStringField(gpDate, "duration", 16, tDuration, BNONE);
		tfDuration.setToolTipText(I18N.getMsg("duration.format") + "\n" + I18N.getMsg("duration.use"));
		if (!scene.getRelativetime().isEmpty()) {
			rbRel.setSelected(true);
			pDate.setVisible(false);
			pRel.setVisible(true);
		} else if (scene.getScenets() != null) {
			rbAbs.setSelected(true);
			pDate.setVisible(true);
			pRel.setVisible(false);
		} else {
			pDate.setVisible(false);
			pRel.setVisible(false);
		}
		return gpDate;
	}

	private void initScenario(JPanel p1) {
		pScenario = new ScenarioPanel(this);
		if (book.info.scenarioGet()) {
			//pScenario.setScene(scene);
			JTabbedPane tb0 = new JTabbedPane();
			tb0.add(I18N.getMsg("scenario"), pScenario);
			tb0.add(I18N.getMsg("scene"), p1);
			pUpper.add(tb0, MIG.get(MIG.GROWX, MIG.SPAN));
		} else {
			//JScrollPane scroll = new JScrollPane(p1);
			//pUpper.add(p1, MIG.get(MIG.GROW, MIG.SPAN));
		}
	}

	private void initSummary() {
		if (book.info.markdownGet()) {
			mkTexte = new Markdown(TT, "text/plain", scene.getSummary());
			mkTexte.setView(Markdown.VIEW.TEXT);
			mkTexte.setDimension();
			mkTexte.setPreferredSize(MINIMUM_SIZE);
			tab1.add(mkTexte, I18N.getMsg("scene.summary"));
		} else {
			JPanel txtPanel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0)));
			shefEditor = new ShefEditor(mainFrame.project.getPath(), "lang_all, allow, colored", scene.getSummary());
			shefEditor.setBase(mainFrame.getProject().getPath());
			endnotes = mainFrame.project.endnotes.find(Endnote.TYPE.ENDNOTE, scene);
			shefEditor.setPreferredSize(MINIMUM_SIZE);
			shefEditor.setCaretPosition(0);
			shefEditor.getWysiwyg().addSaveButton(mainFrame);
			shefEditor.getWysiwyg().addImportButton(mainFrame);
			shefEditor.getWysiwyg().addLinkInternalButton(mainFrame);
			JScrollPane scroller = new JScrollPane(shefEditor);
			SwingUtil.setUnitIncrement(scroller);
			scroller.setPreferredSize(SwingUtil.getScreenSize());
			txtPanel.add(scroller, MIG.GROW);
			initEndnote();
			tab1.add(txtPanel, I18N.getMsg("scene.summary"));
		}
	}

	private void initLinks() {
		JPanel links = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP),
			"[grow][grow][grow]"));
		lPersons = Ui.initCkList(links, mainFrame, TYPE.PERSON,
			scene.getPersons(), null, BBORDER);
		lLocations = Ui.initCkList(links, mainFrame, TYPE.LOCATION,
			scene.getLocations(), null, BBORDER);
		lItems = Ui.initCkList(links, mainFrame, TYPE.ITEM,
			scene.getItems(), null, BBORDER);
		lPlots = Ui.initCkList(links, mainFrame, TYPE.PLOT,
			scene.getPlots(), null, BBORDER);
		lStrands = Ui.initCkList(links, mainFrame, TYPE.STRAND,
			scene.getStrands(), null, BBORDER);
		lStrands.removeEntity(scene.getStrand());
		JScrollPane scroll = new JScrollPane(links);
		tab1.add(scroll, I18N.getMsg("links"));
	}

	/**
	 * initialize the bottom datas
	 */
	@Override
	public void initBottom() {
		super.initBottom();
		JPanel pxFile = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.FLOWX, MIG.INS0)));
		String fname = IOUtil.fileToAbsolute(mainFrame.getProject().getPath(), scene.getOdf());
		xfile = initFileChooser(pxFile, "scene.xfile", fname);
		lbXsize = new JLabel();
		lbXsize.setFont(FontUtil.getSmall());
		pxFile.add(lbXsize, MIG.GAP0);
		Icon xicon = IconUtil.getIconSmall(ICONS.K.EDIT);
		switch (book.getParam().getParamEditor().getName().toLowerCase()) {
			case "ms word":
				xicon = IconUtil.getIconSmall(ICONS.K.MSWORD);
				break;
			case "libreoffice":
				xicon = IconUtil.getIconSmall(ICONS.K.WRITER);
				break;
		}
		JPanel pp = new JPanel(new MigLayout());
		btImport = SwingUtil.createButton("", "", "", true, this);
		//btImport.setText(I18N.getMsg("import");
		btImport.setIcon(IconUtil.getIconSmall(ICONS.K.BK_REST));
		btImport.setToolTipText(I18N.getMsg("import.text"));
		btImport.addActionListener(e -> importText());
		pp.add(btImport, MIG.get(MIG.SG, MIG.RIGHT));
		btExternal = SwingUtil.createButton("", "", "", true, this);
		//btExternal.setText(book.getParamEditor().getName());
		btExternal.setIcon(xicon);
		btExternal.setToolTipText(I18N.getMsg("xeditor.launching")
			+ " " + book.getParam().getParamEditor().getName());
		btExternal.addActionListener((ActionEvent evt) -> {
			if (xfile.getText().isEmpty()) {
				return;
			}
			String name = xfile.getText();
			XEditorFile.createFile(mainFrame, name);
			name = XEditorFile.launchExternal(name);
			xfile.setText(name);
		});
		pp.add(btExternal);
		pxFile.add(pp, MIG.get(MIG.SG, MIG.RIGHT));
		add(pxFile, MIG.GROWX);
		setExportImport();
		xfile.addCaretListener(this);
		if (!book.isXeditorUse()) {
			SwingUtil.setEnable(pxFile, false);
		}
	}

	/**
	 * allow or disallow the import button
	 */
	private void setExportImport() {
		if (btImport != null) {
			if (!xfile.getText().isEmpty()) {
				File f = new File(IOUtil.fileToAbsolute(mainFrame.getProject().getPath(), xfile.getText()));
				btImport.setEnabled(f.exists());
				SwingUtil.setErrorForeground(xfile, !f.exists());
			} else {
				btImport.setEnabled(false);
			}
		}
		if (btExternal != null) {
			btExternal.setEnabled(!xfile.getText().isEmpty());
		}
		this.editor.setBtExternal(xfile.getText());
		refeshXeditorSize();
	}

	/**
	 * get the external editor file name
	 *
	 * @return
	 */
	public JTextField getXfile() {
		return xfile;
	}

	/**
	 * get the text of the Scene
	 *
	 * @return
	 */
	public String getText() {
		return (book.info.markdownGet() ? mkTexte.getText()
			: Html.checkImages(mainFrame, shefEditor.getText()));
	}

	/**
	 * verifier for all datas
	 *
	 * @return
	 */
	@Override
	public boolean verifier() {
		resetError();
		@SuppressWarnings("unchecked")
		List<Scene> scenes = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		if (tfNumber.getText().equals("+")) {
			tfNumber.setText(Scene.getNextNumber(scenes).toString());
		}
		if (tfNumber.getText().isEmpty()) {
			errorMsg(tfNumber, Const.ERROR_MISSING);
		} else if (!StringUtil.isNumeric(tfNumber.getText())) {
			errorMsg(tfNumber, Const.ERROR_NOTNUMERIC);
		} else {
			int n = Integer.parseInt(tfNumber.getText());
			Scene c = Scene.findNumber(scenes, n);
			if (c != null
				&& (cbChapter.getSelectedItem() != null && c.getChapter() != null
				&& c.getChapter().equals(cbChapter.getSelectedItem()))
				&& !Objects.equals(c.getId(), entity.getId())) {
				errorMsg(tfNumber, Const.ERROR_EXISTS);
			}
		}
		if (cbStatus.getSelectedIndex() < 0) {
			errorMsg(cbStatus, Const.ERROR_MISSING);
		}
		if (rbRel.isSelected() && !fRelTime.getText().isEmpty()) {
			if (cbRelScene.getSelectedItem() == null) {
				errorMsg(cbRelScene, Const.ERROR_MISSING);
			} else if (fRelTime.getText().equals("?")) {
				Long l = ((EntityCbItem) cbRelScene.getSelectedItem()).getId();
				Scene sx = (Scene) mainFrame.project.scenes.get(l);
				SbDuration st = SbDuration.getFromWords(sx.getSummary());
				fRelTime.setText(st.toText());
			}
			String str = SbDuration.check(fRelTime.getText(), INITIALES);
			if (!str.isEmpty()) {
				errorString(fRelTime, str);
			}
		}
		if (!tfDuration.getText().isEmpty()) {
			String str = SbDuration.check(tfDuration.getText(), INITIALES);
			if (!str.isEmpty()) {
				errorString(tfDuration, str);
			}
		}
		String text;
		JComponent comp;
		if (book.info.markdownGet()) {
			text = mkTexte.getText();
			comp = mkTexte;
		} else {
			text = Html.fileToRel(shefEditor.getText(), mainFrame.getProject().getPath());
			comp = shefEditor;
		}
		if (text.length() > 32768) {
			errorMsg(comp, Const.ERROR_EXCEED);
		}
		return msgError.isEmpty();
	}

	/**
	 * get the name of the Scene
	 *
	 * @return
	 */
	public String getTfName() {
		return tfName.getText();
	}

	/**
	 * apply the changes
	 */
	@Override
	public void apply() {
		//LOG.trace(TT + ".apply()");
		scene.setTitle(tfName.getText());
		scene.setInformative(ckInformative.isSelected());
		// apply for chapter
		if (cbChapter.getSelectedIndex() > 0) {
			scene.setChapter((Chapter) cbChapter.getSelectedItem());
		} else {
			scene.setChapter(null);
		}
		// apply for scene number
		if (tfNumber.getText().equals("+")) {
			scene.setSceneno(mainFrame.project.scenes.getNextNumber(mainFrame, scene.getChapter()));
		} else {
			scene.setSceneno(Integer.valueOf(tfNumber.getText()));
		}
		// apply for strand
		if (cbStrand != null && cbStrand.getSelectedIndex() > 0) {
			scene.setStrand((Strand) cbStrand.getSelectedItem());
		} else if (scene.getStrand() == null) {
			scene.setStrand((Strand) mainFrame.project.getList(TYPE.STRAND).get(0));
		}
		// apply for other datas
		scene.setStatus(cbStatus.getSelectedIndex());
		scene.setIntensity(pIntensity.getValue());
		if (tfDuration.getText().isEmpty()) {
			scene.setDuration("");
		} else {
			SbDuration dur = SbDuration.getFromText(tfDuration.getText(), INITIALES);
			scene.setDuration(dur.toString());
		}
		if (cbNarrator != null && cbNarrator.getSelectedIndex() > 0) {
			scene.setNarrator((Person) cbNarrator.getSelectedItem());
		} else {
			scene.setNarrator(null);
		}
		if (book.isXeditorUse()) {
			scene.setOdf(xfile.getText());
		} else {
			scene.setOdf("");
		}
		// apply for date
		if (rbDatenone.isSelected()) {
			scene.setDate(null);
			scene.setRelativetime("");
			scene.setRelativesceneid(-1L);
		} else if (rbAbs.isSelected()) {
			scene.setDate(dcDate.getDate());
			scene.setRelativetime("");
			scene.setRelativesceneid(-1L);
		} else {
			scene.setDate(null);
			SbDuration d = SbDuration.getFromText(fRelTime.getText(), INITIALES);
			scene.setRelativetime(d.toString());
			scene.setRelativesceneid(((EntityCbItem) cbRelScene.getSelectedItem()).getId());
		}
		// aplly for scenario
		if (book.info.scenarioGet()) {
			pScenario.getScenarioData(scene);
		}
		// apply for text
		String text = getText();
		if (!book.info.markdownGet()) {
			text = applyEndnotes();
		}
		scene.setSummary(text);
		if (cbStage != null && cbStage.getSelectedIndex() != -1) {
			scene.setScenario_stage(cbStage.getSelectedIndex());
		} else {
			scene.setScenario_stage(0);
		}
		// apply for links
		scene.setPersons(getPersons());
		scene.setLocations(getLocations());
		scene.setItems(getItems());
		scene.setPlots(getPlots());
		scene.setStrands(getStrands());

		// setting mainFrame last used data
		if (scene.getChapter() != null) {
			mainFrame.lastChapterSet(scene.getChapter());
		}
		if (scene.hasDate()) {
			mainFrame.lastDateSet(scene.getDate());
		}
		super.apply();
	}

	public Markdown getMkTexte() {
		return mkTexte;
	}

	/**
	 * perform a model property change
	 *
	 * @param evt
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	/**
	 * set the date in a group data
	 */
	private void setGroupDate() {
		pRel.setVisible(false);
		pDate.setVisible(false);
		switch (scene.getDateType()) {
			case 1:
				rbAbs.setSelected(true);
				pDate.setVisible(rbAbs.isSelected());
				break;
			case 2:
				rbRel.setSelected(true);
				pRel.setVisible(rbRel.isSelected());
				break;
			default:
				rbDatenone.setSelected(true);
				break;
		}
	}

	private void importText() {
		File f = new File(xfile.getText());
		if (f.exists()) {
			if (!(Html.htmlToText(shefEditor.getText())).isEmpty()) {
				String buttonTexts[] = {
					I18N.getMsg("import.text.replace"),
					I18N.getMsg("import.text.copy"),
					I18N.getMsg("cancel")};
				int ret = JOptionPane.showOptionDialog(this,
					I18N.getMsg("import.text.exists"),
					I18N.getMsg("import"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,
					buttonTexts,
					null);
				switch (ret) {
					case JOptionPane.YES_OPTION:
						//simply replace
						break;
					case JOptionPane.NO_OPTION:
						hNotes.setText(hNotes.getText() + "\n" + shefEditor.getText());
						break;
					case JOptionPane.CANCEL_OPTION:
					default:
						return;
				}
			}
			ImportDocument doc = new ImportDocument(mainFrame, f);
			if (doc.openDocument()) {
				shefEditor.setText(doc.getDocument().body().html());
				doc.close();
			}
		} else {
			JOptionPane.showMessageDialog(this,
				I18N.getMsg("file.not.exist", xfile.getText()),
				I18N.getMsg("file.import"),
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * perform an action
	 *
	 * @param evt
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		//App.traceEvent(TT+".actionPerformed", evt);
		if (evt.getSource() instanceof JButton) {
			JButton bt = (JButton) evt.getSource();
			switch (bt.getName()) {
				case BT_STAGE:
					Net.openBrowser(I18N.getMsg("assistant.stage.web"));
					break;
				default:
					break;
			}
		} else if (evt.getSource() instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) evt.getSource();
			pDate.setVisible(false);
			pRel.setVisible(false);
			switch (rb.getName()) {
				case RB_DATEFIX:
					pDate.setVisible(true);
					break;
				case RB_DATEREL:
					pRel.setVisible(true);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * temporary toXml
	 */
	@Override
	public void tempSave() {
		if (entity instanceof Scene) {
			//EntityUtil.copyEntityProperties(mainFrame, entity, scene);
			if (book.info.scenarioGet()) {
				scene.setSummary(mkTexte.getText());
			} else {
				scene.setSummary(shefEditor.getText());
			}
			TempUtil.write(mainFrame, entity);
			//EntityUtil.copyEntityProperties(mainFrame, scene, entity);
		}
	}

	/**
	 * set the externale editor file name
	 *
	 * @param name
	 */
	public void setXfile(String name) {
		xfile.setText(name);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		setExportImport();
	}

	/**
	 * get the selected Persons
	 *
	 * @return
	 */
	public List<Person> getPersons() {
		List<Person> ls = new ArrayList<>();
		lPersons.getSelectedEntities().forEach((e) -> {
			ls.add((Person) e);
		});
		return ls;
	}

	/**
	 * get the selected Locations
	 *
	 * @return
	 */
	public List<Location> getLocations() {
		List<Location> ls = new ArrayList<>();
		lLocations.getSelectedEntities().forEach((e) -> {
			ls.add((Location) e);
		});
		return ls;
	}

	/**
	 * get the selected Items
	 *
	 * @return
	 */
	public List<Item> getItems() {
		List<Item> ls = new ArrayList<>();
		lItems.getSelectedEntities().forEach((e) -> {
			ls.add((Item) e);
		});
		return ls;
	}

	/**
	 * get the selected Plots
	 *
	 * @return
	 */
	public List<Plot> getPlots() {
		List<Plot> ls = new ArrayList<>();
		lPlots.getSelectedEntities().forEach((e) -> {
			ls.add((Plot) e);
		});
		return ls;
	}

	/**
	 * get the selected Strands
	 *
	 * @return
	 */
	public List<Strand> getStrands() {
		List<Strand> ls = new ArrayList<>();
		lStrands.getSelectedEntities().forEach((e) -> {
			ls.add((Strand) e);
		});
		return ls;
	}

	/**
	 * get the scenario picth text
	 *
	 * @return
	 */
	public String getScenario_pitch() {
		if (pScenario != null) {
			return pScenario.getPitch();
		}
		return "";
	}

	@Override
	@SuppressWarnings("unchecked")
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) e.getSource();
			if (cb.getName().equals(TYPE.STRAND.toString()) && cbStrand.getSelectedIndex() > 0) {
				Strand st = (Strand) cbStrand.getSelectedItem();
				lStrands.removeAll();
				lStrands.reload();
				lStrands.removeEntity(st);
				List strands = scene.getStrands();
				strands.remove(st);
				lStrands.setSelectedEntities(strands);
			}
		}
	}

	/**
	 * restore all endnotes and remove if not exists in text
	 */
	private String applyEndnotes() {
		//LOG.trace(TT+".applyEndnotes()");
		String text = getText();
		for (Endnote en : endnotes) {
			if (!text.contains(Endnote.linkTo("", en))) {
				try {
					mainFrame.getBookModel().ENTITY_Delete(en);
				} catch (Exception ex) {
					//empty
				}
			}
		}
		return text;
	}

	/**
	 * initialize the endnote functions
	 *
	 * @return a JButton for activate the liste of Endnotes
	 */
	@SuppressWarnings("unchecked")
	private void initEndnote() {
		//LOG.trace(TT + ".initEndnote()");
		shefEditor.wysEditorGet().tb2Get().addSeparator();
		// add create endnote button to SHEF toolbar
		shefEditor.wysEditorGet().addEndnoteButtons(mainFrame, scene);
		shefEditor.wysEditorGet().btEndnoteShow.setEnabled(!endnotes.isEmpty());
	}

	/**
	 * action for adding an Endnote
	 *
	 * @return
	 */
	/*private ActionBasic endnoteAdd() {
	ActionBasic act = new ActionBasic("endnote_add", IconUtil.getIconSmall(ICONS.K.CHAR_ENDNOTE));
	act.setAccelerator(Shortcuts.getKeyStroke("shef", "endnote_add"));
	act.setShortDescription(Shortcuts.getTooltips("shef", "endnote_add"));
	return act;
    }*/
	/**
	 * action for showing an Endnote
	 *
	 * @return
	 */
	/*private AbstractAction endnoteShow() {
	return new AbstractAction() {
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		if (endnotes.isEmpty()) {
		    return;
		}
		EndnotesListDlg.showDlg(mainFrame, 0, scene);
	    }
	};
    }
	 */
	/**
	 * refresh size for the external editor label
	 */
	private void refeshXeditorSize() {
		String szfile = "            ";
		if (!xfile.getText().isEmpty()) {
			File file = new File(xfile.getText());
			if (!file.exists()) {
				xfile.setForeground(Color.red);
			} else {
				int nbwords = TextUtil.countWords(ZipXml.getDocumentText(file)), nb = 0;
				if (xfile.getText().endsWith("odt")) {
					nb = TextUtil.countChars(ZipXml.getDocumentText(file, "content.xml", "text:h, text:p"));
				} else if (xfile.getText().endsWith("docx")) {
					nb = TextUtil.countChars(ZipXml.getDocumentText(file, "word/document.xml", "w:t"));
				}
				szfile = String.format("%d (%d)", nbwords, nb);
			}
		}
		this.lbXsize.setText(szfile);
	}

	/**
	 * set the modified flag
	 */
	public void setModified() {
		this.modified = true;
	}
}
