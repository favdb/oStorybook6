/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.tools.swing.js.JSPanelMemory;
import storybook.ui.panel.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StatusbarPanel extends AbstractPanel implements ActionListener {

	private JTextField lbChapters,
			lbScenes,
			lbPersons,
			lbLocations,
			lbItems,
			lbWords;
	private JSPanelMemory memPanel;
	private JPanel pPersons;
	private JPanel pLocations;
	private JPanel pItems;

	//JComboBox layoutCombo = new JComboBox();
	public StatusbarPanel(MainFrame mainFrame) {
		//LOG.trace(TT+"(mainFrame)");
		this.mainFrame = mainFrame;
		setName(this.getClass().getSimpleName());
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT+"modelPropertyChange("+evt.toString()+")");
		String propName = evt.getPropertyName();

		if (PROPS.REFRESH.check(propName)) {
			//refresh();
			refreshStat();
			return;
		}
		ActKey act = new ActKey(propName);
		switch (Book.getTYPE(act.type)) {
			case PART:
			case PARTS:
			case CHAPTER:
			case CHAPTERS:
			case SCENE:
			case SCENES:
			case PERSON:
			case PERSONS:
				refreshStat();
				break;
		}
	}

	@Override
	public void init() {
		//LOG.trace(TT+".init()");
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.FLOWX, MIG.FILLX)));
		if (App.preferences.getBoolean(Pref.KEY.LAF_COLORED)) {
			setBackground(Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR)));
		}

		add(initStat());
		memPanel = new JSPanelMemory();
		add(memPanel, MIG.RIGHT);

		refreshStat();
		revalidate();
		repaint();
	}

	private JTextField initTf(int size) {
		JTextField tf = new JTextField();
		tf.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		tf.setBackground(this.getBackground());
		Color c = Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR));
		tf.setEditable(false);
		tf.setHorizontalAlignment(JTextField.CENTER);
		if (size > 0) {
			tf.setColumns(size);
		}
		return (tf);
	}

	private JPanel initStat() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.HIDEMODE3)));
		p.setBackground(this.getBackground());
		lbChapters = initTf(3);
		p.add(initStatField("chapters", lbChapters));
		lbScenes = initTf(3);
		p.add(initStatField("scenes", lbScenes));
		pPersons = new JPanel(new MigLayout(MIG.INS0));
		lbPersons = initTf(3);
		pPersons.add(initStatField("persons", lbPersons));
		p.add(pPersons);
		pLocations = new JPanel(new MigLayout(MIG.INS0));
		lbLocations = initTf(3);
		pLocations.add(initStatField("locations", lbLocations));
		p.add(pLocations);
		pItems = new JPanel(new MigLayout(MIG.INS0));
		lbItems = initTf(3);
		pItems.add(initStatField("items", lbItems));
		p.add(pItems);
		lbWords = initTf(0);
		p.add(initStatField("words", lbWords));
		return (p);
	}

	private JPanel initStatField(String title, JTextField lb) {
		JPanel p = new JPanel(new MigLayout(MIG.get("ins 0")));
		p.setBackground(this.getBackground());
		p.add(new JLabel(I18N.getMsg(title)));
		p.add(lb);
		return p;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT+".actionPerformed("+e.paramString()+")");
		refreshStat();
	}

	public void refreshStat() {
		//LOG.trace(TT+".refreshStat()");
		lbChapters.setText(Book.getNbOf(mainFrame, Book.TYPE.CHAPTER) + "");
		lbScenes.setText(Book.getNbOf(mainFrame, Book.TYPE.SCENE) + "");
		lbPersons.setText(Book.getNbOf(mainFrame, Book.TYPE.PERSON) + "");
		lbLocations.setText(Book.getNbOf(mainFrame, Book.TYPE.LOCATION) + "");
		lbItems.setText(Book.getNbOf(mainFrame, Book.TYPE.ITEM) + "");
		String strStat = String.format(" %,d (%,d) ",
				BookUtil.getNbWords(mainFrame.project),
				BookUtil.getNbChars(mainFrame.project));
		lbWords.setText(strStat);
		memPanel.repaint();
		memPanel.setVisible(App.preferences.getBoolean(Pref.KEY.MEMORY) || App.isDev());
		pPersons.setVisible(Book.getNbOf(mainFrame, Book.TYPE.PERSON) > 0);
		pLocations.setVisible(Book.getNbOf(mainFrame, Book.TYPE.LOCATION) > 0);
		pItems.setVisible(Book.getNbOf(mainFrame, Book.TYPE.ITEM) > 0);
		revalidate();
		repaint();
	}
}
