/*
 * Copyright (C) 2018 FaVdB
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
package api.shef.dialogs;

import i18n.I18N;
import java.awt.Dialog;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author FaVdB
 */
public class HTMLLinkInternalDialog extends HTMLOptionDialog {

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.LINKINTERNAL);
	private static String title = I18N.getMsg("shef.linkinternal");
	private static String desc = I18N.getMsg("shef.linkinternal_desc");
	private HTMLLinkInternalPanel linkPanel;
	private MainFrame mainFrame;

	public HTMLLinkInternalDialog(MainFrame m, Frame parent) {
		this(m, parent, title, desc, icon);
	}

	public HTMLLinkInternalDialog(MainFrame m, Dialog parent) {
		this(m, parent, title, desc, icon);
	}

	public HTMLLinkInternalDialog(MainFrame m, Dialog parent, String title, String desc, Icon ico) {
		super(parent, title, desc, ico);
		mainFrame = m;
		init();
	}

	public HTMLLinkInternalDialog(MainFrame m, Frame parent, String title, String desc, Icon ico) {
		super(parent, title, desc, ico);
		mainFrame = m;
		init();
	}

	public void setLinkText(String linkText) {
		linkPanel.setLinkText(linkText);
	}

	public String getLinkText() {
		return linkPanel.getLinkText();
	}

	@Override
	public String getHTML() {
		if ("".equals(linkPanel.getUrl())) {
			return (getLinkText());
		}
		return Html.intoA("", "#" + linkPanel.getUrl(), getLinkText());
	}

	private void init() {
		linkPanel = new HTMLLinkInternalPanel(mainFrame);
		linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setContentPane(linkPanel);
		setSize(315, 370);
		setResizable(false);
		this.pack();
	}

}
