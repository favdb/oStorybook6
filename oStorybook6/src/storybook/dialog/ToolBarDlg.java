/*
 * Copyright (C) 2017 favdb
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
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ToolBarDlg extends AbsDialog {

	private static final String TT = "ToolBarDlg",
			FILE = "file",
			NEW = "new",
			TABLE = "table",
			VIEW = "view";

	/* desciption des boutons
	paramètre 1 = groupe (file,new, table, view
	paramètre 2 =
	paramètre 3 = nom de l'icone
	paramètre 4 = texte du tooltips
	 */
	String boutons[][] = {
		{FILE, "0", ICONS.K.F_NEW.toString(), "file.new"},
		{FILE, "0", ICONS.K.F_OPEN.toString(), "file.open"},
		{FILE, "0", ICONS.K.F_SAVE.toString(), "file.save"},
		{NEW, "0", ICONS.K.NEW_STRAND.toString(), "strand.new"},
		{NEW, "0", ICONS.K.NEW_PART.toString(), "part.new"},
		{NEW, "0", ICONS.K.NEW_CHAPTER.toString(), "chapter.new"},
		{NEW, "0", ICONS.K.NEW_SCENE.toString(), "scene.new"},
		{NEW, "1", ICONS.K.NEW_PERSON.toString(), "person.new"},
		{NEW, "1", ICONS.K.NEW_GENDER.toString(), "gender.new"},
		{NEW, "1", ICONS.K.ENT_RELATION.toString(), "relation.new"},
		{NEW, "2", ICONS.K.NEW_LOCATION.toString(), "location.new"},
		{NEW, "3", ICONS.K.NEW_ITEM.toString(), "item.new"},
		{NEW, "3", ICONS.K.ENT_ITEMLINK.toString(), "itemlink.new"},
		{NEW, "4", ICONS.K.NEW_TAG.toString(), "tag.new"},
		{NEW, "4", ICONS.K.ENT_TAGLINK.toString(), "taglink.new"},
		{NEW, "5", ICONS.K.ENT_MEMO.toString(), "memo.new"},
		{NEW, "6", ICONS.K.ENT_IDEA.toString(), "idea.new"},
		{TABLE, "0", ICONS.K.TABLE_STRANDS.toString(), "strand"},
		{TABLE, "0", ICONS.K.TABLE_PARTS.toString(), "part"},
		{TABLE, "0", ICONS.K.TABLE_CHAPTERS.toString(), "chapter"},
		{TABLE, "0", ICONS.K.TABLE_SCENES.toString(), "scene"},
		{TABLE, "1", ICONS.K.TABLE_PERSONS.toString(), "person"},
		{TABLE, "1", ICONS.K.TABLE_GENDERS.toString(), "gender"},
		{TABLE, "1", ICONS.K.TABLE_RELATIONS.toString(), "relationship"},
		{TABLE, "2", ICONS.K.TABLE_LOCATIONS.toString(), "location"},
		{TABLE, "3", ICONS.K.TABLE_ITEMS.toString(), "item"},
		//{TABLE, "3", ICONS.K.TABLE_ITEMLINKS.toString(), "itemlinks"},
		{TABLE, "4", ICONS.K.TABLE_TAGS.toString(), "tag"},
		//{TABLE, "4", ICONS.K.TABLE_TAGLINKS.toString(), "taglinks"},
		{TABLE, "5", ICONS.K.TABLE_MEMOS.toString(), "memo"},
		{TABLE, "6", ICONS.K.TABLE_IDEAS.toString(), "idea"},
		{VIEW, "0", ICONS.K.VW_CHRONO.toString(), "view.chrono"},
		{VIEW, "0", ICONS.K.VW_TIMELINE.toString(), "view.timeline"},
		{VIEW, "1", ICONS.K.VW_BOOK.toString(), "view.book"},
		{VIEW, "2", ICONS.K.VW_MANAGE.toString(), "view.manage"},
		{VIEW, "3", ICONS.K.VW_READING.toString(), "view.reading"},
		{VIEW, "4", ICONS.K.VW_MEMORIA.toString(), "view.memoria"},
		{VIEW, "5", ICONS.K.VW_STORYBOARD.toString(), "view.storyboard"},
		{VIEW, "6", ICONS.K.VW_TYPIST.toString(), "typist"}
	};
	private JPanel panelFile;
	private JPanel panelNew;
	private JPanel panelTable;
	private JPanel panelView;
	private JButton btSelectAll;
	private JButton btSelectNotAll;
	private JButton btReinit;

	public ToolBarDlg(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new MigLayout(MIG.WRAP, "[][][][]"));
		setTitle(I18N.getMsg("toolbar.select"));
		String param = App.preferences.getString(Pref.KEY.TOOLBAR, Pref.KEY.TOOLBAR.getValue());
		while (param.length() < (boutons.length - 1)) {
			param += "1";// default all button are visible
		}
		if (param.length() > boutons.length) {
			param = param.substring(0, boutons.length);
		}
		while (param.length() < boutons.length) {
			param = param + "1";
		}
		JPanel top = new JPanel(new MigLayout());
		btSelectAll = new JButton(I18N.getMsg("select.all"));
		btSelectAll.addActionListener((java.awt.event.ActionEvent evt) -> {
			selectAll(true);
		});
		top.add(btSelectAll, MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		btSelectNotAll = new JButton(I18N.getMsg("select.notall"));
		btSelectNotAll.addActionListener((java.awt.event.ActionEvent evt) -> {
			selectAll(false);
		});
		top.add(btSelectNotAll);
		btReinit = new JButton(I18N.getMsg("toolbar.reset"));
		btReinit.addActionListener((java.awt.event.ActionEvent evt) -> {
			reinit(true);
		});
		top.add(btReinit);
		add(top, MIG.get(MIG.SPAN, MIG.CENTER));
		panelFile = initPanel(FILE, param);
		add(panelFile);
		panelNew = initPanel(NEW, param);
		add(panelNew);
		panelTable = initPanel(TABLE, param);
		add(panelTable);
		panelView = initPanel(VIEW, param);
		add(panelView);
		add(getOkButton(), MIG.get(MIG.SG, MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(getCancelButton(), MIG.SG);
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	public JPanel initPanel(String name, String param) {
		//LOG.trace(TT + ".initPanel(name=" + name + ", param=" + param + ")"
		//		+ " param length=" + param.length() + ", boutons length=" + boutons.length);
		JPanel p = new JPanel();
		p.setBorder(
				BorderFactory.createTitledBorder(null,
						I18N.getMsg(name),
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION,
						new Font("Dialog", 1, 12)));
		p.setLayout(new MigLayout());
		String line = "0";
		int i = 0;
		while (param.length() < boutons.length) {
			param += "0";
		}
		for (String b[] : boutons) {
			if (i >= boutons.length) {
				break;
			}
			if (b[0].equals(name)) {
				JToggleButton bt = new JToggleButton(IconUtil.getIconSmall(b[2]));
				bt.setName(b[2].substring(b[2].indexOf("/") + 1));
				bt.setToolTipText(I18N.getMsg(b[3]));
				bt.setMargin(new Insets(0, 0, 0, 0));
				bt.setSelected((param.charAt(i) == '1'));
				if (param.charAt(i) == '1') {
					bt.setBorder(BorderFactory.createLineBorder(Color.green, 3));
				} else {
					bt.setBorder(BorderFactory.createLineBorder(Color.red, 3));
				}
				if (!b[1].equals(line)) {
					p.add(new JLabel(""), MIG.WRAP);
					line = b[1];
				}
				bt.addActionListener((java.awt.event.ActionEvent evt) -> {
					JToggleButton bx = (JToggleButton) evt.getSource();
					if (bx.isSelected()) {
						bt.setBorder(BorderFactory.createLineBorder(Color.green, 3));
					} else {
						bt.setBorder(BorderFactory.createLineBorder(Color.red, 3));
					}
				});
				p.add(bt);
			}
			i++;
		}
		return (p);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String param = "";
				param += checkSel(panelFile);
				param += checkSel(panelNew);
				param += checkSel(panelTable);
				param += checkSel(panelView);
				App.preferences.setString(Pref.KEY.TOOLBAR, param);
				canceled = false;
				dispose();
			}
		};
	}

	private String checkSel(JPanel panel) {
		String r = "";
		Component[] comps = panel.getComponents();
		for (Component comp : comps) {
			if (!(comp instanceof JToggleButton)) {
				continue;
			}
			JToggleButton bt = (JToggleButton) comp;
			r += (bt.isSelected() ? "1" : "0");
		}
		return (r);
	}

	private void selectAll(boolean b) {
		setSel(panelFile, b);
		setSel(panelNew, b);
		setSel(panelTable, b);
		setSel(panelView, b);
	}

	private void setSel(JPanel panel, boolean b) {
		Component comps[] = panel.getComponents();
		for (Component comp : comps) {
			if (!(comp instanceof JToggleButton)) {
				continue;
			}
			JToggleButton bt = (JToggleButton) comp;
			bt.setSelected(b);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	private void reinit(boolean b) {
		String param = Pref.KEY.TOOLBAR.getValue();
		reinitPanel(panelFile, FILE, param);
		reinitPanel(panelNew, NEW, param);
		reinitPanel(panelTable, TABLE, param);
		reinitPanel(panelView, VIEW, param);
	}

	private void reinitPanel(JPanel panel, String name, String param) {
		//LOG.trace(TT + ".reinitPanel(panel, name=" + name + ", param=" + param);
		int icomp = 0;
		Component[] comps = panel.getComponents();
		while (!(comps[icomp] instanceof JToggleButton)) {
			icomp++;
		}
		for (int i = 0; i < boutons.length; i++) {
			String b[] = boutons[i];
			if (b[0].equals(name)) {
				while (!(comps[icomp] instanceof JToggleButton)) {
					icomp++;
				}
				if (icomp < comps.length) {
					JToggleButton bt = (JToggleButton) comps[icomp];
					if (param.charAt(i) == '1') {
						bt.setBorder(BorderFactory.createLineBorder(Color.green, 3));
					} else {
						bt.setBorder(BorderFactory.createLineBorder(Color.red, 3));
					}
					icomp++;
				}
			}
		}
	}

}
