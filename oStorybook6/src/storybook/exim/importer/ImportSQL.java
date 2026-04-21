/*
 * Copyright (C) 2023 favdb
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
package storybook.exim.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JFileChooser;
import storybook.App;
import storybook.Pref;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.*;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.episode.Episode;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.db.status.Status;
import storybook.db.strand.Strand;
import storybook.db.tag.AbsTag;
import storybook.db.tag.Tag;
import static storybook.exim.importer.SqlUtil.*;
import storybook.project.Project;
import storybook.tools.LOG;
import storybook.tools.file.FileFilter;
import storybook.tools.file.IOUtil;
import storybook.tools.zip.ZipUtil;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class ImportSQL {

	private static final String TT = "ImportSQL.";
	private final String file;
	private InputStream inStream;
	private final Project project;
	private final List<String> attributeSt = new ArrayList<>(),
			categorySt = new ArrayList<>(),
			chapterSt = new ArrayList<>(),
			endnoteSt = new ArrayList<>(),
			episodeSt = new ArrayList<>(),
			eventSt = new ArrayList<>(),
			genderSt = new ArrayList<>(),
			ideaSt = new ArrayList<>(),
			internalSt = new ArrayList<>(),
			itemSt = new ArrayList<>(),
			locationSt = new ArrayList<>(),
			memoSt = new ArrayList<>(),
			partSt = new ArrayList<>(),
			personSt = new ArrayList<>(),
			plotSt = new ArrayList<>(),
			relationSt = new ArrayList<>(),
			sceneSt = new ArrayList<>(),
			statusSt = new ArrayList<>(),
			strandSt = new ArrayList<>(),
			tagSt = new ArrayList<>();
	List<String> links = new ArrayList<>();

	public static void selectFile(MainFrame mainFrame) {
		String nf = App.preferences.getString(Pref.KEY.LASTOPEN_DIR);
		JFileChooser chooser = new JFileChooser(nf);
		File f = new File(nf);
		chooser.setCurrentDirectory(f);
		FileFilter filter = new FileFilter("osbk");
		chooser.addChoosableFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File fileImport = chooser.getSelectedFile();
		if (fileImport.getAbsolutePath().endsWith(".osbk")) {
			List<String> entries = ZipUtil.listEntries(fileImport.getAbsolutePath());
			if (entries.isEmpty() || !entries.contains("script.sql")) {
				LOG.err(fileImport.getAbsolutePath() + " not a V5 OSBK file");
				return;
			}
			execSql(fileImport);
		}
	}

	public static void execSql(File fileImport) {
		String nf = fileImport.getParent();
		String infile = fileImport.getAbsolutePath();
		App.preferences.setString(Pref.KEY.LASTOPEN_DIR, nf);
		App.preferences.save();
		Project proj = new Project();
		proj.initData();
		proj.setFile(fileImport);
		ImportSQL sql = new ImportSQL(proj, infile);
		if (sql.exec()) {
			// set the internals links
			for (Book.TYPE t : Book.TYPE.values()) {
				proj.setLinks(t);
			}
			//LOG.trace(proj.printInfos(false));
			// rename the original file extension to osbk5
			File fnew = new File(IOUtil.changeExt(infile, ".osbk5"));
			if (fnew.exists()) {
				fnew.delete();
			}
			fileImport.renameTo(fnew);
			// write the imported project
			proj.save(fileImport);
			// open the new converted file in a new Mainframe
			//App.getInstance().openProject(new Project(fileImport.getAbsolutePath()));
		}
	}

	public ImportSQL(Project project, String file) {
		this.project = project;
		this.file = file;
	}

	public boolean exec() {
		//LOG.trace(TT + "exec()");
		boolean rc = false;
		try {
			ZipFile zipFile = new ZipFile(file);
			ZipEntry zipEntry = zipFile.getEntry("script.sql");
			inStream = zipFile.getInputStream(zipEntry);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
			while (reader.ready()) {
				String line = getUTF8(reader);
				if (line.startsWith("CREATE CACHED TABLE ")) {
					String objtype = line.replace("\"", "").replace("CREATE CACHED TABLE PUBLIC.", "")
							.replace("(", "").trim();
					List<String> structure = getStructure(reader);
					switch (objtype) {
						case "ATTRIBUTE":
							attributeSt.addAll(structure);
							break;
						case "CATEGORY":
							categorySt.addAll(structure);
							break;
						case "CHAPTER":
							chapterSt.addAll(structure);
							break;
						case "ENDNOTE":
							endnoteSt.addAll(structure);
							break;
						case "EPISODE":
							episodeSt.addAll(structure);
							break;
						case "TIMEEVENT":
							eventSt.addAll(structure);
							break;
						case "GENDER":
							genderSt.addAll(structure);
							break;
						case "IDEAS":
							ideaSt.addAll(structure);
							break;
						case "INTERNAL":
							internalSt.addAll(structure);
							break;
						case "ITEM":
							itemSt.addAll(structure);
							break;
						case "LOCATION":
							locationSt.addAll(structure);
							break;
						case "MEMO":
							memoSt.addAll(structure);
							break;
						case "PART":
							partSt.addAll(structure);
							break;
						case "PERSON":
							personSt.addAll(structure);
							break;
						case "PLOT":
							plotSt.addAll(structure);
							break;
						case "RELATIONSHIP":
							relationSt.addAll(structure);
							break;
						case "SCENE":
							sceneSt.addAll(structure);
							break;
						case "STRAND":
							strandSt.addAll(structure);
							break;
						case "TAG":
							tagSt.addAll(structure);
							break;
					}
				}
				if (line.startsWith("INSERT INTO")) {
					createEntities(reader, line);
				}
			}
			linksSet();
			rc = true;
		} catch (IOException ex) {
			LOG.err(TT + "exec()", ex);
		}
		return rc;
	}

	public static String ensureUtf8(String input) throws CharacterCodingException {
		// Vérifie si la chaîne est déjà en UTF-8
		if (isUtf8(input)) {
			return input;
		}

		// Essaie différents encodages courants
		String[] commonEncodings = {"ISO-8859-1", "Windows-1252", "ASCII"};

		for (String encoding : commonEncodings) {
			try {
				// Convertit la chaîne vers UTF-8
				byte[] bytes = input.getBytes(encoding);
				return new String(bytes, StandardCharsets.UTF_8);
			} catch (UnsupportedEncodingException e) {
			}
		}
		throw new CharacterCodingException();
	}

	/**
	 * Vérifie si une chaîne est valide en UTF-8
	 *
	 * @param input la chaîne à vérifier
	 * @return true si la chaîne est en UTF-8, false sinon
	 */
	private static boolean isUtf8(String input) {
		try {
			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();

			ByteBuffer bytes = encoder.encode(CharBuffer.wrap(input));
			decoder.decode(bytes);
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}

	private void createEntities(BufferedReader reader, String line) throws IOException {
		//select the right object type
		String objtype = line.replace("\"", "").replace("INSERT INTO PUBLIC.", "")
				.replace("VALUES", "").trim();
		if (objtype.equals("RELATIONSHIP")) {
			relationsCreate(reader);
		} else {
			switch (Book.getTYPE(objtype)) {
				case ATTRIBUTE:
					attributesCreate(reader);
					break;
				case CATEGORY:
					categoriesCreate(reader);
					break;
				case CHAPTER:
					chaptersCreate(reader);
					break;
				case ENDNOTE:
					endnotesCreate(reader);
					break;
				case EPISODE:
					episodesCreate(reader);
					break;
				case EVENT:
					eventsCreate(reader);
					break;
				case GENDER:
					gendersCreate(reader);
					break;
				case IDEA:
					ideasCreate(reader);
					break;
				case INTERNAL:
					bookCreate(reader);
					break;
				case ITEM:
					itemsCreate(reader);
					break;
				case LOCATION:
					locationsCreate(reader);
					break;
				case MEMO:
					memosCreate(reader);
					break;
				case PART:
					partsCreate(reader);
					break;
				case PERSON:
					personsCreate(reader);
					break;
				case PLOT:
					plotsCreate(reader);
					break;
				case RELATION:
					relationsCreate(reader);
					break;
				case SCENE:
					scenesCreate(reader);
					break;
				case STATUS:
					statussCreate(reader);
					break;
				case STRAND:
					strandsCreate(reader);
					break;
				case TAG:
					tagsCreate(reader);
					break;
				default:
					linksMemo(objtype, reader);
					break;
			}
		}
	}

	/**
	 * create the Book XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void bookCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			List<String> ls = internalSt;
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				String key = getString(ls, "KEY", s);
				String vStr = getString(ls, "STRING_VALUE", s);
				String bv = "BOOLEAN_VALUE";
				Integer vInt = getInteger(ls, "INTEGER_VALUE", s);
				switch (key.toLowerCase()) {
					// info
					case "bookcreationdate":
						project.book.info.creationSet(vStr);
						break;
					case "bookupdate":
						project.book.info.majSet(vStr);
						break;
					case "title":
						project.book.info.titleSet(vStr);
						break;
					case "subtitle":
						project.book.info.subtitleSet(vStr);
						break;
					case "author":
						project.book.info.authorSet(vStr);
						break;
					case "copyright":
						project.book.info.copyrightSet(vStr);
						break;
					case "blurb":
						project.book.info.blurbSet(vStr);
						break;
					case "notes":
						project.book.info.notesSet(vStr);
						break;
					case "assistant":
						project.book.info.assistantSet(vStr);
						break;
					case "scenario":
						project.book.info.scenarioSet(getBoolean(ls, bv, s));
						break;
					case "markdown":
						project.book.info.markdownSet(getBoolean(ls, bv, s));
						break;
					case "isbn":
						project.book.info.isbnSet(vStr);
						break;
					case "uuid":
						project.book.info.uuidSet(vStr);
						break;
					case "language":
						project.book.info.languageSet(vStr);
						break;
					case "nature":
						project.book.info.natureSet(vInt);
						break;
					case "dedication":
						project.book.info.dedicationSet(vStr);
						break;
					case "review":
						project.book.info.reviewSet(getBoolean(ls, bv, s));
						break;
					// param Backup
					case "backupdir":
						project.book.param.getParamBackup().setDirectory(vStr);
						break;
					case "backupauto":
						project.book.param.getParamBackup().setAuto(getBoolean(ls, bv, s));
						break;
					case "backupincrement":
						project.book.param.getParamBackup().setIncrement(vInt);
						break;
					// param Editor
					case "xeditormodless":
						project.book.param.getParamEditor().setModless(getBoolean(ls, bv, s));
						break;
					case "xeditoruse":
						project.book.param.getParamEditor().setUse(getBoolean(ls, bv, s));
						break;
					case "xeditorname":
						project.book.param.getParamEditor().setName(vStr);
						break;
					case "xeditorextension":
						project.book.param.getParamEditor().setExtension(vStr);
						break;
					case "xeditortemplate":
						project.book.param.getParamEditor().setTemplate(vStr);
						break;
					// param Export
					case "exportdirectory":
						project.book.param.getParamExport().setDirectory(vStr);
						break;
					case "exportchapterbooktitle":
						project.book.param.getParamExport().setHtmlChapterTitle(getBoolean(ls, bv, s));
						break;
					case "exportchapterbreakpage":
						project.book.param.getParamExport().setHtmlChapterBreakPage(getBoolean(ls, bv, s));
						break;
					case "exporthtmlcss":
						project.book.param.getParamExport().setHtmlCss(vStr);
						break;
					case "exporthtmlnav":
						project.book.param.getParamExport().setHtmlNav(getBoolean(ls, bv, s));
						break;
					case "exporthtmlnavimage":
						project.book.param.getParamExport().setHtmlNavImage(getBoolean(ls, bv, s));
						break;
					case "exporthtmlmultichapter":
						project.book.param.getParamExport().setMultiChapter(getBoolean(ls, bv, s));
						break;
					case "exporthtmlmultiscene":
						project.book.param.getParamExport().setMultiScene(getBoolean(ls, bv, s));
						break;
					case "epubcover":
						project.book.param.getParamExport().setEpubCover(getBoolean(ls, bv, s));
						break;
					// param Import
					case "importdirectory":
						project.book.param.getParamImport().setDirectory(vStr);
						break;
					case "importfile":
						project.book.param.getParamImport().setFile(vStr);
						break;
					// param Layout
					case "booklayout":
						project.book.param.getParamLayout().setBookLayout(vStr);
						break;
					case "sceneseparator":
						project.book.param.getParamLayout().setSceneSeparatorValue(vStr);
						break;
				}
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * get a line for the given parameter
	 *
	 * @param pline
	 * @return
	 */
	private List<String> getLine(String pline) {
		List<String> ls = new ArrayList<>();
		StringBuilder b = new StringBuilder();
		boolean q = false;
		String line = pline.substring(1, pline.lastIndexOf(")"));
		for (int i = 0; i < line.length(); i++) {
			char curChar = line.charAt(i);
			if (curChar == '\'') {
				q = !q;
				b.append(curChar);
			} else if (curChar == ',' && !q) {
				ls.add(b.toString());
				b.setLength(0);
			} else {
				b.append(curChar);
			}
		}
		ls.add(b.toString());
		return ls;
	}

	private String getUTF8(BufferedReader reader) throws IOException {
		String line = reader.readLine().trim();
		return ensureUtf8(line);
	}

	/**
	 * create the Attributes XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void attributesCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = attributeSt;
				Attribute e = new Attribute();
				getCommon(ls, e, s);
				e.setKey(getString(ls, "KEY", s));
				e.setValue(getString(ls, "VALUE", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.attributes.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Categories XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void categoriesCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = categorySt;
				Category e = new Category();
				getCommon(ls, e, s);
				e.setSort(getInteger(ls, "SORT", s));
				e.setSupId(getLong(ls, "CATEGORY_ID", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.categorys.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Chapters XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void chaptersCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = chapterSt;
				Chapter e = new Chapter();
				getCommon(ls, e, s);
				e.setTitle(getString(ls, "TITLE", s));
				e.setPartId(getLong(ls, "PART_ID", s));
				e.setChapterno(getInteger(ls, "CHAPTERNO", s));
				//e.setCreationTime(getTimestamp(ls, "CREATION_TS", s));
				e.setObjectiveTime(getTimestamp(ls, "OBJECTIVE_TS", s));
				e.setDoneTime(getTimestamp(ls, "DONE_TS", s));
				e.setObjectiveChars(getInteger(ls, "OBJECTIVE_CH", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.chapters.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Endnotes XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void endnotesCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = endnoteSt;
				Endnote e = new Endnote();
				getCommon(ls, e, s);
				e.setType(getInteger(ls, "TYPE", s));
				e.setNumber(getInteger(ls, "NUMBER", s));
				e.setSceneId(getLong(ls, "SCENE_ID", s));
				e.setAspect(getString(ls, "ASPECT", s));
				e.setSort(getString(ls, "SORT", s));
				project.endnotes.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Episodes XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void episodesCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = episodeSt;
				Episode e = new Episode();
				getCommon(ls, e, s);
				e.setNumber(getInteger(ls, "NUMBER", s));
				e.setStrandId(getLong(ls, "STRAND_ID", s));
				e.setSceneId(getLong(ls, "SCENE_ID", s));
				e.setChapterId(getLong(ls, "CHAPTER_ID", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.episodes.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Events XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void eventsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = eventSt;
				Event e = new Event();
				getCommon(ls, e, s);
				//e.setTitle(getString(ls,"TITLE",s));
				e.setEventTime(getTimestamp(ls, "EVENT_TS", s));
				e.setDuration(getString(ls, "DURATION", s));
				e.setTimeStep(getInteger(ls, "TIME_STEP", s));
				e.setCategory(getString(ls, "CATEGORY", s));
				e.setColor(getInteger(ls, "COLOR", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.events.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Genders XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void gendersCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = genderSt;
				Gender e = new Gender();
				getCommon(ls, e, s);
				e.setChildhood(getInteger(ls, "CHILDHOOD", s));
				e.setAdolescence(getInteger(ls, "ADOLESCENCE", s));
				e.setAdulthood(getInteger(ls, "ADULTHOOD", s));
				e.setRetirement(getInteger(ls, "RETIREMENT", s));
				e.setIcone(getString(ls, "ICONE", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.genders.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Ideas XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void ideasCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = ideaSt;
				Idea e = new Idea();
				getCommon(ls, e, s);
				e.setStatus(getInteger(ls, "STATUS", s));
				e.setCategory(getString(ls, "CATEGORY", s));
				e.setAspect(getString(ls, "ASPECT", s));
				e.setUuid(getString(ls, "UUID", s));
				project.ideas.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Items XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void itemsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = itemSt;
				Item e = new Item();
				getCommon(ls, e, s);
				e.setCategory(getString(ls, "CATEGORY", s));
				project.items.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Locations XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void locationsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = locationSt;
				Location e = new Location();
				getCommon(ls, e, s);
				e.setAddress(getString(ls, "ADDRESS", s));
				e.setCity(getString(ls, "CITY", s));
				e.setCountry(getString(ls, "COUNTRY", s));
				e.setAltitude(getInteger(ls, "ALTITUDE", s));
				e.setGps(getString(ls, "GPS", s));
				e.setSiteId(getLong(ls, "LOCATION_ID", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.locations.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Memos XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void memosCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = memoSt;
				Memo e = new Memo();
				getCommon(ls, e, s);
				e.setCategory(getString(ls, "CATEGORY", s));
				project.items.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Parts XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void partsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = partSt;
				Part e = new Part();
				getCommon(ls, e, s);
				e.setNumber(getInteger(ls, "NUMBER", s));
				e.setSuperpartId(getLong(ls, "PART_ID", s));
				//e.setCreationTime(getTimestamp(ls, "CREATION_TS", s));
				e.setObjectiveTime(getTimestamp(ls, "OBJECTIVE_TS", s));
				e.setDoneTime(getTimestamp(ls, "DONE_TS", s));
				e.setObjectiveChars(getInteger(ls, "OBJECTIVE_CH", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.parts.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Persons XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void personsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = personSt;
				Person e = new Person();
				getCommon(ls, e, s);
				e.setGenderId(getLong(ls, "GENDER_ID", s));
				e.setFirstname(getString(ls, "FIRSTNAME", s));
				e.setLastname(getString(ls, "LASTNAME", s));
				e.setAbbreviation(getString(ls, "ABBREVIATION", s));
				e.setBirthday(getTimestamp(ls, "BIRTHDAY", s));
				e.setDayofdeath(getTimestamp(ls, "DAYOFDEATH", s));
				e.setOccupation(getString(ls, "OCCUPATION", s));
				e.setColor(getInteger(ls, "COLOR", s));
				e.setCategoryId(getLong(ls, "CATEGORY_ID", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.persons.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Plots XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void plotsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = plotSt;
				Plot e = new Plot();
				getCommon(ls, e, s);
				e.setCategory(getString(ls, "CATEGORY", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.plots.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Relations XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void relationsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = relationSt;
				Relation e = new Relation();
				getCommon(ls, e, s);
				e.setAspect(getString(ls, "ASPECT", s));
				e.setStartSceneId(getLong(ls, "START_SCENE_ID", s));
				e.setEndSceneId(getLong(ls, "END_SCENE_ID", s));
				project.relations.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Scenes XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void scenesCreate(BufferedReader reader) throws IOException {
		//LOG.trace(TT + "scenesCreate(reader)");
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = sceneSt;
				Scene e = new Scene();
				getCommon(ls, e, s);
				if (e.getName() == null | e.getName().isEmpty()) {
					e.setName(getString(ls, "TITLE", s));
				}
				e.setChapterId(getLong(ls, "CHAPTER_ID", s));
				e.setStrandId(getLong(ls, "STRAND_ID", s));
				e.setSceneno(getInteger(ls, "SCENENO", s));
				e.setScenets(getTimestamp(ls, "SCENE_TS", s));
				e.setSummary(getString(ls, "SUMMARY", s));
				e.setStatus(getInteger(ls, "STATUS", s));
				e.setIntensity(getInteger(ls, "INTENSITY", s));
				e.setDuration(getString(ls, "DURATION", s));
				e.setRelativeDateDifference(getInteger(ls, "RELATIVE_DATE_DIFFERENCE", s));
				e.setRelativetime(getString(ls, "RELATIVE_TIME", s));
				e.setRelativesceneid(getLong(ls, "RELATIVE_SCENE_ID", s));
				e.setInformative(getBoolean(ls, "INFORMATIVE", s));
				e.setScenario_pitch(getString(ls, "SCENARIO_PITCH", s));
				e.setScenario_stage(getInteger(ls, "SCENARIO_STAGE", s));
				e.setScenario_start(getInteger(ls, "SCENARIO_START", s));
				e.setScenario_end(getInteger(ls, "SCENARIO_END", s));
				e.setScenario_moment(getInteger(ls, "SCENARIO_MOMENT", s));
				e.setScenario_loc(getInteger(ls, "SCENARIO_LOC", s));
				e.setOdf(getString(ls, "ODF", s));
				e.setNarratorId(getLong(ls, "NARRATOR_ID", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.scenes.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Statuss XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void statussCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = statusSt;
				Status e = new Status();
				getCommon(ls, e, s);
				e.setIcone(getString(ls, "ICONE", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.statuss.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Strands XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void strandsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = strandSt;
				Strand e = new Strand();
				getCommon(ls, e, s);
				e.setAbbreviation(getString(ls, "ABBREVIATION", s));
				e.setColor(getInteger(ls, "COLOR", s));
				e.setSort(getInteger(ls, "SORT", s));
				e.setAspect(getString(ls, "ASPECT", s));
				project.strands.add(e);
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * create the Items, Memos and Tags XML
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void tagsCreate(BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			List<String> s = getLine(line);
			if (!s.isEmpty()) {
				List<String> ls = tagSt;
				Integer ztype = getInteger(ls, "TYPE", s);
				if (ztype == AbsTag.TYPE_TAG) {
					AbsTag e = new Tag();
					getCommon(ls, e, s);
					e.setType(getInteger(ls, "TYPE", s));
					e.setCategory(getString(ls, "CATEGORY", s));
					project.tags.add(e);
				} else if (ztype == AbsTag.TYPE_ITEM) {
					Item e = new Item();
					getCommon(ls, e, s);
					e.setType(getInteger(ls, "TYPE", s));
					e.setCategory(getString(ls, "CATEGORY", s));
					project.items.add(e);
				}
				if (ztype == AbsTag.TYPE_MEMO) {
					Memo e = new Memo();
					getCommon(ls, e, s);
					e.setType(getInteger(ls, "TYPE", s));
					e.setCategory(getString(ls, "CATEGORY", s));
					project.memos.add(e);
				}
			}
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	/**
	 * get the structure of the SQL table
	 *
	 * @param reader
	 * @throws IOException
	 */
	private List<String> getStructure(BufferedReader reader) throws IOException {
		List<String> ls = new ArrayList<>();
		while (reader.ready()) {
			String line = getUTF8(reader);
			if (line.equals(");")) {
				break;
			}
			if (line.contains("\"")) {
				line = line.replace("\"", "");
			}
			String s = line.trim().substring(0, line.indexOf(" "));
			ls.add(s.trim());
		}
		return ls;
	}

	private void linksMemo(String vx, BufferedReader reader) throws IOException {
		while (reader.ready()) {
			String line = getUTF8(reader);
			links.add(vx + "@" + line);
			if (line.endsWith(";")) {
				break;
			}
		}
	}

	private void linksSet() throws IOException {
		String line = "";
		try {
			for (String sl : links) {
				line = sl;
				String values[] = sl.split("@");
				String head[] = values[0].split("_");
				switch (head[0]) {
					case "PERSON":
						personsLinks(head[1], values[1]);
						break;
					case "RELATIONSHIP":
						relationsLinks(head[1], values[1]);
						break;
					case "SCENE":
						scenesLinks(head[1], values[1]);
						break;
				}
			}
		} catch (IOException ex) {
			LOG.err(TT + "linkSet()" + ex.getLocalizedMessage());
			LOG.err("sl=\"" + line + "\"");
			LOG.err("trace", ex);
		}
	}

	private void personsLinks(String type, String line) throws IOException {
		String v[] = line.substring(1, line.lastIndexOf(")")).replace(" ", "").split(",");
		if (v.length == 2) {
			Long id = Long.valueOf(v[0]);
			Person s = project.persons.get(id);
			if (s == null) {
				LOG.err("relation ID '" + id.toString() + "' not found");
			} else {
				Long l = Long.valueOf(v[1]);
				switch (type) {
					case "ATTRIBUTE":
						//TODO add Attribute to delete links
						//s.getAttributesId().add(l);
						break;
					case "CATEGORIES":
						s.getCategoriesId().add(l);
						break;
				}
			}
		}
	}

	private void relationsLinks(String type, String line) throws IOException {
		//LOG.trace("relationsLinks(type=" + type + ", line=" + line + ")");
		String v[] = line.substring(1, line.lastIndexOf(")")).replace(" ", "").split(",");
		if (v.length == 2) {
			Long id = Long.valueOf(v[0]);
			Relation s = project.relations.get(id);
			if (s == null) {
				LOG.err("relation ID '" + id.toString() + "' not found");
			} else {
				Long l = Long.valueOf(v[1]);
				switch (type) {
					case "ITEMS":
						s.getItemsId().add(l);
						break;
					case "LOCATIONS":
						s.getLocationsId().add(l);
						break;
					case "PERSONS":
						s.getPersonsId().add(l);
						break;
				}
			}
		}
	}

	private void scenesLinks(String type, String line) throws IOException {
		//LOG.trace(TT + "scenesLinks(type="+type+", line len="+line.length()+")");
		Scenes scenes = project.scenes;
		String v[] = line.substring(1, line.lastIndexOf(")"))
				.replace(" ", "").split(",");
		if (v.length > 1) {
			Long id = Long.valueOf(v[0]);
			Scene s = scenes.get(id);
			if (s == null) {
				LOG.err("scene ID '" + id.toString() + "' not found");
			} else {
				Long l = Long.valueOf(v[1]);
				switch (type) {
					case "ITEM":
						s.getItemsId().add(l);
						break;
					case "LOCATION":
						s.getLocationsId().add(l);
						break;
					case "PERSON":
						s.getPersonsId().add(l);
						break;
					case "PLOT":
						s.getPlotsId().add(l);
						break;
					case "STRAND":
						s.getStrandsId().add(l);
						break;
					case "TAG":
						s.getTagsId().add(l);
						break;
				}
			}
		}
	}

}
