/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.dialog.cover;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import storybook.Const;
import storybook.db.book.Book;
import storybook.dialog.cover.Cover.GAPS;
import storybook.tools.LOG;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSTextArea;
import storybook.ui.MIG;

/**
 * JPanel to preview a cover
 *
 * @author favdb
 */
public class CoverPreview extends JPanel {

	private static final String TT = "CoverPreview.";

	//initial font size
	public int SZauthor = 12,
			SZtitle = 24,
			SZsubtitle = 18,
			SZfooter = 12;
	private final Cover creator;
	private BufferedImage image;
	private Book book;
	private JSTextArea lbTitle, lbSubtitle, lbAuthor, lbFooter;
	private JLabel lbImage;
	private Font tFont;
	private int align = 1, margin = 0;

	public CoverPreview(Cover creator, Book book) {
		this.creator = creator;
		this.book = book;
		initialize();
	}

	/**
	 * initialization
	 */
	private void initialize() {
		//LOG.trace(TT + "initialize()");
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		this.setBorder(BorderFactory.createEtchedBorder());
		Dimension dim = new Dimension(Cover.COVER_WIDTH, Cover.COVER_HEIGHT);
		SwingUtil.setFixedSize(this, dim);
		lbImage = new JLabel();
		lbImage.setHorizontalAlignment(JLabel.CENTER);
		SwingUtil.setFixedSize(lbImage, dim);

		lbAuthor = initData(book.getAuthor(), SZauthor, 1);
		lbTitle = initData(book.getTitle(), SZtitle, 5);
		SwingUtil.setFixedSize(lbTitle, new Dimension(Cover.COVER_WIDTH - 5, ((SZtitle + 2) * 5)));
		lbTitle.setAlignment(creator.titleGetAlign());
		tFont = lbTitle.getFont();
		lbSubtitle = initData(book.getSubtitle(), SZsubtitle, 2);
		lbTitle.setAlignment(creator.titleGetAlign());
		lbFooter = initData(Const.getName(), SZfooter, 1);

		// add the components to the panel
		int posx = 0, posy = 0;
		add(lbImage, "pos 0 " + posy);
		posy = creator.spinnerGetValue(GAPS.TOP);
		add(lbAuthor, "pos 0 " + posy);
		posy = creator.spinnerGetValue(GAPS.TITLE);
		add(lbTitle, "pos 0 " + posy);
		posy += creator.spinnerGetValue(GAPS.SUBTITLE);
		add(lbSubtitle, "pos 0 " + posy);
		posy = Cover.COVER_HEIGHT - creator.spinnerGetValue(GAPS.FOOTER) - SZfooter;
		add(lbFooter, "pos 0 " + posy);
		refresh();
	}

	/**
	 * initialize a JSTextArea
	 *
	 * @param text
	 * @param fntsz
	 * @param lines
	 * @return
	 */
	private JSTextArea initData(String text, int fntsz, int lines) {
		//LOG.trace("initData(text, fntsz=" + fntsz + ", lines=" + lines + ")");
		JSTextArea js = new JSTextArea(text);
		//js.setBorder(BorderFactory.createLineBorder(Color.red));
		js.setCharColor(creator.textColor, Color.BLACK, 2);
		Font fnt = new Font("Serif", Font.BOLD, fntsz);
		js.setFont(fnt);
		SwingUtil.setFixedSize(js, new Dimension(Cover.COVER_WIDTH - 5, ((fnt.getSize() + 2) * (lines))));
		return js;
	}

	/**
	 * set the background image
	 *
	 * @param img
	 */
	public void setBgImage(BufferedImage img) {
		this.image = img;
	}

	/**
	 * set the Book
	 *
	 * @param book
	 */
	public void setBook(Book book) {
		this.book = book;
	}

	/**
	 * painting the background
	 *
	 * @param g
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, 0, 0, null);
		} else {
			ColorIcon ic = new ColorIcon(Color.WHITE, getSize());
			g.drawImage(ic.toImage(), 0, 0, null);
		}
	}

	/**
	 * refresh the preview
	 */
	public void refresh() {
		//LOG.trace(TT + "refresh()");
		// set author
		if (creator.ckAuthor.isSelected() && !creator.ckText.isSelected()) {
			lbAuthor.setText(book.getAuthor());
		} else {
			lbAuthor.setText("");
		}

		//set title
		if (creator.ckTitle.isSelected() && !creator.ckText.isSelected()) {
			lbTitle.setText(book.getTitle());
		} else {
			lbTitle.setText("");
		}

		//set subtitle
		if (creator.ckSubtitle.isSelected() && !creator.ckText.isSelected()) {
			lbSubtitle.setText(book.getSubtitle());
		} else {
			lbSubtitle.setText("");
		}

		//set footer
		if (creator.ckFooter.isSelected() && !creator.ckText.isSelected()) {
			lbFooter.setText(Const.getName());
		} else {
			lbFooter.setText("");
		}
		lbFooter.setCharColor(creator.textColor, Color.BLACK, 2);

		// change font for title and subtitle
		lbTitle.setFont(tFont);
		Font f1 = lbSubtitle.getFont();
		Font f2 = new Font(tFont.getFamily(), tFont.getStyle(), SZsubtitle);
		lbSubtitle.setFont(f2);
		// set all locations
		setLocAll();
	}

	private void setLocAll() {
		//LOG.trace(TT+"setLocAll()");
		setLoc(lbAuthor, 0, GAPS.TOP);
		setLoc(lbTitle, 0, GAPS.TITLE);
		setLoc(lbSubtitle, creator.spinnerGetValue(GAPS.TITLE), GAPS.SUBTITLE);
		Point p = lbFooter.getLocation();
		p.y = Cover.COVER_HEIGHT - creator.spinnerGetValue(GAPS.FOOTER) - lbFooter.getFont().getSize();
		lbFooter.setLocation(p);
		repaint();
	}

	/**
	 * set a JsTextArea location and set the textColor, return next location
	 *
	 * @param js: the JsTextArea
	 * @param yy: start position
	 * @param gap: gap from yy
	 */
	public void setLoc(JSTextArea js, int yy, Cover.GAPS gap) {
		//LOG.trace(TT + "setLoc(js, yy=" + yy + ", gap=" + gap.name() + ")");
		js.setCharColor(creator.textColor, Color.BLACK, 2);
		Point p = js.getLocation();
		p.y = yy + creator.spinnerGetValue(gap);
		js.setLocation(p);
	}

	/**
	 * save the preview in the given file
	 *
	 * @param outfile
	 */
	public void getCover(File outfile) {
		//LOG.trace(TT+"getCover(outfile="+outfile.getAbsolutePath()+")");
		BufferedImage bi = new BufferedImage(this.getSize().width,
				this.getSize().height, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.createGraphics();
		this.paint(g);
		g.dispose();
		try {
			ImageIO.write(bi, "jpg", outfile);
		} catch (IOException e) {
			LOG.err(TT + "saveCover(outfile) error", e);
		}
	}

	/**
	 * set the font for the title and the subtitle
	 *
	 * @param font
	 */
	public void setTitleFont(Font font) {
		//LOG.trace(TT + "setTitleFont(fnt=[" + FontUtil.getString(font) + "])");
		tFont = font;
		lbTitle.setFont(font);
		lbTitle.repaint();
		Font f1 = lbSubtitle.getFont();
		Font f2 = new Font(tFont.getFamily(), tFont.getStyle(), SZsubtitle);
		lbSubtitle.setFont(f2);
		SwingUtilities.invokeLater(() -> {
			setLocAll();
		});
	}

	/**
	 * get the font of the title
	 *
	 * @return
	 */
	public Font getTitleFont() {
		return tFont;
	}

	void setNewLoc(GAPS gap) {
		//LOG.trace(TT + "setNewLoc(gap=" + (gap != null ? gap.toString() : "null") + ")");
		if (null != gap) {
			switch (gap) {
				case TOP:
					setLoc(lbAuthor, 0, GAPS.TOP);
					break;
				case TITLE:
					setLoc(lbTitle, 0, GAPS.TITLE);
					break;
				case SUBTITLE:
					setLoc(lbSubtitle, creator.spinnerGetValue(GAPS.TITLE), GAPS.SUBTITLE);
					break;
				case FOOTER:
					Point p = lbFooter.getLocation();
					p.y = Cover.COVER_HEIGHT - creator.spinnerGetValue(GAPS.FOOTER) - lbFooter.getFont().getSize();
					lbFooter.setLocation(p);
					break;
				default:
					break;
			}
		}
	}

	public void setAlign(int value) {
		align = value;
		if (value == 1) {
			margin = 0;
		} else {
			margin = creator.getMargin();
		}
		lbTitle.setAlignment(value);
		if (align == 1) {
			lbTitle.setMargins(0, 0);
		} else {
			lbTitle.setMargins((align == 0 ? margin : 0), (align == 2 ? margin : 0));
		}
		lbTitle.repaint();
		lbSubtitle.setAlignment(value);
		if (align == 1) {
			lbSubtitle.setMargins(0, 0);
		} else {
			lbSubtitle.setMargins((align == 0 ? margin : 0), (align == 2 ? margin : 0));
		}
		lbSubtitle.repaint();
	}

	public void setNewMargin(int value) {
		margin = value;
		lbTitle.setMargins((align == 0 ? margin : 0), (align == 2 ? margin : 0));
		lbTitle.repaint();
	}

}
