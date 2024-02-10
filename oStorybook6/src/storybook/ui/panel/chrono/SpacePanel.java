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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import api.mig.swing.MigLayout;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import i18n.I18N;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import resources.icons.ICONS;
import resources.icons.IconButton;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

@SuppressWarnings("serial")
public class SpacePanel extends AbstractPanel implements MouseListener {

	private Strand strand = null;
	private Date date = null;
	private AbstractAction newSceneAction;

	public SpacePanel(MainFrame mainFrame, Strand strand, Date date) {
		super(mainFrame);
		this.strand = strand;
		if (date instanceof Timestamp) {
			this.date = new Date(date.getTime());
		} else {
			this.date = date;
		}
		//refresh();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (Book.TYPE.STRAND.compare(act.type) && Ctrl.PROPS.UPDATE.check(act.getCmd())) {
			EntityUtil.refresh(mainFrame, strand);
			refresh();
		}
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setFocusable(true);
		setMinimumSize(new Dimension(150, 150));
		addMouseListener(this);
		Color color = Color.gray;
		if (strand != null) {
			color = strand.getJColor();
		}
		MigLayout layout = new MigLayout(MIG.FILL, "[center]", "[center]");
		setLayout(layout);
		setBorder(BorderFactory.createLineBorder(color, 2));
		setOpaque(false);

		IconButton btNewScene = new IconButton("btNewScene", ICONS.K.PLUS, getNewAction());
		btNewScene.setFlat();
		btNewScene.setToolTipText(I18N.getMsg("add.new"));
		add(btNewScene, MIG.CENTER);
	}

	private AbstractAction getNewAction() {
		if (newSceneAction == null) {
			newSceneAction = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Ctrl ctrl = mainFrame.getBookController();
					Scene scene = new Scene();
					scene.setStrand(strand);
					scene.setDate(date);
					ctrl.setEditEntity(scene);
				}
			};
		}
		return newSceneAction;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			newSceneAction.actionPerformed(null);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	public Strand getStrand() {
		return strand;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
