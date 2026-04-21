/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.db.attribute;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import storybook.App;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.person.Person;
import storybook.tools.html.Html;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.panels.AbstractScrollPanel;

/**
 *
 * @author favdb
 */
public class AttributesViewPanel extends AbstractScrollPanel implements Printable, MouseWheelListener {

	public static final int ZOOMMIN = 20;
	public static final int ZOOMMAX = 100;

	private JTextPane attributePane;

	public AttributesViewPanel(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.INS0)));
		attributePane = new JTextPane();
		attributePane.setEditable(false);
		attributePane.setContentType(Html.TYPE);
		rowsPanel = new JPanel(new MigLayout(MIG.FLOWY, "[grow,left]", ""));
		rowsPanel.add(attributePane);
		if (!LaF.isDark()) {
			rowsPanel.setBackground(SwingUtil.getBackgroundColor());
		}
		rowsScroller = new JScrollPane(attributePane);
		SwingUtil.setUnitIncrement(rowsScroller);
		rowsScroller.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		add(rowsScroller, MIG.GROW);
		refresh();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		if (Ctrl.PROPS.REFRESH.check(propName)) {
			View newView = (View) newValue;
			View view = (View) getParent().getParent();
			if (view == newView) {
				refresh();
			}
			return;
		}
		if (Ctrl.PROPS.SHOWINFO.check(propName)) {
			refresh();
		}
	}

	private String getAttributes() {
		StringBuilder html = new StringBuilder(Html.HTML_B);
		html.append(Html.BODY_B);
		html.append(Html.intoH(1, I18N.getMsg("attribute.list")));
		List<String> attributes = mainFrame.project.attributes.findKeys();
		for (String attribute : attributes) {
			html.append(Html.intoP(Html.intoB(attribute) + Html.BR + getValues(attribute)));
		}
		html.append(Html.BODY_E).append(Html.HTML_E);
		return (html.toString());
	}

	private String getValues(String attribute) {
		StringBuilder html = new StringBuilder();
		@SuppressWarnings("unchecked")
		List<Person> persons = (List) mainFrame.project.getList(Book.TYPE.PERSON);
		List<String> values = new ArrayList<>();
		//constitution de la liste des valeurs
		for (Person person : persons) {
			if (!person.getAttributes().isEmpty()) {
				for (Attribute attr : person.getAttributes()) {
					if (attribute.equals(attr.getKey())
					   && !values.contains(attr.getValue())) {
						values.add(attr.getValue());
					}
				}
			}
		}
		// finalisation de la liste
		html.append(getValuesOf(attribute, persons, values));
		return (html.toString());
	}

	private String getValuesOf(String attribute, List<Person> persons, List<String> values) {
		StringBuilder b = new StringBuilder();
		int i = 0;
		for (String v : values) {
			b.append(String.format("- %s : ", v));
			for (Person person : persons) {
				for (Attribute attr : person.getAttributes()) {
					if (attribute.equals(attr.getKey())
					   && v.equals(attr.getValue())) {
						if (i > 0) {
							b.append(", ");
						}
						b.append(person.getAbbr());
						i++;
					}
				}
			}
			b.append(Html.BR);
		}
		return b.toString();
	}

	@Override
	public void refresh() {
		attributePane.setText(getAttributes());
		attributePane.setCaretPosition(0);
	}

	@Override
	protected void zoomSet(int val) {
		// empty
	}

	@Override
	protected int zoomGetValue() {
		return App.preferences.chronoGetZoom();
	}

	@Override
	protected int zoomGetMin() {
		return ZOOMMIN;
	}

	@Override
	protected int zoomGetMax() {
		return ZOOMMAX;
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		//T O D O print
		return Printable.NO_SUCH_PAGE;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
