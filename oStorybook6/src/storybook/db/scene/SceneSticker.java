/*
 * Copyright (C) 2025 favdb
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
package storybook.db.scene;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.status.Status;
import storybook.db.status.StatusButton;
import storybook.db.strand.Strand;
import storybook.db.strand.StrandButton;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.EntityLinksPanel;
import storybook.ui.panel.chrono.Chrono;
import storybook.ui.panel.manage.Manage;

/**
 * class for the scene sticker
 * <ul>
 * <li>shorter mode has only title, left icons for status, main strand and informational</li>
 * <li>normal mode contains :
 * <ul>
 * <li>left: icons for status, main strand and informational</li>
 * <li>top first line: short synthetic panel for links</li>
 * <li>center second line: title</li>
 * </ul>
 * </li>
 * </ul>
 * <b>Note:</b>The default size is width=icon size * 5, height=icon size * 5
 *
 * @author favdb
 */
public class SceneSticker extends AbstractPanel implements MouseListener {

	private static final String TT = "SceneSticker.";

	public static final String LB_TITLE = "lbTitle",
			CN_PANEL = "scenePanel";
	// caller Chrono or Manage
	public Object caller;
	public Scene scene;
	private final Integer zoom;

	//basics computed sizes
	private int iconSize, fontSize;

	//the scene dimension
	private Dimension sceneSize;

	//the title
	public JLabel title;

	//the left panel buttons
	private StatusButton btStatus;
	private StrandButton btStrand;
	private IconButton btInformative;
	public Color bkcolor;
	private JPopupMenu popup;

	public static int getFontSize(int zoom) {
		return Math.max(6, Math.min(28, (int) (FontUtil.getDefault().getSize() * (zoom / 5.0))));
	}

	public static int getIconSize(int zoom) {
		return Math.max(8, Math.min(64, (int) (IconUtil.getDefSize() * (zoom / 5.0)))) + 2;
	}

	/**
	 * get the default size, width is 5 * icon size, height is 3 * icon size (icon size is default
	 * icon size modulo zoom)
	 *
	 * @param zoom
	 * @return
	 */
	public static Dimension getDefaultSize(int zoom) {
		int w = (getIconSize(zoom) * 6) + getDefaultLeft(zoom).width;
		int h = (getIconSize(zoom) * 3) + getFontSize(zoom);
		return new Dimension(w, h);
	}

	/**
	 * get the default left panel size, width is 1 * icon size, height is 3 * icon size (icon size
	 * is default icon size modulo zoom)
	 *
	 * @param zoom
	 * @return
	 */
	public static Dimension getDefaultLeft(int zoom) {
		int w = getIconSize(zoom) * 1;
		int h = getIconSize(zoom) * 3;
		return new Dimension(w, h);
	}

	/**
	 * scene sticker with a zoom size for chrono
	 *
	 * @param chrono
	 * @param scene
	 * @param zoom
	 */
	public SceneSticker(Chrono chrono, Scene scene, int zoom) {
		super(chrono.getMainFrame());
		this.caller = chrono;
		this.scene = scene;
		this.zoom = zoom;
		initAll();
	}

	/**
	 * scene sticker with a zoom size for manage
	 *
	 * @param manage
	 * @param scene
	 * @param zoom
	 */
	public SceneSticker(Manage manage, Scene scene, int zoom) {
		super(manage.getMainFrame());
		this.caller = manage;
		this.scene = scene;
		this.zoom = zoom;
		initAll();
	}

	public Scene getScene() {
		return scene;
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
		//LOG.trace(TT + "init() for scene=" + LOG.trace(scene));
		bkcolor = this.getBackground();
		popup = EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO);
		iconSize = getIconSize(zoom);
		fontSize = getFontSize(zoom);
		sceneSize = getDefaultSize(zoom);
	}

	/**
	 * initialize user interface
	 */
	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi() scene=" + LOG.trace(scene));
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1)));
		if (scene != null) {
			setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO));
			setBackground(scene.getIntensityColor());
			add(initLeft(), "dock west");
			initPanel();
			SwingUtil.setFixedSize(this, sceneSize);
			setInfoIcon();
			this.setToolTipText(getTooltip());
		}
		setSelected(false);
	}

	/**
	 * JPanel for the scene
	 *
	 * @return
	 */
	private void initPanel() {
		//LOG.trace(TT + "initPanel()");
		// scene title
		int len = ((sceneSize.width - getDefaultLeft(zoom).width) * 3) / fontSize;
		title = new JLabel();
		title.setOpaque(false);
		title.setText(Html.intoHTML(TextUtil.ellipsize(scene.getName(), len)));
		title.setFont(title.getFont().deriveFont(0, fontSize));
		SwingUtil.setFixedSize(title,
				new Dimension(sceneSize.width - getDefaultLeft(zoom).width - 2, fontSize * 3));
		title.addMouseListener(this);
		title.setComponentPopupMenu(popup);
		add(title, MIG.get(MIG.TOP, MIG.GROW));
		title.setToolTipText(getTooltip());
		add(initLinks(), "dock south");
	}

	private String getTooltip() {
		StringBuilder b = new StringBuilder("<html><body><p>");
		if (scene.hasChapter()) {
			if (scene.getChapter().hasPart() && book != null && !book.project.parts.getList().isEmpty()) {
				b.append(Html.intoB(I18N.getColonMsg("part")))
						.append(scene.getChapter().getPart().toString());
				b.append("<br>");
			}
			b.append(Html.intoB(I18N.getColonMsg("chapter")))
					.append(scene.getChapter().toString())
					.append("<br>");
		}
		b.append(Html.intoB(I18N.getColonMsg("scene")))
				.append(scene.getName()).append("<br>");
		//the links
		b.append(EntityUtil.getNamesToHtml("persons", scene.getPersons()));
		b.append(EntityUtil.getNamesToHtml("locations", scene.getLocations()));
		b.append(EntityUtil.getNamesToHtml("items", scene.getItems()));
		b.append(EntityUtil.getNamesToHtml("strands", scene.getStrands()));
		b.append(EntityUtil.getNamesToHtml("plots", scene.getPlots()));
		b.append("</p></body></html>");
		return b.toString();
	}

	/**
	 * left JPanel for status, main strand and informational
	 *
	 * @return
	 */
	private JPanel initLeft() {
		//LOG.trace(TT + "initLeft()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1)));
		p.setOpaque(false);
		p.addMouseListener(this);
		p.add(btStatus = new StatusButton(scene.getStatus(), iconSize, e -> statusMenu()));
		p.add(btStrand = new StrandButton(scene.getStrand(), iconSize, e -> strandMenu()));
		p.add(btInformative
				= new IconButton("btInformative", ICONS.K.EMPTY, iconSize, e -> setInformative()));
		btInformative.setToolTipText(I18N.getMsg("informative.tip"));
		p.setMinimumSize(new Dimension(iconSize, sceneSize.height));
		p.setToolTipText(getTooltip());
		return p;
	}

	/**
	 * initalize the links JPanel
	 *
	 * @return
	 */
	private JPanel initLinks() {
		//LOG.trace(TT + "initLinks()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1)));
		p.setOpaque(false);
		p.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		p.setToolTipText(getTooltip());
		p.addMouseListener(this);
		Dimension d = new Dimension(iconSize, iconSize);
		p.add(initLink(Book.TYPE.PERSON, d));
		p.add(initLink(Book.TYPE.LOCATION, d));
		p.add(initLink(Book.TYPE.ITEM, d));
		p.add(initLink(Book.TYPE.STRAND, d));
		p.add(initLink(Book.TYPE.PLOT, d));
		return p;
	}

	/**
	 * initialize one link
	 *
	 * @param type type of entity links
	 * @param dim maximum/preferred dimension
	 */
	private EntityLinksPanel initLink(Book.TYPE type, Dimension dim) {
		//LOG.trace(TT + "initLink(type=" + type.toString() + ", dim=" + dim.toString() + ")");
		EntityLinksPanel p = new EntityLinksPanel(mainFrame, scene, type, iconSize);
		SwingUtil.setMaxPreferredSize(p, dim);
		return p;
	}

	/**
	 * popup menu for status
	 */
	private void statusMenu() {
		JPopupMenu menu = new JPopupMenu();
		for (Status.STATUS st : Status.STATUS.values()) {
			JMenuItem it = new JMenuItem(st.getLabel());
			it.setIcon(st.getIcon());
			it.addActionListener(a -> statusChange(st));
			menu.add(it);
		}
		menu.show(this, btStatus.getX(), btStatus.getY());
	}

	/**
	 * change status value
	 *
	 * @param st
	 */
	private void statusChange(Status.STATUS st) {
		scene.setStatus(st.ordinal());
		changeSelected();
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * popup menu for changing strand
	 */
	private void strandMenu() {
		JPopupMenu menu = new JPopupMenu();
		@SuppressWarnings("unchecked")
		List<Strand> strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		for (Strand st : strands) {
			JMenuItem it = new JMenuItem(st.getName());
			it.setIcon(st.getColorIcon());
			it.addActionListener(a -> strandChange(st));
			menu.add(it);
		}
		menu.show(this, btStrand.getX(), btStrand.getY());
	}

	/**
	 * change the main Strand value
	 *
	 * @param st
	 */
	private void strandChange(Strand st) {
		scene.setStrand(st);
		changeSelected();
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * change the informative
	 */
	private void setInformative() {
		scene.setInformative(!scene.getInformative());
		setInfoIcon();
		changeSelected();
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * set the informative icon
	 */
	private void setInfoIcon() {
		ColorIcon noinfo = new ColorIcon(scene.getIntensityColor(), iconSize);
		btInformative.setIcon(!scene.getInformative() ? noinfo : IconUtil.getIcon(ICONS.K.INFO, iconSize));
	}

	/**
	 * refresh the SceneSticker
	 */
	@Override
	public void refresh() {
		//LOG.trace(TT + "refresh() scene=" + LOG.trace(scene));
		title.setText(scene.getName());
		title.setToolTipText(getTooltip());
	}

	/**
	 * reset the background color
	 */
	public void resetBackground() {
		setBackground(scene == null ? bkcolor : scene.getIntensityColor());
	}

	/**
	 * show the selected Scene by drawing the border
	 *
	 * @param b : true is selected, false to remove the selected border
	 */
	public void setSelected(boolean b) {
		//LOG.trace(TT + "setSelected(b=" + (b ? "true" : "false") + ") for scene=" + LOG.trace(scene));
		Color color = (!b
				? Color.LIGHT_GRAY//UIManager.getColor("Panel.background")
				: UIManager.getColor("List.selectionBackground"));
		setBorder(BorderFactory.createLineBorder(color, 2));
	}

	private void changeSelected() {
		if (caller != null) {
			if (caller instanceof Chrono) {
				((Chrono) caller).setSelected(this);
			} else if (caller instanceof Manage) {
				((Manage) caller).sceneSelect(getScene());
			}
		}
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	//// mouse listener actions ///////////////////
	/**
	 * mous clicked
	 *
	 * @param evt
	 */
	@Override
	public void mouseClicked(MouseEvent evt) {
		//LOG.trace(TT + "mouseClicked(evt="evt.toString()+")");
		if (scene == null) {
			return;
		}
		changeSelected();
		if (evt.getClickCount() == 2) {
			mainFrame.showEditorAsDialog(scene);
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
