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
package storybook.dialog;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.book.Book;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public abstract class AbsDialog extends JDialog implements ActionListener {

	public static boolean MANDATORY = true, TOP = true;
	protected MainFrame mainFrame;
	protected Book book;
	protected JComponent parent;
	protected boolean canceled = false;

	public AbsDialog() {
		this.setModal(true);
	}

	public AbsDialog(AbsDialog dlg) {
		super(dlg);
		this.mainFrame = dlg.getMainFrame();
		this.book = mainFrame.getBook();
		this.parent = null;
		this.setModal(true);
	}

	public AbsDialog(MainFrame mainFrame) {
		super(mainFrame);
		this.mainFrame = mainFrame;
		if (mainFrame != null) {
			this.book = mainFrame.getBook();
		}
		this.parent = null;
		this.setModal(true);
	}

	public AbsDialog(JComponent parent) {
		this.parent = parent;
		this.mainFrame = null;
		this.setModal(true);
	}

	public abstract void init();

	public void initUi() {
		if (mainFrame != null) {
			setIconImage(mainFrame.getIconImage());
		}
	}

	public void initAll() {
		init();
		initUi();
	}

	public void addOkCancel() {
		add(getOkButton(), MIG.get(MIG.RIGHT, MIG.SPLIT2));
		add(getCancelButton());
	}

	public MainFrame getMainFrame() {
		return (mainFrame);
	}

	/**
	 * initialize a field label
	 *
	 * @param title the title of the field
	 * @param icon the icon of the field
	 * @param mandatory true if this field is mandatory
	 * @return the intialized JTextField
	 */
	public JLabel initLabel(String title, String icon, boolean mandatory) {
		JLabel lb = new JLabel();
		if (icon != null && !icon.isEmpty()) {
			lb.setIcon(IconUtil.getIconSmall(icon));
		}
		if (title != null && !title.isEmpty()) {
			String m = "";
			if (mandatory) {
				m = "*";
				lb.setFont(FontUtil.getBold());
			}
			m = m + I18N.getColonMsg(title);
			lb.setText(m);
		}
		return (lb);
	}

	/**
	 * initialize a titled JTextField
	 *
	 * @param p the JPanel where field will be added
	 * @param name the name of the field
	 * @param title the title of the field
	 * @param icon the icon of the field
	 * @param len the len of the JTextField
	 * @param value value to initialize
	 * @param mandatory true if this field is mandatory
	 * @param grow MingInfo instruction for adding the JTextField
	 * @return the intialized JTextField
	 */
	public JTextField initTF(JPanel p, String name, String title, String icon, int len,
			String value, boolean mandatory, String grow) {
		p.add(initLabel(title, icon, mandatory));
		JTextField tf = new JTextField();
		tf.setName(name);
		if (value != null && !value.isEmpty()) {
			tf.setText(value);
		}
		if (len > 0) {
			tf.setColumns(len);
		}
		tf.addActionListener(this);
		p.add(tf, grow);
		return (tf);
	}

	/**
	 * initialize a titled JTextArea
	 *
	 * @param p the JPanel where field will be added
	 * @param name the name of the field
	 * @param title the title of the field
	 * @param top true for positioning the label at top
	 * @param icon the icon of the field
	 * @param cols the number of columns of the field
	 * @param rows the number of rows of the field
	 * @param value value to initialize
	 * @param mandatory true if this field is mandatory
	 * @param grow MingInfo instruction(s) for adding the JTextArea
	 * @return the intialized JTextArea
	 */
	public JTextArea initTA(JPanel p, String name, String title, boolean top, String icon, int cols, int rows,
			String value, boolean mandatory, String grow) {
		p.add(initLabel(title, icon, mandatory), (top ? MIG.TOP : ""));

		JTextArea tf = new JTextArea();
		tf.setName(name);
		if (value != null && !value.isEmpty()) {
			tf.setText(value);
		}
		if (cols > 0) {
			tf.setColumns(cols);
		}
		if (rows > 0) {
			tf.setLineWrap(true);
			tf.setRows(rows);
		}
		JScrollPane scroll = new JScrollPane(tf);
		SwingUtil.setMaxPreferredSize(scroll);
		p.add(scroll, grow);
		return (tf);
	}

	/**
	 * initialize a JCheckBox
	 *
	 * @param p the JPanel where to set the JCheckBox
	 * @param name the name of the JCheckBox
	 * @param title the title to set
	 * @param sel true if the JCheckBox must be selected
	 * @param mandatory if this one is mandatory
	 * @param grow Minginfo instruction
	 *
	 * @return the initialized JCheckBox
	 */
	public JCheckBox initCB(JPanel p, String name, String title,
			boolean sel, boolean mandatory, String grow) {
		JCheckBox cb = new JCheckBox((mandatory ? "*" : "") + I18N.getMsg(title));
		if (mandatory) {
			cb.setFont(FontUtil.getBold());
		}
		cb.setName(name);
		if (sel) {
			cb.setSelected(true);
		}
		p.add(cb, grow);
		cb.addActionListener(this);
		return (cb);
	}

	protected JButton getButton(String text, ICONS.K icon, String tips) {
		return (SwingUtil.createButton(text, icon, tips));
	}

	protected JButton getButton(String text, String icon, String tips) {
		return (SwingUtil.createButton(text, icon, tips, true));
	}

	protected JButton getButton(String text, String icon) {
		return (getButton(text, icon, ""));
	}

	protected JButton getButton(String text, ICONS.K icon) {
		return (getButton(text, icon, ""));
	}

	protected JButton getButton(String text) {
		return (getButton(text, "", ""));
	}

	protected JButton getOkButton() {
		AbstractAction act = getOkAction();
		JButton bt = new JButton(act);
		bt.setFont((App.fonts.defGet()));
		bt.setText(I18N.getMsg("ok"));
		bt.setIcon(IconUtil.getIconSmall(ICONS.K.OK));
		SwingUtil.addEnterAction(bt, act);
		return bt;
	}

	protected JButton getOkButton(String txt) {
		AbstractAction act = getOkAction();
		JButton bt = new JButton(act);
		bt.setFont((App.fonts.defGet()));
		bt.setText(I18N.getMsg(txt));
		bt.setIcon(IconUtil.getIconSmall(ICONS.K.OK));
		SwingUtil.addEnterAction(bt, act);
		return bt;
	}

	protected JButton getCancelButton() {
		AbstractAction act = getCancelAction();
		JButton bt = new JButton(act);
		bt.setFont((App.fonts.defGet()));
		bt.setText(I18N.getMsg("cancel"));
		bt.setIcon(IconUtil.getIconSmall(ICONS.K.CANCEL));
		SwingUtil.addEscAction(bt, act);
		return bt;
	}

	protected JButton getCloseButton() {
		JButton bt = new JButton(getOkAction());
		bt.setFont((App.fonts.defGet()));
		bt.setIcon(IconUtil.getIconSmall(ICONS.K.CLOSE));
		bt.setText(I18N.getMsg("close"));
		return bt;
	}

	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				dispose();
			}
		};
	}

	protected AbstractAction getCancelAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		};
	}

	public boolean isCanceled() {
		return canceled;
	}
}
