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
package assistant;

import api.mig.swing.MigLayout;
import assistant.Assistant.SECTION;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import storybook.db.abs.AbstractEntity;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 * Assistant dialog
 *
 * @author favdb
 */
public class AssistantDlg extends JDialog {

	private static final String TT = "AssistantDlg";

	public static String show(Container comp, AbstractEntity entity) {
		AssistantDlg dlg = new AssistantDlg(comp,
		   Assistant.xml,
		   Assistant.getSECTION(entity.getObjType().toString()),
		   entity.getAssistant());
		dlg.setVisible(true);
		if (dlg.cancel) {
			return null;
		}
		return dlg.getResult();
	}

	public static String show(Container comp, AssistantXml xml, SECTION type, String value) {
		AssistantDlg dlg = new AssistantDlg(comp, xml, type, value);
		dlg.setVisible(true);
		if (dlg.cancel) {
			return null;
		}
		return dlg.getResult();
	}

	private final AssistantXml xml;
	private final Component caller;
	private final SECTION typeDlg;
	private boolean cancel = false;
	private AssistantSection section;
	private final Document assistantDoc;

	public AssistantDlg(Container parent, AbstractEntity entity) {
		this(parent,
		   Assistant.getXml(),
		   Assistant.getSECTION(entity.getObjType().toString()),
		   entity.getAssistant());
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public AssistantDlg(Container parent, AssistantXml xml, SECTION typeDlg, String values) {
		super();
		this.xml = xml;
		this.caller = parent;
		this.typeDlg = typeDlg;
		this.assistantDoc = AssistantXml.getXmlValues(values);
		initialize();
	}

	/**
	 * initialize all data
	 *
	 */
	public void initialize() {
		//LOG.trace(TT + ".initialize() for " + typeDlg.toString());
		setTitle(I18N.getMsg("assistant") + ": " + I18N.getMsg("assistant.type." + typeDlg.toString()));
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[grow]"));
		Node node = xml.getSectionSingle(typeDlg.toString());
		if (node != null) {
			section = new AssistantSection(null, node, false, assistantDoc);
			add(section, MIG.GROW);
		} else {
			JLabel msg = new JLabel(I18N.getMsg("assistant.unknown"));
			msg.setForeground(Color.red);
			add(msg, MIG.GROW);
		}
		JButton btOK = Ui.initButton("ok", "ok", ICONS.K.OK, "", e -> {
			cancel = false;
			dispose();
		});
		if (node != null) {
			add(btOK, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		}
		JButton btCancel = Ui.initButton("cancel", "cancel", ICONS.K.CANCEL, "", e -> {
			cancel = true;
			dispose();
		});
		add(btCancel, MIG.RIGHT);
		setMinimumSize(new Dimension(800, 10));
		pack();
		setLocationRelativeTo(caller);
		this.setModal(true);
	}

	/**
	 * get result of dialog data as a String
	 *
	 * @return
	 */
	public String getResult() {
		//LOG.trace(TT + ".getResult()");
		StringBuilder b = new StringBuilder();
		section.getResult(b);
		return b.toString();
	}

	/**
	 * check if the dialog was canceled
	 *
	 * @return
	 */
	public boolean isCanceled() {
		return cancel;
	}

	/**
	 * show result as a MessageDialog
	 *
	 */
	public void showResult() {
		JTextPane textArea = new JTextPane();
		textArea.setContentType("text/html");
		String str = AssistantXml.getHtml(AssistantXml.getXmlValues(getResult()));
		textArea.setText(str);
		textArea.setEditable(false);
		textArea.setMinimumSize(new Dimension(800, 600));
		JScrollPane sc = new JScrollPane(textArea);
		sc.setSize(new Dimension(800, 10));
		sc.setPreferredSize(new Dimension(800, sc.getPreferredSize().height));
		JOptionPane.showMessageDialog(caller, sc, "Test", JOptionPane.INFORMATION_MESSAGE);
	}

	public JComponent findComponentByName(JComponent comp, String tosearch) {
		//LOG.trace(TT + ".findComponentByName(comp="
		//   + (comp == null ? "null" : comp.getClass().getSimpleName())
		//   + ", name='" + tosearch + "')");
		Component[] clist;
		if (comp == null) {
			clist = section.getComponents();
		} else {
			clist = comp.getComponents();
		}
		for (Object c : clist) {
			if (c instanceof JComponent) {
				String nm = ((JComponent) c).getName();
				if (nm != null && nm.equals(tosearch)) {
					return (JComponent) c;
				}
			}
			if ((c instanceof AssistantTab)
			   || (c instanceof JPanel)
			   || (c instanceof JTabbedPane)
			   || (c instanceof JScrollPane)
			   || (c instanceof JViewport)) {
				JComponent cx = findComponentByName((JComponent) c, tosearch);
				if (cx != null) {
					return cx;
				}
			}
		}
		return null;
	}

	public int getComboboxValue(String name) {
		JComponent comp = findComponentByName(null, name);
		if (comp == null || !(comp instanceof JComboBox)) {
			return -1;
		}
		return ((JComboBox) comp).getSelectedIndex();
	}

}
