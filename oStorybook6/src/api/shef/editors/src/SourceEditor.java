/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.src;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.ShefEditor;
import api.shef.actions.manager.ActionList;
import api.shef.editors.AbstractPanel;
import i18n.I18N;

/**
 *
 * @author favdb
 */
public class SourceEditor extends AbstractPanel {

	public JTextComponent srcEditor;
	private final ShefEditor editor;

	public SourceEditor(ShefEditor parent) {
		super(parent);
		this.editor = parent;
		setName("source");
		initAll();
	}

	@Override
	public void init() {
		setLayout(new BorderLayout());
		setName(ShefEditor.SOURCE);
		setOpaque(true);
	}

	@Override
	public void initUi() {
		initToolbar();
		add(toolbar, BorderLayout.PAGE_START);
		srcEditor = new JEditorPane();
		try {
			if (editor.isColored()) {
				srcEditor = new RSyntaxTextArea();
				((RSyntaxTextArea) srcEditor).setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
			} else {
				srcEditor = new JTextArea();
			}
		} catch (NoClassDefFoundError ex) {
			srcEditor = new JTextArea();
		}
		if (srcEditor instanceof JTextArea) {
			((JTextArea) srcEditor).setWrapStyleWord(true);
			((JTextArea) srcEditor).setLineWrap(true);
		}
		JScrollPane scroll = new JScrollPane(srcEditor);
		add(scroll, BorderLayout.CENTER);
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		actionList = new ActionList("editor-actions");
		// button to indent/unindent text
		JButton bt = initButton("indent", "", ICONS.K.AL_RIGHT.toString(), "indent");
		bt.addActionListener(e -> {
			indent(3);
		});
		toolbar.add(bt);
		bt = initButton("unindent", "", ICONS.K.AL_LEFT.toString(), "unindent");
		bt.addActionListener(e -> {
			unindent();
		});
		toolbar.add(bt);
		// button to go to Wysiwyg mode
		bt = new JButton(IconUtil.getIconSmall(ICONS.K.PENCIL));
		bt.setName(ShefEditor.WYSIWYG);
		bt.setText("");
		bt.setToolTipText(I18N.getMsg("shef." + ShefEditor.WYSIWYG));
		bt.addActionListener(e -> {
			editor.changeTo(ShefEditor.WYSIWYG);
		});
		toolbar.add(bt);
		return toolbar;
	}

	@Override
	public void setText(String texte) {
		Document doc = Jsoup.parse(texte);
		doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
		srcEditor.setText(doc.body().html());
		srcEditor.setCaretPosition(0);
	}

	@Override
	public void setCaretPosition(int position) {
		srcEditor.setCaretPosition(position);
	}

	public String getText() {
		Document doc = Jsoup.parse(srcEditor.getText());
		doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
		return doc.body().html();
	}

	private void indent(int n) {
		String html = srcEditor.getText();
		int pos = srcEditor.getCaretPosition();
		api.jsoup.nodes.Document doc = Jsoup.parse(html);
		doc.outputSettings().indentAmount(4).outline(true);
		srcEditor.setText(doc.body().html());
		srcEditor.setCaretPosition(pos);
	}

	private void unindent() {
		String texte = srcEditor.getText();
		int pos = srcEditor.getCaretPosition();
		String t[] = texte.split("\n");
		StringBuilder b = new StringBuilder();
		for (String s : t) {
			b.append(s.trim()).append("\n");
		}
		srcEditor.setText(b.toString());
		srcEditor.setCaretPosition(pos);
	}

	public JTextComponent getEditor() {
		return this.srcEditor;
	}

}
