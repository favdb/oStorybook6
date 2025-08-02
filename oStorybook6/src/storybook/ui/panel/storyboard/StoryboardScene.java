/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2016 FaVdB

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
package storybook.ui.panel.storyboard;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.db.status.StatusLabel;
import storybook.db.strand.Strand;
import storybook.tools.DateUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractGradientPanel;
import storybook.ui.panel.EntityLinksPanel;

@SuppressWarnings("serial")
public class StoryboardScene extends AbstractGradientPanel implements FocusListener {

	private static final String TT = "StoryboardScenePanel";

	private final String CN_UPPER_PANEL = "upperPanel";
	private JPanel upperPanel;
	private JLabel lbStatus;
	private JLabel lbInformational;
	private JLabel lbSceneNo;
	private JLabel lbTime;
	protected Scene scene;
	protected JButton btNew;
	protected JButton btEdit;
	protected JButton btDelete;
	private boolean horizontal;
	private Dimension dim;

	public StoryboardScene(MainFrame mainFrame, Scene scene) {
		super(mainFrame, true, Color.white, Strand.getJColor(scene.getStrand()));
		this.mainFrame = mainFrame;
		this.scene = scene;
		initAll();
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	private void getNewAction() {
		//LOG.trace(TT + "getNewAction()");
		Scene newScene = new Scene();
		newScene.setStrand(scene.getStrand());
		newScene.setScenets(scene.getScenets());
		newScene.setDate(new Date());
		if (scene.hasChapter()) {
			newScene.setChapter(scene.getChapter());
		}
		mainFrame.showEditorAsDialog(newScene);
	}

	protected JButton getEditButton() {
		if (btEdit != null) {
			return btEdit;
		}
		btEdit = Ui.initButton("btEdit", "", ICONS.K.EDIT, "edit",
				(evt -> mainFrame.showEditorAsDialog(scene)));
		SwingUtil.setFixedSize(btEdit, IconUtil.getDefDim());
		return btEdit;
	}

	protected JButton getDeleteButton() {
		if (btDelete != null) {
			return btDelete;
		}
		btDelete = Ui.initButton("btDelete", "", ICONS.K.DELETE, "delete",
				e -> EntityUtil.delete(mainFrame, scene));
		SwingUtil.setFixedSize(btDelete, IconUtil.getDefDim());
		return btDelete;
	}

	protected JButton getNewButton() {
		if (btNew != null) {
			return btNew;
		}
		btNew = Ui.initButton("btNew", "", ICONS.K.NEW, "new", (evt -> getNewAction()));
		SwingUtil.setFixedSize(btNew, IconUtil.getDefDim());
		return btNew;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case STRAND:
				if (isUpdate(act)) {
					EntityUtil.refresh(mainFrame, scene.getStrand());
					repaint();
					return;
				}
				break;
			case SCENE:
				if (isUpdate(act)) {
					Scene newScene = (Scene) newValue;
					if (!newScene.getId().equals(scene.getId())) {
						// not this scene
						return;
					}
					scene = newScene;
					lbSceneNo.setText(scene.getChapterSceneNo(false));
					lbSceneNo.setToolTipText(scene.getChapterSceneToolTip());
					lbStatus.setIcon(scene.getStatusIcon());
					if (scene.hasScenets()) {
						if (!DateUtil.isZeroTimeDate(scene.getScenets())) {
							DateFormat formatter = I18N.getDateTimeFormatter();
							lbTime.setText(formatter.format(scene.getScenets()));
						} else {
							lbTime.setText("");
						}
					}
					return;
				}
				break;
			default:
				break;
		}
		if (PROPS.STORYBOARD_ZOOM.check(propName)) {
			refresh();
		}
	}

	@Override
	public void init() {
		horizontal = App.preferences.storyboardGetDirection();
	}

	@Override
	public void initUi() {
		refresh();
	}

	@Override
	public void refresh() {
		ImageIcon img = IconUtil.getImageIcon(horizontal ? "striph" : "stripv");
		//img = IconUtil.resizeIcon(img, IconUtil.getDefSize());
		if (horizontal) {
			setLayout(new MigLayout(
					MIG.get(MIG.FILL, MIG.FLOWY, MIG.INS0, MIG.GAP0),
					"[grow]"));
			dim = new Dimension(img.getIconWidth(), img.getIconWidth());
		} else {
			setLayout(new MigLayout(
					MIG.get(MIG.FILL, MIG.FLOWX, MIG.INS0, MIG.GAP0),
					"[]", "[][grow]"));
			dim = new Dimension(img.getIconHeight(), img.getIconHeight());
		}
		setPreferredSize(dim);
		setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO));
		removeAll();
		add(new JLabel(img));
		add(initPanel());
		add(new JLabel(img));
		revalidate();
		repaint();
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel(new MigLayout(
				MIG.get(MIG.FILL, MIG.FLOWY, MIG.INS0, MIG.GAP0),
				"[]", "[][grow][]"));
		panel.setOpaque(false);
		panel.setBorder(SwingUtil.getBorderDefault(4));
		panel.setMinimumSize(dim);

		// chapter and scene number
		lbSceneNo = new JLabel("", SwingConstants.LEFT);
		lbSceneNo.setText(scene.getChapterSceneNo(false));
		lbSceneNo.setToolTipText(scene.getChapterSceneToolTip());

		upperPanel = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1), "[grow][][][]"));
		upperPanel.setName(CN_UPPER_PANEL);
		upperPanel.setBackground(Color.WHITE);
		upperPanel.setOpaque(false);
		upperPanel.setPreferredSize(dim);

		//upper stamp
		JLabel lbTitle = new JLabel(scene.getName());
		lbTitle.setMaximumSize(new Dimension(FontUtil.getWidth() * 10, FontUtil.getHeight()));
		lbTitle.setToolTipText(scene.getName());
		lbTitle.setBorder(SwingUtil.getBorderDefault());
		if (!Html.isEmpty(scene.getNotes())
				|| !Html.isEmpty(scene.getDescription())) {
			lbTitle.setToolTipText(getToolTitle());
		}
		upperPanel.add(lbTitle, MIG.get(MIG.GROW, MIG.SPANX + " 2"));
		//scene status
		lbStatus = new StatusLabel(scene.getScenestate(), true);
		upperPanel.add(lbStatus);
		//scene informational
		lbInformational = new JLabel(IconUtil.getIconSmall(ICONS.K.EMPTY));
		if (scene.getInformative()) {
			lbInformational.setIcon(IconUtil.getIconSmall(ICONS.K.INFO));
			lbInformational.setToolTipText(I18N.getMsg("informative"));
		}
		upperPanel.add(lbInformational);
		// button panel
		JPanel buttonPanel = new JPanel(new MigLayout(MIG.get(MIG.FLOWY, MIG.INS1)));
		buttonPanel.setName("buttonpanel");
		buttonPanel.setOpaque(false);
		// buttons
		btEdit = getEditButton();
		buttonPanel.add(btEdit);
		btDelete = getDeleteButton();
		buttonPanel.add(btDelete);
		btNew = getNewButton();
		buttonPanel.add(btNew);
		//upperPanel.add(buttonPanel, MIG.get(MIG.SPAN + "y 4", MIG.WRAP, MIG.TOP));
		// persons
		ImageIcon img = IconUtil.getImageIcon("striph");
		int w = img.getIconWidth() - (IconUtil.getDefSize() + (IconUtil.getDefSize() / 3));
		EntityLinksPanel ep = new EntityLinksPanel(mainFrame, scene, Book.TYPE.PERSON);
		ep.setMaximumSize(new Dimension(w, IconUtil.getDefSize()));
		upperPanel.add(ep, MIG.get(MIG.NEWLINE, MIG.SPANX, MIG.GROWX, MIG.WRAP));
		//locations
		upperPanel.add(new EntityLinksPanel(mainFrame, scene, Book.TYPE.LOCATION),
				MIG.get(MIG.SPANX, MIG.GROWX, MIG.WRAP));
		//items
		upperPanel.add(new EntityLinksPanel(mainFrame, scene, Book.TYPE.ITEM),
				MIG.get(MIG.SPANX, MIG.GROWX, MIG.WRAP));
		//plots
		upperPanel.add(new EntityLinksPanel(mainFrame, scene, Book.TYPE.PLOT),
				MIG.get(MIG.SPANX, MIG.GROWX, MIG.WRAP));
		// scene date-time
		lbTime = new JLabel(" ");
		if (scene.hasScenets() && !DateUtil.isZeroTimeDate(scene.getScenets())) {
			lbTime.setText(DateUtil.simpleDateTimeToString(scene.getScenets()));
		}
		Color fg = lbTime.getForeground();
		if (ColorUtil.isDark(scene.getStrand().getJColor()) && ColorUtil.isDark(fg)) {
			lbTime.setForeground(ColorUtil.lighter(fg, 0.75));
		} else if (ColorUtil.isLight(scene.getStrand().getJColor()) && ColorUtil.isLight(fg)) {
			lbTime.setForeground(ColorUtil.darker(fg, 0.75));
		} else {
			lbTime.setForeground(Color.BLACK);
		}
		upperPanel.add(lbTime, MIG.get(MIG.SPAN, MIG.WRAP, MIG.CENTER, MIG.GROWX));
		//bottom stamp

		// main panel
		panel.add(upperPanel);
		return panel;
	}

	protected StoryboardScene getThis() {
		return this;
	}

	@Override
	public void focusGained(FocusEvent e) {
		// empty
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource() instanceof JTextComponent) {
			mainFrame.getBookController().updateEntity(scene);
		}
	}

	private String getToolTitle() {
		StringBuilder b = new StringBuilder();
		b.append(Html.HTML_B).append(Html.P_B);
		b.append(scene.getName()).append(Html.BR);
		if (!Html.isEmpty(scene.getNotes())) {
			b.append(I18N.getColonMsg("notes")).append(scene.getNotes()).append(Html.BR);
		}
		if (!Html.isEmpty(scene.getDescription())) {
			b.append(I18N.getColonMsg("description")).append(scene.getDescription()).append(Html.BR);
		}
		b.append(Html.P_E).append(Html.HTML_E);
		return (b.toString());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
