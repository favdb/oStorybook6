/*
Copyright 2018 by FaVdB
Modifications :

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
package storybook.db.status;

import i18n.I18N;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenu;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;
import storybook.ui.MainMenu;

/**
 *
 * @author FaVdB
 */
public class Status extends AbstractEntity {

	private Status sup = null;
	private Long sup_id = -1L;
	private String icone = "";

	public Status() {
		super(Book.TYPE.STATUS, "110");
	}

	public Status(ResultSet rs) {
		super(Book.TYPE.STATUS, "110", rs);
		try {
			sup = rs.getObject("sup", Status.class);
			icone = rs.getString("icone");
		} catch (SQLException ex) {
			//empty
		}
	}

	public Status(Long id, String name) {
		this();
		setId(id);
		setName(name);
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String str) {
		icone = str;
	}

	public Status getSup() {
		return sup;
	}

	public void setSup(Status str) {
		sup = str;
	}

	public Long getSupId() {
		return sup_id;
	}

	public void setSupId(Long str) {
		sup_id = str;
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder(toCsvHeader(quoteStart, quoteEnd, separator));
		b.append(quoteStart).append(getClean(getIcone())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getSup())).append(quoteEnd).append(separator);
		b.append(toCsvFooter(quoteStart, quoteEnd, separator));
		return (b.toString());
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		b.append(getInfo(detailed, DATA.STATUS_ICONFILE, icone));
		if (sup != null) {
			b.append(getInfo(detailed, DATA.STATUS_SUP, sup.getName()));
		}
		b.append(toDetailFooter(detailed));
		return b.toString();
	}

	@Override
	public String toString() {
		return (getName());
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.ICONE, getIcone()));
		b.append(XmlUtil.setAttribute(0, XK.SUP, getSup()));
		return b.toString();
	}

	public static Status fromXml(Node node) {
		Status p = new Status();
		p.setIcone(XmlUtil.getString(node, XK.ICONE));
		p.setSupId(XmlUtil.getLong(node, XK.SUP));
		return p;
	}

	public static Status find(List<Status> list, String str) {
		for (Status elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Status find(List<Status> list, Long id) {
		for (Status elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public enum STATUS {
		OUTLINE,
		DRAFT,
		EDIT1,
		EDIT2,
		DONE;

		private STATUS() {
		}

		public String getLabel() {
			return (I18N.getMsg("status." + toString()));
		}

		public Icon getIcon() {
			return IconUtil.getIconSmall(ICONS.getIconKey("status_" + toString()));
		}

		public Icon getIcon(int sz) {
			return IconUtil.getIcon("png/status_" + toString(), sz);
		}

		@Override
		public String toString() {
			return (name().toLowerCase());
		}
	}

	public static String getHtmlValue(int status) {
		return (status + " (" + getStatusMsg(status) + ")");
	}

	public static String getStatusMsg(int status) {
		if (status < 0 || status > STATUS.values().length - 1) {
			return STATUS.values()[0].getLabel();
		}
		return (STATUS.values()[status].getLabel());
	}

	public static Icon getStatusIcon(int status) {
		if (status < 0 || status > STATUS.values().length - 1) {
			return STATUS.values()[0].getIcon();
		}
		return (STATUS.values()[status].getIcon());
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.STATUS);
		list.add("icone, 256");
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "status";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",icone,String,256");
		return (ls);
	}

	public static JMenu getMenu(MainFrame mainFrame, Scene scene) {
		JMenu mn = new JMenu(I18N.getMsg("status"));
		mn.add(MainMenu.initMenuItem(ICONS.K.STATUS_OUTLINE,
				"status.outline", "status.outline", ' ', "status.outline",
				e -> mainFrame.project.scenes.changeStatus(mainFrame, scene, 0)));
		mn.add(MainMenu.initMenuItem(ICONS.K.STATUS_DRAFT,
				"status.draft", "status.outline", ' ', "status.draft",
				e -> mainFrame.project.scenes.changeStatus(mainFrame, scene, 1)));
		mn.add(MainMenu.initMenuItem(ICONS.K.STATUS_EDIT1,
				"status.edit1", "status.outline", ' ', "status.edit1",
				e -> mainFrame.project.scenes.changeStatus(mainFrame, scene, 2)));
		mn.add(MainMenu.initMenuItem(ICONS.K.STATUS_EDIT2,
				"status.edit2", "status.outline", ' ', "status.edit2",
				e -> mainFrame.project.scenes.changeStatus(mainFrame, scene, 3)));
		mn.add(MainMenu.initMenuItem(ICONS.K.STATUS_DONE,
				"status.done", "status.outline", ' ', "status.done",
				e -> mainFrame.project.scenes.changeStatus(mainFrame, scene, 4)));
		return mn;
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Status ne = new Status();
		doCopyTo(m, ne);
		ne.setIcone(getIcone());
		ne.setSup(getSup());
		return ne;
	}

}
