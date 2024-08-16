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
package storybook.ui;

import assistant.Assistant;
import assistant.AssistantWizardDlg;
import assistant.app.AppAssistant;
import i18n.I18N;
import i18n.I18NDlg;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import resources.icons.ICONS;
import resources.icons.ICONS.K;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.challenge.ChallengeDlg;
import storybook.db.chapter.Chapter;
import storybook.db.chapter.ChaptersCreateDlg;
import storybook.db.chapter.ChaptersOrderDlg;
import storybook.db.endnote.Endnote;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.FoiDlg;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.part.PartsCreateDlg;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneRenumber;
import storybook.db.scene.Scenes;
import storybook.db.scene.ScenesCreateDlg;
import storybook.db.scene.ScenesLinks;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.dialog.AboutDlg;
import storybook.dialog.EntityCopyDlg;
import storybook.dialog.ExceptionDlg;
import storybook.dialog.RenameDlg;
import storybook.dialog.ReplaceDlg;
import storybook.dialog.SearchDlg;
import storybook.dialog.ToolBarDlg;
import storybook.dialog.preferences.PreferencesDlg;
import storybook.exim.exporter.ExportBookDlg;
import storybook.exim.exporter.ExportBookToHtml;
import storybook.exim.exporter.ExportDlg;
import storybook.exim.exporter.ExportStoryboard;
import storybook.ideabox.IdeaxFrm;
import storybook.project.Project;
import storybook.project.PropEpubCover;
import storybook.project.PropertiesDlg;
import storybook.shortcut.Shortcuts;
import storybook.shortcut.ShortcutsDlg;
import storybook.tools.DateUtil;
import storybook.tools.DockUtil;
import storybook.tools.LOG;
import storybook.tools.clip.Clip;
import storybook.tools.file.EnvUtil;
import storybook.tools.net.Net;
import storybook.tools.net.Updater;
import storybook.tools.spell.SpellDlg;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.splash.WaitingSplash;
import storybook.ui.SbView.VIEWNAME;

/**
 *
 * @author favdb
 */
public class MainMenu implements MouseListener {

	private static final String TT = "MainMenu";

	private final MainFrame mainFrame;
	public boolean testFunction = false;

	public JToolBar toolBar;
	public JButton btFileNew, btFileOpen, btFileSave,
		btIdea, btMemo,
		btNewChapter, btNewGender, btNewItem, btNewLocation,
		btNewPart, btNewPerson, btNewRelation, btNewScene, btNewStrand,
		btNewTag,
		btTabChapter, btTabGender, btTabIdea, btTabItem,// btTabItemlink,
		btTabLocation, btTabMemo, btTabPart, btTabPerson, btTabRelation,
		btTabScene, btTabStrand, btTabTag,// btTabTaglink,
		btViewBook, btViewChrono, btViewManage, btViewMemoria,
		btViewReading, btViewStoryboard, btViewTypist;

	public JMenuBar menuBar;

	private JMenu menuFile;
	private JMenuItem fileNew, fileOpen,
		fileProperties, fileRename, fileClose, fileExit,
		fileSave, fileSaveAs, fileAssistant;
	private JMenu menuFileExport;
	private JMenuItem fileExportBOOK,
		fileExportOther,
		fileExportXml,
		//fileExportOptions,
		fileExportStoryboard;
	//private JMenu menuFileImport;
	private JMenuItem fileImportXml;

	private JMenu menuFileBackRest;
	private JMenuItem fileBackupNew, fileBackupRest;

	private JMenu menuFileRecent;

	//menu for new entity
	private JMenu menuNewEntity;
	private JMenuItem newAttribute,
		newCategory, newChapter, newChapters,
		newEvent,
		newFOI,
		newGender,
		newIdea, newItem,// newItemLink,
		newLocation,
		newMemo,
		newPart, newPerson, newPlot,
		newRelation,
		newScene, newStrand,
		newTag/*, newTagLink*/;
	private JMenu menuPrimaryObjects;
	private JMenu menuSecondaryObjects;

	//menu for tables
	private JMenu menuTables;
	private JMenuItem tabAttribute,
		tabCategory, tabChapter,
		tabEndnote, tabEvent,
		tabGender,
		tabIdea, tabInternal, tabItem,// tabItemLink,
		tabLocation,
		tabMemo,
		tabPart, tabPerson, tabPlot,
		tabRelation,
		tabScene, tabStrand,
		tabTag/*, tabTagLink*/;

	//menu for charts
	private JMenu menuCharts;
	private JMenuItem chartItems, chartLocations,
		chartPersons, chartPersonsByDate, chartPersonsByScene,
		chartStrands, chartWIWW, chartAttributes;

	//menu for help
	private JMenu menuHelp;
	private JMenuItem helpAbout, helpCheckUpdates, helpDoc, helpFaq, helpHome, helpReportBug,
		helpTranslate, toolsChaptersOrder;
	private JMenuItem devTest;

	//menu for edit
	private JMenu menuEdit;
	private JMenuItem editPaste;
	private JMenuItem editCopyBlurb, editCopyBook, editPreferences, editSpellChecker, editCopyEntity;

	//menu for tools
	private JMenu menuTools;
	private JMenuItem toolsPlan,
		toolsReplace,
		toolsScenesOrder,
		toolsPersonsLinks,
		toolsLocationsLinks,
		toolsItemsLinks,
		toolsSearch,
		toolsEpisodes,
		toolsNewScenes,
		toolsNewParts,
		toolsRenumberEndnotes,
		toolsEpubCover;
	private JMenu toolsRename;
	private JMenuItem renameCity,
		renameCountry,
		renameItemCategory,
		renameTagCategory;

	//menu for views
	private JMenu menuView;
	private JMenuItem vueBook,
		vueChrono,
		vueInfo,
		vueMemos,
		vueManageScene;
	private JMenuItem vueMemoria,
		/* vueNavigation,*/
		vueReading,
		vueStoryboard,
		vueTree,
		vueTypist,
		vueTimeline,
		vueStorymap;

	//menu for window
	private JMenu menuWindow;
	public JMenu windowPredefinedLayout;
	private JMenuItem windowDefaultLayout;
	public JMenu windowLoadLayout;
	private JMenuItem windowRefresh,
		windowResetLayout,
		windowSaveLayout;
	public JCheckBoxMenuItem allParts;

	//separators
	private JPopupMenu.Separator separator0,
		separator1,
		separator2,
		separator3,
		separator4;
	private JButton btViewTimeline;
	private JMenuItem editAssistant;
	private JMenuItem setOut;
	private JMenu menuFileCreate;
	private JMenuItem resetEditor;
	private JMenuItem toolsTypist;
	private JMenuItem fileImportSql;
	private JMenuItem fileExportProject;
	private JMenuItem toolsChallenge;
	private JMenuItem toolsClassic;
	private JMenuItem toolsFreytag;
	private JMenuItem toolsVogler;

	public MainMenu(MainFrame m) {
		//LOG.printInfos("MainMenu(mainFrame)");
		mainFrame = m;
		init();
		initUi();
	}

	public void refresh() {
		menuBar.removeAll();
		toolBar.removeAll();
		initUi();
	}

	private void init() {
		//LOG.printInfos(TT+"init()");
		UIManager.put("Menu.font", App.fonts.defGet());
		if (toolBar == null) {
			toolBar = new JToolBar();
		}
		toolBar.setFloatable(false);
		toolBar.setName("MainToolbar");

		//create MenuBar
		menuBar = new JMenuBar();
		menuBar.setFont(App.fonts.defGet());
		menuBar.setName("menuBar");
	}

	private void initUi() {
		initToolbar();
		menuFileInit();
		menuEditInit();
		menuNewInit();
		menuTableInit();
		menuToolsInit();
		menuViewInit();
		menuChartsInit();
		menuWindowInit();
		menuHelpInit();
		setToolbar();
		enableSave(false);
	}

	public void setToolbar() {
		toolBar.removeMouseListener(this);
		JButton[] btn = {
			btFileNew, btFileOpen, btFileSave, //3
			btNewStrand, btNewPart, btNewChapter, btNewScene,//4
			btNewPerson, btNewGender, btNewRelation,//3
			btNewLocation,//1
			btNewItem,//1
			btNewTag,//1
			btMemo, btIdea, //2
			btTabStrand, btTabPart, btTabChapter, btTabScene,//4
			btTabPerson, btTabGender, btTabRelation, //3
			btTabLocation, //1
			btTabItem,/* btTabItemlink,*///2
			btTabTag,/* btTabTaglink,*/ //2
			btTabMemo, btTabIdea,//2
			btViewChrono, btViewTimeline, btViewBook, btViewManage,
			btViewReading, btViewMemoria, btViewStoryboard, btViewTypist //6
		}; // total 37
		//load parameters as a single String
		String param = App.preferences.getString(Pref.KEY.TOOLBAR);
		while (param.length() < btn.length) {
			param += "1";// default allParts button are visible
		}
		if (param.length() > btn.length) {
			param = param.substring(0, btn.length);
		}
		for (int i = 0; i < param.length(); i++) {
			btn[i].setVisible((param.charAt(i) == '1'));
		}
		toolBar.setFloatable(true);
		toolBar.addMouseListener(this);
		if (mainFrame.isBlank()) {
			hideToolbar();
		}
	}

	public void setTimelineMenu() {
		List<Scene> s = Scenes.find(mainFrame);
		if (!s.isEmpty()) {
			vueTimeline.setEnabled(s.get(0).hasScenets());
		}
	}

	/**
	 * initialize a JButton
	 *
	 * @param icon
	 * @param tips
	 * @param action
	 * @return
	 */
	private JButton initButton(K icon, String tips, ActionListener... action) {
		JButton bt = SwingUtil.createButton("", icon.toString(), tips, true, action);
		bt.addMouseListener(this);
		return bt;
	}

	/**
	 * initialize the toolbar
	 */
	private void initToolbar() {
		tbFileInit();
		toolBar.add(new JToolBar.Separator());
		tbNewInit();
		toolBar.add(new JToolBar.Separator());
		tbTabInit();
		toolBar.add(new JToolBar.Separator());
		tbViewInit();
		toolBar.add(new JToolBar.Separator());
		tbMemoInit();
	}

	/**
	 * initialize the file toolbar
	 */
	private void tbFileInit() {
		//LOG.printInfos(TT+"initTbFile()");
		btFileNew = initButton(K.F_NEW, "file.new",
			evt -> App.getInstance().createNewProject());
		toolBar.add(btFileNew);

		btFileOpen = initButton(K.F_OPEN, "file.open",
			evt -> {
				mainFrame.cursorSetWaiting();
				//App.getInstance().openProject();
				App.getInstance().selectProject();
				mainFrame.cursorSetDefault();
			});
		toolBar.add(btFileOpen);

		btFileSave = initButton(K.F_SAVE, "file.save",
			evt -> mainFrame.fileSave(true));
		toolBar.add(btFileSave);

	}

	/**
	 * initialize the news entity toolbar
	 */
	private void tbNewInit() {
		//LOG.printInfos(TT+"initTbNew()");
		btNewStrand = initButton(K.NEW_STRAND, "strand.new", evt -> mainFrame.newEntity(new Strand()));
		toolBar.add(btNewStrand);

		btNewPart = initButton(K.NEW_PART, "part.new", evt -> mainFrame.newEntity(new Part()));
		toolBar.add(btNewPart);

		btNewChapter = initButton(K.NEW_CHAPTER, "chapter.new", evt -> mainFrame.newEntity(new Chapter()));
		toolBar.add(btNewChapter);

		btNewScene = initButton(K.NEW_SCENE, "scene.new", evt -> mainFrame.newEntity(new Scene()));
		toolBar.add(btNewScene);

		btNewPerson = initButton(K.NEW_PERSON, "person.new", evt -> mainFrame.newEntity(new Person()));
		toolBar.add(btNewPerson);

		btNewGender = initButton(K.NEW_GENDER, "gender.new", evt -> mainFrame.newEntity(new Gender()));
		toolBar.add(btNewGender);

		btNewRelation = initButton(K.ENT_RELATION, "relation.new", evt -> mainFrame.newEntity(new Relation()));
		toolBar.add(btNewRelation);

		btNewLocation = initButton(K.NEW_LOCATION, "location.new", evt -> mainFrame.newEntity(new Location()));
		toolBar.add(btNewLocation);

		btNewItem = initButton(K.NEW_ITEM, "item.new", evt -> mainFrame.newEntity(new Item()));
		toolBar.add(btNewItem);

		btNewTag = initButton(K.NEW_TAG, "tag.new", evt -> mainFrame.newEntity(new Tag()));
		toolBar.add(btNewTag);

	}

	/**
	 * initialize the tables toolbar
	 */
	private void tbTabInit() {
		//LOG.printInfos(TT+"initTbTab()");
		btTabStrand = initButton(K.TABLE_STRANDS, "strands", evt -> mainFrame.showAndFocus(VIEWNAME.STRANDS));
		toolBar.add(btTabStrand);

		btTabPart = initButton(K.TABLE_PARTS, "parts", evt -> mainFrame.showAndFocus(VIEWNAME.PARTS));
		toolBar.add(btTabPart);

		btTabChapter = initButton(K.TABLE_CHAPTERS, "chapters", evt -> mainFrame.showAndFocus(VIEWNAME.CHAPTERS));
		toolBar.add(btTabChapter);

		btTabScene = initButton(K.TABLE_SCENES, "scenes", evt -> mainFrame.showAndFocus(VIEWNAME.SCENES));
		toolBar.add(btTabScene);

		btTabPerson = initButton(K.TABLE_PERSONS, "persons", evt -> mainFrame.showAndFocus(VIEWNAME.PERSONS));
		toolBar.add(btTabPerson);

		btTabGender = initButton(K.TABLE_GENDERS, "genders", evt -> mainFrame.showAndFocus(VIEWNAME.GENDERS));
		toolBar.add(btTabGender);

		btTabRelation = initButton(K.TABLE_RELATIONS, "relations", evt -> mainFrame.showAndFocus(VIEWNAME.RELATIONS));
		toolBar.add(btTabRelation);

		btTabLocation = initButton(K.TABLE_LOCATIONS, "locations", evt -> mainFrame.showAndFocus(VIEWNAME.LOCATIONS));
		toolBar.add(btTabLocation);

		btTabItem = initButton(K.TABLE_ITEMS, "items", evt -> mainFrame.showAndFocus(VIEWNAME.ITEMS));
		toolBar.add(btTabItem);

		/*btTabItemlink = initButton(K.TABLE_ITEMLINKS, "itemlinks", evt -> mainFrame.showAndFocus(VIEWNAME.ITEMLINKS));
		toolBar.add(btTabItemlink);*/
		btTabTag = initButton(K.TABLE_TAGS, "tags", evt -> mainFrame.showAndFocus(VIEWNAME.TAGS));
		toolBar.add(btTabTag);

		/*btTabTaglink = initButton(K.TABLE_TAGLINKS, "taglinks", evt -> mainFrame.showAndFocus(VIEWNAME.TAGLINKS));
		toolBar.add(btTabTaglink);*/
		btTabMemo = initButton(K.TABLE_MEMOS, "memos", evt -> mainFrame.showAndFocus(VIEWNAME.MEMOS));
		toolBar.add(btTabMemo);

		btTabIdea = initButton(K.TABLE_IDEAS, "ideas", evt -> mainFrame.showAndFocus(VIEWNAME.IDEAS));
		toolBar.add(btTabIdea);

	}

	/**
	 * initialize the views toolbar
	 */
	private void tbViewInit() {
		//LOG.printInfos(TT+"initTbView()");
		btViewChrono = initButton(K.VW_CHRONO, "view.chrono", evt -> mainFrame.showAndFocus(VIEWNAME.CHRONO));
		btViewChrono.setEnabled(false);
		toolBar.add(btViewChrono);

		btViewTimeline = initButton(K.VW_TIMELINE, "view.timeline", evt -> mainFrame.showAndFocus(VIEWNAME.TIMELINE));
		toolBar.add(btViewTimeline);

		btViewBook = initButton(K.VW_BOOK, "view.book", evt -> mainFrame.showAndFocus(VIEWNAME.BOOK));
		toolBar.add(btViewBook);

		btViewManage = initButton(K.VW_MANAGE, "view.manage", evt -> mainFrame.showAndFocus(VIEWNAME.MANAGE));
		toolBar.add(btViewManage);

		btViewReading = initButton(K.VW_READING, "view.reading");
		btViewReading.addActionListener(evt -> mainFrame.showAndFocus(VIEWNAME.READING));
		toolBar.add(btViewReading);

		btViewMemoria = initButton(K.VW_MEMORIA, "view.memoria", evt -> mainFrame.showAndFocus(VIEWNAME.MEMORIA));
		toolBar.add(btViewMemoria);
		//btViewMemoria.setEnabled(App.isDev());

		btViewStoryboard = initButton(K.STAMP, "view.storyboard", evt -> mainFrame.showAndFocus(VIEWNAME.STORYBOARD));
		toolBar.add(btViewStoryboard);

		toolBar.addSeparator();

		btViewTypist = initButton(K.VW_TYPIST, "typist", evt -> mainFrame.typistActivate());
		toolBar.add(btViewTypist);

	}

	/**
	 * initalize the memo and idea toolbar
	 */
	private void tbMemoInit() {
		//LOG.printInfos(TT+"initTbMemo()");
		btMemo = initButton(K.ENT_MEMO, "memos");
		btMemo.addActionListener(evt -> mainFrame.showEditorAsDialog(new Memo()));
		toolBar.add(btMemo);

		btIdea = initButton(K.ENT_IDEA, "ideas");
		btIdea.addActionListener(evt -> mainFrame.showEditorAsDialog(new Idea()));
		toolBar.add(btIdea);
	}

	/**
	 * initialize a menu item with a shortcut
	 *
	 * @param shortcut : the value name of the shortcut
	 * @param action
	 *
	 * @return
	 */
	public static JMenuItem initScMenuItem(String shortcut, ActionListener... action) {
		return (initScMenuItem(K.EMPTY, shortcut, "", action));
	}

	/**
	 * initialize a menu item for a shortcut
	 *
	 * @param icon
	 * @param shortcut : the name of the shortcut
	 * @param tips
	 * @param action
	 *
	 * @return
	 */
	public static JMenuItem initScMenuItem(K icon, String shortcut,
		String tips, ActionListener... action) {
		JMenuItem it = new JMenuItem();
		it.setName(shortcut);
		if (icon != K.EMPTY) {
			it.setIcon(IconUtil.getIconSmall(icon));
		}
		it.setFont(App.fonts.defGet());
		String text = I18N.getMsg("shortcut.i." + shortcut);
		if (text.startsWith("!")) {
			text = I18N.getMsg(shortcut);
		}
		it.setText(text);
		if (!Shortcuts.getKeyBinding(shortcut).isEmpty()) {
			it.setAccelerator(Shortcuts.getKeyStroke(shortcut));
		}
		if (tips != null && !tips.isEmpty()) {
			it.setToolTipText(I18N.getMsg(tips));
		}
		if (action != null && action.length > 0) {
			it.addActionListener(action[0]);
		}
		return it;
	}

	/**
	 * initialize a menu item
	 *
	 * @param title
	 * @param action
	 * @return
	 */
	public static JMenuItem initMenuItem(String title, ActionListener... action) {
		return (initMenuItem(K.EMPTY, title, "", ' ', title, action));
	}

	/**
	 * initialize a menu item
	 *
	 * @param title
	 * @param key
	 * @param action
	 * @return
	 */
	public static JMenuItem initMenuItem(String title,
		String key,
		ActionListener... action) {
		JMenuItem m = initMenuItem(K.EMPTY, title, "", ' ', title);
		if (title.startsWith("!")) {
			m.setText(title.substring(1));
		}
		if (key != null && !key.isEmpty()) {
			KeyStroke k = KeyStroke.getKeyStroke(key);
			if (k != null) {
				m.setAccelerator(k);
			}
		}
		if (action != null && action.length > 0) {
			m.addActionListener(action[0]);
		}
		return (m);
	}

	/**
	 * initialize a menu item
	 *
	 * @param icon
	 * @param name: the name
	 * @param key: a shortkey String, may be empty
	 * @param mnemonic: the menmonic char, may be space for no mnemonic
	 * @param text: the text, may be empty
	 * @param action: the action, optional
	 * @return
	 */
	public static JMenuItem initMenuItem(K icon,
		String name,
		String key,
		char mnemonic,
		String text,
		ActionListener... action) {
		if (icon == K.EMPTY) {
			return initMenuItem("", name, key, mnemonic, text, action);
		}
		return initMenuItem(icon.toString(), name, key, mnemonic, text, action);
	}

	public static JMenuItem initMenuItem(K icon,
		String text,
		String name, ActionListener... action) {
		return initMenuItem(icon, name, "", ' ', text, action);
	}

	/**
	 * initialize a menu item
	 *
	 * @param icon
	 * @param key
	 * @param mnemonic
	 * @param text
	 * @param name
	 * @param action
	 * @return
	 */
	public static JMenuItem initMenuItem(String icon,
		String name,
		String key,
		char mnemonic,
		String text,
		ActionListener... action) {
		JMenuItem m = new JMenuItem();
		m.setFont(App.fonts.defGet());
		if (!key.isEmpty()) {
			KeyStroke k = KeyStroke.getKeyStroke(key);
			if (k != null) {
				m.setAccelerator(k);
			}
		}
		if (!icon.isEmpty() && !icon.equals("empty")) {
			m.setIcon(IconUtil.getIconSmall(icon));
		}
		if (mnemonic != ' ') {
			m.setMnemonic(mnemonic);
		}
		if (text.startsWith("!")) {
			m.setText(text.substring(1));
		} else {
			m.setText(I18N.getMsg(text));
		}
		m.setActionCommand(name);
		m.setName(name);
		if (action != null && action.length > 0) {
			m.addActionListener(action[0]);
		}
		return (m);
	}

	public static JMenu initMenu(String key) {
		JMenu m = new JMenu(I18N.getMsg(key));
		m.setMnemonic(I18N.getMnemonic(key));
		return m;
	}

	/**
	 * initialize the file menu
	 */
	private void menuFileInit() {
		//LOG.printInfos(TT+"initMenuFile()");
		menuFile = initMenu("file");

		// create new project
		menuFileCreate = new JMenu(I18N.getMsg("project.create"));
		menuFileCreate.setIcon(IconUtil.getIconSmall(K.PLUS));

		fileNew = initScMenuItem(K.F_NEW, "file.new", "file.new_tip",
			evt -> App.getInstance().createNewProject());
		menuFileCreate.add(fileNew);

		JMenuItem fileNewDoc = initMenuItem(K.F_IMPORT, "file.from_doc", "import-doc",
			evt -> mainFrame.fileImportDoc());
		fileNewDoc.setToolTipText(I18N.getMsg("file.from_doc_tip"));
		menuFileCreate.add(fileNewDoc);

		menuFile.add(menuFileCreate);

		// open a project and open recent project
		//fileOpen = initMenuItem(K.F_OPEN, Ui.CTRL + "O", 'O', "project.open", "open-command",
		fileOpen = initScMenuItem(K.F_OPEN, "project.open", "",
			evt -> {
				mainFrame.cursorSetWaiting();
				//App.getInstance().openProject();
				App.getInstance().selectProject();
				mainFrame.cursorSetDefault();
			});
		menuFile.add(fileOpen);

		menuFileRecent = new JMenu(I18N.getMsg("file.open.recent"));
		menuFileRecent.setActionCommand("recent-menu-command");
		menuFileRecent.setName("recent-menu-command");
		menuFile.add(menuFileRecent);

		// write the project
		fileSave = initScMenuItem(K.F_SAVE, "project.save", "",
			evt -> mainFrame.fileSave(true));
		menuFile.add(fileSave);

		fileSaveAs = initMenuItem(K.F_SAVEAS, "project.save.as", "",
			evt -> mainFrame.fileSaveAs());
		menuFile.add(fileSaveAs);

		// rename the project file
		fileRename = initMenuItem(K.RENAME, "project.rename", "",
			evt -> mainFrame.fileRename());
		menuFile.add(fileRename);

		// close the project
		fileClose = initScMenuItem(K.CLOSE, "project.close", "",
			evt -> mainFrame.close(false));
		menuFile.add(fileClose);

		// backup and restore
		menuFileBackRestInit();

		// properties
		separator1 = new JPopupMenu.Separator();
		menuFile.add(separator1);

		fileProperties = initMenuItem(K.EMPTY, "file.properties", "fileProperties",
			evt -> PropertiesDlg.show(mainFrame));
		menuFile.add(fileProperties);

		// export and import
		separator2 = new JPopupMenu.Separator();
		menuFile.add(separator2);
		menuFileImportInit();
		menuFileExportInit();

		// exit
		separator4 = new JPopupMenu.Separator();
		menuFile.add(separator4);

		fileExit = initScMenuItem(K.EXIT, "file.exit", "",
			evt -> {
				App.getInstance().exit();
			});
		menuFile.add(fileExit);

		menuBar.add(menuFile);
	}

	private void menuFileBackRestInit() {
		// backup/restore
		separator0 = new JPopupMenu.Separator();
		menuFile.add(separator0);

		menuFileBackRest = new JMenu(I18N.getMsg("file.backup"));
		menuFileBackRest.setName("menuFileBackRest");
		fileBackupNew = initMenuItem(K.BK_SAVE, "file.backup_new", "backup_new",
			evt -> mainFrame.backupNew(true));
		menuFileBackRest.add(fileBackupNew);
		fileBackupRest = initMenuItem(K.BK_REST, "file.backup_rest", "backup_rest",
			evt -> mainFrame.backupRest());
		menuFileBackRest.add(fileBackupRest);
		menuFile.add(menuFileBackRest);
	}

	/**
	 * initialize the file import menu
	 */
	private void menuFileImportInit() {
		/*menuFileImport = new JMenu(I18N.getMsg("file.import"));
		if (App.isDev()) {
			menuFileImport.setVisible(false);
		}
		menuFile.add(menuFileImport);

		fileImportSql = initMenuItem("file.import.sql",
		   evt -> ImportSQL.selectFile(mainFrame));
		menuFileImport.add(fileImportSql);
		fileImportXml = initMenuItem("file.import.xml",
		   evt -> ImportDlg.show(mainFrame));
		menuFileImport.add(fileImportXml);*/
	}

	/**
	 * initialize the file export menu
	 */
	private void menuFileExportInit() {
		menuFileExport = new JMenu(I18N.getMsg("file.export"));
		menuFile.add(menuFileExport);

		fileExportStoryboard = initMenuItem(K.VW_STORYBOARD, "exportStoryboard", "", ' ',
			"view.storyboard",
			evt -> ExportStoryboard.execute(mainFrame));
		menuFileExport.add(fileExportStoryboard);

		fileExportBOOK = initMenuItem(K.VW_BOOK, "book", "exportBOOK",
			evt -> ExportBookDlg.show(mainFrame));
		menuFileExport.add(fileExportBOOK);

		fileExportProject = initMenuItem(K.COLUMNS, "project.archive", "exportProject",
			evt -> mainFrame.projectArchive());
		menuFileExport.add(fileExportProject);
		fileExportOther = initMenuItem(K.EXPAND, "file.export.other", "exportOther",
			evt -> ExportDlg.show(mainFrame));
		menuFileExport.add(fileExportOther);
	}

	/**
	 * initialize the edit menu
	 */
	private void menuEditInit() {
		//LOG.printInfos(TT+"initMenuEdit()");
		menuEdit = initMenu("edit");

		editCopyBook = initMenuItem(K.EDIT_COPY, "book.copy.text", "bookCopy",
			evt -> ExportBookToHtml.toClipboard(mainFrame, (Part) null));
		menuEdit.add(editCopyBook);

		editCopyBlurb = initMenuItem(K.EDIT_COPY, "book.copy.blurb", "blurbCopy");
		editCopyBlurb.addActionListener(evt -> {
			Clip.to(mainFrame.getBook().getBlurb(), "txt");
		});
		menuEdit.add(editCopyBlurb);

		editCopyEntity = initMenuItem("copy.menu", evt -> EntityCopyDlg.show(mainFrame));
		menuEdit.add(editCopyEntity);

		editPaste = initMenuItem("edit.paste_as", evt -> mainFrame.pasteAs());
		menuEdit.add(editPaste);

		resetEditor = initScMenuItem("editor.reset",
			evt -> SwingUtil.resetDlgPosition());
		menuEdit.add(resetEditor);

		menuEdit.add(new JPopupMenu.Separator());

		editPreferences = initMenuItem("preferences.title");
		editPreferences.setIcon(IconUtil.getIconSmall(K.PREFERENCES));
		editPreferences.addActionListener(evt -> {
			PreferencesDlg dlg = new PreferencesDlg(mainFrame);
			SwingUtil.showModalDialog(dlg, mainFrame);
		});
		menuEdit.add(editPreferences);

		menuBar.add(menuEdit);
	}

	/**
	 * initialize the new entity menu
	 */
	private void menuNewInit() {
		//LOG.printInfos(TT+"initMenuNew()");
		menuNewEntity = initMenu("new");
		//ctrl alt D
		newStrand = initScMenuItem(K.ENT_STRAND, "new.strand", "",
			evt -> mainFrame.newEntity(new Strand()));
		menuNewEntity.add(newStrand);
		//ctrl alt A
		newPart = initScMenuItem(K.ENT_PART, "new.part", "",
			evt -> mainFrame.newEntity(new Part()));
		menuNewEntity.add(newPart);
		//ctrl alt C
		newChapter = initScMenuItem(K.ENT_CHAPTER, "new.chapter", "",
			evt -> mainFrame.newEntity(new Chapter()));
		menuNewEntity.add(newChapter);
		//ctrl alt S
		newScene = initScMenuItem(K.ENT_SCENE, "new.scene", "",
			evt -> mainFrame.newEntity(new Scene()));
		menuNewEntity.add(newScene);

		JMenu multi = new JMenu(I18N.getMsg("new.multi"));
		menuNewEntity.add(multi);
		toolsNewParts = initMenuItem(K.ENT_PART, "parts", "newParts",
			evt -> {
				PartsCreateDlg dlg = new PartsCreateDlg(mainFrame);
				dlg.setVisible(true);
				if (!dlg.isCanceled()) {
					mainFrame.getBookModel().refreshParts();
					mainFrame.setUpdated();
				}
			});
		multi.add(toolsNewParts);

		newChapters = initMenuItem(K.ENT_CHAPTER, "chapters", "newChapters",
			evt -> {
				ChaptersCreateDlg dlg = new ChaptersCreateDlg(mainFrame);
				dlg.setVisible(true);
				if (!dlg.isCanceled()) {
					mainFrame.getBookModel().refreshChapters();
					mainFrame.setUpdated();
				}
			});
		multi.add(newChapters);

		toolsNewScenes = initMenuItem(K.ENT_SCENE, "scenes", "newScenes",
			evt -> {
				ScenesCreateDlg dlg = new ScenesCreateDlg(mainFrame);
				dlg.setVisible(true);
				if (!dlg.isCanceled()) {
					mainFrame.getBookModel().refreshScenes();
					mainFrame.setUpdated();
				}
			});
		multi.add(toolsNewScenes);
		//ctrl alt O
		newPlot = initScMenuItem(K.ENT_PLOT, "new.plot", "",
			evt -> mainFrame.newEntity(new Plot()));
		menuNewEntity.add(newPlot);
		menuNewEntity.add(new JPopupMenu.Separator());
		//ctrl alt P
		newPerson = initScMenuItem(K.ENT_PERSON, "new.person", "",
			evt -> mainFrame.newEntity(new Person()));
		menuNewEntity.add(newPerson);
		//ctrl alt G
		newGender = initScMenuItem(K.ENT_GENDER, "new.gender", "",
			evt -> mainFrame.newEntity(new Gender()));
		menuNewEntity.add(newGender);
		//ctrl alt Y
		newCategory = initScMenuItem(K.ENT_CATEGORY, "new.category", "",
			evt -> mainFrame.newEntity(new Category()));
		menuNewEntity.add(newCategory);
		//ctrl alt R
		newRelation = initScMenuItem(K.ENT_RELATION, "new.relation", "",
			evt -> {
				mainFrame.newEntity(new Relation());
			});
		menuNewEntity.add(newRelation);

		newAttribute = initMenuItem(K.EMPTY, "new.attribute", "newAttribute",
			evt -> mainFrame.newEntity(new Attribute()));
		menuNewEntity.add(newAttribute);
		//ctrl alt V
		newEvent = initScMenuItem(K.ENT_EVENT, "new.event", "",
			evt -> mainFrame.newEntity(new Event()));
		menuNewEntity.add(newEvent);

		menuNewEntity.add(new JPopupMenu.Separator());
		//ctrl alt L
		newLocation = initScMenuItem(K.ENT_LOCATION, "new.location", "",
			evt -> mainFrame.newEntity(new Location()));
		menuNewEntity.add(newLocation);
		menuNewEntity.add(new JPopupMenu.Separator());
		//ctrl alt I
		newItem = initScMenuItem(K.ENT_ITEM, "new.item", "",
			evt -> mainFrame.newEntity(new Item()));
		menuNewEntity.add(newItem);

		menuNewEntity.add(new JPopupMenu.Separator());
		//ctrl alt T
		newTag = initScMenuItem(K.ENT_TAG, "new.tag", "",
			evt -> mainFrame.newEntity(new Tag()));
		menuNewEntity.add(newTag);

		menuNewEntity.add(new JPopupMenu.Separator());
		//ctrl Z
		newFOI = initScMenuItem(K.ENT_IDEA, "foi.title", "",
			evt -> FoiDlg.show(mainFrame));
		menuNewEntity.add(newFOI);
		//ctrl alt F
		newIdea = initScMenuItem(K.ENT_IDEA, "new.idea", "",
			evt -> mainFrame.newEntity(new Idea()));
		menuNewEntity.add(newIdea);
		//ctrl alt M
		newMemo = initScMenuItem(K.ENT_MEMO, "new.memo", "",
			evt -> mainFrame.newEntity(new Memo()));
		menuNewEntity.add(newMemo);

		menuBar.add(menuNewEntity);
	}

	/**
	 * initialize the table menu
	 */
	private void menuTableInit() {
		//LOG.printInfos(TT+"initMenuTable()");
		menuTables = initMenu("tables");
		menuTablePrimaryInit();
		menuTables.add(menuPrimaryObjects);
		menuTableSecondaryInit();
		menuTables.add(menuSecondaryObjects);
		menuBar.add(menuTables);
	}

	private void menuTablePrimaryInit() {
		menuPrimaryObjects = new JMenu(I18N.getMsg("primary.objects"));

		tabInternal = initMenuItem(K.EMPTY, "internal", "tabInternal",
			evt -> mainFrame.showAndFocus(VIEWNAME.INTERNALS));
		menuPrimaryObjects.add(tabInternal);
		if (!App.isDev()) {
			tabInternal.setVisible(false);
		}
		tabStrand = initMenuItem(K.TABLE_STRANDS, "strand", "tabStrand",
			evt -> mainFrame.showAndFocus(VIEWNAME.STRANDS));
		menuPrimaryObjects.add(tabStrand);

		tabPart = initMenuItem(K.TABLE_PARTS, "part", "tabPart",
			evt -> mainFrame.showAndFocus(VIEWNAME.PARTS));
		menuPrimaryObjects.add(tabPart);

		tabChapter = initMenuItem(K.TABLE_CHAPTERS, "chapter", "tabChapter",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHAPTERS));
		menuPrimaryObjects.add(tabChapter);

		tabScene = initMenuItem(K.TABLE_SCENES, "scene", "tabScene",
			evt -> mainFrame.showAndFocus(VIEWNAME.SCENES));
		menuPrimaryObjects.add(tabScene);

		tabPlot = initMenuItem(K.TABLE_PLOTS, "plot", "tabPlot",
			evt -> mainFrame.showAndFocus(VIEWNAME.PLOTS));
		menuPrimaryObjects.add(tabPlot);

		tabPerson = initMenuItem(K.TABLE_PERSONS, "person", "tabPerson",
			evt -> mainFrame.showAndFocus(VIEWNAME.PERSONS));
		menuPrimaryObjects.add(tabPerson);

		tabLocation = initMenuItem(K.TABLE_LOCATIONS, "location", "tabLocation",
			evt -> mainFrame.showAndFocus(VIEWNAME.LOCATIONS));
		menuPrimaryObjects.add(tabLocation);

		tabItem = initMenuItem(K.TABLE_ITEMS, "item", "tabItem",
			evt -> mainFrame.showAndFocus(VIEWNAME.ITEMS));
		menuPrimaryObjects.add(tabItem);
	}

	private void menuTableSecondaryInit() {
		menuSecondaryObjects = new JMenu(I18N.getMsg("secondary.objects"));

		tabGender = initMenuItem(K.TABLE_GENDERS, "genders", "tabGender",
			evt -> mainFrame.showAndFocus(VIEWNAME.GENDERS));
		menuSecondaryObjects.add(tabGender);

		tabCategory = initMenuItem(K.TABLE_CATEGORIES, "persons.category", "tabCategory",
			evt -> mainFrame.showAndFocus(VIEWNAME.CATEGORIES));
		menuSecondaryObjects.add(tabCategory);

		tabRelation = initMenuItem(K.TABLE_RELATIONS, "relations", "tabRelation",
			evt -> mainFrame.showAndFocus(VIEWNAME.RELATIONS));
		menuSecondaryObjects.add(tabRelation);

		tabAttribute = initMenuItem(K.EMPTY, "attributes", "tabAttribute",
			evt -> mainFrame.showAndFocus(VIEWNAME.ATTRIBUTES));
		menuSecondaryObjects.add(tabAttribute);

		tabEvent = initMenuItem(K.ENT_EVENT, "events", "tabEvent",
			evt -> mainFrame.showAndFocus(VIEWNAME.EVENTS));
		menuSecondaryObjects.add(tabEvent);

		menuSecondaryObjects.add(new JPopupMenu.Separator());

		/*tabItemLink = initMenuItem(K.TABLE_ITEMLINKS, "itemlink", "tabItemLink",
		   evt -> mainFrame.showAndFocus(VIEWNAME.ITEMLINKS));
		menuSecondaryObjects.add(tabItemLink);*/
		menuSecondaryObjects.add(new JPopupMenu.Separator());

		tabTag = initMenuItem(K.TABLE_TAGS, "tags", "tabTag",
			evt -> mainFrame.showAndFocus(VIEWNAME.TAGS));
		menuSecondaryObjects.add(tabTag);

		/*tabTagLink = initMenuItem(K.TABLE_TAGLINKS, "taglink", "tabTagLink",
		   evt -> mainFrame.showAndFocus(VIEWNAME.TAGLINKS));
		menuSecondaryObjects.add(tabTagLink);
		menuSecondaryObjects.add(new JPopupMenu.Separator());*/
		tabEndnote = initMenuItem(K.CHAR_ENDNOTE, "endnote", "tabEndnote",
			evt -> mainFrame.showAndFocus(VIEWNAME.ENDNOTES));
		menuSecondaryObjects.add(tabEndnote);

		menuSecondaryObjects.add(new JPopupMenu.Separator());

		tabMemo = initMenuItem(K.TABLE_MEMOS, "memos", "tabMemo",
			evt -> mainFrame.showAndFocus(VIEWNAME.MEMOS));
		menuSecondaryObjects.add(tabMemo);

		tabIdea = initMenuItem(K.TABLE_IDEAS, "ideas", "tabIdea",
			evt -> mainFrame.showAndFocus(VIEWNAME.IDEAS));
		menuSecondaryObjects.add(tabIdea);
	}

	/**
	 * initialize the tools menu
	 */
	private void menuToolsInit() {
		//LOG.printInfos(TT+"initMenuTools()");
		menuTools = initMenu("tools");

		toolsChallenge = initScMenuItem("challenge", evt -> ChallengeDlg.show(mainFrame));
		if (App.isDev()) {
			menuTools.add(toolsChallenge);
		}
		toolsTypist = initMenuItem("typist", evt -> {
			if (mainFrame.isEpisode) {
				mainFrame.episodeActivate();
				mainFrame.typistActivate();
				toolsTypist.setVisible(false);
			} else {
				toolsEpisodes.setVisible(true);
			}
		});
		toolsTypist.setVisible(false);
		menuTools.add(toolsTypist);
		JMenu submenu = initMenu("story");
		toolsEpisodes = initMenuItem("episodes",
			evt -> {
				if (mainFrame.isTypist) {
					mainFrame.typistActivate();
					mainFrame.episodeActivate();
					toolsEpisodes.setVisible(false);
				} else if (!mainFrame.isEpisode) {
					mainFrame.showAndFocus(VIEWNAME.EPISODES);
				}
			});
		submenu.add(toolsEpisodes);
		toolsClassic = initMenuItem("story.three", evt -> {
			mainFrame.showAndFocus(VIEWNAME.STORY_THREE);
		});
		toolsClassic.setEnabled(Assistant.hasClassic());
		submenu.add(toolsClassic);
		toolsFreytag = initMenuItem("story.freytag", evt -> {
			mainFrame.showAndFocus(VIEWNAME.STORY_FREYTAG);
		});
		toolsFreytag.setEnabled(Assistant.hasFreytag());
		submenu.add(toolsFreytag);
		toolsVogler = initMenuItem("story.vogler", evt -> {
			mainFrame.showAndFocus(VIEWNAME.STORY_VOGLER);
		});
		toolsVogler.setEnabled(Assistant.hasVogler());
		submenu.add(toolsVogler);
		menuTools.add(submenu);

		toolsSearch = initScMenuItem("find", evt -> SearchDlg.show(mainFrame));
		menuTools.add(toolsSearch);

		toolsReplace = initScMenuItem("replace", evt -> ReplaceDlg.show(mainFrame));
		menuTools.add(toolsReplace);

		toolsChaptersOrder = initMenuItem("chapters.order",
			evt -> ChaptersOrderDlg.show(mainFrame));
		menuTools.add(toolsChaptersOrder);

		toolsScenesOrder = initMenuItem("scenes.renumber",
			evt -> {
				if (SceneRenumber.show(mainFrame)) {
					App.getInstance().setWaitCursor();
					final WaitingSplash dlg
					= new WaitingSplash(mainFrame, I18N.getMsg("scenes.renumber"));
					dlg.setVisible(true);
					SwingUtilities.invokeLater(() -> {
						try {
							mainFrame.project.scenes.renumber(mainFrame);
						} catch (Exception e) {
							ExceptionDlg.show(getClass().getSimpleName()
								+ ".initMenuTools()", e);
						}
						dlg.dispose();
					});
					App.getInstance().setDefaultCursor();
				}
			});
		menuTools.add(toolsScenesOrder);

		JMenu menuLinks = new JMenu(I18N.getMsg("links"));
		toolsPersonsLinks = initMenuItem("links.person",
			evt -> ScenesLinks.show(mainFrame, Book.TYPE.PERSON));
		menuLinks.add(toolsPersonsLinks);
		toolsLocationsLinks = initMenuItem("links.location",
			evt -> ScenesLinks.show(mainFrame, Book.TYPE.LOCATION));
		menuLinks.add(toolsLocationsLinks);
		toolsItemsLinks = initMenuItem("links.item",
			evt -> ScenesLinks.show(mainFrame, Book.TYPE.PERSON));
		menuLinks.add(toolsItemsLinks);
		menuTools.add(menuLinks);

		toolsRenumberEndnotes = initMenuItem("endnotes.renumber",
			evt -> {
				if (Endnote.renumber(mainFrame, 0)) {
					mainFrame.setUpdated();
					mainFrame.getBookModel().fireAgain();
				}
			});
		menuTools.add(toolsRenumberEndnotes);

		toolsRename = new JMenu(I18N.getMsg("tools.rename"));
		toolsRename.setIcon(IconUtil.getIconSmall(K.RENAME));

		renameCity = initMenuItem("location.rename.city",
			evt -> {
				RenameDlg dlg = new RenameDlg(mainFrame, "city");
				SwingUtil.showModalDialog(dlg, mainFrame);
			});
		toolsRename.add(renameCity);

		renameCountry = initMenuItem("location.rename.country",
			evt -> {
				RenameDlg dlg = new RenameDlg(mainFrame, "country");
				SwingUtil.showModalDialog(dlg, mainFrame);
			});
		toolsRename.add(renameCountry);

		renameTagCategory = initMenuItem("tag.rename.category",
			evt -> {
				RenameDlg dlg = new RenameDlg(mainFrame, "tag");
				SwingUtil.showModalDialog(dlg, mainFrame);
			});
		toolsRename.add(renameTagCategory);

		renameItemCategory = initMenuItem("item.rename.category",
			evt -> {
				RenameDlg dlg = new RenameDlg(mainFrame, "item");
				SwingUtil.showModalDialog(dlg, mainFrame);
			});
		toolsRename.add(renameItemCategory);

		menuTools.add(toolsRename);

		separator3 = new JPopupMenu.Separator();
		menuTools.add(separator3);
		toolsEpubCover = initMenuItem("epub.cover.create",
			evt -> PropEpubCover.show(mainFrame));
		menuTools.add(toolsEpubCover);

		menuBar.add(menuTools);

	}

	/**
	 * initialize the views menu
	 */
	private void menuViewInit() {
		//LOG.printInfos(TT+"initMenuView()");
		menuView = initMenu("view");

		vueTypist = initMenuItem(K.VW_TYPIST, "typist", "",
			evt -> mainFrame.typistActivate());
		menuView.add(vueTypist);
		menuView.addSeparator();

		vueChrono = initMenuItem(K.VW_CHRONO, "view.chrono", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHRONO));
		menuView.add(vueChrono);

		vueTimeline = initMenuItem(K.VW_TIMELINE, "view.timeline", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.TIMELINE));
		menuView.add(vueTimeline);

		vueBook = initMenuItem(K.VW_BOOK, "view.book", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.BOOK));
		menuView.add(vueBook);

		vueReading = initMenuItem(K.VW_READING, "view.reading", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.READING));
		menuView.add(vueReading);

		vueManageScene = initMenuItem(K.VW_MANAGE, "view.manage", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.MANAGE));
		menuView.add(vueManageScene);

		vueMemoria = initMenuItem(K.VW_MEMORIA, "view.memoria", "",
			evt -> {
				mainFrame.showAndFocus(VIEWNAME.MEMORIA);
			});
		menuView.add(vueMemoria);

		vueStoryboard = initMenuItem(K.STAMP, "view.storyboard", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.STORYBOARD));
		menuView.add(vueStoryboard);

		vueStorymap = initMenuItem(K.VW_STORYMAP, "view.storymap", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.STORYMAP));
		menuView.add(vueStorymap);

		menuView.add(new JPopupMenu.Separator());

		toolsPlan = initMenuItem(K.VW_PLAN, "view.plan", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.PLANNING));
		menuView.add(toolsPlan);

		vueTree = initMenuItem(K.VW_TREE, "tree", "", ' ', "tree",
			evt -> mainFrame.showAndFocus(VIEWNAME.TREE));
		menuView.add(vueTree);

		vueInfo = initMenuItem(K.INFO, "view.info", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.INFO));
		menuView.add(vueInfo);

		/*vueNavigation = initMenuItem(K.COMPASS, "", ' ',
			"navigation", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.NAVIGATION));
		menuView.add(vueNavigation);*/
		vueMemos = initMenuItem(K.ENT_MEMO, "memo", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.MEMOS));
		menuView.add(vueMemos);

		menuBar.add(menuView);
	}

	/**
	 * initialize the charts menu
	 */
	private void menuChartsInit() {
		//LOG.printInfos(TT+"initMenuCharts()");
		menuCharts = initMenu("charts");

		chartAttributes = initMenuItem(K.COLUMNS, "attribute.list", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.ATTRIBUTESLIST));
		if (App.isDev()) {
			menuCharts.add(chartAttributes);
		}

		chartPersonsByDate = initMenuItem(K.VW_CHART, "tools.charts.overall.character.date", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_PERSONS_BY_DATE));
		menuCharts.add(chartPersonsByDate);

		chartPersonsByScene = initMenuItem(K.VW_CHART, "tools.charts.part.character.scene", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_PERSONS_BY_SCENE));
		menuCharts.add(chartPersonsByScene);

		chartWIWW = initMenuItem(K.VW_CHART, "tools.charts.overall.whoIsWhereWhen", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_WIWW));
		menuCharts.add(chartWIWW);

		chartStrands = initMenuItem(K.VW_CHART, "tools.charts.overall.strand.date", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_STRANDS_BY_DATE));
		menuCharts.add(chartStrands);
		menuCharts.add(new JPopupMenu.Separator());

		chartPersons = initMenuItem(K.VW_CHART, "tools.charts.overall.character.occurrence", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_OCCURRENCE_OF_PERSONS));
		menuCharts.add(chartPersons);

		chartLocations = initMenuItem(K.VW_CHART, "tools.charts.overall.location.occurrence", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_OCCURRENCE_OF_LOCATIONS));
		menuCharts.add(chartLocations);

		chartItems = initMenuItem(K.VW_CHART, "tools.charts.overall.item.occurrence", "",
			evt -> mainFrame.showAndFocus(VIEWNAME.CHART_OCCURRENCE_OF_ITEMS));
		menuCharts.add(chartItems);

		menuBar.add(menuCharts);
	}

	/**
	 * initialize the window menu
	 */
	private void menuWindowInit() {
		menuWindow = initMenu("window");

		windowLoadLayout = new JMenu(I18N.getMsg("docking.load.layout"));
		menuWindow.add(windowLoadLayout);

		windowSaveLayout = initMenuItem("docking.save.layout", evt -> mainFrame.windowLayoutSave());
		menuWindow.add(windowSaveLayout);

		windowPredefinedLayout = new JMenu(I18N.getMsg("docking.predefined"));
		DockUtil.layoutGetMenu(mainFrame, windowPredefinedLayout);
		menuWindow.add(windowPredefinedLayout);

		windowDefaultLayout = initMenuItem("layout.default");
		windowDefaultLayout.addActionListener(evt -> DockUtil.layoutLoadFromResource(mainFrame, "default"));

		windowResetLayout = initMenuItem("docking.reset.layout", evt -> {
			SwingUtil.setWaitingCursor(mainFrame);
			mainFrame.setDefaultLayout();
			SwingUtil.setDefaultCursor(mainFrame);
		});
		menuWindow.add(windowResetLayout);

		windowRefresh = initScMenuItem(K.EMPTY, "refresh", "",
			evt -> mainFrame.refresh());
		menuWindow.add(windowRefresh);

		menuBar.add(menuWindow);
	}

	/**
	 * initialize the help menu
	 */
	private void menuHelpInit() {
		//LOG.printInfos(TT+"initMenuHelp()");
		menuHelp = initMenu("help");

		helpHome = initMenuItem("help.homepage",
			evt -> Net.openBrowser(Net.getI18nUrl(Net.KEY.HOME)));
		menuHelp.add(helpHome);

		helpDoc = initMenuItem("help.doc",
			evt -> Net.openBrowser(Net.getI18nUrl(Net.KEY.HOME) + Net.KEY.DOC));
		menuHelp.add(helpDoc);

		helpFaq = initMenuItem("help.faq",
			evt -> Net.openBrowser(Net.getI18nUrl(Net.KEY.HOME) + Net.KEY.FAQ));
		menuHelp.add(helpFaq);

		helpReportBug = initMenuItem("help.bug",
			evt -> Net.openBrowser(Net.KEY.BUG.toString()));
		menuHelp.add(helpReportBug);

		JMenuItem helpShortcut = initScMenuItem(K.HELP, "help.shortcut", "",
			evt -> ShortcutsDlg.show(mainFrame.getFullFrame()));
		menuHelp.add(helpShortcut);

		helpAbout = initMenuItem("help.about", evt -> AboutDlg.show(mainFrame));
		menuHelp.add(helpAbout);
		menuHelp.add(new JPopupMenu.Separator());

		editSpellChecker = initMenuItem("preferences.spelling");
		//editSpellChecker.setIcon(new ImageIcon(getClass().getResource("/api/jortho/jortho.png")));
		editSpellChecker.setIcon(IconUtil.getIconSmall(K.SPELL));
		editSpellChecker.addActionListener(evt -> {
			SpellDlg dlg = new SpellDlg(mainFrame);
			dlg.setVisible(true);
		});
		menuHelp.add(editSpellChecker);

		editAssistant = initMenuItem(K.ASSISTANT, "assistant", "assistant",
			evt -> {
				AssistantWizardDlg dlg = new AssistantWizardDlg(mainFrame);
				dlg.setVisible(true);
			});
		menuHelp.add(editAssistant);

		JMenuItem ideabox2 = initMenuItem(K.IDEABOX, "ideabox", "ideabox",
			evt -> {
				IdeaxFrm dlg = new IdeaxFrm();
				dlg.setVisible(true);
			});
		menuHelp.add(ideabox2);

		menuHelp.add(new JPopupMenu.Separator());

		helpCheckUpdates = initMenuItem("help.update",
			evt -> {
				if (Updater.checkForUpdate(true)) {
					JOptionPane.showMessageDialog(mainFrame,
						I18N.getMsg("update.no.text"),
						I18N.getMsg("update.no.title"),
						JOptionPane.INFORMATION_MESSAGE);
				}
			});
		menuHelp.add(helpCheckUpdates);

		JCheckBoxMenuItem helpTrace = new JCheckBoxMenuItem();
		helpTrace.setFont(App.fonts.defGet());
		helpTrace.setMnemonic('T');
		helpTrace.setText(I18N.getMsg("help.trace"));
		helpTrace.addActionListener(evt -> {
			LOG.setTrace(!LOG.getTrace());
			helpTrace.setSelected(LOG.getTrace());
		});
		menuHelp.add(helpTrace);

		devTest = initMenuItem("!Dev-test", evt -> {
			testFunction = (devTest.getText().contains("Dev"));
			devTest.setText((testFunction ? "Test in progress" : "Dev-Test"));
			//function to test
			System.out.println("résultat = " + (5 / 0));

			//restore dev menu
			devTest.setText((testFunction ? "Test in progress" : "Dev-Test"));
		});
		if (App.isDev()) {
			menuHelp.add(devTest);
		}

		setOut = initMenuItem("!Log to file", evt -> setSystemOut());
		menuHelp.add(setOut);

		JMenu menuTranslate = new JMenu(I18N.getMsg("help.translate"));
		JMenuItem helpAssistant = initMenuItem("assistant",
			evt -> AppAssistant.show("--sub")
		);
		menuTranslate.add(helpAssistant);
		helpTranslate = initMenuItem("help.ui",
			evt -> I18NDlg.show(mainFrame));
		menuTranslate.add(helpTranslate);
		menuHelp.add(menuTranslate);

		menuBar.add(menuHelp);
	}

	public void setMenuForTypist() {
		JMenu menus[] = {
			menuFileCreate,
			menuFileRecent,
			menuFileBackRest,
			//menuFileImport,
			menuTables,
			menuView,
			menuCharts,
			menuWindow,
			toolsRename
		};
		for (javax.swing.JMenu m : menus) {
			if (m != null) {
				m.setVisible(false);
			}
		}
		JMenuItem submenus[] = {
			fileRename, fileOpen, fileSaveAs,
			//fileImportXml,
			fileBackupNew, fileBackupRest,
			fileAssistant, menuFileExport, fileClose,
			editPaste,
			resetEditor, editCopyBlurb, editCopyBook, editCopyEntity,
			toolsEpubCover, toolsChaptersOrder, toolsScenesOrder,
			toolsRenumberEndnotes
		};
		for (JMenuItem m : submenus) {
			if (m != null) {
				m.setVisible(false);
			}
		}
		JSeparator seps[] = {
			separator0, separator1, separator2, separator3, separator4
		};
		for (JSeparator sep : seps) {
			if (sep != null) {
				sep.setVisible(false);
			}
		}
		JMenu separators[] = {
			menuFile, menuEdit, menuTools
		};
		for (JMenu mx : separators) {
			for (Component comp : mx.getMenuComponents()) {
				if (comp instanceof JSeparator) {
					comp.setVisible(false);
				}
			}
		}
		toolsEpisodes.setVisible(!mainFrame.isEpisode);
		toolsTypist.setVisible(mainFrame.isEpisode);
		menuFile.add(new JSeparator());
		menuFile.add(menuEdit);
		menuFile.add(menuNewEntity);
		menuFile.add(menuTools);
		menuFile.add(menuHelp);
		menuBar.remove(menuEdit);
		menuBar.remove(menuNewEntity);
		menuBar.remove(menuTools);
		menuBar.remove(menuHelp);
	}

	/**
	 * set the visible menu for blank mainframe
	 */
	public void setMenuForBlank() {
		//LOG.printInfos(TT+"initMenuForBlank()");
		// hide menus from MenuBar
		JMenu menus[] = {
			menuNewEntity, menuTables, menuPrimaryObjects, menuSecondaryObjects,
			menuCharts, menuCharts, menuView, menuWindow, menuTools
		};
		for (javax.swing.JMenu m : menus) {
			if (m != null) {
				m.setVisible(false);
			}
		}
		JMenuItem submenus[] = {
			editCopyBlurb, editCopyBook, editCopyEntity,
			fileClose, menuFileExport,
			fileProperties, fileRename, fileSave, fileSaveAs, menuFileExport,
			//fileImportXml,
			fileBackupNew, fileBackupRest, fileAssistant
		};
		for (JMenuItem m : submenus) {
			if (m != null) {
				m.setVisible(false);
			}
		}
		JSeparator separators[] = {
			separator0, separator1, separator2//, separator3
		};
		for (JSeparator sep : separators) {
			sep.setVisible(false);
		}
		for (Component comp : toolBar.getComponents()) {
			if (comp instanceof JButton) {
				((JButton) comp).setVisible(false);
			}
		}
		btFileNew.setVisible(true);
		btFileOpen.setVisible(true);
		devTest.setVisible(App.isDev());
	}

	/**
	 * enable/disable write in the menu
	 *
	 * @param b
	 */
	public void enableSave(boolean b) {
		//LOG.printInfos(TT+"enableSave()");
		if (fileSave != null) {
			fileSave.setEnabled(b);
		}
		if (btFileSave != null) {
			btFileSave.setEnabled(b);
		}
	}

	/**
	 * get the recent file submenu
	 *
	 * @return
	 */
	public JMenu getMenuFileOpenRecent() {
		return menuFileRecent;
	}

	/**
	 * reload the toolbar
	 */
	public void reloadToolbar() {
		if (toolBar != null) {
			toolBar.removeAll();
		}
		initToolbar();
		setToolbar();
		if (fileExportStoryboard != null && mainFrame.getBook() != null) {
			fileExportStoryboard.setEnabled(mainFrame.getBook().info.scenarioGet());
		}
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		//LOG.printInfos(TT+".mouseClicked(evt=" + evt.toPrint());
		if (SwingUtilities.isRightMouseButton(evt)) {
			ToolBarDlg dlg = new ToolBarDlg(mainFrame);
			dlg.setVisible(true);
			if (!dlg.isCanceled()) {
				setToolbar();
			}
			evt.consume();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	/**
	 * enable/disable the links tool
	 */
	void setLinks() {
		if (mainFrame == null || toolsPersonsLinks == null) {
			return;
		}
		toolsPersonsLinks.setEnabled(Book.getNbPersons(mainFrame) > 0);
		toolsLocationsLinks.setEnabled(Book.getNbLocations(mainFrame) > 0);
		toolsItemsLinks.setEnabled(Book.getNbItems(mainFrame) > 0);
	}

	/**
	 * enable paste
	 *
	 * @param b
	 */
	public void enablePaste(boolean b) {
		editPaste.setVisible(b);
	}

	/**
	 * enable copy entities
	 *
	 * @param b
	 */
	public void enableCopyEntity(boolean b) {
		editCopyEntity.setVisible(b);
	}

	/**
	 * hide the toolbar when mainframe is blank
	 */
	public void hideToolbar() {
		toolBar.setVisible(false);
	}

	public void setMenuCopy() {
		//LOG.printInfos(TT + ".setMenuCopy()");
		int nb = App.getInstance().getMainFrames().size();
		editCopyEntity.setVisible(nb > 1);
	}

	private void setSystemOut() {
		this.setOut.setEnabled(false);
		try {
			File log = EnvUtil.getLogFile();
			if (log.exists()) {
				log.delete();
			}
			PrintStream pslog = new PrintStream(log);
			System.setErr(pslog);
			System.setOut(pslog);
			System.out.println(DateUtil.dateToStandard(new Date())
				+ " " + Const.getFullName()
				+ "-> Starting logfile");
		} catch (FileNotFoundException ex) {
			LOG.err("Redirecting standard output error");
		}
	}

	public void reloadRecentMenu() {
		//LOG.printInfos(TT + ".reloadRecentMenu()");
		JMenu miRecent = menuFileRecent;
		miRecent.removeAll();
		App.preferences.recentFilesLoad();
		List<Pref.PrefFile> list = App.preferences.recentFiles;
		for (Pref.PrefFile p : list) {
			File f = new File(p.file);
			if (!f.exists()) {
				continue;
			}
			miRecent.add(initMenuItem("!" + p.title,
				e -> App.getInstance().openProject(new Project(p.file))));
		}
		miRecent.addSeparator();
		miRecent.add(initMenuItem("file.clear.recent",
			e -> App.getInstance().recentfilesClear()));
		miRecent.setEnabled(!list.isEmpty());
		setMenuCopy();
	}

	public void reloadWindowMenu() {
		//LOG.printInfos(TT+".reloadWindowMenu(" + menubar.getName() + ")");
		JMenu miLoad = windowLoadLayout;
		miLoad.removeAll();
		File dir = new File(EnvUtil.getPrefDir().getAbsolutePath());
		File[] files = dir.listFiles();
		if (files == null || files.length < 1) {
			LOG.log("** Pref dir is empty");
			return;
		}
		for (File file : files) {
			if (file.isFile()) {
				String name = file.getName();
				if (name.endsWith(".layout")) {
					String text = name.substring(0, name.lastIndexOf(".layout"));
					if (text.equals("_internal_last_used_layout_")
						|| text.equals("LastUsedLayout")) {
						continue;
					}
					miLoad.add(
						MainMenu.initMenuItem("!" + text,
							e -> DockUtil.layoutLoadFromFile(mainFrame, text)));
				}
			}
		}
	}

	public static JMenuBar getHiddenMenu(JPanel comp) {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuHelp = initMenu("menu");
		JMenuItem item = initScMenuItem(ICONS.K.HELP, "help.shortcut", "",
			evt -> ShortcutsDlg.show(comp));
		menuHelp.add(item);
		menuBar.add(menuHelp);
		menuBar.setMaximumSize(new Dimension(1, 1));
		return menuBar;
	}

}
