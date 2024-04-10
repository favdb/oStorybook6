/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.actions.CompoundManager;
import api.shef.tools.HtmlUtils;
import storybook.shortcut.Shortcuts;

/**
 * Action which toggles inline HTML elements
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLInlineAction extends HTMLTextEditAction {

	public static final int EM = 0,
		STRONG = 1,
		CODE = 2,
		CITE = 3,
		SUP = 4,
		SUB = 5,
		BOLD = 6,
		ITALIC = 7,
		UNDERLINE = 8,
		STRIKE = 9;

	public static final String[] TYPES = {
		"highlight",
		"strong",
		"code",
		"cite",
		"superscript",
		"subscript",
		"bold",
		"italic",
		"underline",
		"strike"
	};

	private int type;

	/**
	 * Creates a new HTMLInlineAction
	 *
	 * @param editor
	 * @param itype an inline element type (BOLD, ITALIC, STRIKE, etc)
	 * @throws IllegalArgumentException
	 */
	public HTMLInlineAction(WysiwygEditor editor, int itype) throws IllegalArgumentException {
		super(editor, "");
		type = itype;
		if (type < 0 || type >= TYPES.length) {
			throw new IllegalArgumentException("Illegal Argument");
		}
		putValue(NAME, TYPES[type]);
		setShortDescription(Shortcuts.getTooltips("shef", TYPES[type]));
		Icon ico = null;
		switch (type) {
			case EM:
				ico = IconUtil.getIconSmall(ICONS.K.HIGHLIGHTER);
				break;
			case BOLD:
				ico = IconUtil.getIconSmall(ICONS.K.TX_BOLD);
				break;
			case ITALIC:
				ico = IconUtil.getIconSmall(ICONS.K.TX_ITALIC);
				break;
			case UNDERLINE:
				ico = IconUtil.getIconSmall(ICONS.K.TX_UNDERLINE);
				break;
			case STRIKE:
				ico = IconUtil.getIconSmall(ICONS.K.TX_STRIKE);
				break;
			case SUB:
				ico = IconUtil.getIconSmall(ICONS.K.TX_SUBSCRIPT);
				break;
			case SUP:
				ico = IconUtil.getIconSmall(ICONS.K.TX_SUPERSCRIPT);
				break;
			default:
				break;
		}
		setSmallIcon(ico);
		setAccelerator(Shortcuts.getKeyStroke("shef", TYPES[type]));
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		setSelected(isDefined(HtmlUtils.getCharacterAttributes(ed)));
	}

	public HTML.Tag getTag() {
		return getTagForType(type);
	}

	private HTML.Tag getTagForType(int type) {
		HTML.Tag tag = null;
		switch (type) {
			case EM:
				tag = HTML.Tag.EM;
				break;
			case STRONG:
				tag = HTML.Tag.STRONG;
				break;
			case CODE:
				tag = HTML.Tag.CODE;
				break;
			case SUP:
				tag = HTML.Tag.SUP;
				break;
			case SUB:
				tag = HTML.Tag.SUB;
				break;
			case CITE:
				tag = HTML.Tag.CITE;
				break;
			case BOLD:
				tag = HTML.Tag.B;
				break;
			case ITALIC:
				tag = HTML.Tag.I;
				break;
			case UNDERLINE:
				tag = HTML.Tag.U;
				break;
			case STRIKE:
				tag = HTML.Tag.STRIKE;
				break;
		}
		return tag;
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		CompoundManager.beginCompoundEdit(editor.getDocument());
		toggleStyle(editor);
		CompoundManager.endCompoundEdit(editor.getDocument());
	}

	private boolean isDefined(AttributeSet attr) {
		boolean hasSC = false;
		switch (type) {
			case EM:
				hasSC = isEM(attr);
				break;
			case BOLD:
				hasSC = StyleConstants.isBold(attr);
				break;
			case ITALIC:
				hasSC = StyleConstants.isItalic(attr);
				break;
			case STRIKE:
				hasSC = StyleConstants.isStrikeThrough(attr);
				break;
			case SUB:
				hasSC = StyleConstants.isSubscript(attr);
				break;
			case SUP:
				hasSC = StyleConstants.isSuperscript(attr);
				break;
			case UNDERLINE:
				hasSC = StyleConstants.isUnderline(attr);
				break;
			default:
				break;
		}
		return hasSC || (attr.getAttribute(getTag()) != null);
	}

	private void toggleStyle(JEditorPane editor) {
		MutableAttributeSet attr = new SimpleAttributeSet();
		attr.addAttributes(HtmlUtils.getCharacterAttributes(editor));
		boolean enable = !isDefined(attr);
		HTML.Tag tag = getTag();
		if (enable) {
			attr = new SimpleAttributeSet();
			attr.addAttribute(tag, new SimpleAttributeSet());
			//doesn't replace any attribs, just adds the new one
			HtmlUtils.setCharacterAttributes(editor, attr);
		} else {
			//Kind of a ham-fisted way to do this, but sometimes there are
			//CSS attributes, someties there are HTML.Tag attributes, and sometimes
			//there are both. So, we have to remove 'em all to make sure this type
			//gets completely disabled
			//remove the CSS style
			//STRONG, EM, CITE, CODE have no CSS analogs
			switch (type) {
				case BOLD:
					HtmlUtils.removeCharacterAttribute(editor, CSS.Attribute.FONT_WEIGHT, "bold");
					break;
				case ITALIC:
					HtmlUtils.removeCharacterAttribute(editor, CSS.Attribute.FONT_STYLE, "italic");
					break;
				case UNDERLINE:
					HtmlUtils.removeCharacterAttribute(editor, CSS.Attribute.TEXT_DECORATION, "underline");
					break;
				case STRIKE:
					HtmlUtils.removeCharacterAttribute(editor, CSS.Attribute.TEXT_DECORATION, "line-through");
					break;
				case SUP:
					HtmlUtils.removeCharacterAttribute(editor, CSS.Attribute.VERTICAL_ALIGN, "sup");
					break;
				case SUB:
					HtmlUtils.removeCharacterAttribute(editor, CSS.Attribute.VERTICAL_ALIGN, "sub");
					break;
				case EM:
					break;
				default:
					break;
			}
			//make certain the tag is also removed
			HtmlUtils.removeCharacterAttribute(editor, tag);
		}
		setSelected(enable);
	}

	private boolean isEM(AttributeSet attr) {
		Color em = (Color) attr.getAttribute(StyleConstants.Background);
		if (em != null && em.equals(Color.YELLOW)) {
			return true;
		}
		return false;
	}

}
