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
package storybook.ui.panels.typist;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.endnote.EndnotesListDlg;
import storybook.db.idea.Idea;
import storybook.db.scene.IntensityPanel;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneStateCbModel;
import storybook.db.scene.SceneStateLCR;
import storybook.db.scene.Scenes;
import storybook.db.strand.Strand;
import storybook.edit.Editor;
import storybook.review.ReviewPanel;
import storybook.tools.LOG;
import storybook.tools.Markdown;
import storybook.tools.StringUtil;
import storybook.tools.file.TempUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.panels.AbstractPanel;

public class TypistPanel extends AbstractPanel {

	private static final String TT = "TypistPanel.";

	private static final String CHAPTER = "chapter",
			BT_EXIT = "btExit", BT_IDEA = "btIdea", BT_IGNORE = "btIgnore",
			BT_FIRST = "btFirst", BT_LAST = "btLast",
			BT_PRIOR = "btPrior", BT_NEXT = "btNext",
			BT_NEW = "btNew", BT_SAVE = "btSave",
			BT_STRAND = "btStrand", STRAND_NEW = "new.strand",
			BT_INFO = "btInfo",
			BT_SCREEN = "btScreen", BT_TITLE = "btTitle",
			CB_SCENES = "cbScenes";

	private int origin;
	private Scene scene = null;
	private Dimension screenSize;
	private int defaultHeight, saveSplit;
	private ShefEditor shef;
	private JPanel panelInfo;
	private JComboBox cbScenes, cbStatus;
	private JButton btNewScene,
			btFirst, btPrior, btNext, btLast,
			btScreen, /*btChapter, */ btTitle, btShowInfo,
			btStrand, btHide, btIgnore, btSave;
	private JPanel statusbar;
	private IntensityPanel intensityPanel;
	private JPanel tbTop1, tbMain;
	private boolean toRefresh, hiden = false, modified = false, mdType = false, full = false;
	private JButton btEndnotes;
	private List<Endnote> endnotes;
	private JSplitPane split;
	private ReviewPanel reviewPanel;
	private JCheckBox ckReview;
	private boolean bMarkdown;
	private Markdown mdEdit;
	private TypistInfo typistInfo;
	private JPanel tbTop;
	private boolean shefTB;
	private JComboBox cbChapter;
	private boolean endInit;

	public TypistPanel(MainFrame mainFrame) {
		super(mainFrame);
		this.scene = sceneGetFirst();
		initAll();
	}

	public TypistPanel(MainFrame mainFrame, Scene scene) {
		super(mainFrame);
		if (scene == null) {
			this.scene = sceneGetFirst();
		} else {
			this.scene = (Scene) TempUtil.write(mainFrame, scene);
		}
		initAll();
	}

	@Override
	public void init() {
		this.withPart = false;
		this.removeAll();
		toRefresh = false;
		App.preferences.typistSet(true);
		hiden = App.preferences.typistShowbarGet();
		full = App.preferences.typistFullGet();
		shefTB = App.preferences.typistShefGet();
		bMarkdown = book.getMarkdown();
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		endInit = false;
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1, MIG.HIDEMODE3), "[][]"));
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		defaultHeight = 32;
		add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX));
		add(initTbMain(), MIG.get(MIG.SPAN, MIG.GROWX));
		add(initMiddle(), MIG.get(MIG.GROW));
		add(initReview(), MIG.GROW);
		add(initStatusbar(), MIG.get(MIG.NEWLINE, MIG.SPAN));
		if (!App.preferences.typistShowinfoGet()) {
			infosShowHide();
		}
		sceneSet(scene);
		setHiden(hiden);
		reviewPanel.setVisible(ckReview.isSelected());
		modified = false;
		if (bMarkdown) {
			mdEdit.initSpelling(mainFrame);
		}
		setOrigin();
		tbTop.setVisible(!full);
		refresh();
		endInit = true;
	}

	private Markdown initMarkdown() {
		Markdown md = new Markdown("TypistPanel", Html.TYPE, "");
		md.setCaretPosition(0);
		md.setCallback(this);
		md.setView(Markdown.VIEW.TEXT);
		return md;
	}

	private ShefEditor initShef() {
		ShefEditor sh = new ShefEditor(mainFrame.project.getPath(), "lang_all, allow, colored", scene.getSummary());
		sh.getWysiwyg().addSaveButton(mainFrame);
		sh.getWysiwyg().addImportButton(mainFrame);
		sh.getWysiwyg().addLinkInternalButton(mainFrame);
		shefTB = App.preferences.typistShefGet();
		sh.getWysiwyg().setShowHideTB(shefTB);
		return sh;
	}

	private JPanel initMiddle() {
		//LOG.trace(TT + "initMiddle()");
		panelInfo = new JPanel(new MigLayout());
		typistInfo = new TypistInfo(this, null);
		JScrollPane scroll = new JScrollPane(typistInfo);
		Dimension sx = SwingUtil.getScreenSize();
		scroll.setPreferredSize(new Dimension((int) (sx.width * 0.23), sx.height - (16 * 8)));
		panelInfo.add(scroll, MIG.GROW);
		JPanel p = new JPanel(new MigLayout());
		if (bMarkdown) {
			mdEdit = initMarkdown();
			split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mdEdit, panelInfo);
		} else {
			shef = initShef();
			split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, shef, panelInfo);
		}
		split.setResizeWeight(0.65);
		split.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		p.add(split, MIG.get(MIG.SPAN, MIG.GROW));
		initEndnote();
		return p;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JToolBar initToolbar() {
		//LOG.trace(TT + "initToolbar()");
		super.initToolbar();
		toolbar.setFloatable(false);
		toolbar.setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.HIDEMODE3)));
		toolbar.setName("TypistToolbar");
		toolbar.add(initTbTop(), MIG.GROWX);
		tbTop1 = initTbTopRight();
		toolbar.add(tbTop1, MIG.get(MIG.SPAN, MIG.RIGHT));
		return toolbar;
	}

	/**
	 * initalize left toolbar (hide, change title, new Scene, nav scenes, ignore changes, save,
	 *
	 * @return
	 */
	private JPanel initTbTop() {
		//LOG.trace(TT + "initTB1()");
		tbTop = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		tbTop.setOpaque(false);
		btHide = Ui.initButton("btHide", "", ICONS.K.DP_UP,
				"typist.show_bar", evt -> setHiden(!hiden));
		tbTop.add(btHide);
		cbScenes = Ui.initComboBox(CB_SCENES, "",
				(List) mainFrame.project.getList(Book.TYPE.SCENE),
				scene, !EMPTY, !ALL, e -> sceneSelect());
		cbScenes.setMaximumRowCount(15);
		cbScenes.setMaximumSize(new Dimension(IconUtil.getDefSize() * 16, IconUtil.getDefSize()));
		tbTop.add(cbScenes);
		//title of the Scène, and new Scene
		btTitle = Ui.initButton(BT_TITLE, "", ICONS.K.EDIT,
				"title.change", e -> sceneChangeTitle());
		tbTop.add(btTitle);
		btNewScene = Ui.initButton(BT_NEW, "", ICONS.K.ADD,
				"scene.new", e -> sceneNew());
		tbTop.add(btNewScene);
		tbTop.add(new JLabel(" "));
		//navigation into scènes
		initTbNav();
		// ignore changes
		btIgnore = Ui.initButton(BT_IGNORE, "", ICONS.K.CANCEL,
				"discard.changes", e -> refresh());
		tbTop.add(btIgnore);
		// save changes
		btSave = Ui.initButton(BT_SAVE, "", ICONS.K.F_SAVE,
				"file.save", e -> save());
		tbTop.add(btSave);
		tbTop.add(new JLabel(" "));
		return tbTop;
	}

	/**
	 * initalize the scenes navigation
	 */
	private void initTbNav() {
		btFirst = Ui.initButton(BT_FIRST, "", ICONS.K.NAV_FIRST,
				"export.nav.first", e -> sceneToFirst());
		tbTop.add(btFirst);
		btPrior = Ui.initButton(BT_PRIOR, "", ICONS.K.NAV_PREV,
				"export.nav.prior", e -> sceneToPrior());
		tbTop.add(btPrior);
		btNext = Ui.initButton(BT_NEXT, "", ICONS.K.NAV_NEXT,
				"export.nav.next", e -> sceneToNext());
		tbTop.add(btNext);
		btLast = Ui.initButton(BT_LAST, "", ICONS.K.NAV_LAST,
				"export.nav.last", e -> sceneToLast());
		tbTop.add(btLast);
		tbTop.add(new JLabel(" "));
	}

	/**
	 * initalize top right toolbar for idea, change to normal mode, exit
	 *
	 * @return
	 */
	private JPanel initTbTopRight() {
		//LOG.trace(TT + "initTbTopRight()");
		JPanel tb = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		tb.setOpaque(false);
		tb.setBorder(BorderFactory.createEmptyBorder());
		// idea
		tb.add(Ui.initButton(BT_IDEA, "", ICONS.K.ENT_IDEA,
				"foi.new", e -> mainFrame.showEditorAsDialog(new Idea())));
		tb.add(new JSeparator(), MIG.GROWX);
		//sortie du mode typist
		btScreen = Ui.initButton(BT_SCREEN, "", ICONS.K.SCREEN_NORMAL,
				"screen.normal", e -> returnToMainFrame());
		tb.add(btScreen, MIG.RIGHT);
		// exit
		tb.add(Ui.initButton(BT_EXIT, "", ICONS.K.EXIT,
				"file.exit", e -> doExit()));
		return tb;
	}

	/**
	 * initialize toolbar for chapter, status, intensity and strand
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JPanel initTbMain() {
		//LOG.trace(TT + "initTbMain()");
		tbMain = new JPanel(new MigLayout(MIG.get(MIG.get(MIG.FILLX, MIG.INS0))));
		tbMain.setOpaque(false);
		//panel for main infos (chapter, status, intensity, strand)
		JPanel tbMain1 = new JPanel(new MigLayout(MIG.INS0));
		//chapitre
		tbMain1.add(initLabel(CHAPTER, true));
		cbChapter = Ui.initComboBox("cbChapters", "", mainFrame.project.chapters.getList(),
				null, EMPTY, ALL_NOT);
		cbChapter.setMaximumSize(new Dimension(IconUtil.getDefSize() * 24, IconUtil.getDefSize()));
		cbChapter.addItemListener(e -> changeChapter());
		tbMain1.add(cbChapter);
		//status
		cbStatus = new JComboBox(new SceneStateCbModel(false));
		cbStatus.setName(TypistScenario.CMD.CB_STATUS.toString());
		cbStatus.setSelectedIndex(scene.getStatus());
		cbStatus.setRenderer(new SceneStateLCR());
		tbMain1.add(initLabel("status", true));
		tbMain1.add(cbStatus);
		//intensity
		intensityPanel = new IntensityPanel(scene.getIntensity().get());
		tbMain1.add(initLabel("intensity", true));
		tbMain1.add(intensityPanel);
		//strand
		tbMain1.add(new JLabel(""), MIG.GROWX);
		tbMain1.add(initLabel("strands", true));
		btStrand = new JButton(new ColorIcon(scene.getStrand().getJColor(), IconUtil.getDefSize()));
		btStrand.setName(BT_STRAND);
		btStrand.setToolTipText(scene.getStrand().getName());
		btStrand.addActionListener(this);
		tbMain1.add(btStrand);
		tbMain.add(tbMain1);

		// empty separator
		tbMain.add(new JSeparator(), MIG.GROWX);

		// panel for ckReview and btShowInfo
		JPanel tbMain2 = new JPanel(new MigLayout(MIG.INS0));
		// show/hide review panel
		ckReview = Ui.initCheckBox(null, "cbComment", "comments",
				book.getReview(), BNONE, e -> changeCkReview());
		tbMain2.add(ckReview);
		// show info panel
		btShowInfo = Ui.initButton(BT_INFO, "", ICONS.K.INFO_HIDE,
				"typist.show_infos", e -> doInfo());
		tbMain2.add(btShowInfo);

		tbMain.add(tbMain2, MIG.RIGHT);

		return tbMain;
	}

	/**
	 * initalize status bar
	 *
	 * @return
	 */
	private JPanel initStatusbar() {
		//LOG.trace(TT + "initStatusBar()");
		statusbar = new JPanel();
		statusbar.setName("TypistStatusbar");
		statusbar.setLayout(new MigLayout(MIG.FILLX));
		statusbar.setSize(screenSize.width, defaultHeight);
		return statusbar;
	}

	/**
	 * get current scene
	 *
	 * @return
	 */
	public Scene sceneGet() {
		return this.scene;
	}

	/**
	 * get current Shef editor
	 *
	 * @return
	 */
	public ShefEditor shefGet() {
		if (bMarkdown) {
			return null;
		}
		return this.shef;
	}

	/**
	 * propety change
	 *
	 * @param evt
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		if (propName == null
				|| "SHOWINFO".equalsIgnoreCase(propName)) {
			return;
		}
	}

	/**
	 * refresh the view
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void refresh() {
		//LOG.trace(TT + "refresh()");
		if (scene == null) {
			return;
		}
		if (scene.hasChapter()) {
			cbChapter.setSelectedItem(scene.getChapter());
		} else {
			cbChapter.setSelectedIndex(0);
		}
		cbStatus.setSelectedIndex(scene.getStatus());
		intensityPanel.setValue(scene.getIntensity().get());
		strandRefresh(scene.getStrand());
		typistInfo.refreshInfo(scene);
		cbScenes.removeActionListener(this);
		cbScenes.setSelectedItem(scene);
		cbScenes.addActionListener(this);
		setOrigin();
		mainFrame.lastSceneSet(scene);
		refreshText();
		reviewPanel.refresh();
		setNav(Scenes.find(mainFrame));
		setCaretPosition(0);
		repaint();
	}

	/**
	 * get the carret position
	 *
	 * @return
	 */
	public int getCaretPosition() {
		if (bMarkdown) {
			return mdEdit.getCaretPosition();
		} else {
			return shef.getWysEditor().getCaretPosition();
		}
	}

	/**
	 * set the carret position
	 *
	 * @param pos
	 */
	public void setCaretPosition(int pos) {
		if (bMarkdown) {
			mdEdit.setCaretPosition(0);
		} else {
			shef.setCaretPosition(0);
		}
	}

	/**
	 * refresh the summary text
	 */
	public void refreshText() {
		//LOG.trace(TT + "refreshText()");
		if (scene == null) {
			LOG.err("scene is null");
			return;
		}
		scene = mainFrame.project.scenes.get(scene.getId());
		int curpos = getCaretPosition();
		if (curpos < 0) {
			curpos = 0;
		}
		setText(scene.getSummary());
		if (curpos > (bMarkdown ? mdEdit.getText().length() : shef.getText().length())) {
			curpos = 0;
		}
		setCaretPosition(curpos);
		setOrigin();
	}

	/**
	 * set the summary text
	 *
	 * @param text
	 */
	public void setText(String text) {
		if (bMarkdown) {
			mdEdit.setText(text);
		} else {
			shef.setText(text);
		}
	}

	/**
	 * save the summary text
	 */
	public void saveText() {
		LOG.trace(TT + "saveText()");
		scene = mainFrame.project.scenes.get(scene.getId());
		scene.setSummary(getText());
		mainFrame.getBookController().updateEntity(scene);
		setOrigin();
	}

	/**
	 * get the summary text
	 *
	 * @return
	 */
	public String getText() {
		if (bMarkdown) {
			return mdEdit.getText();
		} else {
			return (shef.getText().isEmpty() ? Html.P_EMPTY : shef.getText());
		}
	}

	/**
	 * set the origin to detect changes
	 */
	public void setOrigin() {
		if (bMarkdown) {
			origin = mdEdit.getText().hashCode();
		} else {
			origin = shef.getText().hashCode();
		}
	}

	/**
	 * set the navigation
	 *
	 * @param scenes
	 */
	private void setNav(List<Scene> scenes) {
		//LOG.trace(TT + "setNav(scenes size=)" + (scenes != null ? scenes.size() : "null"));
		if (scenes == null || scenes.isEmpty() || toolbar == null) {
			return;
		}
		int idx = scenes.indexOf(scene);
		btFirst.setEnabled(true);
		btPrior.setEnabled(true);
		btNext.setEnabled(true);
		btLast.setEnabled(true);
		if (scenes.size() == 1) {
			btFirst.setEnabled(false);
			btPrior.setEnabled(false);
			btNext.setEnabled(false);
			btLast.setEnabled(false);
		} else if (idx == 0) {
			btFirst.setEnabled(false);
			btPrior.setEnabled(false);
		} else if (idx >= scenes.size() - 1) {
			btNext.setEnabled(false);
			btLast.setEnabled(false);
		}
	}

	/**
	 * get the first Scene
	 *
	 * @return
	 */
	private Scene sceneGetFirst() {
		//LOG.trace(TT + "sceneGetFirst()");
		if (scene != null) {
			TempUtil.remove(mainFrame, scene);
		}
		List<Scene> scenes = Scenes.find(mainFrame);
		if (scenes.isEmpty()) {
			return null;
		}
		return scenes.get(0);
	}

	/**
	 * get the last Scene
	 *
	 * @return
	 */
	private Scene sceneGetLast() {
		//LOG.trace(TT + "lastSceneGet()");
		if (scene != null) {
			TempUtil.remove(mainFrame, scene);
		}
		List<Scene> scenes = Scenes.find(mainFrame);
		if (scenes.isEmpty()) {
			return (null);
		}
		return scenes.get(scenes.size() - 1);
	}

	/**
	 * detect if text is too large
	 *
	 * @param topText
	 * @return
	 */
	private boolean textTooLarge(String topText) {
		//LOG.trace(TT + "textToLarge(topText len=" + topText.length() + ")");
		if (topText.length() > 32767) {
			String str = I18N.getMsg("editor.text_too_large");
			JOptionPane.showMessageDialog(this, str,
					I18N.getMsg("editor"), JOptionPane.ERROR_MESSAGE);
			return (true);
		}
		return (false);
	}

	/**
	 * check if modified
	 *
	 * @return
	 */
	public int askModified() {
		//LOG.trace(TT + "askModified()");
		if (scene != null && endInit) {
			String topText = getText();
			int nhash = topText.hashCode();
			if (origin != nhash) {
				if (textTooLarge(topText)) {
					return (JOptionPane.CANCEL_OPTION);
				}
				updatedSet();
			}
		}
		if (modified && endInit) {
			final Object[] options = {
				I18N.getMsg("save.changes"),
				I18N.getMsg("discard.changes"),
				I18N.getMsg("cancel")
			};
			String str = I18N.getMsg("save.or.discard.changes") + "\n\n"
					+ I18N.getMsg(scene.getObjType().toString()) + ": "
					+ scene.toString() + "\n\n";
			int n = JOptionPane.showOptionDialog(this, str, I18N.getMsg("save.changes.title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			switch (n) {
				case JOptionPane.CANCEL_OPTION:
					return JOptionPane.CANCEL_OPTION;
				case JOptionPane.YES_OPTION:
					save();
					break;
				case JOptionPane.NO_OPTION:
					break;
				default:
					break;
			}
		}
		return JOptionPane.YES_OPTION;
	}

	/**
	 * create a new Scene
	 */
	private void sceneNew() {
		//LOG.trace(TT + "sceneNew()");
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		Scene s = new Scene();
		s.setChapter(scene.getChapter());
		JDialog dlg = new JDialog(mainFrame, true);
		dlg.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		Editor editor = new Editor(mainFrame, s, dlg);
		dlg.setTitle(I18N.getMsg("editor"));
		Dimension pos = SwingUtil.getDlgPosition(s);
		dlg.add(editor);
		Dimension size = SwingUtil.getDlgSize(s);
		if (size != null) {
			dlg.setSize(size.height, size.width);
		} else {
			dlg.setSize(this.getWidth() / 2, 680);
		}
		if (pos != null) {
			dlg.setLocation(pos.height, pos.width);
		} else {
			dlg.setLocationRelativeTo(this);
		}
		dlg.setVisible(true);
		if (editor.canceled) {
			return;
		}
		TempUtil.remove(mainFrame, s, true);
		if (editor.entity != null) {
			//scene = (Scene) editor.entity;
			cbScenesRefresh();
			updatedReset();
			this.sceneSet((Scene) editor.entity);
			//cbScenes.setSelectedItem(scene);
		}
		SwingUtil.saveDlgPosition(dlg, s);
		refresh();
	}

	/**
	 * refresh the scenes
	 */
	@SuppressWarnings("unchecked")
	private void cbScenesRefresh() {
		//LOG.trace(TT + "cbScenesRefresh()");
		cbScenes.removeActionListener(this);
		DefaultComboBoxModel combo = (DefaultComboBoxModel) cbScenes.getModel();
		combo.removeAllElements();
		List<Scene> scenes = Scenes.find(mainFrame);
		for (Scene entity : scenes) {
			combo.addElement(entity);
		}
		cbScenes.addActionListener(this);
	}

	/**
	 * reload the scenes
	 */
	@SuppressWarnings("unchecked")
	private void cbScenesReload() {
		//LOG.trace(TT + "cbScenesReload()");
		int idx = cbScenes.getSelectedIndex();
		cbScenes.removeActionListener(this);
		DefaultComboBoxModel combo = (DefaultComboBoxModel) cbScenes.getModel();
		combo.removeAllElements();
		List<Scene> scenes = Scenes.find(mainFrame);
		for (Scene entity : scenes) {
			combo.addElement(entity);
		}
		cbScenes.setSelectedIndex(idx);
		cbScenes.addActionListener(this);
	}

	/**
	 * change the Strand
	 *
	 * @param name
	 */
	private void strandChange(String name) {
		//LOG.trace(TT + "strandChange()");
		String s[] = name.split("_");
		if (s.length > 1 && StringUtil.isNumeric(s[1])) {
			Long id = Long.valueOf(s[1]);
			Strand strand = (Strand) mainFrame.project.get(Book.TYPE.STRAND, id);
			if (strand != null) {
				scene.setStrand(strand);
				btStrand.setIcon(new ColorIcon(strand.getJColor(), IconUtil.getDefSize()));
				btStrand.setToolTipText(strand.getName());
				updatedSet();
			}
		}
	}

	/**
	 * change the Chapter
	 */
	/*private void chapterChange() {
		LOG.trace(TT + "chapterChange()");
		ChapterSelectDlg dlg = new ChapterSelectDlg(this, scene.getChapter());
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			Chapter chapter = dlg.getSelectedChapter();
			scene.setChapter(chapter);
			btChapter.setText(chapter.getName());
			updatedSet();
		}
	}*/
	/**
	 * change the title
	 */
	private void sceneChangeTitle() {
		//LOG.trace(TT + "sceneChangeTitle()");
		String s = (String) JOptionPane.showInputDialog(
				this, "", I18N.getMsg("title"),
				JOptionPane.PLAIN_MESSAGE, null, null, scene.getTitle());
		if ((s != null) && (s.length() > 0)) {
			scene.setTitle(s);
			save();
			cbScenesReload();
		}
	}

	/**
	 * save all changes
	 *
	 * @return
	 */
	private boolean save() {
		//LOG.trace(TT + "save()");
		App.preferences.typistShefSet(shef.getWysiwyg().getShowHideTB());
		String topText = getText();
		if (!Html.htmlToText(topText).isEmpty()) {
			topText = topText.replace("--", "—&nbsp;");
			if (textTooLarge(topText)) {
				return (false);
			}
		}
		Chapter c = null;
		if (cbChapter.getSelectedIndex() > 0) {
			c = (Chapter) cbChapter.getSelectedItem();
		}
		scene.setChapter(c);
		scene.setSummary(topText);
		scene.setStatus(cbStatus.getSelectedIndex());
		scene.setIntensity(intensityPanel.getValue());
		mainFrame.getBookController().updateEntity(scene);
		toRefresh = true;
		updatedReset();
		setNav(Scenes.find(mainFrame));
		return true;
	}

	/**
	 * set buttons
	 */
	public void setButtons() {
		int hash = getText().hashCode();
		if (origin != hash) {
			modified = true;
		}
		if (toolbar != null) {
			btSave.setEnabled(modified);
			btIgnore.setEnabled(modified);
		}
	}

	/**
	 * set update flag
	 */
	public void updatedSet() {
		modified = true;
		setButtons();
	}

	/**
	 * remove update flag
	 */
	public void updatedReset() {
		modified = false;
		this.origin = getText().hashCode();
		setButtons();
	}

	/**
	 * close the Scene
	 */
	public void sceneClose() {
		//LOG.trace(TT + "sceneClose()");
		if (modified) {
			save();
		}
		if (scene != null) {
			TempUtil.remove(mainFrame, scene, true);
		}
	}

	/**
	 * save temporary
	 */
	public void tempSave() {
		//LOG.trace(TT + "tempSave()");
		String svt = scene.getSummary();
		scene.setSummary(getText());
		TempUtil.write(mainFrame, scene);
		scene.setSummary(svt);
	}

	/**
	 * do exit function
	 */
	private void doExit() {
		//LOG.trace(TT + "doExit()");
		if (!save()) {
			return;
		}
		mainFrame.fileSave(true);
		SwingUtilities.invokeLater(() -> {
			mainFrame.close(true);
		});
	}

	/**
	 * go to first Scene
	 */
	private void sceneToFirst() {
		//LOG.trace(TT + "sceneToFirst()");
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		sceneSet(sceneGetFirst());
	}

	/**
	 * go to last Scene
	 */
	private void sceneToLast() {
		//LOG.trace(TT + "sceneToLast()");
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		sceneSet(sceneGetLast());
	}

	/**
	 * go to prior Scene
	 */
	private void sceneToPrior() {
		//LOG.trace(TT + "sceneToPrior()");
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		setNextScene(-1);
	}

	/**
	 * go to next Scene
	 */
	private void sceneToNext() {
		//LOG.trace(TT + "sceneToNext()");
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		setNextScene(1);
	}

	/**
	 * do showing info panel
	 */
	private void doInfo() {
		//LOG.trace(TT + "doInfo()");
		infosShowHide();
		App.preferences.typistShowinfoSet(panelInfo.isVisible());
		//mainFrame.setUpdated();
	}

	/**
	 * return to the MainFrame view
	 */
	private void returnToMainFrame() {
		//LOG.trace(TT + "returnToMainFrame()");
		App.preferences.typistShefSet(shef.getWysiwyg().getShowHideTB());
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (toRefresh) {
			SwingUtil.setWaitingCursor(this);
			mainFrame.refresh();
			SwingUtil.setDefaultCursor(this);
		}
		App.preferences.typistSet(false);
		sceneClose();
		mainFrame.typistActivate();
	}

	/**
	 * select a scene
	 */
	private void sceneSelect() {
		//LOG.trace(TT + "sceneSelect()");
		if (askModified() == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (cbScenes.getSelectedItem() != null) {
			Scene s = (Scene) cbScenes.getSelectedItem();
			if (!s.equals(scene)) {
				sceneSet(s);
			}
		}
	}

	/**
	 * performed action
	 *
	 * @param evt
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		//LOG.trace(TT + "actionPerformed(evt=" + evt.toString() + ")");
		JComponent source = (JComponent) evt.getSource();
		if (source.getName().startsWith("strand_")) {
			strandChange(source.getName());
			return;
		}
		switch (source.getName()) {
			case BT_STRAND:
				strandSelect(evt);
				break;
			case STRAND_NEW:
				strandNew();
				break;
			default:
				break;
		}
	}

	/**
	 * show/hide info panel
	 */
	private void infosShowHide() {
		//LOG.trace(TT + "infosShowHide()");
		if (panelInfo.isVisible()) {
			saveSplit = split.getDividerLocation();
		}
		panelInfo.setVisible(!panelInfo.isVisible());
		if (panelInfo.isVisible()) {
			btShowInfo.setIcon(IconUtil.getIconSmall(ICONS.K.INFO_HIDE));
			split.setDividerLocation(saveSplit);
		} else {
			btShowInfo.setIcon(IconUtil.getIconSmall(ICONS.K.INFO));
		}
		split.setEnabled(panelInfo.isVisible());
	}

	/**
	 * set the current Scene
	 *
	 * @param s
	 */
	private void sceneSet(Scene s) {
		//LOG.trace(TT + "setScene()");
		if (s.equals(scene)) {
			return;
		}
		System.gc();
		scene = s;
		cbScenes.removeActionListener(this);
		cbScenes.setSelectedItem(s);
		cbScenes.addActionListener(this);
		refresh();
		reviewPanel.setScene(s);
		setCaretPosition(0);
		updatedReset();
	}

	/**
	 * set current Scene to the indexed given
	 *
	 * @param i
	 */
	private void setNextScene(int i) {
		//LOG.trace(TT + "setNextScene(i=" + i + ")");
		int n1 = cbScenes.getSelectedIndex() + i;
		if (n1 > -1 && n1 < cbScenes.getItemCount()) {
			cbScenes.setSelectedItem(cbScenes.getItemAt(n1));
		}
	}

	/**
	 * get a MenuItem with the given name
	 *
	 * @param name
	 * @return
	 */
	private JMenuItem getMenuItem(String name) {
		JMenuItem m = new JMenuItem(I18N.getMsg(name));
		m.setName(name);
		m.setText(I18N.getMsg(name));
		m.setIcon(IconUtil.getIconSmall(ICONS.K.NEW));
		m.addActionListener(this);
		return m;
	}

	/**
	 * get a MenuItem for the given Strand
	 *
	 * @param strand
	 * @return
	 */
	private JMenuItem getMenuItem(Strand strand) {
		JMenuItem m = new JMenuItem(strand.getName());
		m.setName("strand_" + strand.getId().toString());
		m.setIcon(new ColorIcon(strand.getJColor(), IconUtil.getDefSize()));
		m.addActionListener(this);
		return m;
	}

	private void strandsAction() {
		//LOG.trace(TT + "strandsAction()");
		TypistEntitiesListDlg dlg = new TypistEntitiesListDlg(this, scene, "strand");
		dlg.setModal(true);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setStrands(dlg.getStrands());
			updatedSet();
		}
	}

	/**
	 * select a Strand
	 *
	 * @param evt
	 */
	private void strandSelect(ActionEvent evt) {
		//LOG.trace(TT + "strandSelect()");
		JPopupMenu menu = new JPopupMenu();
		JButton bt = (JButton) evt.getSource();
		for (Object strand : (List) mainFrame.project.getList(Book.TYPE.STRAND)) {
			menu.add(getMenuItem((Strand) strand));
		}
		menu.add(new JSeparator());
		JMenuItem addstrand = getMenuItem(STRAND_NEW);
		menu.add(addstrand);
		menu.show(bt, 0, bt.getBounds().height);
	}

	/**
	 * refresh the given Strand
	 *
	 * @param strand
	 */
	private void strandRefresh(Strand strand) {
		//LOG.trace(TT + "strandRefresh(" + LOG.trace(strand) + ")");
		scene.setStrand(strand);
		btStrand.setIcon(new ColorIcon(scene.getStrand().getJColor(), IconUtil.getDefSize()));
		btStrand.setToolTipText(scene.getStrand().getName());
	}

	/**
	 * create a new Strand
	 */
	private void strandNew() {
		//LOG.trace(TT + "strandNew()");
		Strand strand = new Strand();
		boolean r = mainFrame.showEditorAsDialog(strand);
		if (!r) {
			strandRefresh(strand);
			updatedSet();
		}
	}

	/**
	 * set full screen mode
	 */
	public void setFull() {
		//LOG.trace(TT + "setFull()");
		full = !full;
		App.preferences.typistFullSet(full);
		toolbar.setVisible(!full);
		shef.getWysiwyg().setShowHideTB(full);
		revalidate();
	}

	/**
	 * set the deploy button
	 */
	private void setHiden(boolean b) {
		//LOG.trace(TT + "setHiden(b=" + (b ? "true" : "false") + ") hiden=" + (hiden ? "true" : "false"));
		hiden = b;
		tbMain.setVisible(hiden);
		btHide.setIcon(IconUtil.getIconSmall((hiden ? ICONS.K.DP_UP : ICONS.K.DP_DOWN)));
		App.preferences.typistShowbarSet(hiden);
	}

	/**
	 * initialize the Reviw panel, not usable for Markdown
	 *
	 * @return
	 */
	private JPanel initReview() {
		LOG.trace(TT + "initReview()");
		if (bMarkdown) {

		} else {
			shef.wysEditorGet().tb2Get().addSeparator();
		}
		reviewPanel = new ReviewPanel(mainFrame, this);
		reviewPanel.setVisible(book.getReview());
		return reviewPanel;
	}

	/**
	 * initialize the endnote functions not visible for Markdown
	 *
	 * @return a JButton for activate the list of Endnotes
	 */
	@SuppressWarnings("unchecked")
	private void initEndnote() {
		LOG.trace(TT + "initEndnote()");
		if (!bMarkdown) {
			endnotes = mainFrame.project.endnotes.find(Endnote.TYPE.ENDNOTE, scene);
			btEndnotes = Endnote.getButtonAdd(e -> {
				Endnote.create(mainFrame, Endnote.TYPE.ENDNOTE, scene, shef);
				endnotes = mainFrame.project.endnotes.find(Endnote.TYPE.ENDNOTE, scene);
				btEndnotes.setEnabled(!endnotes.isEmpty());
			});
			shef.wysEditorGet().tb2Get().add(btEndnotes);
			btEndnotes = Endnote.getButtonShow(e -> EndnotesListDlg.showDlg(mainFrame, 0, scene));
			btEndnotes.setEnabled(!endnotes.isEmpty());
		}
	}

	/**
	 * show/hide review panel
	 */
	private void changeCkReview() {
		LOG.trace(TT + "changeCkReview()");
		book.setReview(!book.getReview());
		reviewPanel.setVisible(book.getReview());
	}

	/**
	 * reset modified flag
	 */
	public void resetModified() {
		this.modified = false;
	}

	private void changeChapter() {
		updatedSet();
	}

}
