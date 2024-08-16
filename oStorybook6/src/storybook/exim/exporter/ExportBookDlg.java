/*
 * Copyright (C) 2021 favdb
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
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.book.BookParamExport;
import storybook.dialog.AbsDialog;
import storybook.exim.exporter.options.BOOKpanel;
import storybook.exim.exporter.options.HTMLpanel;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import static storybook.ui.SbView.VIEWNAME.READING;
import storybook.ui.Ui;

/**
 * dialog for exporting the book
 *
 * @author favdb
 */
public class ExportBookDlg extends AbsDialog {

	private static final String TT = "ExportBookDlg.";

	private BookParamExport paramExport;
	private JTextField tfFolder;
	private JPanel formatPanel;
	private JComboBox cbFormat;
	public HTMLpanel HTML;
	public BOOKpanel BOOK;
	private JButton btOK;
	private JCheckBox ckEpubByScenes;
	private String origin;

	/**
	 * static show the dialog
	 *
	 * @param mainFrame
	 */
	public static void show(MainFrame mainFrame) {
		//LOG.printInfos(TT + ".show(mainFrame) " + mainFrame.getBook().getParam().getParamExport().toPrint());
		ExportBookDlg dlg = new ExportBookDlg(mainFrame);
		dlg.setVisible(true);
	}

	/**
	 * public class for the exporting class
	 *
	 * @param mainFrame
	 */
	public ExportBookDlg(MainFrame mainFrame) {
		super(mainFrame);
		this.mainFrame = mainFrame;
		initAll();
	}

	/**
	 * initialize the class
	 *
	 */
	@Override
	public void init() {
		mainFrame.getBook().getParam().getParamExport().refresh();
		paramExport = mainFrame.getBook().getParam().getParamExport();
		origin = paramExport.toString();
	}

	/**
	 * initialize the user interface
	 *
	 */
	@Override
	public void initUi() {
		//LOG.printInfos(TT + ".initUi() " + paramExport.toPrint());
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3)));
		setBackground(Color.white);
		setTitle(I18N.getMsg("export"));
		// initialize the folder part
		initFolder();
		// initialize the export format
		initFormat();
		// initialize book general parameters
		BOOK = new BOOKpanel(this);
		BOOK.setBorder(BorderFactory.createEtchedBorder());
		BOOK.setVisible(false);
		add(BOOK, MIG.get(MIG.GROWX, MIG.SPAN));
		// initialize specific HTML parameteres
		HTML = new HTMLpanel(this);
		HTML.setBorder(BorderFactory.createEtchedBorder());
		HTML.setVisible(false);
		add(HTML, MIG.get(MIG.GROWX, MIG.SPAN));
		// OK/Cancel buttons
		btOK = Ui.initButton("btOk", "ok", ICONS.K.OK, "", e -> apply());
		add(btOK, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.SG, MIG.RIGHT));
		add(Ui.initButton("btCancel", "cancel", ICONS.K.CANCEL, "", e -> dispose()), MIG.SG);

		pack();
		setLocationRelativeTo(mainFrame);
		checkExists();
	}

	private void initFolder() {
		add(new JLabel(I18N.getMsg("export.folder")), MIG.SPLIT + " 3");
		// text field for the folder
		tfFolder = new JTextField();
		tfFolder.setColumns(32);
		if (IOUtil.dirExists(paramExport.getDirectory())) {
			tfFolder.setText(paramExport.getDirectory());
		}
		tfFolder.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				checkExists();
			}
		});
		add(tfFolder, MIG.GROWX);
		// button for the JFileChooser
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		bt.addActionListener((java.awt.event.ActionEvent evt) -> {
			String dir = tfFolder.getText();
			if (dir.isEmpty()) {
				dir = mainFrame.getProject().getPath();
			}
			JFileChooser chooser = new JFileChooser(dir);
			chooser.setFileSelectionMode(1);
			int i = chooser.showOpenDialog(null);
			if (i != 0) {
				return;
			}
			File file = chooser.getSelectedFile();
			tfFolder.setText(file.getAbsolutePath());
			if (checkExists()) {
				mainFrame.project.book.param.getParamExport().setDirectory(file.getAbsolutePath());
				mainFrame.setUpdated();
			}
		});
		bt.setMargin(new Insets(0, 0, 0, 0));
		add(bt, MIG.WRAP);
	}

	/**
	 * check if export dir exists
	 *
	 * @return
	 */
	private boolean checkExists() {
		File f = new File(tfFolder.getText());
		if (f.exists() && f.isDirectory()) {
			tfFolder.setForeground(UIManager.getDefaults().getColor("TextField.foreground"));
			btOK.setEnabled(true);
			return true;
		} else {
			tfFolder.setForeground(Color.red);
			btOK.setEnabled(false);
			return false;
		}
	}

	/**
	 * initialize the allowed format
	 *
	 */
	@SuppressWarnings("unchecked")
	private void initFormat() {
		if (formatPanel == null) {
			formatPanel = new JPanel(new MigLayout("", "[][][]"));
			JLabel lb3 = new JLabel(I18N.getMsg("export.format") + ":");
			formatPanel.add(lb3);
			String[] fmt = {
				I18N.getMsg("file.type.choose"),
				I18N.getMsg("file.type.html") + " (*.html)",
				I18N.getMsg("file.type.epub") + " (*.epub)",
				I18N.getMsg("file.type.docx") + " (*.docx)",
				I18N.getMsg("file.type.odt") + " (*.odt)",
				I18N.getMsg("file.type.tex") + " (*.tex)",
				I18N.getMsg("file.type.txt") + " (*.txt)"
			};
			cbFormat = new JComboBox(fmt);
			cbFormat.setName("cbFormat");
			cbFormat.addActionListener(e -> {
				String sel = (String) cbFormat.getSelectedItem();
				BOOK.setVisible(sel.contains("*.html"));
				HTML.setVisible(sel.contains("*.html"));
				ckEpubByScenes.setVisible(sel.contains("*.epub"));
				pack();
				setLocationRelativeTo(mainFrame);
			});
			formatPanel.add(cbFormat);
			ckEpubByScenes = Ui.initCheckBox(null, "ckByScenes", "epub.by_scenes", false, null);
			ckEpubByScenes.setToolTipText(I18N.getMsg("epub.by_scenes_prompt"));
			ckEpubByScenes.setVisible(false);
			formatPanel.add(ckEpubByScenes);
		}
		add(formatPanel, MIG.get(MIG.NEWLINE, MIG.SPAN));
	}

	/**
	 * ok action
	 */
	public void apply() {
		//LOG.trace(TT + "apply()");
		if (cbFormat.getSelectedIndex() == 0) {
			return;
		}
		String f = tfFolder.getText();
		if (f.isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame,
				I18N.getMsg("directory.empty"),
				I18N.getMsg("directory.select"),
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		File folder = new File(f);
		if (!folder.exists()) {
			JOptionPane.showMessageDialog(mainFrame,
				I18N.getMsg("directory.not_exists", f),
				I18N.getMsg("directory.select"),
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		paramExport = mainFrame.getBook().getParam().getParamExport();
		paramExport.setDirectory(f);
		HTML.apply(paramExport);
		BOOK.apply(paramExport);
		if (!origin.equals(paramExport.toString())) {
			mainFrame.setUpdated();
			mainFrame.getBookModel().fireAgain(mainFrame.getView(READING));
		}
		doExport();
		dispose();
	}

	public BookParamExport getParamExport() {
		return this.paramExport;
	}

	/**
	 * do the export
	 *
	 * @return
	 */
	private boolean doExport() {
		//LOG.trace(TT + "doExport()");
		String sel = (String) cbFormat.getSelectedItem();
		sel = sel.substring(sel.indexOf('(')).replace("(*.", "").replace(")", "");
		mainFrame.fileSave(true);
		switch (sel) {
			case "html":
				return ExportBookToHtml.toFile(mainFrame, HTML.isAdvanced());
			case "docx":
				return ExportBookToDoc.createDocx(mainFrame);
			case "odt":
				return ExportBookToDoc.createOdt(mainFrame);
			case "tex":
				return ExportBookToDoc.createLatex(mainFrame);
			case "txt":
				ExportBookToHtml export = new ExportBookToHtml(mainFrame);
				String fileName = tfFolder.getText() + File.separator + mainFrame.getProject().getName() + ".txt";
				String html = Html.htmlToText(export.getString(), true).replace("\n\n", "\n");
				IOUtil.fileWriteString(fileName, html);
				break;
			case "epub":
				ExportBookToEpub.execute(mainFrame, tfFolder.getText(), ckEpubByScenes.isSelected());
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
