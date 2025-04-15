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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package i18n;

import api.mig.swing.MigLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import resources.icons.ICONS;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.dialog.ExceptionDlg;
import storybook.tools.LOG;
import storybook.tools.StringUtil;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSTable;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class I18NDlg extends AbsDialog {

	private static final String TT = "I18NDlg",
			PROP_EXT = ".properties";

	private JComboBox<String> cbLanguage;
	private JButton btSave;
	private JSTable msgTable;
	private JTextArea text;
	private JTextArea txComment;
	private JTextPane infoPane1;
	private ArrayList<String> languages;
	private DefaultTableModel msgModel;
	boolean bModified = false;
	private String curLanguage;

	public I18NDlg(MainFrame m) {
		super(m);
		initAll();
	}

	public static void show(MainFrame m) {
		SwingUtil.showDialog(new I18NDlg(m), m, true);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		JToolBar.Separator separator = new JToolBar.Separator();
		JLabel spacer = new JLabel(" ");
		JToolBar toolbar = new JToolBar();
		toolbar.setLayout(new MigLayout());
		toolbar.setRollover(true);

		JLabel lbLanguages = new JLabel(I18N.getMsg("language") + ":");
		toolbar.add(lbLanguages);
		cbLanguage = new JComboBox<>();
		cbLanguage.addItemListener((ItemEvent evt) -> {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				changeLanguage();
			}
		});
		toolbar.add(cbLanguage);
		toolbar.add(separator);

		JButton btNew = Ui.initButton("btNew", "", ICONS.K.F_NEW, "new",
				(evt -> btNewAction()));
		toolbar.add(btNew);
		JButton btOpen = Ui.initButton("btOpen", "", ICONS.K.F_OPEN, "open",
				(evt -> btOpenAction()));
		toolbar.add(btOpen);
		toolbar.add(separator);

		JButton btRefresh = Ui.initButton("btRefresh", "", ICONS.K.REFRESH, "refresh",
				(ActionEvent evt) -> {
					bModified = false;
					changeLanguage();
				});
		toolbar.add(btRefresh);
		toolbar.add(separator);

		btSave = Ui.initButton("btSave", "", ICONS.K.F_SAVE, "save", (evt -> save()));
		toolbar.add(btSave);
		toolbar.add(spacer, MIG.GROWX);
		JButton btExit = Ui.initButton("btExit", "", ICONS.K.EXIT, "exit", (evt -> onExit()));
		toolbar.add(btExit, MIG.get(MIG.SPAN, MIG.RIGHT));
		msgTable = new JSTable();
		initTable();
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		model.addElement(I18N.getMsg("language.select"));
		languages = new ArrayList<>();
		for (Const.Language lang : Const.Language.values()) {
			model.addElement(lang.name().substring(0, 2) + " " + lang.getI18N());
			languages.add(lang.name()/*.substring(0, 2)*/);
		}
		cbLanguage.setModel(model);
		this.addWindowListener(new WindowAdapter() {
			private boolean bModified;

			@Override
			public void windowClosing(WindowEvent e) {
				if (bModified) {
					int confirmed = JOptionPane.showConfirmDialog(null,
							I18N.getMsg("language.confirm"),
							I18N.getMsg("confirm"),
							JOptionPane.YES_NO_OPTION);
					if (confirmed == JOptionPane.YES_OPTION) {
						dispose();
					}
				} else {
					dispose();
				}
			}
		});
		msgTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				selectRow();
			}
		});
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setViewportView(msgTable);

		JLabel lbText = new JLabel(I18N.getColonMsg("text"));
		text = new JTextArea();
		text.setColumns(20);
		text.setRows(5);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.addCaretListener(evt -> textCaretUpdate(evt));
		JScrollPane scrollInfoText = new JScrollPane();
		scrollInfoText.setViewportView(text);

		JLabel lbComment = new JLabel(I18N.getColonMsg("language.comment"));
		JScrollPane scrollComment = new JScrollPane();
		txComment = new javax.swing.JTextArea();
		txComment.setEditable(false);
		txComment.setColumns(25);
		txComment.setRows(5);
		txComment.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				bModified = true;
			}
		});
		scrollComment.setViewportView(txComment);

		infoPane1 = new JTextPane();

		infoPane1.setEditable(false);
		infoPane1.setText("{0}, {1}, ... are for insertion of variables data, they are mandatory.\n\n"
				+ "\\n stands for line feed.\n\n"
				+ "Don't translate the 'shortcut.k...' - it's only for keyboard shortcuts,\n"
				+ "which remain the same in any language.");
		JScrollPane scrollInfo = new JScrollPane();
		scrollInfo.setViewportView(infoPane1);
		scrollInfo.setFocusable(false);

		//layout
		setLayout(new MigLayout("wrap 9"));
		setTitle("UI Translation");
		add(toolbar, MIG.get(MIG.GROWX, MIG.SPAN));
		add(lbComment, MIG.WRAP);
		add(scrollComment, MIG.get(MIG.SG, MIG.SPAN, MIG.SPLIT2, MIG.GROW));
		add(scrollInfo, MIG.get(MIG.GROW, MIG.WRAP));
		add(scrollTable, MIG.get(MIG.SPAN, MIG.GROWX));
		add(lbText, MIG.SPAN);
		add(scrollInfoText, MIG.get(MIG.SPAN, MIG.GROWX));
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
		msgTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		msgTable.pack();
	}

	private void btNewAction() {
		I18NnewDlg dlg = new I18NnewDlg(this, mainFrame, languages);
		dlg.setVisible(true);
		String str = dlg.getLanguage();
		if (str.isEmpty()) {
			return;
		}
		cbLanguage.addItem(str);
		cbLanguage.setSelectedItem(str);
		changeLanguage();
	}

	private void btOpenAction() {
		File f = IOUtil.fileSelect(this,
				App.preferences.getString(Pref.KEY.LASTOPEN_DIR, ""),
				PROP_EXT,
				"Properties file (*.properties)",
				"file.select");
		if (f == null) {
			return;
		}
		curLanguage = "file:" + f.getAbsolutePath();
		cbLanguage.addItem(curLanguage);
		cbLanguage.setSelectedItem(curLanguage);
		changeLanguage();
	}

	private void textCaretUpdate(javax.swing.event.CaretEvent evt) {
		String str = text.getText().replace("\n", "\\n");
		int row = msgTable.getSelectedRow();
		if (row != -1) {
			msgTable.setValueAt(str, row, 2);
		}
	}

	private void initTable() {
		List<String> cols = new ArrayList<>();
		cols.add(I18N.getMsg("internal.key"));
		cols.add(I18N.getMsg("language.default"));
		cols.add(" ");
		msgModel = new DefaultTableModel(cols.toArray(), 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 2) {
					return (true);
				}
				return (false);
			}
		};
		// read all keys
		InputStream stream = App.getInstance().getClass().getResourceAsStream("/i18n/msg/messages.properties");
		BufferedReader txt = new BufferedReader(new InputStreamReader(stream));
		String line;
		try {
			line = txt.readLine();
			while (line != null) {
				if (!line.isEmpty() && !(line.startsWith("#") || line.startsWith(" ")) && line.contains("=")) {
					String[] s = line.split("=");
					s[0] = s[0].trim();
					s[1] = s[1].trim();
					if (!s[0].startsWith("language.")) {
						msgModel.addRow(s);
					}
				}
				line = txt.readLine();
			}
			txt.close();
		} catch (IOException ex) {
			LOG.err("DlgI18N error", ex);
		}
		msgTable.setModel(msgModel);
		//table.setSortOrder(0, SortOrder.ASCENDING);
		CellEditorListener ChangeNotification = new CellEditorListener() {
			@Override
			public void editingCanceled(ChangeEvent e) {
				// empty
			}

			@Override
			public void editingStopped(ChangeEvent e) {
				bModified = true;
				int row = msgTable.getSelectedRow();
				text.setText((String) msgTable.getValueAt(row, 2));
			}
		};
		msgTable.getDefaultEditor(String.class).addCellEditorListener(ChangeNotification);
	}

	private void changeLanguage() {
		if (cbLanguage.getSelectedIndex() < 1) {
			return;
		}
		if (msgModel.getRowCount() > 0) {
			for (int i = msgTable.getRowCount() - 1; i > 0; i--) {
				msgModel.setValueAt("", i, 2);
			}
		}
		curLanguage = (String) cbLanguage.getSelectedItem();
		if (curLanguage.contains("=")) {
			return;
		}
		String bundler;
		Object src;
		if (curLanguage.startsWith("file:")) {
			bundler = curLanguage.replace("file:", "");
			bundler = bundler.replace(PROP_EXT, "");
			String path = bundler.substring(0, bundler.lastIndexOf("/"));
			String name = bundler.substring(bundler.lastIndexOf("/") + 1);
			URL resourceURL;
			File fl = new File(path);
			try {
				resourceURL = fl.toURI().toURL();
			} catch (MalformedURLException ex) {
				LOG.err("ChangeLanguage", ex);
				return;
			}
			URLClassLoader urlLoader = new URLClassLoader(new URL[]{resourceURL});
			src = ResourceBundle.getBundle(name, java.util.Locale.getDefault(), urlLoader);
		} else {
			bundler = "i18n.msg.messages" + "_" + languages.get(cbLanguage.getSelectedIndex() - 1);
			try {
				src = ResourceBundle.getBundle(bundler);
			} catch (Exception ex) {
				bundler = bundler.substring(0, bundler.lastIndexOf("_"));
				src = ResourceBundle.getBundle(bundler);
			}
		}
		setLanguageHeader();
		bModified = false;
		for (int i = 0; i < msgModel.getRowCount(); i++) {
			String s1 = (String) msgModel.getValueAt(i, 0);
			String s2 = "";
			try {
				Object x = ((ResourceBundle) src).getObject(s1);
				s2 = ((String) x).replace("\n", "\\n");
			} catch (Exception e) {
			}
			if (s2.equals("")) {
				continue;
			}
			msgModel.setValueAt(s2.trim(), i, 2);
		}
		readComment(curLanguage.substring(0, 2).toLowerCase());
		selectRow();
		text.setEditable(true);
		msgTable.packAll();
	}

	private void selectRow() {
		int row = msgTable.getSelectedRow();
		if (row == -1) {
			return;
		}
		int col = 2;
		if (msgTable.getValueAt(row, col) != null) {
			String str = ((String) msgTable.getValueAt(row, col)).replace("\\n", "\n");
			text.setText(str);
			text.setCaretPosition(0);
		}
	}

	private void save() {
		//LOG.trace(TT + ".save() curLanguage=" + curLanguage);
		String lng = (String) cbLanguage.getSelectedItem();
		String filename;
		if (lng.startsWith("file:")) {
			filename = lng.replace("file:", "");
		} else {
			filename = EnvUtil.getHomeDir().getAbsolutePath()
					+ File.separator
					+ "messages_"
					+ lng + PROP_EXT;
		}
		File dir = new File(filename);
		dir = IOUtil.fileSelect(this,
				dir.getAbsolutePath(),
				PROP_EXT,
				"*.properties file",
				"file.save");
		if (dir == null) {
			return;
		}
		try (Writer out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(dir), "UTF-8"))) {
			if (!txComment.getText().isEmpty()) {
				out.write("#" + txComment.getText().replace("\n", "\n#") + "\n");
			}
			out.write("#Date: " + new java.util.Date() + "\n");
			for (int i = 0; i < msgTable.getRowCount(); i++) {
				String val = (String) msgTable.getValueAt(i, 2);
				if (val != null && !val.isEmpty()) {
					val = StringUtil.escapeJava(val);
					out.write((String) msgTable.getValueAt(i, 0) + "=" + val + "\n");
				}
			}
			out.flush();
			bModified = false;
		} catch (Exception ex) {
			LOG.err(TT + ".save() exception", ex);
		}
	}

	private void readComment(String lang) {
		//LOG.trace(TT + ".readComment(lang=" + lang + ")");
		String fileName = "msg/messages_" + lang + PROP_EXT;
		InputStream stream;
		if (curLanguage.startsWith("file:")) {
			try {
				stream = new FileInputStream(curLanguage.replace("file:", ""));
			} catch (FileNotFoundException ex) {
				LOG.err("readComment", ex);
				return;
			}
		} else {
			stream = I18N.class.getResourceAsStream(fileName);
			if (stream == null) {
				String lng = languages.get(cbLanguage.getSelectedIndex() - 1);
				fileName = "msg/messages_" + lng + PROP_EXT;
				stream = I18N.class.getResourceAsStream(fileName);
				if (stream == null) {
					ExceptionDlg.show(this.getClass().getSimpleName()
							+ ".readComment(lang=" + lang + ") no language file",
							null);
				}
			}
		}
		InputStreamReader streamreader = new InputStreamReader(stream);
		BufferedReader txt = new BufferedReader(streamreader);
		String line;
		StringBuilder comment = new StringBuilder();
		try {
			line = txt.readLine();
			while (line != null) {
				if (!line.isEmpty() && line.startsWith("#")) {
					comment.append(line.replaceFirst("#", "").trim()).append("\n");
				} else {
					break;
				}
				line = txt.readLine();
			}
			txt.close();
			txComment.setText(comment.toString());
			txComment.setEditable(true);
			txComment.setCaretPosition(0);
		} catch (IOException ex) {
			LOG.err("I18NDlg error", ex);
		}
	}

	private int onExit() {
		if (bModified) {
			if (JOptionPane.showConfirmDialog(null,
					I18N.getMsg("language.confirm"),
					I18N.getMsg("confirm"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				dispose();
				return (DISPOSE_ON_CLOSE);
			} else {
				//dispose();
				return (DO_NOTHING_ON_CLOSE);
			}
		} else {
			dispose();
		}
		return (DO_NOTHING_ON_CLOSE);
	}

	private void setLanguageHeader() {
		msgTable.getTableHeader().getColumnModel().getColumn(2).setHeaderValue(curLanguage);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
