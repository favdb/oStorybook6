/*
 * Copyright (C) 2020 favdb
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import resources.icons.ICONS;
import storybook.App;
import storybook.Pref;
import storybook.db.book.BookParamLayout;
import storybook.db.DB;
import storybook.dialog.preferences.PreferencesDlg;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class PropBookLayoutPanel extends JPanel implements ChangeListener, ActionListener {

	private static final String TT = "BookLayoutPanel";

	private JCheckBox ckPartTitles;
	private JCheckBox ckChapterNumbers;
	private JCheckBox ckChapterRoman;
	private JCheckBox ckChapterTitles;
	private JCheckBox ckChapterDatesLocations;
	private JCheckBox ckChapterDescription;
	private JCheckBox ckSceneTitles;
	private JCheckBox ckSceneDidascalie;
	private JCheckBox ckSceneSeparator;
	private JTextField tfSceneSeparator;
	private String separator = "";
	private final BookParamLayout bookLayout;
	private boolean withPref;
	private JCheckBox ckShowReview;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PropBookLayoutPanel(PreferencesDlg parent, boolean withPref) {
		this.bookLayout = new BookParamLayout(App.preferences.getString(Pref.KEY.BOOK_LAYOUT));
		this.withPref = withPref;
		separator = App.preferences.getString(Pref.KEY.SCENE_SEPARATOR);
		initialize();
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PropBookLayoutPanel(BookParamLayout layout, boolean withPref) {
		this.bookLayout = layout;
		this.withPref = withPref;
		separator = layout.getSceneSeparatorValue();
		initialize();
	}

	public void initialize() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, "ins 1", MIG.WRAP), "[][]"));
		//setBorder(BorderFactory.createTitledBorder(I18N.getMsg("book.layout")));
		ckPartTitles = addCk("ckPartTitles", "part_titles", bookLayout.getPartTitle());
		add(ckPartTitles, MIG.SPAN);
		JPanel c = new JPanel(new MigLayout(MIG.get("ins 1", MIG.WRAP), "[][]"));
		c.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("chapters")));
		ckChapterTitles = addCk("ckChapterTitles", "chapter_titles", bookLayout.getChapterTitle());
		c.add(ckChapterTitles, MIG.SKIP);
		ckChapterNumbers = addCk("ckChapterNumbers", "chapter_numbers",
		   bookLayout.getChapterNumber());
		c.add(ckChapterNumbers, MIG.get(MIG.SKIP, MIG.SPLIT2));
		ckChapterNumbers.addChangeListener(this);
		ckChapterRoman = addCk("ckRoman", "chapter_roman", bookLayout.getChapterRoman());
		c.add(ckChapterRoman);
		ckChapterDescription = addCk("ckChapterDescription", "chapter_description",
		   bookLayout.getChapterDescription());
		c.add(ckChapterDescription, MIG.SKIP);
		ckChapterDatesLocations = addCk("ckChapterDatesLocations", "chapter_dateslocations",
		   bookLayout.getChapterDateLocation());
		c.add(ckChapterDatesLocations, MIG.SKIP);
		add(c, MIG.get(MIG.SPAN, "sgx book"));

		c = new JPanel(new MigLayout(MIG.get("ins 1", MIG.WRAP, MIG.HIDEMODE3), "[][]"));
		c.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("scenes")));
		ckSceneTitles = addCk("ckSceneTitles", "scene_titles", bookLayout.getSceneTitle());
		ckSceneTitles.addChangeListener(this);
		c.add(ckSceneTitles, MIG.SKIP);
		ckSceneDidascalie = addCk("ckSceneDidascalie", "scene_didascalie",
		   bookLayout.getSceneDidascalie());
		c.add(ckSceneDidascalie, MIG.SKIP);
		ckSceneSeparator = addCk("ckSceneSeparator", "scene_separator",
		   bookLayout.getSceneSeparator());
		ckSceneSeparator.addChangeListener(this);
		c.add(ckSceneSeparator, MIG.get(MIG.SKIP, MIG.SPAN, MIG.SPLIT2));
		tfSceneSeparator = Ui.getStringField(DB.DATA.SCENE_SEPARATOR, 16, separator, BNONE);
		tfSceneSeparator.setEnabled(ckSceneSeparator.isSelected());
		c.add(tfSceneSeparator, MIG.get(MIG.SPAN, MIG.GROW));
		add(c, MIG.get(MIG.SPAN, "sgx book"));
		if (withPref) {
			JButton btPref = Ui.initButton("btPref", "book.layout.copy",
			   ICONS.K.PREFERENCES, "", this);
			add(btPref, MIG.get(MIG.SPAN, MIG.RIGHT));
		}
		ckShowReview = addCk("ckHideReview", "show_review",
		   bookLayout.getShowReview());
		add(ckShowReview, MIG.SPAN);
		boolean b = ckChapterTitles.isSelected() || ckChapterNumbers.isSelected();
		ckChapterRoman.setEnabled(ckChapterNumbers.isSelected());
		ckChapterDatesLocations.setEnabled(b);
		ckChapterDescription.setEnabled(b);
		ckSceneDidascalie.setEnabled(b);
	}

	public void apply() {
		//LOG.trace(TT + ".apply()");
		bookLayout.setPartTitle(ckPartTitles.isSelected());
		bookLayout.setChapterTitle(ckChapterTitles.isSelected());
		bookLayout.setChapterNumber(ckChapterNumbers.isSelected());
		bookLayout.setChapterRoman(ckChapterRoman.isSelected());
		bookLayout.setChapterDescription(ckChapterDescription.isSelected());
		bookLayout.setChapterDateLocation(ckChapterDatesLocations.isSelected());
		bookLayout.setSceneTitle(ckSceneTitles.isSelected());
		bookLayout.setSceneDidascalie(ckSceneDidascalie.isSelected());
		bookLayout.setSceneSeparator(ckSceneSeparator.isSelected());
		bookLayout.setSceneSeparatorValue(tfSceneSeparator.getText());
		bookLayout.setShowReview(ckShowReview.isSelected());
	}

	public String getBookLayout() {
		return (bookLayout.getBookLayout());
	}

	public String getSeparator() {
		if (tfSceneSeparator != null) {
			return tfSceneSeparator.getText();
		}
		return "";
	}

	private JCheckBox addCk(String cbName, String title, boolean value) {
		JCheckBox cb = new JCheckBox(I18N.getMsg("book.layout." + title));
		cb.setSelected(value);
		cb.setName(cbName);
		return (cb);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox c = (JCheckBox) e.getSource();
			if (c.getName().equals("ckChapterNumbers")) {
				ckChapterRoman.setEnabled(ckChapterNumbers.isSelected());
			}
			if (c.getName().equals("ckChapterTitles")) {
				ckChapterNumbers.setEnabled(ckChapterTitles.isSelected());
				ckChapterRoman.setEnabled(ckChapterTitles.isSelected());
				ckChapterDatesLocations.setEnabled(ckChapterTitles.isSelected());
				ckChapterDescription.setEnabled(ckChapterTitles.isSelected());
			}
			if (c.getName().equals("ckSceneTitles")) {
				ckSceneDidascalie.setEnabled(ckSceneTitles.isSelected());
			}
			if (c.getName().equals("ckSceneSeparator")) {
				tfSceneSeparator.setEnabled(c.isSelected());
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
