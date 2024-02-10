/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

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
package storybook.ui.panel.book;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.db.status.StatusLabel;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class BookTextPanel extends AbstractPanel implements FocusListener {

	private static final String TT = "BookTextPanel",
	   CN_TITLE = "taTitle", CN_TEXT = "shef";
	private final Scene scene;
	private JLabel lbSceneNo;
	private StatusLabel lbStatus;
	private JTextField taTitle;
	private Dimension dimension;
	private JTextComponent tcText;

	public BookTextPanel(MainFrame mainFrame, Scene scene) {
		super(mainFrame);
		this.scene = scene;
		initAll();
	}

	private void setZoomedSize(int zoomValue) {
		int zv = (zoomValue);
		dimension = new Dimension(zv * 160, zv * 80);
	}

	@Override
	public void init() {
		setZoomedSize(App.preferences.bookGetZoom());
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "", "[][grow][grow]"));
		setBorder(SwingUtil.getBorderDefault());
		refresh();
	}

	@Override
	public void refresh() {
		removeAll();
		// scene number
		lbSceneNo = new JLabel(scene.getChapterSceneNo(false));
		lbSceneNo.setFont(FontUtil.getBold());
		add(lbSceneNo, MIG.get("split 3", MIG.GROWX));
		// informational
		JLabel lbInformational = new JLabel("");
		if (scene.getInformative()) {
			lbInformational.setIcon(IconUtil.getIconSmall(ICONS.K.INFO));
			lbInformational.setText(I18N.getMsg("informative"));
		}
		add(lbInformational, MIG.get(MIG.GROWX, MIG.CENTER));
		// scene status
		lbStatus = new StatusLabel(scene.getStatus(), false);
		add(lbStatus);
		// title
		JTextField tx = new JTextField();
		taTitle = new JTextField(scene.getTitle());
		taTitle.setName(CN_TITLE);
		taTitle.setCaretPosition(0);
		taTitle.setDragEnabled(true);
		taTitle.addFocusListener(this);
		taTitle.setEditable(false);
		taTitle.setBackground(tx.getBackground());
		taTitle.setForeground(tx.getForeground());
		add(taTitle, MIG.GROWX);
		// textArea
		tcText = initHtmlField(CN_TEXT, scene);
		JScrollPane textScroller = new JScrollPane(tcText);
		textScroller.setPreferredSize(dimension);
		textScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(textScroller);
		revalidate();
		repaint();
	}

	@Override
	public void focusGained(FocusEvent evt) {
		//LOG.trace(TT + ".focusGained(evt=" + evt.toString() + ")");
	}

	@Override
	public void focusLost(FocusEvent evt) {
		//LOG.trace(TT + ".focusLost(evt=" + evt.getSource().toString() + ")");
		if (evt.getSource() instanceof JComponent) {
			JComponent comp = (JComponent) evt.getSource();
			switch (comp.getName()) {
				case CN_TITLE:
					if (scene.getTitle().equals(taTitle.getText())) {
						return;
					}
					scene.setTitle(taTitle.getText());
					break;
				case CN_TEXT:
					String txt = tcText.getText();
					if (scene.getSummary().equals(txt)) {
						return;
					}
					scene.setSummary(txt);
					break;
				default:
					return;
			}
			mainFrame.getBookController().updateEntity(scene);
		}
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		ActKey act = new ActKey(evt);
		if (Book.TYPE.SCENE.compare(act.type) && Ctrl.PROPS.UPDATE.check(act.getCmd())) {
			Scene newScene = (Scene) newValue;
			if (!newScene.getId().equals(scene.getId())) {
				return;
			}
			refresh();
			return;
		}
		if (Ctrl.getPROPS(propName) == Ctrl.PROPS.BOOK_ZOOM) {
			setZoomedSize((Integer) newValue);
			refresh();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
