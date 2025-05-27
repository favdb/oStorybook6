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
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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
import storybook.tools.Markdown;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.undo.UndoableTextArea;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractScenePanel;
import storybook.ui.panel.EntityLinksPanel;

/**
 * Class JPanel viewing a scene as a sticker
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class SceneSticker extends AbstractScenePanel implements FocusListener {

	private static final String TT = "SceneSticker.";

	private static final String CN_TITLE = "taTitle",
			CN_TEXT = "tcText",
			CN_UPPER_PANEL = "upperPanel";
	private JPanel upperPanel;
	private UndoableTextArea taTitle;
	private JTextComponent tcText;
	private StatusLabel lbStatus;
	private JLabel lbInformational, lbSceneNo, lbTime;
	private Integer size;

	public SceneSticker(MainFrame mainFrame, Scene scene) {
		super(mainFrame, scene, true, Color.white, Strand.getJColor(scene.getStrand()));
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case STRAND:
				if (PROPS.UPDATE.check(act.getCmd())) {
					EntityUtil.refresh(mainFrame, scene.getStrand());
					setEndBgColor(Strand.getJColor(scene.getStrand()));
					repaint();
					return;
				}
				break;
			case SCENE:
				if (PROPS.UPDATE.check(act.getCmd())) {
					Scene newScene = (Scene) newValue;
					if (!newScene.getId().equals(scene.getId())) {
						// not this scene
						return;
					}
					scene = newScene;
					lbSceneNo.setText(scene.getChapterSceneNo(false));
					lbSceneNo.setToolTipText(scene.getChapterSceneToolTip());
					lbStatus.setIcon(scene.getStatusIcon());
					taTitle.setText(scene.getTitle());
					taTitle.setCaretPosition(0);
					if (book.info.markdownGet()) {
						tcText.setText(Markdown.toHtml(scene.getSummary()));
						tcText.setEditable(false);
					} else {
						tcText.setText(scene.getSummary());
					}
					tcText.setCaretPosition(0);
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
		if (PROPS.CHRONO_ZOOM.check(propName)) {
			setZoomedSize((Integer) newValue);
			refresh();
		}
	}

	private void setZoomedSize(int zoomValue) {
		size = zoomValue * 8;
	}

	@Override
	public void init() {
		setZoomedSize(App.preferences.chronoGetZoom() * 9);
		book = mainFrame.getBook();
	}

	@Override
	public void initUi() {
		refresh();
	}

	@Override
	public void refresh() {
		//LOG.trace(TT + "refresh() for scene=" + scene.getName());
		removeAll();
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.FLOWY, MIG.INS0, MIG.GAP0), "[]", "[][grow]"));
		SwingUtil.setForcedSize(this, new Dimension(size, size));
		setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO));
		setFont(App.fonts.defGet());
		this.setBackground(scene.getStrand().getJColor());
		// set dotted border for scenes of other parts
		setBorder(SwingUtil.getBorderDefault());
		upperPanel = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0), "[][grow][]", "[top][top][top]"));
		upperPanel.setBackground(scene.getStrand().getJColor());
		upperPanel.setName(CN_UPPER_PANEL);
		// button panel
		JPanel buttonPanel = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		buttonPanel.setName("buttonpanel");
		buttonPanel.setOpaque(false);
		buttonPanel.add(getNewButton());
		buttonPanel.add(getDeleteButton());
		buttonPanel.add(getEditButton());
		upperPanel.add(buttonPanel, MIG.get(MIG.GROWX));
		// chapter and scene number
		lbSceneNo = new JLabel("", SwingConstants.CENTER);
		lbSceneNo.setText(scene.getChapterSceneNo(false));
		lbSceneNo.setToolTipText(scene.getChapterSceneToolTip());
		lbSceneNo.setOpaque(true);
		if (!LaF.isDark()) {
			lbSceneNo.setBackground(Color.white);
		}
		upperPanel.add(lbSceneNo, MIG.get(MIG.GROW));
		// status
		lbStatus = new StatusLabel(scene.getStatus(), true);
		upperPanel.add(lbStatus, MIG.GROWY);
		// informational
		lbInformational = new JLabel("");
		if (scene.getInformative()) {
			lbInformational.setIcon(IconUtil.getIconSmall(ICONS.K.INFO));
			lbInformational.setToolTipText(I18N.getMsg("informative"));
		} else {
			lbInformational.setIcon(IconUtil.getIconSmall(ICONS.K.EMPTY));
		}
		upperPanel.add(lbInformational, MIG.GROWY);
		// person links
		EntityLinksPanel personLinksPanel = new EntityLinksPanel(mainFrame, scene, Book.TYPE.PERSON, false);
		JScrollPane scroller = new JScrollPane(personLinksPanel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setMinimumSize(new Dimension(FontUtil.getHeight(), FontUtil.getHeight()));
		scroller.setOpaque(false);
		scroller.getViewport().setOpaque(false);
		scroller.setBorder(null);
		String sgn = MIG.get(MIG.SPANX2, MIG.GROWX, MIG.NEWLINE);
		upperPanel.add(scroller, sgn);
		// location links
		upperPanel.add(new EntityLinksPanel(mainFrame, scene, Book.TYPE.LOCATION, false), sgn);
		// items links
		upperPanel.add(new EntityLinksPanel(mainFrame, scene, Book.TYPE.ITEM, false), sgn);
		// plot links
		upperPanel.add(new EntityLinksPanel(mainFrame, scene, Book.TYPE.PLOT, false), sgn);
		// scene time
		lbTime = new JLabel();
		if (scene.hasScenets() && !DateUtil.isZeroTimeDate(scene.getScenets())) {
			String tx = DateUtil.simpleDateTimeToString(scene.getScenets(), true);
			lbTime.setText(tx);
		}
		add(upperPanel, MIG.GROWX);
		// title
		taTitle = new UndoableTextArea();
		taTitle.setName(CN_TITLE);
		taTitle.setText(scene.getTitle());
		taTitle.setCaretPosition(0);
		taTitle.setLineWrap(true);
		taTitle.setWrapStyleWord(true);
		taTitle.setDragEnabled(true);
		taTitle.setCaretPosition(0);
		taTitle.getUndoManager().discardAllEdits();
		taTitle.addFocusListener(this);
		JScrollPane spTitle = new JScrollPane(taTitle);
		spTitle.setPreferredSize(new Dimension(50, 35));
		// text
		tcText = initHtmlField(CN_TEXT, scene);
		JScrollPane spText = new JScrollPane(tcText);
		spText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		spText.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		add(spText, MIG.GROW);
		revalidate();
		repaint();
	}

	protected SceneSticker getThis() {
		return this;
	}

	@Override
	public Scene getScene() {
		return this.scene;
	}

	@Override
	public void focusGained(FocusEvent e) {
		// empty
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource() instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) e.getSource();
			switch (tc.getName()) {
				case CN_TITLE:
					scene.setTitle(tc.getText());
					break;
				case CN_TEXT:
					if (book.info.markdownGet()) {
					} else {
						scene.setSummary(tc.getText());
					}
					break;
				default:
					break;
			}
			mainFrame.getBookController().updateEntity(scene);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
