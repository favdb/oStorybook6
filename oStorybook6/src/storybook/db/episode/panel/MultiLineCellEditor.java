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
package storybook.db.episode.panel;

import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

/**
 * from
 * https://www.developpez.net/forums/d1415489/java/interfaces-graphiques-java/awt-swing/composants/jtable-multiligne-textearea/
 *
 * @author favdb
 */
public class MultiLineCellEditor extends AbstractCellEditor
		implements TableCellEditor, DocumentListener {

	ETextArea textArea;
	private final EpisodePanel episode;
	private boolean modified = false;
	private String textOrigin;

	public MultiLineCellEditor(EpisodePanel episode) {
		super();
		this.episode = episode;
	}

	public EpisodePanel getEpisodePanel() {
		return this.episode;
	}

	@Override
	public Object getCellEditorValue() {
		return textArea.getText().trim();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		textArea = new ETextArea(this);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.getDocument().addDocumentListener(this);
		textOrigin = table.getValueAt(row, column).toString().trim();
		textArea.setText(textOrigin);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		EventQueue.invokeLater(() -> {
			textArea.setCaretPosition(textOrigin.length());
			textArea.requestFocusInWindow();
		});
		return scroll;
	}

	private void setModified() {
		modified = true;
		episode.setModified();
	}

	@Override
	public boolean stopCellEditing() {
		super.stopCellEditing();
		if (modified && !textOrigin.equals(textArea.getText().trim())) {
			episode.setModified();
		}
		modified = false;
		return true;
	}

	@Override
	public void cancelCellEditing() {
		modified = false;
		super.cancelCellEditing();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		modified = true;
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		modified = true;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		modified = true;
	}

}
