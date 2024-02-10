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
package storybook.db.challenge;

import api.mig.swing.MigLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class ChallengePanel extends AbstractPanel {

	private final Challenge challenge;

	public ChallengePanel(Challenge challenge) {
		super();
		this.challenge = challenge;
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		//todo
		this.setLayout(new MigLayout());
		initDate();
		initTable();
		initDiagram();
		initButtons();
	}

	private void initDate() {

	}

	private void initTable() {
		int nbrows = challenge.days;
	}

	private void initDiagram() {

	}

	private void initButtons() {

	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
