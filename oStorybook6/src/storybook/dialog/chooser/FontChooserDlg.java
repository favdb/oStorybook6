/*
 * Copyright (C) 2017 favdb
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
package storybook.dialog.chooser;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import i18n.I18N;
import storybook.tools.swing.LaF;
import storybook.tools.swing.js.JSLabel;

/**
 *
 * @author favdb
 */
public class FontChooserDlg extends JDialog {

	String[] styleList = new String[]{"Plain", "Bold", "Italic"};
	String[] sizeList = new String[]{"3", "4", "5", "6", "7", "8", "9", "10",
		"11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22",
		"24", "27", "30", "34", "39", "45", "51", "60"};
	NwList listStyle;
	NwList listFont;
	NwList listSize;
	static JSLabel sample = new JSLabel();
	private boolean canceled = false;

	public FontChooserDlg(JDialog parent, Font font) {
		super(parent, true);
		initAll();
		setTitle(I18N.getMsg("font.chooser"));
		if (font == null) {
			font = sample.getFont();
		} else {
			sample.setFont(font);
		}
		listFont.setSelectedItem(font.getName());
		listSize.setSelectedItem(font.getSize() + "");
		listStyle.setSelectedItem(styleList[font.getStyle()]);

	}

	private void initAll() {
		getContentPane().setLayout(null);
		setBounds(200, 200, 460, 430);
		addLists();
		addButtons();
		sample.setBounds(10, 320, 415, 25);
		sample.setForeground(Color.black);
		getContentPane().add(sample);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				setVisible(false);
			}
		});
	}

	private void addLists() {
		listFont = new NwList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		listFont.setBounds(10, 10, 260, 295);
		getContentPane().add(listFont);
		listStyle = new NwList(styleList);
		listStyle.setBounds(280, 10, 80, 295);
		getContentPane().add(listStyle);
		listSize = new NwList(sizeList);
		listSize.setBounds(370, 10, 60, 295);
		getContentPane().add(listSize);
	}

	private void addButtons() {
		JButton ok = new JButton(I18N.getMsg("ok"));
		ok.setMargin(new Insets(0, 0, 0, 0));
		ok.setBounds(260, 350, 70, 24);
		getContentPane().add(ok);
		ok.addActionListener((ActionEvent e) -> {
			setVisible(false);
			canceled = false;
		});
		JButton cancel = new JButton(I18N.getMsg("cancel"));
		cancel.setMargin(new Insets(0, 0, 0, 0));
		cancel.setBounds(340, 350, 70, 24);
		getContentPane().add(cancel);
		cancel.addActionListener((ActionEvent e) -> {
			setVisible(false);
			canceled = true;
		});
	}

	private void showSample() {
		int g = 0;
		try {
			g = Integer.parseInt(listSize.getSelectedValue());
		} catch (NumberFormatException nfe) {
		}
		String st = listStyle.getSelectedValue();
		int s = Font.PLAIN;
		if (st.equalsIgnoreCase("Bold")) {
			s = Font.BOLD;
		}
		if (st.equalsIgnoreCase("Italic")) {
			s = Font.ITALIC;
		}
		sample.setFont(new Font(listFont.getSelectedValue(), s, g));
		sample.setText("The quick brown fox jumped over the lazy dog.");
	}

	public Font getSelectedFont() {
		return (sample.getFont());
	}

	public boolean isCanceled() {
		return canceled;
	}

	public class NwList extends JPanel {

		JList jl;
		JScrollPane sp;
		JSLabel jt;
		String si = " ";

		@SuppressWarnings("unchecked")
		public NwList(String[] values) {
			setLayout(null);
			jl = new JList(values);
			sp = new JScrollPane(jl);
			jt = new JSLabel();
			if (!LaF.isDark()) {
				jt.setBackground(Color.white);
				jt.setForeground(Color.black);
			}
			jt.setOpaque(true);
			jt.setBorder(new JTextField().getBorder());
			jt.setFont(getFont());
			jl.setBounds(0, 0, 100, 1000);
			if (!LaF.isDark()) {
				jl.setBackground(Color.white);
			}
			jl.addListSelectionListener((ListSelectionEvent e) -> {
				jt.setText((String) jl.getSelectedValue());
				si = (String) jl.getSelectedValue();
				showSample();
			});
			add(sp);
			add(jt);
		}

		public String getSelectedValue() {
			return (si);
		}

		public void setSelectedItem(String s) {
			jl.setSelectedValue(s, true);
		}

		@Override
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, y, w, h);
			sp.setBounds(0, y + 12, w, h - 23);
			sp.revalidate();
			jt.setBounds(0, 0, w, 20);
		}
	}
}
