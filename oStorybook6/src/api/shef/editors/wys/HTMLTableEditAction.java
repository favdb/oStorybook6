/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.event.ActionEvent;
import java.io.StringWriter;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import api.shef.actions.CompoundManager;
import api.shef.tools.ElementWriter;
import api.shef.tools.HtmlUtils;
import i18n.I18N;

/**
 *
 * Action for adding and removing table elements
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLTableEditAction extends HTMLTextEditAction {

	public static final int INSERT_CELL = 0;
	public static final int DELETE_CELL = 1;
	public static final int INSERT_ROW = 2;
	public static final int DELETE_ROW = 3;
	public static final int INSERT_COL = 4;
	public static final int DELETE_COL = 5;
	private static final String NAMES[] = {
		I18N.getMsg("shef.insert_cell"),
		I18N.getMsg("shef.delete_cell"),
		I18N.getMsg("shef.insert_row"),
		I18N.getMsg("shef.delete_row"),
		I18N.getMsg("shef.insert_column"),
		I18N.getMsg("shef.delete_column")
	};
	private int type;

	public HTMLTableEditAction(WysiwygEditor editor, int type) throws IllegalArgumentException {
		super(editor, "");
		if (type < 0 || type >= NAMES.length) {
			throw new IllegalArgumentException("Invalid type");
		}
		this.type = type;
		putValue(NAME, NAMES[type]);
		addShouldBeEnabledDelegate((Action a) -> isInTD(getCurrentEditor()));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane ed) {
		if (ed == null) {
			return;
		}
		HTMLDocument document = (HTMLDocument) ed.getDocument();
		Element curElem = document.getParagraphElement(ed.getCaretPosition());
		Element td = HtmlUtils.getParent(curElem, HTML.Tag.TD);
		Element tr = HtmlUtils.getParent(curElem, HTML.Tag.TR);
		if (td == null || tr == null) {
			return;
		}
		CompoundManager.beginCompoundEdit(document);
		try {
			switch (type) {
				case INSERT_CELL:
					document.insertAfterEnd(td, "<td></td>");
					break;
				case DELETE_CELL:
					removeCell(td);
					break;
				case INSERT_ROW:
					insertRowAfter(tr);
					break;
				case DELETE_ROW:
					removeRow(tr);
					break;
				case INSERT_COL:
					insertColumnAfter(td);
					break;
				case DELETE_COL:
					removeColumn(td);
					break;
				default:
					break;
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		CompoundManager.endCompoundEdit(document);
	}

	private void removeCell(Element td) throws Exception {
		Element tr = HtmlUtils.getParent(td, HTML.Tag.TR);
		if (tr != null && td.getName().equals("td")) {
			if (td.getEndOffset() != tr.getEndOffset()) {
				remove(td);
			} else if (getRowCellCount(tr) <= 1) {
				remove(tr);
			} else {
				StringWriter out = new StringWriter();
				ElementWriter w = new ElementWriter(out, tr, tr.getStartOffset(), td.getStartOffset());
				w.write();
				HTMLDocument doc = (HTMLDocument) tr.getDocument();
				doc.setOuterHTML(tr, out.toString());
			}
		}
	}

	private void insertRowAfter(Element tr) throws Exception {
		Element table = HtmlUtils.getParent(tr, HTML.Tag.TABLE);
		if (table != null && tr.getName().equals("tr")) {
			HTMLDocument doc = (HTMLDocument) tr.getDocument();
			if (tr.getEndOffset() != table.getEndOffset()) {
				doc.insertAfterEnd(tr, getRowHTML(tr));
			} else {
				AttributeSet atr = table.getAttributes();
				String tbl = HtmlUtils.getElementHTML(table, false);
				tbl += getRowHTML(tr);
				tbl = HtmlUtils.createTag(HTML.Tag.TABLE, atr, tbl);
				doc.setOuterHTML(table, tbl);
			}
		}
	}

	private void removeRow(Element tr) throws Exception {
		Element table = HtmlUtils.getParent(tr, HTML.Tag.TABLE);
		if (table != null && tr.getName().equals("tr")) {
			if (tr.getEndOffset() != table.getEndOffset()) {
				remove(tr);
			} else if (getTableRowCount(table) <= 1) {
				remove(table);
			} else {
				StringWriter out = new StringWriter();
				ElementWriter w = new ElementWriter(out, table, table.getStartOffset(), tr.getStartOffset());
				w.write();
				HTMLDocument doc = (HTMLDocument) tr.getDocument();
				doc.setOuterHTML(table, out.toString());
			}
		}
	}

	private int getTableRowCount(Element table) {
		int count = 0;
		for (int i = 0; i < table.getElementCount(); i++) {
			Element e = table.getElement(i);
			if (e.getName().equals("tr")) {
				count++;
			}
		}
		return count;
	}

	private int getRowCellCount(Element tr) {
		int count = 0;
		for (int i = 0; i < tr.getElementCount(); i++) {
			Element e = tr.getElement(i);
			if (e.getName().equals("td")) {
				count++;
			}
		}
		return count;
	}

	private void remove(Element el) throws BadLocationException {
		int start = el.getStartOffset();
		int len = el.getEndOffset() - start;
		Document document = el.getDocument();
		if (el.getEndOffset() > document.getLength()) {
			len = document.getLength() - start;
		}
		document.remove(start, len);
	}

	private int getCellIndex(Element tr, Element td) {
		int tdIndex = -1;
		for (int i = 0; i < tr.getElementCount(); i++) {
			Element e = tr.getElement(i);
			if (e.getStartOffset() == td.getStartOffset()) {
				tdIndex = i;
				break;
			}
		}
		return tdIndex;
	}

	private void removeColumn(Element td) throws Exception {
		Element tr = HtmlUtils.getParent(td, HTML.Tag.TR);
		int tdIndex = getCellIndex(tr, td);
		if (tdIndex == -1) {
			return;
		}
		Element table = HtmlUtils.getParent(tr, HTML.Tag.TABLE);
		for (int i = 0; i < table.getElementCount(); i++) {
			Element row = table.getElement(i);
			if (row.getName().equals("tr")) {
				Element e = row.getElement(tdIndex);
				if (e != null && e.getName().equals("td")) {
					removeCell(e);
				}
			}
		}
	}

	private void insertColumnAfter(Element td) throws Exception {
		Element tr = HtmlUtils.getParent(td, HTML.Tag.TR);
		HTMLDocument doc = (HTMLDocument) tr.getDocument();
		int tdIndex = getCellIndex(tr, td);
		if (tdIndex == -1) {
			return;
		}
		Element table = HtmlUtils.getParent(tr, HTML.Tag.TABLE);
		for (int i = 0; i < table.getElementCount(); i++) {
			Element row = table.getElement(i);
			if (row.getName().equals("tr")) {
				AttributeSet attr = row.getAttributes();
				int cellCount = row.getElementCount();
				String rowHTML = "";
				String cell = "<td></td>";
				for (int j = 0; j < cellCount; j++) {
					Element e = row.getElement(j);
					rowHTML += HtmlUtils.getElementHTML(e, true);
					if (j == tdIndex) {
						rowHTML += cell;
					}
				}
				int tds = row.getElementCount() - 1;
				if (tds < tdIndex) {
					for (; tds <= tdIndex; tds++) {
						rowHTML += cell;
					}
				}
				rowHTML = HtmlUtils.createTag(HTML.Tag.TR, attr, rowHTML);
				doc.setOuterHTML(row, rowHTML);
			}
		}
	}

	private String getRowHTML(Element tr) {
		String trTag = "<tr>";
		if (tr.getName().equals("tr")) {
			for (int i = 0; i < tr.getElementCount(); i++) {
				if (tr.getElement(i).getName().equals("td")) {
					trTag += "<td></td>";
				}
			}
		}
		trTag += "</tr>";
		return trTag;
	}

	private boolean isInTD(JEditorPane tc) {
		Element td = null;
		if (tc != null) {
			HTMLDocument doc = (HTMLDocument) tc.getDocument();
			try {
				Element curElem = doc.getParagraphElement(tc.getCaretPosition());
				td = HtmlUtils.getParent(curElem, HTML.Tag.TD);
			} catch (Exception ex) {
			}
		}
		return td != null;
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane wysEditor) {
		boolean isInTd = isInTD(wysEditor);
		if ((isInTd && !isEnabled()) || (isEnabled() && !isInTd)) {
			updateEnabled();
		}
	}

}
