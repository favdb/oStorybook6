/*
Copyright 2018 by FaVdB
Modifications ???? by ????

This file is part of oStorybook.

    oStorybook is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.

    oStorybook is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with oStorybook.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import java.io.File;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author FaVdB
 */
public class BookParamEditor extends BookParamAbstract {

	public enum KW {
		MODLESS, NAME, TEMPLATE, EXT, XUSE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private boolean modless;
	private boolean use;
	private String name;
	private String extension;
	private String template;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamEditor(BookParam param) {
		super(param, "editor");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("editor");
		}
		init();
	}

	public void setModless(boolean b) {
		modless = b;
	}

	public boolean getModless() {
		return modless;
	}

	public void setUse(boolean value) {
		use = value;
	}

	public boolean getUse() {
		return use;
	}

	public void setName(String d) {
		name = d;
	}

	public String getName() {
		return name == null ? "" : name;
	}

	public void setExtension(String d) {
		extension = d.toLowerCase();
	}

	public String getExtension() {
		return (extension == null ? "" : extension.toLowerCase());
	}

	public void setTemplate(String d) {
		template = d;
	}

	public String getTemplate() {
		return template;
	}

	@Override
	protected void init() {
		if (node != null) {
			setModless(getBoolean(node, KW.MODLESS.toString()));
			String str = getString(node, KW.TEMPLATE.toString());
			boolean exists = false;
			if (!str.isEmpty()) {
				File f = new File(str);
				exists = f.exists();
			}
			if (getBoolean(node, KW.XUSE.toString()) && exists) {
				setUse(getBoolean(node, KW.XUSE.toString()));
				setName(getString(node, KW.NAME.toString()));
				setExtension(getString(node, KW.EXT.toString()));
				setTemplate(str);
			} else {
				setUse(false);
				setTemplate("");
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append("        <editor ");
		b.append(setAttribute(0, KW.MODLESS.toString(), modless));
		b.append(setAttribute(0, KW.XUSE.toString(), use));
		b.append(setAttribute(0, KW.NAME.toString(), name));
		b.append(setAttribute(0, KW.EXT.toString(), extension));
		b.append(setAttribute(12, KW.TEMPLATE.toString(), template));
		b.append("/>\n");
		return b.toString();
	}

	@Override
	public String toString() {
		return toXml();
	}

}
