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

import i18n.I18N;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import api.mig.swing.MigLayout;
import storybook.exim.exporter.ExportBookDlg;
import storybook.db.book.BookParamExport;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class BOOKpanel extends JPanel implements ChangeListener {

	private static final String TT = "BOOKPanel";

	private final BookParamExport paramExport;
	private JCheckBox ckChapterBooktitle;
	private JRadioButton rbMultiChapter;
	private JRadioButton rbMultiScene;
	public JRadioButton rbOneFile;
	private JRadioButton rbIgnore;
	private JRadioButton rbSuppress;
	private JRadioButton rbLeft;
	private final ExportBookDlg dlgExport;

	public BOOKpanel(ExportBookDlg dlg) {
		this.dlgExport = dlg;
		this.paramExport = dlg.getParamExport();
		initUi();
	}

	private void initUi() {
		//LOG.trace(TT + ".initUi() " + paramExport.toString());
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE2, MIG.WRAP), "[][]"));
		add(new JLabel(I18N.getMsg("export.book.htmloption")), MIG.SPAN);
		ButtonGroup bg1 = new ButtonGroup();

		rbOneFile = new JRadioButton(I18N.getMsg("export.book.htmloption.onefile"));
		rbOneFile.setSelected(!paramExport.isMulti());
		rbOneFile.addChangeListener(this);
		bg1.add(rbOneFile);
		add(rbOneFile, MIG.SKIP + " 1");

		rbMultiChapter = new JRadioButton(I18N.getMsg("export.book.htmloption.multichapter"));
		rbMultiChapter.setSelected(paramExport.getHtmlMultiChapter());
		rbMultiChapter.addChangeListener(this);
		bg1.add(rbMultiChapter);
		add(rbMultiChapter, MIG.get(MIG.SKIP + " 1", MIG.SPLIT2));

		ckChapterBooktitle = new JCheckBox(I18N.getMsg("export.chapter.booktitle"));
		ckChapterBooktitle.setSelected(paramExport.getHtmlChapterTitle());
		ckChapterBooktitle.setEnabled(rbMultiChapter.isSelected());
		add(ckChapterBooktitle);

		rbMultiScene = new JRadioButton(I18N.getMsg("export.book.htmloption.multiscene"));
		rbMultiScene.setSelected(paramExport.getHtmlMultiScene());
		rbMultiScene.addChangeListener(this);
		bg1.add(rbMultiScene);
		add(rbMultiScene, MIG.SKIP + " 1");
		// highligther
		add(new JLabel(I18N.getMsg("export.book.highlight")), MIG.SPAN);
		int t = paramExport.getHighlight();
		ButtonGroup bg2 = new ButtonGroup();
		rbIgnore = Ui.initRadio(bg2, "export.book.highlight.ignore", t == 0);
		add(rbIgnore, MIG.SKIP + " 1");
		rbSuppress = Ui.initRadio(bg2, "export.book.highlight.suppress", t == 1);
		add(rbSuppress, MIG.SKIP + " 1");
		rbLeft = Ui.initRadio(bg2, "export.book.highlight.left", t == 2);
		add(rbLeft, MIG.SKIP + " 1");
	}

	public void apply(BookParamExport p) {
		//LOG.trace(TT + ".apply(p)");
		p.setHtmlMultiChapter(rbMultiChapter.isSelected());
		p.setHtmlMultiScene(rbMultiScene.isSelected());
		int x = 0;
		if (rbSuppress.isSelected()) {
			x = 1;
		}
		if (rbLeft.isSelected()) {
			x = 2;
		}
		p.setHighlight(x);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		ckChapterBooktitle.setEnabled(rbMultiChapter.isSelected() || rbMultiScene.isSelected());
	}

}
