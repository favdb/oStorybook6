/*
Copyright ???? by ????
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

import java.util.ArrayList;
import java.util.List;
import storybook.tools.ListUtil;
import storybook.tools.StringUtil;
import storybook.tools.file.IOUtil;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author FaVdB
 */
public class BookParamBackup extends BookParamAbstract {

	public enum KW {
		DIRECTORY, AUTO, INCREMENT;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private String directory;
	private boolean auto;
	private int increment;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamBackup(BookParam param) {
		super(param, "backup");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("backup");
		}
		init();
	}

	public void setDirectory(String d) {
		directory = d;
	}

	public String getDirectory() {
		return (directory);
	}

	public void setAuto(boolean d) {
		auto = d;
	}

	public boolean getAuto() {
		return (auto);
	}

	public void setIncrement(int d) {
		increment = d;
	}

	public int getIncrement() {
		return (increment);
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
			setAuto(getBoolean(node, KW.AUTO.toString()));
			String inc = getString(node, KW.INCREMENT.toString());
			if (StringUtil.isNumeric(inc)) {
				setIncrement(getInteger(node, KW.INCREMENT.toString()));
			} else {
				setIncrement(0);
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append("        <backup ");
		b.append(setAttribute(0, KW.DIRECTORY.toString(), directory));
		b.append(setAttribute(0, KW.AUTO.toString(), auto));
		b.append(setAttribute(0, KW.INCREMENT.toString(), getIncrement()));
		b.append("/>\n");
		return b.toString();
	}

	@Override
	public String toString() {
		List<String> b = new ArrayList<>();
		b.add(getDirectory());
		b.add((getAuto() ? "true" : "false"));
		b.add(getIncrement() + "");
		return ListUtil.join(b, ", ");
	}

}
