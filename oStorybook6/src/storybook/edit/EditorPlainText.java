/*
 * Copyright (C) 2019 favdb
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
package storybook.edit;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.panels.AbstractPanel;

/**
 *
 * @author favdb
 */
public class EditorPlainText extends AbstractPanel implements CaretListener {

	private JTextArea ta;
	private int maxLength;
	private JLabel lbMessage;
	private JScrollPane scroller;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public EditorPlainText(String text, int maxLength) {
		super();
		this.maxLength = maxLength;
		initAll();
		setText(text);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout());

		ta = new JTextArea(10, 20);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.addCaretListener(this);
		ta.setMaximumSize(SwingUtil.getScreenSize());
		SwingUtil.setFixedSize(ta, SwingUtil.getScreenSize());

		scroller = new JScrollPane(ta);
		ta.setMaximumSize(SwingUtil.getScreenSize());
		lbMessage = new JLabel(" ");

		// layout
		add(scroller, MIG.GROW);
		add(lbMessage, MIG.RIGHT);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (maxLength > 0) {
			int len = maxLength - getText().length() - 1;
			if (len < 0) {
				lbMessage.setForeground(Color.red);
			} else {
				lbMessage.setForeground(Color.black);
			}
			lbMessage.setText(I18N.getMsg("editor.letters_left", len));
		}
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public String getText() {
		return ta.getText();
	}

	public void setText(String txt) {
		ta.setText(txt);
		ta.setCaretPosition(0);
	}

	public void setEditable(boolean b) {
		ta.setEditable(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
