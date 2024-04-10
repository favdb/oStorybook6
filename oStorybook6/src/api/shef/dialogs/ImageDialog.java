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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Icon;
import resources.icons.ICONS;
import resources.icons.IconUtil;

public class ImageDialog extends HTMLOptionDialog {

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.IMAGE);
	private static String title = I18N.getMsg("shef.image");
	private static String desc = I18N.getMsg("shef.image_desc");
	private ImagePanel imagePanel;

	public ImageDialog(Frame parent) {
		super(parent, title, desc, icon);
		init();
	}

	public ImageDialog(Dialog parent) {
		super(parent, title, desc, icon);
		init();
	}

	private void init() {
		imagePanel = new ImagePanel();
		setContentPane(imagePanel);
		pack();
		setResizable(false);
	}

	public void setImageAttributes(Map attr) {
		imagePanel.setAttributes(attr);
	}

	public Map getImageAttributes() {
		return imagePanel.getAttributes();
	}

	private String createImgAttributes(Map ht) {
		String html = "";
		for (Iterator e = ht.keySet().iterator(); e.hasNext();) {
			Object k = e.next();
			if (k.toString().equals("a") || k.toString().equals("name")) {
				continue;
			}
			html += " " + k + "=" + "\"" + ht.get(k) + "\"";
		}
		return html;
	}

	/**
	 * get the HTML code for the choosen image
	 *
	 * @return
	 */
	@Override
	public String getHTML() {
		Map imgAttr = imagePanel.getAttributes();
		boolean hasLink = imgAttr.containsKey("a");
		String html = "";
		if (hasLink) {
			html = "<a " + imgAttr.get("a") + ">";
		}
		// copy image to current MainFrame Images directory
		String imgLink = "<img" + createImgAttributes(imgAttr) + ">";
		Container c = this.getParent();
		if (imgAttr.containsKey("src")) {
			while (c != null) {
				if (c.getClass().getSimpleName().equals("MainFrame")) {
					String src = imgAttr.get("src").toString();
					String dest = ((storybook.ui.MainFrame) c).imageCopy(src);
					imgLink = imgLink.replace(src, dest);
					break;
				}
				c = c.getParent();
			}
		}
		html += imgLink;
		if (hasLink) {
			html += "</a>";
		}
		return html;
	}

}
