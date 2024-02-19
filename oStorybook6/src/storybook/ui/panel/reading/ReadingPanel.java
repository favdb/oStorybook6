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

You should have received a fileCopy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui.panel.reading;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.strand.Strand;
import storybook.dialog.OptionsDlg;
import storybook.exim.exporter.ExportBookToHtml;
import storybook.review.Review;
import storybook.tools.ViewUtil;
import storybook.tools.html.CSS;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.print.HtmlPrinter;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.panel.AbstractPanel;
import static storybook.ui.panel.AbstractPanel.ALL;
import static storybook.ui.panel.AbstractPanel.EMPTY;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class ReadingPanel extends AbstractPanel implements HyperlinkListener {

	private static final String TT = "ReadingPanel.";

	private JEditorPane tpText;
	private JScrollPane scroller;
	private int scrollerWidth;
	private JComboBox cbToc;
	private JComboBox cbStrand;
	private JCheckBox ckReview;
	private JPanel pStrand;

	public ReadingPanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		if (newValue instanceof AbstractEntity) {
			switch (ActKey.getType(evt)) {
				case CHAPTER:
				case ENDNOTE:
				case SCENE:
					refresh();
					break;
				case PART:
					if (cbPartFilter != null) {
						int p = cbPartFilter.getSelectedIndex();
						Ui.fillCB(cbPartFilter, (List) mainFrame.project.getList(Book.TYPE.PART), BALL, this);
						cbPartFilter.setSelectedIndex(p);
					}
					refresh();
					ViewUtil.scrollToTop(scroller);
					break;
				case STRAND:
					int n = cbStrand.getSelectedIndex();
					Ui.fillCB(cbStrand, (List) mainFrame.project.getList(Book.TYPE.STRAND), BALL, this);
					cbStrand.setSelectedIndex(n);
					refresh();
					ViewUtil.scrollToTop(scroller);
					break;
				case LOCATION:
					if (mainFrame.getBook().getParam().getParamLayout().getChapterDateLocation()) {
						refresh();
					}
					break;
				case ITEM:
					if (mainFrame.getBook().getParam().getParamLayout().getSceneDidascalie()) {
						refresh();
					}
					break;
				default:
					break;
			}
		} else if (newValue instanceof View) {
			View view = (View) evt.getNewValue();
			if (view.getName().equals(SbView.VIEWNAME.READING.toString())) {
				switch (Ctrl.getPROPS(propName)) {
					case REFRESH:
						View vparent = (View) (getParent().getParent());
						if (vparent.equals(view)) {
							refresh();
						}
						break;
					case SHOWOPTIONS:
						OptionsDlg.show(mainFrame, view.getName());
						break;
					case READING_LAYOUT:
						setZoomedSize((Integer) newValue);
						scroller.setMaximumSize(new Dimension(scrollerWidth, 10000));
						scroller.getParent().invalidate();
						scroller.getParent().validate();
						scroller.getParent().repaint();
						break;
					case SHOWINFO:
						refresh();
						break;
					case PRINT:
						printAction();
						break;
					default:
						break;
				}
			}
		}
	}

	private void setZoomedSize(int zoomValue) {
		scrollerWidth = zoomValue * 10;
	}

	@Override
	public void init() {
		this.withPart = true;
		try {
			setZoomedSize(App.preferences.readingGetZoom());
		} catch (Exception e) {
			setZoomedSize(Pref.KEY.READING_ZOOM.getInteger());
		}
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.HIDEMODE3), "[][fill,grow]", ""));
		initToolbar();
		if (toolbar.getComponentCount() > 0) {
			add(toolbar, MIG.get(MIG.GROWX, MIG.SPAN, MIG.WRAP));
		}
		tpText = new JEditorPane();
		tpText.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		String emRule = "em {"
		   + " background-color: yellow;"
		   + " font-style: normal;"
		   + "}";
		styleSheet.addRule(emRule);
		tpText.setEditorKitForContentType(Html.TYPE, kit);
		tpText.setContentType(Html.TYPE);
		tpText.addHyperlinkListener(this);
		scroller = new JScrollPane(tpText);
		SwingUtil.setMaxPreferredSize(scroller);
		add(scroller, MIG.GROWY);
		refresh();
	}

	@Override
	@SuppressWarnings("unchecked")
	public JToolBar initToolbar() {
		//LOG.trace(TT + "initToolbar()");
		super.initToolbar();
		pStrand = new JPanel(new MigLayout(MIG.INS1));
		pStrand.add(new JLabel(I18N.getColonMsg("strand")));
		cbStrand = Ui.initComboBox("cbStrand", "",
		   (List) mainFrame.project.getList(Book.TYPE.STRAND),
		   null, !EMPTY, ALL, this);
		pStrand.add(cbStrand);
		toolbar.add(pStrand);

		ckReview = Ui.initCheckBox(null, "ckReview", "review",
		   book.param.getParamLayout().getShowReview(), BNONE,
		   e -> changeReview());
		ckReview.setVisible(!Review.find(mainFrame).isEmpty());
		toolbar.add(ckReview);
		toolbar.add(new JLabel(I18N.getColonMsg("toc")));
		cbToc = new JComboBox();
		cbToc.addItem(I18N.getMsg("view.reading.toc_normal"));
		cbToc.addItem(I18N.getMsg("parts"));
		cbToc.addItem(I18N.getMsg("chapters"));
		cbToc.addItem(I18N.getMsg("scenes"));
		cbToc.setSelectedIndex(App.preferences.readingGetToclevel());
		cbToc.addActionListener(e -> changeToclevel());
		toolbar.add(cbToc);
		return toolbar;
	}

	private void changeToclevel() {
		App.preferences.readingSetToclevel(cbToc.getSelectedIndex());
		App.preferences.save();
		refresh();
	}

	@Override
	public void refresh() {
		boolean nb = mainFrame.project.getList(Book.TYPE.STRAND).size() > 1;
		pStrand.setVisible(nb);
		ckReview.setVisible(!Review.find(mainFrame).isEmpty());
		int n = 0;
		for (Component c : toolbar.getComponents()) {
			if (c.isVisible()) {
				n++;
			}
		}
		toolbar.setVisible(n > 0);
		StringBuilder buf = new StringBuilder();
		buf.append(Html.HTML_B);
		buf.append(Html.HEAD_B);
		buf.append(Html.getHeadTitle(book.getTitle()));
		buf.append(Html.STYLE_B);
		if (mainFrame.project.book.getScenario()) {
			buf.append(CSS.forScenario(true));
		} else {
			buf.append(CSS.forEditor());
		}
		buf.append(Html.STYLE_E);
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		// content
		Strand strand = null;
		if (cbStrand.getSelectedIndex() > 0) {
			strand = (Strand) cbStrand.getSelectedItem();
		}
		buf.append(ExportBookToHtml.toPanel(mainFrame,
		   strand, getCbPart(), ckReview.isSelected(), cbToc.getSelectedIndex()));
		buf.append(Html.P_EMPTY);
		buf.append(Html.BODY_E);
		buf.append(Html.HTML_E);
		final int pos = scroller.getVerticalScrollBar().getValue();
		tpText.setText(buf.toString());
		final Action restoreAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scroller.getVerticalScrollBar().setValue(pos);
			}
		};
		SwingUtilities.invokeLater(() -> restoreAction.actionPerformed(null));
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		//LOG.trace(TT + ".hyperlinkUpdate(evt=" + evt.toPrint() + ")");
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			Net.openUrl(evt);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT+"actionPerformed(e=" + e.toPrint());
		if (e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) e.getSource();
			if (cb.getName().equals("cbPartFilter")) {
				refresh();
			}
			if (cb.getName().equals("cbStrand")) {
				refresh();
			}
		}
	}

	private void changeReview() {
		book.param.getParamLayout().setShowReview(ckReview.isSelected());
		mainFrame.setUpdated();
		refresh();
	}

	private void printAction() {
		//LOG.trace(TT + "printAction()");
		String html = ExportBookToHtml.toPrint(mainFrame);
		HtmlPrinter.pr("reading", this, html, book.getTitle(), "");
	}

}
