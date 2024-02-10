/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import api.shef.actions.TextEditPopupManager;
import api.shef.tools.HtmlUtils;

public class ImagePanel extends HTMLAttributeEditorPanel {

	private ImageAttributesPanel imageAttrPanel;
	private LinkAttributesPanel linkAttrPanel;
	private JTextField linkUrlField;
	private JCheckBox linkCB;

	public ImagePanel() {
		this(new Hashtable());
	}

	public ImagePanel(Hashtable at) {
		super();
		initialize();
		setAttributes(at);
		updateComponentsFromAttribs();
	}

	private String createAttribs(Map ht) {
		String html = "";
		for (Iterator e = ht.keySet().iterator(); e.hasNext();) {
			Object k = e.next();
			html += " " + k + "=" + "\"" + ht.get(k) + "\"";
		}

		return html;
	}

	@Override
	public void updateComponentsFromAttribs() {
		imageAttrPanel.setAttributes(attribs);
		if (attribs.containsKey("a")) {
			linkCB.setSelected(true);
			linkAttrPanel.setEnabled(true);
			linkUrlField.setEditable(true);
			Map ht = HtmlUtils.tagAttribsToMap(attribs.get("a").toString());
			if (ht.containsKey("href")) {
				linkUrlField.setText(ht.get("href").toString());
			} else {
				linkUrlField.setText("");
			}
			linkAttrPanel.setAttributes(ht);
		} else {
			linkCB.setSelected(false);
			linkAttrPanel.setEnabled(false);
			linkUrlField.setEditable(false);
			linkAttrPanel.setAttributes(new HashMap());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		imageAttrPanel.updateAttribsFromComponents();
		linkAttrPanel.updateAttribsFromComponents();
		if (linkCB.isSelected()) {
			Map ht = linkAttrPanel.getAttributes();
			ht.put("href", linkUrlField.getText());
			attribs.put("a", createAttribs(ht));
		} else {
			attribs.remove("a");
		}
	}

	private void initialize() {
		setLayout(new BorderLayout());
		linkCB = new JCheckBox(I18N.getMsg("shef.link"));
		linkUrlField = new JTextField();
		linkUrlField.setColumns(32);

		JPanel linkPanel = new JPanel(new BorderLayout(5, 5));
		linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel urlPanel = new JPanel(new GridBagLayout());
		urlPanel.add(linkCB, new GBC("0, 0,anchor W, ins 0 0 5 5"));
		urlPanel.add(linkUrlField, new GBC("0,1,anchor W, wx 1.0, ins 0 0 5 0"));
		linkPanel.add(urlPanel, BorderLayout.NORTH);

		linkAttrPanel = new LinkAttributesPanel();
		linkPanel.add(linkAttrPanel, BorderLayout.CENTER);

		imageAttrPanel = new ImageAttributesPanel();
		imageAttrPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(I18N.getMsg("shef.image"), imageAttrPanel);
		tabs.addTab(I18N.getMsg("shef.link"), linkPanel);
		add(tabs);

		linkAttrPanel.setEnabled(linkCB.isSelected());
		linkUrlField.setEditable(linkCB.isSelected());
		linkCB.addItemListener((ItemEvent e) -> {
			linkAttrPanel.setEnabled(linkCB.isSelected());
			linkUrlField.setEditable(linkCB.isSelected());
		});
		TextEditPopupManager.getInstance().registerJTextComponent(linkUrlField);
	}

	public String getUrlField() {
		return linkUrlField.getText();
	}

	public void setUrlField(String image) {
		linkUrlField.setText(image);
	}

}
