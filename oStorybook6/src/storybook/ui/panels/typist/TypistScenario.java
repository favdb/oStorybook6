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
package storybook.ui.panels.typist;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.book.Book;
import storybook.db.idea.Idea;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.scene.ScenarioPanel;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneStateCbModel;
import storybook.db.scene.SceneStateLCR;
import storybook.db.scene.Scenes;
import storybook.edit.Editor;
import storybook.tools.ListUtil;
import storybook.tools.Markdown;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.swing.CbUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panels.AbstractPanel;

/**
 *
 * @author favdb
 */
public class TypistScenario extends AbstractPanel implements ActionListener, IRefreshable {

	private static final String TT = "TypistScenario.";

	private Scene scene;
	private JPanel left, leftPersons;
	private boolean modified;
	private int origine;
	private JButton btNext, btPrevious, btScreen, btSave, btIgnore,
			btFirst, btLast, btHideLeft;
	private JTextArea taDescription, taNotes;
	private JComboBox cbStage, cbScenes, cbStatus;
	private JTextField tfName = new JTextField();
	private Markdown mdEdit = new Markdown("ScenarioPanel");
	private ScenarioPanel scenario;
	private JSCheckList listPersons;
	private boolean leftPanelHide = false, rightPanelHide = false;

	public enum CMD {
		BT_EXIT, // exit oStorybook
		BT_HELP, // help about Markdown syntax
		BT_IDEA, // add new idea
		BT_PERSON_NEW, // add new person and add a button
		BT_IGNORE_CHANGE,// ignore changes and restore original current scene
		BT_SCENE_FIRST, // goto the first scene
		BT_SCENE_LAST, // goto the last scene
		BT_SCENE_PREVIOUS, //goto previous scene
		BT_SCENE_NEXT, //goto next scene
		BT_SCENE_NEW,// add new scene
		BT_SCENE_SAVE,// toXml changes
		BT_SCREEN, // change full/normal screen
		BT_STAGE,// call browser to online help about assistant.stage
		CB_SCENES,// list of scenes
		CB_STAGE,// list of stage
		CB_STATUS,// list of status
		// scenario
		BT_ADD_LOCATION,
		CB_MOMENT,
		CB_LOC,
		CB_IN,
		CB_OUT,
		CB_LOCATIONS,
		RB_INT,
		// communs
		PRINT,// printing current scene
		REFRESH,// refresh current scene
		NONE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public static CMD getCMD(String str) {
		for (CMD a : CMD.values()) {
			if (a.toString().equals(str)) {
				return a;
			}
		}
		return CMD.NONE;
	}

	public TypistScenario(MainFrame mainFrame) {
		super(mainFrame);
		this.scene = (Scene) mainFrame.project.getList(Book.TYPE.SCENE).get(0);
		initAll();
	}

	public TypistScenario(MainFrame mainFrame, Scene scene) {
		super(mainFrame);
		this.scene = scene;
		initAll();
	}

	@Override
	public void init() {
		this.withPart = false;
		App.preferences.typistSet(true);
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi() for scene=" + LOG.trace(scene));
		if (this.getComponentCount() > 0) {
			this.removeAll();
		}
		this.setLayout(
				new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE3, MIG.TOP, MIG.INS1),
						"[][65%, grow 100][20%]"));
		//no part to select
		initToolbar();
		//combobox scene
		cbScenes = Ui.initComboBox(CMD.CB_SCENES.toString(), "",
				(List) mainFrame.project.getList(Book.TYPE.SCENE), scene, !EMPTY, !ALL, this);
		toolbar.add(cbScenes);
		toolbar.add(Ui.initButton(CMD.BT_SCENE_NEW.toString(), "",
				ICONS.K.NEW, "scene.new", this));
		//navigation dans les scènes
		//first scene
		btFirst = Ui.initButton(CMD.BT_SCENE_FIRST.toString(), "",
				ICONS.K.NAV_FIRST, "export.nav.first", this);
		btFirst.setEnabled(!isSceneFirst());
		toolbar.add(btFirst);
		//previous scene
		btPrevious = Ui.initButton(CMD.BT_SCENE_PREVIOUS.toString(), "",
				ICONS.K.NAV_PREV, "export.nav.previous", this);
		btPrevious.setEnabled(false);
		toolbar.add(btPrevious);
		btNext = Ui.initButton(CMD.BT_SCENE_NEXT.toString(), "",
				ICONS.K.NAV_NEXT, "export.nav.next", this);
		int n = Book.getNbOf(mainFrame, Book.TYPE.SCENE);
		if (n < 1) {
			btNext.setEnabled(false);
		}
		toolbar.add(btNext);
		//dernière scène
		btLast = Ui.initButton(CMD.BT_SCENE_LAST.toString(), "",
				ICONS.K.NAV_LAST, "export.nav.last", this);
		btLast.setEnabled(!isSceneLast());
		toolbar.add(btLast);
		toolbar.add(new JToolBar.Separator());
		//enregistrer les modifications
		btSave = Ui.initButton(CMD.BT_SCENE_SAVE.toString(), "", ICONS.K.F_SAVE, "save.changes", this);
		toolbar.add(btSave);
		//ignorer les modifications
		btIgnore = Ui.initButton(CMD.BT_IGNORE_CHANGE.toString(), "", ICONS.K.REFRESH, "discard.changes", this);
		toolbar.add(btIgnore);
		toolbar.add(new JLabel(" "), MIG.get(MIG.PUSH, MIG.GROWX));
		add(toolbar, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.GROWX));
		JToolBar tbRight = new JToolBar();
		tbRight.setLayout(new MigLayout());
		tbRight.setBorder(BorderFactory.createEmptyBorder());
		//tbRight.setOpaque(false);
		tbRight.setFloatable(false);
		//sortie du mode typist
		btScreen = Ui.initButton(CMD.BT_SCREEN.toString(), "", ICONS.K.SCREEN_NORMAL, "screen.normal", this);
		tbRight.add(btScreen);
		// idea
		JButton ideaButton = Ui.initButton(CMD.BT_IDEA.toString(), "", ICONS.K.ENT_IDEA, "foi.new", this);
		tbRight.add(ideaButton);
		// quit
		JButton exitButton = Ui.initButton(CMD.BT_EXIT.toString(), "", ICONS.K.EXIT, "exit", this);
		tbRight.add(exitButton);
		add(tbRight, MIG.get(MIG.SPAN, MIG.RIGHT, MIG.WRAP));
		add(initHeader(), MIG.get(MIG.SPAN, MIG.GROW, MIG.WRAP));
		add(initLeft(), MIG.get(MIG.GROW, MIG.TOP));
		add(initMiddle());
		add(initRight(), MIG.GROWY);
		add(initFooter(), MIG.SPAN);
		if (scene == null) {
			sceneSet((Scene) cbScenes.getItemAt(n));
		} else {
			sceneSet(scene);
		}
		mdEdit.initSpelling(mainFrame);
		origine = getHash();
	}

	@SuppressWarnings("unchecked")
	private JPanel initHeader() {
		JPanel head = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3)));
		head.setBorder(BorderFactory.createRaisedBevelBorder());
		tfName = initTextField("name", 16, "");
		tfName.setEditable(false);
		head.add(new JLabel(I18N.getColonMsg("name")), MIG.get(MIG.SPAN, "split 4"));
		head.add(tfName);
		cbStatus = new JComboBox(new SceneStateCbModel());
		cbStatus.setName(CMD.CB_STATUS.toString());
		cbStatus.setSelectedIndex(6);
		cbStatus.setRenderer(new SceneStateLCR());
		head.add(new JLabel(I18N.getColonMsg("status")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		head.add(cbStatus, MIG.WRAP);
		cbStage = new JComboBox();
		cbStage.setName("cbStage");
		CbUtil.fillCbStage(mainFrame, cbStage, "");
		head.add(new JLabel(I18N.getColonMsg("assistant.stage")), MIG.get(MIG.SPAN, "split 3"));
		head.add(cbStage, MIG.SPLIT2);
		head.add(Ui.initButton(CMD.BT_STAGE.toString(), "", ICONS.K.HELP, "assistant.stage.help", this));
		scenario = new ScenarioPanel(this);
		head.add(scenario, MIG.SPAN);
		return head;
	}

	private JPanel initLeft() {
		left = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.WRAP, MIG.INS1, MIG.GAP0), "[][]"));
		btHideLeft = Ui.initButton("btHide", "", ICONS.K.AR_LEFT, "persons", e -> leftShow());
		left.add(btHideLeft, MIG.get(MIG.TOP, MIG.RIGHT));
		leftPersons = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.HIDEMODE3)));
		listPersons = Ui.initCkList(leftPersons, mainFrame, Book.TYPE.PERSON,
				scene.getPersons(), null, BBORDER);
		left.add(leftPersons);
		return left;
	}

	private void leftShow() {
		leftPanelHide = !leftPanelHide;
		btHideLeft.setIcon((leftPanelHide
				? IconUtil.getIconSmall(ICONS.K.AR_RIGHT)
				: IconUtil.getIconSmall(ICONS.K.AR_LEFT)));
		leftPersons.setVisible(!leftPanelHide);
	}

	private JPanel initMiddle() {
		JPanel middle = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.INS1)));
		//the text editor
		mdEdit = new Markdown("ScenarioPanel", Html.TYPE, "");
		mdEdit.setCaretPosition(0);
		mdEdit.setCallback(this);
		middle.add(mdEdit, MIG.GROW);
		mdEdit.setView(Markdown.VIEW.TEXT);
		return middle;
	}

	private JPanel initRight() {
		//LOG.trace(TT+".initRght()");
		JPanel right = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP, MIG.HIDEMODE3)));
		taDescription = initTextArea(right, "description", "");
		taNotes = initTextArea(right, "notes", "");
		return right;
	}

	private JPanel initFooter() {
		//LOG.trace(TT+".initFooter()");
		return new JPanel(new MigLayout());
	}

	private boolean isSceneFirst() {
		return (scene == null);
	}

	private boolean isSceneLast() {
		return (scene == null);
	}

	public Markdown mdEditGet() {
		return mdEdit;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();
		String types = "scene, location, person, plot, photo";
		if (propName.contains("_")) {
			String m[] = propName.split("_");
			if (!types.contains(m[0])) {
				return;
			}
			switch (Book.getTYPE(m[0])) {
				case SCENE:
					scenesLoad();
					if (m[1].equalsIgnoreCase("new")) {
						cbScenes.setSelectedItem((Scene) evt.getNewValue());
					}
					break;
				case PERSON:
					personsLoad();
					break;
				default:
					break;
			}
		}
	}

	public void sceneRefresh(Scene scene) {
		if (scene != null) {
			this.scene = scene;
			sceneRefresh();
			scenario.setScene(scene);
		}
	}

	public void sceneRefresh() {
		resetListener();
		if (scene == null) {
			tfName.setText("");
			mdEdit.setText("");
			cbStage.setSelectedIndex(-1);
			cbStatus.setSelectedIndex(-1);
		} else {
			tfName.setText(scene.getName());
			if (scene.getSummary() != null) {
				if (scene.getSummary().contains("<p")) {
					mdEdit.setText(Markdown.toMarkdown(scene.getSummary()));
				} else {
					mdEdit.setText(scene.getSummary());
				}
			} else {
				mdEdit.setText("");
			}
			cbStage.setSelectedIndex(scene.getScenario_stage());
			cbStatus.setSelectedIndex(scene.getStatus());
			scenario.setScene(scene);
			mainFrame.lastSceneSet(scene);
			mdEdit.setHeader(scene, book.info.scenarioGet());
		}
		mdEdit.setCaretPosition(0);
		personsLoad();
		cbScenes.setSelectedItem(scene);
		enableNav(cbScenes.getSelectedIndex());
		resetModified();
		setListener();
	}

	private void setListener() {
		cbScenes.addActionListener(this);
		cbStage.addActionListener(this);
		cbStatus.addActionListener(this);
	}

	private void resetListener() {
		cbScenes.removeActionListener(this);
		cbStage.removeActionListener(this);
		cbStatus.removeActionListener(this);
	}

	public JTextField initTextField(String title, int len, String val) {
		JTextField tf = new JTextField();
		tf.setName(title);
		tf.setText(val);
		if (len > 0) {
			tf.setColumns(len);
		}
		return tf;
	}

	public JTextArea initTextArea(JPanel p, String title, String val) {
		p.add(new JLabel(I18N.getColonMsg(title)));
		JTextArea ta = new JTextArea();
		ta.setName(title);
		if (val != null) {
			ta.setText(val);
		}
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		JScrollPane scroller = new JScrollPane(ta);
		SwingUtil.setMaxPreferredSize(scroller);
		p.add(scroller);
		return ta;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public void actionPerformed(ActionEvent evt) {
		//LOG.trace(TT+".actionPerformed(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JButton) {
			JButton btn = (JButton) evt.getSource();
			switch (getCMD(btn.getName())) {
				case BT_HELP:
					JOptionPane.showMessageDialog(this, I18N.getMsg("markdown.help"));
					break;
				case BT_EXIT:
					if (isModified()) {
						save();
						mainFrame.fileSave(true);
					}
					SwingUtilities.invokeLater(() -> {
						mainFrame.close(true);
					});
					break;
				case BT_SCENE_FIRST:
					if (isModified()) {
						save();
					}
					sceneFirst();
					sceneRefresh();
					break;
				case BT_SCENE_LAST:
					if (isModified()) {
						save();
					}
					sceneLast();
					sceneRefresh();
					break;
				case BT_SCENE_PREVIOUS:
					if (isModified()) {
						save();
					}
					scenePrior();
					sceneRefresh();
					break;
				case BT_SCENE_NEXT:
					if (isModified()) {
						save();
					}
					sceneNext();
					sceneRefresh();
					break;
				case BT_SCENE_NEW:
					if (scene != null && checkModified() == JOptionPane.CANCEL_OPTION) {
						return;
					}
					sceneNew();
					break;
				case BT_IGNORE_CHANGE:
					modified = false;
					scene = (Scene) mainFrame.project.scenes.get(scene.getId());
					sceneRefresh();
					break;
				case BT_SCENE_SAVE:
					scene = scenarioGetData();
					save();
					break;
				case BT_SCREEN:
					if (modified) {
						scenarioGetData();
						save();
					}
					close();
					break;
				case BT_IDEA:
					mainFrame.showEditorAsDialog(new Idea());
					break;
				case BT_STAGE:
					Net.openBrowser(I18N.getMsg("assistant.stage.web"));
					break;
				case BT_ADD_LOCATION:
					Location entity = new Location();
					JDialog dlg = new JDialog(mainFrame.fullFrame, true);
					Editor editor = new Editor(mainFrame, entity, dlg);
					dlg.setTitle(I18N.getMsg("editor"));
					dlg.add(editor);
					Dimension pos = SwingUtil.getDlgPosition(entity);
					Dimension size = SwingUtil.getDlgSize(entity);
					dlg.setSize((size != null ? size.height : this.getWidth() / 2),
							(size != null ? size.width : 680));
					if (pos != null) {
						dlg.setLocation(pos.height, pos.width);
					} else {
						dlg.setLocationRelativeTo(this);
					}
					dlg.setVisible(true);
					SwingUtil.saveDlgPosition(dlg, entity);
					break;
				default:
					break;
			}
		} else if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			switch (getCMD(cb.getName())) {
				case CB_SCENES:
					checkModified();
					if (cbScenes.getSelectedIndex() != -1) {
						scene = (Scene) cbScenes.getSelectedItem();
					}
					sceneRefresh();
					enableNav(cbScenes.getSelectedIndex());
					break;
				case CB_LOCATIONS:
				case CB_LOC:
				case CB_MOMENT:
				case CB_IN:
				case CB_OUT:
					scenarioGetData();
					mdEdit.setHeader(scene, book.info.scenarioGet());
					modified = true;
					break;
				default:
					break;
			}
		} else if (evt.getSource() instanceof JRadioButton) {
			scenarioGetData();
			mdEdit.setHeader(scene, book.info.scenarioGet());
			modified = true;
		}
		if (modified) {
			this.btSave.setEnabled(true);
			this.btIgnore.setEnabled(true);
		}
	}

	public int askModified() {
		if (scene != null && mdEdit.isModified()) {
			if (mdEdit.getText().length() > 32767) {
				String str = I18N.getMsg("editor.text_too_large");
				JOptionPane.showMessageDialog(
						this,
						str,
						I18N.getMsg("editor"),
						JOptionPane.ERROR_MESSAGE);
				return (JOptionPane.CANCEL_OPTION);
			}
			modified = true;
			scene.setSummary(mdEdit.getText());
			mdEdit.resetModified();
		}
		if (origine != getHash()) {
			scenarioGetData();
			save();
		}
		return JOptionPane.YES_OPTION;
	}

	public Scene sceneGet() {
		return this.scene;
	}

	public void sceneClose() {
		// empty
	}

	private void close() {
		mainFrame.typistActivate();
	}

	private void enableNav(int n) {
		btFirst.setEnabled(n > 0);
		btPrevious.setEnabled(n > 0);
		btNext.setEnabled(n < cbScenes.getItemCount() - 1);
		btLast.setEnabled(n < cbScenes.getItemCount() - 1);
	}

	private Scene sceneNext() {
		int i = cbScenes.getSelectedIndex();
		if (i == cbScenes.getItemCount() - 1) {
			return scene;
		}
		scene = (Scene) cbScenes.getItemAt(i + 1);
		return scene;
	}

	private Scene scenePrior() {
		int i = cbScenes.getSelectedIndex();
		if (i < 1) {
			return scene;
		}
		scene = (Scene) cbScenes.getItemAt(i - 1);
		origine = getHash();
		return scene;
	}

	private Scene sceneFirst() {
		if (cbScenes.getSelectedIndex() == 0) {
			return scene;
		}
		scene = (Scene) cbScenes.getItemAt(0);
		origine = getHash();
		return scene;
	}

	private Scene sceneLast() {
		int i = cbScenes.getItemCount();
		if (cbScenes.getSelectedIndex() == cbScenes.getItemCount() - 1) {
			return scene;
		}
		if (i < 1) {
			return scene;
		}
		scene = (Scene) cbScenes.getItemAt(i - 1);
		origine = getHash();
		return scene;
	}

	public Scene scenarioGetData() {
		//LOG.trace(TT + ".scenarioGetData()");
		scene.setStatus(cbStatus.getSelectedIndex());
		scene.setPersons(personsGet());
		scene.setScenario_stage(cbStage.getSelectedIndex());
		scene.setSummary(mdEdit.getText());
		scene.setDescription(taDescription.getText());
		scene.setNotes(taNotes.getText());
		return scene;
	}

	/**
	 * get the selected Persons
	 *
	 * @return
	 */
	public List<Person> personsGet() {
		List<Person> ls = new ArrayList<>();
		listPersons.getSelectedEntities().forEach((e) -> {
			ls.add((Person) e);
		});
		return ls;
	}

	public int checkModified() {
		if (scene == null) {
			return (JOptionPane.CANCEL_OPTION);
		}
		if (mdEdit.getText().length() > 32767) {
			String str = I18N.getMsg("editor.text_too_large");
			JOptionPane.showMessageDialog(
					this,
					str,
					I18N.getMsg("editor"),
					JOptionPane.ERROR_MESSAGE);
			return (JOptionPane.CANCEL_OPTION);
		}
		if (modified) {
			scenario.getScenarioData(scene);
			save();
		}
		return (JOptionPane.YES_OPTION);
	}

	private void save() {
		scenario.getScenarioData(scene);
		scenarioGetData();
		if (scene.getId() < 0) {
			mainFrame.getBookController().newEntity(scene);
		} else {
			mainFrame.getBookController().updateEntity(scene);
		}
		mainFrame.setUpdated();
		resetModified();
	}

	@SuppressWarnings("unchecked")
	private void sceneNew() {
		Scene s = new Scene();
		if (scene != null) {
			s.setChapter(scene.getChapter());
			s.setStrand(scene.getStrand());
		}
		s.setSceneno(Book.getNbOf(mainFrame, Book.TYPE.SCENE) + 1);
		String title = I18N.getMsg("scene") + " " + s.getSceneno();
		s.setName(title);
		s.setTitle(title);
		mainFrame.getBookModel().ENTITY_New(s);
		scenesLoad();
		cbScenes.setSelectedIndex(cbScenes.getItemCount() - 1);
		sceneSet((Scene) cbScenes.getSelectedItem());
	}

	private void sceneSet(Scene s) {
		System.gc();
		cbScenes.setSelectedItem(s);
		scene = (Scene) cbScenes.getSelectedItem();
		origine = getHash();
		enableNav(cbScenes.getSelectedIndex());
	}

	private void personsLoad() {
		//LOG.trace(TT + ".personLoad()");
		leftPersons.removeAll();
		listPersons = Ui.initCkList(leftPersons, mainFrame, Book.TYPE.PERSON,
				scene.getPersons(), null, BBORDER);
		leftPersons.revalidate();
	}

	@SuppressWarnings("unchecked")
	private void scenesLoad() {
		cbScenes.removeActionListener(this);
		DefaultComboBoxModel combo = (DefaultComboBoxModel) cbScenes.getModel();
		combo.removeAllElements();
		List<Scene> scenes = Scenes.find(mainFrame);
		for (Scene entity : scenes) {
			combo.addElement(entity);
		}
		cbScenes.addActionListener(this);
	}

	public void setModified() {
		modified = true;
		enableSave();
	}

	public void resetModified() {
		modified = false;
		origine = getHash();
		enableSave();
	}

	private void enableSave() {
		btSave.setEnabled(modified);
		btIgnore.setEnabled(modified);
	}

	@SuppressWarnings("unchecked")
	public JComboBox initCbScenario(CMD action) {
		//LOG.trace(TT+".initCbScenario("+action.toString()+")");
		String type = action.toString().toLowerCase().replaceFirst("cb_", "");
		JComboBox cb = new JComboBox();
		cb.setName(action.toString());
		for (int i = 0;; i++) {
			String s = scenarioFindKey(type, i);
			if (s.isEmpty()) {
				break;
			}
			cb.addItem(s);
		}
		return (cb);
	}

	public String scenarioFindKey(String type, int i) {
		//LOG.trace(TT+".scenarioFindKey(type=" + type + ", i=" + i + ")");
		String key = "scenario." + type + "." + String.format("%02d", i);
		try {
			String msg = I18N.getMsg(key);
			if (msg.equals("!" + key + "!")) {
				return ("");
			}
			return (msg);
		} catch (Exception ex) {
			return ("");
		}
	}

	private boolean isModified() {
		return (getHash() != origine);
	}

	private int getHash() {
		StringBuilder b = new StringBuilder();
		b.append(getHash(cbStage));
		b.append(getHash(cbStatus));
		b.append(ListUtil.join(personsGet()));
		b.append(taDescription.getText());
		b.append(taNotes.getText());
		b.append(tfName.getText());
		b.append(mdEdit.getText());
		b.append(scenario.getHash());
		return b.toString().hashCode();
	}

	private String getHash(JComboBox cb) {
		if (cb == null) {
			return "";
		}
		return (cb.getSelectedIndex() != -1 ? cb.getSelectedItem().toString() : "");
	}
}
