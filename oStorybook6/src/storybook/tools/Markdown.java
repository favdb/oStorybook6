/*
 * oStorybook: Libre software for novelists and authors.
 * Original idea Martin Mustun
 * Copyright (C) 2013 - 2020 The oStorybook Team
 *
 * This program is libre software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools;

import api.jortho.SpellChecker;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import resources.icons.ICONS;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneEdit;
import storybook.tools.html.CSS;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.typist.TypistPanel;
import storybook.ui.panel.typist.TypistScenario;

/**
 *
 * @author favdb
 */
public class Markdown extends JPanel implements ActionListener, CaretListener {

	private boolean modified = false;
	private String original;
	private AbstractPanel caller;
	private int maxLength;
	private JLabel lbMessage;
	private JToolBar toolbar;
	private JScrollPane textScroll, htmlScroll;
	private JButton btText, btHtml, btBoth;
	private String header = "", footer = "";
	public JTextArea textArea = new JTextArea();
	//private final JTextArea status = new JTextArea();
	public JEditorPane html = new JEditorPane();
	private String type = "text/plain";
	private int objsize = 32768, inisize = 0;

	public enum ACTION {
		BT_LEFT("btLeft"),
		BT_RIGHT("btRight"),
		BT_BOTH("btBoth"),
		NONE("none");
		final private String text;

		private ACTION(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}

		public boolean check(String prop) {
			return text.equals(prop);
		}
	}

	public static ACTION getAction(String str) {
		for (ACTION a : ACTION.values()) {
			if (a.toString().equals(str)) {
				return (a);
			}
		}
		return (ACTION.NONE);
	}

	public enum VIEW {
		TEXT,
		PREVIEW,
		ALL
	}

	/**
	 * Mardown class
	 *
	 * @param name: name of the caller
	 */
	public Markdown(String name) {
		super();
		setName(name);
		initAll();
	}

	/**
	 * Mardown class
	 *
	 * @param name: name of the caller
	 * @param type: type of textArea, may be textArea or html
	 * @param text: textArea content
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Markdown(String name, String type, String text) {
		this(name);
		setContentType(type);
		setText(text);
	}

	public void setView(VIEW view) {
		switch (view) {
			case TEXT:
				textScroll.setVisible(true);
				htmlScroll.setVisible(false);
				break;
			case PREVIEW:
				textScroll.setVisible(false);
				htmlScroll.setVisible(true);
				break;
			case ALL:
				textScroll.setVisible(true);
				htmlScroll.setVisible(true);
				break;
		}
	}

	public void resetModified() {
		modified = false;
	}

	public boolean isModified() {
		return (modified);
	}

	public void setContentType(String type) {
		this.type = (type.equals("text") ? "text/plain" : type);
		textScroll.setVisible(this.type.equals("text/plain"));
		htmlScroll.setVisible(!this.type.equals("text/plain"));
	}

	public String getContentType() {
		return (type);
	}

	public void setText(String text) {
		String str;
		if (text.contains("</p>")) {
			str = toMarkdown(text);
		} else {
			str = text;
		}
		textArea.setText(str);
		textArea.setCaretPosition(0);
		original = this.textArea.getText();
		inisize = original.length();
		modified = false;
		setHtml();
	}

	public String getText() {
		return (this.textArea.getText());
	}

	public void setHtml() {
		//App.trace("Markdown.setHtml()");
		StringBuilder buf = new StringBuilder("<html>");
		buf.append("<head>\n<style type='text/css'>\n");
		buf.append(CSS.forScenario(true));
		buf.append("</style></head>\n");
		buf.append("<body>");
		buf.append(getHtmlBody());
		buf.append("</body></html>");
		this.html.setText(buf.toString());
		updateState();
		html.setCaretPosition(0);
	}

	public String getHtml() {
		return (html.getText());
	}

	public String getHtmlBody() {
		StringBuilder buf = new StringBuilder();
		if (!header.isEmpty()) {
			buf.append(header);
		}
		buf.append(toHtml(this.textArea.getText()));
		if (!footer.isEmpty()) {
			buf.append(footer);
		}
		return (buf.toString());
	}

	public void setCaretPosition(int pos) {
		textArea.setCaretPosition(pos);
		try {
			html.setCaretPosition(pos);
		} catch (Exception e) {
		}
	}

	public int getCaretPosition() {
		return (textArea.getCaretPosition());
	}

	public Font getEditorFont() {
		return (textArea.getFont());
	}

	public void setEditorFont(Font font) {
		textArea.setFont(font);
		html.setFont(font);
	}

	public void setFocus() {
		textArea.requestFocus();
	}

	public void setHeader(Scene scene, boolean isScenario) {
		//App.trace("Markdown.setHeader(scene="+(scene==null?"null":scene.getScenarioHeader())+")");
		if (scene == null) {
			header = "";
			footer = "";
		} else {
			header = scene.getScenarioHeader(isScenario);
			footer = scene.getScenarioFooter();
		}
		setHtml();
	}

	private void initAll() {
		init();
		initUi();
	}

	private void init() {
		setFont(App.fonts.monoGet());
		setEditorFont(App.fonts.monoGet());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.addCaretListener(this);
		html.setContentType("text/html");
		html.setEditable(false);
	}

	private void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.INS0, MIG.GAP0, MIG.WRAP), "[grow][]"));
		//toolbar
		initToolbar();
		add(toolbar, MIG.get(MIG.GROW));
		add(Ui.initButton("btHelp", "", ICONS.K.HELP, "markdown.help"), MIG.get(MIG.SPAN, MIG.RIGHT));
		textScroll = new JScrollPane(textArea);
		SwingUtil.setMaxPreferredSize(textScroll);
		htmlScroll = new JScrollPane(html);
		SwingUtil.setMaxPreferredSize(htmlScroll);
		JPanel px = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3, MIG.INS0, MIG.GAP1)));
		px.add(textScroll, "grow");
		px.add(htmlScroll, "grow");
		add(px, MIG.get(MIG.SPAN, MIG.GROW));
		setContentType("text/plain");
		modified = false;
		lbMessage = new JLabel("", JLabel.RIGHT);
		add(lbMessage, "shrink, pos null null 100% 100%");
	}

	private void initToolbar() {
		toolbar = new JToolBar();
		toolbar.setLayout(new MigLayout(MIG.get("ins 1 3 1 3")));
		toolbar.setFloatable(false);
		// show textArea
		btText = Ui.initButton(ACTION.BT_LEFT.toString(),
				"", ICONS.K.SCREEN_NORMAL, "screen.text", this);
		toolbar.add(btText);
		btHtml = Ui.initButton(ACTION.BT_RIGHT.toString(),
				"", ICONS.K.SCREEN_RIGHT, "screen.preview", this);
		toolbar.add(btHtml);
		btBoth = Ui.initButton(ACTION.BT_BOTH.toString(),
				"", ICONS.K.SCREEN_BOTH, "screen.both", this);
		toolbar.add(btBoth);
		//toolbar.add(Ui.initButton("btHelp", "", ICONS.K.HELP, "markdown.help"), MIG.RIGHT);
	}

	public void hideToolbar() {
		toolbar.setVisible(false);
	}

	public void setDimension() {
		textArea.setColumns(80);
	}

	public void setCallback(AbstractPanel caller) {
		this.caller = caller;
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		updateState();
		if (!textArea.getText().equals(original)) {
			modified = true;
			if (caller != null) {
				if (caller instanceof TypistPanel) {
					((TypistPanel) caller).updatedSet();
				}
				if (caller instanceof TypistScenario) {
					((TypistScenario) caller).setModified();
				}
			}
		} else {
			modified = false;
		}
		setHtml();
		try {
			if (e.getDot() >= 0 && textArea != null && html != null) {
				int i = e.getDot();
				if (html.getText().length() > i) {
					html.setCaretPosition(i);
				}
			}
		} catch (Exception exc) {
		}
	}

	int nbChange = 0;

	public void checkCallback() {
		if (nbChange > 20) {
			if (caller instanceof TypistPanel || caller instanceof TypistScenario) {
				((TypistPanel) caller).tempSave();
				nbChange = 0;
			}
			if (caller instanceof SceneEdit) {
				((SceneEdit) caller).tempSave();
				nbChange = 0;
			}
		}
		nbChange++;
	}

	private void updateState() {
		if (maxLength > 0) {
			int len = maxLength - getText().length() - 1;
			if (len < 0) {
				lbMessage.setForeground(Color.red);
			}
			if (len < 100) {
				lbMessage.setForeground(Color.orange);
			} else {
				lbMessage.setForeground(Color.black);
			}
			lbMessage.setText(I18N.getMsg("editor.letters.left", (len + "/" + maxLength))
					+ ", " + I18N.getMsg("editor.words") + ": " + TextUtil.countWords(getText()));
		}
	}

	public JPopupMenu initSpelling(MainFrame mainFrame) {
		// create popup menu
		JPopupMenu pop = new JPopupMenu();
		// spell checker
		String spelling = App.preferences.getString(Pref.KEY.SPELLING);
		if (!Const.Spelling.none.name().equals(spelling)) {
			pop.add(SpellChecker.createCheckerMenu());
			pop.add(SpellChecker.createLanguagesMenu());
			pop.addSeparator();
			SpellChecker.register(textArea);
		}
		return (pop);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//App.trace("Mardown.actionPerformed(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JButton) {
			JButton btn = (JButton) evt.getSource();
			switch (getAction(btn.getName())) {
				case BT_LEFT: {
					setView(VIEW.TEXT);
					break;
				}
				case BT_RIGHT: {
					setView(VIEW.PREVIEW);
					break;
				}
				case BT_BOTH: {
					setView(VIEW.ALL);
					break;
				}
				default:
					return;
			}
			revalidate();
		}
	}

	public static String toHtml(String mark) {
		if (mark == null || mark.isEmpty()) {
			return ("");
		}
		boolean isBold = false,
				isUnderline = false,
				isItalic = false,
				isDidascalie = false,
				isDialog = false,
				isTitle = false,
				isPara = false;
		int title = 0;
		String[] lines = mark.split("\\r?\\n");
		StringBuilder html = new StringBuilder();
		StringBuilder b = new StringBuilder(html);
		for (String li : lines) {
			StringBuilder x = new StringBuilder();
			String line = li.trim();
			if (isTitle) {
				x.append(String.format("</h%d>\n", title));
				title = 0;
				isTitle = false;
				isPara = false;
			} else if (line.length() == 0) {
				if (isDidascalie) {
					b.append("</p>\n");
					isDidascalie = false;
					isPara = false;
				} else if (isDialog) {
					b.append("</p>\n");
					isDialog = false;
					isPara = false;
				} else {
					if (isPara) {
						b.append("</p>\n");
						isPara = false;
					}
				}
				continue;
			}
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				switch (c) {
					case '$':
						if (line.substring(i).startsWith("$$")) {
							x.append("$");
							i++;
						} else {
							x.append("<br>");
						}
						break;
					case '*': // start/end bold
						if (i == 0 && !isBold && !isItalic) {
							if (isPara) {
								x.append("</p>\n<p>");
								isPara = false;
							} else {
								x.append("<p>");
								isPara = true;
							}
						}
						if (line.substring(i).startsWith("***")) {
							if (isItalic && isBold) {
								x.append("</b></i>");
								isItalic = false;
							} else if (isItalic && !isBold) {
								x.append("</i><b>");
								isItalic = false;
								isBold = true;
							} else if (!isItalic && isBold) {
								x.append("</b><i>");
								isItalic = true;
								isBold = false;
							} else {
								x.append("<b><i>");
								isItalic = true;
								isBold = true;
							}
							i += 2;
							break;
						} else if (line.substring(i).startsWith("**")) {
							x.append((isBold ? "</b>" : "<b>"));
							isBold = !isBold;
							i++;
							break;
						} else {
							if (isItalic) {
								x.append("</i>");
								isItalic = false;
							} else {
								x.append("<i>");
								isItalic = true;
							}
							break;
						}
					case '_': //start/end underline
						if (i == 0 && !isPara) {
							x.append("<p>");
							isPara = true;
						}
						if (isUnderline) {
							x.append("</u>");
							isUnderline = false;
						} else {
							x.append("<u>");
							isUnderline = true;
						}
						break;
					case '#': // start/end header type
						if (isPara && !isTitle) {
							x.append("</p>\n");
							isPara = false;
						}
						int j = 0;
						for (; j < line.length(); j++) {
							if (i + j >= line.length()) {
								break;
							}
							if (line.charAt(i + j) != '#') {
								break;
							}
						}
						title = j;
						x.append(String.format("<h%d>", title));
						i += j;
						isTitle = true;
						break;
					case '(': // start didascalie (italic)
						if (line.substring(i).startsWith("((")) {
							x.append("(");
							i++;
						} else if (i == 0) {
							if (isPara) {
								x.append("</p>\n<p>");
							} else {
								x.append("<p>");
							}
							x.append("<i>");
							isDidascalie = true;
							isPara = true;
						} else {
							x.append("<i>");
						}
						break;
					case ')': // end didascalie
						if (line.substring(i).startsWith("))")) {
							x.append(")");
							i++;
						} else {
							x.append("</i>");
							if (i + 1 < line.length()) {
								if (line.charAt(i + 1) == ' ') {
									x.append("<br />");
								}
							}
						}
						break;
					case '/': // start/end dialog
						if (line.substring(i).startsWith("//")) {
							x.append("/");
							i++;
							break;
						}
						if (i == 0) {
							if (isPara) {
								x.append("</p");
							}
							x.append("<p style=\"text-align:center;margin-left: 1cm;margin-right: 1cm;\"><b>");
							isDialog = true;
							isPara = true;
						} else if (isDialog) {
							x.append("</b><br>\n");
							isDialog = false;
							isPara = true;
						} else {
							x.append(c);
						}
						break;
					case '-': // if double then mdash
						if (line.substring(i).startsWith("--")) {
							if (i == 0 && !isPara) {
								x.append("<p>");
								isPara = true;
							}
							x.append("&mdash;");
							i++;
							break;
						} else {
							x.append("-");
						}
						break;
					default:
						if (!isPara && !isTitle) {
							x.append("<p>");
							isPara = true;
						}
						x.append(c);
				}
			}
			b.append(x.toString());
		}
		int i = b.toString().lastIndexOf("<p");
		int j = b.toString().lastIndexOf("</p");
		if (j < i) {
			b.append("</p>");
		}
		return (b.toString());
	}

	public static String toMarkdown(String html) {
		if (html == null || html.isEmpty()) {
			return ("");
		}
		String r = html;
		r = r.replace("<i>", "*").replace("</i>", "*");
		r = r.replace("<b>", "**").replace("</b>", "**");
		r = r.replace("<u>", "_").replace("</u>", "_");
		r = r.replace("&mdash;", "--");
		r = Html.htmlToText(r);
		r = r.replace("\n", "\n\n");
		return (r);
	}

}
