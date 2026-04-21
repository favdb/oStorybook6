/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import api.jortho.SpellChecker;
import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import api.shef.actions.CompoundManager;
import api.shef.actions.EditCopyAction;
import api.shef.actions.EditPasteAction;
import api.shef.actions.EditPasteFormattedAction;
import api.shef.actions.FindReplaceAction;
import api.shef.actions.manager.ActionList;
import api.shef.editors.AbstractPanel;
import static api.shef.editors.wys.HTMLCadratinAction.*;
import api.shef.tools.HtmlUtils;
import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.UndoManager;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const.Spelling;
import storybook.db.scene.Scene;
import storybook.tools.Dictaphone;
import storybook.tools.TextUtil;
import storybook.tools.html.CSS;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.spell.SpellCheck;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSMenuScroller;
import storybook.tools.synonyms.Synonyms;
import storybook.tools.synonyms.Word;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;

/**
 * initial adaptation by favdb (2020)
 *
 */
public class WysiwygEditor extends AbstractPanel implements HyperlinkListener {

	//private static final String TT = "WysiwygEditor";
	private static final String INVALID_TAGS[] = {"html", "head", "body", "title"};
	private JEditorPane wysEditor;
	private JComboBox cbParagraph;
	private JPopupMenu wysPopupMenu;
	private FocusListener focusHandler = new FocusHandler();
	public DocumentListener textChanged = new TextChangedHandler();
	private CaretListener caretHandler = new CaretHandler();
	private MouseListener popupHandler = new PopupHandler();
	private boolean isTextChanged = false;
	private int reduced = 2;//0= no toolbar, 1=reduced toolbar, 2=full toolbar
	private int dlgLang = ShefEditor.DLG_EN;//for dialog open/close, 0=all, 1=english only, 2=french only, 3=none
	private ShefEditor editor;
	private JPanel statusbar, shortcut = new JPanel();
	private JLabel lbStatus;
	private JToolBar tb1, tb11, tb12, tb13, tb14, tb2, linksBar;
	private int maxLen = 32768;
	private JProgressBar progress;
	private boolean tb2Show = true;
	private Dimension tbSize;
	private JMenu synMenu = null, antMenu = null, mshortcut;
	private JMenuItem synLook = null, antLook = null;
	private boolean spell;
	private ActionList blockActs;
	private JButton btTb2, btSource, btWys, btShowHideTB, btEndnoteAdd;
	public JButton btEndnoteShow;
	private WysiwygEditorKit editorKit;
	private String baseDir = "";

	public WysiwygEditor(ShefEditor editor, String param) {
		super(editor);
		this.editor = editor;
		if (!param.isEmpty()) {
			String p[] = param.split(",");
			for (String pp : p) {
				if (pp.startsWith("len ")) {
					this.maxLen = Integer.parseInt(pp.substring(pp.indexOf(" ") + 1));
				} else {
					switch (pp.trim().toLowerCase()) {
						case "reduced":
							reduced = ShefEditor.REDUCED;
							break;
						case "full":
							reduced = ShefEditor.FULL;
							break;
						case "none":
							reduced = ShefEditor.NONE;
							break;
						case "lang_en":
							dlgLang = ShefEditor.DLG_EN;
							break;
						case "lang_fr":
							dlgLang = ShefEditor.DLG_FR;
							break;
						case "lang_all":
							dlgLang = ShefEditor.DLG_ALL;
							break;
						case "lang_none":
							dlgLang = ShefEditor.DLG_NONE;
							break;
						default:
							break;
					}
				}
			}
		}
		initUI();
	}

	/**
	 * set the caret position
	 *
	 * @param pos
	 */
	@Override
	public void setCaretPosition(int pos) {
		//LOG.trace(TT + ".setCaretPosition(pos=\"" + pos + "\")");
		if (wysEditor.getCaretPosition() != pos) {
			try {
				wysEditor.setCaretPosition(pos);
			} catch (Exception ex) {
				wysEditor.setCaretPosition(wysEditor.getDocument().getLength());
			}
		}
	}

	/**
	 * set the base URL
	 *
	 * @param dirname
	 */
	public void setBase(String dirname) {
		try {
			File f = new File(dirname);
			URL url = f.toURI().toURL();
			HTMLDocument htmlDocument = (HTMLDocument) wysEditor.getDocument();
			htmlDocument.setBase(url);
			baseDir = url.toString();
		} catch (MalformedURLException ex) {
			// ignore
		} finally {
		}
	}

	public String getBase() {
		return baseDir;
	}

	/**
	 * initialize the user interface
	 */
	private void initUI() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new BorderLayout());
		setName(ShefEditor.WYSIWYG);
		add(initTop(), BorderLayout.NORTH);
		wysEditor = createWysEditor();
		add(new JScrollPane(wysEditor), BorderLayout.CENTER);
		add(initStatusbar(), BorderLayout.SOUTH);
		statusbar.setVisible(editor.isStatus());
	}

	/**
	 * initialize the top part
	 *
	 * @return
	 */
	private JPanel initTop() {
		JPanel top = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1, MIG.FILL, MIG.HIDEMODE3), "[][grow]"));
		btShowHideTB = Ui.initButton("btShowHideTB", "▲",
				ICONS.K.NONE, "shef.showhide", e -> showHideTB());
		btShowHideTB.setText("▲");
		btShowHideTB.setFont(FontUtil.getSmall());
		btShowHideTB.setMaximumSize(new Dimension(IconUtil.getDefSize() / 2, IconUtil.getDefSize() / 2));
		btShowHideTB.setMargin(new Insets(0, 0, 0, 0));
		top.add(btShowHideTB);
		progress = new JProgressBar(0, 32768);
		progress.setBorder(BorderFactory.createEmptyBorder());
		progress.setMaximumSize(new Dimension(32768, 4));
		if (editor.isStatus()) {
			top.add(progress, MIG.get(MIG.SPAN, MIG.GROWX));
		}
		top.add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX));
		btShowHideTB.setVisible(reduced != 0);
		return top;
	}

	/**
	 * initialize the toolbar
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JToolBar initToolbar() {
		//LOG.trace(TT + ".initToolbar()");
		actionList = new ActionList("editor-actions");
		if (toolbar == null) {
			super.initToolbar();
			toolbar.setFocusable(false);
		} else {
			toolbar.removeAll();
		}
		wysPopupMenu = new JPopupMenu();
		wysPopupMenu.add(CompoundManager.ActionUndo);
		wysPopupMenu.add(CompoundManager.ActionRedo);
		if (Dictaphone.installed()) {
			wysPopupMenu.add((new JSeparator()));
			JMenuItem it = new JMenuItem();
			if (!Dictaphone.isStarted()) {
				it.setText(I18N.getMsg("dictaphone.start"));
				it.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_START));
			} else {
				it.setText(I18N.getMsg("dictaphone.stop"));
				it.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_STOP));
			}
			it.addActionListener(e -> Dictaphone.start(it));
			wysPopupMenu.add(it);
		}
		wysPopupMenu.add((new JSeparator()));
		wysPopupMenu.add(new HTMLElementPropertiesAction(this));
		initShortcuts();
		if (reduced == ShefEditor.FULL) {
			btTb2 = new JButton("▲");
			btTb2.setToolTipText(I18N.getMsg("shef.2line.hide"));
			btTb2.addActionListener(e -> tb2Hide());
			toolbar.add(btTb2);
		}
		tb1Init();
		tb2Init();
		toolbar.add(tb1);
		//toolbar.add(tb14, MIG.get(MIG.RIGHT));
		if (reduced != 1) {
			toolbar.add(tb2, MIG.get(MIG.SPAN, MIG.NEWLINE));
		}
		tbSize = toolbar.getSize();
		if (reduced == 0) {
			toolbarHide(tb1);
			toolbarHide(tb2);
			toolbarHide(toolbar);
		}
		return toolbar;
	}

	private JToolBar getDefaultToolbar() {
		JToolBar tbx = new JToolBar();
		tbx.setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.INS0, MIG.GAP0)));
		return tbx;
	}

	/**
	 * initialize tb1 toolbar contains tb11, tb12, tb13 and tb14
	 */
	@SuppressWarnings("unchecked")
	private void tb1Init() {
		//LOG.trace(TT + ".tb1Init()");
		tb1 = getDefaultToolbar();//new JToolBar();
		tb1.setBorder(null);
		tb11Init();
		tb12Init();
		tb13Init();
		tb14Init();
	}

	/**
	 * initialize toolbar 1.1 contains paragraphe style and highlighter
	 */
	@SuppressWarnings("unchecked")
	private void tb11Init() {
		tb11 = getDefaultToolbar();
		// paragraph style
		tb11.add(new JLabel(I18N.getColonMsg("shef.style")));
		blockActs = HTMLBlockAction.createBlockElementActionList(this);
		cbParagraph = new JComboBox(toArray(blockActs));
		cbParagraph.setToolTipText(I18N.getMsg("shef.style_prompt"));
		cbParagraph.setRenderer(new ParagraphComboRenderer());
		// add shortcut
		for (Object act : blockActs) {
			if (act instanceof HTMLBlockAction) {
				shortcut.add(initButton(actionList, (HTMLBlockAction) act));
			}
		}
		cbParagraph.addActionListener(new ParagraphComboHandler());
		tb11.add(cbParagraph, MIG.GROWX);
		// EM tag style for highlighter
		tb11.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.EM)));
		// add the tb11
		tb1.add(tb11);
	}

	/**
	 * initialize toolar 1.2, contains font style, size and color
	 */
	@SuppressWarnings("unchecked")
	private void tb12Init() {
		//replaced by a button
		if (reduced != 1) {
			tb12 = getDefaultToolbar();
			JButton btFont = new JButton(IconUtil.getIconSmall("font"));
			btFont.setName("btFontsize");
			btFont.setToolTipText(I18N.getMsg("shef.font_desc"));
			btFont.addActionListener(e -> popupFont(btFont));
			tb12.add(btFont);
			tb1.add(tb12);
		}
	}

	/**
	 * initialize toolbar 1.3, contains undo, redo, zoomin, zoomout
	 */
	private void tb13Init() {
		tb13 = getDefaultToolbar();
		// undo and redo
		tb13.add(initButton(actionList, CompoundManager.ActionUndo));
		tb13.add(initButton(actionList, CompoundManager.ActionRedo));
		//zoomin and zoomout
		tb13.add(initButton(actionList, new Zoom(this, 1)));
		tb13.add(initButton(actionList, new Zoom(this, 0)));
		tb1.add(tb13);
	}

	/**
	 * initialize toolbar 1.4, contains wysiwyg/html, spellchecker and other tools
	 */
	private void tb14Init() {
		tb14 = getDefaultToolbar();
		// view wysiwyg/html
		btWys = Ui.initButton(ShefEditor.SOURCE, "", ICONS.K.SOURCE,
				"shef." + ShefEditor.SOURCE,
				e -> editor.changeTo(ShefEditor.SOURCE));
		tb14.add(btWys);
		btSource = Ui.initButton(ShefEditor.HTML, "", ICONS.K.HTML,
				"shef." + ShefEditor.HTML,
				e -> editor.changeTo(ShefEditor.HTML));
		tb14.add(btSource);
		// external spellchecker
		if (SpellCheck.checkForCkEditor()) {
			JButton bt = Ui.initButton("langtool", "", ICONS.K.LANGTOOL,
					"shef.spellcheck_external",
					e -> SpellCheck.launchExternal(wysEditor.getText()));
			tb14.add(bt);
		}
		// dictaphone
		if (Dictaphone.installed()) {
			JButton dict = new JButton();
			if (!Dictaphone.isStarted()) {
				dict.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_START));
				dict.setToolTipText(I18N.getMsg("dictaphone.start"));
			} else {
				dict.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_STOP));
				dict.setToolTipText(I18N.getMsg("dictaphone.stop"));
			}
			dict.addActionListener(e -> Dictaphone.start(dict));
			tb14.add(dict);
		}
		tb1.add(tb14);
	}

	/**
	 * get the tb1 toolbar
	 *
	 * @return
	 */
	public JToolBar tb1Get() {
		return tb1;
	}

	/**
	 * init toolbar 2 for character format, lists, alignments, links, image, table, special
	 * characters
	 */
	private void tb2Init() {
		//LOG.trace(TT + ".initTb2()");
		tb2 = new JToolBar();
		tb2.setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.INS0, MIG.GAP0)));
		if (reduced == 1) {
			tb2 = tb1;
		}
		//character styles
		tb2.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.BOLD)));
		tb2.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.ITALIC)));
		tb2.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.UNDERLINE)));
		if (reduced != 1) {
			tb2.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.STRIKE)));
			tb2.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.SUB)));
			tb2.add(initButton(actionList, new HTMLInlineAction(this, HTMLInlineAction.SUP)));
		}
		tb2.addSeparator();
		//lists ol ul
		tb2.add(initButton(actionList, new HTMLBlockAction(this, HTMLBlockAction.OL)));
		tb2.add(initButton(actionList, new HTMLBlockAction(this, HTMLBlockAction.UL)));
		tb2.addSeparator();
		tb2.add(initButton(actionList, new HTMLAlignAction(this, HTMLAlignAction.LEFT)));
		tb2.add(initButton(actionList, new HTMLAlignAction(this, HTMLAlignAction.CENTER)));
		tb2.add(initButton(actionList, new HTMLAlignAction(this, HTMLAlignAction.RIGHT)));
		tb2.add(initButton(actionList, new HTMLAlignAction(this, HTMLAlignAction.JUSTIFY)));
		tb2.addSeparator();
		//links, image, table
		if (reduced != 1) {
			linksBar = new JToolBar();
			linksBar.setFloatable(false);
			linksBar.add(initButton(actionList, new HTMLLinkAction(this)));
			linksBar.add(initButton(actionList, new HTMLImageAction(this)));
			linksBar.add(initButton(actionList, new HTMLTableAction(this)));
			tb2.add(linksBar);
		}
		//special characters (unicode, linebreak, dialog open/close)
		JButton btChar = this.initButton("btChar", "", "cogs", "char_special");
		btChar.addActionListener(e -> popupChar(btChar));
		tb2.add(btChar);
	}

	public void addEndnoteButtons(MainFrame mainFrame, Scene scene) {
		tb2.addSeparator();
		btEndnoteAdd = initButton(actionList, new HTMLEndnoteAddAction(this, mainFrame, scene));
		tb2.add(btEndnoteAdd);
		btEndnoteShow = initButton(actionList, new HTMLEndnoteShowAction(this, mainFrame, scene));
		tb2.add(btEndnoteShow);
	}

	public void addSaveButton(MainFrame mainFrame) {
		tb14.add(initButton(actionList, new FileSaveAction(mainFrame, this)));
	}

	public void addImportButton(MainFrame mainFrame) {
		tb14.add(initButton(actionList, new FileReadAction(mainFrame, this)));
	}

	public void addLinkInternalButton(MainFrame mainFrame) {
		linksBar.add(initButton(actionList, new HTMLLinkInternalAction(mainFrame, this)));
	}

	/**
	 * initialize phantom menu
	 */
	private void initShortcuts() {
		//LOG.trace(TT + "initShortcuts()");
		SwingUtil.setFixedSize(shortcut, new Dimension(1, 1));
		shortcut.add(initButton(actionList, new HTMLLineBreakAction(this)));
		shortcut.add(initButton(actionList, new HTMLCadratinAction(this, UNBREAK)));
		shortcut.add(initButton(actionList, new HTMLCadratinAction(this, CADRATIN)));
		shortcut.add(initButton(actionList, new EditCopyAction(this)));
		shortcut.add(initButton(actionList, new EditPasteAction(this)));
		shortcut.add(initButton(actionList, new EditPasteFormattedAction(this)));
		shortcut.add(initButton(actionList, new FindReplaceAction(this, false)));
		toolbar.add(shortcut);
	}

	public void setHide(boolean hide) {
		//LOG.trace(TT + "setHide()");
		tb1.setVisible(hide);
		tb2.setVisible(hide);
	}

	public void setStatus(boolean b) {
		statusbar.setVisible(b);
	}

	public boolean getHide() {
		return tb1.isVisible() && tb2.isVisible();
	}

	public JToolBar getLinksBar() {
		return linksBar;
	}

	/**
	 * get the tb2 toolbar
	 *
	 * @return
	 */
	public JToolBar tb2Get() {
		return tb2;
	}

	private boolean tbShow = true;

	/**
	 * hide the toolbar
	 */
	public void hideToolbar() {
		tbShow = !tbShow;
		if (tbShow) {
			toolbar.setPreferredSize(tbSize);
		} else {
			toolbarHide(toolbar);
		}
	}

	/**
	 * hide the given toolbar
	 *
	 * @param tb
	 */
	public void toolbarHide(JToolBar tb) {
		//LOG.trace(TT + ".hideToolbar(tb)");
		Dimension d = new Dimension(0, 0);
		tb.setMaximumSize(d);
		tb.setPreferredSize(d);
	}

	/**
	 * hide the tb2 toolbar
	 */
	private void tb2Hide() {
		tb2Show = !tb2Show;
		if (tb2Show) {
			tb2.setPreferredSize(tb1.getSize());
			tb2.setMaximumSize(tb1.getSize());
			btTb2.setToolTipText(I18N.getMsg("shef.2line.hide"));
		} else {
			tb2.setPreferredSize(new Dimension(0, 0));
			tb2.setMaximumSize(new Dimension(0, 0));
			btTb2.setToolTipText(I18N.getMsg("shef.2line.show"));
		}
		btTb2.setText((tb2Show ? "▲" : "▼"));
	}

	/**
	 * init status bar
	 *
	 * @return
	 */
	private JPanel initStatusbar() {
		//LOG.trace(TT + ".initStatusbar()");
		statusbar = new JPanel(new BorderLayout());
		lbStatus = new JLabel();
		lbStatus.setFont(FontUtil.getSmall());
		statusbar.add(lbStatus, BorderLayout.LINE_END);
		return statusbar;
	}

	/**
	 * refresh status info
	 */
	public void refreshStatus() {
		//LOG.trace(TT + ".refreshStatus()");
		String text = getText();
		Integer txtLen = text.length();
		if (maxLen > 0) {
			Integer pourcent = (int) (maxLen * 0.1);
			int len = maxLen - txtLen;
			if (len < 0) {
				lbStatus.setForeground(Color.red);
				progress.setForeground(Color.red);
			} else if (len < pourcent) {
				lbStatus.setForeground(Color.orange);
				progress.setForeground(Color.orange);
			} else {
				lbStatus.setForeground(Color.black);
				progress.setForeground(Color.green);
			}
			progress.setValue(maxLen - len);
		}
		lbStatus.setText(I18N.getColonMsg("editor.words") + TextUtil.countWords(text) + " (" + txtLen + ")");
	}

	/**
	 * Converts an action list to an array. Any of the null "separators" or sub ActionLists are
	 * ommited from the array.
	 *
	 * @param lst
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Action[] toArray(ActionList lst) {
		List acts = new ArrayList();
		for (Iterator it = lst.iterator(); it.hasNext();) {
			Object v = it.next();
			if (v != null && v instanceof Action) {
				acts.add(v);
			}
		}
		return (Action[]) acts.toArray(new Action[acts.size()]);
	}

	/**
	 * create de Wysiwyg editor pane
	 *
	 * @return
	 */
	private JEditorPane createWysEditor() {
		//LOG.trace(TT + ".createWysEditor()");
		Font font = App.getInstance().fonts.editorGet();
		JEditorPane ed = new JEditorPane();
		ed.setName("wysEditor");
		ed.setFont(font);
		editorKit = new WysiwygEditorKit(this);
		ed.setEditorKitForContentType("text/html", editorKit);
		ed.setContentType("text/html");
		insertHTML(ed, "<p></p>", 0);
		ed.addCaretListener(caretHandler);
		ed.addFocusListener(focusHandler);
		// spell checker, must be registered before the popup handler
		if (!Spelling.none.name().equals(ShefEditor.getSpelling())) {
			SpellChecker.register(ed);
		}
		ed.addMouseListener(popupHandler);
		HTMLDocument document = (HTMLDocument) ed.getDocument();
		CompoundManager cuh = new CompoundManager(document, new UndoManager());
		document.addUndoableEditListener(cuh);
		document.getStyleSheet().addRule(CSS.forEditor());
		ed.addHyperlinkListener(this);
		if (!Spelling.none.name().equals(ShefEditor.getSpelling())) {
			wysPopupMenu.addSeparator();
			wysPopupMenu.add(SpellChecker.createCheckerMenu());
			wysPopupMenu.add(SpellChecker.createLanguagesMenu());
		}
		document.addDocumentListener(textChanged);
		return ed;
	}

	/**
	 * inserts html into the wysiwyg editor
	 *
	 * @param editor
	 * @param html
	 * @param location
	 */
	private void insertHTML(JEditorPane editor, String html, int location) {
		try {
			HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
			Document doc = editor.getDocument();
			StringReader reader = new StringReader(HtmlUtils.jEditorPaneizeHTML(html));
			kit.read(reader, doc, location);
		} catch (IOException | BadLocationException ex) {
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * set text to the Wysiwyg pane
	 *
	 * @param text
	 */
	@Override
	public void setText(String text) {
		//LOG.trace(TT + ".setText(text=\"" + text + "\")");
		String rtext = removeInvalidTags(text);
		wysEditor.setText("");
		insertHTML(wysEditor, rtext, 0);
		refreshStatus();
		CompoundManager.discardAllEdits(wysEditor.getDocument());
	}

	public void changeWord(String word) {
		//LOG.trace(TT + ".setSynonym(word=" + word + ")");
		wysEditor.replaceSelection(word);
		setChange();
	}

	public void lookForSynonyms(MouseEvent evt, Word word) {
		//LOG.trace(TT + ".synonymsLook()");
		SwingUtil.setWaitingCursor(this);
		String target = wysEditor.getSelectedText();
		if (word != null && !word.getMot().isEmpty() && !word.getLemme().isEmpty()) {
			target = word.getLemme();
		}
		List<String> words = Synonyms.lookForSynonyms(wysEditor, target);
		SwingUtil.setDefaultCursor(this);
		if (!words.isEmpty()) {
			JPopupMenu pop = new JPopupMenu();
			JSMenuScroller.setScrollerFor(pop, 15, 200, 0, 0);
			for (String s : words) {
				JMenuItem it = new JMenuItem(s);
				it.addActionListener(e -> changeWord(s));
				pop.add(it);
			}
			JMenuItem it = new JMenuItem(I18N.getMsg("word.synonyms.url_show") + " >");
			it.setToolTipText(Synonyms.SYNONYMS_URL_PROMPT);
			final String sword = target;
			it.addActionListener(e -> Synonyms.showForSynonyms(wysEditor, sword));
			pop.add(it);
			pop.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	public void lookForAntonyms(MouseEvent evt, Word word) {
		//LOG.trace(TT + ".synonymsLook()");
		SwingUtil.setWaitingCursor(this);
		String target = wysEditor.getSelectedText();
		if (word != null && !word.getMot().isEmpty() && !word.getLemme().isEmpty()) {
			target = word.getLemme();
		}
		List<String> words = Synonyms.lookForAntonyms(wysEditor, target);
		SwingUtil.setDefaultCursor(this);
		if (!words.isEmpty()) {
			JPopupMenu pop = new JPopupMenu();
			JSMenuScroller.setScrollerFor(pop, 15, 200, 0, 0);
			for (String s : words) {
				JMenuItem it = new JMenuItem(s);
				it.addActionListener(e -> changeWord(s));
				pop.add(it);
			}
			JMenuItem it = new JMenuItem(I18N.getMsg("word.antonyms.url_show") + " >");
			it.setToolTipText(Synonyms.ANTONYMS_URL_PROMPT);
			final String sword = target;
			it.addActionListener(e -> Synonyms.showForAntonyms(wysEditor, sword));
			pop.add(it);
			pop.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	/**
	 * get the text content of the Wyiwyg pane
	 *
	 * @return
	 */
	public String getText() {
		String html = Html.removeTag(removeInvalidTags(wysEditor.getText()), "style");
		return html;
	}

	/**
	 * check if content has changed
	 *
	 * @return : true if content has changed
	 */
	public boolean isTextChanged() {
		return this.isTextChanged;
	}

	/**
	 * removing invalid tags
	 *
	 * @param html
	 * @return
	 */
	private String removeInvalidTags(String html) {
		for (String tag : INVALID_TAGS) {
			html = deleteOccurance(html, "<" + tag + ">");
			html = deleteOccurance(html, "</" + tag + ">");
		}
		return html.trim();
	}

	/**
	 * delete a word from a String
	 *
	 * @param text
	 * @param word
	 * @return
	 */
	private String deleteOccurance(String text, String word) {
		StringBuilder sb = new StringBuilder(text);
		int p;
		while ((p = sb.toString().toLowerCase().indexOf(word.toLowerCase())) != -1) {
			sb.delete(p, p + word.length());
		}
		return sb.toString();
	}

	/**
	 * update the state
	 */
	private void updateState() {
		//LOG.trace(TT + ".updateState()");
		actionList.putContextValueForAll(HTMLTextEditAction.EDITOR, wysEditor);
		actionList.updateEnabledForAll();
	}

	/**
	 * get the Wysiwyg editor pane
	 *
	 * @return
	 */
	public JEditorPane getWysEditor() {
		return this.wysEditor;
	}

	/**
	 * set the editor to be editable or not
	 *
	 * @param b
	 */
	public void setEditable(boolean b) {
		wysEditor.setEditable(b);
		btSource.setIcon(IconUtil.getIconSmall(b ? "html" : "pencil"));
		btSource.setToolTipText(I18N.getMsg("shef." + (b ? "html" : "html.normal")));
	}

	/**
	 * call the default navigator with the hyperlink clicked
	 *
	 * @param evt
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED
				&& !evt.getDescription().startsWith("#")) {
			Net.openUrl(evt);
		}
	}

	/**
	 * get the actionList
	 *
	 * @return
	 */
	public ActionList getActionList() {
		return actionList;
	}

	/**
	 * set the maxLen
	 *
	 * @param maxLen
	 */
	public void setMaxLen(int maxLen) {
		//LOG.trace(TT + ".setMaxLen(maxLen=\"" + maxLen + "\")");
		this.maxLen = maxLen;
		refreshStatus();
	}

	/**
	 * set the change indicator
	 */
	public void setChange() {
		this.isTextChanged = true;
	}

	/**
	 * remove specific tag String from the HTML code
	 *
	 * @param tag
	 */
	public void removeTag(String tag) {
		//LOG.trace(TT + ".removeTag(tag=\"" + tag + "\")");
		String html = wysEditor.getText();
		if (html.contains(tag)) {
			html = html.replace(tag, "");
			int idx = wysEditor.getCaretPosition();
			wysEditor.setText(html);
			if (idx < 0 || idx > wysEditor.getText().length()) {
				idx = 0;
			}
			wysEditor.setCaretPosition(idx);
			setChange();
		}
	}

	/**
	 * add synonyms list to the wysPopupMenu
	 *
	 * @param evt
	 * @param word
	 */
	private void addSynonyms(MouseEvent evt, Word word) {
		if (synMenu != null) {
			wysPopupMenu.remove(synMenu);
		}
		synMenu = null;
		if (word != null) {
			List<String> synonyms = word.getSynonyms();
			if (!synonyms.isEmpty()) {
				synMenu = new JMenu(I18N.getMsg("word.synonyms"));
				if (!word.getLemme().isEmpty()) {
					JLabel lb = new JLabel("= " + word.getLemme());
					lb.setIcon(IconUtil.getIconSmall(ICONS.K.AR_RIGHT));
					lb.setFont(FontUtil.getBold());
					lb.setForeground(Color.blue);
					synMenu.add(lb);
				}
				int n = 10;
				for (String s : synonyms) {
					JMenuItem it = new JMenuItem(s);
					it.addActionListener(e -> changeWord(s));
					synMenu.add(it);
					if (n-- < 0) {
						break;
					}
				}
				wysPopupMenu.add(synMenu);
			}
		}
		addSynonymsLook(evt, word);
	}

	/**
	 * add a synonym look
	 *
	 * @param evt
	 * @param word
	 */
	private void addSynonymsLook(MouseEvent evt, Word word) {
		if (synLook != null) {
			wysPopupMenu.remove(synLook);
		}
		synLook = null;
		if (!App.preferences.getWorkOffline()) {
			String url = Synonyms.SYNONYMS_URL;
			if (!url.isEmpty() && !url.startsWith("!")) {
				synLook = new JMenuItem(I18N.getMsg("word.synonyms.look"));
				synLook.setToolTipText(Synonyms.SYNONYMS_URL_PROMPT);
				synLook.addActionListener(e -> lookForSynonyms(evt, word));
				if (synMenu != null) {
					synMenu.add(synLook);
				} else {
					wysPopupMenu.add(synLook);
				}
			}
		}
	}

	/**
	 * add antonyms list to the wysPopupMenu
	 *
	 * @param evt
	 * @param word
	 */
	private void addAntonyms(MouseEvent evt, Word word) {
		if (antMenu != null) {
			wysPopupMenu.remove(antMenu);
		}
		antMenu = null;
		if (word != null) {
			List<String> words = word.getAntonyms();
			if (!words.isEmpty()) {
				antMenu = new JMenu(I18N.getMsg("word.antonyms"));
				if (!word.getLemme().isEmpty()) {
					JLabel lb = new JLabel("# " + word.getLemme());
					lb.setIcon(IconUtil.getIconSmall(ICONS.K.AR_RIGHT));
					lb.setFont(FontUtil.getBold());
					lb.setForeground(Color.blue);
					antMenu.add(lb);
				}
				int n = 10;
				for (String s : words) {
					JMenuItem it = new JMenuItem(s);
					it.addActionListener(e -> changeWord(s));
					antMenu.add(it);
					if (n-- < 0) {
						break;
					}
				}
				wysPopupMenu.add(antMenu);
			} else {
				antMenu = null;
			}
		}
		addAntonymsLook(evt, word);
	}

	/**
	 * add an antonyme look for
	 *
	 * @param evt
	 * @param word
	 */
	private void addAntonymsLook(MouseEvent evt, Word word) {
		if (antLook != null) {
			wysPopupMenu.remove(antLook);
		}
		antLook = null;
		if (!App.preferences.getWorkOffline()) {
			String url = Synonyms.ANTONYMS_URL;
			if (!url.isEmpty() && !url.startsWith("!")) {
				antLook = new JMenuItem(I18N.getMsg("word.antonyms.look"));
				antLook.setToolTipText(Synonyms.ANTONYMS_URL_PROMPT);
				antLook.addActionListener(e -> lookForAntonyms(evt, word));
				if (antMenu != null) {
					antMenu.add(antLook);
				} else {
					wysPopupMenu.add(antLook);

				}
			}
		}
	}

	/**
	 * popup menu for Character
	 *
	 * @param bt
	 */
	private void popupChar(JButton bt) {
		JPopupMenu pop = new JPopupMenu();
		pop.add(initMenuItem(new HTMLLineBreakAction(this)));
		pop.add(initMenuItem(new HTMLCadratinAction(this, UNBREAK)));
		pop.add(initMenuItem(new HTMLUnicodeAction(this)));
		pop.add(initMenuItem(new HTMLCadratinAction(this, CADRATIN)));
		if (dlgLang == ShefEditor.DLG_ALL || dlgLang == ShefEditor.DLG_EN) {
			pop.add(initMenuItem(new HTMLCadratinAction(this, DLG_OPEN_EN)));
			pop.add(initMenuItem(new HTMLCadratinAction(this, DLG_CLOSE_EN)));
		}
		if (dlgLang == ShefEditor.DLG_ALL || dlgLang == ShefEditor.DLG_FR) {
			pop.add(initMenuItem(new HTMLCadratinAction(this, DLG_OPEN_FR)));
			pop.add(initMenuItem(new HTMLCadratinAction(this, DLG_CLOSE_FR)));
		}
		pop.show(bt, bt.getSize().height, 0);
	}

	/**
	 * popup menu for Font family, size and color
	 */
	private void popupFont(JButton bt) {
		JPopupMenu pop = new JPopupMenu();
		// font family
		JMenu mn = new JMenu(I18N.getMsg("shef.font"));
		List<String> fonts = new ArrayList<>();
		fonts.add(I18N.getMsg("shef.default"));
		fonts.add("Serif");
		fonts.add("Sans-Serif");
		fonts.add("Monospaced");
		fonts.add("---");
		GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fonts.addAll(Arrays.asList(gEnv.getAvailableFontFamilyNames()));
		for (String str : fonts) {
			if (str.equals("---")) {
				mn.add(new JSeparator());
			} else {
				JMenuItem it = new JMenuItem(str);
				Action fact = new HTMLFontFamilyAction(this, str);
				it.addActionListener(fact);
				mn.add(it);
			}
		}
		JSMenuScroller.setScrollerFor(mn, 15, 200, 0, 0);
		pop.add(mn);
		// font size
		mn = new JMenu(I18N.getMsg("shef.fontsize_desc"));
		for (int i = 0; i < HTMLFontSizeAction.FONT_SIZES.length; i++) {
			mn.add(initMenuItem(new HTMLFontSizeAction(this, i)));
		}
		pop.add(mn);
		//font color
		Action act = new HTMLFontColorAction(this);
		actionList.add(act);
		JMenuItem it = new JMenuItem(act);
		pop.add(it);

		pop.show(bt, bt.getSize().height, 0);
	}

	/**
	 * show or hide the toolbar
	 */
	public void showHideTB() {
		if (btShowHideTB.getText().equals("▲")) {
			btShowHideTB.setText("▼");
			toolbar.setMaximumSize(new Dimension(1, 1));
		} else {
			btShowHideTB.setText("▲");
			toolbar.setMaximumSize(new Dimension(SwingUtil.getScreenSize()));
		}
	}

	/**
	 * show or hide the toolbar
	 */
	public void setShowHideTB(boolean b) {
		if (b) {
			btShowHideTB.setText("▼");
			toolbar.setMaximumSize(new Dimension(1, 1));
		} else {
			btShowHideTB.setText("▲");
			toolbar.setMaximumSize(new Dimension(SwingUtil.getScreenSize()));
		}
	}

	/**
	 * get the showing state of the toolbar
	 *
	 * @return
	 */
	public boolean getShowHideTB() {
		return !btShowHideTB.getText().equals("▲");
	}

	/**
	 * handle listener for the caret
	 */
	private class CaretHandler implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
			//LOG.trace(TT + "::" + "CaretHandler" + ".caretUpdate(e=\"" + e.toString() + "\")");
			updateState();
		}
	}

	private class PopupHandler extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			checkForPopupTrigger(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			checkForPopupTrigger(e);
		}

		private void checkForPopupTrigger(MouseEvent evt) {
			if (evt.isPopupTrigger()) {
				if (evt.getSource() == wysEditor) {
					String selectedText = wysEditor.getSelectedText();
					if (selectedText != null && !selectedText.isEmpty()) {
						//LOG.trace("selectedText=\"" + selectedText + "\"");
						Word word = Synonyms.findWord(selectedText);
						if (word != null && !word.getMot().equals(selectedText)) {
							word.setLemme(word.getMot());
							word.setMot(selectedText);
						}
						addSynonyms(evt, word);
						addAntonyms(evt, word);
					}
				} else {
					return;
				}
				wysPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
	}

	private class FocusHandler implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			//LOG.trace(TT + "FocusHandler(e=" + e.toString() + ")");
			if (e.getComponent() instanceof JEditorPane) {
				JEditorPane ed = (JEditorPane) e.getComponent();
				CompoundManager.updateUndo(ed.getDocument());
				updateState();
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (e.getComponent() instanceof JEditorPane) {
			}
		}
	}

	private class TextChangedHandler implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			textChanged();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			textChanged();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			textChanged();
		}

		private void textChanged() {
			//LOG.trace(TT + "::TextChangedHandler.textChanged()");
			isTextChanged = true;
			refreshStatus();
		}
	}

	private class ParagraphComboHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cbParagraph) {
				Action a = (Action) (cbParagraph.getSelectedItem());
				a.actionPerformed(e);
			}
		}
	}

	private class ParagraphComboRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (value instanceof Action) {
				value = ((Action) value).getValue(Action.NAME);
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

}
