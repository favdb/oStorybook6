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
package storybook;

import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import storybook.tools.swing.FontUtil;

/**
 * class for managing the fonts (def, book, editor, mono)
 *
 * @author favdb
 */
public class AppFont {

	private Font def, book, editor, mono;

	public AppFont() {
		// empty
	}

	/**
	 * reset the font to default
	 *
	 */
	public void reset() {
		if (def == null) {
			return;
		}
		FontUtil.setDefault(def);
	}

	/**
	 * get the default font
	 *
	 * @return
	 */
	public Font defGet() {
		if (def == null) {
			defReset();
		}
		return this.def;
	}

	/**
	 * set the default Font
	 *
	 * @param font
	 */
	public void defSet(Font font) {
		//LOG.trace("App.defSet(font="+(font!=null?font.toString():"null")+")");
		if (font == null) {
			JMenu e = new JMenu();
			def = e.getFont();
		}
		def = font;
		App.preferences.setString(Pref.KEY.FONT_DEFAULT, FontUtil.getString(def));
		reset();
	}

	/**
	 * restore the default font
	 *
	 */
	public void defReset() {
		//LOG.trace("App.fontRestoreDefault()");
		if (App.preferences == null) {
			App.preferences = new Pref();
		}
		String name = App.preferences.getString(Pref.KEY.FONT_DEFAULT, Const.FONT_DEFAULT);
		defSet(FontUtil.getFont(name));
	}

	/**
	 * restore the default font
	 *
	 */
	public void defRestore() {
		//LOG.trace("App.fontRestoreDefault()");
		if (App.preferences == null) {
			App.preferences = new Pref();
		}
		String name = App.preferences.getString(Pref.KEY.FONT_DEFAULT, Const.FONT_DEFAULT);
		defSet(FontUtil.getFont(name));
	}

	/**
	 * get the Book font
	 *
	 * @return
	 */
	public Font bookGet() {
		if (book == null) {
			bookRestore();
		}
		return this.book;
	}

	/**
	 * set the Book font
	 *
	 * @param font
	 */
	public void bookSet(Font font) {
		//LOG.trace("App.fontSetBook()");
		if (font == null) {
			JEditorPane e = new JEditorPane();
			book = e.getFont();
		} else {
			book = font;
		}
		App.preferences.setString(Pref.KEY.FONT_BOOK, FontUtil.getString(book));
	}

	/**
	 * restore the Book font
	 *
	 */
	public void bookRestore() {
		//LOG.trace("LOG.fontRestoreBook()");
		String name = App.preferences.getString(Pref.KEY.FONT_BOOK, Const.FONT_BOOK);
		bookSet(FontUtil.getFont(name));
	}

	/**
	 * get the editor font
	 *
	 * @return
	 */
	public Font editorGet() {
		if (editor == null) {
			editorRestore();
		}
		return this.editor;
	}

	/**
	 * set the editor font
	 *
	 * @param font
	 */
	public void editorSet(Font font) {
		//trace("LOG.fontSetEditor()");
		if (font == null) {
			JEditorPane e = new JEditorPane();
			editor = e.getFont();
		} else {
			editor = font;
		}
		App.preferences.setString(Pref.KEY.FONT_EDITOR, FontUtil.getString(editor));
	}

	/**
	 * restore the editor font
	 *
	 */
	public void editorRestore() {
		//trace("App.fontRestoreEditor()");
		String name = App.preferences.getString(Pref.KEY.FONT_EDITOR, Const.FONT_EDITOR);
		editorSet(FontUtil.getFont(name));
	}

	/**
	 * get the Monospaced font
	 *
	 * @return
	 */
	public Font monoGet() {
		if (mono == null) {
			monoRestore();
		}
		return this.mono;
	}

	/**
	 * set the Monospaced font
	 *
	 * @param font
	 */
	public void monoSet(Font font) {
		//LOG.trace(TT+".fontSetMono()");
		if (font == null) {
			JEditorPane e = new JEditorPane();
			mono = e.getFont();
		} else {
			mono = font;
		}
		App.preferences.setString(Pref.KEY.FONT_MONO, FontUtil.getString(mono));
	}

	/**
	 * restore the Monospaced font
	 */
	public void monoRestore() {
		//LOG.trace(TT+".fontRestoreMono()");
		String name = App.preferences.getString(Pref.KEY.FONT_MONO, Const.FONT_MONO);
		monoSet(FontUtil.getFont(name));
	}

}
