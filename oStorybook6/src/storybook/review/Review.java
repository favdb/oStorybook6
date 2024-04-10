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
import api.shef.ShefEditor;
import i18n.I18N;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import resources.icons.ICONS;
import storybook.db.endnote.Endnote;
import storybook.db.scene.Scene;
import storybook.model.Model;
import storybook.tools.html.Html;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.BNONE;
import storybook.dialog.MessageDlg;

/**
 * static class for Review function
 *
 * @author favdb
 */
public class Review {

	private static final String TT = "Review";

	private static JCheckBox ckReview;
	private static JButton btRenumber;
	private static JButton btRemove;

	/**
	 * get the menu function
	 *
	 * @param mainFrame
	 */
	public static void Menu(MainFrame mainFrame) {
		// empty
	}

	/**
	 * get the properties parameters
	 *
	 * @param mainFrame
	 * @return
	 */
	public static JPanel Properties(MainFrame mainFrame) {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE2)));
		ckReview = Ui.initCheckBox(null, "ckReview", "book.revision",
		   mainFrame.getBook().info.reviewGet(), BNONE);
		ckReview.addActionListener(e -> {
			btRenumber.setVisible(ckReview.isSelected());
			btRemove.setVisible(ckReview.isSelected());
		});
		p.add(ckReview);
		btRenumber = Ui.initButton("btRenumber", "", ICONS.K.SORT, "comments.renumber",
		   e -> Endnote.renumber(mainFrame, 1));
		btRenumber.setVisible(ckReview.isSelected());
		p.add(btRenumber);
		btRemove = Ui.initButton("btRemove", "", ICONS.K.DELETE, "comments.remove_all",
		   e -> Endnote.removeAll(mainFrame, Endnote.TYPE.COMMENT));
		btRemove.setVisible(ckReview.isSelected());
		p.add(btRemove);
		return p;
	}

	/**
	 * find reviews of the given type
	 *
	 * @param mainFrame
	 *
	 * @return
	 */
	public static List<Endnote> find(MainFrame mainFrame) {
		//LOG.trace(TT + ".find(mainFrame, type=" + type + ")");
		return Endnote.find(mainFrame, Endnote.TYPE.COMMENT);
	}

	/**
	 * find reviews of the given type
	 *
	 * @param mainFrame
	 * @param scene
	 *
	 * @return
	 */
	public static List<Endnote> find(MainFrame mainFrame, Scene scene) {
		//LOG.trace(TT + ".find(mainFrame, type=" + type + ")");
		List<Endnote> list = mainFrame.project.endnotes.find(Endnote.TYPE.COMMENT.ordinal(), scene);
		return list;
	}

	/**
	 * find a review of the given scene with the given number
	 *
	 * @param mainFrame
	 * @param scene
	 * @param num
	 *
	 * @return
	 */
	public static Endnote find(MainFrame mainFrame, Scene scene, int num) {
		//LOG.trace(TT + ".find(mainFrame, type=" + type + ")");
		for (Endnote en : find(mainFrame, scene)) {
			if (en.getNumber().equals(num)) {
				return en;
			}
		}
		return null;
	}

	/**
	 * get the reviews of the given scene as a HTML table
	 *
	 * @param mainFrame
	 * @param scene
	 *
	 * @return
	 */
	public static String reviewsToHtml(MainFrame mainFrame, Scene scene) {
		return reviewsToHtml(mainFrame, mainFrame.project.endnotes.find(Endnote.TYPE.COMMENT, scene));
	}

	/**
	 * get the given list of reviews as a HTML table
	 *
	 * @param mainFrame
	 * @param list of review
	 * @param withTitle
	 *
	 * @return
	 */
	public static String reviewsToHtml(MainFrame mainFrame, List<Endnote> list, boolean... withTitle) {
		//LOG.trace(TT + ".reviewsToHtml(mainFrame, endnotes list nb=" + list.size() + ")");
		if (list.isEmpty()) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		if (withTitle != null) {
			b.append(Html.intoPcenter(I18N.getColonMsg("review")));
		}
		b.append(Html.TABLE_B);
		int idx = 1;
		for (Endnote en : list) {
			b.append(Html.TR_B);
			b.append(Html.intoTD(linkFrom(idx++, en),
			   "valign=\"top\" style=\"width:3%\""));
			b.append(Html.intoTD(en.getNotes(),
			   "style=\"width:97%\""));
			b.append(Html.TR_E);
		}
		b.append(Html.TABLE_E);
		return b.toString();
	}

	/**
	 * create a review
	 *
	 * @param mainFrame
	 * @param scene
	 * @param htTexte
	 *
	 * @return
	 */
	public static Endnote create(MainFrame mainFrame, Scene scene, ShefEditor htTexte) {
		//LOG.trace(TT + ".create(mainFrame, scene=" + LOG.trace(scene) + ", shef)");
		JEditorPane ht = htTexte.wysEditorGet().getWysEditor();
		if (scene == null) {
			MessageDlg.show(null,
			   I18N.getMsg("comment.missing_scene"),
			   "comment.create",
			   true);
			return null;
		}
		if (ht.getSelectedText() == null || ht.getSelectedText().isEmpty()) {
			MessageDlg.show(null,
			   I18N.getMsg("comment.missing_text"),
			   "comment.create",
			   true);
			return null;
		}
		int num = Endnote.getNextNumber(Endnote.find(mainFrame, Endnote.TYPE.COMMENT));
		//initialize notes with selected text
		String notes = "<div class=\"review\">" + ht.getSelectedText() + "</div>"
		   + "<p>insérer votre commentaire ici</p>";
		Endnote en = new Endnote(Endnote.TYPE.COMMENT, scene, num, notes);
		String sort = ht.getSelectionStart() + " " + ht.getSelectionEnd();
		en.setSort(sort);
		if (mainFrame.showEditorAsDialog(en)) {
			return null;
		}
		return en;
	}

	/**
	 * remove all reviews
	 *
	 * @param mainFrame
	 */
	public static void removeAll(MainFrame mainFrame) {
		//LOG.trace(TT + ".removeAll(mainFrame)");
		Model md = mainFrame.getBookModel();
		for (Endnote en : find(mainFrame)) {
			md.ENTITY_Delete(en);
		}
	}

	/**
	 * get a String which link to the scene from the Endnote link
	 *
	 * @param idx
	 * @param endnote
	 * @return
	 */
	public static String linkFrom(int idx, Endnote endnote) {
		//LOG.trace(TT + ".linkFrom(endnote=" + LOG.trace(endnote) + ")");
		String libname = "comment";
		String libhref = "in" + libname;
		StringBuilder b = new StringBuilder();
		String sname = String.format("%s_%03d", libname, endnote.getId());
		String shref = String.format("#%s_%03d", libhref, endnote.getId());
		String text = String.format("[%d]", idx);
		b.append(Html.intoA(sname, shref, text, " class=\"comment\""));
		return b.toString();
	}

}
