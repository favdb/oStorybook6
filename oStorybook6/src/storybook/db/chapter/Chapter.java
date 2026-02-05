/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.chapter;

import i18n.I18N;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.w3c.dom.Node;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.part.Part;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

@SuppressWarnings("serial")
public class Chapter extends AbstractEntity {

	private String title = "";
	private Part part;
	private Integer chapterno = 0;
	private Timestamp creationTime;
	private Timestamp objectiveTime;
	private Timestamp doneTime;
	private Integer objectiveChars = 0;
	private Integer words = 0;
	private Integer nbScenes = 0;
	private Integer chars = 0;
	private int intensity;
	private Long part_id = -1L;

	public Chapter() {
		super(Book.TYPE.CHAPTER, "111");
	}

	public Chapter(Part part, Integer chapterno, String title,
			String description, String notes,
			Timestamp creationTime, Timestamp objectiveTime,
			Timestamp doneTime, Integer objectiveChars) {
		this();
		this.part = part;
		this.chapterno = chapterno;
		setName(title);
		this.title = title;
		setDescription(description);
		setNotes(notes);
		this.creationTime = creationTime;
		this.objectiveTime = objectiveTime;
		this.doneTime = doneTime;
		this.objectiveChars = objectiveChars;
		setAssistant("");
	}

	public Chapter(Part part, Integer chapterno, String title) {
		this();
		this.part = part;
		this.chapterno = chapterno;
		setName(title);
		this.title = title;
		setDescription("");
		setNotes("");
		this.creationTime = new Timestamp(new Date().getTime());
		this.objectiveChars = 0;
		setAssistant("");
	}

	public Chapter(Long id, Part part, int number, String title) {
		this(part, number, title);
		setId(id);
	}

	public boolean hasPart() {
		return part != null;
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public Long getPartId() {
		return part_id;
	}

	public void setPartId(Long value) {
		this.part_id = value;
	}

	public Integer getChapterno() {
		return (this.chapterno == null ? -1 : this.chapterno);
	}

	public static Integer getNextNumber(List chapters) {
		int n = 0;
		for (Object c : chapters) {
			if (((Chapter) c).chapterno > n) {
				n = ((Chapter) c).chapterno;
			}
		}
		return (n + 1);
	}

	public static Chapter findNumber(List chapters, int n) {
		if (chapters == null || chapters.isEmpty()) {
			return (null);
		}
		for (Object obj : chapters) {
			Chapter c = (Chapter) obj;
			if (c.chapterno == n) {
				return c;
			}
		}
		return (null);
	}

	public void setChapterno(Integer chapterno) {
		this.chapterno = chapterno;
	}

	public String getChapternoStr() {
		String s = String.format("%02d", getChapterno());
		return s;
	}

	public String getDefaultName() {
		String s = String.format("P%02dC%02d", getPart().getNumber(), getChapterno());
		return s;
	}

	public String getTitle() {
		return title;
	}

	public String getTitle(int truncate) {
		return TextUtil.ellipsize(title, truncate);
	}

	public void setTitle(String title) {
		this.title = title;
		setName(title);
	}

	public String getNbScenes() {
		return (nbScenes.toString());
	}

	public void setNbScenes(Integer n) {
		nbScenes = n;
	}

	public String getNbWords() {
		return words.toString();
	}

	public void setNbWords(Integer n) {
		words = n;
	}

	public String getNbChars() {
		return chars.toString();
	}

	public void setNbChars(Integer n) {
		chars = n;
	}

	public boolean hasCreationTime() {
		return creationTime != null;
	}

	public void setCreationTime(Timestamp ts) {
		creationTime = ts;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public boolean hasObjectiveTime() {
		return objectiveTime != null;
	}

	public void setObjectiveTime(Timestamp ts) {
		objectiveTime = ts;
	}

	public Timestamp getObjectiveTime() {
		return objectiveTime;
	}

	public boolean isDone() {
		return hasDoneTime();
	}

	public boolean hasDoneTime() {
		return doneTime != null;
	}

	public void setDoneTime(Timestamp ts) {
		doneTime = ts;
	}

	public Timestamp getDoneTime() {
		return doneTime;
	}

	public Integer getObjectiveChars() {
		return (this.objectiveChars == null ? 0 : this.objectiveChars);
	}

	public void setObjectiveChars(Integer objectiveChars) {
		this.objectiveChars = objectiveChars;
	}

	@Override
	public String toString() {
		if (chapterno == null) {
			return I18N.getMsg("scenes.unassigned") + (hasNotes() ? "*" : "");
		}
		return (getChapterno() + ": " + getName() + (hasNotes() ? "*" : ""));
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(part)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(chapterno)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getName())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(creationTime)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(objectiveTime)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(doneTime)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(objectiveChars)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getDescription())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getNotes())).append(quoteEnd).append("\n");
		return (b.toString());
	}

	@Override
	public String toHtml() {
		return (toCsv("<td>", "</td>", "\n"));
	}

	@Override
	public String getName() {
		if (super.getName() == null) {
			setName("");
		}
		if (super.getName().isEmpty() && title != null && !title.isEmpty()) {
			setName(title);
		}
		return super.getName();
	}

	@Override
	public String getFullName() {
		return getChapternoStr() + " " + getName();
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(super.toDetailHeader(detailed));
		b.append(getInfo(detailed, DATA.NUMBER, getChapterno()));
		b.append(getInfo(detailed, DATA.PART, getPart()));
		if (getObjectiveTime() != null || detailed > 1) {
			StringBuilder bx = new StringBuilder();
			bx.append(Html.TABLE_B).append(Html.TR_B);
			bx.append(getInfo(detailed, DATA.OBJECTIVE_DATE, getObjectiveTime()));
			bx.append(getInfo(detailed, DATA.OBJECTIVE_DONE, getDoneTime()));
			bx.append(getInfo(detailed, DATA.OBJECTIVE_SIZE, getObjectiveChars()));
			bx.append(Html.TR_E).append(Html.TABLE_E);
			b.append(getInfo(detailed, DATA.OBJECTIVE, bx.toString()));
		}
		b.append(super.toDetailFooter(detailed));
		return b.toString();
	}

	@Override
	public String toText() {
		return (toCsv("", "", "\t"));
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.PART, getPart()));
		b.append(XmlUtil.setAttribute(0, XK.NUMBER, chapterno));
		if (getObjectiveTime() != null) {
			b.append(XmlUtil.setAttribute(8, XK.OBJECTIVEDATE, getObjectiveTime()));
			b.append(XmlUtil.setAttribute(0, XK.OBJECTIVEDONE, getDoneTime()));
			b.append(XmlUtil.setAttribute(0, XK.OBJECTIVECHARS, getObjectiveChars()));
		}
		b.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	public static Chapter fromXml(Node node) {
		Chapter p = new Chapter();
		fromXmlBeg(node, p);
		p.setChapterno(XmlUtil.getInteger(node, XK.NUMBER));
		p.setPartId(XmlUtil.getLong(node, "part"));
		p.setTitle(XmlUtil.getString(node, XK.NAME).replace("''", "\""));
		if (XmlUtil.getTimestamp(node, XK.OBJECTIVEDATE) != null) {
			p.setObjectiveTime(XmlUtil.getTimestamp(node, XK.OBJECTIVEDATE));
			p.setDoneTime(XmlUtil.getTimestamp(node, XK.OBJECTIVEDONE));
			p.setObjectiveChars(XmlUtil.getInteger(node, XK.OBJECTIVECHARS));
		}
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Chapter test = (Chapter) obj;
		boolean ret = true;
		ret = ret && equalsObjectNullValue(part, test.getPart());
		ret = ret && equalsIntegerNullValue(chapterno, test.getChapterno());
		ret = ret && equalsStringNullValue(getName(), test.getName());
		ret = ret && equalsStringNullValue(getDescription(), test.getDescription());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(),
				part,
				chapterno,
				creationTime,
				objectiveChars,
				objectiveTime,
				doneTime);
	}

	@Override
	public int compareTo(AbstractEntity ch) {
		return chapterno.compareTo(((Chapter) ch).chapterno);
	}

	public static Chapter find(List<Chapter> list, String str) {
		for (Chapter elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Chapter find(List<Chapter> list, Long id) {
		for (Chapter elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.CHAPTER);
		list.add("title, 256");
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "chapter";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",title,String,256");
		ls.add(tableName + ",part_id,Integer,0");
		ls.add(tableName + ",chapterno,Integer,0");
		ls.add(tableName + ",creation_ts,Time,0");
		ls.add(tableName + ",objective_ts,Time,0");
		ls.add(tableName + ",done_ts,Time,0");
		ls.add(tableName + ",objective_ch,Integer,0");
		return (ls);
	}

	/**
	 * get the identification like PpCcc where p is the Part ID and cc is the Chapter ID
	 *
	 * @return
	 */
	public String getIdent() {
		StringBuilder ident = new StringBuilder();
		if (hasPart()) {
			ident.append(getPart().getIdent());
		} else {
			ident.append("P0");
		}
		ident.append(String.format("C%02d", getId()));
		return ident.toString();
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Chapter ne = new Chapter();
		doCopyTo(m, ne);
		ne.setChapterno(0);
		ne.setPart(getPart());
		return ne;
	}

}
