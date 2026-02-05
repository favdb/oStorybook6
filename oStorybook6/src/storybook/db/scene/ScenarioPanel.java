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
package storybook.db.scene;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import resources.icons.ICONS;
import storybook.App;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.location.Location;
import storybook.edit.Editor;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.typist.TypistScenario;

/**
 *
 * @author favdb
 */
public class ScenarioPanel extends AbstractPanel {

	private static final String TT = "ScenarioPanel.";

	private JComboBox cbMoment,
			cbLocation,
			cbStart,
			cbEnd;
	private JTextField tfPitch;
	private JRadioButton rbLocInt, rbLocExt;
	private JButton btLoc;
	private TypistScenario scenarioPanel;
	private SceneEdit scenePanel;
	private Scene scene;
	private int origine;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ScenarioPanel(TypistScenario scenarioPanel) {
		super(scenarioPanel.mainFrame);
		this.scenarioPanel = scenarioPanel;
		scene = scenarioPanel.sceneGet();
		initAll();
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ScenarioPanel(SceneEdit scenePanel) {
		super(scenePanel.mainFrame);
		this.scenePanel = scenePanel;
		scene = scenePanel.getScene();
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		this.setLayout(new MigLayout(MIG.HIDEMODE3));
		// pitch
		add(new JLabel(I18N.getColonMsg("scenario.pitch")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		tfPitch = new JTextField();
		tfPitch.setName("tfPitch");
		tfPitch.setFont(App.fonts.editorGet());
		add(tfPitch, MIG.GROWX);
		// liste des moments
		add(new JLabel(I18N.getColonMsg("scenario.moment")), MIG.RIGHT);
		cbMoment = initCbScenario("moment");
		add(cbMoment);
		// radio des INT/EXT
		add(new JLabel(I18N.getColonMsg("location")));
		rbLocInt = new JRadioButton(I18N.getMsg("scenario.loc.int"));
		rbLocInt.setName("rbInt");
		add(rbLocInt);
		rbLocExt = new JRadioButton(I18N.getMsg("scenario.loc.ext"));
		rbLocExt.setName("rbExt");
		ButtonGroup group = new ButtonGroup();
		group.add(rbLocInt);
		group.add(rbLocExt);
		add(rbLocExt);
		// liste des lieux
		cbLocation = new JComboBox();
		cbLocation.setName("cbLocation");
		Location location = null;
		if (scene.getLocations() != null && !scene.getLocations().isEmpty()) {
			location = scene.getLocations().get(0);
		}
		EntityUtil.cbFill(mainFrame, cbLocation, Book.TYPE.LOCATION, location, false, false);
		add(cbLocation);
		btLoc = Ui.initButton("btLoc", "", ICONS.K.NEW, "location.add", this);
		add(btLoc, MIG.WRAP);
		// liste de la transition du début
		add(new JLabel(I18N.getColonMsg("scenario.start")));
		cbStart = initCbScenario("in");
		add(cbStart);
		// liste de la transition de fin
		add(new JLabel(I18N.getColonMsg("scenario.end")),
				MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		cbEnd = initCbScenario("out");
		add(cbEnd);
		origine = getHash();
	}

	@SuppressWarnings("unchecked")
	public JComboBox initCbScenario(String name) {
		//App.trace("ScenarioEdit.initCbScenario("+action.toString()+")");
		JComboBox cb = new JComboBox();
		cb.setName(name);
		for (int i = 0;; i++) {
			String s = Scene.getScenarioKey(name, i);
			if (s.isEmpty()) {
				break;
			}
			cb.addItem(s);
		}
		return (cb);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		boolean toRefresh = false;
		if (evt.getSource() instanceof JButton) {
			JButton bt = (JButton) evt.getSource();
			switch (bt.getName()) {
				case "btLoc":
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
					if (!editor.canceled) {
						EntityUtil.cbFill(mainFrame, cbLocation, Book.TYPE.LOCATION, null, false, false);
						cbLocation.setSelectedItem(cbLocation.getItemCount() - 1);
						mainFrame.setUpdated();
					}
					break;
				default:
					break;
			}
		} else if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			switch (cb.getName()) {
				case "cbLocations":
				case "cbLoc":
				case "cbMoment":
				case "cbIn":
				case "cbOut":
				default:
					toRefresh = true;
					break;
			}
		} else if (evt.getSource() instanceof JRadioButton) {
			toRefresh = true;
		}
		if (toRefresh) {
			if (scenarioPanel != null) {
				Scene scene = scenarioPanel.scenarioGetData();
				scenarioPanel.mdEditGet().setHeader(scene, book.info.scenarioGet());
				scenarioPanel.setModified();
			} else if (scenePanel != null) {
				getScenarioData((Scene) scenePanel.entity);
			}
		}
	}

	public String getPitch() {
		return tfPitch.getText();
	}

	public void getScenarioData(Scene scene) {
		//LOG.trace(TT + ".scenarioGetData(scene=" + LOG.trace(scene) + ")");
		List<Location> locs = new ArrayList<>();
		if (cbLocation.getSelectedIndex() != -1) {
			locs.add((Location) cbLocation.getSelectedItem());
			scene.setLocations(locs);
		}
		scene.setScenario_pitch(tfPitch.getText());
		scene.setScenario_moment(cbMoment.getSelectedIndex());
		scene.setScenario_loc((rbLocInt.isSelected() ? 1 : 2));
		scene.setScenario_start(cbStart.getSelectedIndex());
		scene.setScenario_end(cbEnd.getSelectedIndex());
		if (scenePanel != null && scenePanel.getMarkdown() != null) {
			scenePanel.getMarkdown().setHeader(scene, book.info.scenarioGet());
		}
		if (scenePanel != null) {
			scenePanel.setModified();
		}
		if (scenarioPanel != null) {
			scenarioPanel.setModified();
		}
	}

	public boolean isModified() {
		return (origine != getHash());
	}

	public void setScene(Scene scene) {
		this.scene = scene;
		tfPitch.setText(scene.getScenario_pitch());
		cbMoment.setSelectedIndex(scene.getScenario_moment());
		cbStart.setSelectedIndex(scene.getScenario_start());
		cbEnd.setSelectedIndex(scene.getScenario_end());
		rbLocInt.setSelected(scene.getScenario_loc() == 1);
		rbLocExt.setSelected(scene.getScenario_loc() == 2);
		if (!scene.getLocations().isEmpty()) {
			cbLocation.setSelectedItem(scene.getLocations().get(0));
		} else {
			cbLocation.setSelectedIndex(-1);
		}
		origine = getHash();
	}

	public int getHash() {
		StringBuilder b = new StringBuilder();
		b.append(tfPitch.getText());
		b.append(cbMoment.getSelectedItem().toString());
		b.append(cbStart.getSelectedItem().toString());
		b.append(cbEnd.getSelectedItem().toString());
		b.append((rbLocInt.isSelected() ? "INT" : "EXT"));
		if (cbLocation.getSelectedIndex() != -1) {
			b.append(cbLocation.getSelectedItem().toString());
		}
		return b.toString().hashCode();
	}

}
