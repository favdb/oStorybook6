/**
 * Copyright 2020 by FaVdB
 *
 * This file is part of oStorybook.
 *
 *  oStorybook is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License.
 *
 *  oStorybook is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with oStorybook. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package storybook.db.book;

import java.io.File;
import storybook.tools.file.IOUtil;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author FaVdB
 */
public class BookParamImport extends BookParamAbstract {

	public enum KW {
		DIRECTORY, FILE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private String directory;
	private String file;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamImport(BookParam param) {
		super(param, "import");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("import");
		}
		init();
	}

	public void setDirectory(String d) {
		directory = d;
	}

	public String getDirectory() {
		if (directory == null) {
			return "";
		}
		return directory;
	}

	public void setFile(String d) {
		file = d;
	}

	public String getFile() {
		if (file == null) {
			return "";
		}
		return file;
	}

	@Override
	protected void init() {
		if (node != null) {
			String str = getString(node, KW.DIRECTORY.toString());
			if (IOUtil.dirExists(str)) {
				setDirectory(str);
			} else {
				setDirectory("");
			}
			str = getString(node, KW.FILE.toString());
			File f = new File(str);
			if (f.exists()) {
				setFile(str);
			} else {
				setFile("");
			}
		}
	}

	@Override
	public String toXml() {
		//LOG.trace(TT + ".toXml(b)");
		StringBuilder b = new StringBuilder();
		if (!getDirectory().isEmpty() || !getFile().isEmpty()) {
			b.append("        <import ");
			b.append(setAttribute(0, KW.DIRECTORY.toString(), getDirectory()));
			b.append(setAttribute(0, KW.FILE.toString(), getFile()));
			b.append(" />\n");
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return toXml();
	}
}
