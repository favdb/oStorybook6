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
import javax.swing.JTextField;
import api.shef.actions.TextEditPopupManager;
import i18n.I18N;
import storybook.tools.swing.js.JSLabel;

public class StyleAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String SCLASS = "class";

	private JSLabel classLabel = null;
	private JSLabel idLabel = null;
	private JTextField classField = null;
	private JTextField idField = null;

	/**
	 * This method initializes
	 *
	 */
	public StyleAttributesPanel() {
		this(new Hashtable());
	}

	public StyleAttributesPanel(Hashtable attr) {
		super();
		initialize();
		setAttributes(attr);
		this.updateComponentsFromAttribs();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(new java.awt.Dimension(210, 60));
		setPreferredSize(new java.awt.Dimension(210, 60));
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(new JSLabel(I18N.getMsg("shef.nothing")));
		/*
		 * nothing because class is not allowed*/
		classLabel = new JSLabel(I18N.getMsg("shef." + SCLASS));
		add(classLabel, new GBC("0,0,anchor W, ins 0 0 5 5"));
		classLabel.setVisible(false);
		add(getClassField(), new GBC("0,1,fill H, wx 1.0, wy 0.0, ins 0 0 5 0"));
		idLabel = new JSLabel(I18N.getMsg("shef.id"));
		add(idLabel, new GBC("1,0,anchor W, ins 0 0 5 5"));
		add(getIdField(), new GBC("1,1,fill H, ins 0 0 5 0, wx 1.0"));

		TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
		popupMan.registerJTextComponent(classField);
		popupMan.registerJTextComponent(idField);
		classLabel.setVisible(false);
		classField.setVisible(false);
		idLabel.setVisible(false);
		idField.setVisible(false);

	}

	@Override
	public void updateComponentsFromAttribs() {
		if (attribs.containsKey(SCLASS)) {
			classField.setText(attribs.get(SCLASS).toString());
		} else {
			classField.setText("");
		}
		if (attribs.containsKey("id")) {
			idField.setText(attribs.get("id").toString());
		} else {
			idField.setText("");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (!classField.getText().equals("")) {
			attribs.put(SCLASS, classField.getText());
		} else {
			attribs.remove(SCLASS);
		}
		if (!idField.getText().equals("")) {
			attribs.put("id", idField.getText());
		} else {
			attribs.remove("id");
		}
	}

	/**
	 * This method initializes classField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getClassField() {
		if (classField == null) {
			classField = new JTextField();
		}
		return classField;
	}

	/**
	 * This method initializes idField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getIdField() {
		if (idField == null) {
			idField = new JTextField();
		}
		return idField;
	}

}
