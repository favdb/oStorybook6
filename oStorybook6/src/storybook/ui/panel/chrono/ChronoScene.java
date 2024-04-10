/*
 * Copyright (C) 2022 favdb
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
package storybook.ui.panel.chrono;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JLabel;
import api.mig.swing.MigLayout;
import storybook.db.scene.Scene;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class ChronoScene extends AbstractPanel {

	private static final String TT = "ChronoScene";

	private final ChronoStrand strand;
	private final Scene scene;

	public ChronoScene(ChronoStrand strand, Scene scene) {
		super(strand.mainFrame);
		this.strand = strand;
		this.scene = scene;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new MigLayout());
		add(new JLabel(scene.getName()));
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {

	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

}
