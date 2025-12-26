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
package storybook.project;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import resources.icons.ICONS;
import storybook.App;
import storybook.Pref;
import storybook.db.book.BookInfo;
import storybook.db.book.BookUtil;
import storybook.dialog.AbsDialog;
import storybook.exim.exporter.ExportToPhpBB;
import storybook.review.Review;
import storybook.tools.html.CSS;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.Ui.BMANDATORY;
import static storybook.ui.panel.AbstractPanel.*;

/**
 *
 * @author favdb
 */
public class PropertiesDlg extends AbsDialog implements ChangeListener {

	private static final String TT = "PropertiesDlg.";

	private final Project project;
	private final BookInfo bookInfo;
	public JTextField tfTitle, tfSubtitle, tfAuthor, tfCopyright;
	private JTextArea taNotes, taBlurb;
	private JCheckBox ckUseNonModalEditors, ckScenario, ckMarkdown;
	private JComboBox cbSceneDateInit, cbNature;
	private JTextPane fileInfoPanel;
	private PropAssistantPanel PropAssistant;
	private PropBookLayoutPanel PropLayout;
	private PropPublicationPanel propPub;
	private PropXEditor PropEditor;
	private ShefEditor hDedication;
	private JButton btInitPhp;
	private JTabbedPane tabbedPane;
	private JPanel tbProject, tbDedication, tbAssistant, tbWorking, tbBookLayout;
	private boolean bMarkdown, bNew;//are these properties for a new file
	//TODO calendar
	//private JCheckBox ckUseCalendar;

	public static void show(MainFrame mainFrame) {
		PropertiesDlg dlg = new PropertiesDlg(mainFrame, false);
		dlg.setVisible(true);
	}
	private int origin;

	public PropertiesDlg(MainFrame m, Project proj) {
		super(m);
		this.bNew = false;
		this.project = proj;
		this.bookInfo = proj.book.info;
		origin = proj.book.hashCode();
		setModal(true);
		initAll();
	}

	public PropertiesDlg(MainFrame m, boolean bNew) {
		this(m, m.project);
		this.bNew = bNew;
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "[][]"));
		setTitle(I18N.getMsg("properties.title"));
		tabbedPane = new JTabbedPane();
		tbProject = initProject();
		tabbedPane.addTab(I18N.getMsg("properties"), tbProject);
		tbDedication = initDedication();
		tabbedPane.addTab(I18N.getMsg("book.dedication"), tbDedication);
		initUiForMainframe();
		Dimension dim = new Dimension(800, 600);
		tabbedPane.setPreferredSize(dim);
		tabbedPane.setMaximumSize(SwingUtil.getScreenSize());
		add(tabbedPane, MIG.get(MIG.GROW, MIG.SPAN));
		btInitPhp = Ui.initButton("btInit", "export.phpbb", ICONS.K.COGS, "", e -> getInitPhpBB());
		add(btInitPhp);
		btInitPhp.setVisible(App.preferences.getBoolean(Pref.KEY.EXP_PHPBB));
		JPanel pOK = new JPanel(new MigLayout());
		pOK.add(getOkButton());
		pOK.add(getCancelButton());
		add(pOK, MIG.RIGHT);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	private void initUiForMainframe() {
		if (mainFrame != null) {
			tbWorking = iniTravail();
			tabbedPane.addTab(I18N.getMsg("preferences.global"), tbWorking);
			tbAssistant = initAssistant();
			if (tbAssistant.isVisible()) {
				tabbedPane.addTab(I18N.getMsg("assistant.title"), tbAssistant);
			}
			tbBookLayout = initBookLayout();
			tabbedPane.addTab(I18N.getMsg("book.layout"), tbBookLayout);
			propPub = new PropPublicationPanel(mainFrame, this);
			tabbedPane.addTab(I18N.getMsg("epub.title"), propPub);
			tabbedPane.addTab(I18N.getMsg("file.info"), initFileInfo());
		}
	}

	private void getInitPhpBB() {
		StringBuilder b = new StringBuilder();
		b.append(Html.intoB(I18N.getColonMsg("book.title"))).append(" ")
				.append(book.getTitle()).append("\n");
		if (!book.getSubtitle().isEmpty()) {
			b.append(Html.intoB(I18N.getColonMsg("book.title"))).append(" ")
					.append(book.getTitle()).append("\n");
		}
		if (book.info.natureGet() > 0) {
			String nature = "";
			String x = I18N.getMsg("book.nature." + book.info.natureGet());
			if (!x.startsWith("!")) {
				nature = x.substring(0, x.indexOf(":")).trim();
			}
			b.append(Html.intoB(I18N.getColonMsg("book.nature"))).append(" ")
					.append(nature).append("\n");
		}
		if (!book.info.blurbGet().isEmpty()) {
			b.append(Html.intoB(I18N.getColonMsg("book.blurb"))).append(" ")
					.append(book.info.blurbGet()).append("\n");
		}
		ExportToPhpBB.getInit(b.toString());
	}

	/**
	 * initialize project properties: scenario, type, title, subtitle, author, copyright, blob,
	 * notes, markdown
	 *
	 * @return
	 */
	private JPanel initProject() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout(MIG.WRAP, "[right][grow]"));

		ckScenario = Ui.initCheckBox(null, "scenario", "scenario", false, BMANDATORY);
		panel.add(ckScenario, MIG.get(MIG.SKIP));

		panel.add(new JLabel(I18N.getColonMsg("book.nature")), MIG.RIGHT);
		cbNature = initCbNature();
		panel.add(cbNature);

		panel.add(new JLabel(I18N.getColonMsg("book.title")));
		tfTitle = new JTextField();
		panel.add(tfTitle, MIG.GROWX);

		panel.add(new JLabel(I18N.getColonMsg("book.subtitle")));
		tfSubtitle = new JTextField();
		panel.add(tfSubtitle, MIG.GROWX);

		panel.add(new JLabel(I18N.getColonMsg("book.author")));
		tfAuthor = new JTextField();
		panel.add(tfAuthor, MIG.GROWX);

		panel.add(new JLabel(I18N.getColonMsg("book.copyright")));
		tfCopyright = new JTextField();
		panel.add(tfCopyright, MIG.GROWX);

		panel.add(new JLabel(I18N.getColonMsg("book.blurb")), "top");
		taBlurb = new JTextArea();
		taBlurb.setLineWrap(true);
		taBlurb.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane(taBlurb);
		SwingUtil.setMaxPreferredSize(scroll);
		panel.add(scroll, MIG.GROW);

		panel.add(new JLabel(I18N.getColonMsg("notes")), "top");
		taNotes = new JTextArea();
		taNotes.setLineWrap(true);
		taNotes.setWrapStyleWord(true);
		JScrollPane scroller = new JScrollPane(taNotes);
		SwingUtil.setMaxPreferredSize(scroller);
		panel.add(scroller, MIG.GROW);

		panel.add(new JLabel(""));

		ckMarkdown = Ui.initCheckBox(null, "markdown", "markdown", false, BMANDATORY);
		panel.add(ckMarkdown, MIG.get(MIG.SPAN, MIG.RIGHT));

		if (project != null) {
			tfTitle.setText(bookInfo.titleGet());
			tfSubtitle.setText(bookInfo.subtitleGet());
			tfAuthor.setText(bookInfo.authorGet());
			tfCopyright.setText(bookInfo.copyrightGet());
			taNotes.setText(bookInfo.notesGet());
			taNotes.setCaretPosition(0);
			taBlurb.setText(bookInfo.blurbGet());
			taBlurb.setCaretPosition(0);
			ckScenario.setSelected(bookInfo.scenarioGet());
			ckMarkdown.setSelected(bookInfo.markdownGet());
		}
		ckScenario.addChangeListener(this);
		return panel;
	}

	@SuppressWarnings("unchecked")
	public static JComboBox initCbNature() {
		//LOG.trace(TT + "initCbNature()");
		JComboBox cb = new JComboBox();
		cb.setName("cbNature");
		int i = 0;
		while (true) {
			String x = I18N.getMsg("book.nature." + i);
			if (x.startsWith("!")) {
				break;
			}
			cb.addItem(x.substring(0, x.indexOf(":")).trim());
			i++;
		}
		return cb;
	}

	@Override
	public MainFrame getMainFrame() {
		return (mainFrame);
	}

	private void initSceneDate(JPanel p) {
		p.add(new JLabel(I18N.getColonMsg("date.init")), MIG.SPLIT2);
		cbSceneDateInit = Ui.initComboBox("cbSceneDateInit",
				"date.init",
				new String[]{"date.init.empty", "date.init.last", "date.init.today"},
				1, !EMPTY, !ALL);
		cbSceneDateInit.setSelectedIndex(book.info.sceneDateInitGet());
		p.add(cbSceneDateInit);
	}

	private void initModalEditors(JPanel p) {
		/*ckUseNonModalEditors = Ui.initCheckBox(null,
				"ckUseNonModalEditors",
				"editors.nonmodal",
				book.param.getParamEditor().getModless(),
				!MANDATORY);
		ckUseNonModalEditors.setEnabled(false);
		p.add(ckUseNonModalEditors, MIG.WRAP);*/
	}

	/**
	 * initialize Travail properties: scenes date, external editor
	 *
	 * @return
	 */
	private JPanel iniTravail() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.HIDEMODE2, MIG.WRAP)));
		initSceneDate(p);
		p.add(Review.Properties(mainFrame));
		initModalEditors(p);
		PropEditor = new PropXEditor(mainFrame);
		p.add(PropEditor, MIG.GROWX);
		//initCalendar(p);
		JPanel ps = new JPanel(new MigLayout(MIG.FILL));
		JScrollPane scroll = new JScrollPane(p);
		SwingUtil.setMaxPreferredSize(scroll);
		ps.add(scroll);
		return ps;
	}

	/**
	 * initialize book layout: part title, chapter title..., scene title..., reread comments
	 *
	 * @return
	 */
	private JPanel initBookLayout() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP + " 2")));

		PropLayout = new PropBookLayoutPanel(book.getParam().getParamLayout(), true);
		p.add(PropLayout);

		JPanel ps = new JPanel(new MigLayout(MIG.FILL));
		JScrollPane scroll = new JScrollPane(p);
		SwingUtil.setMaxPreferredSize(scroll);
		ps.add(scroll);

		return ps;
	}

	/**
	 * initialize dedication
	 *
	 * @return
	 */
	private JPanel initDedication() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "", "[grow]"));
		hDedication = new ShefEditor(mainFrame.project.getPath(), "none", book.getDedication());
		hDedication.setName("book.dedication");
		hDedication.setMinimumSize(new Dimension(200, 80));
		hDedication.setPreferredSize(new Dimension(800, 600));
		hDedication.setMaximumSize(new Dimension(1600, 1200));
		p.add(hDedication);
		return p;
	}

	/**
	 * initialize assistant usage
	 *
	 * @return
	 */
	private JPanel initAssistant() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FLOWX, MIG.HIDEMODE3, MIG.WRAP), "[grow]"));
		PropAssistant = new PropAssistantPanel(this, mainFrame);
		JScrollPane sc = new JScrollPane(PropAssistant);
		p.add(sc, MIG.get(MIG.SPAN, MIG.GROW));
		return (p);
	}

	/**
	 * initialize file informations
	 *
	 * @return
	 */
	private JPanel initFileInfo() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "", "[grow]"));
		int textLength = BookUtil.getNbChars(project);
		int words = BookUtil.getNbWords(project);
		File file = project.getFile();
		StringBuilder buf = new StringBuilder();
		buf.append(Html.HTML_B);
		buf.append(CSS.getHeadWithCss(mainFrame.getFont()));
		buf.append(Html.BODY_B).append(Html.TABLE_B);
		buf.append(Html.getRow2Cols("file.info.filename", (file != null ? file.toString() : "null")));
		buf.append(Html.getRow2Cols("file.info.creation", project.book.getCreation()));
		String size
				= String.format("%,d %s (%,d %s) => %d %s",
						words, I18N.getMsg("words"),
						textLength, I18N.getMsg("characters"),
						words / 480, I18N.getMsg("pages"));
		//nb de pages sur la base de 480 mots par page
		buf.append(Html.getRow2Cols("file.info.text.length", size));
		buf.append(Html.getRow2Cols("episodes", project.episodes.getCount()));
		buf.append(Html.getRow2Cols("strands", project.strands.getCount()));
		buf.append(Html.getRow2Cols("parts", project.parts.getCount()));
		buf.append(Html.getRow2Cols("chapters", project.chapters.getCount()));
		buf.append(Html.getRow2Cols("scenes", project.scenes.getCount()));
		buf.append(Html.getRow2Cols("persons", project.persons.getCount()));
		buf.append(Html.getRow2Cols("locations", project.locations.getCount()));
		buf.append(Html.getRow2Cols("items", project.items.getCount()));
		buf.append(Html.getRow2Cols("plots", project.plots.getCount()));
		buf.append(Html.getRow2Cols("tags", project.tags.getCount()));
		buf.append(Html.getRow2Cols("endnotes", project.endnotes.getCount()));
		buf.append(Html.getRow2Cols("events", project.events.getCount()));
		buf.append(Html.getRow2Cols("ideas", project.ideas.getCount()));
		buf.append(Html.getRow2Cols("memos", project.memos.getCount()));
		buf.append(Html.getRow2Cols("duration", project.getDuration()));
		buf.append(Html.TABLE_E)
				.append(Html.BODY_E)
				.append(Html.HTML_E);
		fileInfoPanel = new JTextPane();
		fileInfoPanel.setEditable(false);
		fileInfoPanel.setContentType(Html.TYPE);
		fileInfoPanel.setText(buf.toString());
		fileInfoPanel.setCaretPosition(0);
		JScrollPane scroller = new JScrollPane();
		scroller.setViewportView(fileInfoPanel);
		p.add(scroller, MIG.GROW);
		return (p);
	}

	private boolean verify() {
		String rc = BookInfo.isDataOK(tfTitle.getText(),
				tfSubtitle.getText(),
				tfAuthor.getText(),
				tfCopyright.getText());
		if (!rc.isEmpty()) {
			tabbedPane.setSelectedComponent(tbProject);
			showError(rc);
			return false;
		}
		rc = PropEditor.verify();
		if (!rc.isEmpty()) {
			tabbedPane.setSelectedComponent(tbWorking);
			showError(rc);
			return false;
		}
		return true;
	}

	private void showError(String rc) {
		JOptionPane.showMessageDialog(this, rc, I18N.getMsg("error"), JOptionPane.OK_OPTION);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (verify()) {
					apply();
				}
			}
		};
	}

	private void apply() {
		//LOG.trace(TT + "apply()");
		if (mainFrame == null) {
			dispose();
			return;
		}
		//book info : title, subtitle, author, copyright, blurb, notes, scenario,markdown, nature
		bookInfo.titleSet(tfTitle.getText());
		bookInfo.subtitleSet(tfSubtitle.getText());
		bookInfo.authorSet(tfAuthor.getText());
		bookInfo.copyrightSet(tfCopyright.getText());
		bookInfo.blurbSet(taBlurb.getText());
		bookInfo.notesSet(taNotes.getText());
		bookInfo.dedicationSet(hDedication.getText());
		// if not new file and markdown change then convert all text
		if (!bNew && ckMarkdown.isSelected() != bookInfo.markdownGet()) {
			BookUtil.convertTo(mainFrame, !bookInfo.markdownGet());
		}
		bookInfo.markdownSet(ckMarkdown.isSelected());
		bookInfo.scenarioSet(ckScenario.isSelected());
		bookInfo.natureSet(cbNature.getSelectedIndex());

		//TODO calendar
		if (ckUseNonModalEditors != null) {
			book.param.getParamEditor().setModless(ckUseNonModalEditors.isSelected());
		}
		bookInfo.sceneDateInitSet(cbSceneDateInit.getSelectedIndex());

		// book assistant, epub, layout
		PropAssistant.apply();
		propPub.apply();
		PropLayout.apply();
		if (origin != book.hashCode()) {
			mainFrame.getBookModel().setRefresh(mainFrame.getView(SbView.VIEWNAME.READING));
		}
		PropEditor.apply();

		SwingUtil.setWaitingCursor(this);
		mainFrame.setUpdated();

		SwingUtil.setDefaultCursor(this);
		dispose();
	}

	/*public void setSettings() {
		if (book != null) {
			book.info.majSet();
			book.info.titleSet(tfTitle.getText());
			book.info.subtitleSet(tfSubtitle.getText());
			book.info.authorSet(tfAuthor.getText());
			book.info.copyrightSet(tfCopyright.getText());
			book.info.blurbSet(taBlurb.getText());
			book.info.notesSet(taNotes.getText());
			book.info.scenarioSet(ckScenario.isSelected());
			book.info.markdownSet(ckMarkdown.isSelected());
		}
	}*/
	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox ck = (JCheckBox) e.getSource();
			if (ck.getName().equals("scenario") && ck.isSelected()) {
				ckMarkdown.setSelected(true);
			}
		}
	}

	public PropXEditor getXeditor() {
		return PropEditor;
	}

}
