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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.action.ScrollToStrandDateAction;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.ideabox.IdeaxFrm;
import storybook.ideabox.IdeaxXml;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.Ui.MINIMUM_SIZE;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.book.BookPanel;
import storybook.ui.panel.chrono.ChronoPanel;

/**
 *
 * @author favdb
 */
public class SearchDlg extends AbsDialog implements ItemListener {

	private String[] words;
	private JPanel resultat = new JPanel();
	private JTextField txWords;
	List<JCheckBox> cbList;
	String[] objects = {
		"strand",
		"part",
		"chapter",
		"scene",
		"person",
		"location",
		"item",
		"tag",
		"idea",
		"memo",
		"plot",
		"ideabox"
	};
	private JButton btAll;
	private JButton btCancel;
	private int nbResultat;
	private JLabel lbResultat;
	private JTabbedPane tabbedPane;
	private JComboBox chapterCombo;
	private JSPanelViewsRadioButton viewsRbPanel;
	private JComboBox strandCombo;
	private JComboBox dateCombo;
	private JLabel lbWarning;
	private JButton btFindWords;
	private JButton btFindChapter;
	private JButton btFindDate;

	public SearchDlg(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout());
		setTitle(I18N.getMsg("find"));
		btCancel = getCancelButton();
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(I18N.getMsg("words"), initFindWords());
		tabbedPane.addTab(I18N.getMsg("chapter"), initFindChapter());
		tabbedPane.addTab(I18N.getMsg("date"), initFindDate());
		add(tabbedPane);
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	private JPanel initFindWords() {
		JPanel p = new JPanel(new MigLayout(MIG.FILL));
		p.add(new JLabel(I18N.getColonMsg("search.words")));
		txWords = new JTextField();
		p.add(txWords, MIG.get(MIG.GROWX, MIG.WRAP));
		btAll = new JButton(I18N.getMsg("all"));
		btAll.addActionListener(evt -> selectAll());
		JPanel jPanel1 = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP + " 4")));
		jPanel1.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("search.for")));
		cbList = new ArrayList<>();
		for (String str : objects) {
			JCheckBox cb = new JCheckBox(I18N.getMsg(str));
			cb.setName(str);
			cb.addActionListener(evt -> checkIfOk());
			jPanel1.add(cb);
			cbList.add(cb);
		}
		jPanel1.add(new JLabel(""), MIG.WRAP);
		jPanel1.add(btAll, MIG.get(MIG.SPAN, MIG.RIGHT));
		p.add(jPanel1, MIG.get(MIG.SPAN, MIG.WRAP));

		JPanel end = new JPanel(new MigLayout());
		btFindWords = Ui.initButton("btFind", "find", ICONS.K.SEARCH, "", (ActionEvent evt) -> {
			searchEntities();
		});
		end.add(btFindWords);
		end.add(getCancelButton());
		p.add(end, MIG.get(MIG.SPAN, MIG.BOTTOM, MIG.RIGHT));
		return p;
	}

	private void checkIfOk() {
		boolean bb = false;
		for (JCheckBox cb : cbList) {
			if (cb.isSelected()) {
				bb = true;
				break;
			}
		}
		btFindWords.setEnabled(bb);
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
		words = s.split(" ");
		for (String word : words) {
			if (word.length() < 4) {
				SwingUtil.showError("search.error.word");
				return;
			}
		}
		initResultat();
		boolean x = false;
		for (JCheckBox cb : cbList) {
			if (cb.isSelected()) {
				switch (Book.getTYPE(cb.getName())) {
					case STRAND:
						findStrands();
						break;
					case PART:
						findParts();
						break;
					case CHAPTER:
						findChapters();
						break;
					case SCENE:
						findScenes();
						break;
					case PERSON:
						findPersons();
						break;
					case PLOT:
						findPlots();
						break;
					case LOCATION:
						findLocations();
						break;
					case ITEM:
						findItems();
						break;
					case TAG:
						findTags();
						break;
					case IDEA:
						findIdeas();
						break;
					case IDEABOX:
						findIdeabox();
						break;
					case MEMO:
						findMemos();
						break;
					default:
						break;
				}
				x = true;
			}
		}
		if (!x) {
			SwingUtil.showError("search.error.object");
			return;
		}
		if (nbResultat > 0) {
			lbResultat.setText(I18N.getMsg("search.find", nbResultat));
		}
		showResults(resultat);
	}

	private void findStrands() {
		@SuppressWarnings("unchecked")
		List<Strand> entities = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		boolean finds = false;
		doTitle(I18N.getMsg("strand"));
		for (Strand entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findParts() {
		@SuppressWarnings("unchecked")
		List<Part> entities = (List) mainFrame.project.getList(Book.TYPE.PART);
		boolean finds = false;
		doTitle(I18N.getMsg("part"));
		for (Part entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findChapters() {
		@SuppressWarnings("unchecked")
		List<Chapter> entities = (List) mainFrame.project.getList(Book.TYPE.CHAPTER);
		boolean finds = false;
		doTitle(I18N.getMsg("chapter"));
		for (Chapter entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getChapternoStr() + " " + entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findScenes() {
		@SuppressWarnings("unchecked")
		List<Scene> entities = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		boolean finds = false;
		doTitle(I18N.getMsg("scene"));
		for (Scene entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))
			   || searchWordsHtml(entity.getSummary())) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getFullTitle(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findPersons() {
		@SuppressWarnings("unchecked")
		List<Person> entities = (List) mainFrame.project.getList(Book.TYPE.PERSON);
		boolean finds = false;
		doTitle(I18N.getMsg("person"));
		for (Person entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getFullNameAbbr(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findPlots() {
		@SuppressWarnings("unchecked")
		List<Plot> entities = (List) mainFrame.project.getList(Book.TYPE.PLOT);
		boolean finds = false;
		doTitle(I18N.getMsg("plot"));
		for (Plot entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findLocations() {
		@SuppressWarnings("unchecked")
		List<Location> entities = (List) mainFrame.project.getList(Book.TYPE.LOCATION);
		boolean finds = false;
		doTitle(I18N.getMsg("location"));
		for (Location entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findItems() {
		@SuppressWarnings("unchecked")
		List<Item> entities = (List) mainFrame.project.getList(Book.TYPE.ITEM);
		boolean finds = false;
		doTitle(I18N.getMsg("item"));
		for (Item entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findTags() {
		@SuppressWarnings("unchecked")
		List<Tag> entities = (List) mainFrame.project.getList(Book.TYPE.TAG);
		boolean finds = false;
		doTitle(I18N.getMsg("tag"));
		for (Tag entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findIdeas() {
		@SuppressWarnings("unchecked")
		List<Idea> entities = (List) mainFrame.project.getList(Book.TYPE.IDEA);
		boolean finds = false;
		doTitle(I18N.getMsg("idea"));
		for (Idea entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getId() + " " + TextUtil.ellipsize(entity.getNotes(), 30), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private void findIdeabox() {
		boolean finds = false;
		doTitle(I18N.getMsg("ideabox"));
		IdeaxXml xml = new IdeaxXml();
		for (Idea idea : xml.ideas) {
			if (searchWords(idea.toDetail(3))) {
				if (!finds) {
					doNext();
				}
				doEntity("ideabox", idea);
			}
		}
	}

	private void findMemos() {
		@SuppressWarnings("unchecked")
		List<Memo> entities = (List) mainFrame.project.getList(Book.TYPE.MEMO);
		boolean finds = false;
		doTitle(I18N.getMsg("memo"));
		for (Memo entity : entities) {
			if (searchWords(entity.toCsv(" ", " ", "\t"))) {
				if (!finds) {
					doNext();
				}
				doEntity(entity.getName(), entity);
				finds = true;
			}
		}
		if (!finds) {
			doEmpty();
		}
	}

	private boolean searchWords(String str) {
		String r = Html.htmlToText(str);
		for (String word : words) {
			if (r.toLowerCase(Locale.getDefault()).contains(word.toLowerCase(Locale.getDefault()))) {
				return (true);
			}
		}
		return false;
	}

	private boolean searchWordsHtml(String str) {
		String r = Html.htmlToText(str);
		for (String word : words) {
			if (r.toLowerCase(Locale.getDefault()).contains(word.toLowerCase(Locale.getDefault()))) {
				return (true);
			}
		}
		return false;
	}

	private void showResults(JPanel res) {
		SearchResultsDlg dlg = new SearchResultsDlg(mainFrame, res);
		dlg.setVisible(true);
	}

	private void initResultat() {
		resultat.setLayout(new MigLayout(MIG.get(MIG.TOP, MIG.WRAP), "[][]"));
		resultat.setBackground(Color.white);
		resultat.setMinimumSize(MINIMUM_SIZE);
		resultat.removeAll();
		lbResultat = new JLabel("");
		resultat.add(lbResultat, MIG.SPAN);
		nbResultat = 0;
	}

	private void doTitle(String msg) {
		JLabel lb = new JLabel(msg);
		lb.setFont(FontUtil.getBold());
		resultat.add(lb);
	}

	private void doEmpty() {
		JLabel r = new JLabel(I18N.getMsg("search.empty"));
		resultat.add(r, MIG.WRAP);
	}

	private void doEntity(String str, AbstractEntity entity) {
		JLabel lb = new JLabel(IconUtil.getIconSmall(ICONS.K.NAV_NEXT));
		resultat.add(lb, MIG.RIGHT);
		JLabel r = new JLabel(str);
		if (str.startsWith("ideabox")) {
			r.setText(entity.toString());
		}
		r.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (str.startsWith("ideabox")) {
					IdeaxFrm ideaxFrm = IdeaxFrm.getInstance();
					int index = ideaxFrm.ideaSelect((Idea) entity);
					if (index != -1) {
						ideaxFrm.ideaUpdate();
					}
				} else {
					mainFrame.showEditorAsDialog(entity);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				resultat.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				resultat.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		resultat.add(r, MIG.WRAP);
		nbResultat++;
	}

	private void doNext() {
		JLabel r = new JLabel(" ");
		resultat.add(r, MIG.WRAP);
	}

	private JPanel initFindChapter() {
		//LOG.printInfos(TT+".initFindChapter()");
		JPanel p = new JPanel(
		   new MigLayout(MIG.get(MIG.FILL, MIG.WRAP + " 2"),
			  "[]10[grow]10[]",
			  "[]10[]10[]"));
		JPanel px = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		px.add(new JLabel(I18N.getColonMsg("chapter")));
		chapterCombo = new JComboBox();
		Chapter chapter = new Chapter();
		EntityUtil.cbFill(mainFrame, chapterCombo, Book.TYPE.CHAPTER, chapter, false, false);
		SwingUtil.setMaxWidth(chapterCombo, 200);
		px.add(chapterCombo, MIG.get(MIG.GROWX));
		IconButton btPrev = new IconButton("btPrev", ICONS.K.NAV_PREV, evt -> {
			int index = chapterCombo.getSelectedIndex();
			--index;
			if (index == -1) {
				index = 0;
			}
			chapterCombo.setSelectedIndex(index);
			scrollToChapter();
		});
		//btPrev.set20x20();
		px.add(btPrev);
		IconButton btNext = new IconButton("btNext", ICONS.K.NAV_NEXT, evt -> {
			int index = chapterCombo.getSelectedIndex();
			++index;
			if (index == chapterCombo.getItemCount()) {
				index = chapterCombo.getItemCount() - 1;
			}
			chapterCombo.setSelectedIndex(index);
			scrollToChapter();
		});
		//btNext.set20x20();
		px.add(btNext);
		p.add(px, MIG.SPAN);
		p.add(new JLabel(I18N.getColonMsg("navigation.show.in")), MIG.WRAP);
		viewsRbPanel = new JSPanelViewsRadioButton(mainFrame);
		p.add(viewsRbPanel, MIG.WRAP);

		JPanel end = new JPanel(new MigLayout());
		btFindChapter = Ui.initButton("btFind", "find", ICONS.K.SEARCH, "", (ActionEvent evt) -> {
			searchEntities();
			dispose();
		});
		end.add(btFindChapter);
		end.add(getCancelButton());
		p.add(end, MIG.get(MIG.SPAN, MIG.BOTTOM, MIG.RIGHT));
		return p;
	}

	private JPanel initFindDate() {
		//LOG.printInfos(TT+".initFindDate()");
		JPanel p = new JPanel(
		   new MigLayout(MIG.get(MIG.FILL, MIG.WRAP + " 2", MIG.HIDEMODE3)));
		JPanel px = new JPanel(new MigLayout());
		px.add(new JLabel(I18N.getColonMsg("strand")), MIG.RIGHT);
		strandCombo = new JComboBox();
		EntityUtil.cbFill(mainFrame, strandCombo, Book.TYPE.STRAND, new Strand(), false, false);
		strandCombo.addItemListener(this);
		px.add(strandCombo, MIG.get(MIG.SPAN, MIG.GROWX));
		px.add(new JLabel(I18N.getColonMsg("date")));
		dateCombo = new JComboBox();
		refreshDateCombo();
		px.add(dateCombo, MIG.get(MIG.GROWX));
		JButton btPrev = Ui.initButton("btPrev", "", ICONS.K.NAV_PREV, "previous", evt -> {
			int index = dateCombo.getSelectedIndex();
			--index;
			if (index == -1) {
				index = 0;
			}
			dateCombo.setSelectedIndex(index);
			scrollToStrandDate();
		});
		px.add(btPrev);
		JButton btNext = Ui.initButton("btNext", "", ICONS.K.NAV_NEXT, "next", evt -> {
			int index = dateCombo.getSelectedIndex();
			++index;
			if (index == dateCombo.getItemCount()) {
				index = dateCombo.getItemCount() - 1;
			}
			dateCombo.setSelectedIndex(index);
			scrollToStrandDate();
		});
		px.add(btNext);
		p.add(px, MIG.SPAN);
		p.add(new JLabel(I18N.getColonMsg("navigation.show.in")), MIG.SPAN);
		viewsRbPanel = new JSPanelViewsRadioButton(mainFrame, false);
		p.add(viewsRbPanel, MIG.SPAN);
		lbWarning = new JLabel(" ");
		lbWarning.setVisible(false);
		p.add(lbWarning, MIG.get(MIG.SPAN, MIG.GROWX));
		JPanel end = new JPanel(new MigLayout());
		btFindDate = Ui.initButton("btFind", "find", ICONS.K.SEARCH, "", (ActionEvent evt) -> {
			searchEntities();
			dispose();
		});
		end.add(btFindDate);
		end.add(getCancelButton());
		p.add(end, MIG.get(MIG.SPAN, MIG.BOTTOM, MIG.RIGHT));
		return p;
	}

	@SuppressWarnings("unchecked")
	private void refreshDateCombo() {
		Strand strand = (Strand) strandCombo.getSelectedItem();
		List<Date> dates = mainFrame.project.scenes.findDistinctDates(strand);
		dateCombo.removeAllItems();
		for (Date date : dates) {
			dateCombo.addItem(date);
		}
	}

	@SuppressWarnings("null")
	private void scrollToStrandDate() {
		Strand strand = (Strand) strandCombo.getSelectedItem();
		Date date = (Date) dateCombo.getSelectedItem();
		SbView view = null;
		boolean chrono = viewsRbPanel.isChronoSelected();
		boolean isBook = viewsRbPanel.isBookSelected();
		if (chrono) {
			view = mainFrame.getView(SbView.VIEWNAME.CHRONO);
			mainFrame.showView(SbView.VIEWNAME.CHRONO);
		} else if (isBook) {
			view = mainFrame.getView(SbView.VIEWNAME.BOOK);
			mainFrame.showView(SbView.VIEWNAME.BOOK);
		}
		AbstractPanel container = null;
		JPanel panel = null;
		if (chrono) {
			container = (ChronoPanel) view.getComponent();
			panel = ((ChronoPanel) container).getPanel();
		} else if (isBook) {
			container = (BookPanel) view.getComponent();
			panel = ((BookPanel) container).getPanel();
		}
		int delay = 20;
		if (view != null && !view.isLoaded()) {
			delay += 180;
		}
		if (isBook) {
			delay += 100;
		}
		ScrollToStrandDateAction action = new ScrollToStrandDateAction(
		   container, panel, strand, date, lbWarning);
		Timer timer = new Timer(delay, action);
		timer.setRepeats(false);
		timer.start();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			refreshDateCombo();
		}
	}

	private void scrollToChapter() {
		Chapter chapter = (Chapter) chapterCombo.getSelectedItem();
		Ctrl ctrlBook = mainFrame.getBookController();
		if (viewsRbPanel.isChronoSelected()) {
			mainFrame.showView(SbView.VIEWNAME.CHRONO);
			ctrlBook.chronoShow(chapter);
		} else if (viewsRbPanel.isBookSelected()) {
			mainFrame.showView(SbView.VIEWNAME.BOOK);
			ctrlBook.bookShow(chapter);
		} else if (viewsRbPanel.isManageSelected()) {
			mainFrame.showView(SbView.VIEWNAME.MANAGE);
			ctrlBook.manageShow(chapter);
		}
	}

	public static void show(MainFrame m) {
		SwingUtil.showModalDialog(new SearchDlg(m), m, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public class JSPanelViewsRadioButton extends AbstractPanel {

		private JRadioButton rbChrono;
		private JRadioButton rbBook;
		private JRadioButton rbManage;
		private boolean showManage;

		public JSPanelViewsRadioButton(MainFrame mainFrame) {
			this(mainFrame, true);
		}

		public JSPanelViewsRadioButton(MainFrame mainFrame, boolean showManage) {
			super(mainFrame);
			this.showManage = showManage;
			initAll();
		}

		@Override
		public void modelPropertyChange(PropertyChangeEvent evt) {
			// empty
		}

		@Override
		public void init() {
			// empty
		}

		@Override
		public void initUi() {
			setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.WRAP), "[32px][][]"));
			ButtonGroup btGroup = new ButtonGroup();
			add(new JLabel(" "));
			add(new JLabel(IconUtil.getIconSmall(ICONS.K.VW_CHRONO)));
			rbChrono = new JRadioButton(I18N.getMsg("view.chrono"));
			rbChrono.setSelected(true);
			add(rbChrono);
			btGroup.add(rbChrono);

			add(new JLabel(" "));
			add(new JLabel(IconUtil.getIconSmall(ICONS.K.VW_BOOK)));
			rbBook = new JRadioButton(I18N.getMsg("view.book"));
			add(rbBook);
			btGroup.add(rbBook);

			if (showManage) {
				add(new JLabel(" "));
				add(new JLabel(IconUtil.getIconSmall(ICONS.K.VW_MANAGE)));
				rbManage = new JRadioButton(I18N.getMsg("view.manage"));
				add(rbManage);
				btGroup.add(rbManage);
			}
		}

		public boolean isChronoSelected() {
			return rbChrono.isSelected();
		}

		public boolean isBookSelected() {
			return rbBook.isSelected();
		}

		public boolean isManageSelected() {
			return rbManage.isSelected();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// empty
		}

	}
}
