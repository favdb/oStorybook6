/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui.panel.chrono;

import api.mig.swing.MigLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import storybook.App;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.LOG;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabelVertical;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

@SuppressWarnings("serial")
public class ColumnPanel extends AbstractStrandDatePanel {

	private static final String TT = "ColumnPanel";

	public ColumnPanel(MainFrame mainFrame, Strand strand, Date date) {
		super(mainFrame, strand, date);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		try {
			setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.FILL), "[][]", "[top]"));
			setBorder(SwingUtil.getBorderDefault());
			setOpaque(false);
			setFont(App.fonts.defGet());
			// date
			StrandDateLabel lbDate = new StrandDateLabel(strand, date, true);
			lbDate.setUI(new JSLabelVertical(false));
			add(lbDate, MIG.get("cell 0 0", MIG.GROWY));
			// scenes by strand and date
			List<Scene> scenes = mainFrame.project.scenes.getWithDates();
			List<Scene> sceneList = new ArrayList<>();
			for (Scene scene : scenes) {
				if (scene.getDate() != null && scene.getDate().equals(date)) {
					sceneList.add(scene);
				}
			}
			if (sceneList.isEmpty()) {
				SpacePanel spacePanel = new SpacePanel(mainFrame, strand, date);
				add(spacePanel, MIG.GROW);
			} else {
				JPanel colPanel = new JPanel(new MigLayout(MIG.get(MIG.WRAP, MIG.INS0), "[]", "[top]"));
				colPanel.setOpaque(false);
				for (Scene scene : sceneList) {
					if (scene.getStrand() == null) {
						scene.setStrand((Strand) mainFrame.project.getList(Book.TYPE.STRAND).get(0));
					}
					add(new ChronoScenePanel(mainFrame, scene), MIG.GROW);
				}
				add(colPanel, MIG.GROW);
			}
		} catch (Exception e) {
			LOG.err(TT + ".initui() error", e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
