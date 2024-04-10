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
package storybook.db.tag;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;

public abstract class AbsTag extends AbstractEntity {

	public static final int TYPE_TAG = 0;
	public static final int TYPE_ITEM = 1;
	public static final int TYPE_LINK = 10;
	public static final int TYPE_MEMO = 20;
	public static final int TYPE_RESOURCE = 30;

	protected Integer type = TYPE_ITEM;
	private String category = "";

	public AbsTag() {
		super(Book.TYPE.TAG, "000");
		this.type = TYPE_TAG;
	}

	public AbsTag(Book.TYPE objtype, String common) {
		super(objtype, common);
	}

	public AbsTag(Integer type, String category, String name, String description, String notes) {
		this();
		this.type = type;
		this.category = category;
		setName(name);
		setDescription(description);
		setNotes(notes);
		setAssistant("");
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getCategory() {
		return (category != null ? category : "");
	}

	public void setCategory(String category) {
		this.category = (category == null ? "" : category);
	}

	public boolean hasCategory() {
		return (category != null && !category.isEmpty());
	}

	@Override
	public String getFullName() {
		return getName() + (hasCategory() ? " (" + category + ")" : "");
	}

	@Override
	public Icon getIcon() {
		/*if (icone != null) {
			return new ImageIcon(getIcone());
		}*/
		if (type == null || type == TYPE_ITEM) {
			return (IconUtil.getIconSmall(ICONS.K.ENT_ITEM));
		} else if (type == TYPE_MEMO) {
			return (IconUtil.getIconSmall(ICONS.K.ENT_MEMO));
		}
		return (IconUtil.getIconSmall(ICONS.K.ENT_TAG));
	}

	public Icon getImageIcon(int h, int l) {
		ImageIcon ic = (ImageIcon) (IconUtil.getIconSmall(ICONS.K.ENT_ITEM));
		if (null != type) {
			switch (type) {
				case TYPE_ITEM:
					ic = (ImageIcon) (IconUtil.getIconSmall(ICONS.K.ENT_ITEM));
					break;
				case TYPE_MEMO:
					ic = (ImageIcon) (IconUtil.getIconSmall(ICONS.K.ENT_MEMO));
					break;
				default:
					ic = (ImageIcon) IconUtil.getIconSmall(ICONS.K.ENT_TAG);
					break;
			}
		}
		return (IconUtil.resizeIcon(ic, new Dimension(h, l)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getId().toString()).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getName()).append(quoteEnd).append(separator);
		if (getType() == TYPE_MEMO) {
			b.append(quoteStart).append(getClean(getNotes())).append(quoteEnd).append("\n");
		} else {
			b.append(quoteStart).append(getCategory()).append(quoteEnd).append(separator);
			//b.append(quoteStart).append(getIcone()).append(quoteEnd).append(separator);
			b.append(quoteStart).append(getDescription()).append(quoteEnd).append(separator);
			b.append(quoteStart).append(getClean(getNotes())).append(quoteEnd);
		}
		b.append("\n");
		return (b.toString());
	}

	@Override
	public String toHtml() {
		return (toCsv("<td>", "</td>", "\n"));
	}

	@Override
	public String toText() {
		return (toCsv("", "", "\t"));
	}

	/**
	 * get detailed informations
	 *
	 * @param detailed
	 *
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		if (getObjType() != Book.TYPE.MEMO) {
			b.append(getInfo(2, "category", getCategory()));
		}
		if (detailed == 0) {
			b.append(getInfo(2, L_DESCRIPTION, getDescription()));
		} else {
			b.append(super.toDetailFooter(detailed));
		}
		return (b.toString());
	}

	/**
	 * get the XML String
	 *
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String toXml() {
		String typestr;
		switch (getType()) {
			case 0:
				typestr = "tag";
				break;
			case 1:
				typestr = "item";
				break;
			case 10:
				typestr = "link";
				break;
			case 20:
				typestr = "memo";
				break;
			default:
				typestr = "tag";
				break;
		}
		StringBuilder b = new StringBuilder(toXmlBeg());
		if (!typestr.equals("memo") && !getCategory().isEmpty()) {
			b.append(XmlUtil.setAttribute(0, XK.CATEGORY, getCategory()));
		}
		b.append(">\n");
		b.append(toXmlEnd());
		return XmlUtil.beautify(b.toString());
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (obj == null) {
			return (false);
		}
		if (!super.equals(obj)) {
			return false;
		}
		AbsTag test = (AbsTag) obj;
		if (!Objects.equals(type, test.type)) {
			return false;
		}
		boolean ret = true;
		ret = ret && equalsStringNullValue(getName(), test.getName());
		ret = ret && equalsStringNullValue(category, test.getCategory());
		ret = ret && equalsStringNullValue(getDescription(), test.getDescription());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		return ret;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 31 + (type != null ? type.hashCode() : 0);
		hash = hash * 31 + (category != null ? category.hashCode() : 0);
		return hash;
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "tag";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",type,Integer,0");
		ls.add(tableName + ",category,String,255");
		return (ls);
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.ITEM);
		list.add("category, 256");
		return (list);
	}

}
