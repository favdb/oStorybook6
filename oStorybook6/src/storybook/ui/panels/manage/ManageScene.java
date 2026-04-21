/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2025 favdb

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
package storybook.ui.panels.manage;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.db.status.Status.STATUS;
import storybook.db.status.StatusButton;
import storybook.db.strand.Strand;
import storybook.db.strand.StrandButton;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panels.AbstractPanel;
import storybook.ui.panels.EntityLinksPanel;

/**
 * class for showing a Scene in the Manage view
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class ManageScene extends AbstractPanel implements IRefreshable {

	private static final String TT = "ManageScene.";

	public enum TYPE {
		SCENE, UNASSIGNED, BEGIN, AFTER, MAKE_UNASSIGNED
	}
	public JLabel title;
	public TYPE type = TYPE.SCENE;
	public Border borderDefault;
	public Manage manage;
	public ManageChapter manageChapter;
	public Scene scene;
	private StatusButton btStatus;
	private StrandButton btStrand;
	private IconButton btInformative;
	public Color bkcolor;

	/**
	 * new default class
	 *
	 * @param manageChapter
	 * @param scene
	 */
	public ManageScene(ManageChapter manageChapter, Scene scene) {
		this(manageChapter, scene, TYPE.SCENE);
	}

	/**
	 * new complete class
	 *
	 * @param manageChapter
	 * @param scene
	 * @param type
	 */
	public ManageScene(ManageChapter manageChapter, Scene scene, TYPE type) {
		super(manageChapter.mainFrame);
		this.manage = manageChapter.manage;
		this.manageChapter = manageChapter;
		this.scene = scene;
		this.type = type;
		initAll();
		setFocusable(true);
		setPanelName();
	}

	/**
	 * set the panel name
	 */
	private void setPanelName() {
		if (scene != null) {
			setName(type.name() + "_" + scene.getCCSS());
		} else {
			if (manageChapter.getChapter() != null) {
				setName(type.name() + "_" + manageChapter.getChapter().getId().toString());
			} else {
				setName(type.name() + "_00");
			}
		}
	}

	/**
	 * get the chapter
	 *
	 * @return
	 */
	public Chapter getChapter() {
		return manageChapter.getChapter();
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
		//LOG.trace(TT+"init()");
		bkcolor = this.getBackground();
	}

	/**
	 * initialize the user interface
	 */
	@Override
	public void initUi() {
		//LOG.trace(TT+"initUi()");
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1)));
		borderDefault = BorderFactory.createEmptyBorder();
		switch (type) {
			case MAKE_UNASSIGNED:
				SwingUtil.setFixedSize(this, new Dimension(FontUtil.getWidth(), manage.sceneSize.height));
				break;
			case BEGIN:
				setName("begin_" + manageChapter.chapter.getName());
			case AFTER:
				if (!App.preferences.manageGetVertical()) {
					SwingUtil.setFixedSize(this, new Dimension(FontUtil.getWidth(), manage.sceneSize.height));
				} else {
					SwingUtil.setFixedSize(this, new Dimension(manage.sceneSize.width, FontUtil.getHeight() / 2));
				}
				break;
			case UNASSIGNED:
			case SCENE:
				borderDefault = SwingUtil.getBorderDefault();
				setBorder(borderDefault);
				if (scene != null) {
					initPanel();
					setComponentPopupMenu(setPopupmenu());
				}
				SwingUtil.setFixedSize(this, manage.sceneSize);
				break;
			default:
				break;
		}
	}

	/**
	 * *
	 * initialize the panel
	 */
	private void initPanel() {
		//LOG.trace(TT + "initPanel()");
		setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO));
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1, MIG.WRAP1)));
		setBackground(scene.getIntensityColor());
		setToolTipText(getTooltip());
		add(initLeft(), "dock west");
		title = new JLabel();
		StringBuilder buf = new StringBuilder();
		String titleText = TextUtil.ellipsize(scene.getName(), manage.textLen);
		buf.append(Html.htmlToText(titleText));
		title.setText(Html.HTML_B + buf.toString() + Html.HTML_E);
		title.setVerticalAlignment(SwingConstants.TOP);
		title.setFont(title.getFont().deriveFont(0, manage.fontSize));
		if (ColorUtil.isDark(getBackground())) {
			title.setForeground(Color.WHITE);
		}
		title.setToolTipText(getToolTipText());
		SwingUtil.setFixedSize(title,
				new Dimension(manage.sceneSize.width - manage.leftSize.width,
						manage.fontSize * 4));
		add(title, MIG.get(MIG.TOP, MIG.GROWX));
		add(initLinks(), "dock south");
	}

	/**
	 * initalize the left panel
	 *
	 * @return
	 */
	private JPanel initLeft() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1), "[center]"));
		p.setOpaque(false);
		p.add(btStatus = new StatusButton(scene.getStatus(), manage.iconSize, e -> statusMenu()));
		p.add(btStrand = new StrandButton(scene.getStrand(), manage.iconSize, e -> strandMenu()));
		p.add(btInformative
				= new IconButton("btInformative", ICONS.K.EMPTY, manage.iconSize, e -> setInformative()));
		btInformative.setToolTipText(I18N.getMsg("informative.tip"));
		setInfoIcon();
		p.setMinimumSize(new Dimension(manage.iconSize, manage.sceneSize.height));
		p.setToolTipText(getTooltip());
		return p;
	}

	/**
	 * initialize the links JPanel
	 *
	 * @return
	 */
	private JPanel initLinks() {
		//LOG.trace(TT + "initLinks()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		p.setOpaque(false);
		p.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		p.setToolTipText(getTooltip());
		Dimension d = new Dimension(manage.iconSize, manage.iconSize);
		p.add(initLink(Book.TYPE.PERSON));
		p.add(initLink(Book.TYPE.LOCATION));
		p.add(initLink(Book.TYPE.ITEM));
		p.add(initLink(Book.TYPE.STRAND));
		p.add(initLink(Book.TYPE.PLOT));
		return p;
	}

	/**
	 * get the tooltip
	 *
	 * @return
	 */
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
	 * initialize one link
	 *
	 * @param type type of entity links
	 * @param dim maximum/preferred dimension
	 */
	private EntityLinksPanel initLink(Book.TYPE type) {
		//LOG.trace(TT + "initLink(type=" + type.toString() + ", dim=" + dim.toString() + ")");
		EntityLinksPanel p = new EntityLinksPanel(mainFrame, scene, type, manage.iconSize);
		return p;
	}

	/**
	 * get the type of the scene
	 *
	 * @return
	 */
	public TYPE getType() {
		return type;
	}

	/**
	 * get a menu for modifying status
	 */
	private void statusMenu() {
		manage.sceneSelect(this);
		JPopupMenu menu = new JPopupMenu();
		for (STATUS st : STATUS.values()) {
			JMenuItem it = new JMenuItem(st.getLabel());
			it.setIcon(st.getIcon());
			it.addActionListener(a -> statusChange(st));
			menu.add(it);
		}
		menu.show(this, btStatus.getX(), btStatus.getY());
	}

	/**
	 * action to change the status
	 *
	 * @param st
	 */
	private void statusChange(STATUS st) {
		scene.setStatus(st.ordinal());
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * get the menu to change the main Strand
	 */
	private void strandMenu() {
		manage.sceneSelect(this);
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
	 * action to change the Strand
	 *
	 * @param st
	 */
	private void strandChange(Strand st) {
		scene.setStrand(st);
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * reste the background color to the default
	 */
	public void resetBackground() {
		setBackground(scene == null || type != TYPE.SCENE ? bkcolor : scene.getIntensityColor());
	}

	/**
	 * set the popup menu
	 *
	 * @return
	 */
	private JPopupMenu setPopupmenu() {
		JPopupMenu popup = EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO);
		if (scene.hasChapter()) {
			JMenuItem item = new JMenuItem(I18N.getMsg("scene.make_unassigned"));
			item.addActionListener(e -> manage.sceneSetUnassigned(this));
			popup.insert(item, 0);
		}
		return popup;
	}

	/**
	 * change the informative
	 */
	private void setInformative() {
		scene.setInformative(!scene.getInformative());
		setInfoIcon();
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * set the informative icon
	 */
	private void setInfoIcon() {
		ColorIcon noinfo = new ColorIcon(scene.getIntensityColor(), manage.iconSize);
		btInformative.setIcon(!scene.getInformative()
				? noinfo : IconUtil.getIcon(ICONS.K.INFO, manage.iconSize));
	}

	//// common methods
	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT + "actionPerformed(e="+e.toString()+")");
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

}
