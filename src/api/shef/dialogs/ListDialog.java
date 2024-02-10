/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.*;
import javax.swing.Icon;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import i18n.I18N;

public class ListDialog extends OptionDialog {

	public static final int UNORDERED = ListAttributesPanel.UL_LIST;
	public static final int ORDERED = ListAttributesPanel.OL_LIST;
	private static Icon icon = IconUtil.getIconSmall(ICONS.K.CATEGORIES);
	private static String title = I18N.getMsg("shef.list_properties");
	private static String desc = I18N.getMsg("shef.list_properties_desc");
	private ListAttributesPanel listAttrPanel;

	public ListDialog(Frame parent) {
		super(parent, title, desc, icon);
		init();
	}

	public ListDialog(Dialog parent) {
		super(parent, title, desc, icon);
		init();
	}

	private void init() {
		listAttrPanel = new ListAttributesPanel();
		setContentPane(listAttrPanel);
		pack();
		setSize(220, getHeight());
		setResizable(false);
	}

	public void setListType(int t) {
		listAttrPanel.setListType(t);
	}

	public int getListType() {
		return listAttrPanel.getListType();
	}

	public void setListAttributes(Map attr) {
		listAttrPanel.setAttributes(attr);
	}

	public Map getListAttributes() {
		return listAttrPanel.getAttributes();
	}
}
