/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import api.shef.actions.manager.ActionList;
import api.shef.tools.HtmlUtils;
import api.shef.tools.LOG;

/**
 * Action which edits HTML font size
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLFontSizeAction extends HTMLTextEditAction {

	private static final String TT = "HTMLFontSizeAction";

	public static final int XXSMALL = 0;
	public static final int XSMALL = 1;
	public static final int SMALL = 2;
	public static final int MEDIUM = 3;
	public static final int LARGE = 4;
	public static final int XLARGE = 5;
	public static final int XXLARGE = 6;
	private static final String SML = I18N.getMsg("shef.fontsize.small");
	private static final String MED = I18N.getMsg("shef.fontsize.normal");
	private static final String LRG = I18N.getMsg("shef.fontsize.large");
	public static final int FONT_SIZES[] = {8, 10, 12, 14, 18, 24, 36/*28*/};
	public static final String SIZES[] = {
		"xx-" + SML, "x-" + SML, SML, MED,
		LRG, "x-" + LRG, "xx-" + LRG
	};
	private int size;

	public static ActionList createActionList(WysiwygEditor editor) {
		LOG.trace(TT + ".createActionList(editor)");
		ActionList list = new ActionList("font-size");
		int[] t = HTMLFontSizeAction.FONT_SIZES;
		for (int i = 0; i < t.length; i++) {
			list.add(new HTMLFontSizeAction(editor, i));
		}

		return list;
	}

	/**
	 * Creates a new HTMLFontSizeAction
	 *
	 * @param editor
	 * @param size one of the FONT_SIZES (XXSMALL, xSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE)
	 *
	 * @throws IllegalArgumentException
	 */
	public HTMLFontSizeAction(WysiwygEditor editor, int size) throws IllegalArgumentException {
		super(editor, "");
		if (size < 0 || size > 6) {
			throw new IllegalArgumentException("Invalid size");
		}
		this.size = size;
		putValue(NAME, SIZES[size]);
		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		LOG.trace(TT + ".updateWysiwygContextState(ed)");
		AttributeSet at = HtmlUtils.getCharacterAttributes(ed);
		if (at.isDefined(StyleConstants.FontSize)) {
			setSelected(at.containsAttribute(StyleConstants.FontSize, FONT_SIZES[size]));
		} else {
			setSelected(size == MEDIUM);
		}
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		Action a = new StyledEditorKit.FontSizeAction(SIZES[size], FONT_SIZES[size]);
		a.actionPerformed(e);
	}
}
