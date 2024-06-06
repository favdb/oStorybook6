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
package storybook.db.idea;

import i18n.I18N;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.status.Status;
import storybook.tools.LOG;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

public class Idea extends AbstractEntity {

	public enum STATUS {
		NOT_STARTED,
		STARTED,
		COMPLETED,
		ABANDONED
	};

	private Integer status = 0;
	private String category = "";
	private String uuid = "";

	public Idea() {
		super(Book.TYPE.IDEA, "010");
	}

	public Idea(ResultSet rs) {
		super(Book.TYPE.IDEA, "010", rs);
		try {
			status = rs.getInt("status");
			category = rs.getString("category");
			uuid = rs.getString("uuid");
		} catch (SQLException ex) {
			//empty
		}
	}

	public Idea(Integer status, String note, String category) {
		this();
		this.status = status;
		setNotes(note);
		this.category = category;
	}

	/**
	 * reading idea from a Xml node
	 *
	 * @param node
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Idea(Node node) {
		this();
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element) node;
			String nid = el.getAttribute("id");
			Long lid;
			try {
				lid = Long.valueOf(nid);
			} catch (NumberFormatException ex) {
				LOG.err("invalid id attribute ='" + nid + "'");
				lid = -1L;
			}
			setId(lid);
			setCreation(el.getAttribute("creation"));
			setMaj(el.getAttribute("maj"));
			setName(el.getAttribute("name"));
			setUuid(el.getAttribute("uuid"));
			setStatus(Integer.valueOf(el.getAttribute("status")));
			setCategory(el.getAttribute("category"));
			setNotes(el.getTextContent());
		}
	}

	@Override
	public String getName() {
		if (super.getName().isEmpty()) {
			setName(I18N.getMsg("idea") + " " + getId());
		}
		return super.getName();
	}

	public String getUuid() {
		return (uuid == null ? "" : uuid);
	}

	public void setUuid(String value) {
		uuid = value;
	}

	public void setUuid() {
		uuid = UUID.randomUUID().toString();
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		String str = getStatus().toString() + " (" + Status.getStatusMsg(getStatus()) + ")";
		b.append(getInfo(detailed, "status", str));
		b.append(getInfo(detailed, "uuid", getUuid()));
		b.append(getInfo(detailed, "category", getCategory()));
		if (detailed == 0) {
			b.append(getInfo(2, "notes", getNotes()));
		}
		b.append(toDetailFooter(detailed));
		return (b.toString());
	}

	@Override
	public String toCsv(String quoteSart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteSart).append(getId().toString()).append(quoteEnd).append(separator);
		b.append(quoteSart).append(getUuid()).append(quoteEnd).append(separator);
		b.append(quoteSart).append(getStatus().toString()).append(quoteEnd).append(separator);
		b.append(quoteSart).append(getCategory()).append(quoteEnd).append(separator);
		b.append(quoteSart).append(hasNotes() ? getNotes() : "").append(quoteEnd).append("\n");
		return (b.toString());
	}

	@Override
	public String toHtml() {
		return (toCsv("<td>", "</td>", "\n"));
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String toText() {
		return (toCsv("", "", "\t"));
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.UUID, getUuid()));
		b.append(XmlUtil.setAttribute(0, XK.STATUS, getStatus()));
		b.append(XmlUtil.setAttribute(0, XK.CATEGORY, getCategory()));
		b.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	public static Idea fromXml(Node node) {
		Idea p = new Idea();
		fromXmlBeg(node, p);
		p.setUuid(XmlUtil.getString(node, XK.UUID));
		p.setStatus(XmlUtil.getInteger(node, XK.STATUS));
		p.setCategory(XmlUtil.getString(node, XK.CATEGORY));
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Idea) || !super.equals(obj)) {
			return false;
		}
		Idea test = (Idea) obj;
		boolean ret = true;
		ret = ret && equalsLongNullValue(id, test.getId());
		ret = ret && equalsStringNullValue(name, test.getName());
		ret = ret && equalsStringNullValue(uuid, test.getUuid());
		ret = ret && equalsIntegerNullValue(status, test.getStatus());
		ret = ret && equalsStringNullValue(category, test.getCategory());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		return ret;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 31 + (id != null ? id.hashCode() : 0);
		hash = hash * 31 + (name != null ? name.hashCode() : 0);
		hash = hash * 31 + (uuid != null ? uuid.hashCode() : 0);
		hash = hash * 31 + (status != null ? status.hashCode() : 0);
		hash = hash * 31 + (category != null ? category.hashCode() : 0);
		hash = hash * 31 + (getNotes() != null ? getNotes().hashCode() : 0);
		return hash;
	}

	public static Idea find(List<Idea> list, String str) {
		for (Idea elem : list) {
			if (elem.getName().equals(str)) {
				return elem;
			}
		}
		return null;
	}

	public static Idea findByUuid(List<Idea> list, String str) {
		for (Idea elem : list) {
			if (elem.getUuid().equals(str)) {
				return elem;
			}
		}
		return null;
	}

	public static Idea find(List<Idea> list, Long id) {
		for (Idea elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> findCategories(MainFrame mainFrame) {
		@SuppressWarnings("unchecked")
		List<Idea> events = (List) mainFrame.project.getList(Book.TYPE.IDEA);
		List<String> list = new ArrayList<>();
		events.forEach((e) -> {
			if (!list.contains(e.getCategory())) {
				list.add(e.getCategory());
			}
		});
		return (list);
	}

	public Element toXml(Document doc, Element element) {
		if (doc == null) {
			return null;
		}
		Element el = element;
		if (element == null) {
			el = doc.createElement("idea");
		}
		if (el != null) {
			el.setAttribute("id", getId().toString());
			el.setAttribute("creation", getCreation());
			el.setAttribute("maj", getMaj());
			el.setAttribute("uuid", getUuid());
			el.setAttribute("name", getName());
			el.setAttribute("status", getStatus().toString());
			el.setAttribute("category", getCategory());
			el.setTextContent(getNotes());
		}
		return el;
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.IDEA);
		list.add("category, 256");
		return list;
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "ideas";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",status,Integer,0");
		ls.add(tableName + ",uuid,String,36");
		ls.add(tableName + ",category,String,256");
		return (ls);
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Idea ne = new Idea();
		doCopyTo(m, ne);
		ne.setCategory(getCategory());
		ne.setStatus(getStatus());
		ne.setUuid();
		return ne;
	}

}
