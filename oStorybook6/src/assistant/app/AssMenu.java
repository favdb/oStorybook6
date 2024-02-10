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
package assistant.app;

import assistant.Assistant;
import assistant.Assistant.SECTION;
import i18n.I18N;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.w3c.dom.NodeList;

/**
 *
 * @author favdb
 */
public class AssMenu extends JMenuBar {

	private static final String TT = "AssistantMenu";

	private final AppAssistant app;
	private JMenuItem fileMenuSave;
	private JMenuItem fileMenuSaveAs;

	public AssMenu(AppAssistant app) {
		super();
		this.app = app;
		initialize();
	}

	private void initialize() {
		AssAction appAction = app.getAppAction();
		JMenu fileMenu = addMenu(I18N.getMsg("file"));
		//fileMenu.add(addMenuItem(I18N.getMsg("file.new"), e -> appAction.fileNew()));
		fileMenu.add(addMenuItem(I18N.getMsg("file.open"), e -> appAction.fileOpen()));
		fileMenuSave = addMenuItem(I18N.getMsg("file.save"), e -> appAction.fileSave());
		fileMenu.add(fileMenuSave);
		fileMenuSaveAs = addMenuItem(I18N.getMsg("file.save.as"), e -> appAction.fileSaveAs());
		fileMenu.add(fileMenuSaveAs);
		fileMenu.add(addMenuItem(I18N.getMsg("exit"), e -> app.close()));
		add(fileMenu);

		String tmenu = "test";
		String exLocation = "<location><Saison>Automne</Saison></location>";
		String exPerson = "<person><Physique><Bouche>Noir</Bouche><Cheveux>Auburn</Cheveux></Physique></person>";
		String exItem = "<item><Forme>Carré</Forme><Matière>bois</Matière></item>";
		String exScene = "<book><scenetext><Situation>Vengeance</Situation></scenetext></book>";
		String exPlot = "";

		JMenu testMenu = addMenu(I18N.getMsg(tmenu));
		if (Assistant.getXml().getSection(SECTION.BOOK) != null) {
			testMenu.add(addMenuItem(
			   I18N.getMsg(tmenu + "." + SECTION.BOOK.toString()),
			   e -> appAction.testDialog(SECTION.BOOK, exScene)));
		}
		NodeList nodes = Assistant.getXml().getSection(SECTION.PERSON);
		if (nodes != null && nodes.getLength() > 0) {
			testMenu.add(addMenuItem(
			   I18N.getMsg(tmenu + "." + SECTION.PERSON.toString()),
			   e -> appAction.testDialog(SECTION.PERSON, exPerson)));
		}
		nodes = Assistant.getXml().getSection(SECTION.LOCATION);
		if (nodes != null && nodes.getLength() > 0) {
			testMenu.add(addMenuItem(
			   I18N.getMsg(tmenu + "." + SECTION.LOCATION.toString()),
			   e -> appAction.testDialog(SECTION.LOCATION, exLocation)));
		}
		nodes = Assistant.getXml().getSection(SECTION.ITEM);
		if (nodes != null && nodes.getLength() > 0) {
			testMenu.add(addMenuItem(
			   I18N.getMsg(tmenu + "." + SECTION.ITEM.toString()),
			   e -> appAction.testDialog(SECTION.ITEM, exItem)));
		}
		nodes = Assistant.getXml().getSection(SECTION.PLOT);
		if (nodes != null && nodes.getLength() > 0) {
			testMenu.add(addMenuItem(
			   I18N.getMsg(tmenu + "." + SECTION.PLOT.toString()),
			   e -> appAction.testDialog(SECTION.PLOT, exPlot)));
		}
		add(testMenu);

		JMenu helpMenu = addMenu(I18N.getMsg("help"));
		//helpMenu.add(addMenuItem(I18N.getMsg("help.contents"), e -> appAction.helpContents()));
		helpMenu.add(addMenuItem(I18N.getMsg("help.about"), e -> appAction.helpAbout()));
		add(helpMenu);
	}

	public void setSaveEnable(boolean b) {
		//LOG.trace(TT + ".setSaveEnable(b=" + (b ? "true" : "false") + ")");
		fileMenuSave.setEnabled(false);
		if (Assistant.getXml().getSave() != null) {
			fileMenuSave.setEnabled(app.isModified());
		}
		fileMenuSaveAs.setEnabled(app.isModified());
	}

	/**
	 * get a JMenu
	 *
	 * @param str
	 * @return
	 */
	public static JMenu addMenu(String str) {
		String t[] = str.split(";");
		JMenu menu = new JMenu(t[0]);
		if (t.length > 1) {
			menu.setMnemonic(t[1].charAt(0));
		}
		return menu;
	}

	/**
	 * get a JMenuItem
	 *
	 * @param text format may be text;mnemonic;tooltip;accel
	 * @param act optional action
	 * @return
	 */
	public static JMenuItem addMenuItem(String text, ActionListener... act) {
		String s[] = text.split(";");
		int accel = 0;
		int mask = 0;
		if (s.length > 3 && !s[3].isEmpty()) {
			if (s[3].startsWith("Ctrl")) {
				mask = InputEvent.CTRL_DOWN_MASK;
			}
			if (s[3].startsWith("Alt")) {
				mask = InputEvent.ALT_DOWN_MASK;
			}
			if (s[3].startsWith("Ctrl+Alt")) {
				mask = InputEvent.CTRL_DOWN_MASK & InputEvent.ALT_DOWN_MASK;
			}
			accel = s[3].charAt(s[3].length() - 1);
		}
		char mnem = ' ';
		if (s.length > 1) {
			mnem = s[1].charAt(0);
		}
		String tip = "";
		if (s.length > 2) {
			tip = s[2];
		}
		return initMenuItem(s[0], mnem, tip, accel, mask, act);
	}

	public static JMenuItem initMenuItem(String text, char mnemonic, String tip,
	   int accel, int mask, ActionListener... action) {
		JMenuItem mi = new JMenuItem(text.trim());
		if (mnemonic != ' ') {
			mi.setMnemonic(mnemonic);
		}
		if (!tip.isEmpty()) {
			mi.setToolTipText(tip.trim());
		}
		if (accel > 0) {
			mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(accel, mask));
		}
		if (action != null) {
			mi.addActionListener(action[0]);
		}
		return mi;
	}

}
