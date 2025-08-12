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
package assistant;

import api.mig.swing.MigLayout;
import assistant.app.AppAssistant;
import i18n.I18N;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
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
import java.util.Arrays;
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
import static javax.swing.SwingConstants.CENTER;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import resources.icons.ICONS;
import storybook.App;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.net.Net;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSFileSelector;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.BNONE;

/**
 * dialog for installing an assistant
 *
 * @author favdb
 */
public class AssistantWizardDlg extends AbsDialog implements ListSelectionListener {

	private static final String TT = "AssistantWizardDlg.";

	private JList lsInstalled;
	private JButton btSelAssistant;
	private static JList<String> lsLoadable;
	private JButton btRefresh;
	private JButton btDownload;
	private static URL urlAssistant;
	private static File destAssistant;
	private static JProgressBar barDo;
	private static JButton btnCancelLoad;
	private static boolean isLoadCanceled;
	private static AssistantWizardDlg thisDlg;
	private static JFrame frmMain;
	private static Container pane;
	private static JLabel txTitle;
	private JCheckBox ckLocal;
	private JPanel pLocal;
	private JButton btOK;
	private JSFileSelector slAssistant;
	private JButton btEdit;

	public static void install() {
		AssistantWizardDlg dlg = new AssistantWizardDlg(null);
		dlg.refreshAssistants();
		dlg.setVisible(true);
	}

	public AssistantWizardDlg(MainFrame mainFrame) {
		super(mainFrame);
		thisDlg = this;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.WRAP + " 2")));
		setTitle("Assistant");
		add(initInstalled());
		add(initLoadable(), MIG.GROW);
		add(initLocalFile(), MIG.get(MIG.SPAN, MIG.GROWX));
		// edit the assistant
		btEdit = Ui.initButton("btEdit",
				"assistant.create", ICONS.K.EDIT, "",
				e -> AppAssistant.show("--sub"));
		add(btEdit);
		// validate assistant selection
		btOK = Ui.initButton("btOK", "ok", ICONS.K.OK, "", e -> selectLocal());
		add(btOK, MIG.get(MIG.SPLIT2, MIG.RIGHT));
		// cancel
		add(getCancelButton(), MIG.get(MIG.RIGHT));
		//set current assistant selection
		String str = App.preferences.getString(Pref.KEY.ASSISTANT);
		if (str.contains(File.separator + "Assistant" + File.separator)) {
			File f = new File(str);
			if (!f.exists()) {
				lsInstalled.setSelectedIndex(-1);
				btEdit.setVisible(true);
			} else {
				lsInstalled.setSelectedValue(f.getName(), true);
				//btEdit.setText(I18N.getMsg("edit"));
				btEdit.setVisible(false);
			}
			slAssistant.setFileName("");
			ckLocal.setSelected(false);
		} else if (!str.isEmpty()) {
			lsInstalled.setSelectedIndex(-1);
			slAssistant.setFileName(str);
			ckLocal.setSelected(true);
		}
		ckLocal.setSelected(!slAssistant.getFileName().isEmpty());
		SwingUtil.setEnable(pLocal, ckLocal.isSelected());
		btEdit.setVisible(ckLocal.isSelected());
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	@SuppressWarnings("unchecked")
	private JPanel initInstalled() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[grow]"));
		p.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("assistant.installed")));

		// the installed assistant list
		lsInstalled = new JList();
		DefaultListModel model = new DefaultListModel();
		lsInstalled.setModel(model);
		lsInstalled.setMaximumSize(new Dimension(1024, 800));
		JScrollPane scroll = new JScrollPane(lsInstalled);
		p.add(scroll, MIG.GROW);
		// set the selected assistant as current assistant
		btSelAssistant = Ui.initButton("btSelAssistant", "assistant.select",
				ICONS.K.ASSISTANT, "", e -> selectInstalled());
		btSelAssistant.setEnabled(false);
		p.add(btSelAssistant, MIG.CENTER);
		refreshInstalled();
		return p;
	}

	@SuppressWarnings("unchecked")
	public void refreshInstalled() {
		lsInstalled.removeListSelectionListener(this);
		int index = lsInstalled.getSelectedIndex();
		DefaultListModel model = (DefaultListModel) lsInstalled.getModel();
		model.removeAllElements();
		String dirName = EnvUtil.getPrefDir() + File.separator + "Assistant";
		File dir = new File(dirName);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if (files.length > 0) {
				Arrays.sort(files);
				for (File file : files) {
					if (file.getName().toLowerCase().endsWith(".xml")) {
						model.addElement(file.getName());
					}
				}
			} else {
				LOG.err(TT + " \"" + dirName + "\" is empty");
			}
		} else {
			LOG.err(TT + " \"" + dirName + "\" not exists");
		}
		lsInstalled.addListSelectionListener(this);
		lsInstalled.setSelectedIndex(index);
	}

	private JPanel initLoadable() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[grow]"));
		p.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("assistant.downloadable")));

		// assistant loadable list
		lsLoadable = new JList<>();
		lsLoadable.setVisibleRowCount(8);
		lsLoadable.addListSelectionListener((ListSelectionEvent evt) -> {
			btDownload.setEnabled(true);
		});
		lsLoadable.setMaximumSize(new Dimension(1024, 800));
		JScrollPane scroll = new JScrollPane(lsLoadable);
		scroll.setMaximumSize(new Dimension(800, 1024));
		p.add(scroll, MIG.get(MIG.GROWY, MIG.GROWX));

		// refresh the list
		btRefresh = Ui.initButton("btRefresh", "assistant.refresh",
				ICONS.K.REFRESH, "", evt -> refreshAssistants());
		p.add(btRefresh, MIG.get(MIG.SPLIT2, MIG.CENTER));
		// download the selected item
		btDownload = Ui.initButton("btDownload", "assistant.download",
				ICONS.K.AR_DOWN, "", evt -> downloadAssistant());
		btDownload.setEnabled(false);
		p.add(btDownload);
		return p;
	}

	private JPanel initLocalFile() {
		JPanel p = new JPanel(new MigLayout(MIG.HIDEMODE3));
		ckLocal = Ui.initCheckBox(null, "ckLocal", "assistant.external_prompt", false, BNONE,
				e -> {
					SwingUtil.setEnable(pLocal, ckLocal.isSelected());
					btEdit.setVisible(ckLocal.isSelected());
				});
		p.add(ckLocal);
		pLocal = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.WRAP, MIG.INS0, MIG.GAP1), "[][]"));
		slAssistant = new JSFileSelector("slAssistant", "", "", true, "Assistant file (*.xml)", "xml");
		pLocal.add(slAssistant, MIG.GROWX);
		p.add(pLocal);
		return p;
	}

	private void selectInstalled() {
		String fileName = EnvUtil.getPrefDir() + File.separator + "Assistant"
				+ File.separator + (String) lsInstalled.getSelectedValue();
		App.preferences.setString(Pref.KEY.ASSISTANT, fileName);
		App.getInstance().setAssistant(fileName);
		dispose();
	}

	private void selectLocal() {
		//LOG.trace(TT + ".selectLocal()");
		App.preferences.setString(Pref.KEY.ASSISTANT, slAssistant.getFileName());
		App.getInstance().setAssistant(slAssistant.getFileName());
		dispose();
	}

	private void downloadAssistant() {
		frmMain = new JFrame();
		frmMain.setUndecorated(true);
		frmMain.getRootPane().setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createLoweredBevelBorder(),
						BorderFactory.createRaisedBevelBorder()));
		frmMain.setTitle(I18N.getMsg("dlg.build.build.progress"));
		frmMain.setLocationRelativeTo(this);
		frmMain.setSize(310, 100);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Exit when X is clicked
		//get the content pane
		pane = frmMain.getContentPane();
		pane.setLayout(null); //Use the null layout
		//set the title
		txTitle = new JLabel("");
		txTitle.setBounds(10, 10, 280, 20);
		txTitle.setHorizontalAlignment(CENTER);
		pane.add(txTitle);

		//set the cancel button
		btnCancelLoad = new JButton("Cancel");
		btnCancelLoad.setBounds(100, 55, 100, 25);
		btnCancelLoad.addActionListener(e -> isLoadCanceled = true);
		pane.add(btnCancelLoad);

		//set the progress bar
		barDo = new JProgressBar(0, 100); //Min value: 0 Max value: 100
		barDo.setBounds(10, 30, 280, 20);
		pane.add(barDo);

		//Make the frame visible
		frmMain.setResizable(false); //No resize
		frmMain.setVisible(true);
		frmMain.setAlwaysOnTop(true);

		//run the thread
		new Thread(new AssistantWizardDlg.Download()).start();
	}

	@SuppressWarnings("unchecked")
	public void refreshAssistants() {
		URL url;
		try {
			url = new URL(Net.KEY.ASSISTANT.toString() + "list.txt");
		} catch (MalformedURLException ex) {
			LOG.err(TT + "refreshAssistants() exception", ex);
			return;
		}
		String f_assist = "Assistant";
		List<String> remote = new ArrayList<>();
		DefaultListModel installed = (DefaultListModel) lsInstalled.getModel();
		try (@SuppressWarnings("null") BufferedReader in
				= new BufferedReader(new InputStreamReader(url.openStream()))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains(f_assist) && inputLine.contains("xml")) {
					String str = inputLine.substring(inputLine.indexOf(f_assist));
					if (str.contains("\"")) {
						str = str.substring(0, str.indexOf("\""));
					}
					if (!installed.contains(str)) {
						remote.add(str.trim());
					}
				}
			}
			in.close();
		} catch (IOException ex) {
			LOG.err(TT + "refreshAssistant() error", ex);
			return;
		}
		if (remote.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					I18N.getMsg("assistant.no_more"),
					I18N.getMsg("warning"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		DefaultListModel model = new DefaultListModel();
		for (String l : remote) {
			model.addElement(l);
		}
		lsLoadable.setModel(model);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		btSelAssistant.setEnabled(lsInstalled.getSelectedIndex() != -1);
	}

	public static class Download implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			if (lsLoadable.getSelectedIndex() == -1) {
				return;
			}
			String file = lsLoadable.getSelectedValue();
			try {
				urlAssistant = new URL(Net.KEY.ASSISTANT.toString() + file);
				String prefDir = EnvUtil.getPrefDir().getAbsolutePath();
				File dir = new File(prefDir + File.separator + "Assistant");
				if (!dir.exists()) {
					dir.mkdir();
				}
				destAssistant = new File(
						dir.getPath() + File.separator + file);
				//LOG.trace(TT+"run() from url="
				//   + urlAssistant.toString() + "\ndest=" + destAssistant.getAbsolutePath());
				URLConnection connection = urlAssistant.openConnection();
				int total = connection.getContentLength();
				barDo.setMaximum(total);
				barDo.setStringPainted(true);
				//LOG.trace(TT+"run() url content size=" + total);
				BufferedInputStream inputStream
						= new BufferedInputStream(connection.getInputStream());
				try (BufferedOutputStream outStream
						= new BufferedOutputStream(new FileOutputStream(destAssistant))) {
					int curent = 0;
					int bytesRead;
					barDo.setString(curent + "/" + total);
					byte[] buffer = new byte[1024];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						if (isLoadCanceled) {
							break;
						}
						outStream.write(buffer, 0, bytesRead);
						curent += bytesRead;
						barDo.setString(curent + "/" + total);
						barDo.setValue(curent);
						barDo.repaint(); //Refresh graphics
						Thread.sleep(1);
					}
					outStream.flush();
					outStream.close();
					//LOG.trace(TT+"run() "+urlAssistant.toString() + " total file size=" + curent);
				} catch (InterruptedException ex) {
					LOG.err(ex.getLocalizedMessage(), ex);
					return;
				}
				if (isLoadCanceled) {
					if (destAssistant.exists()) {
						destAssistant.delete();
					}
				} else {
					thisDlg.refreshInstalled();
					thisDlg.repaint();
				}
				frmMain.setVisible(false);
			} catch (MalformedURLException ex) {
				LOG.err(TT + "run() error", ex);
			} catch (IOException ex) {
				LOG.err(TT + "run() error", ex);
			}
		}

	}

}
