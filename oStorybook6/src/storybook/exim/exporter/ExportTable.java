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
package storybook.exim.exporter;

import i18n.I18N;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import storybook.App;
import storybook.Pref;
import storybook.db.DB.DATA;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import storybook.db.book.BookParamExport;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.tools.LOG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;

/**
 * class for exporting a table
 *
 * @author favdb
 */
public class ExportTable extends AbstractExport {
	//TODO à réécrire pour profiter pleinement de getColumns et getRow

	/**
	 * export a table
	 *
	 * @param mainFrame
	 * @param format
	 */
	public ExportTable(MainFrame mainFrame, String format) {
		super(mainFrame, format);
	}

	enum KW {
		ID,
		NAME,
		TITLE,
		BLURB,
		DESCRIPTION,
		NOTES,
		ASSISTANT,
		STATUS,
		CATEGORY,
		EXPORT,
		DATABASE,
		XML,
		HTML,
		KEY,
		VALUE,
		STEP,
		POLTI,
		PARAM,
		BACKUP,
		LAYOUT,
		DIRECTORY,
		AUTO,
		INCREMENT,
		EDITOR,
		MODLESS,
		XUSE,
		EXTEND,
		TEMPLATE,
		IMPORT, FILE,
		SCENE_START, SCENE_END,
		LOCATION,
		PERSON,
		ADRESS,
		CITY,
		COUNTRY,
		ALTITUDE,
		NUMBER,
		DATE,
		COLOR,
		SORT;

		@Override
		public String toString() {
			return this.name().toLowerCase().replace("_", ".");
		}
	}

	public static void exportDB(MainFrame m, String format) {
		ExportTable export = new ExportTable(m, format);
		export.writeDataBase();
	}

	public static void exportTable(MainFrame m, SbView view, String format) {
		//App.printInfos("ExportTable.exportTable(mainFrame, view="+view.toString()+", f="+f+")");
		exportTable(m, view.getName(), format);
	}

	public static void exportTable(MainFrame m, String viewName, String format) {
		//App.printInfos("ExportTable.exportTable(mainFrame, v="+v+", f="+f+")");
		ExportTable export = new ExportTable(m, format);
		if (export.askFileExists(viewName)
		   && JOptionPane.showConfirmDialog(m,
			  I18N.getMsg("export.replace", export.param.getFileName()),
			  I18N.getMsg(KW.EXPORT.toString()),
			  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (!export.openFile("table_" + viewName, false)) {
			return;
		}
		export.writeTable(viewName);
		export.closeFile(true);
	}

	public void setDir(String d) {
		param.setDirectory(d);
	}

	public void setFormat(String f) {
		param.setFormat(f);
	}

	public void writeTable(String name) {
		//App.printInfos("ExportTable.writeTable(name="+name+")");
		if (!isOpened) {
			return;
		}
		switch (Book.getTYPE(name)) {
			case ATTRIBUTE:
			case ATTRIBUTES:
				writeAttributes();
				break;
			case CATEGORY:
			case CATEGORIES:
				writeCategories();
				break;
			case CHAPTER:
			case CHAPTERS:
				writeChapters();
				break;
			case ENDNOTE:
			case ENDNOTES:
				writeEndnotes();
				break;
			case EVENT:
			case EVENTS:
				writeEvents();
				break;
			case GENDER:
			case GENDERS:
				writeGenders();
				break;
			case IDEA:
			case IDEAS:
				writeIdeas();
				break;
			case ITEM:
			case ITEMS:
				writeItems();
				break;
			case LOCATION:
			case LOCATIONS:
				writeLocations();
				break;
			case MEMO:
			case MEMOS:
				writeMemos();
				break;
			case PART:
			case PARTS:
				writeParts();
				break;
			case PERSON:
			case PERSONS:
				writePersons();
				break;
			case RELATION:
			case RELATIONS:
				writeRelations();
				break;
			case SCENE:
			case SCENES:
				writeScenes();
				break;
			case STRAND:
			case STRANDS:
				writeStrands();
				break;
			case TAG:
			case TAGS:
				writeTags();
				break;
			default:
				LOG.err("unknown type to export " + name);
				break;
		}
		if (KW.HTML.toString().equals(param.getFormat())) {
			writeText("    </table>\n");
		}
	}

	private void writeAttributes() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.ATTRIBUTE_KEY.i18n()), 10));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.ATTRIBUTE_VALUE.i18n()), 80));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Attribute> entities = (List) mainFrame.project.getList(Book.TYPE.ATTRIBUTE);
		for (Attribute e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeCategories() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.NAME.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.SORT.i18n()), 5));
		headers.add(new ColumnHeader(I18N.getMsg("category.value"), 80));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Category> entities = (List) mainFrame.project.getList(Book.TYPE.CATEGORY);
		for (Category e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeChapters() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.PART.i18n()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.CHAPTER_CHAPTERNO.i18n()), 2));
		headers.add(new ColumnHeader(I18N.getMsg(KW.TITLE.toString()), 60));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.DATE_CREATION.i18n()), 10));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.OBJECTIVE_DATE.i18n()), 10));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.OBJECTIVE_DONE.i18n()), 10));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.OBJECTIVE_SIZE.i18n()), 6));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DESCRIPTION.toString()), 30));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 30));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Chapter> entities = (List) mainFrame.project.getList(Book.TYPE.CHAPTER);
		for (Chapter e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeGenders() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 32));
		headers.add(new ColumnHeader(I18N.getMsg("gender.childhood"), 3));
		headers.add(new ColumnHeader(I18N.getMsg("gender.adolescence"), 3));
		headers.add(new ColumnHeader(I18N.getMsg("gender.adulthood"), 3));
		headers.add(new ColumnHeader(I18N.getMsg("gender.retirement"), 3));
		headers.add(new ColumnHeader(I18N.getMsg("icone"), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Gender> entities = (List) mainFrame.project.getList(Book.TYPE.GENDER);
		for (Gender e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeIdeas() {
		@SuppressWarnings("unchecked")
		List<Idea> entities = (List) mainFrame.project.getList(Book.TYPE.IDEA);
		//writeText("nb ideas="+entities.size());
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.STATUS.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.CATEGORY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		for (Idea e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeInfo() {
		writeText("    <info \n");
		StringBuilder b = new StringBuilder();
		b.append(stringAttribute(2, KW.TITLE.toString(), book.getTitle())).append("\n");
		b.append(stringAttribute(2, "subtitle", book.getSubtitle())).append("\n");
		b.append(stringAttribute(2, "author", book.getAuthor())).append("\n");
		b.append(stringAttribute(2, "copyright", book.getCopyright())).append("\n");
		writeText(b.toString() + "    >\n");
		String v = book.info.assistantGet();
		if (!v.isEmpty()) {
			writeXml(2, KW.ASSISTANT.toString(), "");
			writeXml(3, v, "");
		}
		//blurb
		v = book.getBlurb();
		if (!v.isEmpty()) {
			writeXml(2, "blurb", v);
		}
		//notes
		v = book.getNotes();
		if (!v.isEmpty()) {
			writeXml(2, KW.NOTES.toString(), v);
		}
		writeXml(1, "/info", "");
	}

	private void writeParam() {
		writeXml(1, KW.PARAM.toString(), "");
		writeParamBackup();
		writeParamEditor();
		writeParamExport();
		writeParamImport();
		writeParamLayout();
		writeXml(1, "/" + KW.PARAM.toString(), "");
	}

	private void writeParamBackup() {
		StringBuilder b;
		b = new StringBuilder();
		b.append(stringAttribute(0, KW.DIRECTORY.toString(),
		   App.preferences.getString(Pref.KEY.BACKUP_DIR, "")));
		b.append(stringAttribute(0, KW.AUTO.toString(),
		   App.preferences.getBoolean(Pref.KEY.BACKUP_OPEN)));
		b.append(stringAttribute(0, KW.INCREMENT.toString(),
		   App.preferences.getBoolean(Pref.KEY.BACKUP_INCREMENT)));
		writeXmlAttribute(2, KW.BACKUP.toString(), b.toString());
	}

	private void writeParamEditor() {
		StringBuilder b;
		b = new StringBuilder();
		b.append(stringAttribute(0, KW.MODLESS.toString(),
		   App.preferences.editorGetModless()));
		if (book.isXeditorUse()) {
			b.append(stringAttribute(0, KW.XUSE.toString(),
			   book.isXeditorUse()));
			b.append(stringAttribute(0, KW.EXTEND.toString(),
			   book.getParam().getParamEditor().getExtension()));
		}
		writeXmlAttribute(2, KW.EDITOR.toString(), b.toString());
	}

	private void writeParamExport() {
		BookParamExport paramBook = mainFrame.getBook().getParam().getParamExport();
		StringBuilder b = new StringBuilder();
		b.append(stringAttribute(0, KW.DIRECTORY.toString(), paramBook.getDirectory()));
		b.append(stringAttribute(13, KW.TITLE.toString(), param.getLayout().getPartTitle()));
		b.append(stringAttribute(13, "csvcomma", param.getCsvComma()));
		b.append(stringAttribute(0, "csvquote", param.getCsvQuote()));
		b.append(stringAttribute(13, "txttab", param.getTxtSeparator()));
		b.append(stringAttribute(13, "htmlmultichapter", paramBook.getHtmlMultiChapter()));
		b.append(stringAttribute(0, "htmlmultiscene", paramBook.getHtmlMultiScene()));
		b.append(stringAttribute(0, "htmlnav", paramBook.getHtmlNav()));
		b.append(stringAttribute(0, "htmlnavimage", paramBook.getHtmlNavImage()));
		b.append(stringAttribute(0, "htmlcss", paramBook.getHtmlCss()));
		b.append(stringAttribute(13, "highlight", paramBook.getHighlight()));
		writeXmlAttribute(2, KW.EXPORT.toString(), b.toString());
	}

	private void writeParamImport() {
		StringBuilder b = new StringBuilder();
		b.append(stringAttribute(0, KW.DIRECTORY.toString(),
		   App.preferences.getString(Pref.KEY.IMP_DIRECTORY)));
		b.append("\n");
		b.append(stringAttribute(3, KW.FILE.toString(),
		   App.preferences.getString(Pref.KEY.IMP_FILE)));
		writeXmlAttribute(2, KW.IMPORT.toString(), b.toString());
	}

	private void writeParamLayout() {
		BookParamExport paramBook = mainFrame.getBook().getParam().getParamExport();
		StringBuilder b = new StringBuilder();
		b.append(stringAttribute(0, "booktitle", paramBook.getHtmlChapterTitle()));
		b.append(stringAttribute(0, "booklayout", param.getLayout().getBookLayout()));
		b.append(stringAttribute(0, "breakpage", paramBook.getHtmlChapterBreakPage()));
		writeXmlAttribute(2, "layout", b.toString());
	}

	private void writeItems() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.CATEGORY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.STATUS.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DESCRIPTION.toString()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Item> entities = (List) mainFrame.project.getList(Book.TYPE.ITEM);
		for (Item e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}

	}

	private void writeLocations() {
		//App.printInfos("ExportTable.writeLocations()");
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.ADRESS.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.CITY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.COUNTRY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.ALTITUDE.toString()), 8));
		headers.add(new ColumnHeader(I18N.getMsg("location.site"), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DESCRIPTION.toString()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Location> entities = (List) mainFrame.project.getList(Book.TYPE.LOCATION);
		for (Location e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeMemos() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Memo> entities = (List) mainFrame.project.getList(Book.TYPE.MEMO);
		for (Memo e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}

	}

	private void writeParts() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NUMBER.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg("part"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("manage.date.creation"), 10));
		headers.add(new ColumnHeader(I18N.getMsg("manage.date.objective"), 10));
		headers.add(new ColumnHeader(I18N.getMsg("manage.date.done"), 10));
		headers.add(new ColumnHeader(I18N.getMsg("manage.size.objective"), 6));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Part> entities = (List) mainFrame.project.getList(Book.TYPE.PART);
		for (Part e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writePersons() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg("gender"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("person.firstname"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("person.lastname"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("person.abbr"), 5));
		headers.add(new ColumnHeader(I18N.getMsg("person.birthday"), 10));
		headers.add(new ColumnHeader(I18N.getMsg("person.death"), 10));
		headers.add(new ColumnHeader(I18N.getMsg("person.occupation"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("person.color"), 10));
		headers.add(new ColumnHeader(I18N.getMsg(KW.CATEGORY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg("attributes"), 10));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DESCRIPTION.toString()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Person> entities = (List) mainFrame.project.getList(Book.TYPE.PERSON);
		for (Person e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeRelations() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg("person.first"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("person.second"), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.SCENE_START.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.SCENE_END.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg("persons"), 32));
		headers.add(new ColumnHeader(I18N.getMsg("items"), 32));
		headers.add(new ColumnHeader(I18N.getMsg("locations"), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DESCRIPTION.toString()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Relation> entities = (List) mainFrame.project.getList(Book.TYPE.RELATION);
		for (Relation e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeScenes() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.TITLE.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.CHAPTER.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.STRAND.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NUMBER.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DATE.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.STATUS.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.INFORMATIVE.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.DURATION.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.INTENSITY.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENE_NARRATOR.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.PERSONS.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.ITEMS.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.LOCATIONS.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.PLOTS.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.STRANDS.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENE_RELATIVETIME.i18n()), 16));
		headers.add(new ColumnHeader(I18N.getMsg("scene.relative"), 16));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENE_SUMMARY.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENE_XEDITOR.i18n()), 32));
		if (book.info.scenarioGet()) {
			headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENARIO_STAGE.i18n()), 16));
			headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENARIO_PITCH.i18n()), 16));
			headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENARIO_LOC.i18n()), 16));
			headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENARIO_MOMENT.i18n()), 16));
			headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENARIO_START.i18n()), 16));
			headers.add(new ColumnHeader(I18N.getMsg(DATA.SCENARIO_END.i18n()), 16));
		}
		headers.add(new ColumnHeader(I18N.getMsg(DATA.DESCRIPTION.i18n()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Scene> entities = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		for (Scene e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeStrands() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg("strand.abbr"), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.COLOR.toString()), 8));
		headers.add(new ColumnHeader(I18N.getMsg(KW.SORT.toString()), 10));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Strand> entities = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		for (Strand e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeTags() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.CATEGORY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.STATUS.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.DESCRIPTION.toString()), 32));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Tag> entities = (List) mainFrame.project.getList(Book.TYPE.TAG);
		for (Tag e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	private void writeEndnotes() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NAME.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg("endnote.type"), 8));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NUMBER.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Endnote> entities = (List) mainFrame.project.getList(Book.TYPE.ENDNOTE);
		for (Endnote e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}

	}

	private void writeEvents() {
		List<ColumnHeader> headers = new ArrayList<>();
		headers.add(new ColumnHeader(I18N.getMsg(KW.ID.toString()), 5));
		headers.add(new ColumnHeader(I18N.getMsg(KW.TITLE.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg("timeevent.date"), 16));
		headers.add(new ColumnHeader(I18N.getMsg("timeevent.combo.label"), 8));
		headers.add(new ColumnHeader(I18N.getMsg(KW.CATEGORY.toString()), 16));
		headers.add(new ColumnHeader(I18N.getMsg(KW.NOTES.toString()), 32));
		writeHeaderColumn(headers);
		@SuppressWarnings("unchecked")
		List<Event> entities = (List) mainFrame.project.getList(Book.TYPE.EVENT);
		for (Event e : entities) {
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeText(e.toXml());
					break;
				case F_HTML:
					writeHtml(e.toHtml());
					break;
				case F_CSV:
					writeText(e.toCsv(param.getCsvQuote(), param.getCsvQuote(), param.getCsvComma()));
					break;
				case F_TXT:
					writeText(e.toText());
					break;
				default:
					break;
			}
		}
	}

	public void writeDataBase() {
		String viewName = "DataBase";
		if (!openFile(viewName, false)) {
			return;
		}
		writeInfo();
		writeParam();
		writeAttributes();
		writeCategories();
		writeGenders();
		writeItems();
		writeLocations();
		writePersons();
		writeTags();
		writeIdeas();
		writeMemos();
		writeRelations();
		writeEvents();
		writeStrands();
		writeParts();
		writeChapters();
		writeScenes();
		writeEndnotes();
		closeFile(VERBOSE);
	}

	private void writeHeaderColumn(List<ColumnHeader> headers) {
		//App.printInfos("ExportTable.writeHeaderColumn(headers="+headers.size()+" columns");
		if (headers == null || headers.isEmpty()) {
			return;
		}
		switch (param.getFormat()) {
			case F_HTML:
				writeText("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n<tr>\n");
				for (ColumnHeader header : headers) {
					writeColumn(header.getName(), header.getSize());
				}
				writeText("</tr>\n");
				break;
			case F_ZXML:
			case F_XML: //no headers
				break;
			case F_CSV:
				for (ColumnHeader header : headers) {
					writeText(param.getCsvQuote() + header.getName() + param.getCsvQuote() + param.getCsvComma());
				}
				writeText("\n");
				break;
			case F_TXT:
				for (ColumnHeader header : headers) {
					writeText(header.getName() + param.getTxtSeparator());
				}
				writeText("\n");
				break;
			default:
				break;
		}
	}

	private void writeColumn(String name, int size) {
		switch (param.getFormat()) {
			case F_HTML:
				if (size > 0) {
					writeText("    <td width=\"" + size + "%\">" + ("".equals(name) ? "&nbsp" : name) + "</td>\n");
				} else {
					writeText("    <td>" + name + "</td>\n");
				}
				break;
			case F_CSV:
				writeText("\"" + name + "\";");
				break;
			case F_TXT:
				writeText("\t" + name + "");
				break;
			default:
				break;
		}
	}

	public class ColumnHeader {

		private final String name;
		private final int size;

		ColumnHeader(String n, int s) {
			name = n;
			size = s;
		}

		public String getName() {
			return (name);
		}

		public int getSize() {
			return (size);
		}

	}
}
