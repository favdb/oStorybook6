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
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import static storybook.ui.Ui.MINIMUM_SIZE;

/**
 *
 * @author favdb
 */
public class ReplaceDlg extends AbsDialog {

	private String words;
	private final JPanel resultat = new JPanel();
	private JTextField txWords;
	List<JCheckBox> cbList;
	String[] objects = {"strand", "part", "chapter", "scene", "person", "plot", "location", "item", "tag", "idea", "memo"};
	private JButton btAll;
	private JTextField txByWords;
	private String bywords;
	private JButton btReplace;

	public static void show(MainFrame m) {
		SwingUtil.showModalDialog(new ReplaceDlg(m), m, true);
	}

	public ReplaceDlg(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		JLabel lb1 = new JLabel(I18N.getMsg("search.words"));
		txWords = new JTextField(32);
		txWords.addCaretListener((CaretEvent evt) -> {
			checkIfOk();
		});
		JLabel lb2 = new JLabel(I18N.getMsg("replace.bywords"));
		txByWords = new JTextField(32);
		txByWords.addCaretListener((CaretEvent evt) -> {
			checkIfOk();
		});
		btAll = new JButton(I18N.getMsg("all"));
		btAll.addActionListener((ActionEvent evt) -> {
			selectAll();
		});
		JPanel jPanel1 = new JPanel(new MigLayout("wrap 4"));
		jPanel1.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("search.for")));
		cbList = new ArrayList<>();
		for (String str : objects) {
			JCheckBox cb = new JCheckBox(I18N.getMsg(str));
			cb.setName(str);
			cb.addActionListener((ActionEvent evt) -> {
				checkIfOk();
			});
			jPanel1.add(cb);
			cbList.add(cb);
		}
		jPanel1.add(new JLabel(""), MIG.WRAP);
		jPanel1.add(btAll, MIG.get(MIG.SPAN, MIG.RIGHT));

		btReplace = new JButton(I18N.getMsg("find"));
		btReplace.setIcon(IconUtil.getIconSmall(ICONS.K.SEARCH));
		btReplace.setEnabled(false);
		btReplace.addActionListener((ActionEvent evt) -> {
			searchEntities();
		});

		//layout
		setLayout(new MigLayout());
		setTitle(I18N.getMsg("search"));
		add(lb1, MIG.WRAP);
		add(txWords, MIG.get(MIG.CENTER, MIG.WRAP));
		add(lb2, MIG.WRAP);
		add(txByWords, MIG.get(MIG.CENTER, MIG.WRAP));
		add(jPanel1, MIG.get(MIG.SPAN, MIG.WRAP));
		add(getCancelButton(), MIG.get(MIG.SG, MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(btReplace, MIG.SG);
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	private void checkIfOk() {
		boolean b = !(txWords.getText().isEmpty() && txByWords.getText().isEmpty());
		boolean bb = false;
		for (JCheckBox cb : cbList) {
			if (cb.isSelected()) {
				bb = true;
				break;
			}
		}
		if (bb == false) {
			b = false;
		}
		btReplace.setEnabled(b);
	}

	private void selectAll() {
		boolean x;
		if (btAll.getText().equals(I18N.getMsg("all"))) {
			btAll.setText(I18N.getMsg("none"));
			x = true;
		} else {
			btAll.setText(I18N.getMsg("all"));
			x = false;
		}
		for (JCheckBox cb : cbList) {
			cb.setSelected(x);
		}
		checkIfOk();
	}

	private void searchEntities() {
		String s = Html.htmlToText(txWords.getText());
		if (s.isEmpty()) {
			return;
		}
		words = s;
		initResultat();
		int x = 0;
		for (JCheckBox cb : cbList) {
			if (cb.isSelected()) {
				int y = 0;
				switch (Book.getTYPE(cb.getName())) {
					case STRAND:
						y += findStrands();
						break;
					case PART:
						y += findParts();
						break;
					case CHAPTER:
						y += findChapters();
						break;
					case SCENE:
						y += findScenes();
						break;
					case PERSON:
						y += findPersons();
						break;
					case PLOT:
						y += findPlots();
						break;
					case LOCATION:
						y += findLocations();
						break;
					case ITEM:
						y += findItems();
						break;
					case TAG:
						y += findTags();
						break;
					case IDEA:
						y += findIdeas();
						break;
					case MEMO:
						y += findMemos();
						break;
					default:
						y = -1;
						break;
				}
				if (y == -1) {
					x = y;
					break;
				}
				if (y > 0) {
					x += y;
				}
			}
		}
		if (x < 0) {
			SwingUtil.showError("search.error.object");
			return;
		}
		if (x > 0) {
			JButton bt = new JButton(I18N.getMsg("replace.all"));
			bt.addActionListener((ActionEvent evt) -> {
				replaceAll("all");
			});
			resultat.add(bt, MIG.SPAN);
		}
		showResults(resultat);
	}

	private int findStrands() {
		@SuppressWarnings("unchecked")
		List<Strand> entities = (List) mainFrame.project.strands;
		int finds = 0;
		for (Strand entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("strand", finds);
		for (Strand entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("strand", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findParts() {
		@SuppressWarnings("unchecked")
		List<Part> entities = (List) mainFrame.project.getList(Book.TYPE.PART);
		int finds = 0;
		for (Part entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("part", finds);
		for (Part entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("prat", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findChapters() {
		@SuppressWarnings("unchecked")
		List<Chapter> entities = (List) mainFrame.project.getList(Book.TYPE.CHAPTER);
		int finds = 0;
		for (Chapter entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("chapter", finds);
		for (Chapter entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("chapter", entity.getChapternoStr() + " " + entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findScenes() {
		@SuppressWarnings("unchecked")
		List<Scene> entities = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		int finds = 0;
		for (Scene entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))
					|| searchWordsHtml(entity.getSummary())) {
				finds++;
			}
		}
		doTitle("scene", finds);
		for (Scene entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))
					|| searchWordsHtml(entity.getSummary())) {
				if (finds == 0) {
					doNext();
				}
				doEntity("scene", entity.getFullTitle(), entity);
			}
		}
		return (finds);
	}

	private int findPersons() {
		@SuppressWarnings("unchecked")
		List<Person> entities = (List) mainFrame.project.getList(Book.TYPE.PERSON);
		int finds = 0;
		for (Person entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("person", finds);
		for (Person entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("person", entity.getFullNameAbbr(), entity);
			}
		}
		return (finds);
	}

	private int findLocations() {
		@SuppressWarnings("unchecked")
		List<Location> entities = (List) mainFrame.project.getList(Book.TYPE.LOCATION);
		int finds = 0;
		for (Location entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("location", finds);
		for (Location entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("location", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findItems() {
		@SuppressWarnings("unchecked")
		List<Item> entities = (List) mainFrame.project.getList(Book.TYPE.ITEM);
		int finds = 0;
		for (Item entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("item", finds);
		for (Item entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("item", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findPlots() {
		@SuppressWarnings("unchecked")
		List<Plot> entities = (List) mainFrame.project.getList(Book.TYPE.PLOT);
		int finds = 0;
		for (Plot entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("plot", finds);
		for (Plot entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("plot", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findTags() {
		@SuppressWarnings("unchecked")
		List<Tag> entities = (List) mainFrame.project.getList(Book.TYPE.TAG);
		int finds = 0;
		for (Tag entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("tag", finds);
		for (Tag entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("tag", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private int findIdeas() {
		@SuppressWarnings("unchecked")
		List<Idea> entities = (List) mainFrame.project.getList(Book.TYPE.IDEA);
		int finds = 0;
		for (Idea entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("idea", finds);
		for (Idea entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("idea", entity.getId() + " " + TextUtil.ellipsize(entity.getNotes(), 30), entity);
			}
		}
		return (finds);
	}

	private int findMemos() {
		@SuppressWarnings("unchecked")
		List<Memo> entities = (List) mainFrame.project.getList(Book.TYPE.MEMO);
		int finds = 0;
		for (Memo entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				finds++;
			}
		}
		doTitle("memo", finds);
		for (Memo entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (finds == 0) {
					doNext();
				}
				doEntity("memo", entity.getName(), entity);
			}
		}
		return (finds);
	}

	private boolean searchWords(String str) {
		String r = Html.htmlToText(str);
		if (r.toLowerCase(Locale.getDefault()).contains(words.toLowerCase(Locale.getDefault()))) {
			return (true);
		}
		return (false);
	}

	private boolean searchWordsHtml(String str) {
		String r = str;
		if (r.contains(words)) {
			return (true);
		}
		return (false);
	}

	private void showResults(JPanel res) {
		ReplaceResultsDlg dlg = new ReplaceResultsDlg(mainFrame, res, words, bywords);
		dlg.setVisible(true);
	}

	private void initResultat() {
		resultat.setLayout(new MigLayout(MIG.get(MIG.TOP, MIG.WRAP), "[][]"));
		resultat.setBackground(Color.white);
		resultat.setMinimumSize(MINIMUM_SIZE);
		resultat.removeAll();
	}

	private void doTitle(String msg, int finds) {
		JLabel lb = new JLabel(I18N.getMsg(msg));
		lb.setFont(FontUtil.getBold());
		resultat.add(lb);
		if (finds == 0) {
			doEmpty();
		} else {
			JButton bt = new JButton(I18N.getMsg("replace.all") + " " + I18N.getMsg(msg));
			bt.setName("btReplaceAll_" + msg);
			bt.addActionListener((ActionEvent evt) -> {
				replaceAll(msg);
			});
			resultat.add(bt, MIG.SPAN);
		}
	}

	private void doEmpty() {
		JLabel r = new JLabel(I18N.getMsg("search.empty"));
		resultat.add(r, MIG.WRAP);
	}

	private void doEntity(String nature, String str, AbstractEntity entity) {
		resultat.add(new JLabel(" "), MIG.RIGHT);
		JLabel r = new JLabel(str);
		JLabel doOk = new JLabel(IconUtil.getIconSmall(ICONS.K.OK));
		doOk.setVisible(false);
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.RENAME));
		bt.setName("btReplace_" + nature);
		bt.setToolTipText(I18N.getMsg("replace"));
		bt.addActionListener((ActionEvent evt) -> {
			replace(entity, bt, doOk);
		});
		bt.setMargin(new Insets(0, 0, 0, 0));
		resultat.add(r, MIG.get("split 3", MIG.GROWX));
		resultat.add(doOk);
		resultat.add(bt, MIG.WRAP);
	}

	private void doNext() {
		JLabel r = new JLabel(" ");
		resultat.add(r, MIG.WRAP);
	}

	private void replace(AbstractEntity entity, JButton bt, JLabel doOk) {
		words = txWords.getText();
		bywords = txByWords.getText();
		switch (Book.getTYPE(entity)) {
			case ATTRIBUTE:
				replaceAttribute((Attribute) entity);
				break;
			case CATEGORY:
				replaceCategory((Category) entity);
				break;
			case CHAPTER:
				replaceChapter((Chapter) entity);
				break;
			case EVENT:
				replaceEvent((Event) entity);
				break;
			case GENDER:
				replaceGender((Gender) entity);
				break;
			case IDEA:
				replaceIdea((Idea) entity);
				break;
			case ITEM:
				replaceItem((Item) entity);
				break;
			case LOCATION:
				replaceLocation((Location) entity);
				break;
			case MEMO:
				replaceMemo((Memo) entity);
				break;
			case PART:
				replacePart((Part) entity);
				break;
			case PERSON:
				replacePerson((Person) entity);
				break;
			case PLOT:
				replacePlot((Plot) entity);
				break;
			case RELATION:
				replaceRelation((Relation) entity);
				break;
			case SCENE:
				replaceScene((Scene) entity);
				break;
			case STRAND:
				replaceStrand((Strand) entity);
				break;
			case TAG:
				replaceTag((Tag) entity);
				break;
			default:
				break;
		}
		bt.setVisible(false);
		doOk.setVisible(true);
	}

	private void replaceAll(String name) {
		JButton bt = null;
		boolean all = (name.equals("all"));
		for (Component c : resultat.getComponents()) {
			if (c instanceof JButton) {
				if (all && (c.getName() != null) && c.getName().contains("btReplaceAll_")) {
					bt = (JButton) c;
				} else if ((c.getName() != null) && c.getName().contains("btReplaceAll_" + name)) {
					bt = (JButton) c;
				}
				boolean d = false;
				if (all && (c.getName() != null) && c.getName().contains("btReplace_")) {
					d = true;
				}
				if ((c.getName() != null) && c.getName().contains("btReplace_" + name)) {
					d = true;
				}
				if (d) {
					((JButton) c).doClick();
					if (bt != null) {
						bt.setVisible(false);
					}
				}
			}
		}
	}

	private void replaceEntity(AbstractEntity entity) {
		boolean b = false;
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceAttribute(Attribute entity) {
		replaceEntity(entity);
	}

	private void replaceCategory(Category entity) {
		replaceEntity(entity);
	}

	private void replaceChapter(Chapter entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setTitle(entity.getName().replace(words, bywords));
		}
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceEvent(Event entity) {
		replaceEntity(entity);
	}

	private void replaceGender(Gender entity) {
		replaceEntity(entity);
	}

	private void replaceIdea(Idea entity) {
		boolean b = false;
		if (entity.getCategory().contains(words)) {
			b = true;
			entity.setCategory(entity.getCategory().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceItem(Item entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getCategory().contains(words)) {
			b = true;
			entity.setCategory(entity.getCategory().replace(words, bywords));
		}
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceLocation(Location entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getAddress().contains(words)) {
			b = true;
			entity.setAddress(entity.getAddress().replace(words, bywords));
		}
		if (entity.getCity().contains(words)) {
			b = true;
			entity.setCity(entity.getCity().replace(words, bywords));
		}
		if (entity.getCountry().contains(words)) {
			b = true;
			entity.setCountry(entity.getCountry().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceMemo(Memo entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getCategory().contains(words)) {
			b = true;
			entity.setCategory(entity.getCategory().replace(words, bywords));
		}
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replacePart(Part entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replacePerson(Person entity) {
		boolean b = false;
		if (entity.getFirstname().contains(words)) {
			b = true;
			entity.setFirstname(entity.getFirstname().replace(words, bywords));
		}
		if (entity.getLastname().contains(words)) {
			b = true;
			entity.setLastname(entity.getLastname().replace(words, bywords));
		}
		if (entity.getOccupation().contains(words)) {
			b = true;
			entity.setOccupation(entity.getOccupation().replace(words, bywords));
		}
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replacePlot(Plot entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceRelation(Relation entity) {
		replaceEntity(entity);
	}

	private void replaceScene(Scene entity) {
		boolean b = false;
		if (entity.getTitle().contains(words)) {
			b = true;
			entity.setTitle(entity.getTitle().replace(words, bywords));
		}
		if (entity.getSummary().contains(words)) {
			b = true;
			String x = entity.getSummary();
			String y = x.replace(words, bywords);
			entity.setSummary(y);
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceStrand(Strand entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	private void replaceTag(Tag entity) {
		boolean b = false;
		if (entity.getName().contains(words)) {
			b = true;
			entity.setName(entity.getName().replace(words, bywords));
		}
		if (entity.getCategory().contains(words)) {
			b = true;
			entity.setCategory(entity.getCategory().replace(words, bywords));
		}
		if (entity.getDescription().contains(words)) {
			b = true;
			entity.setDescription(entity.getDescription().replace(words, bywords));
		}
		if (entity.getNotes().contains(words)) {
			b = true;
			entity.setNotes(entity.getNotes().replace(words, bywords));
		}
		if (b) {
			mainFrame.getBookController().updateEntity(entity);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
