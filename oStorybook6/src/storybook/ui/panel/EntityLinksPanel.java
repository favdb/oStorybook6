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
import javax.swing.JLabel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.ListUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorIcon;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class EntityLinksPanel extends AbstractPanel {

	private static final String TT = "EntityLinksPanel";

	private final Scene scene;
	private final boolean withText;
	private final Book.TYPE type;

	public EntityLinksPanel(MainFrame mainFrame, Scene scene, Book.TYPE type, boolean withText) {
		super(mainFrame);
		this.scene = scene;
		this.type = type;
		this.withText = withText;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.INS1)));
		setOpaque(false);
		//setBorder(SwingUtil.getBorderDot());
		List list = new ArrayList<>();
		switch (type) {
			case ITEM:
				list = scene.getItems();
				break;
			case LOCATION:
				list = scene.getLocations();
				break;
			case PERSON:
				list = scene.getPersons();
				break;
			case PLOT:
				list = scene.getPlots();
				break;
			case STRAND:
				list = scene.getStrands();
				break;
		}
		if (list.isEmpty()) {
			add(new JLabel(IconUtil.getIconSmall(ICONS.K.EMPTY)));
			return;
		}
		List<String> tooltip = new ArrayList<>();
		for (Object obj : list) {
			AbstractEntity entity = (AbstractEntity) obj;
			JLabel lb = new JLabel(entity.getIcon());
			lb.setOpaque(false);
			if (entity instanceof Strand) {
				lb.setIcon(new ColorIcon(((Strand) entity).getJColor(), IconUtil.getDefSize()));
			}
			if (withText || list.size() < 2) {
				if (!(entity instanceof Person)) {
					lb.setText(entity.getName());
				}
			}
			lb.setToolTipText(entity.getFullName());
			if (entity instanceof Person) {
				lb.setBackground(((Person) entity).getJColor());
			}
			add(lb);
			tooltip.add(entity.getName());
		}
		if (tooltip.size() > 1) {
			this.setToolTipText(Html.intoHTML(ListUtil.join(tooltip, "<br>")));
		}
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
