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
import java.util.Map;
import javax.swing.Icon;
import i18n.I18N;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * Dialogue pour modifier le style
 *
 * @author favdb
 */
public class ElementStyleDialog extends OptionDialog {

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.PENCIL);
	private static String title = I18N.getMsg("shef.element_style");
	private static String desc = I18N.getMsg("shef.element_style_desc");
	private StyleAttributesPanel stylePanel;

	public ElementStyleDialog(Frame parent) {
		super(parent, title, desc, icon);
		init();
	}

	public ElementStyleDialog(Dialog parent) {
		super(parent, title, desc, icon);
		init();
	}

	private void init() {
		stylePanel = new StyleAttributesPanel();
		setContentPane(stylePanel);
		pack();
		setSize(300, getHeight());
		setResizable(false);
	}

	public void setStyleAttributes(Map attr) {
		stylePanel.setAttributes(attr);
	}

	public Map getStyleAttributes() {
		return stylePanel.getAttributes();
	}

}
