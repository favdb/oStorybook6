/*
 * Copyright (C) 2023 favdb
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
package api.shef.editors.wys;

import java.awt.Font;
import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.shortcut.Shortcuts;

/**
 *
 * @author favdb
 */
public class Zoom extends HTMLTextEditAction {

	private int zoom = 0;

	public Zoom(WysiwygEditor editor, int i) {
		super(editor, "zoom" + (i == 1 ? "in" : "out"));
		//LOG.trace("initialize Zoom(editor, i=" + i + ")");
		zoom = i;
		String str = (i == 1 ? "in" : "out");
		putValue(NAME, "zoom" + str);
		setShortDescription(Shortcuts.getTooltips("shef", "zoom" + str));
		setAccelerator(Shortcuts.getKeyStroke("shef", "zoom" + str));
		setSmallIcon(IconUtil.getIconSmall((zoom == 1 ? ICONS.K.ZOOM_IN : ICONS.K.ZOOM_OUT)));
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		setSelected(true);
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		//LOG.trace("zoom=" + zoom);
		Font fnt = editor.getFont();
		int sz = fnt.getSize();
		if (zoom == 1) {
			sz += 2;
		} else {
			sz -= 2;
		}
		if (sz < 8) {
			sz = 8;
		}
		Font fntz = new Font(fnt.getFontName(), fnt.getStyle(), sz);
		editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		editor.setFont(fntz);
		String bodyRule = "body { "//font-family: " + fntz.getFamily() + "; "
			+ "font-size: " + fntz.getSize() + "pt; }";
		((HTMLDocument) editor.getDocument()).getStyleSheet().addRule(bodyRule);
	}

}
