/*
 * Copyright (C) 2017 favdb
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
package storybook.ui.panels.typist;

import storybook.ui.frames.scriber.ScriberPanel;
import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.MINIMUM_SIZE;

public class TypistEditText extends JDialog {

	private final String dirBase;
	private ShefEditor taText;
	public boolean canceled = false;
	private TypistPanel typist = null;
	private ScriberPanel scriber = null;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TypistEditText(TypistPanel typist, String msg, String text) {
		super(typist.getMainFrame().getFullFrame());
		this.typist = typist;
		dirBase = typist.getProject().getPath();
		setTitle(I18N.getMsg(msg));
		initialize();
		setText(text);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TypistEditText(ScriberPanel scriber, String msg, String text) {
		super(scriber.getMainFrame().getFullFrame());
		this.scriber = scriber;
		dirBase = scriber.getProject().getPath();
		setTitle(I18N.getMsg(msg));
		initialize();
		setText(text);
	}

	public void initialize() {
		setModal(true);
		setIconImage(IconUtil.getIconImageSmall("edit"));
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL)));
		taText = new ShefEditor(dirBase, "disallow, reduced", "");
		taText.setName("taText");
		JScrollPane scroller = new JScrollPane(taText);
		scroller.setMinimumSize(MINIMUM_SIZE);
		scroller.setMaximumSize(new Dimension(1024, 780));
		SwingUtil.setMaxPreferredSize(scroller);
		add(scroller);
		JPanel pb = new JPanel(new MigLayout());
		pb.add(Ui.initButton("btCancel", "cancel", ICONS.K.CANCEL, "", e -> {
			canceled = true;
			dispose();
		}));
		pb.add(Ui.initButton("btOk", "ok", ICONS.K.OK, "", e -> dispose()));
		add(pb, MIG.get(MIG.SPAN, MIG.RIGHT));
		pack();
		setLocationRelativeTo(null);
	}

	public void setText(String text) {
		taText.setText(text);
	}

	public String getText() {
		return taText.getText();
	}

	public boolean isCanceled() {
		return (canceled);
	}

}
