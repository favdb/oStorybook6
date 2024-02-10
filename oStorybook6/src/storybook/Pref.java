/*
 * Copyright (C) 2016 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.file.EnvUtil;
import storybook.tools.swing.ColorUtil;

/**
 *
 * @author favdb
 */
public class Pref {

	private static final String TT = "Pref";

	List<PrefValue> preferences = new ArrayList<>();
	private final String prefFile = EnvUtil.getIniFile().getAbsolutePath();
	public List<PrefFile> recentFiles = new ArrayList<>();

	/**
	 * dump preferences to log
	 *
	 */
	public void dump() {
		for (PrefValue preference : preferences) {
			LOG.log(preference.toString());
		}
	}

	/**
	 * set a date for a key
	 *
	 * @param key
	 * @param date
	 */
	public void setDate(KEY key, Date date) {
		if (date == null) {
			setString(key, "");
		} else {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			setString(key, fmt.format(date));
		}
	}

	public Date getDate(KEY key) {
		Date date = new Date();
		String str = getString(key);
		if (!str.isEmpty()) {
			try {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				date = fmt.parse(str);
			} catch (ParseException ex) {
				date = new Date();
			}
		}
		return date;
	}

	// story preference options
	public String storyGetOptions(String story) {
		return getString(story + "Options", "010");
	}

	public void storySetOptions(String story, String value) {
		setString(story + "Options", value);
	}

	// Freytag preferences: options
	public String freytagGetOptions() {
		return getString(KEY.FREYTAG_OPTIONS);
	}

	public void freytagSetOptions(String value) {
		setString(KEY.FREYTAG_OPTIONS, value);
	}

	public String voglerGetOptions() {
		return (getString(KEY.VOGLER_OPTIONS));
	}

	public void voglerSetOptions(String value) {
		setString(KEY.VOGLER_OPTIONS, value);
	}

	public enum KEY {
		ASSISTANT("Assistant", ""),
		ASSISTANT_USE("AssistantUse", "internal"),
		BACKUP_AUTO("BackupAuto", "0"),
		BACKUP_CLOSE("BackupClose", ""),
		BACKUP_DIR("BackupDir", ""),
		BACKUP_INCREMENT("BackupIncrement", "1"),
		BACKUP_OPEN("BackupOpen", ""),
		BOOK_LASTAUTHORNAME("LastAuthorName", ""),
		BOOK_LASTCOPYRIGHT("LastCopyright", ""),
		BOOK_LAYOUT("BookLayout", "000000000"),
		BOOK_REVISION("BookRevision", "0"),
		BOOK_ZOOM("BookZoom", "4"),
		CHRONO_LAYOUT_DIRECTION("ChornoLayoutDirection", "1"),
		CHRONO_SHOW_DATE_DIFFERENCE("ChornoShowDateDiff", "0"),
		CHRONO_ZOOM("ChronoZoom", "40"),
		CONFIRM_EXIT("ConfirmExit", "1"),
		DATEFORMAT("DateFormat", "MM-dd-yyyy"),
		DOCKING_LAYOUT("DockingLayout", ""),
		EDITOR_DEPLOY("EditorDeploy", "1"),
		EDITOR_MODLESS("EditorModless", "0"),
		EDITOR_TABBED_DESCRIPTION("EditorTabbedDescription", "1"),
		EDITOR_TABBED_NOTES("EditorTabbedNotes", "1"),
		EDITOR_USE_HTML_SCENES("UseHtmlScenes", "1"),
		EPISODES("Episodes", "1,65,198,59,254"),
		EPISODES_WITHTITLE("EpisodesWithTitle", "0"),
		EXP_CHAPTER_BOOKTITLE("ExportChapterBooktitle", "1"),
		EXP_CHAPTER_BREAKPAGE("ExportChapterBreakPage", "0"),
		EXP_CSV_SEPARATOR("ExportCsvSeparator", ""),
		EXP_DIRECTORY("ExportDirectory", ""),
		EXP_FORMAT("ExportFormat", "xml"),
		EXP_HIGHLIGHT("ExportHighlight", "2"),
		EXP_HTML_BOOK_MULTICHAPTER("HTMLBookMultifile", "0"),
		EXP_HTML_BOOK_MULTISCENE("HTMLBookMultiScene", "0"),
		EXP_HTML_CSS_FILE("HTMLCssFile", ""),
		EXP_HTML_NAV("HTMLNav", "0"),
		EXP_HTML_NAV_IMAGE("HTMLNavImage", "0"),
		EXP_HTML_USE_CSS("HTMLUseCSS", "0"),
		EXP_PART_TITLES("ExportPartTitles", "1"),
		EXP_PDF_LANDSCAPE("PDFLandscape", "0"),
		EXP_PDF_PAGE_SIZE("PDFPageSize", "A4"),
		EXP_PHPBB("ExportPhpBB", "0"),
		EXP_PHPBB_THEME("ExportPhpBBTheme", "blue"),
		EXP_TXT_SEPARATOR("ExportTxtSeparator", "|"),
		EXP_TXT_TAB("ExportTxtTab", "1"),
		FIRST_START("FirstStart", "1"),
		FONT_DEFAULT("FontDefault", Const.FONT_DEFAULT),
		FONT_BOOK("FontBook", Const.FONT_BOOK),
		FONT_EDITOR("FontEditor", Const.FONT_EDITOR),
		FONT_MONO("FontMono", Const.FONT_MONO),
		GALLERY_DIRECTION("GalleryDirection", "0"),
		GALLERY_FOLDER("GalleryFolder", "Photos"),
		GALLERY_ZOOM("GalleryZoom", "1"),
		ICON_SIZE("IconSize", "0"),
		ICON_SCREEN("IconScreen", "0"),
		IDEAX_SHEF_SHOW_HIDE("IdeaxShefShowHide", "0"),
		IDEABOX_FRAME("IdeaboxFrame", "1024x800|0,0"),
		IDEABOX_DIALOG("IdeaboxDialog", "880x650|0,0"),
		IMAGE_LATSDIR("LastImageDir", ""),
		IMP_DIRECTORY("ImportDirectory", ""),
		IMP_FILE("ImportFile", ""),
		INFO_DETAIL("InfoDetail", "1"),
		INFO_DETAIL_HIDE("InfoDetailHide", "0"),
		INTENSITY_COLORS("IntensityColors", ""),
		LAF("LookAndFeel", Const.LookAndFeel.LIGHT.name()),
		LAF_COLORED("LAF_Colored", "0"),
		LAF_COLOR("LAF_Color", ColorUtil.getHTML(ColorUtil.PALETTE.STORYBOOK.getColor())),
		LANGUAGE("Language", "en_US"),
		LASTOPEN_BOOK("LastOpenBook", ""),
		LASTOPEN_DIR("LastOpenDir", ""),
		LASTOPEN_FILE("LastOpenFile", ""),
		LAYOUT_LAST_USED("LastUsedLayout", ""),
		MANAGE_COLUMNS("ManageColumns", "5"),
		MANAGE_HIDE_UNASSIGNED("ManageHideUnassigned", "0"),
		MANAGE_VERTICAL("ManageVertical", "1"),
		MANAGE_ZOOM("ManageZoom", "5"),
		MEMO_LAYOUT_DIRECTION("MemoLayoutDirection", "1"),
		MEMORIA_ALL("MemoriaAll", "0"),
		MEMORIA_LAYOUT("MemoriaLayout", "0"),
		MEMORIA_SEL("MemoriaSel", "111111"),
		MEMORY("SeeMemory", "0"),
		MSGFILE("MessagesFile", ""),
		OPEN_LASTFILE("OpenLastFile", "false"),
		READING_FONT_SIZE("ReadingFontSize", "11"),
		READING_TOCLEVEL("ReadingToclevel", "0"),
		READING_ZOOM("ReadingZoom", "60"),
		RECENT_FILES("RecentFiles", ""),
		SCENE_LASTDATE("SceneLastDate", ""),
		SCENE_RENUM("SceneRenum", "1;1"),
		SCENE_SEPARATOR("SceneSeparator", ".oOo."),
		SPELLING("Spelling", "none"),
		SPELLING_ENABLE("SpellingEnable", "0"),
		STORYBOARD_DIRECTION("StoryboardDirection", "0"),
		STORYMAP("Storymap", "0001"),
		FREYTAG_OPTIONS("FreytagOptions", "111"),
		VOGLER_OPTIONS("VoglerOptions", "111"),
		TIMEFORMAT("TimeFormat", "HH:mm:ss"),
		TIMELINE_ZOOM("TimelineZoom", "1024"),
		TIMELINE_OPTIONS("TimelineOptions", "111"),
		TREE_SHOW("TreeShow", "101111111"), //tree view options
		TREE_OPTIONS("TreeOptions", ""),//for expanding all
		TREE_TRUNC("TreeTruncation", "0"),//for text truncation, 0=false(leave as is), 1=true (ellipsize at nb char)
		TREE_CHAR("TreeTruncNbChar", "8"),//for text truncation to nb char, default value is 8
		TOOLBAR_ORIENTATION("ToolBarOrientation", "North"),
		TOOLBAR("Toolbar",
		   "1"//file.new
		   + "1"//file.open
		   + "1"//file.save
		   + "0"//strand.new
		   + "0"//part.new
		   + "1"//chapter.new
		   + "1"//scene.new
		   + "1"//person.new
		   + "0"//gender.new
		   + "0"//relation.new
		   + "1"//location.new
		   + "1"//item.new
		   + "0"//temlink.new
		   + "0"//tag.new
		   + "0"//taglink.new
		   + "1"//memo.new
		   + "1"//idea.new
		   + "1"//manage_strands
		   + "0"//manage_parts
		   + "1"//manage_chapters
		   + "1"//manage_scenes
		   + "1"//manage_persons
		   + "0"//manage_genders
		   + "0"//manage_relations
		   + "1"//manage_locations
		   + "1"//manage_items
		   + "0"//manage_itemlinks
		   + "0"//manage_tags
		   + "0"//manage_taglinks
		   + "0"//manage_memos
		   + "0"//manage_ideas
		   + "1"//chrono
		   + "0"//timeline
		   + "1"//book
		   + "1"//manage_view
		   + "1"//reading
		   + "1"//memoria
		   + "0"//storyboard
		   + "1"//typist
		),
		TYPIST_SHOWBAR("TypistShowBar", "1"),
		TYPIST_SHOWINFO("TypistShowInfo", "0"),
		TYPIST_USE("TypistUse", "0"),
		UPDATER_DO("UpdaterDo", "0"),
		UPDATER_LAST("UpdaterLast", ""),
		VERSION("Version", Const.getVersion()),
		WINDOW_MAXIMIZED("WindowMaximized", "0"),
		WINDOW_POS("WindowPos", "100,100"),
		WINDOW_SIZE("WindowSize", "1024x768"),
		WORK_OFFLINE("WorkOffline", "0"),
		NONE("None", "");
		final private String text, value;

		private KEY(String text, String value) {
			this.text = text;
			this.value = value;
		}

		public String getValue() {
			return (value);
		}

		public Integer getInteger() {
			return (Integer.valueOf(value));
		}

		public boolean getBoolean() {
			return (value.equals("1") || value.equals("true"));
		}

		@Override
		public String toString() {
			return text;
		}
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Pref() {
		load();
		String av = get(KEY.VERSION.toString(), KEY.VERSION.getValue());
		String cv = KEY.VERSION.getValue();
		if (!av.equals(cv)) {
			setString(KEY.VERSION.toString(), cv);
			LOG.log("Set Preferences version to " + cv);
		}
	}

	/**
	 * initialize the Fonts
	 *
	 */
	public void initFont() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int sz = (int) d.getWidth() / 800 * 10;
		setString(KEY.FONT_DEFAULT, "Dialog,plain," + sz);
		setString(KEY.FONT_BOOK, "Serif,plain," + sz);
		setString(KEY.FONT_EDITOR, "Sans Serif,plain," + sz);
		setString(KEY.FONT_MONO, "Monospace,plain," + sz);
	}

	public String get(String key, String def) {
		return (getString(key, def));
	}

	public String get(KEY key, String def) {
		return (getString(key, def));
	}

	public String getString(KEY key) {
		return (getString(key.toString(), key.getValue()));
	}

	public String getString(KEY key, String def) {
		return (getString(key.toString(), def));
	}

	public String getString(String key, String def) {
		for (PrefValue v : preferences) {
			if (v.key.equals(key)) {
				return (v.value);
			}
		}
		if (!"".equals(def)) {
			preferences.add(new PrefValue(key, def));
		}
		return (def);
	}

	public String getChar(KEY key) {
		String r = getString(key, key.getValue());
		if (r.length() > 1) {
			int n = Integer.parseInt(r);
			String s = "" + (char) n;
			return (s);
		}
		return (r);
	}

	public Integer getInteger(KEY key) {
		if (key.getValue().isEmpty()) {
			return 0;
		}
		return getInteger(key, Integer.valueOf(key.getValue()));
	}

	public Integer getInteger(KEY key, Integer val) {
		String r = getString(key, val.toString());
		return (r.isEmpty() ? 0 : Integer.valueOf(r));
	}

	public boolean getBoolean(KEY key) {
		String b = key.getValue();
		return getBoolean(key.toString(), ("1".equals(b) || "true".equals(b)));
	}

	public boolean getBoolean(KEY key, boolean b) {
		return getBoolean(key.toString(), b);
	}

	public boolean getBoolean(String key, boolean val) {
		String r = getString(key, (val ? "true" : "false"));
		return "true".equals(r) || "1".equals(r);
	}

	public Dimension getDimension(KEY key) {
		String r[] = getString(key.toString(), "1024x768").split("x");
		if (r.length < 2) {
			return new Dimension(1024, 768);
		}
		return new Dimension(Integer.parseInt(r[0]), Integer.parseInt(r[1]));
	}

	public void setDimension(KEY key, Dimension dim) {
		setString(key, dim.width + "x" + dim.height);
	}

	public Point getPoint(KEY key) {
		String r[] = getString(key.toString(), "0,0").split(",");
		if (r.length < 2) {
			return new Point(0, 0);
		}
		return new Point(Integer.parseInt(r[0]), Integer.parseInt(r[1]));
	}

	public void setPoint(KEY key, Point dim) {
		setString(key, dim.x + "," + dim.y);
	}

	/**
	 * add a file name to the recent files list
	 *
	 * @param file : file name to add
	 * @param title : title of the book
	 */
	public void recentFilesAdd(String file, String title) {
		//LOG.trace("Pref.recentFilesAdd(file=" + file + ", title=" + title);
		PrefFile pfFile = new PrefFile(file, title);
		for (int i = 0; i < recentFiles.size(); i++) {
			PrefFile pf = recentFiles.get(i);
			if (pf.file.equals(file)) {
				recentFiles.remove(i);
				i = -1;
			}
		}
		recentFiles.add(0, pfFile);
		recentFilesSave();
	}

	/**
	 * clear the recent Files list
	 *
	 */
	public void recentFilesClear() {
		recentFiles = new ArrayList<>();
		setString(KEY.RECENT_FILES, "");
	}

	/**
	 * refresh the recent files list
	 *
	 */
	@SuppressWarnings("unchecked")
	public void recentFilesLoad() {
		List<String> lsf = new ArrayList<>();
		recentFiles = new ArrayList<>();
		String v = getString(KEY.RECENT_FILES, "");
		if (!v.isEmpty()) {
			String vfiles[] = v.split(";");
			for (String vfile : vfiles) {
				String vx[] = vfile.split("\\|");
				if (vx.length == 2) {
					File f = new File(vx[0]);
					PrefFile pfFile = new PrefFile(vx[0], vx[1]);
					if (f.exists() && !lsf.contains(vx[0])) {
						lsf.add(vx[0]);
						recentFiles.add(pfFile);
					}
				}
			}
		}
	}

	/**
	 * refresh the recent Files list
	 *
	 */
	public void recentFilesSave() {
		StringBuilder b = new StringBuilder();
		recentFiles.forEach(pf -> {
			if (!b.toString().isEmpty()) {
				b.append(";");
			}
			File f = new File(pf.file);
			if (f.exists()) {
				b.append(pf.toString());
			}
		});
		setString(KEY.RECENT_FILES, b.toString());
	}

	public void set(String key, String value) {
		setString(key, value);
	}

	public void removeString(KEY key) {
		removeString(key.toString());
	}

	public void removeString(String keyName) {
		for (int i = 0; i < preferences.size(); i++) {
			if (preferences.get(i).key.equals(keyName)) {
				preferences.remove(i);
				return;
			}
		}
	}

	public void setString(KEY key, String value) {
		setString(key.toString(), value);
	}

	public void setString(String key, String value) {
		boolean notok = true;
		if (value == null || value.isEmpty() || value.equals("null")) {
			removeString(key);
			return;
		}
		for (PrefValue v : preferences) {
			if (v.key.equals(key)) {
				v.set(value);
				notok = false;
			}
		}
		if (notok) {
			preferences.add(new PrefValue(key, value));
		}
	}

	public void setChar(KEY key, String value) {
		int n = value.charAt(0);
		String f = String.format("%d", n);
		setString(key, f);
	}

	public void setInteger(KEY key, Integer value) {
		setString(key, value.toString());
	}

	public void setInteger(String key, Integer value) {
		setString(key, value.toString());
	}

	public void setBoolean(KEY key, boolean value) {
		setString(key, (value ? "true" : "false"));
	}

	public void setBoolean(String key, boolean value) {
		setString(key, (value ? "true" : "false"));
	}

	public void typistSet(boolean b) {
		setBoolean(KEY.TYPIST_USE, b);
	}

	public boolean typistGet() {
		return getBoolean(KEY.TYPIST_USE);
	}

	public void typistShowbarSet(boolean b) {
		setBoolean(KEY.TYPIST_SHOWBAR, b);
	}

	public boolean typistShowbarGet() {
		return getBoolean(KEY.TYPIST_SHOWBAR);
	}

	public void typistShowinfoSet(boolean b) {
		setBoolean(KEY.TYPIST_SHOWINFO, b);
	}

	public boolean typistShowinfoGet() {
		return getBoolean(KEY.TYPIST_SHOWINFO);
	}

	/**
	 * get the docking list
	 *
	 * @return
	 */
	public List<String> dockingGetList() {
		List<String> list = new ArrayList<>();
		for (PrefValue v : preferences) {
			if (v.key.equals(KEY.DOCKING_LAYOUT.toString())) {
				String values[] = v.value.split("#");
				list.addAll(Arrays.asList(values));
			}
		}
		return (list);
	}

	/**
	 * set the docking list
	 *
	 * @param key
	 * @param values
	 */
	public void dockingSetList(KEY key, List<String> values) {
		@SuppressWarnings("unchecked")
		List<String> uniqueList = ListUtil.setUnique(values);
		try {
			if (uniqueList.size() > 10) {
				uniqueList = uniqueList.subList(uniqueList.size() - 10,
				   uniqueList.size());
			}
		} catch (IndexOutOfBoundsException e) {
		}
		String val = ListUtil.join(uniqueList, "#");
		setString(key, val);
	}

	/**
	 * check if a preference loaded
	 *
	 * @param key: the preference key
	 *
	 * @return : true if it is loaded
	 */
	private boolean isToLoad(String key) {
		//LOG.trace(TT + ".isLoad(key=" + key + ")");
		if (key.startsWith("Editor_") || key.startsWith("Table_")) {
			return true;
		}
		for (KEY k : KEY.values()) {
			if (k.toString().equals(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * load preferences
	 *
	 */
	public void load() {
		File file = new File(prefFile);
		if (!file.exists()) {
			create(file);
			return;
		}
		LOG.trace("Load Preferences from " + file.getAbsolutePath());
		try {
			InputStream ips = new FileInputStream(prefFile);
			InputStreamReader ipsr = new InputStreamReader(ips);
			try (BufferedReader br = new BufferedReader(ipsr)) {
				String ligne;
				while ((ligne = br.readLine()) != null) {
					String[] s = ligne.split("=");
					if (s.length < 2) {
						preferences.add(new PrefValue(s[0], ""));
					} else {
						if (isToLoad(s[0])) {
							preferences.add(new PrefValue(s[0], s[1]));
						}
					}
				}
				br.close();
			}
			recentFilesLoad();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		intensityLoad();
	}

	// Backup preferences: auto, dir, increment
	public void backupSetAuto(boolean str) {
		setBoolean(KEY.BACKUP_AUTO, str);
	}

	public boolean backupGetAuto() {
		return (getBoolean(KEY.BACKUP_AUTO));
	}

	public void backupSetDir(String str) {
		setString(KEY.BACKUP_DIR, str);
	}

	public String backupGetDir() {
		return (getString(KEY.BACKUP_DIR));
	}

	public void backupSetIncrement(boolean str) {
		setBoolean(KEY.BACKUP_INCREMENT, str);
	}

	public boolean backupGetIncrement() {
		return getBoolean(KEY.BACKUP_INCREMENT);
	}

	// Book preferences: zoom
	public void bookSetZoom(Integer val) {
		setInteger(KEY.BOOK_ZOOM, val);
	}

	public Integer bookGetZoom() {
		return getInteger(KEY.BOOK_ZOOM);
	}

	// Chrono preference:zoom, direction, show date diff
	public void chronoSetZoom(Integer val) {
		setInteger(KEY.CHRONO_ZOOM, val);
	}

	public Integer chronoGetZoom() {
		return getInteger(KEY.CHRONO_ZOOM);
	}

	public void chronoSetLayoutDirection(boolean val) {
		setBoolean(KEY.CHRONO_LAYOUT_DIRECTION, val);
	}

	public boolean chronoGetLayoutDirection() {
		return getBoolean(KEY.CHRONO_LAYOUT_DIRECTION);
	}

	public void chronoSetShowDateDiff(boolean val) {
		setBoolean(KEY.CHRONO_SHOW_DATE_DIFFERENCE, val);
	}

	public boolean chronoGetShowDateDiff() {
		return getBoolean(KEY.CHRONO_SHOW_DATE_DIFFERENCE);
	}

	// Editor preferences: use HTML, modless
	public void editorSetUseHtml(boolean val) {
		setBoolean(KEY.EDITOR_USE_HTML_SCENES, val);
	}

	public boolean editorGetUseHtml() {
		return getBoolean(KEY.EDITOR_USE_HTML_SCENES);
	}

	public void editorSetModless(boolean val) {
		setBoolean(KEY.EDITOR_MODLESS, val);
	}

	public boolean editorGetModless() {
		return getBoolean(KEY.EDITOR_MODLESS);
	}

	// Episode preferences: set and get, zoom, with title
	public void episodesSet(String sizes) {
		setString(KEY.EPISODES, sizes);
	}

	public String episodesGet() {
		return getString(KEY.EPISODES);
	}

	public int episodesGetZoom() {
		String param[] = getString(KEY.EPISODES).split(",");
		if (param.length < 1) {
			return 1;
		}
		return Integer.parseInt(param[0]);
	}

	public boolean episodesGetWithTitle() {
		return getString(KEY.EPISODES_WITHTITLE).equals("1");
	}

	public void episodesSetWithTitle(boolean b) {
		setString(KEY.EPISODES_WITHTITLE, (b ? "1" : "0"));
	}

	// Gallery preferences: direction, folder, zoom
	public void gallerySetDirection(boolean b) {
		this.setBoolean(KEY.GALLERY_DIRECTION, b);
	}

	public boolean galleryGetDirection() {
		return getBoolean(KEY.GALLERY_DIRECTION, false);
	}

	public void gallerySetFolder(String b) {
		setString(KEY.GALLERY_FOLDER, b);
	}

	public String galleryGetFolder() {
		return getString(KEY.GALLERY_FOLDER, "Photo");
	}

	public void gallerySetZoom(int value) {
		setInteger(KEY.GALLERY_ZOOM, value);
	}

	public int galleryGetZoom() {
		return getInteger(KEY.GALLERY_ZOOM);
	}

	//Manage preferences: columns, hide unassigned, vertical, zoom
	public int manageGetColumns() {
		return getInteger(KEY.MANAGE_COLUMNS);
	}

	public void manageSetColumns(int value) {
		setInteger(KEY.MANAGE_COLUMNS, value);
	}

	public boolean manageGetHideUnassigned() {
		return getBoolean(KEY.MANAGE_HIDE_UNASSIGNED);
	}

	public void manageSetHideUnassgned(boolean value) {
		setBoolean(KEY.MANAGE_HIDE_UNASSIGNED, value);
	}

	public boolean manageGetVertical() {
		return getBoolean(KEY.MANAGE_VERTICAL);
	}

	public void manageSetVertical(boolean value) {
		setBoolean(KEY.MANAGE_VERTICAL, value);
	}

	public void manageSetZoom(int value) {
		setInteger(KEY.MANAGE_ZOOM, value);
	}

	public int manageGetZoom() {
		return getInteger(KEY.MANAGE_ZOOM);
	}

	// Memoria preferences: layout, all
	public void memoriaSetLayout(int value) {
		setInteger(KEY.MEMORIA_LAYOUT, value);
	}

	public int memoriaGetLayout() {
		return getInteger(KEY.MEMORIA_LAYOUT);
	}

	public void memoriaSetAll(boolean value) {
		setBoolean(KEY.MEMORIA_ALL, value);
	}

	public boolean memoriaGetAll() {
		return getBoolean(KEY.MEMORIA_ALL);
	}

	// Reading preferences: font size, zoom, direction
	public int readingGetFontSize() {
		return getInteger(KEY.READING_FONT_SIZE);
	}

	public void readingSetFontSize(Integer val) {
		setInteger(KEY.READING_FONT_SIZE, val);
	}

	public int readingGetZoom() {
		return getInteger(KEY.READING_ZOOM);
	}

	public void readingSetZoom(Integer val) {
		setInteger(KEY.READING_ZOOM, val);
	}

	public int readingGetToclevel() {
		return getInteger(KEY.READING_TOCLEVEL);
	}

	public void readingSetToclevel(Integer val) {
		setInteger(KEY.READING_TOCLEVEL, val);
	}

	// Storyboard preference: direction, zoom
	public boolean storyboardGetDirection() {
		return (getBoolean(KEY.STORYBOARD_DIRECTION));
	}

	public void storyboardSetDirection(boolean val) {
		setBoolean(KEY.STORYBOARD_DIRECTION, val);
	}

	// Timeline preferences: zoom, options
	public int timelineGetZoom() {
		return (getInteger(KEY.TIMELINE_ZOOM));
	}

	public void timelineSetZoom(int value) {
		setInteger(KEY.TIMELINE_ZOOM, value);
	}

	public String timelineGetOptions() {
		return (getString(KEY.TIMELINE_OPTIONS));
	}

	public void timelineSetOptions(String value) {
		setString(KEY.TIMELINE_OPTIONS, value);
	}

	// Tree preferences: show, trunc, char
	public String treeviewGetShow() {
		return getString(KEY.TREE_SHOW);
	}

	public void treeviewSetShow(String param) {
		setString(KEY.TREE_SHOW, param);
	}

	public boolean treeviewGetTrunc() {
		return getBoolean(KEY.TREE_TRUNC);
	}

	public int treeviewGetChar() {
		return getInteger(KEY.TREE_CHAR);
	}

	public void treeviewSetTrunc(boolean value) {
		setBoolean(KEY.TREE_TRUNC, value);
	}

	public void treeviewSetChar(int value) {
		setInteger(KEY.TREE_CHAR, value);
	}

	// renumber scenes
	public boolean sceneIsRenumAuto() {
		return getString(KEY.SCENE_RENUM).split(";")[0].equals("1");
	}

	public boolean sceneIsRenumByChapter() {
		String[] r = getString(KEY.SCENE_RENUM).split(";");
		if (r.length < 3 || r[2].isEmpty()) {
			return false;
		}
		return getString(KEY.SCENE_RENUM).split(";")[2].equals("1");
	}

	public int sceneGetRenumInc() {
		String[] r = getString(KEY.SCENE_RENUM).split(";");
		if (r.length < 2 || r[1].isEmpty()) {
			return 1;
		}
		return Integer.parseInt(r[1]);
	}

	public void sceneSetRenum(String str) {
		setString(KEY.SCENE_RENUM, str);
	}

	List<Color> intensityColors = new ArrayList<>();

	/**
	 * get the intensity Color
	 *
	 * @param i
	 * @return
	 */
	public Color intensityGet(int i) {
		if (intensityColors.isEmpty()) {
			return ColorUtil.getIntensityColor(i);
		}
		return intensityColors.get(i);
	}

	/**
	 * set the intensity Color
	 *
	 * @param i
	 * @param cc
	 */
	public void intensitySet(int i, Color cc) {
		if (intensityColors == null || i > intensityColors.size() - 1) {
			intensityLoad();
		}
		intensityColors.set(i, cc);
	}

	/**
	 * load colors for intensities
	 *
	 */
	public void intensityLoad() {
		intensityColors = new ArrayList<>();
		String s = getString(KEY.INTENSITY_COLORS, "");
		if (!s.isEmpty()) {
			String[] sc = s.split(",");
			for (String sc1 : sc) {
				intensityColors.add(ColorUtil.fromHexString(sc1));
			}
		} else {
			intensityColors.addAll(Arrays.asList(ColorUtil.intensityColors));
		}
	}

	/**
	 * save intensity
	 */
	public void intensitySave() {
		if (!intensityColors.isEmpty()) {
			List<String> ls = new ArrayList<>();
			for (Color c : intensityColors) {
				ls.add("#" + ColorUtil.getHexName(c));
			}
			setString(KEY.INTENSITY_COLORS, ListUtil.join(ls));
		}
	}

	public boolean getWorkOffline() {
		return getBoolean(KEY.WORK_OFFLINE);
	}

	public void setWorkOffline(boolean value) {
		setBoolean(KEY.WORK_OFFLINE, value);
	}

	/**
	 * create preferences File
	 *
	 * @param file
	 */
	private void create(File file) {
		LOG.log("Create new Preferences in " + file.getAbsolutePath());
		try {
			EnvUtil.getPrefDir().mkdir();
			file.createNewFile();
		} catch (IOException e) {
			LOG.log("Unable to create new file " + file.getAbsolutePath());
		}
		preferences.add(new PrefValue(KEY.VERSION));
		preferences.add(new PrefValue(KEY.FONT_DEFAULT));
		preferences.add(new PrefValue(KEY.FONT_BOOK));
		preferences.add(new PrefValue(KEY.FONT_EDITOR));
		preferences.add(new PrefValue(KEY.FONT_MONO));
		save();
	}

	/**
	 * save preferences
	 *
	 */
	public void save() {
		intensitySave();
		preferences.sort((PrefValue c1, PrefValue c2) -> c1.key.compareTo(c2.key));
		String newline = System.getProperty("line.separator");
		try (OutputStream f = new FileOutputStream(prefFile)) {
			for (PrefValue p : preferences) {
				if (isToLoad(p.key)) {
					f.write((p.toString() + newline).getBytes());
				}
			}
		} catch (FileNotFoundException ex) {
			LOG.err("Unable to save Preferences (file not found)", ex);
		} catch (IOException ex) {
			LOG.err("Unable to save Preferences", ex);
		}
	}

	/**
	 * Prefrence value class
	 */
	private static class PrefValue {

		String key;
		String value;

		public PrefValue(String k, String v) {
			key = k;
			value = v;
		}

		private PrefValue(KEY key) {
			this.key = key.toString();
			this.value = key.getValue();
		}

		public void set(String v) {
			value = v;
		}

		public String get() {
			return (value);
		}

		@Override
		public String toString() {
			return (key + "=" + value);
		}
	}

	/**
	 * Recent File class to associate filename with title
	 *
	 */
	public static class PrefFile {

		public String file, title;

		public PrefFile(String file, String title) {
			this.file = file;
			this.title = title;
		}

		@Override
		public String toString() {
			return (file + "|" + title);
		}
	}

}
