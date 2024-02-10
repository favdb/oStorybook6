/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.GridBagLayout;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.JPanel;

public class RowAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String BGCOLOR = "bgcolor";

	private AlignmentAttributesPanel alignPanel = null;
	private BGColorPanel bgColorPanel = null;
	private JPanel expansionPanel = null;

	/**
	 * This is the default constructor
	 */
	public RowAttributesPanel() {
		this(new Hashtable());
	}

	public RowAttributesPanel(Hashtable attr) {
		super(attr);
		initialize();
		alignPanel.setAttributes(getAttributes());
		updateComponentsFromAttribs();
	}

	@Override
	public void updateComponentsFromAttribs() {
		if (attribs.containsKey(BGCOLOR)) {
			bgColorPanel.setSelected(true);
			bgColorPanel.setColor(attribs.get(BGCOLOR).toString());
		}
		alignPanel.updateComponentsFromAttribs();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (bgColorPanel.isSelected()) {
			attribs.put(BGCOLOR, bgColorPanel.getColor());
		} else {
			attribs.remove(BGCOLOR);
		}
		alignPanel.updateAttribsFromComponents();
	}

	public void setComponentStates(Hashtable attribs) {
		if (attribs.containsKey(BGCOLOR)) {
			bgColorPanel.setSelected(true);
			bgColorPanel.setColor(attribs.get(BGCOLOR).toString());
		}
		alignPanel.setComponentStates(attribs);
	}

	@Override
	public void setAttributes(Map attr) {
		alignPanel.setAttributes(attr);
		super.setAttributes(attr);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(279, 140);
		setPreferredSize(new java.awt.Dimension(215, 140));

		add(getAlignPanel(), new GBC("0,0,fill H, anchor W, ins 0 0 5 0, wx 0.0"));
		add(getBgColorPanel(), new GBC("1,0,anchor W, wx 1.0"));
		add(getExpansionPanel(), new GBC("2,0,fill H, anchor W, wy 1.0"));
	}

	/**
	 * This method initializes alignPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private AlignmentAttributesPanel getAlignPanel() {
		if (alignPanel == null) {
			alignPanel = new AlignmentAttributesPanel();
		}
		return alignPanel;
	}

	/**
	 * This method initializes bgColorPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private BGColorPanel getBgColorPanel() {
		if (bgColorPanel == null) {
			bgColorPanel = new BGColorPanel();
		}
		return bgColorPanel;
	}

	/**
	 * This method initializes expansionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getExpansionPanel() {
		if (expansionPanel == null) {
			expansionPanel = new JPanel();
		}
		return expansionPanel;
	}

}
