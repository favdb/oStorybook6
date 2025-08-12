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
package storybook.ui.chart.occurences;

import i18n.I18N;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

/**
 *
 * @author favdb
 */
public class OccurencesPanel extends JPanel {

	private static final int DEFAULT_SIZE = GroupLayout.DEFAULT_SIZE,
			MAX_VALUE = Short.MAX_VALUE,
			PREFERRED_SIZE = GroupLayout.PREFERRED_SIZE;

	private final String titre;
	private final String titreX;
	private final String titreY;
	private final Dataset dataset;
	private JLabel lbXaxis;
	private JLabel lbYaxis;
	private JPanel panel;
	private Occurence occurence;

	/**
	 * Creates new form TimelinePanel
	 *
	 * @param title
	 * @param dataset
	 * @param tX
	 * @param tY
	 */
	public OccurencesPanel(String title, String tX, String tY, Dataset dataset) {
		setPreferredSize(new Dimension(800, 500));
		setName(title);
		this.titre = title;
		this.titreX = tX;
		this.titreY = tY;
		this.dataset = dataset;
		init();
	}

	private void init() {
		JLabel jTitre = new JLabel(titre);
		jTitre.setHorizontalAlignment(SwingConstants.CENTER);
		panel = new JPanel();
		panel.setBorder(null);
		GroupLayout panelLayout = new GroupLayout(panel);
		panel.setLayout(panelLayout);
		panelLayout.setHorizontalGroup(
				panelLayout.createParallelGroup(Alignment.LEADING)
						.addGap(0, 0, MAX_VALUE)
		);
		panelLayout.setVerticalGroup(
				panelLayout.createParallelGroup(Alignment.LEADING)
						.addGap(0, 0, MAX_VALUE)
		);

		lbXaxis = new JLabel();
		lbYaxis = new JLabel();
		lbYaxis.setUI(new VerticalLabelUI());
		if ("date".equals(titreX)) {
			lbXaxis.setText(I18N.getMsg(titreX));
			lbYaxis.setText(I18N.getMsg(titreY));
		} else {
			lbXaxis.setText(" ");
			lbYaxis.setText(" ");
		}
		occurence = new Occurence(titreX, dataset);
		panel.add(occurence);
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.LEADING)
						//.addComponent(jTitre, DEFAULT_SIZE, 800, MAX_VALUE)
						.addComponent(lbXaxis, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(lbYaxis, PREFERRED_SIZE, 24, PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(panel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE))
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								//.addComponent(jTitre)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(lbXaxis)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(Alignment.LEADING)
										.addComponent(panel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE)
										.addComponent(lbYaxis, DEFAULT_SIZE, 433, MAX_VALUE))
								.addContainerGap())
		);
	}

	@Override
	public void paintComponent(Graphics g) {
		occurence.setSize(panel.getWidth(), panel.getHeight());
		occurence.redraw();
	}
}
