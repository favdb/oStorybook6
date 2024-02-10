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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import resources.icons.ICONS;
import resources.icons.IconUtil;

public class HyperlinkDialog extends HTMLOptionDialog {

	private static final String TT = "HyperlinkDialog";

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.LINK);
	private static String title = I18N.getMsg("shef.hyperlink");
	private static String desc = I18N.getMsg("shef.hyperlink_desc");
	private LinkPanel linkPanel;

	public HyperlinkDialog(Frame parent) {
		this(parent, title, desc, icon, true);
	}

	public HyperlinkDialog(Dialog parent) {
		this(parent, title, desc, icon, true);
	}

	public HyperlinkDialog(Dialog parent, String title, String desc, Icon ico, boolean urlFieldEnabled) {
		super(parent, title, desc, ico);
		init(urlFieldEnabled);
	}

	public HyperlinkDialog(Frame parent, String title, String desc, Icon ico, boolean urlFieldEnabled) {
		super(parent, title, desc, ico);
		init(urlFieldEnabled);
	}

	private void init(boolean urlFieldEnabled) {
		linkPanel = new LinkPanel(urlFieldEnabled);
		linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setContentPane(linkPanel);
		setMinimumSize(new Dimension(315, 370));
		pack();
		setResizable(false);
	}

	public Map getAttributes() {
		return linkPanel.getAttributes();
	}

	public void setAttributes(Map attribs) {
		//LOG.trace(TT + ".setAttributes(attribs=" + attribs.toString() + ")");
		linkPanel.setAttributes(attribs);
	}

	public void setLinkText(String text) {
		//LOG.trace(TT + ".setText(text=" + text + ")");
		linkPanel.setLinkText(text);
	}

	public String getLinkText() {
		return linkPanel.getLinkText();
	}

	@Override
	public String getHTML() {
		String html = "<a";
		Map ht = getAttributes();
		for (Iterator e = ht.keySet().iterator(); e.hasNext();) {
			Object k = e.next();
			html += " " + k + "=" + "\"" + ht.get(k) + "\"";
		}
		html += ">" + getLinkText() + "</a>";
		return html;
	}

}
