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
package storybook.exim.exporter;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.book.BookParamExport;
import storybook.dialog.AbsDialog;
import storybook.exim.exporter.options.CSVpanel;
import storybook.exim.exporter.options.TXTpanel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ExportOptionsDlg extends AbsDialog implements ItemListener {

	private final BookParamExport param;
	private CSVpanel csvPanel;
	private TXTpanel txtPanel;
	private JTextField txFolder;
	private final AbsDialog thisDlg;
	private String ckParam;
	private JComboBox cbFormat;

	public ExportOptionsDlg(MainFrame parent) {
		super(parent);
		this.param = mainFrame.getBook().param.getParamExport();
		this.thisDlg = this;
		initAll();
	}

	@Override
	public void init() {
		ckParam = param.toCheck();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		UIDefaults defaults = javax.swing.UIManager.getDefaults();
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE3), "[][]"));
		setTitle(I18N.getMsg("export.options"));
		add(new JLabel(I18N.getColonMsg("export.folder")));
		txFolder = new JTextField();
		txFolder.setText(param.getDirectory());
		//txFolder.setColumns(28);
		txFolder.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				File f = new File(txFolder.getText());
				if (f.exists() && f.isDirectory()) {
					txFolder.setForeground(defaults.getColor("TextField.foreground"));
				} else {
					txFolder.setForeground(Color.red);
				}
			}
		});
		add(txFolder, MIG.get(MIG.SPLIT + " 2", MIG.GROWX));
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		bt.addActionListener((java.awt.event.ActionEvent evt) -> {
			JFileChooser chooser = new JFileChooser(txFolder.getText());
			chooser.setFileSelectionMode(1);
			int i = chooser.showOpenDialog(null);
			if (i != 0) {
				return;
			}
			File file = chooser.getSelectedFile();
			txFolder.setText(file.getAbsolutePath());
			txFolder.setForeground(defaults.getColor("TextField.foreground"));
		});
		bt.setMargin(new Insets(0, 0, 0, 0));
		add(bt, MIG.WRAP);
		add(new JLabel("default export format: "));
		cbFormat = new JComboBox();
		cbFormat.addItem("html");
		//XMLformat not allowed for export table
		//cbFormat.addItem("xml");
		cbFormat.addItem("txt");
		cbFormat.addItem("csv");
		add(cbFormat);
		csvPanel = new CSVpanel(param);
		add(csvPanel, MIG.SKIP);
		txtPanel = new TXTpanel(param);
		add(txtPanel, MIG.SKIP);

		add(getCancelButton(), MIG.get(MIG.NEWLINE, MIG.SPAN, MIG.SPLIT2, MIG.SG, MIG.RIGHT));
		add(getOkButton(), MIG.get(MIG.SG, MIG.RIGHT));
		cbFormat.addItemListener(this);
		String xs = mainFrame.getBook().getParam().getParamExport().getFormat();
		cbFormat.setSelectedItem(xs);
		csvPanel.setVisible(xs.equals("csv"));
		txtPanel.setVisible(xs.equals("txt"));
		pack();
		//setLocationRelativeTo(mainFrame);
		setLocation(MouseInfo.getPointerInfo().getLocation());
		setModal(true);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check if the folder exists
				String dir = txFolder.getText();
				if (!dir.isEmpty()) {
					//check if dir is a directory
					File f = new File(dir);
					if (!(f.exists() && f.isDirectory())) {
						JOptionPane.showMessageDialog(thisDlg,
								I18N.getMsg("export.dir.error"),
								I18N.getMsg("export"), 1);
						return;
					}
				}
				param.setDirectory(dir);
				csvPanel.apply();
				txtPanel.apply();
				String xs = (String) cbFormat.getSelectedItem();
				param.setFormat(xs);
				if (!ckParam.equals(param.toCheck())) {
					mainFrame.project.save();
					mainFrame.setUpdated();
				}
				dispose();
			}
		};
	}

	public static boolean show(MainFrame m) {
		ExportOptionsDlg dlg = new ExportOptionsDlg(m);
		dlg.setVisible(true);
		return dlg.isCanceled();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		String xs = (String) cbFormat.getSelectedItem();
		csvPanel.setVisible(xs.equals("csv"));
		txtPanel.setVisible(xs.equals("txt"));
		this.pack();
	}

}
