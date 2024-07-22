/*
 STORYBOOK: Open Source software for novelists and authors.
 Copyright (C) 2008 - 2012 Martin Mustun
        2013-2023 adaptation, evolution FaVdB

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a fileCopy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui;

import api.infonode.docking.DockingWindow;
import api.infonode.docking.DockingWindowAdapter;
import api.infonode.docking.RootWindow;
import api.infonode.docking.SplitWindow;
import api.infonode.docking.TabWindow;
import api.infonode.docking.View;
import api.infonode.docking.ViewSerializer;
import api.infonode.docking.properties.RootWindowProperties;
import api.infonode.docking.theme.DockingWindowsTheme;
import api.infonode.docking.theme.ShapedGradientDockingTheme;
import api.infonode.docking.util.DockingUtil;
import api.infonode.docking.util.MixedViewHandler;
import api.infonode.docking.util.StringViewMap;
import api.infonode.util.Direction;
import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.action.MainActions;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.episode.panel.EpisodePanel;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.dialog.ChooseFileDlg;
import storybook.dialog.MessageDlg;
import storybook.edit.Editor;
import storybook.exim.importer.ImportDocument;
import storybook.exim.importer.ImportEpub;
import storybook.model.BlankModel;
import storybook.model.Model;
import storybook.project.Project;
import storybook.tools.DockUtil;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.file.TempUtil;
import storybook.tools.file.XEditorFile;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.xml.Xml;
import storybook.ui.SbView.VIEWNAME;
import storybook.ui.interfaces.IPaintable;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.BlankPanel;
import storybook.ui.panel.typist.TypistPanel;
import storybook.ui.panel.typist.TypistScenario;

/**
 * Main frame class
 *
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements IPaintable {

    private static final String TT = "MainFrame.";

    public final static int SCREEN_WIDTH = 1000,
	    SCREEN_HEIGHT = 700,
	    SCREEN_X = 100,
	    SCREEN_Y = 100;

    private Model model;
    private Ctrl ctrl;
    private SbViewFactory viewFactory;
    private MainActions mainActions;
    private MainMenu mainMenu;
    private JToolBar mainToolBar;
    private RootWindow rootWindow;
    public StatusbarPanel statusBar;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final HashMap<Integer, JComponent> dynamicViews = new HashMap<>();
    private boolean editorModless;
    private Part lastPart = null;
    private Chapter lastChapter;
    private Scene lastScene = null;
    private boolean modified = false;
    public boolean showAllParts = false;
    private TypistPanel typistScene;
    private TypistScenario typistScenario;
    //private SbCalendar calendar;
    public boolean isTypist = false, isEpisode = false, isScenario = false;
    public JFrame fullFrame;
    private Book book;
    private EpisodePanel episodePanel;
    public Project project;
    private Date lastSceneDate;

    /**
     * Main frame class with only Font initialization
     *
     */
    public MainFrame() {
	FontUtil.setDefault(new Font("Arial", Font.PLAIN, 12));
    }

    /**
     * Main frame class with file initialization
     *
     * @param dbF
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public MainFrame(Project dbF) {
	this();
	this.project = dbF;
	initProject();
	initUi();
    }

    /**
     * initBegin the frame
     *
     */
    @Override
    public void init() {
	//LOG.printInfos(TT+"init()");
	//TODO calendar = new SbCalendar();
	viewFactory = new SbViewFactory(this);
	mainActions = new MainActions(this);
	mainActions.init();
	mainMenu = mainActions.getMainMenu();
	ctrl = new Ctrl(this);
	BlankModel blank = new BlankModel(this);
	ctrl.attachModel(blank);
	setIconImage(IconUtil.getIconImage("icon"));
	addWindowListener(new MainFrameWindowAdaptor());
    }

    public void initProject() {
	//LOG.trace(TT + "initProject()");
	try {
	    viewFactory = new SbViewFactory(this);
	    viewFactory.initBegin();
	    mainActions = new MainActions(this);
	    mainActions.init();
	    mainMenu = mainActions.getMainMenu();
	    ctrl = new Ctrl(this);
	    model = new Model(this);
	    ctrl.attachModel(model);
	    addWindowListener(new MainFrameWindowAdaptor());
	    viewFactory.initEnd();
	    setImageDir();
	    App.getInstance().enableCopyEntity();
	} catch (Exception ex) {
	    LOG.err(TT + "initProject(filename=" + project.getFilename() + ")", ex);
	}
    }

    /**
     * set the image directory
     */
    private void setImageDir() {
	// set base summary image
	String imgDir = getImageDir();
	File f = new File(imgDir);
	if (!f.exists()) {
	    if (!f.mkdir()) {
		LOG.err(TT + "setImageDir() error: unable to create \"" + imgDir + "\"");
		return;
	    }
	}
	imgDir = imgDir + File.separator + "summary.png";
	f = new File(imgDir);
	if (!f.exists()) {
	    try {
		IOUtil.resourceCopyTo("icons/png/summary.png", f);
	    } catch (Exception ex) {
		LOG.err("unable to copy summary image to \"" + imgDir + "\"", ex);
	    }
	}
    }

    /**
     * intialize the user interface
     */
    @Override
    public void initUi() {
	//LOG.printInfos(TT+"initUi()");
	if (App.preferences.getBoolean(Pref.KEY.SPELLING_ENABLE)) {
	    ShefEditor.setSpelling(App.preferences.getString(Pref.KEY.SPELLING));
	} else {
	    ShefEditor.setSpelling("none");
	}
	setLayout(
		new MigLayout(MIG.get(MIG.FLOWY, MIG.FILL, MIG.INS0, MIG.GAP0, MIG.HIDEMODE2),
			"", "[grow]"));
	setIconImage(IconUtil.getIconImage("icon"));
	App.fonts.reset();
	setFont(App.fonts.defGet());
	UIManager.put("default", App.fonts.defGet());
	initRootWindow();
	setDefaultLayout();
	add(rootWindow, "grow");
	statusBar = new StatusbarPanel(this);
	add(statusBar, "growx");
	ctrl.attachView(statusBar);
	setTitle();
	restoreDimension();
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	pack();
	setVisible(true);
	initAfterPack();
	JMenuBar menubar = getJMenuBar();
	menubar.setFont(App.fonts.defGet());
	ctrl.detachView(menubar);
	ctrl.attachView(menubar);
	// load last used layout
	if (!DockUtil.layoutLoadFromFile(this, Pref.KEY.LAYOUT_LAST_USED.toString())) {
	    DockUtil.layoutLoadFromResource(this, "default");
	}
	typistSet();
	LOG.trace("end of MainFrame.initUi");
    }

    /**
     * get the current active frame
     *
     * @return
     */
    public JFrame getFullFrame() {
	if (isTypist || isEpisode) {
	    return fullFrame;
	} else {
	    return this;
	}
    }

    /**
     * activate the typist mode
     *
     */
    public void typistActivate() {
	//LOG.printInfos(TT+"activateTipist()=>" + (isTypist ? "true" : "false"));
	if (isTypist) {
	    this.setVisible(true);
	    if (project.book.info.scenarioGet() || isScenario) {
		lastScene = typistScenario.sceneGet();
		typistScenario.sceneClose();
		fullFrame.remove(typistScenario);
		isScenario = false;
	    } else {
		lastScene = typistScene.sceneGet();
		typistScene.sceneClose();
		fullFrame.remove(typistScene);
	    }
	    fullFrame.dispose();
	    isTypist = false;
	    App.preferences.typistSet(false);
	} else {
	    initFullFrame();
	    if (project.book.info.scenarioGet()) {
		if (lastScene == null) {
		    lastScene = (Scene) project.scenes.getList().get(0);
		}
		typistScenario = new TypistScenario(this, lastScene);
		fullFrame.add(typistScenario, "grow");
		isScenario = true;
	    } else {
		typistScene = new TypistPanel(this, lastScene);
		fullFrame.add(typistScene, "grow");
	    }
	    fullFrame.setVisible(true);
	    fullFrame.pack();
	    isTypist = true;
	    App.preferences.typistSet(true);
	    this.setVisible(false);
	}
    }

    /**
     * the mode to typist
     */
    public void typistSet() {
	if (project.isOK()
		&& App.preferences.typistGet()) {
	    typistActivate();
	}
    }

    /**
     * activate Episode mode in full screen
     */
    public void episodeActivate() {
	//LOG.printInfos(TT+"activateTipist()=>" + (isTypist ? "true" : "false"));
	if (isEpisode) {
	    isEpisode = false;
	    fullFrame.remove(episodePanel);
	    fullFrame.dispose();
	} else {
	    isEpisode = true;
	    initFullFrame();
	    episodePanel = new EpisodePanel(this);
	    episodePanel.initAll();
	    fullFrame.add(episodePanel, "grow");
	    fullFrame.setVisible(true);
	    fullFrame.pack();
	    this.setVisible(false);
	}
    }

    private void initFullFrame() {
	fullFrame = new JFrame();
	fullFrame.setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.GAP1, MIG.WRAP1)));
	fullFrame.setIconImage(IconUtil.getIconImage("icon"));
	MainMenu menu = new MainMenu(this);
	menu.setMenuForTypist();
	JPanel pm = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
	pm.add(menu.menuBar);
	pm.setMaximumSize(new Dimension(1, 1));
	fullFrame.add(pm, MIG.SPAN);
	fullFrame.setUndecorated(true);
	fullFrame.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	fullFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * set the title of the frame
     */
    public void setTitle() {
	//LOG.printInfos(TT + "titleSet()");
	if (project != null) {
	    String title = (isUpdated() ? "*" : "") + project.book.getTitle() + (isUpdated() ? "*" : "");
	    setTitle(title + " - " + Const.getFullName());
	} else {
	    setTitle(Const.getFullName());
	}
	if (mainActions != null && mainMenu != null) {
	    mainMenu.enableSave(isUpdated());
	}
	mainMenu.setLinks();
    }

    /**
     * initBegin the root window
     *
     */
    private void initRootWindow() {
	//LOG.printInfos(TT+"initRootWindow()");
	StringViewMap viewMap = viewFactory.getViewMap();
	MixedViewHandler handler = new MixedViewHandler(viewMap, new ViewSerializer() {
	    @Override
	    public void writeView(View view, ObjectOutputStream out) throws IOException {
		out.writeInt(((DynamicView) view).getId());
	    }

	    @Override
	    public View readView(ObjectInputStream in) throws IOException {
		return getDynamicView(in.readInt());
	    }
	});
	rootWindow = DockingUtil.createRootWindow(viewMap, handler, true);
	rootWindow.setName("rootWindow");
	rootWindow.setPreferredSize(new Dimension(4096, 2048));
	// set theme
	DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
	RootWindowProperties properties = new RootWindowProperties();
	properties.addSuperObject(currentTheme.getRootWindowProperties());
	// Our properties object is the super object of the root window
	// properties object, so all property values of the
	// theme and in our property object will be used by the root window
	rootWindow.getRootWindowProperties().addSuperObject(properties);
	rootWindow.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }

    /**
     * set the default layout
     */
    public void setDefaultLayout() {
	//LOG.printInfos(TT+"setDefaultLayout()");
	SbView attributesView = getView(VIEWNAME.ATTRIBUTES);
	SbView categoriesView = getView(VIEWNAME.CATEGORIES);
	SbView chaptersView = getView(VIEWNAME.CHAPTERS);
	SbView endnotesView = getView(VIEWNAME.ENDNOTES);
	SbView episodesView = getView(VIEWNAME.EPISODES);
	SbView eventsView = getView(VIEWNAME.EVENTS);
	SbView gendersView = getView(VIEWNAME.GENDERS);
	SbView ideasView = getView(VIEWNAME.IDEAS);
	SbView internalsView = getView(VIEWNAME.INTERNALS);
	SbView itemsView = getView(VIEWNAME.ITEMS);
	SbView itemLinksView = getView(VIEWNAME.ITEMLINKS);
	SbView locationsView = getView(VIEWNAME.LOCATIONS);
	SbView memosView = getView(VIEWNAME.MEMOS);
	SbView partsView = getView(VIEWNAME.PARTS);
	SbView personsView = getView(VIEWNAME.PERSONS);
	SbView plotView = getView(VIEWNAME.PLOTS);
	SbView relationsView = getView(VIEWNAME.RELATIONS);
	SbView scenesView = getView(VIEWNAME.SCENES);
	SbView strandsView = getView(VIEWNAME.STRANDS);
	SbView tagsView = getView(VIEWNAME.TAGS);
	SbView tagLinksView = getView(VIEWNAME.TAGLINKS);

	SbView attributesListView = getView(VIEWNAME.ATTRIBUTESLIST);
	SbView bookView = getView(VIEWNAME.BOOK);
	SbView chronoView = getView(VIEWNAME.CHRONO);
	SbView storyClassicView = getView(VIEWNAME.STORY_THREE);
	SbView storyFreytagView = getView(VIEWNAME.STORY_FREYTAG);
	SbView storyVoglerView = getView(VIEWNAME.STORY_VOGLER);
	SbView timelineView = getView(VIEWNAME.TIMELINE);
	SbView infoView = getView(VIEWNAME.INFO);
	SbView manageView = getView(VIEWNAME.MANAGE);
	SbView memoriaView = getView(VIEWNAME.MEMORIA);
	//SbView navigationView = getView(VIEWNAME.NAVIGATION);
	SbView planningView = getView(VIEWNAME.PLANNING);
	SbView readingView = getView(VIEWNAME.READING);
	SbView scenarioView = getView(VIEWNAME.SCENARIO);
	SbView storyboard = getView(VIEWNAME.STORYBOARD);
	SbView storymap = getView(VIEWNAME.STORYMAP);
	SbView treeView = getView(VIEWNAME.TREE);
	SbView typistView = getView(VIEWNAME.TYPIST);

	SbView chartPersonsByDate = getView(VIEWNAME.CHART_PERSONS_BY_DATE);
	SbView chartPersonsByScene = getView(VIEWNAME.CHART_PERSONS_BY_SCENE);
	SbView chartWiWW = getView(VIEWNAME.CHART_WIWW);
	SbView chartStrandsByDate = getView(VIEWNAME.CHART_STRANDS_BY_DATE);
	SbView chartOccurrenceOfPersons = getView(VIEWNAME.CHART_OCCURRENCE_OF_PERSONS);
	SbView chartOccurrenceOfLocations = getView(VIEWNAME.CHART_OCCURRENCE_OF_LOCATIONS);
	SbView chartOccurrenceOfItems = getView(VIEWNAME.CHART_OCCURRENCE_OF_ITEMS);

	TabWindow tabInfoNavi = new TabWindow(new SbView[]{infoView/*, navigationView*/});
	tabInfoNavi.setName("tabInfoNaviWindow");
	SplitWindow swTreeInfo = new SplitWindow(false, 0.6f, treeView, tabInfoNavi);
	swTreeInfo.setName("swTreeInfo");
	TabWindow tabWindow;
	tabWindow = new TabWindow(new SbView[]{
	    //les vues
	    attributesListView,
	    chronoView,
	    storyClassicView, storyFreytagView, storyVoglerView,
	    timelineView,
	    bookView,
	    manageView,
	    planningView,
	    readingView,
	    memoriaView,
	    scenarioView,
	    storyboard,
	    storymap,
	    typistView,
	    //les principaux
	    partsView,
	    chaptersView,
	    scenesView,
	    locationsView,
	    personsView,
	    itemsView,
	    //les autres
	    attributesView,
	    categoriesView,
	    endnotesView,
	    episodesView,
	    eventsView,
	    gendersView,
	    ideasView,
	    internalsView,
	    itemLinksView,
	    memosView,
	    relationsView,
	    plotView,
	    strandsView,
	    tagsView,
	    tagLinksView,
	    //les charts
	    chartPersonsByDate,
	    chartPersonsByScene,
	    chartWiWW,
	    chartStrandsByDate,
	    chartOccurrenceOfPersons,
	    chartOccurrenceOfItems,
	    chartOccurrenceOfLocations,});
	tabWindow.setName("tabWindow");
	SplitWindow swMain = new SplitWindow(true, 0.20f, swTreeInfo, tabWindow);
	swMain.setName("swMain");
	rootWindow.setWindow(swMain);
	//close all views
	attributesListView.close();
	chronoView.close();
	storyClassicView.close();
	storyFreytagView.close();
	storyVoglerView.close();
	timelineView.close();
	infoView.restoreFocus();
	manageView.close();
	memoriaView.close();
	planningView.close();
	readingView.close();
	scenarioView.close();
	storyboard.close();
	storymap.close();
	typistView.close();
	//close all tables
	attributesView.close();
	categoriesView.close();
	chaptersView.close();
	endnotesView.close();
	episodesView.close();
	eventsView.close();
	gendersView.close();
	ideasView.close();
	internalsView.close();
	itemsView.close();
	itemLinksView.close();
	memosView.close();
	partsView.close();
	plotView.close();
	relationsView.close();
	strandsView.close();
	tagsView.close();
	tagLinksView.close();
	//close all charts
	chartPersonsByDate.close();
	chartPersonsByScene.close();
	chartWiWW.close();
	chartStrandsByDate.close();
	chartOccurrenceOfPersons.close();
	chartOccurrenceOfItems.close();
	chartOccurrenceOfLocations.close();
	//restore the focus to the book view
	bookView.restoreFocus();
	rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
	DockUtil.setRespectMinimumSize(this);
    }

    /**
     * initBegin after pack
     */
    private void initAfterPack() {
	SbView scenesView = getView(VIEWNAME.SCENES);
	SbView locationsView = getView(VIEWNAME.LOCATIONS);
	SbView personsView = getView(VIEWNAME.PERSONS);
	SbView bookView = getView(VIEWNAME.BOOK);
	SbView treeView = getView(VIEWNAME.TREE);
	SbView quickInfoView = getView(VIEWNAME.INFO);
	//SbView navigationView = getView(VIEWNAME.NAVIGATION);
	// add docking window adapter to all views (except editor)
	MainDockingWindowAdapter dockingAdapter = new MainDockingWindowAdapter();
	for (int i = 0; i < viewFactory.getViewMap().getViewCount(); ++i) {
	    View view = viewFactory.getViewMap().getViewAtIndex(i);
	    view.addListener(dockingAdapter);
	}
	// load initially shown views here
	SbView[] views2 = {scenesView,
	    personsView, locationsView,
	    bookView, treeView, quickInfoView/*, navigationView*/};
	for (SbView view : views2) {
	    if (!viewFactory.loadView(view)) {
		continue;
	    }
	    ctrl.attachView(view.getComponent());
	    if (view.isLoaded()) {
		model.fireAgain(view);
	    }
	}
	quickInfoView.restoreFocus();
	bookView.restoreFocus();
    }

    /**
     * get a given view by entity type
     *
     * @param entity
     * @return
     */
    public SbView getView(AbstractEntity entity) {
	return viewFactory.getView(entity);
    }

    /**
     * get the view by its name
     *
     * @param viewName
     * @return
     */
    public SbView getView(String viewName) {
	return viewFactory.getView(viewName);
    }

    /**
     * get the view by view type
     *
     * @param viewType
     * @return
     */
    public SbView getView(VIEWNAME viewType) {
	return viewFactory.getView(viewType);
    }

    /**
     * show the given view
     *
     * @param viewName
     */
    public void showView(VIEWNAME viewName) {
	//LOG.printInfos(TT+"showView(" + viewName.name() + ")");
	cursorSetWaiting();
	SbView view = getView(viewName);
	if (view.getRootWindow() != null) {
	    view.restoreFocus();
	} else {

	    DockingUtil.addWindow(view, rootWindow);
	}
	view.requestFocusInWindow();
	DockUtil.setRespectMinimumSize(this);
	cursorSetDefault();
    }

    /**
     * show and focus the given view
     *
     * @param viewName
     */
    public void showAndFocus(VIEWNAME viewName) {
	//LOG.printInfos(TT+"showAndFocus(" + viewName.name() + ")");
	System.gc();
	SbView view = getView(viewName);
	view.restore();
	view.restoreFocus();
    }

    /**
     * close the given view
     *
     * @param viewName
     */
    public void closeView(VIEWNAME viewName) {
	//LOG.printInfos(TT + "closeView(" + viewName.name() + ")");
	SbView view = getView(viewName);
	view.close();
    }

    /**
     * refresh the frame
     */
    public void refresh() {
	//LOG.printInfos(TT + "refresh()");
	cursorSetWaiting();
	getBookModel().fireAgain();
	/*for (int i = 0; i < viewFactory.getViewMap().getViewCount(); ++i) {
			SbView view = (SbView) viewFactory.getViewMap().getViewAtIndex(i);
			viewFactory.setViewTitle(view);
			getBookController().refresh(view);
		}*/
	cursorSetDefault();
    }

    /**
     * refresh the status bar
     */
    public void refreshStatusBar() {
	//LOG.printInfos(TT + "refreshStatusBar()");
	if (statusBar != null) {
	    statusBar.refresh();
	}
    }

    /**
     * initBegin an empty frame
     */
    public void initBlankUi() {
	project = null;
	setTitle(Const.getFullName());
	Dimension screen = SwingUtil.getScreenSize();
	setLocation(screen.width / 2 - 450, screen.height / 2 - 320);
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	App.fonts.reset();
	mainMenu.reloadToolbar();
	BlankPanel blankPanel = new BlankPanel(this);
	blankPanel.initAll();
	add(blankPanel);
	pack();
	setVisible(true);
    }

    /**
     * set curosr to default
     */
    public void cursorSetDefault() {
	SwingUtil.setDefaultCursor(this);
    }

    /**
     * set a waiting cursor
     */
    public void cursorSetWaiting() {
	SwingUtil.setWaitingCursor(this);
    }

    /**
     * get the current Project
     *
     * @return
     */
    public Project getProject() {
	return project;
    }

    /**
     * restore for a blank frame
     *
     * @return
     */
    public boolean isBlank() {
	return project == null;
    }

    /**
     * get the Book controler
     *
     * @return
     */
    public Ctrl getBookController() {
	return ctrl;
    }

    /**
     * get the Book model
     *
     * @return
     */
    public Model getBookModel() {
	return model;
    }

    /**
     * get the root window
     *
     * @return
     */
    public RootWindow getRootWindow() {
	return rootWindow;
    }

    /**
     * get the action manager
     *
     * @return
     */
    public MainActions getMainActions() {
	return mainActions;
    }

    /**
     * get the view factory
     *
     * @return
     */
    public SbViewFactory getViewFactory() {
	return viewFactory;
    }

    /**
     * get this instance
     *
     * @return
     */
    private MainFrame getThis() {
	return this;
    }

    /**
     * restore if the frame is maximized
     *
     * @return
     */
    public boolean isMaximized() {
	return (getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
    }

    /**
     * set the frame to be maximized
     */
    public void setMaximized() {
	setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    /**
     * close the frame
     *
     * @param forceExit
     */
    public void close(boolean forceExit) {
	//LOG.printInfos(TT + "close(forceExit=" + (forceExit ? "true" : "false") + ")");
	App app = App.getInstance();
	if (!isBlank()) {
	    if (askForSave() == 0) {
		return;
	    }
	    if (App.preferences.getBoolean(Pref.KEY.CONFIRM_EXIT, true)) {
		int r = JOptionPane.showConfirmDialog(getThis(),
			I18N.getMsg("ask.close"),
			I18N.getMsg("close"),
			JOptionPane.YES_NO_OPTION);
		if (r == JOptionPane.NO_OPTION || r == JOptionPane.CLOSED_OPTION) {
		    return;
		}
	    }
	    viewFactory.saveAllTableDesign();
	    saveContext();
	    if (project != null) {
		app.recentfilesUpdate(project);
	    }
	} else if (this.getProject() != null) {
	    saveContext();
	    app.recentfilesUpdate(this.getProject());
	}
	if (model != null) {
	    model.closeSession();
	}
	app.removeMainFrame(this);
	dispose();
	if (app.getMainFrames().isEmpty()) {
	    if (forceExit) {
		app.exit();
	    } else { //re-create blank
		MainFrame mainFrame = new MainFrame();
		mainFrame.init();
		mainFrame.initBlankUi();
		app.addMainFrame(mainFrame);
	    }
	}
    }

    /**
     * get the view by its id
     *
     * @param id
     * @return
     */
    private View getDynamicView(int id) {
	View view = (View) dynamicViews.get(id);
	if (view == null) {
	    view = new DynamicView("Dynamic View " + id, null, createDummyViewComponent("Dynamic View " + id), id);
	}
	return view;
    }

    /**
     * create a dummy view
     *
     * @param text
     * @return
     */
    private static JComponent createDummyViewComponent(String text) {
	StringBuilder sb = new StringBuilder();
	for (int j = 0; j < 100; j++) {
	    sb.append(text).append(". This is line ").append(j).append("\n");
	}
	return new JScrollPane(new JTextArea(sb.toString()));
    }

    /**
     * restore the last used dimension
     */
    private void restoreDimension() {
	Dimension dim = App.preferences.getDimension(Pref.KEY.WINDOW_SIZE);
	int w = dim.width, h = dim.height;
	Dimension screen = SwingUtil.getScreenSize();
	if (w > screen.width) {
	    h = screen.width;
	}
	if (h > screen.height) {
	    h = screen.height;
	}
	setPreferredSize(dim);

	Point pos = App.preferences.getPoint(Pref.KEY.WINDOW_POS);
	int x = pos.x;
	int y = pos.y;
	if (x > w) {
	    x = 0;
	}
	if (y > h) {
	    y = 0;
	}
	setLocation(x, y);

	boolean maximized = App.preferences.getBoolean(Pref.KEY.WINDOW_MAXIMIZED, false);
	if (maximized) {
	    setMaximized();
	}
    }

    /**
     * get the App preferences
     *
     * @return
     */
    public Pref getPref() {
	return App.preferences;
    }

    /**
     * set last used chapter
     *
     * @param c
     */
    public void lastChapterSet(Chapter c) {
	lastChapter = c;
    }

    public String imageCopy(String src) {
	//LOG.printInfos(TT + "imageCopy(src='" + src + "')");
	if (src.startsWith("file://")) {
	    File f = new File(src.replace("file://", ""));
	    File imgF = new File(getImageDir() + File.separator + f.getName());
	    if (!imgF.exists()) {
		if (IOUtil.fileCopy(f, imgF)) {
		    return imgF.getAbsolutePath();
		}
	    }
	}
	return src;
    }

    /**
     * get last used chapter
     *
     * @return
     */
    public Chapter lastChapterGet() {
	return lastChapter;
    }

    /**
     * change the file path in the scenes text
     *
     * @param oldPath
     * @param newPath
     */
    public void changePath(String oldPath, String newPath) {
	//LOG.printInfos(TT+"changePath(" + oldPath + ", " + newPath + ")");
	if (oldPath.equals(newPath)) {
	    return;
	}
	@SuppressWarnings("unchecked")
	List<Scene> scenes = project.scenes.getList();
	for (Scene scene : scenes) {
	    String text = scene.getSummary();
	    if (scene.getSummary().contains(oldPath)) {
		text = text.replace(oldPath, newPath);
		scene.setSummary(text);
		this.getBookController().updateEntity(scene);
	    }
	}
    }

    /**
     * set frame status to be modified
     */
    public void setUpdated() {
	//LOG.trace(TT + "setUpdated()");
	setUpdated(true);
    }

    /**
     * set the modified status of the frame
     *
     * @param b
     */
    public void setUpdated(boolean b) {
	//LOG.trace(TT + "setUpdated(b=" + (b ? "true" : "false") + ")");
	modified = b;
	setTitle();
    }

    /**
     * restore if the frame was modified
     *
     * @return
     */
    public boolean isUpdated() {
	return modified;
    }

    /**
     * set the last used Scene
     *
     * @param scene
     */
    public void lastSceneSet(Scene scene) {
	if (scene.hasChapter()) {
	    lastChapter = scene.getChapter();
	}
	lastScene = scene;
    }

    /**
     * get the last used Scene
     *
     * @return
     */
    public Scene lastSceneGet() {
	if (lastScene == null) {
	    return ((Scene) project.getList(Book.TYPE.SCENE).get(0));
	}
	return lastScene;
    }

    /**
     * get the image directory
     *
     * @return
     */
    public String getImageDir() {
	return project.getPath() + File.separator + "Images";
    }

    /**
     * set the image directory
     *
     * @param directory
     */
    public void setImageDir(String directory) {
	project.setImageDir(directory);
    }

    /**
     * toXml the context of the frame (dimension, location, maximized)
     */
    public void saveContext() {
	if (this.isVisible()) {
	    Dimension dim = this.getSize();
	    dim.width += 10;//because MigLayout reduce this by 10
	    dim.height -= 2;//because MigLayout increase this by 2
	    App.preferences.setDimension(Pref.KEY.WINDOW_SIZE, dim);
	    App.preferences.setPoint(Pref.KEY.WINDOW_POS, getLocationOnScreen());
	    App.preferences.setBoolean(Pref.KEY.WINDOW_MAXIMIZED, isMaximized());
	    String s = "North";
	    if (mainToolBar.getOrientation() == 0) {
		if (mainToolBar.getY() != 0) {
		    s = "South";
		}
	    } else if (mainToolBar.getX() != 0) {
		s = "East";
	    } else {
		s = "West";
	    }
	    App.preferences.setString(Pref.KEY.TOOLBAR_ORIENTATION, s);
	    DockUtil.layoutSave(this, Pref.KEY.LAYOUT_LAST_USED.toString());
	    App.preferences.save();
	}
    }

    /**
     * toXml the current file
     *
     * @param force: true toXml even if not modified
     */
    public void fileSave(boolean force) {
	//LOG.printInfos(TT+"fileSave()");
	if (!force && !isUpdated()) {
	    return;
	}
	if (project != null) {
	    viewFactory.saveAllTableDesign();
	    project.setTypist(isTypist);
	    boolean rc = project.save();
	    if (rc) {
		App.getInstance().recentfilesUpdate(project);
	    }
	}
	modified = false;
	setTitle();
    }

    public void fileSaveAs() {
	ChooseFileDlg dlg = new ChooseFileDlg(this, false);
	dlg.setForceDbExtension(false);
	dlg.setDefaultDBExt(Const.STORYBOOK.FILE_EXT_OSBK.toString());
	dlg.setDefaultPath(getProject().getPath());
	dlg.setVisible(true);
	if (dlg.isCanceled()) {
	    return;
	}
	File outFile = dlg.getFile();
	File inFile = getProject().getFile();
	String oldPath = getProject().getPath();
	close(false);
	if (!IOUtil.fileCopy(inFile, outFile)) {
	    LOG.err("FileUtils.copyFile(?,?) IOex : ");
	}
	Project newDB = new Project(outFile);
	String newPath = newDB.getPath();
	App.getInstance().openProject(newDB, oldPath, newPath);
    }

    public void fileRename() {
	//App.printInfos("MainAction.fileRename()");
	String m = JOptionPane.showInputDialog(this,
		I18N.getMsg("project.rename.new.name"),
		I18N.getMsg("project.rename"),
		QUESTION_MESSAGE);
	if (m != null && !m.isEmpty()) {
	    m = getProject().getPath() + File.separator + m + ".osbk";
	    File outFile = new File(m);
	    if (outFile.exists()) {
		int ret = JOptionPane.showConfirmDialog(this,
			I18N.getMsg("file.exists", m),
			I18N.getMsg("file.save.overwrite.title"),
			JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.NO_OPTION) {
		    return;
		}
	    }
	    App.getInstance().renameFile(this, outFile);
	}
    }

    public void fileImportDoc() {
	//LOG.printInfos(TT + "fileImportDoc()");
	ImportDocument doc = new ImportDocument(this);
	File file = doc.selectFile();
	if (file != null) {
	    doc.setFile(file);
	    if (IOUtil.getExtension(file).equals("epub")) {
		ImportEpub.doImport(file);
		return;
	    }
	    if (doc.openDocument() && doc.openDB()) {
		cursorSetWaiting();
		App.getInstance().importBook(doc);
		doc.close();
		cursorSetDefault();
	    }
	}
    }

    /**
     * reload the Book informations
     */
    public void reloadBook() {
	book = project.book;
	SbView view = getView(VIEWNAME.READING);
	if (view != null && view.isLoaded()) {
	    getBookModel().fireAgain(view);
	}
    }

    /**
     * get the current Book
     *
     * @return
     */
    public Book getBook() {
	if (project == null) {
	    return null;
	}
	return project.book;
    }

    /**
     * get last used Part
     *
     * @return
     */
    public Part lastPartGet() {
	return lastPart;
    }

    /**
     * set last used Part
     *
     * @param part
     */
    public void lastPartSet(Part part) {
	this.lastPart = part;
    }

    /**
     * paste clipboard as a Part
     */
    public void pasteAs() {
	//LOG.printInfos(TT + "pasteAsPart()");
	String content = getClipboardContents();
	if (content.contains("<?xml")) {
	    // xml content
	    pasteAsEntity(content);
	} else {
	    // show popup menu for pasted text
	    content = "<p>" + content + "</p>";
	    content = content.replace("<p><p>", "<p>").replace("</p></p>", "</p>");
	    JPopupMenu menu = new JPopupMenu();
	    final String ptxt = content;
	    menu.add(MainMenu.initMenuItem("part", evt -> pasteAsPart(ptxt)));
	    menu.add(MainMenu.initMenuItem("chapter", evt -> pasteAsChapter(ptxt)));
	    menu.add(MainMenu.initMenuItem("scene", evt -> pasteAsScene(ptxt)));
	    menu.show(this, getMousePosition().x, getMousePosition().y);
	}
    }

    public void pasteAsEntity(String content) {
	Element rootNode = Xml.getRootNode(Xml.intoXml(content));
	NodeList nodes = rootNode.getChildNodes();
	Element node = (Element) nodes.item(0);
	if (EntityUtil.fromXml(this, node)) {

	}
    }

    /**
     * paste clipboard as a Part
     *
     * @param content
     */
    public void pasteAsPart(String content) {
	//LOG.printInfos(TT + "pasteAsPart()");
	Part c = new Part();
	c.setDescription(getClipboardContents());
	showEditorAsDialog(c);
    }

    /**
     * paste clipboard as a Chapter
     *
     * @param content
     */
    public void pasteAsChapter(String content) {
	//LOG.printInfos(TT + "pasteAsChapter()");
	Chapter c = new Chapter();
	c.setDescription(content);
	showEditorAsDialog(c);
    }

    /**
     * paste clipboard as a Scene
     *
     * @param content
     */
    public void pasteAsScene(String content) {
	//LOG.printInfos(TT + "pasteAsScene()");
	Scene c = new Scene();
	c.setSummary(content);
	showEditorAsDialog(c);
    }

    /**
     * set the last used date in file
     *
     * @param date
     */
    public void lastDateSet(Date date) {
	lastSceneDate = date;
    }

    /**
     * get the last used date from file
     *
     * @return
     */
    public Date lastDateGet() {
	return lastSceneDate;
    }

    public int askForSave() {
	//LOG.printInfos(TT + "askForSave() modified=" + (isUpdated() ? "true" : "false"));
	if (isUpdated()) {
	    if (isTypist) {
		if (isScenario) {
		    if (typistScenario.askModified() != JOptionPane.YES_OPTION) {
			return 0;
		    }
		    typistScenario.sceneClose();
		} else {
		    if (typistScene.askModified() != JOptionPane.YES_OPTION) {
			return 0;
		    }
		    typistScene.sceneClose();
		}
	    }
	    //check if data are modified
	    Object[] choix = {
		I18N.getMsg("cancel"),
		I18N.getMsg("file.save"),
		I18N.getMsg("ignore")
	    };
	    int n = JOptionPane.showOptionDialog(this,
		    I18N.getMsg("close.confirm"),
		    I18N.getMsg("close"),
		    JOptionPane.YES_NO_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null, choix, choix[0]);
	    if (n == 0) {
		return 0;
	    }
	    if (n == 1) {
		boolean rc = project.save();
		if (rc) {
		    modified = false;
		}
	    }
	    return n;
	}
	return 2;
    }

    public void projectArchive() {
	if (askForSave() == 0) {
	    return;
	}
	if (project.archive()) {
	    MessageDlg.show(this, I18N.getMsg("project.archive_ok"), "project.archive", true);
	}
    }

    /**
     * class for dynamic view
     */
    private static class DynamicView extends View {

	private final int id;

	DynamicView(String title, Icon icon, Component component, int id) {
	    super(title, icon, component);
	    this.id = id;
	}

	public int getId() {
	    return id;
	}
    }

    /**
     * class for the window adaptor of the frame
     *
     */
    private class MainFrameWindowAdaptor extends WindowAdapter {

	@Override
	public void windowClosing(WindowEvent evt) {
	    //LOG.printInfos("MainFrameWindowAdaptor.windowClosing(evt=" + evt.toString() + ")");
	    if (askForSave() != 0) {
		modified = false;
		close(true);
	    }
	}
    }

    /**
     * clas for docking window
     */
    private class MainDockingWindowAdapter extends DockingWindowAdapter {

	@Override
	@SuppressWarnings("null")
	public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
	    //LOG.printInfos("MainDockingWindowAdapter.windowAdded(" + addedToWindow.getName() + ", " + addedWindow.getName() + ")");
	    if (addedWindow instanceof SbView) {
		SbView view = (SbView) addedWindow;
		if (!view.isLoaded() && viewFactory.loadView(view)) {
		    ctrl.attachView(view.getComponent());
		    model.fireAgain(view);
		}
	    }
	}

	@Override
	@SuppressWarnings("null")
	public void windowClosed(DockingWindow window) {
	    //LOG.printInfos("MainDockingWindowAdapter.windowClosed(" + window.getName() + ")");
	    if (window instanceof SbView) {
		SbView view = (SbView) window;
		if (!view.isLoaded()) {
		    return;
		}
		ctrl.detachView((AbstractPanel) view.getComponent());
		viewFactory.unloadView(view);
	    }
	}
    }

    /**
     * set the tool bar of the frame
     *
     * @param toolBar
     */
    public void setMainToolBar(JToolBar toolBar) {
	if (mainToolBar != null) {
	    getContentPane().remove(mainToolBar);
	}
	this.mainToolBar = toolBar;
	String bl = App.preferences.getString(Pref.KEY.TOOLBAR_ORIENTATION, "North");
	if ("East".equals(bl) || "West".equals(bl)) {
	    mainToolBar.setOrientation(1);
	}
	getContentPane().add(mainToolBar, bl);
    }

    /**
     * get the tool bar of the frame
     *
     * @return
     */
    public JToolBar getMainToolBar() {
	return mainToolBar;
    }

    public MainMenu getMainMenu() {
	return mainMenu;
    }

    /**
     * show the editor, not usable
     */
    public void showEditor() {
	//LOG.printInfos(TT+"showEditor()");
	/*SwingUtilities.invokeLater(new Runnable() {
		 @Override
		 public void run() {
		 LOG.printInfos(TT+"showEditor()-->run");
		 SbView editorView = getView(VIEWNAME.EDITOR);
		 editorView.cleverRestoreFocus();
		 }
		 });*/
    }

    /**
     * call the dialog editor
     *
     * @param entity
     * @param bt: optional, locate the dialog upon a button
     *
     * @return true if dialog was canceled
     */
    public boolean showEditorAsDialog(AbstractEntity entity, JButton... bt) {
	/*LOG.trace(TT + "showEditorAsDialog(entity=" + LOG.trace(entity)
				+ (bt == null ? "" : ", upon bt")
				+ ")");*/
	JDialog dlg;
	if (isTypist) {
	    dlg = new JDialog(getFullFrame(), true);
	} else {
	    dlg = new JDialog((Frame) this, true);
	}
	editorModless = App.preferences.editorGetModless();
	if (editorModless) {
	    dlg.setModalityType(Dialog.ModalityType.MODELESS);
	    dlg.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	Editor editor = new Editor(this, entity, dlg);
	dlg.add(editor);
	dlg.setTitle(I18N.getMsg("editor"));
	dlg.pack();
	Dimension pos = SwingUtil.getDlgPosition(entity);
	Dimension size = SwingUtil.getDlgSize(entity);
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
	if (bt != null && bt.length > 0) {
	    Dimension btSize = bt[0].getSize();
	    Point loc = dlg.getLocation();
	    loc.x += btSize.height;
	    loc.y += btSize.width;
	    dlg.setLocation(loc);
	}
	dlg.setVisible(true);
	TempUtil.remove(this, entity, true);
	SwingUtil.saveDlgPosition(dlg, entity);
	return editor.canceled;
    }

    public void showXeditor(AbstractEntity entity) {
	//LOG.trace(TT + "actionPerformed(...) entity=" + entity.toString());
	String name = XEditorFile.getFilePath(this, (Scene) entity);
	if (name == null || name.isEmpty()) {
	    name = XEditorFile.getDefaultFilePath(this, entity.getName());
	}
	if (name.isEmpty()) {
	    return;
	}
	File file = new File(name);
	if (!file.exists()) {
	    file = XEditorFile.createFile(this, name);
	    if (file == null) {
		return;
	    }
	    ((Scene) entity).setOdf(name);
	}
	XEditorFile.launchExternal(file.getAbsolutePath());
    }

    /**
     * do a backup of the current file
     *
     * @param isManual
     */
    public void backupNew(boolean isManual) {
	String backupDir = App.preferences.backupGetDir();
	//if backup_directory is empty ask for a directory
	if (isManual && backupDir.isEmpty()) {
	    File b = IOUtil.directorySelect(null, "");
	    if (b == null) {
		return;
	    }
	    backupDir = b.getAbsolutePath();
	}
	if (backupDir.isEmpty()) {
	    LOG.err("no backup directory, backup failed or canceled");
	    return;
	}
	String backupFilename = backupDir + File.separator + project.getName();
	//construct backup_filename with properties backup_increment
	if (App.preferences.backupGetIncrement()) {
	    backupFilename += EnvUtil.getDateString();
	}
	File file = new File(backupFilename + ".backup");
	if (file.exists() && isManual
		&& JOptionPane.showConfirmDialog(this,
			I18N.getMsg("file.backup_askreplace", file.getAbsolutePath()),
			I18N.getMsg("backup"),
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
	    return;
	}
	project.doBackup(backupFilename, App.preferences.backupGetIncrement());
    }

    /**
     * restore a backup file
     */
    public void backupRest() {
	File backup = IOUtil.fileSelect(null, "", "backup", "Backup file (*.backup)", "file.select");
	if (backup == null) {
	    return;
	}
	//select the directory to restore to
	if (project == null) {
	    File dir = IOUtil.directorySelect(null, "");
	    if (dir == null) {
		return;
	    }
	    close(false);
	    project.doRestore(dir, backup);
	} else {
	    close(false);
	    project.doRestore(backup);
	}
	App.getInstance().openProject(project);
    }

    /**
     * create a new entity
     *
     * @param entity
     */
    public void newEntity(AbstractEntity entity) {
	//LOG.tace(TT+"newEntity(" + entity.getClass().getName() + ")");
	showEditorAsDialog(entity);
    }

    /**
     * toXml the layout of the current window
     */
    public void windowLayoutSave() {
	String name;
	while (true) {
	    name = JOptionPane.showInputDialog(this,
		    I18N.getColonMsg("enter.name"),
		    I18N.getMsg("docking.save.layout"),
		    JOptionPane.PLAIN_MESSAGE);
	    File f = new File(EnvUtil.getPrefDir().getAbsolutePath()
		    + File.separator + name + ".layout");
	    if (f.exists()) {
		int ret = JOptionPane.showConfirmDialog(this,
			I18N.getMsg("warning") + ":" + I18N.getMsg("docking.save.layout.exists"),
			I18N.getMsg("docking.layout"),
			JOptionPane.YES_NO_OPTION);
		switch (ret) {
		    case JOptionPane.OK_OPTION:
			break;
		    case JOptionPane.NO_OPTION:
			name = "";
			break;
		    default:
			return;
		}
		if (!name.isEmpty()) {
		    break;
		}
	    } else {
		break;
	    }
	}
	DockUtil.layoutSave(this, name);
    }

    /**
     * get the clipboard contents as a String
     *
     * @return empty String if nothing
     *
     */
    @SuppressWarnings("null")
    public String getClipboardContents() {
	String result = "";
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	//odd: the Object param of getContents is not currently used
	Transferable contents = clipboard.getContents(null);
	boolean hasTransferableText
		= (contents != null)
		&& contents.isDataFlavorSupported(DataFlavor.stringFlavor);
	if (hasTransferableText) {
	    try {
		result = (String) contents.getTransferData(DataFlavor.stringFlavor);
	    } catch (UnsupportedFlavorException | IOException ex) {
		LOG.err("getClipboard error", ex);
	    }
	}
	return result;
    }

}
