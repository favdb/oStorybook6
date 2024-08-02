/*
 * Copyright (C) 2022 favdb
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
package storybook.ui;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.DB;
import storybook.db.DB.DATA;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.status.StatusLCR;
import storybook.edit.AbstractEditor;
import storybook.shortcut.Shortcuts;
import storybook.tools.TextUtil;
import storybook.tools.swing.CbUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;

/**
 *
 * @author favdb
 */
public class Ui {

	private static final String TT = "Ui";

	public static final String CTRL = "ctrl ", ALT = "alt ", SHIFT = "shift ",
		CTRL_SHIFT = CTRL + SHIFT, CTRL_ALT = CTRL + ALT;

	private Ui() {
		throw new IllegalStateException("Utility class");
	}

	public static final Dimension MINIMUM_SIZE = new Dimension(440, 120),
		MAXIMUM_SIZE = new Dimension(800, 600),
		PREF_SIZE = new Dimension(750, 560);
	public static String BALL = "all",
		BBORDER = "border",
		BCHECK = "check",
		BEMPTY = "empty",
		BGROW = "grow",
		BINFO = "info",
		BMANDATORY = "mandatory",
		BNEW = "new",
		BNONE = "";
	public static final boolean MANDATORY = true,
		NEW = true,
		ALL = true,
		EMPTY = true,
		HIDE = true,
		INFO = true,
		NEWTAB = true,
		GROW = true,
		TIME = true,
		TOP = true,
		WRAP = true;

	/**
	 * add a component with it's title in a JPanel
	 *
	 * @param panel
	 * @param title
	 * @param top
	 * @param comp
	 * @param opt
	 * @param tab
	 */
	public static void addField(JPanel panel, String title, String top,
		JComponent comp, JTabbedPane tab, String opt) {
		if (tab != null) {
			tab.add(comp, I18N.getMsg(title));
		} else {
			JLabel lb = new JLabel(I18N.getColonMsg(title));
			if (opt.contains(BMANDATORY)) {
				lb.setFont(FontUtil.getBold(lb.getFont()));
			}
			String c = top;
			if (!top.contains(MIG.WRAP)) {
				c = top + ", " + MIG.RIGHT;
			}
			String strgrow = "";
			if (top.contains(MIG.WRAP)) {
				strgrow = MIG.get(MIG.WRAP, MIG.SPAN);
			}
			if (opt.contains(BGROW)) {
				strgrow = MIG.GROW;
			}
			if (!title.isEmpty()) {
				panel.add(lb, c);
			}
			panel.add(comp, strgrow);
		}
	}

	/**
	 * add a label to a JPanel
	 *
	 * @param panel
	 * @param title
	 * @param isTop
	 * @param mandatory
	 */
	public static void addLabel(JPanel panel, String title, boolean isTop, boolean mandatory) {
		String top = "";
		if (isTop) {
			top = "top,";
		}
		JLabel lb = new JLabel(I18N.getColonMsg(title));
		lb.setFont(App.fonts.defGet());
		if (mandatory) {
			lb.setFont(FontUtil.getBold(lb.getFont()));
		}
		panel.add(lb, top + MIG.RIGHT);
	}

	/**
	 * add a label to a JPanel
	 *
	 * @param panel
	 * @param title
	 * @param isTop
	 * @param mandatory
	 */
	public static void addLabelNew(JPanel panel, String title, boolean isTop, boolean mandatory) {
		String top = "";
		if (isTop) {
			top = "top,";
		}
		JLabel lb = new JLabel(I18N.getColonMsg(title));
		lb.setFont(App.fonts.defGet());
		if (mandatory) {
			lb.setFont(FontUtil.getBold(lb.getFont()));
		}
		panel.add(lb, top + MIG.RIGHT + "," + MIG.NEWLINE);
	}

	/**
	 * get a JTextField
	 *
	 * @param p
	 * @param name
	 * @param len length of the field, may be 0
	 * @param val set the initial value
	 * @param opt
	 *
	 * @return
	 */
	public static JTextField initStringField(JPanel p, String name, int len,
		String val, String opt) {
		JTextField tf = new JTextField();
		tf.setName(name);
		tf.setText(val);
		boolean grow = false;
		if (len > 0) {
			tf.setColumns(len);
		} else {
			grow = true;
		}
		if (opt.contains(BINFO)) {
			tf.setEditable(false);
		}
		addField(p, name, "", tf, null, opt);
		return (tf);
	}

	public static JTextField initStringField(JPanel p, DATA data, int len,
		String val, String opt) {
		return initStringField(p, data.i18n(), len, val, opt);
	}

	public static JTextField getStringField(DATA data, int len, String val, String opt) {
		return getStringField(data.toString(), len, val, opt);
	}

	public static JTextField getStringField(String name, int len, String val, String opt) {
		JTextField tf = new JTextField();
		tf.setName(name);
		tf.setText(val);
		if (len > 0) {
			tf.setColumns(len);
		}
		if (opt.contains(BINFO)) {
			tf.setEditable(false);
		}
		return (tf);
	}

	public static JTextField initIntegerField(JPanel p, DATA data, int len,
		Integer val, String opt) {
		return initIntegerField(p, data.i18n(), len, val, opt);
	}

	public static JTextField initIntegerField(JPanel p, String name, int len,
		Integer val, String opt) {
		if (val == null) {
			val = 0;
		}
		String s = val.toString();
		if (val == 0 && !opt.contains(BMANDATORY)) {
			s = "";
		}
		return (initStringField(p, name, len, s, opt));
	}

	/**
	 * get an Html editor field
	 *
	 * @param panel: JPanel where to add the field
	 * @param name
	 * @param val: initial value
	 * @param tab
	 * @param opt
	 * @return
	 */
	public static ShefEditor initHtmlEditor(JPanel panel,
		String name,
		String val,
		JTabbedPane tab,
		String opt) {
		ShefEditor ta = new ShefEditor("", "full", val);
		ta.setName(name);
		ta.setMinimumSize(new Dimension(200, 80));
		ta.setPreferredSize(new Dimension(1024, 780));
		ta.setMaximumSize(SwingUtil.getScreenSize());
		addField(panel, name, MIG.TOP, ta, tab, opt);
		return ta;
	}

	/**
	 * initialize a JComboBox
	 *
	 * @param name
	 * @param empty : empty item allowed
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static JComboBox initCB(String name, String opt) {
		JComboBox combo = new JComboBox();
		combo.setMinimumSize(IconUtil.getDefDim());
		combo.setName(name);
		if (opt.contains(BEMPTY)) {
			combo.addItem(" ");
		}
		return combo;
	}

	/**
	 * initialize a JComboBox with a List of String
	 *
	 * @param panel: where to add the JComboBox
	 * @param caller : the JComponent caller, may be null
	 * @param name
	 * @param list
	 * @param toSel
	 * @param opt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JComboBox initCbStrings(JPanel panel, AbstractEditor caller,
		String name, List<String> list, String toSel, String opt) {
		JComboBox combo = initCB(name, opt);
		//load the list
		for (String str : list) {
			combo.addItem(str);
		}
		// select the item
		if (toSel != null && !toSel.isEmpty()) {
			combo.setSelectedItem(toSel);
		}
		// add the JComboBox to the panel
		if (opt.contains(BNEW)) {
			JPanel p = new JPanel(new MigLayout(MIG.INS0));
			JButton bt = new IconButton("btNew", ICONS.K.NEW, caller);
			p.add(combo);
			p.add(bt, MIG.SHRINK);
			addLabel(panel, name, !TOP, opt.contains(BMANDATORY));
			panel.add(p, MIG.LEFT);
		} else {
			addField(panel, name, "", combo, null, opt);
		}
		return combo;
	}

	/**
	 * initialize a JComboBox for a Book.TYPE
	 *
	 * @param panel
	 * @param caller
	 * @param name
	 * @param objtype
	 * @param toSel
	 * @param toHide
	 * @param opt
	 * @return
	 */
	public static JComboBox initCbEntities(JPanel panel, AbstractEditor caller,
		String name, Book.TYPE objtype,
		AbstractEntity toSel, AbstractEntity toHide,
		String opt) {
		JComboBox cb = new JComboBox();
		cb.setFont(App.fonts.defGet());
		SwingUtil.setCBsize(cb);
		cb.setName(name);
		CbUtil.fillEntity(caller.mainFrame, cb, objtype, toSel, toHide,
			(opt.contains(BNEW) ? "1" : "0") + (opt.contains(BEMPTY) ? "1" : "0") + "0");
		if (opt.contains(BNEW)) {
			JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
			JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.NEW));
			SwingUtil.setForcedSize(bt, IconUtil.getDefDim());
			bt.addActionListener((ActionEvent e) -> {
				AbstractEntity newEntity = BookUtil.getNewEntity(null, objtype);
				boolean rc = caller.mainFrame.showEditorAsDialog(newEntity, bt);
				if (rc != true) {
					CbUtil.fillEntity(caller.mainFrame, cb, objtype, newEntity, toHide,
						(opt.contains(BNEW) ? "1" : "0") + (opt.contains(BEMPTY) ? "1" : "0") + "1");
				}
			});
			p.add(cb, MIG.GROWX);
			p.add(bt, MIG.SHRINK);
			addLabel(panel, name, !TOP, opt.contains(BMANDATORY));
			panel.add(p, MIG.LEFT);
		} else {
			addField(panel, name, "", cb, null, opt);
		}
		return (cb);
	}

	/**
	 * get a JComboBox to select an Entity
	 *
	 * @param panel the panel where the JComboBox must be insert
	 * @param caller the Editor caller
	 * @param type the Book.TYPE
	 * @param toSel the Entity to select, may be null
	 * @param toHide the Entity to hide, may be null
	 * @param opt
	 * @return
	 */
	public static JComboBox getCB(JPanel panel,
		AbstractEditor caller, Book.TYPE type,
		AbstractEntity toSel, AbstractEntity toHide, String opt) {
		JComboBox combo = new JComboBox();
		combo.setName(type.toString());
		SwingUtil.setCBsize(combo);
		CbUtil.fillEntity(caller.mainFrame, combo, type, toSel, toHide,
			(opt.contains(BALL) ? "1" : "0") + (opt.contains(BEMPTY) ? "1" : "0") + "0");
		if (opt.contains(BNEW)) {
			JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
			JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.NEW));
			SwingUtil.setForcedSize(bt, IconUtil.getDefDim());
			bt.addActionListener(e -> {
				AbstractEntity newEntity = BookUtil.getNewEntity(null, type);
				boolean rc = caller.mainFrame.showEditorAsDialog(newEntity, bt);
				if (rc != true) {
					CbUtil.fillEntity(caller.mainFrame, combo, type, newEntity, toHide,
						(opt.contains(BALL) ? "1" : "0") + (opt.contains(BEMPTY) ? "1" : "0") + "1");
				}
			});
			p.add(combo);
			p.add(bt, MIG.SHRINK);
			addLabel(panel, type.toString(), !TOP, opt.contains(BMANDATORY));
			panel.add(p, MIG.LEFT);
		} else {
			panel.add(combo, (opt.contains(BGROW) ? MIG.GROWX : ""));
		}
		return (combo);
	}

	/**
	 * initialize a JCheckBox
	 *
	 * @param panel: JPanel where add the JCheckBox, if null the JCheckBox isn't added anywhere just
	 * return the JCheckBox
	 * @param name: internal name of the JCheckBox
	 * @param text: text for the JCheckBox
	 * @param sel
	 * @param opt
	 * @param action : optional, action when changed
	 * @return an initialized JCheckBox
	 */
	public static JCheckBox initCheckBox(JPanel panel,
		String name, String text, boolean sel,
		String opt, ActionListener... action) {
		JCheckBox cb = new JCheckBox();
		cb.setName(name);
		cb.setText(I18N.getMsg(text));
		cb.setSelected(sel);
		if (opt != null && opt.contains(BMANDATORY)) {
			cb.setFont(FontUtil.getBold());
		}
		if (panel != null) {
			if (opt != null && opt.contains("!")) {
				panel.add(cb, opt.split("!")[1]);
			} else {
				panel.add(cb);
			}
		}
		if (action != null && action.length > 0) {
			cb.addActionListener(action[0]);
		}
		return cb;
	}

	@SuppressWarnings("unchecked")
	public static JComboBox initComboBox(String name,
		String tooltip,
		List list,
		AbstractEntity toSel,
		boolean isEmpty,
		boolean isAll,
		ActionListener... action) {
		//App.trace(TT+".comboboxInit(title="+title+"tooltip, list, toSel, isEmpty, isAll)");
		JComboBox cb = new JComboBox();
		cb.setMinimumSize(IconUtil.getDefDim());
		cb.setName(name);
		if (tooltip != null && !tooltip.isEmpty()) {
			cb.setToolTipText(I18N.getMsg(tooltip));
		}
		cb.setOpaque(false);
		if (isEmpty) {
			cb.addItem("");
		}
		if (isAll) {
			cb.addItem(I18N.getMsg("all"));
		}
		if (list != null && !list.isEmpty()) {
			list.forEach(e -> cb.addItem(e));
			if (toSel != null) {
				cb.setSelectedItem(toSel);
			} else {
				cb.setSelectedIndex(0);
			}
		}
		if (action != null && action.length > 0) {
			cb.addActionListener(action[0]);
		}
		return (cb);
	}

	@SuppressWarnings("unchecked")
	public static JComboBox initComboBox(String name,
		String tooltip,
		String[] list,
		int toSel,
		boolean isEmpty,
		boolean isAll,
		ActionListener... action) {
		//App.trace(TT+".comboboxInit(title="+title+"tooltip, list, toSel, isEmpty, isAll)");
		JComboBox cb = new JComboBox();
		cb.setMinimumSize(IconUtil.getDefDim());
		cb.setName(name);
		if (tooltip != null && !tooltip.isEmpty()) {
			cb.setToolTipText(I18N.getMsg(tooltip));
		}
		cb.setOpaque(false);
		if (isEmpty) {
			cb.addItem("");
		}
		if (isAll) {
			cb.addItem(I18N.getMsg("all"));
		}
		if (list != null && list.length > 1) {
			for (String str : list) {
				cb.addItem(I18N.getMsg(str));
			}
			cb.setSelectedIndex(toSel);
		}
		if (action != null && action.length > 0) {
			cb.addActionListener(action[0]);
		}
		return (cb);
	}

	/**
	 * fill a JComboBox with a given List
	 *
	 * @param cb
	 * @param list
	 * @param opt
	 * @param action
	 */
	@SuppressWarnings("unchecked")
	public static void fillCB(JComboBox cb,
		List list,
		String opt,
		ActionListener... action) {
		//App.trace(TT+".fillComboBox(cb, list, toSel, isEmpty, isAll)");
		int toSel = cb.getSelectedIndex();
		if (action != null && action.length > 0) {
			cb.removeActionListener(action[0]);
		}
		cb.removeAllItems();
		if (opt.contains(BEMPTY)) {
			cb.addItem("");
		}
		if (opt.contains(BALL)) {
			cb.addItem(I18N.getMsg("all"));
		}
		if (list != null && !list.isEmpty()) {
			list.forEach((e) -> {
				AbstractEntity entity = (AbstractEntity) e;
				cb.addItem(entity);
			});
		}
		cb.setSelectedIndex(toSel);
		if (action != null && action.length > 0) {
			cb.addActionListener(action[0]);
		}
	}

	/**
	 * initialize a JRadioButton
	 *
	 * @param panel: JPanel where add the JCheckBox, if null the JCheckBox isn't added anywhere
	 * @param text: text for the JCheckBox
	 * @param check: true if the initial state would be selected
	 * @param opt
	 * @param top: optional, MigLayout directive for positioning the JCheckBox
	 *
	 * @return an initialized JCheckBox
	 */
	public static JRadioButton initRadioButton(JPanel panel,
		String text, boolean check, String opt, String... top) {
		JRadioButton cb = new JRadioButton();
		cb.setName(text);
		cb.setText(I18N.getMsg(text));
		cb.setSelected(check);
		if (opt.contains(BMANDATORY)) {
			cb.setFont(FontUtil.getBold());
		}
		if (panel != null) {
			if (top != null && top.length > 0) {
				panel.add(cb, top[0]);
			} else {
				panel.add(cb);
			}
		}
		return cb;
	}

	/**
	 * initialize a JComboBox for Category
	 *
	 * @param panel
	 * @param list
	 * @param toSel
	 * @param opt
	 * @return
	 */
	public static JComboBox initCategory(JPanel panel,
		List<String> list, String toSel, String opt) {
		return initAutoCombo(panel, DB.DATA.CATEGORY, list, toSel, opt);
	}

	/**
	 * combobox for a String list, with mandatory, empty allowed and optional grow
	 *
	 * @param panel
	 * @param title
	 * @param list
	 * @param toSel
	 * @param opt for mandatory, add button, empty value, growx
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JComboBox initAutoCombo(JPanel panel, DATA title,
		List<String> list, String toSel, String opt) {
		JComboBox cb = new JComboBox();
		cb.setName(title.i18n());
		cb.setMinimumSize(new Dimension(150, 20));
		cb.setEditable(true);
		if (opt.contains(BEMPTY)) {
			cb.addItem(" ");
		}
		list.forEach((str) -> {
			if (str != null && !str.isEmpty()) {
				cb.addItem(str);
			}
		});
		if (toSel != null && !toSel.isEmpty()) {
			cb.setSelectedItem(toSel);
		}
		if (opt.charAt(2) == '1') {
			addField(panel, title.i18n(), "", cb, null, BGROW + opt);
		} else {
			addField(panel, title.i18n(), "", cb, null, BGROW + opt);
		}
		return cb;
	}

	public static JComboBox initAutoCombo(JPanel panel, MainFrame mainFrame,
		DATA title, Book.TYPE objtype,
		AbstractEntity toSel, AbstractEntity toHide,
		String opt) {
		JComboBox combo = new JComboBox();
		combo.setName(title.i18n());
		combo.setMinimumSize(new Dimension(150, IconUtil.getDefSize()));
		CbUtil.fillEntity(mainFrame, combo, objtype, toSel, toHide, opt);
		JButton bt = SwingUtil.getIconButton("new", null);
		bt.addActionListener((ActionEvent e) -> {
			AbstractEntity newEntity = BookUtil.getNewEntity(null, objtype);
			boolean rc = mainFrame.showEditorAsDialog(newEntity, bt);
			if (rc != true) {
				CbUtil.fillEntity(mainFrame, combo, objtype, newEntity, toHide, opt);
			}
		});
		bt.setVisible(isAdd(opt));
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		p.add(combo);
		p.add(bt);
		addField(panel, title.i18n(), "", p, null, opt);
		return (combo);
	}

	public static boolean isAdd(String opt) {
		return opt.charAt(1) == '1';
	}

	public static boolean isMandatory(String opt) {
		return opt.charAt(0) == '1';
	}

	public static JComboBox initAutoCombo(JPanel panel, MainFrame mainFrame,
		String title, Book.TYPE objtype,
		AbstractEntity toSel, AbstractEntity toHide, String opt) {
		JComboBox combo = new JComboBox();
		combo.setName(title);
		combo.setMinimumSize(new Dimension(150, IconUtil.getDefSize()));
		CbUtil.fillEntity(mainFrame, combo, objtype, toSel, toHide, opt);
		JButton bt = SwingUtil.getIconButton("new", null);
		bt.addActionListener((ActionEvent e) -> {
			AbstractEntity newEntity = BookUtil.getNewEntity(null, objtype);
			boolean rc = mainFrame.showEditorAsDialog(newEntity, bt);
			if (rc != true) {
				CbUtil.fillEntity(mainFrame, combo, objtype, newEntity, toHide, opt);
			}
		});
		bt.setVisible(opt.contains(BNEW));
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		p.add(combo);
		p.add(bt);
		addField(panel, title, "", p, null, opt);
		return (combo);
	}

	@SuppressWarnings("unchecked")
	public static JPanel initListBox(JPanel panel, MainFrame mainFrame, Book.TYPE type,
		String title, List<?> list, AbstractEntity tosel, ActionListener actionNew) {
		/*LOG.trace(TT+".initListBox(panel, mainFrame"+
				", type="+type.toString()+
				", title="+ title+
				", list nb="+(list!=null?list.size():"null")+
				", tosel="+(tosel==null?"null":tosel.getName())+
				", addNew="+(addNew==true?"true":"false"));*/
		JPanel p = new JPanel(new MigLayout(MIG.FILL, "[][]"));
		if (!title.isEmpty()) {
			p.add(new JLabel(I18N.getColonMsg(title)), MIG.SPAN);
		}
		DefaultListModel model = new DefaultListModel();
		for (Object obj : list) {
			model.addElement(obj);
		}
		JList ls = new JList(model);
		ls.setName("ls_" + title);
		ls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ls.setMaximumSize(SwingUtil.getScreenSize());
		JScrollPane scroll = new JScrollPane(ls);
		SwingUtil.setMaxPreferredSize(scroll);
		p.add(scroll, MIG.get(MIG.SPAN, MIG.GROW));
		if (actionNew != null) {
			JButton bt = initButton("btAdd", "add", ICONS.K.ADD, "", actionNew);
			p.add(bt, MIG.LEFT);
			bt = initButton("btDelete", "delete", ICONS.K.DELETE, "", actionNew);
			p.add(bt, MIG.RIGHT);
		}
		return p;
	}

	/**
	 * get a JSCheckList
	 *
	 * @param p
	 * @param mainFrame
	 * @param type
	 * @param ls
	 * @param tab
	 * @param opt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSCheckList initCkList(JPanel p, MainFrame mainFrame, Book.TYPE type,
		List<?> ls, JTabbedPane tab, String opt) {
		JPanel p1 = new JPanel(new MigLayout(MIG.get(MIG.INS1, MIG.GAP0, MIG.FILLX), "[grow][]"));
		if (opt.contains(BBORDER)) {
			p1.setBorder(BorderFactory.createTitledBorder(I18N.getColonMsg(type.toString())));
		}
		JSCheckList cklist = new JSCheckList(mainFrame, type);
		cklist.setName(type.toString());
		if (ls != null) {
			cklist.setSelectedEntities((List<AbstractEntity>) ls);
		}
		JScrollPane scroller = new JScrollPane(cklist);
		if (tab != null) {
			scroller.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
			scroller.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		} else {
			scroller.setMinimumSize(new Dimension(120, 320));
			scroller.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		}
		p1.add(scroller, MIG.get(MIG.GROW));

		JButton btAdd = initButton("btAdd", "", ICONS.K.PLUS, "new");
		btAdd.addActionListener((ActionEvent act) -> {
			List<AbstractEntity> xls = cklist.getSelectedEntities();
			AbstractEntity e = BookUtil.getNewEntity(null, type);
			if (e == null) {
				return;
			}
			boolean rc = mainFrame.showEditorAsDialog(e, btAdd);
			if (!rc) {
				cklist.reload();
				cklist.refresh();
				cklist.setSelectedEntities(xls);
			}
		});
		p1.add(btAdd, MIG.TOP);

		if (tab != null) {
			addField(p, type.toString(), "", p1, tab, opt);
		} else {
			p.add(p1, MIG.GROW);
		}
		return cklist;
	}

	/**
	 * get a JRadioButton
	 *
	 * @param bg the button group
	 * @param title the name and the text key
	 * @param checked to initialize the selected state
	 *
	 * @return
	 */
	public static JRadioButton initRadio(ButtonGroup bg, String title, boolean checked) {
		JRadioButton rb = new JRadioButton();
		rb.setName(title);
		rb.setText(I18N.getMsg(title));
		rb.setSelected(checked);
		bg.add(rb);
		return rb;
	}

	/**
	 * short way to get a JButton for an Entity
	 *
	 * @param name
	 * @param entity
	 * @param act optional action
	 *
	 * @return
	 */
	public static JButton initButton(String name, AbstractEntity entity, ActionListener... act) {
		if (entity == null) {
			return initButton(name, "", ICONS.K.UNKNOWN, "");
		}
		JButton bt = initButton(name,
			"!" + TextUtil.ellipsize(entity.getName(), 20),
			null,
			"",
			act);
		bt.setIcon(entity.getIcon());
		bt.setToolTipText(EntityUtil.getTooltip(entity));
		return bt;
	}

	/**
	 * get a JComboBox for status
	 *
	 * @param panel
	 * @param sel the initial value
	 * @param opt
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JComboBox initStatus(JPanel panel, int sel, String opt) {
		//App.trace(TT + ".initStatus(panel, sel=" + sel + ")");
		JComboBox cb = new JComboBox();
		cb.setMinimumSize(IconUtil.getDefDim());
		cb.setName("cbStatus");
		cb.setRenderer(new StatusLCR());
		CbUtil.fillCbStatus(cb, sel);
		addField(panel, "status", "", cb, null, opt);
		return (cb);
	}

	/**
	 * short way to get a JButton
	 *
	 * @param name
	 * @param data
	 * @param icon
	 * @param tooltip
	 * @param act optional action
	 *
	 * @return
	 */
	public static JButton initButton(String name, DATA data,
		ICONS.K icon, String tooltip, ActionListener... act) {
		if (data == null) {
			return initButton(name, "", icon, tooltip, act);
		}
		return initButton(name, data.i18n(), icon, tooltip, act);
	}

	/**
	 * get a JButton
	 *
	 * @param name
	 * @param text
	 * @param icon
	 * @param tooltip
	 * @param act optional action
	 *
	 * @return
	 */
	public static JButton initButton(String name, String text, ICONS.K icon,
		String tooltip, ActionListener... act) {
		//LOG.trace(TT+".initButton(name=" + name + ",
		//text=" + text + ", icon=" + icon.toString() + ")");
		JButton btn = new JButton();
		btn.setName(name);
		if (text != null && !text.isEmpty()) {
			if (text.equals("...") || text.trim().isEmpty()) {
				btn.setText(text);
			} else if (!text.isEmpty() && !text.equals(DATA.NIL.i18n())) {
				btn.setText(I18N.getMsg(text));
			}
		}
		if (icon != null && icon != ICONS.K.NONE && icon != ICONS.K.EMPTY) {
			btn.setIcon(IconUtil.getIconSmall(icon));
			if (text == null || text.isEmpty()) {
				btn.setMargin(new Insets(0, 0, 0, 0));
				btn.setMaximumSize(IconUtil.getDefDim());
			}

		}
		if (!tooltip.isEmpty()) {
			btn.setToolTipText(I18N.getMsg(tooltip));
		}
		if (act != null && act.length > 0) {
			btn.addActionListener(act[0]);
		}
		return btn;
	}

	/**
	 * get a JToggleButton
	 *
	 * @param name
	 * @param text
	 * @param icon
	 * @param act
	 *
	 * @return
	 */
	public static JToggleButton initToggleButton(String name,
		String text, ICONS.K icon, ActionListener act) {
		JToggleButton bt = new JToggleButton();
		bt.setName(name);
		if (!text.isEmpty()) {
			bt.setText(text);
		}
		if (icon != null && icon != ICONS.K.NONE && icon != ICONS.K.EMPTY) {
			bt.setIcon(IconUtil.getIconSmall(icon));
		}
		bt.setToolTipText(I18N.getMsg(name));
		if (act != null) {
			bt.addActionListener(act);
		}
		return bt;
	}

	/**
	 * get a JTextArea like a multiline label
	 *
	 * @param text
	 * @param border optional
	 *
	 * @return
	 */
	public static JTextArea MultiLineLabel(String text, boolean... border) {
		JLabel lb = new JLabel();
		JTextArea ta = new JTextArea(text);
		ta.setFont(FontUtil.getItalic(lb.getFont()));
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		ta.setEditable(false);
		if (border != null && border.length > 0) {
			JTextField tf = new JTextField();
			ta.setBorder(tf.getBorder());
		}
		ta.setBackground(new Color(lb.getBackground().getRGB(), true));
		ta.setForeground(new Color(lb.getForeground().getRGB(), true));
		ta.setOpaque(lb.isOpaque());
		return ta;
	}

	/**
	 * add a Shortcut
	 *
	 * @param listener: source AbstractPanel where usable
	 * @param comp: component, a JButton for exemple
	 * @param name: name of the action
	 * @param shortcut: shortcut String
	 * @param action : action to perform, optional
	 */
	public static void addShortcut(ActionListener listener,
		JComponent comp,
		String name,
		String shortcut,
		Action... action) {
		KeyStroke key = Shortcuts.getKey("shortcut.k." + shortcut);
		comp.getInputMap().put(key, name);
		if (action != null && action.length > 0) {
			comp.getActionMap().put(name, action[0]);
		} else {
			comp.registerKeyboardAction(listener, name, key, JComponent.WHEN_FOCUSED);
		}
	}

}
