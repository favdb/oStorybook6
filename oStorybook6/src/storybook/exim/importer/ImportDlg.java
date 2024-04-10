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
package storybook.exim.importer;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.FileFilter;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ImportDlg extends AbsDialog {

	private static final String DLG_NAME = "import";

	JCheckBox ckForce;
	JTextField txFile;
	private File fileImport = null;
	private Importer importer;
	private JTabbedPane tbPane;
	private JButton btImport;

	public static void show(MainFrame m) {
		ImportDlg dlg = new ImportDlg(m);
		dlg.setVisible(true);
	}
	private JPanel tasksPanel;

	public ImportDlg(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		//LOG.printInfos(TT+".initUi()");
		super.initUi();
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[][grow][]"));
		setBackground(Color.white);
		setTitle(I18N.getMsg(DLG_NAME));
		setPreferredSize(new Dimension(800, 600));

		///select file
		add(new JLabel(I18N.getColonMsg("file")));
		txFile = new JTextField(App.preferences.getString(Pref.KEY.IMP_FILE));
		txFile.setEditable(false);
		add(txFile, MIG.GROWX);
		JButton btFile = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		btFile.setMargin(new Insets(0, 0, 0, 0));
		btFile.addActionListener((java.awt.event.ActionEvent evt) -> {
			btFileAction(evt);
		});
		add(btFile);

		// check for forced paste
		ckForce = new javax.swing.JCheckBox();
		ckForce.setText(I18N.getMsg("import.dlg.force"));
		add(ckForce, MIG.get(MIG.SPAN, MIG.WRAP));

		// panel for entities lists
		tbPane = new JTabbedPane();
		add(tbPane, MIG.get(MIG.SPAN, MIG.GROW));
		//tasksPanel = new JPanel(new MigLayout(MIG.FILL));
		//add(tasksPanel, MIG.get(MIG.SPAN, MIG.GROW));

		// OK and Cancel buttons
		btImport = getOkButton(DLG_NAME);
		add(btImport, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.SG, MIG.RIGHT));
		add(getCancelButton(), MIG.SG);

		pack();
		setLocationRelativeTo(mainFrame);
		tasks = new ArrayList<>();
		//select default import tasks
		//tasks.add(new ImportDlg.ImportTask("gender"));
		//tasks.add(new ImportDlg.ImportTask("strand"));
		//tasks.add(new ImportDlg.ImportTask("part"));
		//tasks.add(new ImportDlg.ImportTask("chapter"));
		tasks.add(new ImportDlg.ImportTask("scene"));
		tasks.add(new ImportDlg.ImportTask("person"));
		tasks.add(new ImportDlg.ImportTask("location"));
		tasks.add(new ImportDlg.ImportTask("item"));
		tasks.add(new ImportDlg.ImportTask("tag"));
		tbPane.removeAll();
		//tasksPanel.removeAll();
		java.awt.EventQueue.invokeLater(() -> {
			btFileAction(null);
		});
	}

	private void btFileAction(ActionEvent evt) {
		String nf = txFile.getText();
		if (!txFile.getText().isEmpty()) {
			nf = App.preferences.getString(Pref.KEY.LASTOPEN_DIR, EnvUtil.getHomeDir().getAbsolutePath());
		}
		JFileChooser chooser = new JFileChooser(nf);
		File f = new File(nf);
		chooser.setCurrentDirectory(f);
		//String x[]={/*".h2.db",".mv.db",*/".xml"};
		FileFilter filter = new FileFilter("xml");
		chooser.addChoosableFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		int i = chooser.showOpenDialog(this);
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
		fileImport = chooser.getSelectedFile();
		if (fileImport.getAbsolutePath().endsWith(".db") || fileImport.getAbsolutePath().endsWith(".xml")) {
			nf = fileImport.getParent();
			txFile.setText(fileImport.getAbsolutePath());
			txFile.setCaretPosition(0);
			App.preferences.setString(Pref.KEY.IMP_FILE, fileImport.getAbsolutePath());
			App.preferences.setString(Pref.KEY.LASTOPEN_DIR, nf);
			loadLists();
		}
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doImport();
			}
		};
	}

	@SuppressWarnings("unchecked")
	private void doImport() {
		//LOG.printInfos(TT+".doImport()");
		//TODO MAJ add the updating date to check
		fileImport = new File(txFile.getText());
		if (!(fileImport.exists() && fileImport.isFile())) {
			return;
		}
		App.preferences.setString(Pref.KEY.IMP_FILE, txFile.getText());
		mainFrame.getProject().doBackup();
		importer = new Importer(mainFrame, txFile.getText());
		if (importer.open() == false) {
			LOG.err("error Importer failed to open");
			return;
		}
		String msgCheck = "";
		for (ImportDlg.ImportTask task : tasks) {
			msgCheck += checkEntities(task.ckList, task.tobj);
		}
		if (!msgCheck.isEmpty()) {
			JOptionPane.showMessageDialog(this,
			   I18N.getMsg("import.dlg.exists", msgCheck),
			   I18N.getMsg(DLG_NAME),
			   JOptionPane.ERROR_MESSAGE);
			return;
		}
		int rc = 0;
		for (ImportDlg.ImportTask task : tasks) {
			int n = loadEntities(task);
			if (n == -1) {
				rc = n;
				break;
			}
			rc += n;
		}
		importer.close();
		if (rc != -1) {
			JOptionPane.showMessageDialog(this,
			   I18N.getMsg("import.dlg.ok", txFile.getText()),
			   I18N.getMsg(DLG_NAME),
			   JOptionPane.INFORMATION_MESSAGE);
			dispose();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadLists() {
		//LOG.printInfos(TT+".loadLists()");
		tbPane.removeAll();
		//tasksPanel.removeAll();
		importer = new Importer(mainFrame, txFile.getText());
		if (importer.open() == false) {
			return;
		}
		int b = 0;
		for (ImportDlg.ImportTask task : tasks) {
			b += listEntities(task);
		}
		btImport.setEnabled(b > 0);
		importer.close();
	}

	@SuppressWarnings("unchecked")
	private int listEntities(ImportDlg.ImportTask task) {
		//LOG.printInfos(TT+".listEntities(lst,tb," + tobj + ")");
		DefaultListModel<JCheckBox> model = new DefaultListModel<>();
		task.ckList.setModel(model);
		task.ckList.setCellRenderer(new ImportDlg.CheckboxListCellRenderer());
		List<ImportEntity> imports = importer.list(task.tobj);
		if (!imports.isEmpty()) {
			for (ImportEntity imp : imports) {
				model.addElement(new JCheckBox(imp.entity.getName()));
			}
		} else {
			task.panel.removeAll();
			task.panel.add(new JLabel(I18N.getMsg("empty")), MIG.CENTER);
		}
		tbPane.add(I18N.getMsg(task.tobj), task.panel);
		//tasksPanel.add(task.panel, MIG.GROW);
		return imports.size();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList lst, Object val, int idx, boolean isSel, boolean hasFocus) {
			setComponentOrientation(lst.getComponentOrientation());
			setFont(lst.getFont());
			setBackground(lst.getBackground());
			setForeground(lst.getForeground());
			setSelected(isSel);
			setEnabled(lst.isEnabled());
			setText(val == null ? "null" : ((JCheckBox) val).getText());
			return this;
		}
	}

	private String checkEntities(JList<JCheckBox> lst, String tobj) {
		if (lst.getSelectedValuesList().isEmpty()) {
			return ("");
		}
		//LOG.printInfos(TT+".checkEntities(lst," + tobj + ") "
		//		+ (ckForce.isSelected() ? "force replace" : "only if not exists"));
		if (ckForce.isSelected()) {
			return ("");//no check because it is forced
		}
		String msg;
		//reload all entities list
		List<JCheckBox> lsSel = lst.getSelectedValuesList();
		List<ImportEntity> entities = importer.list(tobj);
		List<ImportEntity> selEntities = new ArrayList<>();
		for (ImportEntity entity : entities) {
			for (JCheckBox cb : lsSel) {
				if (cb.getText().equals(entity.entity.getName())) {
					selEntities.add(entity);
					break;
				}
			}
		}
		msg = importer.checkAll(selEntities);
		if (!msg.isEmpty()) {
			msg = "- " + I18N.getMsg(tobj) + ": " + msg;
		}
		return (msg);
	}

	private int loadEntities(ImportDlg.ImportTask task) {
		if (task.ckList.getSelectedValuesList().isEmpty()) {
			return (0);
		}
		//LOG.printInfos(TT+".loadEntities(lst," + tobj + ") "
		//		+ (ckForce.isSelected() ? "force replace" : "only if not exists"));
		List<JCheckBox> lsSel = task.ckList.getSelectedValuesList();
		List<ImportEntity> entities = importer.list(task.tobj);
		List<ImportEntity> selEntities = new ArrayList<>();
		for (ImportEntity entity : entities) {
			for (JCheckBox cb : lsSel) {
				if (cb.getText().equals(entity.entity.getName())) {
					selEntities.add(entity);
					break;
				}
			}
		}
		if (importer.writeAll(selEntities, ckForce.isSelected())) {
			return (-1);
		}
		return selEntities.size();
	}

	List<ImportTask> tasks = new ArrayList<>();

	private class ImportTask {

		JList<JCheckBox> ckList;
		JPanel panel;
		String tobj;

		public ImportTask(String tobj) {
			this.ckList = new JList<>();
			this.panel = new JPanel(new MigLayout(MIG.FILL));
			//this.panel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg(tobj + "s")));
			this.panel.setPreferredSize(new Dimension(800, 600));
			this.tobj = tobj;
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(this.ckList);
			scroll.setPreferredSize(new Dimension(800, 600));
			this.panel.add(scroll, MIG.GROW);
		}

	}

}
