/*
oStorybook: Open Source software for novelists and authors.
initial conception Martin Mustun, adaptation FaVdB

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
package storybook;

import assistant.Assistant;
import i18n.I18N;
import java.awt.Component;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import resources.icons.IconUtil;
import storybook.Const.STORYBOOK;
import storybook.db.book.BookUtil;
import storybook.dialog.ConfirmDlg;
import storybook.dialog.ExceptionDlg;
import storybook.dialog.FirstStartDlg;
import storybook.exim.importer.ImportDocument;
import storybook.ideabox.IdeaxFrm;
import storybook.project.Project;
import storybook.project.ProjectNewDlg;
import storybook.project.PropertiesDlg;
import storybook.shortcut.Shortcuts;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.StringUtil;
import storybook.tools.file.EnvUtil;
import static storybook.tools.file.EnvUtil.getHomeDir;
import storybook.tools.file.IOUtil;
import storybook.tools.net.Updater;
import storybook.tools.spell.SpellUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.splash.Waiting;
import storybook.tools.synonyms.Synonyms;
import storybook.ui.MainFrame;

/**
 * class for the application
 *
 * @author favdb
 */
public class App extends Component {

	private static final String TT = "App.";

	private static App instance;
	public static Pref preferences;
	public static AppFont fonts;
	private final List<MainFrame> mainFrames;
	private static String i18nFile = "";
	private Assistant assistant;
	private static boolean dev = false, test = false;
	private static File fileToOpen = null;
	private static boolean startIdeabox = false;

	/**
	 * getting if dev mode
	 *
	 * @return
	 */
	public static boolean isDev() {
		return dev;
	}

	/**
	 * set the mode to dev
	 */
	public static void setDev() {
		dev = true;
	}

	/**
	 * getting if test mode
	 *
	 * @return
	 */
	public static boolean isTest() {
		return test;
	}

	/**
	 * set the mode to test
	 */
	public static void setTest() {
		test = true;
	}

	/**
	 * get the instance of App
	 *
	 * @return
	 */
	public static App getInstance() {
		if (instance == null) {
			instance = new App();
		}
		return instance;
	}

	/**
	 * initialize preferences
	 */
	public static void initPref() {
		LOG.trace(TT + "initPref()");
		preferences = new Pref();
		if (preferences.getBoolean(Pref.KEY.FIRST_START, true)) {
			preferences.initFont();
		}
	}

	/**
	 * the main process
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		//LOG.log("Starting main");
		initArgs(args);
		initPref();
		LaF.init();
		fonts = new AppFont();//initialize Fonts
		String tempDir = System.getProperty("java.io.tmpdir");
		String fn = tempDir + File.separator + Const.getName() + Const.getVersion() + ".lck";
		if (!lockInstance(fn)) {
			Object[] options = {I18N.getMsg("running.remove"), I18N.getMsg("cancel")};
			int n = JOptionPane.showOptionDialog(null,
					I18N.getMsg("running.msg"),
					I18N.getMsg("running.title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 1) {
				File file = new File(fn);
				if (file.exists() && file.canWrite() && !file.delete()) {
					JOptionPane.showMessageDialog(null, "Delete failed",
							"File\n" + file.getAbsolutePath() + "\ncould not be deleted.",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			return;
		}
		dictaphoneInit();
		SwingUtilities.invokeLater(() -> {
			App app = App.getInstance();
			app.initI18N();
			app.initDefault();
			app.initFile(fileToOpen);
			if (startIdeabox) {
				app.ideaboxStart();
			}
		});
	}

	/**
	 * initialize for arguments
	 *
	 * @param args
	 */
	private static void initArgs(String[] args) {
		String vargs = String.join(" ", args);
		if (vargs.length() == 0) {
			vargs = "no option";
		}
		LOG.log(Const.getFullName() + " starting with: " + vargs);
		List<String> mode = new ArrayList<>();
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				switch (args[i].toLowerCase()) {
					case "--trace":
						mode.add("trace");
						LOG.setTrace();
						break;
					case "--dev":
						mode.add("dev");
						App.dev = true;
						break;
					case "--msg":
						i++;
						String fileI18N = args[i];
						File f = new File(args[i]);
						if (!f.exists()) {
							fileI18N = args[i] + ".properties";
							f = new File(fileI18N);
							if (!f.exists()) {
								LOG.log("Message file not exists : " + fileI18N);
								fileI18N = "";
							}
						}
						if (!fileI18N.isEmpty()) {
							App.i18nFile = fileI18N;
							LOG.log("Message from : " + fileI18N);
							I18N.setFileMessages(i18nFile);
						}
						break;
					case "--ideabox":
						startIdeabox = true;
						break;
					default:
						//perhaps the file to open
						fileToOpen = new File(args[i]);
						if (!fileToOpen.exists()
								|| (!fileToOpen.getAbsolutePath().endsWith(".osbk")
								&& !fileToOpen.getAbsolutePath().endsWith(".xml"))) {
							fileToOpen = null;
						}
						break;
				}
			}
			if (!mode.isEmpty()) {
				LOG.trace("Set mode to: " + ListUtil.join(mode, ", "));
			}
		}
	}

	/**
	 * the App class
	 */
	private App() {
		mainFrames = new ArrayList<>();
	}

	/**
	 * default initialization
	 */
	private void initDefault() {
		LOG.trace(TT + "initDefault()");
		Shortcuts.init();
		assistant = new Assistant();
		String s = preferences.getString(Pref.KEY.ASSISTANT);
		if (!s.isEmpty()) {
			setAssistant(s);
		}
		fonts.defRestore();
		IconUtil.setDefSize();
		String spelling = preferences.getString(Pref.KEY.SPELLING, "none");
		String lang = "";
		if (!spelling.equals("none")) {
			SpellUtil.registerDictionaries();
			lang = spelling.substring(0, 2);
		}
		Synonyms.init(getHomeDir().getAbsolutePath()
				+ File.separator + ".storybook5/dicts/", lang);
	}

	/**
	 * initialization for a given file to open
	 *
	 * @param toOpen
	 */
	private void initFile(File toOpen) {
		//LOG.trace(TT + "initFile(toOpen=" + (toOpen != null ? toOpen.toString() : "null") + ")");
		try {
			// first start dialog
			if (preferences.getBoolean(Pref.KEY.FIRST_START, true)) {
				LOG.log("open First Start Dialog");
				FirstStartDlg dlg = new FirstStartDlg();
				dlg.setVisible(true);
				LOG.log("setting preferences, font size and icon size");
				preferences.setBoolean(Pref.KEY.FIRST_START, false);
				fonts.defRestore();
				IconUtil.setDefSize();
			}

			Project dbFile = null;
			if (toOpen != null) {
				LOG.trace("trying to open " + toOpen.getAbsolutePath());
				dbFile = new Project(toOpen);
			} else if (preferences.getBoolean(Pref.KEY.OPEN_LASTFILE, false)
					&& !preferences.getString(Pref.KEY.LASTOPEN_FILE).isEmpty()) {
				String str = preferences.getString(Pref.KEY.LASTOPEN_FILE);
				LOG.trace("loading last file... " + str);
				dbFile = new Project(new File(str));
			}
			if (dbFile == null || !dbFile.isOK()) {
				preferences.removeString(Pref.KEY.LASTOPEN_FILE);
				preferences.save();
				MainFrame mainFrame = new MainFrame();
				mainFrame.init();
				mainFrame.initBlankUi();
				addMainFrame(mainFrame);
			} else {
				this.openProject(dbFile);
			}
			// check for updates
			Updater.checkForUpdate(false);
		} catch (Exception e) {
			ExceptionDlg.show(TT + "init(...) error", e);
		}
	}

	/**
	 * initialize the I18N with preferences
	 */
	public void initI18N() {
		//LOG.trace(TT + "initI18N()");
		Locale locale = Locale.getDefault();
		try {
			String localeStr = preferences.getString(Pref.KEY.LANGUAGE, "en_US");
			String language[] = localeStr.split("_");
			locale = new Locale(language[0], language[1]);
			setLocale(locale);
		} catch (Exception ex) {
			LOG.err("unable to find Locale in oStorybook.ini");
		}
		try {
			String f = preferences.getString(Pref.KEY.MSGFILE, "");
			if (!f.isEmpty()) {
				I18N.setFileMessages(f);
			}
		} catch (Exception ex) {
		}
		I18N.initMessages(locale);
	}

	/**
	 * get the assistant
	 *
	 * @return
	 */
	public static Assistant getAssistant() {
		return getInstance().assistant;
	}

	/**
	 * set the assistant to the given file name
	 *
	 * @param nf
	 */
	public void setAssistant(String nf) {
		assistant = new Assistant(nf);
	}

	/**
	 * get all current opened MainFrame
	 *
	 * @return
	 */
	public List<MainFrame> getMainFrames() {
		return mainFrames;
	}

	/**
	 * add a MainFrame to the opened list
	 *
	 * @param mainFrame
	 */
	public void addMainFrame(MainFrame mainFrame) {
		//LOG.printInfos(TT+"addMainFrame(mainFrame="+mainFrame.getName()+")");
		if (!mainFrames.contains(mainFrame)) {
			mainFrames.add(mainFrame);
		}
	}

	/**
	 * remove a Mainframe from the opened list
	 *
	 * @param mainFrame
	 */
	public void removeMainFrame(MainFrame mainFrame) {
		//LOG.printInfos(TT+"removeMainFrame(mainFrame="+mainFrame.getName()+")");
		if (mainFrame.getProject() != null) {
			mainFrame.getProject().remove();
		}
		mainFrames.remove(mainFrame);
		reloadMenuBars();
	}

	/**
	 * close the blank Frame
	 *
	 */
	public void closeBlank() {
		//LOG.printInfos(TT+"closeBlank()");
		for (MainFrame mainFrame : mainFrames) {
			if (mainFrame.isBlank()) {
				mainFrames.remove(mainFrame);
				mainFrame.dispose();
			}
		}
	}

	/**
	 * get a project file name to create
	 *
	 * @param projtitle
	 * @return
	 */
	public static String getFileName(String projtitle) {
		//LOG.printInfos("LOG.getFileName()");
		String titlename = StringUtil.escapeTxt(projtitle);
		String x[] = titlename.split(" ");
		titlename = "";
		for (String z : x) {
			titlename += StringUtil.capitalize(z);
		}
		String filename = EnvUtil.getHomeDir().getPath()
				+ File.separator
				+ titlename
				+ STORYBOOK.FILE_EXT_OSBK.toString();
		File f = IOUtil.fileSelect(null, filename, "osbk", "osbk", "file.create");
		if (f == null) {
			return "";
		}
		return f.getAbsolutePath();
	}

	/**
	 * initialize a MainFrame for a new file
	 *
	 * @param dbFile
	 *
	 * @return
	 */
	private MainFrame initNewFile(Project dbFile) {
		//LOG.printInfos(TT+"initNewFile(dbFile=" + dbFile.getH2Name() + ", title=" + thetitle + ")");
		MainFrame newMainFrame = new MainFrame(dbFile);
		return newMainFrame;
	}

	/**
	 * initialize the properties for a new file project
	 *
	 * @param newMainFrame
	 * @param thetitle
	 * @param fromDoc
	 */
	private void initNewProperties(MainFrame newMainFrame, String thetitle, String... fromDoc) {
		PropertiesDlg properties = new PropertiesDlg(newMainFrame, true);
		properties.tfTitle.setText(thetitle);
		if (fromDoc != null && fromDoc.length > 0) {
			String ext = (fromDoc[0].endsWith(".docx") ? "docx" : "odt");
			properties.getXeditor().setExtension(ext);
		}
		properties.setVisible(true);
	}

	/**
	 * initialize folders for a new project
	 *
	 * @param m
	 * @param project
	 * @param title
	 */
	private void initNewFileEnd(MainFrame m, Project project, String title) {
		/*LOG.printInfos(TT+"initNewFileEnd(mainFrame"
			+ ", dbFile=" + dbFile.getH2Name()
			+ ", title=" + thetitle + ")");*/
		m.initUi();
		m.getBookController().fireAgain();
		addMainFrame(m);
		closeBlank();
		recentfilesUpdate(project);
		m.reloadBook();
		m.fileSave(true);
		//create new Images directory
		IOUtil.dirCreate(project.getPath(), "Images");
		//create new Documents directory
		IOUtil.dirCreate(project.getPath(), "Documents");
		initNewProperties(m, title);
		setDefaultCursor();
	}

	/**
	 * create a new project file
	 */
	public void createNewProject() {
		//LOG.trace(TT + "createNewFile()");
		ProjectNewDlg dlg = new ProjectNewDlg(mainFrames.get(0));
		dlg.setVisible(true);
		if (dlg.isCanceled()) {
			return;
		}
		String filename = getFileName(dlg.getTitle());
		if (filename.isEmpty()) {
			return;
		}
		if (filename.endsWith(STORYBOOK.FILE_EXT_OSBK.toString())) {
			filename = filename.replace(STORYBOOK.FILE_EXT_OSBK.toString(), "");
		}
		File fOsbk = new File(filename + STORYBOOK.FILE_EXT_OSBK.toString());
		if (fOsbk.exists()) {
			if (ConfirmDlg.show(null,
					I18N.getMsg("file.save.overwrite.title"),
					I18N.getMsg("file.exists", filename),
					false) == ConfirmDlg.CANCEL) {
				return;
			}
			IOUtil.fileDelete(fOsbk);
		}
		Project project = new Project(fOsbk, dlg);
		openProject(project);
	}

	/**
	 * rename the OSBK file
	 *
	 * @param mainFrame
	 * @param outFile
	 */
	public void renameFile(final MainFrame mainFrame, File outFile) {
		//LOG.printInfos(TT+"renameFile(mainFrame,outFile="+outFile.getAbsolutePath()+")");
		try {
			File inFile = mainFrame.getProject().getFile();
			mainFrame.close(false);
			Path inPath = inFile.toPath();
			Path outPath = outFile.toPath();
			Files.move(inPath, outPath, REPLACE_EXISTING);
			Project dbFile = new Project(outFile);
			openProject(dbFile);
		} catch (IOException e) {
			LOG.err(TT + ".renameFile(" + mainFrame.getName() + "," + outFile.getName() + ")", e);
		}
	}

	/**
	 * open the project file
	 *
	 * @return
	 */
	public boolean selectProject() {
		//LOG.trace(TT + "selectProject()");
		Project db = BookUtil.chooseProject();
		if (db == null || db.getFilename() == null) {
			return false;
		}
		return openProject(db);
	}

	/**
	 * open the Project
	 *
	 * @param project
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public boolean openProject(final Project project, String oldPath, String newPath) {
		//LOG.trace(TT + "openProject(project, oldPath=" + oldPath + ", newPath=" + newPath + ")");
		boolean rc = openProject(project);
		SwingUtilities.invokeLater(() -> App.changingProject(project, oldPath, newPath));
		return rc;
	}

	/**
	 * change the file
	 *
	 * @param project
	 * @param oldPath
	 * @param newPath
	 */
	public static void changingProject(Project project, String oldPath, String newPath) {
		MainFrame mf = null;
		for (MainFrame m : App.getInstance().mainFrames) {
			//LOG.trace("changingDbFile h2File="
			//		+ project.getName() + ", m.dbFile=" + m.getProject().getName());
			if (m.getProject().equals(project)) {
				mf = m;
			}
		}
		if (mf == null) {
			//LOG.trace("changingDbFile mf=null");
			return;
		}
		mf.changePath(oldPath, newPath);
		mf.getBook().setTitle(I18N.getMsg("copyof") + " " + mf.getBook().getTitle());
	}

	/**
	 * open the given Project to a new MainFrame
	 *
	 * @param project
	 *
	 * @return : true if OK
	 */
	public boolean openProject(Project project) {
		/*LOG.trace(TT + "openProject("
		+ "project=" + (project == null ? "null" : project.getFilename()) + ")");*/
		if (project == null || !project.isOK()) {
			return false;
		}
		if (project.isAlreadyOpened()) {
			return true;
		}
		try {
			setWaitCursor();
			Waiting dlg = new Waiting(null, I18N.getMsg("loading", project.getFilename()));
			SwingUtilities.invokeLater(() -> {
				try {
					//LOG.trace(">>> open a Project");
					setWaitCursor();
					dlg.setText("--> Initialize MainFrame...");
					MainFrame newMainFrame = new MainFrame(project);
					dlg.setText("--> Adding MainFrame...");
					addMainFrame(newMainFrame);
					//LOG.trace("--> Close blank...");
					closeBlank();
					dlg.setText("--> Update preferences...");
					recentfilesUpdate(project);
					dlg.setText("--> Reload menu bars...");
					reloadMenuBars();
					setDefaultCursor();
					dlg.setText("<<< open a Project");
				} catch (Exception e) {
					ExceptionDlg.show(this.getClass().getSimpleName()
							+ ".openProject(" + project.getFilename() + ")", e);
				}
				dlg.dispose();
			});
		} catch (HeadlessException e) {
			LOG.err(TT + ".openFile(...)", e);
		}
		return true;
	}

	/**
	 * update the preferences for the given recent file
	 *
	 * @param project
	 */
	public void recentfilesUpdate(Project project) {
		//LOG.printInfos(TT+"recentfilesUpdate(project='" + project.getName() + "'")");
		if (project == null) {
			return;
		}
		// toXml last openDocument directory and file
		File file = new File(project.getFilename());
		preferences.setString(Pref.KEY.LASTOPEN_DIR, file.getParent());
		preferences.setString(Pref.KEY.LASTOPEN_FILE, file.getPath());
		// toXml recent files
		preferences.recentFilesAdd(project.getFilename(), project.book.getTitle());
		preferences.save();
		reloadMenuBars();
	}

	/**
	 * clear the recent files list
	 */
	public void recentfilesClear() {
		//LOG.printInfos(TT+"recentFilesClear()");
		preferences.recentFilesClear();
		reloadMenuBars();
	}

	/**
	 * exit the application
	 */
	public void exit() {
		//LOG.printInfos(TT+"exit()");
		if (!mainFrames.isEmpty()) {
			for (MainFrame m : mainFrames) {
				if (m.askForSave() == 0) {
					return;
				}
			}
		}
		if (!mainFrames.isEmpty()
				&& preferences.getBoolean(Pref.KEY.CONFIRM_EXIT)
				&& ConfirmDlg.show(mainFrames.get(0),
						I18N.getMsg("exit"),
						I18N.getMsg("want.exit"),
						false) == ConfirmDlg.CANCEL) {
			return;
		}
		preferences.save();
		System.exit(0);
	}

	/**
	 * refresh all opened MainFrames
	 */
	public void refresh() {
		//LOG.printInfos(TT+"refresh()");
		for (MainFrame mainFrame : mainFrames) {
			int width = mainFrame.getWidth();
			int height = mainFrame.getHeight();
			boolean maximized = mainFrame.isMaximized();
			mainFrame.getMainMenu().reloadToolbar();
			mainFrame.setSize(width, height);
			if (maximized) {
				mainFrame.setMaximized();
			}
			mainFrame.refresh();
		}
	}

	/**
	 * reload the menu bar for all MainFrames
	 */
	public void reloadMenuBars() {
		//LOG.printInfos(TT + "reloadMenuBars()");
		for (MainFrame mainFrame : mainFrames) {
			mainFrame.getMainMenu().reloadRecentMenu();
			mainFrame.getMainMenu().reloadToolbar();
			mainFrame.getMainMenu().reloadWindowMenu();
		}
	}

	/**
	 * reload the status bar for all MainFrames
	 */
	public void reloadStatusBars() {
		for (MainFrame mainFrame : mainFrames) {
			mainFrame.refreshStatusBar();
		}
	}

	/**
	 * set waiting cursor
	 */
	public void setWaitCursor() {
		for (MainFrame mainFrame : mainFrames) {
			SwingUtil.setWaitingCursor(mainFrame);
		}
	}

	/**
	 * set default cursor
	 */
	public void setDefaultCursor() {
		for (MainFrame mainFrame : mainFrames) {
			SwingUtil.setDefaultCursor(mainFrame);
		}
	}

	/**
	 * action for model properties change
	 *
	 * @param evt
	 */
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// works, but currently not used
		// may be used for entity copying between files
	}

	/**
	 * lock the instance
	 *
	 * @param lockFile
	 * @return
	 */
	private static boolean lockInstance(final String lockFile) {
		try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							file.delete();
						} catch (IOException e) {
							LOG.err("Unable to remove lock file: " + lockFile, e);
						}
					}
				});
				return true;
			}
		} catch (IOException e) {
			LOG.err("Unable to create and/or lock file: " + lockFile, e);
		}
		return false;
	}

	/**
	 * get the I18N filename
	 *
	 * @return
	 */
	public String getI18nFile() {
		return i18nFile;
	}

	/**
	 * set the I18N filename
	 *
	 * @param file
	 */
	public void setI18nFile(String file) {
		i18nFile = file;
	}

	/**
	 * refresh view for all MainFrame
	 */
	public void refreshViews() {
		//LOG.printInfos("App.refreshViews()");
		for (MainFrame mainFrame : mainFrames) {
			mainFrame.refresh();
		}
	}

	/**
	 * refresh the status bar for all MainFrame
	 */
	public void refreshStatusBar() {
		for (MainFrame m : mainFrames) {
			m.refreshStatusBar();
		}
	}

	/**
	 * initialize the dictaphone
	 */
	private static void dictaphoneInit() {
		//LOG.printInfos(TT+"initDictaphone()");
		//Dictaphone.init();
	}

	/**
	 * start the Ideabox frame
	 */
	private void ideaboxStart() {
		SwingUtilities.invokeLater(() -> {
			IdeaxFrm dlg = new IdeaxFrm();
			dlg.setVisible(true);
			dlg.toFront();
			dlg.requestFocus();
		});
	}

	/////// Copy/Paste functions ////////
	private boolean pasteOK = false;

	public boolean pasteIsOK() {
		return pasteOK;
	}

	public void enablePaste(boolean b) {
		pasteOK = b;
		for (MainFrame m : mainFrames) {
			m.getMainMenu().enablePaste(b);
		}
	}

	public void enableCopyEntity() {
		for (MainFrame m : mainFrames) {
			m.getMainMenu().enableCopyEntity(mainFrames.size() > 1);
		}
	}
	/////// end Copy/Paste functions ///////////

	/**
	 * import a document as an OSBK project
	 *
	 * @param hi : the import document
	 */
	public void importBook(ImportDocument hi) {
		//LOG.printInfos(TT+"importBook(hi)");
		Project proj = hi.getProject();
		MainFrame newMainFrame = initNewFile(proj);
		newMainFrame.getBookModel().initEntities(hi.getTitle());
		newMainFrame.project.book.param.getParamLayout()
				.setSceneSeparatorValue(preferences.getString(Pref.KEY.SCENE_SEPARATOR));
		hi.getAll(proj);
		initNewFileEnd(newMainFrame, proj, hi.getTitle());
	}

	/**
	 * refresh the user interface language
	 */
	public void refreshLanguage() {
		JOptionPane.showMessageDialog(this,
				I18N.getMsg("preferences.lang.change"),
				I18N.getMsg("preferences"),
				JOptionPane.YES_OPTION);
		// change all tables headers
		for (MainFrame m : mainFrames) {
			m.updateLanguage();
		}
	}

}
