/*
 * Copyright (C) 2019 favdb
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
package storybook.edit;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import assistant.Assistant;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.endnote.Endnote;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.dialog.NameAspectDlg;
import storybook.dialog.chooser.ColorChooserPanel;
import storybook.dialog.chooser.ImageChooserPanel;
import storybook.tools.StringUtil;
import storybook.tools.file.TempUtil;
import storybook.tools.file.XEditorFile;
import storybook.tools.html.Html;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;
import storybook.tools.swing.js.JSDateChooser;
import storybook.ui.MIG;
import storybook.ui.MainMenu;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public abstract class AbstractEditor extends AbstractPanel {

	private static final String TT = "AbstractEditor.";
	private static final String DESCRIPTION = "description", NOTES = "notes";

	public AbstractEntity entity;
	public JTextField tfName;
	private String aspect = "";
	public JTabbedPane tab1;
	public String msgError;
	public boolean modified = false;
	public Editor editor;
	private JTextPane tpAssistant;
	public ShefEditor hDescription, hNotes;
	public boolean bDesc, bNotes;
	public JPanel pHead, pUpper, pFoot;
	public JButton btDeploy;
	private boolean deploy = false;
	private String common;
	private JButton btAssistantToDesc, btAssistantToNotes, btAssistantErase;
	public Integer entityHash;
	private JPanel panelAssistant;

	protected AbstractEditor(Editor m, AbstractEntity e, String common) {
		super(m.getMainFrame());
		this.editor = m;
		this.entity = e;
		this.common = common;
	}

	protected AbstractEditor(AbstractEntity e, String common) {
		super();
		this.editor = null;
		this.entity = e;
		this.common = common;
	}

	@Override
	public void initAll() {
		super.initAll();
		setOrigine();
	}

	@Override
	public void init() {
		entityHash = entity.hashCode();
		aspect = entity.getAspect();
		deploy = App.preferences.getBoolean(Pref.KEY.EDITOR_DEPLOY);
		bDesc = App.preferences.getBoolean(Pref.KEY.EDITOR_TABBED_DESCRIPTION);
		bNotes = App.preferences.getBoolean(Pref.KEY.EDITOR_TABBED_NOTES);
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+"initUi() name=" + entity.getName());
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE3, MIG.INS1, MIG.GAP1), "[][grow][]"));
		add(MainMenu.getHiddenMenu(this), MIG.SPAN);
		entity = TempUtil.restore(mainFrame, entity);
		setTitle(I18N.getColonMsg("editor") + " " + I18N.getMsg(entity.getObjType().toString()));

		// name and deploy button
		initHead();
		// upper panel for main informations
		pUpper = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE2, MIG.WRAP), "[][grow]"));
		pUpper.setName(entity.getObjType().toString());
		add(pUpper, MIG.get(MIG.SPAN, MIG.GROW));

		// first tabbed pane for description, notes, assistant and others
		tab1 = new JTabbedPane();
		tab1.setPreferredSize(new Dimension(1024, 1024));
		add(tab1, MIG.get(MIG.SPAN, MIG.GROW));

		initUpper();
		initBottom();// tabs for description, notes, assistant
		initFoot();// buttons for assistant call, write, cancel and ok
		if (entity instanceof Relation) {
			hNotes.getWysiwyg().setShowHideTB(true);
		} else if (entity instanceof Endnote) {
			pHead.setVisible(false);
			//hNotes.getWysEditor().setCaretPosition(0);
			SwingUtilities.invokeLater(() -> {
				hNotes.getWysEditor().requestFocusInWindow();
			});
		} else {
			setDeploy();
		}
		if (pUpper.getComponentCount() == 0) {
			pUpper.setVisible(false);
		}
	}

	/**
	 * initialize the name and deploy button
	 */
	public void initHead() {
		pHead = new JPanel(new MigLayout(MIG.FILL, "[][][grow][]"));
		btDeploy = Ui.initButton("btDeploy", "", ICONS.K.DP_UP, "deploy",
				(arg0 -> setDeploy(!deploy)));
		pHead.add(btDeploy);
		JLabel lbName = new JLabel(I18N.getColonMsg("name"));
		lbName.setFont(FontUtil.getBold(lbName.getFont()));
		pHead.add(lbName);
		tfName = new JTextField();
		tfName.setName("name");
		tfName.setText(entity.getName());
		SwingUtil.setAspect(tfName, aspect);
		pHead.add(tfName, MIG.GROWX);
		JButton bt = Ui.initButton("btAspect", "",
				ICONS.K.COLOR,
				"name.aspect",
				e -> changeAspect());
		pHead.add(bt);
		add(pHead, MIG.get(MIG.SPAN, MIG.GROWX));
	}

	/**
	 * initialize the upper part of the editor
	 */
	public void initUpper() {

	}

	/**
	 * initialize the foot part of the editor (for description, notes, assistant) and buttons
	 * (assistant, write, cancel, ok)
	 */
	public void initFoot() {
		pFoot = new JPanel(new MigLayout());
	}

	/**
	 * set the deploy button
	 */
	private void setDeploy() {
		deploy = App.preferences.getBoolean(Pref.KEY.EDITOR_DEPLOY);
		if (pUpper != null) {
			pUpper.setVisible(deploy);
		}
		btDeploy.setIcon(IconUtil.getIconSmall((deploy ? ICONS.K.DP_UP : ICONS.K.DP_DOWN)));
	}

	/**
	 * set the deploy button
	 *
	 * @param b : prior state
	 */
	private void setDeploy(boolean b) {
		App.preferences.setBoolean(Pref.KEY.EDITOR_DEPLOY, b);
		setDeploy();
	}

	/**
	 * initialize common data (Description, Notes, Assistant)
	 */
	public void initBottom() {
		//LOG.trace(TT + "initCommon()");
		JTabbedPane tab2 = tab1;
		if (!bDesc || !bNotes) {
			// second tabbed pane
			tab2 = new JTabbedPane();
			add(tab2, MIG.get(MIG.NEWLINE, MIG.SPAN, MIG.GROW));
		}
		while (common.length() < 3) {
			common += "0";
		}
		switch (common.charAt(0)) {
			case '1':
				initDescription(pUpper, (bDesc ? tab1 : tab2));
				break;
			case '2':
				initDescription(pUpper, tab2);
				break;
			case '9':
				initDescription(pUpper, null);
				break;
			default:
				break;
		}
		switch (common.charAt(1)) {
			case '1':
				initNotes(pUpper, (bNotes ? tab1 : tab2));
				break;
			case '2':
				initNotes(pUpper, tab2);
				break;
			case '9':
				initNotes(pUpper, null);
				break;
			default:
				break;
		}
		if (common.charAt(2) == '1') {
			initAssistant(pUpper, tab1);
		}
	}

	/**
	 * initialize Description data
	 *
	 * @param panel
	 * @param tab
	 */
	public void initDescription(JPanel panel, JTabbedPane tab) {
		hDescription = Ui.initHtmlEditor(panel,
				DESCRIPTION, entity.getDescription(), tab, "");
		hDescription.setBase(mainFrame.project.getPath());
	}

	public ShefEditor getDescription() {
		return hDescription;
	}

	/**
	 * initialize Notes data
	 *
	 * @param panel
	 * @param tab
	 */
	public void initNotes(JPanel panel, JTabbedPane tab) {
		hNotes = Ui.initHtmlEditor(panel,
				NOTES, entity.getNotes(), tab, "");
		hNotes.setBase(mainFrame.project.getPath());
	}

	/**
	 * initialize Assistant data
	 *
	 * @param panel
	 * @param tab
	 */
	public void initAssistant(JPanel panel, JTabbedPane tab) {
		//LOG.trace(TT + "initAssistant(panel, tab) for edit=" + this.getName());
		if (!App.getAssistant().isExists(mainFrame.getBook(), entity.getObjType().toString())) {
			return;
		}
		tpAssistant = new JTextPane();
		tpAssistant.setContentType(Html.TYPE);
		tpAssistant.setEditable(false);
		String values = entity.getAssistant();
		if (!entity.getAssistant().isEmpty()) {
			tpAssistant.setText(Assistant.toHtml(values));
		}
		JScrollPane scroller = new JScrollPane(tpAssistant);
		SwingUtil.setUnitIncrement(scroller);
		scroller.setPreferredSize(SwingUtil.getScreenSize());
		panelAssistant = new JPanel(new MigLayout(MIG.FILL));
		panelAssistant.add(scroller, MIG.get(MIG.GROW, MIG.SPAN, MIG.WRAP));
		btAssistantErase = Ui.initButton("btAssistantErase",
				"clear", ICONS.K.CLEAR, "", evt -> clearAssistant());
		panelAssistant.add(btAssistantErase, MIG.get(MIG.NEWLINE, MIG.SPAN, MIG.SPLIT + " 4"));
		panelAssistant.add(new JLabel(I18N.getColonMsg("assistant.transfer")));
		btAssistantToDesc = Ui.initButton("btAssistantToDescription",
				"", ICONS.K.EDIT, "description", evt -> assistantTo(DESCRIPTION));
		panelAssistant.add(btAssistantToDesc);
		btAssistantToNotes = Ui.initButton("btAssistantToNotes",
				"", ICONS.K.NOTE, "notes", evt -> assistantTo(NOTES));
		panelAssistant.add(btAssistantToNotes);
		setAssistantButton();
		addField(panel, "assistant", MIG.TOP, panelAssistant, tab, BGROW);
	}

	/**
	 * set the Assistant content
	 *
	 * @param str
	 */
	public void setAssistant(String str) {
		tpAssistant.setText(Assistant.toHtml(str));
		setAssistantButton();
	}

	/**
	 * getter for Assistant text pane
	 *
	 * @return
	 */
	public JTextPane getAssistant() {
		return tpAssistant;
	}

	/**
	 * set the Assistant button
	 *
	 */
	public void setAssistantButton() {
		//LOG.trace(TT + "setAssistantButton()");
		boolean a = tpAssistant.getText().isEmpty();
		boolean b = false;
		if (!a) {
			b = true;
		}
		btAssistantToDesc.setEnabled(b);
		btAssistantToNotes.setEnabled(b);
		btAssistantErase.setEnabled(b);
	}

	/**
	 * initialize the SB date
	 *
	 * @param p
	 * @param title
	 * @param date
	 * @param opt
	 * @param time
	 * @return
	 */
	public JSDateChooser initSbDate(JPanel p, String title, Date date, String opt, int time) {
		JSDateChooser d = new JSDateChooser(mainFrame, time);
		d.setName(title);
		if (date != null) {
			d.setDate(date);
		}
		addField(p, title, MIG.TOP, d, null, opt);
		return (d);
	}

	/**
	 * initialize the icon chooser
	 *
	 * @param p
	 * @param title
	 * @param val
	 * @param opt
	 * @return
	 */
	public ImageChooserPanel initIconChooser(JPanel p, String title, String val, String opt) {
		Icon icon = IconUtil.getIconSmall(ICONS.K.UNKNOWN);
		String path = mainFrame.getProject().getPath() + "/Images";
		if (val != null && !val.isEmpty()) {
			icon = IconUtil.getIconExternal(val, IconUtil.getDefDim());
			path = val;
		}
		ImageChooserPanel ic = new ImageChooserPanel(path, icon, val, true);
		if (p != null) {
			addField(p, title, MIG.TOP, ic, null, opt);
		}
		return (ic);
	}

	/**
	 * initialize the color chooser
	 *
	 * @param p
	 * @param title
	 * @param val
	 * @param opt
	 * @return
	 */
	public ColorChooserPanel initColorChooser(JPanel p, String title, Integer val, String opt) {
		ColorChooserPanel ic = new ColorChooserPanel(I18N.getMsg("color.choose"), new Color(val), null, false);
		addField(p, title, "", ic, null, opt);
		return (ic);
	}

	/**
	 * initialize the File chooser
	 *
	 * @param panel
	 * @param title
	 * @param val
	 * @return
	 */
	public JTextField initFileChooser(JPanel panel, String title, String val) {
		JPanel p = new JPanel(new MigLayout(MIG.GAP0));
		p.add(new JLabel(I18N.getColonMsg(title)));
		JTextField tfFile = new JTextField(30);
		tfFile.setName(title);
		tfFile.setText(val);
		p.add(tfFile, MIG.GROWX);
		Dimension sz = IconUtil.getDefDim();//new Dimension(20, 20);
		JButton btChooseFile = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		SwingUtil.setFixedSize(btChooseFile, sz);
		btChooseFile.addActionListener((ActionEvent arg0) -> {
			JFileChooser fc = new JFileChooser(tfFile.getText());
			if (tfFile.getText().isEmpty()) {
				fc = new JFileChooser(mainFrame.getProject().getPath());
			}
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int ret = fc.showOpenDialog(mainFrame);
			if (ret != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File dir = fc.getSelectedFile();
			tfFile.setText(dir.getAbsolutePath());
		});
		btChooseFile.setToolTipText(I18N.getMsg("file.select"));
		p.add(btChooseFile);
		JButton btResetFile = new JButton(IconUtil.getIconSmall(ICONS.K.CLEAR));
		SwingUtil.setFixedSize(btResetFile, sz);
		btResetFile.setToolTipText(I18N.getMsg("file.clear"));
		btResetFile.addActionListener((ActionEvent arg0) -> tfFile.setText(""));
		p.add(btResetFile);
		JButton btSetFile = new JButton(IconUtil.getIconSmall(ICONS.K.NEW_SCENE));
		SwingUtil.setFixedSize(btSetFile, sz);
		btSetFile.setToolTipText(I18N.getMsg("xeditor.create"));
		btSetFile.addActionListener((ActionEvent arg0) -> {
			tfFile.setText(XEditorFile.getDefaultFilePath(mainFrame, tfName.getText()));
		});
		p.add(btSetFile);
		if (((Scene) entity).getSceneno() == 0) {
			btSetFile.setEnabled(false);
		}
		panel.add(p, MIG.GROWX);
		return (tfFile);
	}

	/**
	 * verifier to restore data
	 *
	 * @return
	 */
	public abstract boolean verifier();

	/**
	 * verifier for numeric data
	 *
	 * @param tf
	 * @param isMandatory
	 */
	public void verifierNumeric(JTextField tf, boolean isMandatory) {
		if (isMandatory && tf.getText().isEmpty()) {
			errorMsg(tf, Const.ERROR_MISSING);
		} else if (!StringUtil.isNumeric(tf.getText())) {
			errorMsg(tf, Const.ERROR_NOTNUMERIC);
		}
	}

	/**
	 * restore if description and notes not empty
	 *
	 * @param which : <br>
	 * 1=description is mandatory<br>
	 * 2=notes is mandatory<br>
	 * 3=both are mandatory 4 if one only is mandatory
	 */
	public void checkDescNotes(int which) {
		boolean bbDesc = false, bbNotes = false;
		if (hDescription != null) {
			bbDesc = Html.htmlToText(hDescription.getText()).isEmpty();
		}
		if (hNotes != null) {
			bbNotes = Html.htmlToText(hNotes.getText()).isEmpty();
		}
		switch (which) {
			case 1:
				if (bbDesc) {
					errorMsg(hDescription, Const.ERROR_MISSING);
				}
				break;
			case 2:
				if (bbNotes) {
					errorMsg(hNotes, Const.ERROR_MISSING);
				}
				break;
			case 3:
				if (bbDesc || bbNotes) {
					errorMsg(hDescription, Const.ERROR_MISSING);
				}
				break;
			case 4:
				if (bbDesc && bbNotes) {
					errorMsg(hDescription, Const.ERROR_MISSING);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * write the datas
	 *
	 */
	public void save() {
		//LOG.trace(TT + "save()");
		mainFrame.setUpdated();
	}

	/**
	 * ask for saving data
	 *
	 * @return
	 */
	private int askSave() {
		final Object[] options = {
			I18N.getMsg("save.changes"),
			I18N.getMsg("discard.changes"),
			I18N.getMsg("cancel")};
		return JOptionPane.showOptionDialog(this,
				I18N.getMsg("save.or.discard.changes"),
				I18N.getMsg("save.changes.title"),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
	}

	/**
	 * restore if data was modified
	 *
	 * @return
	 */
	public int isModified() {
		//LOG.trace(TT + "isModified() entityHash=" + entityHash + ", hash=" + entity.hashCode());
		if (entity.toText().hashCode() != entityHash) {
			return askSave();
		}
		return JOptionPane.OK_OPTION;
	}

	/**
	 * setting the origin hash
	 *
	 */
	public void setOrigine() {
		//apply();
		entityHash = entity.toText().hashCode();
	}

	/**
	 * apply changes, only get the datas
	 *
	 */
	public void apply() {
		//LOG.trace(TT + "apply()");
		entity.setName(tfName.getText());
		entity.setAspect(aspect);
		if (common.charAt(0) != '0') {
			entity.setDescription(hDescription != null ? Html.checkImages(mainFrame, hDescription.getText()) : "");
		}
		if (common.charAt(1) != '0') {
			entity.setNotes(hNotes != null ? Html.checkImages(mainFrame, hNotes.getText()) : "");
		}
		if (common.charAt(2) != '0') {
			// nothing, made by doAssistant()
		}
	}

	/**
	 * reset error message
	 *
	 */
	public void resetError() {
		msgError = "";
		if (tfName.getText().isEmpty()) {
			errorMsg(tfName, Const.ERROR_MISSING);
		}
		AbstractEntity s = mainFrame.project.findByName(entity.getObjType(), tfName.getText());
		if (s != null && entity.getId() != null && !Objects.equals(s.getId(), entity.getId())) {
			errorMsg(tfName, Const.ERROR_EXISTS);
		}
		if (hDescription != null && hDescription.getText().length() > 32768) {
			errorMsg(hDescription, Const.ERROR_EXCEED);
		}
		if (hNotes != null && hNotes.getText().length() > 32768) {
			errorMsg(hNotes, Const.ERROR_EXCEED);
		}
	}

	/**
	 * add to error message field
	 *
	 * @param comp
	 * @param msg
	 */
	public void errorMsg(JComponent comp, String msg) {
		msgError += I18N.getColonMsg(comp.getName()) + " " + I18N.getMsg(msg) + "\n";
		comp.setBorder(new LineBorder(Color.red, 1));
	}

	/**
	 * add standard error message for a String
	 *
	 * @param comp
	 * @param msg
	 */
	public void errorString(JComponent comp, String msg) {
		msgError += I18N.getColonMsg(comp.getName()) + " " + msg + "\n";
		comp.setBorder(new LineBorder(Color.red, 1));
	}

	/**
	 * action for entity
	 *
	 * @param compName
	 * @param cb
	 */
	public void actionEntity(String compName, JSCheckList cb) {
		//LOG.trace(TT + "actionEntity(compName=" + compName + ", cb=" + cb.getName() + ")");
		if (compName == null) {
			return;
		}
		if (compName.startsWith("BtAdd")) {
			switch (Book.getTYPE(cb.getName())) {
				case CATEGORIES:
					mainFrame.showEditorAsDialog(new Category());
					break;
				case ITEMS:
					mainFrame.showEditorAsDialog(new Item());
					break;
				case LOCATIONS:
					mainFrame.showEditorAsDialog(new Location());
					break;
				case PERSONS:
					mainFrame.showEditorAsDialog(new Person());
					break;
				case PLOTS:
					mainFrame.showEditorAsDialog(new Plot());
					break;
				case STRANDS:
					mainFrame.showEditorAsDialog(new Strand());
					break;
				case TAGS:
					mainFrame.showEditorAsDialog(new Tag());
					break;
				default:
					return;
			}
			cb.initAll();
		} else if (compName.startsWith("BtEdit")) {
			if (cb.getPointedEntity() == null) {
				return;
			}
			mainFrame.showEditorAsDialog(cb.getPointedEntity());
			cb.initAll();
		}
	}

	/**
	 * write data to temporary
	 *
	 */
	public void tempSave() {
		if (entity instanceof Scene) {
			Scene scene = new Scene();
			// EntityUtil.copyEntityProperties(mainFrame, entity, scene);
			scene.setSummary(msgError);
			TempUtil.write(mainFrame, entity);
			//EntityUtil.copyEntityProperties(mainFrame, scene, entity);
		}
	}

	/**
	 * set title of the entity
	 *
	 * @param str
	 */
	public void setTitle(String str) {
		if (str != null && editor != null) {
			editor.setTitle(str);
		}
	}

	/**
	 * set the entity
	 *
	 * @param e
	 */
	public void setEntity(AbstractEntity e) {
		setTitle(I18N.getColonMsg("editor") + " " + I18N.getMsg(entity.getObjType().toString()));
		this.removeAll();
		this.entity = e;
		this.initAll();
		this.revalidate();
	}

	/**
	 * convert Assistant data to HTML
	 *
	 * @param which
	 */
	private void assistantTo(String which) {
		//LOG.trace(TT + "assistantTo(which=" + which + ")");
		String rep = tpAssistant.getText()
				.replace(Html.HTML_B, "")
				.replace(Html.HTML_E, "")
				.replace(Html.BODY_B, "")
				.replace(Html.BODY_E, "");
		if (which.equals(DESCRIPTION)) {
			String st = hDescription.getText() + Html.P_EMPTY + rep;
			hDescription.setText(st);
			tpAssistant.setText("");
			//selectTab(hDescription);
		} else if (which.equals(NOTES)) {
			String st = hNotes.getText() + Html.P_EMPTY + rep;
			hNotes.setText(st);
			tpAssistant.setText("");
			//selectTab(hNotes);
		}
		setAssistantButton();
	}

	/**
	 * clear the Assistant data
	 *
	 */
	private void clearAssistant() {
		tpAssistant.setText("");
		setAssistantButton();
	}

	/**
	 * clear the selected Tab
	 *
	 * @param h
	 */
	public void selectTab(ShefEditor h) {
		try {
			tab1.setSelectedComponent(h);
		} catch (Exception ex) {

		}
	}

	/**
	 * select the Assistant Tab
	 */
	public void selectAssistantTab() {
		try {
			tab1.setSelectedComponent(panelAssistant);
		} catch (Exception ex) {

		}
	}

	/**
	 * change aspect for Name
	 *
	 */
	private void changeAspect() {
		NameAspectDlg dlg = new NameAspectDlg(mainFrame, aspect);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			aspect = dlg.getValue();
			SwingUtil.setAspect(tfName, aspect);
		}
	}

}
