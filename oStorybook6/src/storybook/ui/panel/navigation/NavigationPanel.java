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
package storybook.ui.panel.navigation;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import resources.icons.ICONS;
import resources.icons.IconButton;
import storybook.action.ScrollToStrandDateAction;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.strand.Strand;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.tools.swing.js.JSPanelViewsRadioButton;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.book.BookPanel;
import storybook.ui.panel.chrono.ChronoPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class NavigationPanel extends AbstractPanel implements ItemListener {

	private static final String TT = "Navigation.";

	private JTabbedPane tabbedPane;
	private JComboBox chapterCombo;
	private JSPanelViewsRadioButton viewsRbPanel;
	private JComboBox strandCombo;
	private JSLabel lbWarning;
	private JComboBox dateCombo;

	public NavigationPanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT+"modelPropertyChange("+evt.toString()+")");
		String propName = evt.getPropertyName();
		if ("SHOWINFO".equalsIgnoreCase(propName)) {
			return;
		}
		Object newValue = evt.getNewValue();
		if (Ctrl.PROPS.REFRESH.check(propName)) {
			View newView = (View) newValue;
			View view = (View) getParent().getParent();
			if (view == newView) {
				refresh();
			}
			return;
		}
		if (propName.startsWith("Edit") || propName.startsWith("Init")) {
			return;
		}
		if (propName.contains("Scene") || propName.contains("Chapter") || propName.contains("Strand")) {
			refresh();
		}
	}

	@Override
	public void refresh() {
		//LOG.trace(TT+".refresh()");
		int index = tabbedPane.getSelectedIndex();
		super.refresh();
		tabbedPane.setSelectedIndex(index);
	}

	@Override
	public void init() {
		//LOG.trace(TT+".init()");
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+".initUi()");
		setLayout(new MigLayout(MIG.FILLX));
		//no toolbar to set
		add(new JLabel("<html>" + I18N.getMsg("navigate.not_used") + "</html>"), MIG.CENTER);
		//tabbedPane = new JTabbedPane();
		//tabbedPane.addTab(I18N.getMsg("chapter"), initFindChapter());
		//tabbedPane.addTab(I18N.getMsg("date"), initFindDate());
		//add(tabbedPane, "grow");
	}

	private JPanel initFindChapter() {
		//LOG.trace(TT+".initFindChapter()");
		JPanel p = new JPanel(
				new MigLayout(MIG.get(MIG.FLOWX, MIG.WRAP + " 2"),
						"[]10[grow]10[]",
						"[]10[]10[]"));
		JPanel px = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		px.add(new JSLabel(I18N.getColonMsg("chapter")));
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
		p.add(new JSLabel(I18N.getColonMsg("navigation.show.in")), MIG.WRAP);
		viewsRbPanel = new JSPanelViewsRadioButton(mainFrame);
		p.add(viewsRbPanel, MIG.WRAP);
		JButton btFind = new IconButton("btFind", ICONS.K.SEARCH, evt -> scrollToChapter());
		btFind.setText(I18N.getMsg("find"));
		SwingUtil.addEnterAction(btFind, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scrollToChapter();
			}
		});
		p.add(btFind, MIG.get(MIG.SPAN, MIG.CENTER));
		return p;
	}

	private JPanel initFindDate() {
		//LOG.trace(TT+".initFindDate()");
		JPanel p = new JPanel(
				new MigLayout(MIG.get(MIG.FLOWX, MIG.WRAP + " 2", MIG.HIDEMODE3)));
		JPanel px = new JPanel(new MigLayout());
		px.add(new JSLabel(I18N.getColonMsg("strand")), MIG.RIGHT);
		strandCombo = new JComboBox();
		EntityUtil.cbFill(mainFrame, strandCombo, Book.TYPE.STRAND, new Strand(), false, false);
		strandCombo.addItemListener(this);
		px.add(strandCombo, MIG.get(MIG.SPAN, MIG.GROWX));
		px.add(new JSLabel(I18N.getColonMsg("date")));
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
		p.add(new JSLabel(I18N.getColonMsg("navigation.show.in")), MIG.SPAN);
		viewsRbPanel = new JSPanelViewsRadioButton(mainFrame, false);
		p.add(viewsRbPanel, MIG.SPAN);
		lbWarning = new JSLabel(" ");
		lbWarning.setVisible(false);
		p.add(lbWarning, MIG.get(MIG.SPAN, MIG.GROWX));
		JButton btFind = Ui.initButton("btFind", "find", ICONS.K.SEARCH, "", evt -> scrollToStrandDate());
		SwingUtil.addEnterAction(btFind, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scrollToStrandDate();
			}
		});
		p.add(btFind, MIG.get(MIG.SPAN, MIG.CENTER));
		return p;
	}

	@SuppressWarnings("unchecked")
	private void refreshDateCombo() {
		Strand strand = (Strand) strandCombo.getSelectedItem();
		List<Date> dates = mainFrame.project.scenes.findDistinctDatesByStrand(mainFrame, strand);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
