/*
 * Copyright (C) 2025 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.ui.panels.script;

import storybook.db.scene.SceneScript;
import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import storybook.db.scene.SceneEdit;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.BNONE;
import storybook.ui.panels.AbstractPanel;

/**
 * panel class to manage script data
 *
 * @author favdb
 */
public class ScriptPanel extends AbstractPanel implements CaretListener {

	private SceneScript script;
	private ShefEditor desc, audio, visual, voice;
	private JTextField scDuration;
	private SceneEdit editor;

	public ScriptPanel(MainFrame mainFrame, SceneScript script, SceneEdit editor) {
		super(mainFrame);
		this.script = script;
		this.editor = editor;
		initAll();
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
		//empty
	}

	/**
	 * initialize the user interface
	 */
	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.GAP0, MIG.WRAP + " 2")));
		JPanel gpDate = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.INS0)));
		//gpDate.add(new JLabel(I18N.getColonMsg("duration")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		scDuration = Ui.initStringField(gpDate, "duration", 16, editor.getTfDuration().getText(), BNONE);
		scDuration.addCaretListener(e -> {
			editor.setTfDuration(scDuration.getText());
		});
		gpDate.add(scDuration);
		add(gpDate, MIG.SPAN);
		add(desc = initField("desc", script.getDesc()), MIG.get(MIG.SPAN, MIG.GROW));
		add(visual = initField("visual", script.getVisual()), MIG.GROW);
		add(audio = initField("audio", script.getAudio()), MIG.GROW);
		add(voice = initField("voice", script.getVoice()), MIG.get(MIG.SPAN, MIG.GROW));
	}

	/**
	 * initialize a field
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	private ShefEditor initField(String key, String value) {
		ShefEditor s = new ShefEditor(mainFrame.project.getPath(), "lang_all, allow, colored", value);
		s.hideStatus(true);
		s.setMaxLen(7000);
		SwingUtil.setMaxPreferredSize(s);
		s.setCaretPosition(0);
		s.getWysiwyg().setShowHideTB(true);
		s.setBorder(BorderFactory.createTitledBorder(I18N.getColonMsg("script." + key)));
		return s;
	}

	/**
	 * set the description
	 *
	 * @param value
	 */
	public void setDesc(String value) {
		desc.setText(value);
		desc.setCaretPosition(0);
	}

	/**
	 * get the description
	 *
	 * @return
	 */
	public String getDesc() {
		return desc.getText();
	}

	public SceneScript getScript() {
		SceneScript s = new SceneScript();
		s.setDesc(getScriptShef(desc));
		s.setAudio(getScriptShef(audio));
		s.setVisual(getScriptShef(visual));
		s.setVoice(getScriptShef(voice));
		return s;
	}

	/**
	 * get the script ShefEditor
	 *
	 * @param s
	 * @return
	 */
	private String getScriptShef(ShefEditor s) {
		String v = s.getText().trim();
		if (v.startsWith(SceneScript.EMPTY_LINE)) {
			v = v.substring(SceneScript.EMPTY_LINE.length());
		}
		return v;
	}

	/**
	 * model property change
	 *
	 * @param evt
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	/**
	 * action performed
	 *
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	/**
	 * get the duration
	 *
	 * @return
	 */
	public JTextField getScDuration() {
		return scDuration;
	}

	/**
	 * set the duration
	 *
	 * @param value
	 */
	public void setScDuration(String value) {
		scDuration.removeCaretListener(this);
		scDuration.setText(value);
		scDuration.addCaretListener(this);
	}

	/**
	 * caret update action
	 *
	 * @param e
	 */
	@Override
	public void caretUpdate(CaretEvent e) {
		editor.setTfDuration(scDuration.getText());
	}
}
