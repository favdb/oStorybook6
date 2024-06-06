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
package storybook.exim.exporter.options;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.book.BookParamExport;
import storybook.exim.exporter.ExportBookDlg;
import storybook.tools.file.EnvUtil;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class HTMLpanel extends JPanel implements ChangeListener {

    private static final String TT = "HTMLpanel";

    private final ExportBookDlg dlgExport;
    private JButton btCssFile;
    private JTextField txCssFile;
    public JCheckBox cbNav;
    public JCheckBox cbNavImage;
    private JCheckBox cbUseCss;
    private JCheckBox ckChapterBreakPage;
    private final BookParamExport paramExport;

    public HTMLpanel(ExportBookDlg dlg) {
	this.dlgExport = dlg;
	paramExport = dlg.getParamExport();
	initAll();
    }

    private void initAll() {
	init();
	initUi();
    }

    private void init() {
    }

    private void initUi() {
	//LOG.trace(TT + ".initUi() " + paramExport.toString());
	setLayout(new MigLayout());
	boolean htmlCss = !paramExport.getHtmlCss().isEmpty();
	cbUseCss = new JCheckBox(I18N.getMsg("export.options.html.css"));
	cbUseCss.setSelected(htmlCss);
	cbUseCss.addItemListener(evt -> htmlUseCssChanged(evt));
	add(cbUseCss, MIG.WRAP);

	txCssFile = new JTextField(paramExport.getHtmlCss());
	txCssFile.setColumns(32);
	txCssFile.setEnabled(htmlCss);
	add(txCssFile, MIG.SPLIT2);

	btCssFile = new JButton();
	btCssFile.setMargin(new Insets(0, 0, 0, 0));
	btCssFile.setIcon(IconUtil.getIconSmall(ICONS.K.F_OPEN));
	btCssFile.addActionListener(e -> ChooseCssFile());
	btCssFile.setEnabled(htmlCss);
	add(btCssFile, MIG.WRAP);

	cbNav = new JCheckBox(I18N.getMsg("export.options.html.nav"));
	cbNav.setSelected(paramExport.getHtmlNav());
	cbNav.addChangeListener(this);
	add(cbNav, MIG.WRAP);

	add(new JLabel("     "), "split 2");

	cbNavImage = new JCheckBox(I18N.getMsg("export.options.html.navimage"));
	cbNavImage.setSelected(paramExport.getHtmlNavImage());
	cbNavImage.setEnabled(cbNav.isSelected());
	add(cbNavImage, MIG.WRAP);

	ckChapterBreakPage = new JCheckBox(I18N.getMsg("export.chapter.break_page"));
	ckChapterBreakPage.setSelected(paramExport.getHtmlChapterBreakPage());
	add(ckChapterBreakPage, MIG.WRAP);
    }

    private void htmlUseCssChanged(ItemEvent evt) {
	if (evt.getStateChange() == ItemEvent.SELECTED) {
	    txCssFile.setEnabled(true);
	    btCssFile.setEnabled(true);
	} else {
	    txCssFile.setEnabled(false);
	    btCssFile.setEnabled(false);
	}
    }

    private void ChooseCssFile() {
	JFileChooser chooser = new JFileChooser(txCssFile.getText());
	if (txCssFile.getText().isEmpty()) {
	    chooser.setCurrentDirectory(new File(EnvUtil.getDefaultExportDir(dlgExport.getMainFrame())));
	}
	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	chooser.setFileFilter(new CssFileFilter());
	int i = chooser.showOpenDialog(this);
	if (i != 0) {
	    return;
	}
	File file = chooser.getSelectedFile();
	txCssFile.setText(file.getAbsolutePath());
	txCssFile.setBackground(Color.WHITE);
    }

    public void apply(BookParamExport p) {
	//LOG.trace(TT + ".apply(p)");
	if (!cbUseCss.isSelected()) {
	    txCssFile.setText("");
	}
	p.setHtmlCss(txCssFile.getText());
	p.setHtmlNav(cbNav.isSelected());
	if (cbNav.isSelected()) {
	    p.setHtmlNavImage(cbNavImage.isSelected());
	} else {
	    p.setHtmlNavImage(false);
	}
	p.setHtmlChapterBreakPage(ckChapterBreakPage.isSelected());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
	cbNavImage.setEnabled(cbNav.isSelected());
    }

    public class CssFileFilter extends javax.swing.filechooser.FileFilter {

	@Override
	public boolean accept(File file) {
	    if (file.isDirectory()) {
		return true;
	    }
	    String filename = file.getName();
	    return filename.endsWith(".css");
	}

	@Override
	public String getDescription() {
	    return "CSS Files (*.css)";
	}
    }
}
