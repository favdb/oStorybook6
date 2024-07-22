/*
 * Copyright (C) 2024 favdb
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
package storybook.db.challenge;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import resources.icons.ICONS;
import storybook.db.book.BookUtil;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 * dialog for Challenge, inspired by Philippe <br>
 * in https://www.jeunesecrivains.com/t63749-outils-logiciels-tableau-de-bord-d-avancement-nanowrimo
 *
 * @author favdb
 */
public class ChallengeDlg extends AbsDialog {

	public static void show(MainFrame mainFrame) {
		ChallengeDlg dlg = new ChallengeDlg(mainFrame);
		dlg.setVisible(true);
	}

	public ChallengeDlg(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	/**
	 * initialize all data
	 */
	@Override
	public void init() {
		setTitle(I18N.getMsg("challenge"));
		mainFrame.project.challenge.lastWordsSet(BookUtil.getNbWords(mainFrame.project));;
		ChallengePanel challengePanel = new ChallengePanel(this);
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		add(challengePanel, MIG.GROW);
		JButton btClose = Ui.initButton("btClose", "close", ICONS.K.EMPTY, "", e -> dispose());
		add(btClose, MIG.get(MIG.SPAN, MIG.RIGHT));
		repack();
	}

	/**
	 * repack the JDialog
	 */
	public void repack() {
		this.pack();
		this.setLocationRelativeTo(mainFrame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
