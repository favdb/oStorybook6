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
package storybook.db.endnote;

import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.Ctrl;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.shortcut.Shortcuts;
import storybook.tools.html.Html;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

public class Endnote extends AbstractEntity {

	public static final String TT = "Endnote";

	public enum TYPE {
		ENDNOTE, COMMENT;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private Integer number = -1;
	private Long scene_id = -1L;
	private Scene scene = null;
	private String sort = "";
	private Integer type = TYPE.ENDNOTE.ordinal();

	public Endnote() {
		super(Book.TYPE.ENDNOTE, "010");
	}

	public Endnote(ResultSet rs) {
		super(Book.TYPE.ENDNOTE, "010", rs);
		try {
			type = rs.getInt("type");
			number = rs.getInt("number");
			scene = rs.getObject("scene", Scene.class);
		} catch (SQLException ex) {
			//empty
		}
	}

	public Endnote(int type, Scene scene, Integer number) {
		super(Book.TYPE.ENDNOTE, "010");
		this.type = type;
		this.scene = scene;
		this.number = number;
	}

	public Endnote(int type, Scene scene, Integer number, String notes) {
		this(type, scene, number);
		setNotes(notes);
	}

	public Endnote(TYPE type, Scene scene, Integer number, String notes) {
		this(type.ordinal(), scene, number, notes);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Endnote(int type, Scene scene, Integer number, String notes, int index) {
		this(type, scene, number, notes);
		this.type = type;
		setSort(index);
	}

	public Integer getType() {
		return getMinMax(type, 0, 1);
	}

	public String getTypeLib() {
		return I18N.getMsg("endnote.type_" + TYPE.values()[getType()].toString());
	}

	public void setType(TYPE type) {
		this.type = type.ordinal();
	}

	public void setType(Integer number) {
		this.type = (number == null ? 0 : number);
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = (number == null ? -1 : number);
		setName(String.format("%s %02d", getTypeLib(), number));
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene entity) {
		this.scene = entity;
	}

	public Long getSceneId() {
		return scene_id;
	}

	public void setSceneId(Long value) {
		this.scene_id = value;
	}

	public boolean hasScene() {
		return scene != null;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String value) {
		this.sort = value;
	}

	/**
	 * compute default sort value beautify is: pp.cc.ss.ttttt where: - pp is
	 * part number, 99 if not - cc is chapter number, 99 if not - ss is scene
	 * number, mandatory - ttttt is text index of the endnote
	 *
	 * @param loc
	 */
	public void setSort(int loc) {
		int nchapter = 99, npart = 99;
		if (getScene().hasChapter()) {
			nchapter = getScene().getChapter().getChapterno();
			if (getScene().getChapter().hasPart()) {
				npart = getScene().getChapter().getPart().getNumber();
			}
		}
		this.sort = String.format("%02d.%02d.%03d.%05d",
				npart,
				nchapter,
				scene.getSceneno(),
				loc);
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(super.toDetailHeader(detailed));
		b.append(getInfo(2, DATA.ENDNOTE_TYPE, getType() + " (" + getTypeLib() + ")"));
		b.append(getInfo(2, DATA.ENDNOTE_NUMBER, getNumber()));
		b.append(getInfo(2, DATA.ENDNOTE_SCENE, getScene()));
		if (App.isDev()) {
			b.append(getInfo(2, DATA.SORT, getSort()));
		}
		b.append(super.toDetailFooter(detailed));
		return b.toString();
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(id)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getName())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(type)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(number)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(scene)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getNotes())).append(quoteStart).append("\n");
		return b.toString();
	}

	@Override
	public String toHtml() {
		return toCsv("<td>", "</td>", "\n");
	}

	@Override
	public String toText() {
		return toCsv("", "", "\t");
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.TYPE, type))
				.append(XmlUtil.setAttribute(0, XK.NUMBER, number))
				.append(XmlUtil.setAttribute(0, XK.SCENE, scene))
				.append(XmlUtil.setAttribute(0, XK.SORT, sort))
				.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	public static Endnote fromXml(Node node) {
		//LOG.printInfos(TT + ".importXml(mainFrame, node=" + node.getTextContent() + ")");
		Endnote p = new Endnote();
		fromXmlBeg(node, p);
		p.setType(XmlUtil.getInteger(node, XK.TYPE));
		p.setNumber(XmlUtil.getInteger(node, XK.NUMBER));
		p.setSort(XmlUtil.getString(node, XK.SORT));
		p.setSceneId(XmlUtil.getLong(node, XK.SCENE));
		fromXmlEnd(node, p);
		return p;
	}

	public static Endnote importXml(MainFrame mainFrame, Node node) {
		//LOG.printInfos(TT + ".importXml(mainFrame, node=" + node.getTextContent() + ")");
		Endnote p = new Endnote();
		fromXmlBeg(node, p);
		p.setType(XmlUtil.getInteger(node, XK.TYPE));
		p.setNumber(XmlUtil.getInteger(node, XK.NUMBER));
		p.setSort(XmlUtil.getString(node, XK.SORT));
		p.setScene((Scene) mainFrame.project.get(Book.TYPE.SCENE, XmlUtil.getLong(node, XK.SCENE)));
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Endnote test = (Endnote) obj;
		boolean ret = true;
		ret = ret && equalsIntegerNullValue(getType(), test.getType());
		ret = ret && equalsIntegerNullValue(getNumber(), test.getNumber());
		ret = ret && equalsObjectNullValue(getScene(), test.getScene());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(), number, scene, sort);
	}

	/**
	 * get the link to the endnote from scene text
	 * <a href="#endnote_001" name="innote_001"><small><sup>&#160;(1)</sup></small></a>
	 *
	 * @param dir : for export, the directory of the endnote.html
	 * @return
	 */
	public String getLinkToEndnote(String dir) {
		return linkTo(dir, this);
	}

	/**
	 * get link from the endnote to the scene where it is
	 *
	 * @param dir
	 * @return
	 */
	public String getLinkToScene(String dir) {
		StringBuilder b = new StringBuilder();
		String snumber = String.format("_%03d", id);
		b.append(String.format("<a href=\"%s#innote%s\"", dir, snumber))
				.append(String.format(" name=\"endnote_%s\">", snumber))
				.append(String.format("%d. </a>", number));
		return b.toString();
	}

	/**
	 * ************************
	 */
	//***************************
	//** Utilities for Endnote **
	//***************************
	//***************************
	/**
	 * find an Endnote of the given type
	 *
	 * @param mainFrame
	 * @param type
	 *
	 * @return
	 */
	public static List<Endnote> find(MainFrame mainFrame, TYPE type) {
		//LOG.printInfos(TT + ".find(mainFrame, type=" + type + ")");
		List<Endnote> list = mainFrame.project.endnotes.findBySort(type.ordinal());
		return list;
	}

	/**
	 * remove all given type Endnote in the given Scene
	 *
	 * @param mainFrame
	 * @param type
	 * @param scene
	 */
	@SuppressWarnings("unchecked")
	public static void removeEndnotes(MainFrame mainFrame, TYPE type, Scene scene) {
		//LOG.printInfos(TT + ".removeEndnotes(mainFrame, scene=" + AbstractEntity.printInfos(scene) + ")");
		for (Endnote en : (List<Endnote>) mainFrame.project.endnotes.getList()) {
			removeEndnote(mainFrame, (Endnote) en);
		}
	}

	/**
	 * remove the given Endnote
	 *
	 * @param mainFrame
	 * @param endnote
	 */
	public static void removeEndnote(MainFrame mainFrame, Endnote endnote) {
		//LOG.printInfos(TT + ".removeEndnote(mainFrame, endnote=" + AbstractEntity.printInfos(endnote) + ")");
		Scene scene = endnote.getScene();
		if (scene != null) {
			String link = endnote.getLinkToEndnote("");
			if (!scene.getSummary().isEmpty()) {
				scene.setSummary(scene.getSummary().replace(link, ""));
				mainFrame.getBookModel().ENTITY_Update(scene);
			}
		}
		mainFrame.getBookModel().ENTITY_Delete(endnote);
	}

	/**
	 * renumber the Endnotes of the given type
	 *
	 * @param mainFrame
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean renumber(MainFrame mainFrame, Integer type) {
		//LOG.printInfos(TT + ".renumber(mainFrame)");
		mainFrame.cursorSetWaiting();
		boolean rc = false;
		Ctrl ctrl = mainFrame.getBookController();
		@SuppressWarnings("unchecked")
		int num = 1;
		List<Endnote> toRemove = new ArrayList<>();
		for (Endnote en : (List<Endnote>) mainFrame.project.endnotes.getList()) {
			Scene scene = en.getScene();
			if (scene != null) {
				if (!scene.getSummary().contains(en.getLinkToEndnote(""))) {
					mainFrame.getBookModel().ENTITY_Delete(en);
					continue;
				}
				if (en.getNumber() != num) {
					String oldLink = en.getLinkToEndnote("");
					en.setNumber(num);
					if (type == TYPE.ENDNOTE.ordinal()) {
						//replacing link only for Endnote
						String newLink = en.getLinkToEndnote("");
						String text = scene.getSummary().replace(oldLink, newLink);
						scene.setSummary(text);
						ctrl.updateEntity(scene);
					}
					ctrl.updateEntity(en);
					rc = true;
				}
				num++;
			} else {
				toRemove.add(en);
			}
		}
		if (!toRemove.isEmpty()) {
			for (Endnote en : toRemove) {
				ctrl.deleteEntity(en);
			}
		}
		mainFrame.cursorSetDefault();
		return rc;
	}

	/**
	 * create an Endnote for the given type in the given text
	 *
	 * @param mainFrame
	 * @param type
	 * @param scene
	 * @param htTexte the ShefEditor where to insert the Endnote
	 *
	 * @return
	 */
	public static Endnote createEndnote(MainFrame mainFrame, TYPE type,
			Scene scene, ShefEditor htTexte) {
		int num = mainFrame.project.endnotes.getNextNumber();
		Endnote en = new Endnote(type.ordinal(), scene, num);
		en.setSort(htTexte.wysEditorGet().getWysEditor().getCaretPosition());
		if (mainFrame.showEditorAsDialog(en)) {
			return null;
		}
		en = mainFrame.project.endnotes.find(type.ordinal(), scene, num);
		String link = linkTo("", en);
		htTexte.insertText(link);
		return en;
	}

	/**
	 * remove all Endnotes of the given type
	 *
	 * @param mainFrame
	 * @param type
	 */
	@SuppressWarnings("unchecked")
	public static void removeAll(MainFrame mainFrame, TYPE type) {
		//LOG.printInfos(TT + ".removeAll(mainFrame, type="
		//+ (type == 0 ? "endnote" : "comment") + ")");
		for (Scene scene : (List<Scene>) mainFrame.project.scenes.getList()) {
			removeEndnotes(mainFrame, type, scene);
		}
	}

	/**
	 * resort the Endnotes of the given type, not for Comments
	 *
	 * @param mainFrame
	 * @param type
	 */
	public static void resort(MainFrame mainFrame, TYPE type) {
		if (type == TYPE.ENDNOTE) {
			List<Scene> scenes = mainFrame.project.endnotes.findScenes(type);
			for (Scene scene : scenes) {
				resort(mainFrame, type, scene);
			}
		}
	}

	/**
	 * resort the Endnotes of the given type in the given Scene, not for
	 * Comments
	 *
	 * @param mainFrame
	 * @param type
	 * @param scene
	 */
	public static void resort(MainFrame mainFrame, TYPE type, Scene scene) {
		if (type == TYPE.ENDNOTE) {
			String text = Html.htmlToText(scene.getSummary());
			List<Endnote> endnotes = mainFrame.project.endnotes.find(type, scene);
			for (Endnote en : endnotes) {
				int idx = text.indexOf("[" + en.getNumber().toString() + "]") + 1;
				en.setSort(idx);
				mainFrame.getBookController().updateEntity(en);
			}
		}
	}

	/**
	 * get the JButton to show the list of Endnotes
	 *
	 * @param act the action for button clicked
	 *
	 * @return
	 */
	public static JButton getButtonShow(ActionListener... act) {
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.SORT));
		bt.setName("endnote_show");
		bt.setMnemonic('X');
		bt.setToolTipText(Shortcuts.getTooltips("shef", "endnote_show"));
		if (act != null && act.length > 0) {
			bt.addActionListener(act[0]);
		}
		return bt;
	}

	/**
	 * get a JButton for adding an Endnote
	 *
	 * @param act the action for button clicked
	 *
	 * @return
	 */
	public static JButton getButtonAdd(ActionListener... act) {
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.CHAR_ENDNOTE));
		bt.setName("endnote_add");
		bt.setMnemonic('W');
		bt.setToolTipText(Shortcuts.getTooltips("shef", "endnote_add"));
		if (act != null && act.length > 0) {
			bt.addActionListener(act[0]);
		}
		return bt;
	}

	/**
	 * get the next number to create
	 *
	 * @param endnotes
	 * @return
	 */
	public static Integer getNextNumber(List endnotes) {
		int n = 0;
		for (Object c : endnotes) {
			if (((Endnote) c).number > n) {
				n = ((Endnote) c).number;
			}
		}
		return (n + 1);
	}

	/**
	 * Link to the endnote from text like
	 * <a class="endnote" href="#endnote_001" name="innote_001"><small><sup>(1)</sup></small></a>
	 *
	 * @param file : real name of the endnote.html file
	 * @param endnote
	 *
	 * @return
	 */
	public static String linkTo(String file, Endnote endnote) {
		return linkTo(file, endnote.getId(), endnote.getNumber(), endnote.getType());
	}

	/**
	 * get a String which link to the endnote from text
	 * <a class="endnote" href="#endnote_001" name="innote_001"><small><sup>(1)</sup></small></a>
	 *
	 * @param file : real name of the endnote.html file
	 * @param id of the endnote
	 * @param number
	 * @param type of Endnote
	 *
	 * @return
	 */
	public static String linkTo(String file, Long id, int number, int type) {
		if (type < 0) {
			type = 0;
		}
		if (type > 1) {
			type = 1;
		}
		String snumber = String.format("_%03d", id);
		String ntype = "endnote";
		String enclose = "()";
		if (type == 1) {
			ntype = "comment";
			enclose = "[]";
		}
		String shref = String.format("%s#" + ntype + "%s", file, snumber);
		String sname = String.format("in" + ntype + "%s", snumber);
		String stext = String.format("%c%d%c", enclose.charAt(0), number, enclose.charAt(1));
		String sclass = " class=\"" + ntype + "\"";
		return Html.intoA(sname, shref, stext, sclass);
	}

	/**
	 * get a String which link to the scene from the Endnote like
	 * <a href="#endnote_001" name="innote_001">NN. text note</a>
	 *
	 * @param file : real name of the origin file
	 * @param endnote
	 * @return
	 */
	public static String linkFrom(String file, Endnote endnote) {
		return linkFrom(file,
				endnote.getId(),
				endnote.getNumber(),
				endnote.getType());
	}

	/**
	 * get a String which link to the scene from the Endnote like
	 * <a href="#endnote_001" name="innote_001">NN.text note</a>
	 *
	 * @param file : real name of the origin file
	 * @param id of the Endnote
	 * @param number
	 * @param type of the Endnote
	 *
	 * @return
	 */
	public static String linkFrom(String file, Long id, int number, int type) {
		if (type < 0) {
			type = 0;
		}
		if (type > 1) {
			type = 1;
		}
		String libin = "innote";
		String libend = "endnote";
		if (type == 1) {
			libin = "incomment";
			libend = "comment";
		}
		String sid = String.format("_%03d", id);
		StringBuilder b = new StringBuilder();
		String shref = String.format("%s#%s%s", file, libin, sid);
		String sname = String.format("%s%s", libend, sid);
		String text = String.format("[%d]", number);
		b.append(Html.intoA(sname, shref, text, "class=\"" + libend + "\""));
		return b.toString();
	}

	/**
	 * check if the Endnote is in the Scene text
	 *
	 * @return true if yes
	 */
	public boolean isInText() {
		return (scene == null ? false : scene.getSummary().contains(getLinkToEndnote("")));
	}

	/**
	 * get the default columns for table
	 *
	 * @return
	 */
	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.ENDNOTE);
		return list;
	}

	/**
	 * get the table beautify to check/resize
	 *
	 * @return
	 */
	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "endnote";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",number,Integer,0");
		ls.add(tableName + ",type,Integer,0");
		ls.add(tableName + ",sort,String,256");
		ls.add(tableName + ",scene_id,Integer,0");
		return ls;
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Endnote ne = new Endnote();
		doCopyTo(m, ne);
		ne.setNumber(0);
		ne.setScene(getScene());
		ne.setSort(getSort());
		ne.setType(getType());
		return ne;
	}

}
