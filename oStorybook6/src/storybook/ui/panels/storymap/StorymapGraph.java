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
package storybook.ui.panels.storymap;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.db.strand.Strand;
import storybook.tools.TextUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.Draw;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.panels.AbstractPanel;

/**
 * graphic part for Storymap
 *
 * @author favdb
 */
public class StorymapGraph extends AbstractPanel implements ActionListener, MouseListener {

	private static final String TT = "StorymapGraph";

	public List<Strand> strands = new ArrayList<>();
	private ImageIcon iStrand, iScene;
	public int iconW, iconH, charW, charH, indentW, indentH, gapX, gapY;
	private Color color;
	private int decalage;
	private List<JCheckBox> tbEntities = new ArrayList<>();
	private final Storymap storymap;
	private List<MDATA> nodes;
	private boolean typeScene;
	private int zoom;
	private boolean horizontal;
	private int szStrand;
	private boolean withTitle;

	public StorymapGraph(Storymap storymap) {
		super(storymap.mainFrame);
		this.storymap = storymap;
		initAll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void init() {
		//LOG.trace(TT + ".init()");
		zoom = storymap.getZoom();
		withTitle = storymap.getCkTitle();
		horizontal = storymap.getHorizontal();
		typeScene = (storymap.getTypes() == 0);
		tbEntities = new ArrayList<>();
		// char width, height
		FontMetrics fm = this.getFontMetrics(this.getFont());
		charW = (fm.charWidth('W') + fm.charWidth('I') + fm.charWidth('E')) / 3;
		charH = fm.getHeight();
		// indent width and height
		indentW = charW * 15;
		indentH = charH * 5;
		// icons definition
		iStrand = IconUtil.getImageIcon(ICONS.K.ENT_STRAND, 8 + (zoom * 8));
		iScene = IconUtil.getImageIcon((typeScene ? ICONS.K.ENT_SCENE : ICONS.K.ENT_CHAPTER), 8 + (zoom * 8));
		// icon width and height
		iconW = iStrand.getIconWidth();
		iconH = iStrand.getIconHeight();
		// horizontal and vertical gap for position of icon
		gapX = iconW / 2;
		gapY = iconH / 3;
		strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		if (typeScene) {
			nodes = getScenes();
		} else {
			nodes = getChapters();
		}
		szStrand = (charW * 4) * zoom;
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.INS0)));
		if (LaF.isDark()) {
			this.setForeground(ColorUtil.darker(this.getForeground(), 0));
		} else {
			setBackground(SwingUtil.getBackgroundColor());
		}
		if (getComponents().length > 0) {
			removeAll();
		}
		// draw the Strand check boxes
		Point p;
		for (int i = 0; i < strands.size(); i++) {
			Strand strand = strands.get(i);
			JCheckBox tb = new JCheckBox();
			tb.setName("strand_" + i);
			tb.setText(TextUtil.ellipsize(strand.getAbbr(), 12));
			tb.setToolTipText(strand.getName());
			tb.setSelected(true);
			tb.setOpaque(true);
			tb.setBackground(strand.getJColor());
			tb.setForeground(ColorUtil.isDark(strand.getJColor()) ? Color.WHITE : Color.BLACK);
			tb.setBorder(BorderFactory.createEmptyBorder());
			tb.setRolloverEnabled(false);
			tb.addActionListener(this);
			tbEntities.add(tb);
			p = strandToPoint(i);
			if (horizontal) {
			} else {
				p.y -= iconH;
				tb.setMinimumSize(new Dimension(szStrand, charH));
				tb.setMaximumSize(new Dimension(szStrand, charH));
			}
			add(tb, MIG.getPos(p.x, p.y));
		}
		//draw the node icons
		decalage = 0;
		for (int i = 0; i < nodes.size(); i++) {
			MDATA node = nodes.get(i);
			JLabel lb = new JLabel(iScene);
			lb.setName("node_" + node.getId());
			lb.setBackground(this.getBackground());
			lb.setOpaque(false);
			if (withTitle) {
				lb.setText(TextUtil.ellipsize(node.getName(), 15));
				lb.setIconTextGap(1);
				if (horizontal) {
					lb.setVerticalTextPosition(SwingConstants.BOTTOM);
					lb.setHorizontalAlignment(SwingConstants.LEFT);
				}
			}
			Scene sc = (Scene) mainFrame.project.get(Book.TYPE.SCENE, node.id);
			if (sc != null) {
				lb.setToolTipText(EntityUtil.getTooltip(sc));
			}
			lb.addMouseListener(this);
			p = getP0(node);
			if (horizontal) {
				p.x = p.x + (gapX / 2);
				p.y = p.y - gapY;
			} else {
				p.x = p.x - gapX;
				p.y = p.y + gapY;
			}
			add(lb, MIG.getPos(p));
			if (horizontal) {
				//add(new JLabel(TextUtil.ellipsize(node.getName(), 15)), MIG.getPos(p.x, p.y - charH));
			}
		}
		validate();
	}

	/**
	 * get all MData for chapters
	 *
	 * @return a list of MData
	 */
	private List<MDATA> getChapters() {
		List<MDATA> cs = new ArrayList<>();
		List<Chapter> chapters = mainFrame.project.chapters.find((Part) null);
		int n = 1;
		for (Chapter chapter : chapters) {
			List<Scene> scs = mainFrame.project.scenes.find((Chapter) chapter);
			if (scs.isEmpty()) {
				continue;
			}
			Strand strand = scs.get(0).getStrand();
			List<Strand> ls = new ArrayList<>();
			for (Scene sc : scs) {
				ls.add(sc.getStrand());
				ls.addAll(sc.getStrands());
			}
			Collections.sort(ls, Strand::compareTo);
			ls.remove(strand);
			cs.add(new MDATA(chapter.getId(), n++, chapter.getName(), strand, ls));
		}
		return cs;
	}

	/**
	 * get all MData for scenes
	 *
	 * @return a List of MData
	 */
	@SuppressWarnings("unchecked")
	private List<MDATA> getScenes() {
		List<MDATA> cs = new ArrayList<>();
		int n = 1;
		for (Scene scene : Scenes.find(mainFrame)) {
			cs.add(new MDATA(scene.getId(), n++, scene.getName(), scene.getStrand(), scene.getStrands()));
		}
		return cs;
	}

	/**
	 * get the strand Point the y value is allway 0 for vertical, else x is 0 for horizontal
	 *
	 * @param sx number of the strand in the strands list
	 * @return the Point
	 */
	private Point strandToPoint(int sx) {
		//LOG.printInfos(TT + ".getPoint(sx=" + sx + ", sy=" + sy + ")");
		if (horizontal) {
			return new Point(0, (sx * (iconH + charH)) + (charH));
		}
		return new Point(sx * szStrand, iconH);
	}

	/**
	 * get Point zero from strand and scene number P0 is the original point for label
	 *
	 * @param node
	 * @return the Point
	 */
	private Point getP0(MDATA node) {
		//LOG.printInfos(TT + ".getPoint(sx=" + sx + ", sy=" + sy + ")");
		Point px = strandToPoint(strands.indexOf(node.strand));
		int n = nodes.indexOf(node);
		if (horizontal) {
			return new Point((charW * 15) + (n * (iconW * 2)), px.y /*+ iconH */ + (iconH / 2));
		} else {
			return new Point(px.x + (charW * 3), (charH * 2) + (n * (iconH * 2)));
		}
	}

	/**
	 * get Point 1 (starting point) from node P1 is the origin point for linking a node
	 *
	 * @param node
	 * @return the Point
	 */
	private Point getP1(MDATA node) {
		//LOG.trace(TT + ".getPoint(sx=" + sx + ", sy=" + sy + ")");
		Point p = getP0(node);
		if (horizontal) {
			return new Point(p.x - (iconH / 2), p.y /*+ (iconH / 2)*/);
		} else {
			return new Point(p.x, p.y - (iconH / 3));
		}
	}

	/**
	 * get Point 2 (center of icon) for node
	 *
	 * P2 is the center point of the node
	 *
	 * @param node
	 * @return the Point
	 */
	private Point getP2(MDATA node) {
		//LOG.trace(TT + ".getPoint(sx=" + sx + ", sy=" + sy + ")");
		Point p = getP0(node);
		if (horizontal) {
			return new Point(p.x + (iconH / 2), p.y/* + (iconH / 2)*/);
		} else {
			return new Point(p.x, p.y + (iconH / 2));
		}
	}

	/**
	 * get Point 3 (end icon) for node
	 *
	 * P3 is the end point of the node
	 *
	 * @param node
	 * @return the Point
	 */
	private Point getP3(MDATA node) {
		//LOG.trace(TT + ".getPoint(sx=" + sx + ", sy=" + sy + ")");
		Point p = getP0(node);
		if (horizontal) {
			return new Point(p.x + iconH, p.y);
		} else {
			return new Point(p.x, p.y + iconH + (iconH / 3));
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//LOG.trace(TT + ".actionPerformed(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JCheckBox) {
			refresh();
		}
	}

	/**
	 * paint component
	 *
	 * @param g
	 */
	@Override
	public void paintComponent(Graphics g) {
		//LOG.trace(TT + ".paintComponent(g) for strands nb=" + strands.size() + " and nodes nb=" + nodes.size());
		g.setFont(this.getFont());
		super.paintComponent(g);
		if (strands.isEmpty()) {
			return;
		}
		// draw the links
		decalage = -2;
		for (Strand strand : strands) {
			if (!isShowStrand(strand) || findMData(strand).isEmpty()) {
				continue;
			}
			color = strand.getJColor();
			Point p1 = strandToPoint(strands.indexOf(strand)), p0;
			if (horizontal) {
				p1.y = p1.y + (charH / 2);
				p0 = new Point((charH * 7), p1.y);
				Draw.Hline(g, addDecalage(p1), addDecalage(p0), color, zoom, gapY);
			} else {
				p1.x = p1.x + (charW * 3);
				p1.y = 0;
				p0 = new Point(p1.x, charH + (charH / 2));
				Draw.Vline(g, addDecalage(p1), addDecalage(p0), color, zoom, gapX);
			}
			p1.x = p0.x;
			p1.y = p0.y;
			MDATA lastNode = null;
			int nid = 1;
			for (MDATA node : findMData(strand)) {
				//LOG.printInfos(TT + ".paint node.getNid=" + node.getNid());
				Point pp1 = getP1(node);
				Point pp2 = getP2(node);
				Point pp3 = getP3(node);
				if (lastNode != null && checkNode(lastNode, node)) {
					if (horizontal) {
						Draw.Hgap(g, addDecalage(p1), addDecalage(pp2), color, zoom, gapY, gapX);
						p1.x = pp1.x;
						p1.y = pp1.y;
					} else {
						Draw.Vgap(g, addDecalage(p1), addDecalage(pp2), color, zoom, gapX, gapY);
						p1.x = pp1.x;
						p1.y = pp1.y;
					}
				} else {
					if (horizontal) {
						if (lastNode == null) {
							Point ppx1 = new Point(pp1.x, p1.y);
							Draw.Hline(g, addDecalage(p1), addDecalage(ppx1), color, zoom, gapY);
							p1.x = ppx1.x;
							p1.y = ppx1.y;
						}
						Draw.Hline(g, addDecalage(p1), addDecalage(pp2), color, zoom, gapX);
					} else {
						if (lastNode == null) {
							Point ppx1 = new Point(p1.x, pp1.y);
							Draw.Vline(g, addDecalage(p1), addDecalage(ppx1), color, zoom, gapY);
							p1.x = ppx1.x;
							p1.y = ppx1.y;
						}
						Draw.Vline(g, addDecalage(p1), addDecalage(pp2), color, zoom, gapY);
					}
				}
				lastNode = node;
				p1.x = pp3.x;
				p1.y = pp3.y;
			}
			decalage++;
		}
	}

	private boolean checkNode(MDATA n1, MDATA n2) {
		int i1 = nodes.indexOf(n1), i2 = nodes.indexOf(n2);
		for (int i = i1 + 1; i < i2; i++) {
			if (nodes.get(i).getStrand().equals(nodes.get(i2).getStrand())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * add a decalage to a Point decalage is applyed for x and y
	 *
	 * @param p: the point to increase
	 * @return
	 */
	private Point addDecalage(Point p) {
		return new Point(p.x + decalage, p.y + decalage);
	}

	/**
	 * find the scene at the given point
	 *
	 * @param p
	 * @return
	 */
	private Long findId(Point p) {
		for (MDATA node : nodes) {
			Point ps = getP0(node);
			Rectangle r = new Rectangle(ps.x, ps.y, ps.x + iconW, ps.y + iconH);
			if (r.contains(p)) {
				return node.id;
			}
		}
		return -1L;
	}

	/**
	 * get the entity Id from a label
	 *
	 * @param lb: label to find
	 * @return
	 */
	private Long getIdFromLabel(JLabel lb) {
		if (!lb.getName().contains("_")) {
			return -1L;
		}
		return Long.valueOf(lb.getName().split("_")[1]);
	}

	/**
	 * get entity from a JLabel
	 *
	 * @param lb: th label to find
	 * @return the entity
	 */
	private AbstractEntity getEntityFromLabel(JLabel lb) {
		Long id = getIdFromLabel(lb);
		if (id == -1L) {
			return null;
		}
		return mainFrame.project.get((typeScene ? Book.TYPE.SCENE : Book.TYPE.CHAPTER), id);
	}

	/**
	 * get the entity from mouse event
	 *
	 * @param evt: mouse event
	 * @return the entity, null if no entity found
	 */
	private AbstractEntity getEntity(MouseEvent evt) {
		AbstractEntity entity = null;
		if (evt.getSource() instanceof JLabel) {
			entity = getEntityFromLabel((JLabel) evt.getSource());
		} else {
			Long id = findId(evt.getPoint());
			if (id != -1L) {
				entity = mainFrame.project.get((typeScene ? Book.TYPE.SCENE : Book.TYPE.CHAPTER), id);
			}
		}
		return entity;
	}

	/**
	 * find MData for strand
	 *
	 * @param strand: strand to find
	 * @return a List of MData
	 */
	private List<MDATA> findMData(Strand strand) {
		List<MDATA> md = new ArrayList<>();
		for (MDATA node : nodes) {
			if (node.strand.equals(strand) || node.strands.contains(strand)) {
				md.add(node);
			}
		}
		return md;
	}

	/**
	 * Mouse listener
	 *
	 * @param evt: the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent evt) {
		if (evt.getClickCount() == 2) {
			AbstractEntity entity = null;
			if (evt.getSource() instanceof JLabel) {
				entity = getEntityFromLabel((JLabel) evt.getSource());
			} else {
				Long id = findId(evt.getPoint());
				if (id == -1L) {
					return;
				}
			}
			if (entity != null) {
				mainFrame.showEditorAsDialog(entity);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			AbstractEntity entity = getEntity(evt);
			if (entity != null) {
				JPopupMenu pop = EntityUtil.createPopupMenu(mainFrame, entity, false);
				Point p = evt.getPoint();
				pop.show(this, p.x, p.y);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		// nothing
	}

	@Override
	public void mouseEntered(MouseEvent evt) {
		// nothing
	}

	@Override
	public void mouseExited(MouseEvent evt) {
		// nothing
	}

	private boolean isShowStrand(Strand strand) {
		int i = strands.indexOf(strand);
		return tbEntities.get(i).isSelected();
	}

	public List<SDATA> getSelected() {
		//LOG.trace(TT + ".getSelected()");
		List<SDATA> r = new ArrayList<>();
		for (int i = 0; i < tbEntities.size(); i++) {
			r.add(new SDATA(strands.get(i).getId(), tbEntities.get(i).isSelected()));
		}
		return r;
	}

	private void setSelected(List<SDATA> r) {
		//LOG.trace(TT + ".setSelected(r=" + ListUtil.join(r, ";") + ")");
		for (int i = 0; i < r.size(); i++) {
			for (int j = 0; j < strands.size(); j++) {
				if (strands.get(j).getId().equals(r.get(i).id)) {
					tbEntities.get(j).setSelected(r.get(i).check);
					break;
				}
			}
		}
	}

	@Override
	public void refresh() {
		List<SDATA> r = getSelected();
		removeAll();
		super.refresh();
		setSelected(r);
		repaint();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// nothing
	}

	private static class MDATA {

		public final Long id;
		public final String name;
		public final Strand strand;
		public final List<Strand> strands;
		private final int nid;

		public MDATA(Long id, int n, String name, Strand strand, List<Strand> strands) {
			this.id = id;
			this.nid = n;
			this.name = name;
			this.strand = strand;
			this.strands = strands;
		}

		public Strand getStrand() {
			return strand;
		}

		public List<Strand> getStrandS() {
			return strands;
		}

		public Long getId() {
			return id;
		}

		public int getNid() {
			return nid;
		}

		public String getName() {
			return name;
		}
	}

	public static class SDATA {

		public final long id;
		public final boolean check;

		public SDATA(Long id, boolean check) {
			this.id = id;
			this.check = check;
		}
	}

}
