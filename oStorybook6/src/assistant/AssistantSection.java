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

import assistant.app.AppAssistant;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.TabbedPaneUI;
import api.mig.swing.MigLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.LOG;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class AssistantSection extends JPanel implements MouseListener {

	private static final String TT = "AssistantSection";

	private final AssistantPanel assistantPanel;
	private final AssistantXml xml;
	private final Node section;
	private final boolean editable;
	private AssistantTab compTab;
	private List<AssistantTab> tabs = new ArrayList<>();
	private final Document assistantDoc;

	public AssistantSection(AssistantPanel panel, Node section, boolean editable, Document assistantDoc) {
		this.assistantPanel = panel;
		this.xml = Assistant.getXml();
		this.section = section;
		this.editable = editable;
		this.assistantDoc = assistantDoc;
		initialize();
	}

	public void initialize() {
		//LOG.trace(TT + ".initialize() for section " + AssistantXml.traceNode(section));
		if (getComponents().length > 0) {
			removeAll();
		}
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3, MIG.WRAP), "[grow]"));
		//setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		JTabbedPane tb = new JTabbedPane();
		tb.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		tb.setVisible(false);
		JButton btAddTab = new JButton(I18N.getMsg("assistant.add.tab"));
		FontMetrics fm = btAddTab.getFontMetrics(btAddTab.getFont());
		int width = (int) (fm.charWidth('W') * 2.9);
		int height = fm.getHeight() * btAddTab.getText().length();
		btAddTab.setIcon(IconUtil.getIconSmall(ICONS.K.PLUS));
		btAddTab.addActionListener(e -> addTab());
		AffineTransform at = btAddTab.getFont().getTransform();
		at.rotate(-Math.PI / 2);
		Font font = btAddTab.getFont().deriveFont(at);
		btAddTab.setFont(font);
		btAddTab.setVerticalTextPosition(SwingConstants.TOP);
		btAddTab.setPreferredSize(new Dimension(width, height));
		if (editable) {
			add(btAddTab, MIG.get("dock west", MIG.GROWY));
		}
		NodeList childs = AssistantXml.getTab(section);
		if (childs.getLength() < 1) {
			JLabel lb = new JLabel(I18N.getMsg("assistant.section.empty"));
			lb.setBorder(BorderFactory.createEtchedBorder());
			lb.setHorizontalAlignment(SwingConstants.CENTER);
			lb.setVerticalAlignment(SwingConstants.CENTER);
			lb.setMinimumSize(new Dimension(width * 10, 600));
			lb.setForeground(Color.red);
			add(lb, MIG.get(MIG.SPAN, MIG.GROW));
			return;
		}
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if (child.getNodeName().equals("#text")) {
				continue;
			}
			AssistantTab tab = new AssistantTab(this, child, editable, assistantDoc);
			JScrollPane sc = new JScrollPane(tab);
			sc.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
			tb.addTab(((Element) child).getAttribute("title"), sc);
			tb.setVisible(true);
			tabs.add(tab);
		}
		if (tb.getComponentCount() == 1 && !editable) {
			compTab = (AssistantTab) ((JScrollPane) tb.getComponentAt(0)).getViewport().getView();
			add(compTab, MIG.GROW);
			tb.setVisible(false);
		}
		if (tb.isVisible()) {
			tb.addMouseListener(this);
			add(tb, MIG.get(MIG.TOP, MIG.GROW));
		}
	}

	public AssistantXml getXml() {
		//LOG.trace(TT + ".getXml()");
		return xml;
	}

	private void addTab() {
		//LOG.log(TT + ".addTab()");
		if (!editable) {
			return;
		}
		AssistantAddTab dlg = new AssistantAddTab(assistantPanel, xml, null, true);
		dlg.setVisible(true);
		if (dlg.isCanceled()) {
			return;
		}
		//then create the node
		String n = dlg.getTfName();
		xml.createNode(section, "tab", n, dlg.getTfTitle(), dlg.getTfPrompt());
		initialize();
		assistantPanel.setModified();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger() && evt.getSource() instanceof JTabbedPane) {
			if (!editable) {
				return;
			}
			JTabbedPane tb = (JTabbedPane) evt.getComponent();
			TabbedPaneUI ui = tb.getUI();
			int tab = ui.tabForCoordinate(tb, evt.getX(), evt.getY());
			String n = tb.getTitleAt(tab);
			Node tabNode = xml.findTab(section, n);
			if (tabNode == null) {
				LOG.err("node not found");
				return;
			}
			AssistantAddTab dlg = new AssistantAddTab(assistantPanel, xml, tabNode, true);
			dlg.setVisible(true);
			if (dlg.isCanceled()) {
				return;
			}
			if (dlg.isCanceled()) {
				return;
			}
			String titre = dlg.getTfTitle();
			String aide = dlg.getTfPrompt();
			AssistantXml.setAttribute(tabNode, "title", titre);
			AssistantXml.setAttribute(tabNode, "prompt", aide);
			if (!n.equals(titre)) {
				tb.setTitleAt(tab, titre);
			}
			JScrollPane scroll = (JScrollPane) tb.getComponentAt(tab);
			AssistantTab tabPanel = (AssistantTab) scroll.getViewport().getView();
			tabPanel.setPrompt(aide);
			assistantPanel.setModified();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public AssistantPanel getPanel() {
		return assistantPanel;
	}

	AppAssistant getAssistantApp() {
		return assistantPanel.getAssistantApp();
	}

	public String getResult(StringBuilder b) {
		//LOG.trace(TT + ".getResult()");
		StringBuilder bb = new StringBuilder();
		for (AssistantTab tab : tabs) {
			tab.getResult(bb);
		}
		if (!bb.toString().isEmpty()) {
			String id = section.getNodeName();
			b.append("<").append(id).append(">");
			b.append(bb.toString());
			b.append("</").append(id).append(">");
		}
		return b.toString();
	}

}
