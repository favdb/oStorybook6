/*
 * Copyright (C) 2020 favdb
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
package storybook.ui.panel.timeline;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.event.Event;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.DateUtil;
import storybook.tools.SbDuration;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.LaF;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 * définition d'une entité pour la présentation graphique de la frise chronologique dérivé du JPanel
 * l'élément est constitué de:
 * <ul>
 * <li>un JSLabel name : donnant le nom de l'entité et son icône éventuelle</li>
 * <li>un JPanel color : d'une taille variable en fonction de la durée de l'entité l'ensemble est
 * encadré selon le BorderFactory.createLineBorder dont la couleur est issue de la couleur attribuée
 * à l'entité</li>
 * </ul>
 *
 * @author favdb
 */
public class TimelineEntity extends JPanel implements MouseListener {

	private static final String TT = "TimelineEntity.";

	private final MainFrame mainFrame;
	AbstractEntity entity;
	private SbDuration duration;
	private JLabel name;
	private JPanel color;
	private Date date;

	public TimelineEntity(MainFrame mainFrame, AbstractEntity entity) {
		super();
		this.mainFrame = mainFrame;
		this.entity = entity;
		init();
	}

	public TimelineEntity(MainFrame mainFrame, AbstractEntity entity, Date date) {
		super();
		this.mainFrame = mainFrame;
		this.entity = entity;
		this.date = date;
		init();
	}

	private void init() {
		//LOG.trace(TT + "init() for " + entity.getObjType().name() + "=" + entity.getName());
		setLayout(new MigLayout("ins 0"));
		if (!LaF.isDark()) {
			setBackground(Color.white);
		}
		color = new JPanel();
		int l = 15;
		switch (entity.getObjType()) {
			case SCENE:
				Scene scene = (Scene) entity;
				Strand strand = scene.getStrand();
				if (strand == null) {
					strand = (Strand) mainFrame.project.strands.getFirst();
				}
				color.setBackground(Strand.getJColor(strand));
				date = scene.getDateRel();
				duration = scene.getSbDuration();
				break;
			case PERSON:
				Person person = (Person) entity;
				color.setBackground(person.getJColor());
				break;
			case EVENT:
				Event event = (Event) entity;
				color.setBackground(event.getJColor());
				date = event.getEventTime();
				l = 25;
				break;
			default:
				break;
		}
		name = new JLabel(TextUtil.ellipsize(entity.getName(), l));
		name.setBackground(this.getBackground());
		name.setIcon(entity.getIcon());
		add(name, MIG.WRAP);
		add(color, MIG.SPAN);
		this.addMouseListener(this);
		this.setBorder(BorderFactory.createLineBorder(color.getBackground()));
	}

	public AbstractEntity getEntity() {
		return entity;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return DateUtil.dateToString(date) + " -> "
				+ this.name.getText()
				+ " date=" + DateUtil.dateToString(date)
				+ " width=" + this.color.getSize().width;
	}

	public void setColorSize(TimelineScale scale) {
		int w, h = scale.getFontHeight() / 3;
		String d;
		SbDuration sbd;
		switch (entity.getObjType()) {
			case SCENE:
				Scene scene = (Scene) entity;
				d = scene.getDuration();
				if (d == null || d.isEmpty()) {
					d = scene.getTextDuration();
				}
				sbd = new SbDuration(d);
				w = scale.getColorSize(sbd.toMinutes());
				break;
			case EVENT:
				Event event = (Event) entity;
				sbd = new SbDuration(event.getDuration());
				w = scale.getColorSize(sbd.toMinutes());
				break;
			default:
				w = 0;
				h = 0;
				break;
		}
		Dimension dim = new Dimension(w, h);
		color.setSize(dim);
		color.setMinimumSize(dim);
		color.setMaximumSize(dim);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//LOG.trace(TT + "mouseClicked(e=" + e.toString() + ")");
		if (e.getClickCount() == 2) {
			mainFrame.showEditorAsDialog(entity);
		} else {
			mainFrame.getBookController().infoSetTo(entity);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopupMenu(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	private void showPopupMenu(MouseEvent evt) {
		JPopupMenu menu = EntityUtil.createPopupMenu(mainFrame, entity, !EntityUtil.WITH_CHRONO);
		Point p = SwingUtilities.convertPoint(this, evt.getPoint(), this);
		menu.show(this, p.x, p.y);
		evt.consume();
	}

	public void setTooltips(boolean sameDay) {
		StringBuilder buf = new StringBuilder();
		buf.append(Html.HTML_B);
		if (date != null) {
			String str = sameDay ? DateUtil.timeToString(date, false) : DateUtil.dateToString(date);
			if (entity.getObjType() == Book.TYPE.SCENE) {
				Date d = DateUtil.add(date, duration);
				str += " -> " + (sameDay ? DateUtil.timeToString(d, false) : DateUtil.dateToString(d));
			}
			buf.append(Html.intoP(Html.intoB(Html.intoI(str))));
		}
		buf.append("<table width='300'>");
		buf.append(Html.TR_B).append(Html.TD_B);
		buf.append(Html.P_B).append("<b>")
				.append(I18N.getMsg(entity.getObjType().toString()))
				.append(": ")
				.append(entity.getName())
				.append("</b>").append(Html.P_E);
		buf.append(Html.TD_E).append(Html.TR_E);
		buf.append(Html.TR_B).append(Html.TD_B);
		switch (entity.getObjType()) {
			case EVENT:
				buf.append(setToolTipEvent((Event) entity));
				break;
			case PERSON:
				buf.append(setToolTipPerson((Person) entity));
				break;
			case SCENE:
				buf.append(setToolTipScene((Scene) entity));
				break;
			default:
				break;
		}
		if (entity.hasDescription()) {
			buf.append(getInfo("description", entity.getDescription(), true));
		}
		if (entity.hasNotes()) {
			buf.append(getInfo("notes", entity.getNotes(), true));
		}
		buf.append(Html.TD_E)
				.append(Html.TR_E)
				.append(Html.TABLE_E)
				.append(Html.HTML_E);
		this.setToolTipText(buf.toString());
	}

	private String getInfo(String lib, String info, boolean withP) {
		StringBuilder b = new StringBuilder();
		if (withP) {
			b.append(Html.P_B);
		}
		b.append(Html.intoB(Html.intoI(I18N.getColonMsg(lib))))
				.append(Html.BR)
				.append(TextUtil.ellipsize(info, 64));
		if (withP) {
			b.append(Html.P_E);
		}
		return b.toString();
	}

	private String setToolTipEvent(Event entity) {
		if (entity == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		return buf.toString();
	}

	private String setToolTipPerson(Person entity) {
		if (entity == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		if (date != null && entity.getBirthday() != null) {
			buf.append(getInfo("person.age", "" + entity.calculateAge(date), false));
		}
		if (entity.getGender() != null) {
			buf.append(getInfo("gender", entity.getGender().getName(), false));
		}
		if (entity.getCategory() != null) {
			buf.append(getInfo("category", "" + entity.getCategory().getName(), false));
		}
		return buf.toString();
	}

	private String setToolTipScene(Scene entity) {
		if (entity == null) {
			return "";
		}
		return TextUtil.ellipsize(entity.getSummary(), 150);
	}

}
