/*
 * Copyright (C) 2023 favdb
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
package storybook.review;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.book.Book;
import storybook.db.endnote.Endnote;
import storybook.db.scene.Scene;
import storybook.tools.LOG;
import storybook.tools.file.IOUtil;
import storybook.tools.html.CSS;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSToolBar;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panel.typist.TypistPanel;

/**
 * class for he JPanel for Reviews
 *
 * @author favdb
 */
public class ReviewPanel extends JPanel implements MouseListener {

	private static final String TT = "ReviewPanel";

	private JEditorPane edReviews;
	private Scene scene;
	private final TypistPanel typist;
	private JButton btExport, btImport;
	private final MainFrame mainFrame;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ReviewPanel(MainFrame mainFrame, TypistPanel typist) {
		this.mainFrame = mainFrame;
		this.typist = typist;
		this.scene = typist.sceneGet();
		initialize();
	}

	/**
	 * initalize all
	 */
	public void initialize() {
		//LOG.printInfos(TT + ".initUi()");
		Border bord = BorderFactory.createLineBorder(Color.red);
		setBorder(BorderFactory.createTitledBorder(bord, I18N.getColonMsg("comments")));
		setLayout(new MigLayout(MIG.get(MIG.WRAP + " 2"), "[fill,grow]"));
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		d.width = (d.width / 12) * 9;
		setMaximumSize(d);
		this.add(initToolbar(), MIG.get(MIG.GROWX, MIG.SPAN));
		this.add(initEdComments(), MIG.GROW);
	}

	/**
	 * initialize the comments field
	 *
	 * @return
	 */
	private JScrollPane initEdComments() {
		edReviews = new JEditorPane();
		edReviews.setEditable(false);
		edReviews.setContentType(Html.TYPE);
		edReviews.addMouseListener(this);
		JScrollPane scroller = new JScrollPane(edReviews);
		SwingUtil.setMaxPreferredSize(scroller);
		return scroller;
	}

	/**
	 * initialize the tool bar
	 *
	 * @return
	 */
	public JToolBar initToolbar() {
		//LOG.printInfos(TT + ".initToolbar()");
		JSToolBar tb = new JSToolBar(false);
		tb.setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.HIDEMODE3)));
		tb.setName("CommentsToolbar");
		JButton bten = Ui.initButton("btAdd", "", ICONS.K.CHAR_COMMENT,
		   "comment.create",
		   evt -> reviewAdd());
		tb.add(bten);
		tb.add(new JLabel(), MIG.GROWX);
		JPanel p = new JPanel(new MigLayout(MIG.INS0));
		p.setOpaque(false);
		btExport = Ui.initButton("btExport", "", ICONS.K.F_EXPORT,
		   "comments.export",
		   evt -> reviewsExport());
		p.add(btExport);
		btImport = Ui.initButton("btImport", "", ICONS.K.F_IMPORT,
		   "comments.import",
		   evt -> reviewsImport());
		p.add(btImport);
		tb.add(p, MIG.RIGHT);
		return tb;
	}

	/**
	 * create a new Review allowed only for normal text, not for Markdown
	 *
	 */
	private void reviewAdd() {
		//LOG.printInfos(TT + ".reviewAdd()");
		if (typist.shefGet() == null) {
			return;
		}
		if (Review.create(mainFrame, scene, typist.shefGet()) != null) {
			mainFrame.setUpdated();
			refresh();
		}
	}

	/**
	 * edit the given Review
	 *
	 * @param endnote
	 */
	private void reviewEdit(Endnote endnote) {
		//LOG.printInfos(TT + ".reviewEdit(endnote)");
		if (!mainFrame.showEditorAsDialog(endnote)) {
			mainFrame.setUpdated();
			refresh();
		}
	}

	/**
	 * remove all reviews for the current scene
	 */
	private void reviewsRemove() {
		//LOG.printInfos(TT + ".commentsRemove()");
		for (Endnote en : Review.find(mainFrame, scene)) {
			mainFrame.getBookModel().ENDNOTE_Delete(en);
		}
		mainFrame.setUpdated();
		refresh();
	}

	/**
	 * remove the given review
	 *
	 * @param review
	 */
	private void commentRemove(Endnote review) {
		//LOG.printInfos(TT + ".commentRemove(" + AbstractEntity.printInfos(comment) + ")");
		if (review != null) {
			mainFrame.getBookModel().ENDNOTE_Delete(review);
			mainFrame.setUpdated();
			refresh();
		}
	}

	/**
	 * popup menu to select the format to export
	 */
	private void reviewsExport() {
		//LOG.printInfos(TT + ".commentsExport()");
		JPopupMenu menu = new JPopupMenu();
		menu.add(new JLabel(I18N.getMsg("comments.export_to")));
		JMenuItem item;
		item = new JMenuItem("TXT");
		item.addActionListener(e -> ExportReview.exec(this, "txt"));
		menu.add(item);
		item = new JMenuItem("HTML");
		item.addActionListener(e -> ExportReview.exec(this, "html"));
		menu.add(item);
		item = new JMenuItem("XML");
		item.addActionListener(e -> ExportReview.exec(this, "xml"));
		menu.add(item);
		menu.show(btExport, btExport.getX(), btExport.getY());
	}

	/**
	 * get the MainFrame
	 *
	 * @return
	 */
	public MainFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * select the file to import and select if existing coments	are to be remove
	 */
	private void reviewsImport() {
		//LOG.printInfos(TT + ".reviewsImport()");
		//select a file to import (XML only)
		File file = IOUtil.fileSelect(this, "", "xml",
		   I18N.getMsg("file.type.xml") + " (*.xml)", "import");
		if (file == null) {
			return;
		}
		// ask if add imported Comments or Replace all
		if (ImportReview.exec(mainFrame, file)) {
			typist.refresh();
			JOptionPane.showMessageDialog(null,
			   I18N.getMsg("comments.import.ok", file.getAbsolutePath()),
			   I18N.getMsg("import"),
			   JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * refresh this
	 */
	@SuppressWarnings("unchecked")
	public void refresh() {
		//LOG.printInfos(TT + ".refresh()");
		StringBuilder buf = new StringBuilder(Html.HTML_B);
		buf.append(Html.HEAD_B);
		buf.append(Html.STYLE_B);
		buf.append(CSS.forEditor());
		buf.append("tr {"
		   + "width:100%;\n"
		   + "border: 1px solid black; \n"
		   + "border-collapse: collapse;\n"
		   + "}");
		buf.append(Html.STYLE_E);
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		buf.append(Review.reviewsToHtml(mainFrame, scene));
		buf.append(Html.BODY_E);
		buf.append(Html.HTML_E);
		edReviews.setText(buf.toString());
		edReviews.setCaretPosition(0);
	}

	/**
	 * set the current scene
	 *
	 * @param s
	 */
	public void setScene(Scene s) {
		this.scene = s;
		refresh();
	}

	/**
	 * get the review from the link
	 *
	 * @param href
	 * @return
	 */
	private Endnote getEndnote(String href) {
		if (href == null || href.isEmpty()) {
			return null;
		}
		Long id = Long.valueOf(href.substring(href.indexOf("_") + 1));
		return (Endnote) mainFrame.project.get(Book.TYPE.ENDNOTE, id);
	}

	/**
	 * show the popup menu
	 *
	 * @param evt
	 */
	private void popupMenu(MouseEvent evt) {
		String href = getHref(evt);
		if (href != null) {
			Endnote endnote = getEndnote(href);
			JPopupMenu menu = new JPopupMenu();
			JMenuItem item;
			item = new JMenuItem(I18N.getMsg("edit"));
			item.setIcon(IconUtil.getIconSmall(ICONS.K.EDIT));
			item.addActionListener(e -> reviewEdit(endnote));
			menu.add(item);
			if (!Review.find(mainFrame, scene).isEmpty()) {
				item = new JMenuItem(I18N.getMsg("comment.remove"));
				item.setIcon(IconUtil.getIconSmall(ICONS.K.REMOVE));
				item.addActionListener(e -> commentRemove(endnote));
				menu.add(item);
				item = new JMenuItem(I18N.getMsg("comments.remove_scene"));
				item.setIcon(IconUtil.getIconSmall(ICONS.K.REMOVE));
				item.addActionListener(e -> reviewsRemove());
				menu.add(item);
			}
			Point p = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(p, edReviews);
			menu.show(edReviews, p.x, p.y);
		}
	}

	/**
	 * get the href attribute
	 *
	 * @param evt
	 * @return
	 */
	private String getHref(MouseEvent evt) {
		int pos = edReviews.getUI().viewToModel(edReviews, evt.getPoint());
		if (pos >= 0 && edReviews.getDocument() instanceof HTMLDocument) {
			HTMLDocument hdoc = (HTMLDocument) edReviews.getDocument();
			Element elem = hdoc.getCharacterElement(pos);
			if (elem.getAttributes().getAttribute(HTML.Tag.A) != null) {
				Object attribute = elem.getAttributes().getAttribute(HTML.Tag.A);
				if (attribute instanceof AttributeSet) {
					AttributeSet set = (AttributeSet) attribute;
					return (String) set.getAttribute(HTML.Attribute.HREF);
				}
			}
		}
		return null;
	}

	//*********************
	//*********************
	//** Mouse listening **
	//*********************
	//*********************
	@Override
	public void mouseClicked(MouseEvent evt) {
		// allowed only for normaltext not for Markdown
		if (typist.shefGet() == null) {
			return;
		}
		Endnote endnote = getEndnote(getHref(evt));
		if (endnote == null) {
			return;
		}
		if (evt.getButton() == MouseEvent.BUTTON3) {// right clicked: open popup menu
			popupMenu(evt);
		}
		if (evt.getClickCount() == 2
		   && evt.getButton() == MouseEvent.BUTTON1) {// double clicked: edit the comment
			reviewEdit(endnote);
		}
		// allways select the text
		String sort[] = endnote.getSort().split(" ");
		JEditorPane ht = typist.shefGet().wysEditorGet().getWysEditor();
		if (sort.length == 2) {
			int start = Integer.parseInt(sort[0]);
			int end = Integer.parseInt(sort[1]);
			ht.select(start, end);
			ht.requestFocusInWindow();
		} else {
			LOG.err(TT + ".mouseClicked(...) illegal sort value='" + endnote.getSort() + "'");
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//empty
	}

}
