/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package storybook.tools.print;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import storybook.tools.LOG;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 * from https://www.docs4dev.com/docs/en/java/java8/tutorials/uiswing-misc-printtable.html <br>
 * Basic features of {@code JTable} printing. Allows the user to configure a couple of options and
 * print a table.
 *
 * @author Shannon Hickey
 */
public class TablePrinter extends JFrame {

	private static final String TT = "TablePrinter.";

	/* UI Components */
	private final String title;
	private JPanel contentPane;
	private JLabel titleLabel;
	private JTable table;
	private JScrollPane scroll;
	private JButton printButton;

	/* Protected so that they can be modified/disabled by subclasses */
	private final MainFrame mainFrame;
	private JButton cancelButton;
	private JPanel buttonPane;

	public static void pr(String msg, MainFrame m, JTable t, String header, String footer) {
		/*LOG.trace(TT + "pr(msg=" + msg + ",m"
		   + ", t=" + t.getName()
		   + ", header='" + header + "'"
		   + ", footer='" + footer + "')");*/
		TablePrinter p = new TablePrinter(m, t, header, footer);
		p.setVisible(true);
	}
	private final String header;
	private final String footer;

	/**
	 * Constructs an instance of the demo.
	 *
	 * @param mainFrame
	 * @param table
	 */
	public TablePrinter(MainFrame mainFrame, JTable table, String header, String footer) {
		super(I18N.getMsg("print"));
		this.mainFrame = mainFrame;
		this.table = table;
		this.title = I18N.getMsg(table.getName().toLowerCase().replace("table", "") + "s");
		this.header = header;
		this.footer = footer;
		init();
		initUi();
	}

	private void init() {

		titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));

		scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(800, 600));

		buttonPane = new JPanel(new MigLayout());
		cancelButton = new JButton(I18N.getMsg("cancel"));
		cancelButton.addActionListener((ActionEvent ae) -> {
			dispose();
		});
		buttonPane.add(cancelButton);
		printButton = new JButton(I18N.getMsg("print"));
		printButton.setToolTipText(I18N.getMsg("print.table"));
		printButton.addActionListener((ActionEvent ae) -> {
			printTable();
			dispose();
		});
		buttonPane.add(printButton);
	}

	private void initUi() {
		contentPane = new JPanel(new MigLayout(MIG.FILL));

		JPanel top = new JPanel(new MigLayout(MIG.FILL));
		top.add(titleLabel, MIG.WRAP);
		top.add(scroll, MIG.get(MIG.SPAN, MIG.GROW));
		contentPane.add(top, MIG.get(MIG.SPAN, MIG.GROW));

		contentPane.add(buttonPane, MIG.RIGHT);

		setContentPane(contentPane);
		setSize(800, 600);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	/**
	 * Print the table.
	 */
	private void printTable() {
		try {
			/* print the table */
			boolean complete = table.print(JTable.PrintMode.FIT_WIDTH,
			   new MessageFormat(header), new MessageFormat("Page {0}"),
			   true, null, true, null);

			/* if printing completes */
			if (complete) {
				/* show a success message */
				JOptionPane.showMessageDialog(this,
				   I18N.getMsg("print.ok"),
				   I18N.getMsg("print"),
				   JOptionPane.INFORMATION_MESSAGE);
			} else {
				/* show a message indicating that printing was cancelled */
				JOptionPane.showMessageDialog(this,
				   I18N.getMsg("print.cancelled"),
				   I18N.getMsg("print"),
				   JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (PrinterException ex) {
			/* Printing failed, report to the user */
			LOG.err("Printing error", ex);
			JOptionPane.showMessageDialog(this,
			   I18N.getMsg("print.error", ex.getMessage()),
			   I18N.getMsg("print"),
			   JOptionPane.ERROR_MESSAGE);
		}
	}

}
