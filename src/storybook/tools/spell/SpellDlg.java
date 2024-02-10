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
package storybook.tools.spell;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.dialog.EditDictionaryDlg;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.net.Net;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.BNONE;

/**
 *
 * @author favdb
 */
public class SpellDlg extends AbsDialog {

	private static String TT = "SpellDlg.";

	private static SpellDlg thisDlg;
	private static List<SpellUtil.Language> languages;
	private static String installed;
	private static JFrame frmMain;
	private static Container pane;
	private static JProgressBar barDo;
	private static JButton btnCancelLoad;
	private static JLabel txTitle, txNb;
	static String language;
	private static List<String> dicos;
	static String inFile;
	private static boolean isLoadCanceled;
	private static URL urlDico;
	private static File destDico;
	private JList<String> lsInstalled;
	private static JList<String> lsLoadable;
	private JButton btSelectLang;
	private JButton btRefresh;
	private JButton btDownload;
	private JCheckBox ckEnable;

	public static void install() {
		SpellDlg dlg = new SpellDlg(null);
		dlg.refreshDicts();
		dlg.setVisible(true);
	}

	public SpellDlg(MainFrame m) {
		super(m);
		thisDlg = this;
		initAll();
	}

	@Override
	public void init() {
		languages = SpellUtil.getLanguages();
	}

	/**
	 * initialize UI
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP, "[][]"));
		setTitle(I18N.getMsg("jortho.spelling"));
		add(initPanel1());
		add(initPanel2(), MIG.TOP);

		JButton btUserDict = new JButton(I18N.getMsg("jortho.userDictionary"));
		btUserDict.addActionListener((java.awt.event.ActionEvent evt) -> {
			EditDictionaryDlg dlg = new EditDictionaryDlg(mainFrame);
			dlg.setVisible(true);
		});
		add(btUserDict, MIG.LEFT);
		ckEnable = Ui.initCheckBox(null, "ckEnable", "jortho.enable",
		   App.preferences.getBoolean(Pref.KEY.SPELLING_ENABLE), BNONE,
		   e -> enableSpellcheck());
		add(ckEnable, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(getCloseButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	@SuppressWarnings("unchecked")
	private JPanel initPanel1() {
		JPanel panel1 = new JPanel(new MigLayout(MIG.get(MIG.INS0)));
		panel1.setBorder(BorderFactory
		   .createTitledBorder(I18N.getMsg("spelling.dicts.installed")));

		lsInstalled = new JList();
		initInstalled();
		lsInstalled.addListSelectionListener(evt -> btSelectLang.setEnabled(true));
		JScrollPane scroller1 = new JScrollPane();
		scroller1.setViewportView(lsInstalled);
		panel1.add(scroller1, "wrap");
		btSelectLang = new JButton(I18N.getMsg("spelling.select"));
		btSelectLang.setIcon(IconUtil.getIconSmall(ICONS.K.OK));
		btSelectLang.setEnabled(false);
		btSelectLang.addActionListener(evt -> applySettings());
		panel1.add(btSelectLang, "center");
		return panel1;
	}

	private JPanel initPanel2() {
		JPanel panel2 = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.WRAP1)));
		JPanel panel21 = new JPanel(new MigLayout());
		panel21.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("spelling.dicts.downloadable")));
		panel2.add(panel21);
		lsLoadable = new javax.swing.JList<>();
		lsLoadable.addListSelectionListener(evt -> btDownload.setEnabled(true));
		JScrollPane scroller2 = new javax.swing.JScrollPane();
		scroller2.setViewportView(lsLoadable);
		btRefresh = new JButton(I18N.getMsg("spelling.dicts.refresh"));
		btRefresh.setIcon(IconUtil.getIconSmall(ICONS.K.REFRESH));
		btRefresh.addActionListener((java.awt.event.ActionEvent evt) -> {
			refreshDicts();
		});
		btDownload = new JButton(I18N.getMsg("spelling.dicts.download"));
		btDownload.setIcon(IconUtil.getIconSmall(ICONS.K.AR_DOWN));
		btDownload.setEnabled(false);
		btDownload.addActionListener(evt -> loadDict());
		panel21.add(scroller2, MIG.get(MIG.GROW, MIG.WRAP));
		panel21.add(btRefresh, MIG.get(MIG.SPLIT2, MIG.CENTER));
		panel21.add(btDownload);

		// for installing CkEditor
		/*
		JPanel panel22 = new JPanel(new MigLayout());
		JButton btCkEditor = new JButton(I18N.getMsg("spelling.ckeditor_install"));
		btCkEditor.addActionListener(evt -> Spelling.installCkEditor(this));
		panel22.add(btCkEditor, MIG.GROWX);
		panel2.add(panel22, MIG.RIGHT);
		 */
		return panel2;
	}

	@SuppressWarnings("unchecked")
	private void initInstalled() {
		DefaultListModel model = new DefaultListModel();
		installed = SpellUtil.getAllDicts();
		String curLanguage = App.preferences.getString(Pref.KEY.SPELLING, Const.Spelling.none.name());
		int selLanguage = -1;
		int i = 0;
		for (SpellUtil.Language lang : languages) {
			if (installed.contains(lang.getCode())) {
				model.addElement(lang.getCode() + "=" + lang.getName());
				if (lang.getCode().equals(curLanguage.substring(0, 2))) {
					selLanguage = i;
				}
				i++;
			}
		}
		lsInstalled.setModel(model);
		lsInstalled.setSelectedIndex(selLanguage);
	}

	private void applySettings() {
		String i = lsInstalled.getSelectedValue();
		for (SpellUtil.Language lang : languages) {
			if (i.equals(lang.getCode() + "=" + lang.getName())) {
				App.preferences.set(Pref.KEY.SPELLING.toString(), lang.getCode() + "," + lang.toString());
				break;
			}
		}
		App.preferences.setBoolean(Pref.KEY.SPELLING_ENABLE, ckEnable.isSelected());
		SpellUtil.registerDictionaries();
		dispose();
	}

	private void loadDict() {
		String i2 = lsLoadable.getSelectedValue().substring(0, 2);
		String lang = null;
		for (SpellUtil.Language l : languages) {
			if (i2.equals(l.getCode())) {
				lang = l.getCode();
				break;
			}
		}
		if (lang == null) {
			return;
		}
		dicos = new ArrayList<>();
		dicos.add("dictionary_" + lang + ".ortho");
		dicos.add("thesaurus_" + lang + ".ocid");

		//Create all components
		frmMain = new JFrame();
		frmMain.setUndecorated(true);
		frmMain.getRootPane().setBorder(
		   BorderFactory.createCompoundBorder(
			  BorderFactory.createLoweredBevelBorder(),
			  BorderFactory.createRaisedBevelBorder())
		);
		frmMain.setTitle(I18N.getMsg("dlg.build.build.progress"));
		frmMain.setLocationRelativeTo(this);
		frmMain.setSize(410, 100);
		pane = frmMain.getContentPane();
		pane.setLayout(new MigLayout(MIG.WRAP));
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		btnCancelLoad = new JButton("Cancel");
		btnCancelLoad.addActionListener(
		   new SpellDlg.btnCancelLoadAction());
		barDo = new JProgressBar(0, 100);
		txTitle = new JLabel("");

		//Add components to pane
		pane.add(txTitle);
		pane.add(barDo, MIG.CENTER);
		pane.add(btnCancelLoad, MIG.RIGHT);

		//Make frame visible
		frmMain.setResizable(false);
		frmMain.setVisible(true);
		frmMain.setAlwaysOnTop(true);

		new Thread(new SpellDlg.TheThread()).start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	private void enableSpellcheck() {
		App.preferences.setBoolean(Pref.KEY.SPELLING_ENABLE, ckEnable.isSelected());
		if (!ckEnable.isSelected()) {
			ShefEditor.setSpelling("none");
		} else {
			ShefEditor.setSpelling(App.preferences.getString(Pref.KEY.SPELLING));
		}
	}

	private boolean isSpellEnabled() {
		return ckEnable.getText().equals(I18N.getMsg("jortho.enable"));
	}

	public static class btnCancelLoadAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			isLoadCanceled = true;
		}

	}

	@SuppressWarnings("unchecked")
	public void refreshDicts() {
		URL url;
		try {
			url = new URL(Net.KEY.DICTIONARIES.toString() + "list.txt");
		} catch (MalformedURLException ex) {
			LOG.err(TT + "refreshDicts() error", ex);
			return;
		}
		String dico = "dictionary_";
		String remote = "";
		try (@SuppressWarnings("null") BufferedReader in
		   = new BufferedReader(new InputStreamReader(url.openStream()))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains(dico) && inputLine.contains("ortho")) {
					String str = inputLine.substring(inputLine.indexOf(dico));
					str = str.substring(0, str.indexOf(".ortho"));
					str = str.replace(dico, "");
					remote += str + ",";
				}
			}
			in.close();
		} catch (IOException ex) {
			LOG.err(TT + "refreshDicts() error", ex);
			return;
		}
		if (remote.isEmpty()) {
			JOptionPane.showMessageDialog(this,
			   I18N.getMsg("spelling.no_more"),
			   I18N.getMsg("warning"),
			   JOptionPane.ERROR_MESSAGE);
			return;
		}
		DefaultListModel model = new DefaultListModel();
		for (SpellUtil.Language l : languages) {
			if (remote.contains(l.getCode())) {
				model.addElement(l.getCode() + "=" + l.getName());
			}
		}
		lsLoadable.setModel(model);
	}

	public static class TheThread implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			int n = 1;
			for (String dico : dicos) try {
				txTitle.setText("(" + n + "/" + dicos.size() + ") " + dico);
				barDo.setValue(0);
				LOG.trace(TT + "run() download " + dico);
				urlDico = new URL(Net.KEY.DICTIONARIES.toString() + dico);
				File dir = EnvUtil.getPrefDir();
				destDico = new File(dir.getPath()
				   + File.separator
				   + Const.SpellCheker.USER_DICTS
				   + File.separator + dico
				);
				URLConnection connection = urlDico.openConnection();
				int total = connection.getContentLength();
				barDo.setMaximum(total);
				barDo.setStringPainted(true);
				BufferedInputStream inputStream
				   = new BufferedInputStream(connection.getInputStream());
				try (BufferedOutputStream outputStream
				   = new BufferedOutputStream(new FileOutputStream(destDico))) {
					int j = 0;
					int current = 0;
					int bytesRead;
					barDo.setString(current + "/" + total);
					byte[] buffer = new byte[4096];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						if (isLoadCanceled) {
							break;
						}
						outputStream.write(buffer, 0, bytesRead);
						current += bytesRead;
						barDo.setString(current + "/" + total);
						barDo.setValue(current);
						barDo.repaint();
						j = 0;
						Thread.sleep(1);
					}
					outputStream.flush();
					outputStream.close();
					//LOG.trace(TT+"run() downloaded=" + current + " bytes");
				} catch (InterruptedException ex) {
					LOG.err(TT + "run() " + ex.getLocalizedMessage(), ex);
					return;
				}
				if (isLoadCanceled) {
					if (destDico.exists()) {
						destDico.delete();
					}
				} else {
					thisDlg.initInstalled();
					thisDlg.repaint();
				}
				frmMain.setVisible(false);
			} catch (MalformedURLException ex) {
				LOG.err(TT + "run() " + ex.getLocalizedMessage(), ex);
			} catch (IOException ex) {
				LOG.err(TT + "run() " + ex.getLocalizedMessage(), ex);
			}
		}

	}

}
