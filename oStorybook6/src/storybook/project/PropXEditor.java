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
package storybook.project;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.exim.EXIM;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.FileFilter;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panels.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PropXEditor extends AbstractPanel implements CaretListener, ChangeListener {

	private static final String TT = "PropXEditor";
	private JCheckBox ckUseX;
	private JPanel pExternal;
	private JComboBox cbExtension;
	private JPanel pExternalExtension;
	private JTextField tfExtension;
	private JTextField tfName;
	private JTextField tfTemplate;
	private JButton btDefault;
	private static final String EXT[] = {".docx", ".odt"};
	private static final String EXT_TEXT[] = {
		"", "*.docx (MS Word)", "*.odt (LibreOffice)", I18N.getMsg("other")
	};

	public PropXEditor(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.FILLX, MIG.WRAP), "[grow]"));
		ckUseX = new JCheckBox(I18N.getMsg("xeditor.askuse"));
		ckUseX.addActionListener((java.awt.event.ActionEvent evt) -> {
			changeXeditor();
		});
		add(ckUseX);
		initExternal();
		add(pExternal, MIG.get(MIG.SPAN, MIG.GROWX));
		if (book.isXeditorUse()) {
			ckUseX.setSelected(true);
			cbExtension.setSelectedIndex(EXIM.findExt(book.getParam().getParamEditor().getExtension()) + 1);
			setEditorName(book.getParam().getParamEditor().getName());
			setEditorTemplate(book.getParam().getParamEditor().getTemplate());
		} else {
			ckUseX.setSelected(false);
		}
		ckUseX.addChangeListener(this);
		pExternalExtension.setVisible(cbExtension.getSelectedIndex() == 3);
		pExternal.setVisible(ckUseX.isSelected());
	}

	@SuppressWarnings("unchecked")
	private void initExternal() {
		pExternal = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.WRAP), "[right][grow]"));
		pExternal.setBorder(BorderFactory.createEtchedBorder());
		//set extension type
		pExternal.add(new JLabel(I18N.getColonMsg("xeditor.extension_type")));
		cbExtension = new JComboBox();
		for (String s : EXT_TEXT) {
			cbExtension.addItem(s);
		}
		cbExtension.addItemListener(e -> {
			changeXeditor();
		});
		pExternal.add(cbExtension);
		initExternalExtension();
		btDefault = Ui.initButton("btDefault",
		   "",
		   ICONS.K.F_IMPORT,
		   "xeditor.template_create",
		   e -> setDefaultTemplate());
		//pExternal.add(btDefault, MIG.get(MIG.SKIP, MIG.SPAN));
		btDefault.setVisible(false);
		pExternal.add(new JLabel(I18N.getColonMsg("xeditor.template")));
		tfTemplate = new JTextField();
		tfTemplate.addCaretListener(this);
		pExternal.add(tfTemplate, MIG.get(MIG.SPLIT + " 3", MIG.GROWX));
		pExternal.add(btDefault);
		JButton btTemplate = new JButton();
		btTemplate.setIcon(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		btTemplate.setMargin(new Insets(0, 0, 0, 0));
		btTemplate.setToolTipText(I18N.getMsg("xeditor.template_select"));
		btTemplate.addActionListener((java.awt.event.ActionEvent evt) -> {
			JFileChooser chooser = new JFileChooser(tfTemplate.getText());
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (cbExtension.getSelectedIndex() > 0) {
				FileFilter filter = new FileFilter(EXT[cbExtension.getSelectedIndex() - 1]);
				chooser.setFileFilter(filter);
			}
			int i = chooser.showOpenDialog(null);
			if (i != 0) {
				return;
			}
			File file = chooser.getSelectedFile();
			tfTemplate.setText(file.getAbsolutePath());
			tfTemplate.setCaretPosition(0);
		});
		pExternal.add(btTemplate);
	}

	private void initExternalExtension() {
		pExternalExtension = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[right][grow]"));
		pExternalExtension.add(new JLabel(I18N.getColonMsg("xeditor.extension")));
		tfExtension = new JTextField();
		tfExtension.setText(book.param.getParamEditor().getExtension());
		tfExtension.setColumns(4);
		pExternalExtension.add(tfExtension);
		pExternalExtension.add(new JLabel(I18N.getColonMsg("xeditor.name")));
		tfName = new JTextField();
		tfName.setText(book.param.getParamEditor().getName());
		pExternalExtension.add(tfName, MIG.GROWX);
		pExternal.add(pExternalExtension, MIG.get(MIG.SPAN, MIG.GROWX));
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	private void changeXeditor() {
		//LOG.printInfos(TT + ".changeXeditor() cbExtension=" + cbExtension.getSelectedIndex());
		pExternal.setVisible(ckUseX.isSelected());
		pExternalExtension.setVisible(cbExtension.getSelectedIndex() > 2);
		tfExtension.setText("");
		tfName.setText("");
		switch (cbExtension.getSelectedIndex()) {
			case 1:
				tfExtension.setText("docx");
				btDefault.setVisible(true);
				setEditorName(EXIM.getEditor("docx"));
				break;
			case 2:
				tfExtension.setText("odt");
				btDefault.setVisible(true);
				setEditorName(EXIM.getEditor("odt"));
				break;
			default:
				btDefault.setVisible(false);
				break;
		}
	}

	private void setEditorName(String name) {
		tfName.setText(name);
	}

	private void setEditorTemplate(String template) {
		tfTemplate.setText(template);
	}

	public String verify() {
		String rc = "";
		JTextField tf = new JTextField();
		Border bc = BorderFactory.createLineBorder(Color.red);
		if (ckUseX.isSelected()) {
			cbExtension.setBorder(tf.getBorder());
			if (cbExtension.getSelectedIndex() < 1) {
				rc += "\n" + I18N.getMsg("xeditor.err.missing.extension") + "\n";
				cbExtension.setBorder(bc);
			}
			if (cbExtension.getSelectedIndex() > 2 && tfExtension.getText().isEmpty()) {
				rc += "\n" + I18N.getMsg("xeditor.err.missing.extension") + "\n";
				cbExtension.setBorder(bc);
			}
			tfName.setBorder(tf.getBorder());
			if (tfName.getText().isEmpty()) {
				rc += "\n" + I18N.getMsg("xeditor.err.missing.name");
				tfName.setBorder(bc);
			}
			tfTemplate.setBorder(tf.getBorder());
			if (tfTemplate.getText().isEmpty()) {
				rc += "\n" + I18N.getMsg("xeditor.err.missing.template");
				tfTemplate.setBorder(bc);
			} else if (!tfTemplate.getText().endsWith(tfExtension.getText())) {
				rc += "\n" + I18N.getMsg("xeditor.err.template");
				tfTemplate.setBorder(bc);
			} else {
				File ft = new File(tfTemplate.getText());
				if (!ft.exists()) {
					rc += "\n" + I18N.getMsg("xeditor.err.template");
					tfTemplate.setBorder(bc);
					tfTemplate.setText("");
				}
			}
		}
		return rc;
	}

	public void apply() {
		if (ckUseX.isSelected()) {
			book.getParam().getParamEditor().setName(tfName.getText());
			book.getParam().getParamEditor().setExtension(tfExtension.getText());
			book.getParam().getParamEditor().setTemplate(tfTemplate.getText());
		} else {
			book.getParam().getParamEditor().setName("");
			book.getParam().getParamEditor().setExtension("");
			book.getParam().getParamEditor().setTemplate("");
		}
		book.getParam().getParamEditor().setUse(this.ckUseX.isSelected());
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (tfTemplate != null && !tfTemplate.getText().isEmpty()) {
			File f = new File(IOUtil.fileToAbsolute(mainFrame.getProject().getPath(), tfTemplate.getText()));
			SwingUtil.setErrorForeground(tfTemplate, !f.exists());
		}
	}

	public void setExtension(String ext) {
		int n = EXIM.findExt(ext);
		cbExtension.setSelectedIndex(n);
	}

	/**
	 * set default template for docx or odt
	 */
	private void setDefaultTemplate() {
		// searching template in pref directory
		String model = "Modele." + tfExtension.getText();
		File f = new File(EnvUtil.getPrefDir().getAbsolutePath() + File.separator + model);
		if (!f.exists()) {
			// copy internal template to pref directory directory
			IOUtil.resourceCopyTo("model/" + model, f);
		}
		tfTemplate.setText(f.getAbsolutePath());
		SwingUtil.setBoderDefault(tfTemplate);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			if (!ckUseX.isSelected()) {
				cbExtension.setSelectedIndex(-1);
				tfName.setText("");
				tfExtension.setText("");
				tfTemplate.setText("");
			}
		}
	}

}
