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
package storybook.ui.panel;

import api.mig.swing.MigLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.tools.ListUtil;
import storybook.tools.html.Html;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 * class for a panel containing links to Persons, Locations, Items, Strands, Tags, Plots
 *
 * @author favdb
 */
public class EntityLinksPanel extends AbstractPanel {

	private static final String TT = "EntityLinksPanel.";

	private final Scene scene;
	private final Book.TYPE type;
	private int iconSize;

	public EntityLinksPanel(MainFrame mainFrame, Scene scene, Book.TYPE type) {
		this(mainFrame, scene, type, IconUtil.getDefSize());
	}

	public EntityLinksPanel(MainFrame mainFrame, Scene scene, Book.TYPE type, int iconSize) {
		super(mainFrame);
		this.scene = scene;
		this.type = type;
		this.iconSize = iconSize;
		initAll();
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
	}

	/**
	 * initialize the user interface
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		setOpaque(false);
		//setBorder(SwingUtil.getBorderDot());
		Icon icon = IconUtil.getIconLarge(ICONS.K.UNKNOWN, iconSize);
		List list = new ArrayList<>();
		switch (type) {
			case ITEM:
				list = scene.getItems();
				icon = IconUtil.getIcon(ICONS.K.ENT_ITEM, iconSize);
				break;
			case LOCATION:
				list = scene.getLocations();
				icon = IconUtil.getIcon(ICONS.K.ENT_LOCATION, iconSize);
				break;
			case PERSON:
				list = scene.getPersons();
				icon = IconUtil.getIcon(ICONS.K.ENT_PERSON, iconSize);
				break;
			case PLOT:
				list = scene.getPlots();
				icon = IconUtil.getIcon(ICONS.K.ENT_PLOT, iconSize);
				break;
			case STRAND:
				list = scene.getStrands();
				icon = IconUtil.getIcon(ICONS.K.ENT_STRAND, iconSize);
				break;
			case TAG:
				list = scene.getTags();
				icon = IconUtil.getIcon(ICONS.K.ENT_TAG, iconSize);
				break;
		}
		if (list.isEmpty()) {
			add(new JLabel(IconUtil.getIcon(ICONS.K.EMPTY, iconSize)));
			return;
		}
		List<String> tooltip = new ArrayList<>();
		for (Object obj : list) {
			AbstractEntity entity = (AbstractEntity) obj;
			tooltip.add(entity.getName());
		}
		JLabel lb = new JLabel(icon);
		if (!tooltip.isEmpty()) {
			lb.setToolTipText(Html.intoHTML(ListUtil.join(tooltip)));
		}
		add(lb);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case SCENE:
				if (Ctrl.PROPS.UPDATE.check(propName)) {
					if (((Scene) newValue).getId().equals(scene.getId())) {
						refresh();
					}
				}
				break;
			case ITEM:
			case LOCATION:
			case PERSON:
			case PLOT:
			case TAG:
				if (Ctrl.PROPS.UPDATE.check(propName)) {
					refresh();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
