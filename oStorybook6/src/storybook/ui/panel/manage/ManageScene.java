/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

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
package storybook.ui.panel.manage;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.accessibility.Accessible;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import resources.icons.ICONS;
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
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panel.AbstractScenePanel;

@SuppressWarnings("serial")
public class ManageScene extends AbstractScenePanel
		implements Accessible, FocusListener, IRefreshable {

	private static final String TT = "ManageScene.";

	public enum TYPE {
		SCENE, UNASSIGNED, BEGIN, NEXT, MAKE_UNASSIGNED
	}
	public TYPE type = TYPE.SCENE;
	public Border borderDefault;
	public ManagePanel manage;
	public ManageChapter manageChapter;
	private StatusButton btStatus;
	private StrandButton btStrand;
	public Color bkcolor;

	public ManageScene(ManageChapter manageChapter, Scene scene) {
		this(manageChapter, scene, TYPE.SCENE);
	}

	public ManageScene(ManageChapter manageChapter, Scene scene, TYPE type) {
		super(manageChapter.mainFrame, scene);
		this.manage = manageChapter.manage;
		this.manageChapter = manageChapter;
		this.type = type;
		initAll();
		setFocusable(true);
		setPanelName();
	}

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

	public Chapter getChapter() {
		return manageChapter.getChapter();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void init() {
		//empty
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+"initUi()");
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1)));
		setOpaque(true);
		borderDefault = BorderFactory.createEmptyBorder();
		bkcolor = this.getBackground();
		switch (type) {
			case MAKE_UNASSIGNED:
				setMinimumSize(manage.sceneSize);
				break;
			case BEGIN:
			case NEXT:
				if (!App.preferences.manageGetVertical()) {
					SwingUtil.setForcedSize(this, new Dimension(FontUtil.getWidth(), manage.sceneSize.height));
				} else {
					SwingUtil.setForcedSize(this, new Dimension(manage.sceneSize.width, FontUtil.getHeight() / 2));
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
				SwingUtil.setForcedSize(this, manage.sceneSize);
				break;
			default:
				break;
		}
	}

	private void initPanel() {
		//LOG.trace(TT + "initPanel()");
		setBackground(scene.getIntensityColor());
		add(initLeft(), MIG.GROWY);
		String txt = TextUtil.ellipsize("<b>" + scene.getName() + "</b>:<br>"
				+ Html.htmlToText(scene.getSummary()),
				manage.textLen);
		JLabel lbScene = new JLabel(Html.HTML_B + txt + Html.HTML_E);
		lbScene.setVerticalAlignment(SwingConstants.TOP);
		if (ColorUtil.isDark(getBackground())) {
			lbScene.setForeground(Color.WHITE);
		}
		SwingUtil.setForcedSize(lbScene,
				new Dimension(manage.sceneSize.width - manage.leftSize.width,
						manage.sceneSize.height - 6));
		add(lbScene, MIG.get(MIG.TOP, MIG.GROWX));
		addFocusListener(this);
	}

	private JPanel initLeft() {
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.INS1, MIG.GAP0, MIG.WRAP), "[center]"));
		panel.setOpaque(false);
		panel.add(btStatus = new StatusButton(scene.getStatus(), manage.iconSize, this));
		panel.add(btStrand = new StrandButton(scene.getStrand(), manage.iconSize, this));
		JLabel lbInformational = new JLabel("");
		if (scene.getInformative()) {
			lbInformational.setIcon(IconUtil.getIconSmall(ICONS.K.INFO));
		} else {
			lbInformational.setIcon(IconUtil.getIconSmall(ICONS.K.EMPTY));
		}
		panel.add(lbInformational, MIG.CENTER);
		return panel;
	}

	public TYPE getType() {
		return type;
	}

	@Override
	public void focusGained(FocusEvent e) {
		//LOG.printInfos(TT + ".focusGained(e=...)");
		if (scene != null) {
			mainFrame.getBookController().infoSetTo(scene);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			if (bt.getName().equals("btStatus")) {
				statusMenu();
			} else if (bt.getName().equals("btStrand")) {
				strandMenu();
			}
			manage.sceneSelect(this);
		}
	}

	private void statusMenu() {
		JPopupMenu menu = new JPopupMenu();
		for (STATUS st : STATUS.values()) {
			JMenuItem it = new JMenuItem(st.getLabel());
			it.setIcon(st.getIcon());
			it.addActionListener(a -> statusChange(st));
			menu.add(it);
		}
		menu.show(this, btStatus.getX(), btStatus.getY());
	}

	private void statusChange(STATUS st) {
		scene.setStatus(st.ordinal());
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

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

	private void strandChange(Strand st) {
		scene.setStrand(st);
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	public void resetBackground() {
		this.setBackground(bkcolor);
	}

	private JPopupMenu setPopupmenu() {
		JPopupMenu popup = EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO);
		if (scene.hasChapter()) {
			JMenuItem item = new JMenuItem(I18N.getMsg("scene.make_unassigned"));
			item.addActionListener(e -> manage.sceneSetUnassigned(this));
			popup.insert(item, 0);
		}
		return popup;
	}

}
