/*
 * Copyright (C) 2019 favdb
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
package storybook.edit;

import api.mig.swing.MigLayout;
import assistant.AssistantDlg;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.AttributeEdit;
import storybook.db.book.Book;
import storybook.db.category.CategoryEdit;
import storybook.db.chapter.ChapterEdit;
import storybook.db.endnote.Endnote;
import storybook.db.endnote.EndnoteEdit;
import storybook.db.event.EventEdit;
import storybook.db.gender.GenderEdit;
import storybook.db.idea.Idea;
import storybook.db.idea.IdeaEdit;
import storybook.db.item.ItemEdit;
import storybook.db.location.LocationEdit;
import storybook.db.memo.MemoEdit;
import storybook.db.part.PartEdit;
import storybook.db.person.PersonEdit;
import storybook.db.plot.PlotEdit;
import storybook.db.relation.RelationEdit;
import storybook.db.scene.SceneEdit;
import storybook.db.strand.StrandEdit;
import storybook.db.tag.TagEdit;
import storybook.tools.LOG;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class Editor extends AbstractPanel {

	private static final String TT = "Editor.";

	private static final String BT_ASSISTANT = "btAssistant",
			BT_EXTERNAL = "btExternal",
			BT_IDEA = "btIdea",
			BT_NEXT = "btNext",
			BT_PRIOR = "btPrior",
			BT_CENTER = "btCenter",
			BT_UPDATE = "btUpdate",
			BT_CANCEL = "btCancel",
			BT_OK = "btOk";

	public AbstractEntity entity;
	public AbstractEditor panel;
	private final JDialog fromDialog;
	public boolean canceled = true;
	private JLabel lbTitle;
	private JButton btEntityPrior, btEntityNext, btExternal;
	private final Book.TYPE objType;
	private JLabel lbIcon;

	public Editor(MainFrame frame, AbstractEntity entity, JDialog dlg) {
		super(frame);
		this.entity = entity;
		this.fromDialog = dlg;
		this.objType = entity.getObjType();
		initAll();
	}

	@Override
	public void init() {
		//LOG.trace("Editor.init() entitytype="+objType);
		withPart = false;
	}

	@Override
	public void initUi() {
		//LOG.trace("Editor.initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.HIDEMODE3)));
		setMinimumSize(MINIMUM_SIZE);
		add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX, MIG.WRAP));
		initPanel();
		if (panel == null) {
			LOG.err("Editor.initUi panel is null, no entity type found");
			return;
		} else {
			add(panel, MIG.get(MIG.SPAN, MIG.GROW));
			SwingUtilities.invokeLater(() -> {
				panel.tfName.selectAll();
				panel.tfName.requestFocus();
			});
		}
		initFooter();
		if (entity instanceof Endnote) {
			toolbar.setVisible(false);
		}
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		lbIcon = new JLabel(entity.getIcon());
		toolbar.add(lbIcon);
		lbTitle = new JLabel(entity.toString());
		lbTitle.setFont(FontUtil.getBold(lbTitle.getFont()));
		lbTitle.setForeground(ColorUtil.isDark(toolbar.getBackground())
				? ColorUtil.lighter(Color.GRAY, 0.5)
				: ColorUtil.darker(Color.GRAY, 0.5));
		toolbar.add(lbTitle);

		btEntityPrior = Ui.initButton(BT_PRIOR, "",
				ICONS.K.NAV_PREV, "", e -> setEntityPrior());
		toolbar.add(btEntityPrior);
		btEntityNext = Ui.initButton(BT_NEXT, "",
				ICONS.K.NAV_NEXT, "", e -> setEntityNext());
		toolbar.add(btEntityNext);
		setEntityNav();
		toolbar.add(new JLabel(" "), MIG.PUSHX);

		JButton center = Ui.initButton(BT_CENTER, "",
				ICONS.K.SCREEN_CENTER, "center", e -> {
					if (fromDialog != null) {
						fromDialog.setSize(PREF_SIZE);
						fromDialog.setLocationRelativeTo(mainFrame);
					}
				});
		center.setMnemonic(KeyEvent.VK_C);
		toolbar.add(center, MIG.RIGHT);

		if (!(entity instanceof Idea)) {
			toolbar.add(
					Ui.initButton(BT_IDEA, "",
							ICONS.K.ENT_IDEA, "foi.new",
							e -> mainFrame.showEditorAsDialog(new Idea())),
					MIG.RIGHT);
		}
		return toolbar;
	}

	public void setEntity(AbstractEntity e) {
		panel.apply();
		switch (panel.isModified()) {
			case JOptionPane.CANCEL_OPTION:
				return;
			case JOptionPane.YES_OPTION:
				mainFrame.getBookController().updateEntity(entity);
				mainFrame.setUpdated();
				break;
			default:
				break;
		}
		entity = e;
		panel.setEntity(e);
		setEntityNav();
	}

	private void setEntityNav() {
		AbstractEntity e = EntityUtil.getPrior(mainFrame, entity);
		if (e != null && (e.getId() == null || e.getId() == -1L)) {
			btEntityPrior.setVisible(false);
			btEntityNext.setVisible(false);
		}
		if (e == null) {
			btEntityPrior.setEnabled(false);
			btEntityPrior.setToolTipText("");
		} else {
			btEntityPrior.setEnabled(true);
			btEntityPrior.setToolTipText(e.toString());
		}
		e = EntityUtil.getNext(mainFrame, entity);
		if (e == null) {
			btEntityNext.setEnabled(false);
			btEntityNext.setToolTipText("");
		} else {
			btEntityNext.setEnabled(true);
			btEntityNext.setToolTipText(e.toString());
		}
	}

	private void setEntityNext() {
		AbstractEntity e = EntityUtil.getNext(mainFrame, entity);
		if (e != null) {
			setEntity(e);
			ctrl.setTableRow(e);
		}
	}

	private void setEntityPrior() {
		AbstractEntity e = EntityUtil.getPrior(mainFrame, entity);
		if (e != null) {
			setEntity(e);
			ctrl.setTableRow(e);
		}
	}

	public void initPanel() {
		//LOG.trace("Editor.initPanel()");
		switch (Book.getTYPE(entity)) {
			case ATTRIBUTE:
			case ATTRIBUTES:
				panel = new AttributeEdit(this, entity);
				break;
			case CATEGORY:
			case CATEGORIES:
				panel = new CategoryEdit(this, entity);
				break;
			case CHAPTER:
			case CHAPTERS:
				panel = new ChapterEdit(this, entity);
				break;
			case ENDNOTE:
			case ENDNOTES:
				panel = new EndnoteEdit(this, entity);
				break;
			case EVENT:
			case EVENTS:
				panel = new EventEdit(this, entity);
				break;
			case GENDER:
			case GENDERS:
				panel = new GenderEdit(this, entity);
				break;
			case IDEA:
			case IDEAS:
				panel = new IdeaEdit(this, entity);
				break;
			case ITEM:
			case ITEMS:
				panel = new ItemEdit(this, entity);
				break;
			case LOCATION:
			case LOCATIONS:
				panel = new LocationEdit(this, entity);
				break;
			case MEMO:
			case MEMOS:
				panel = new MemoEdit(this, entity);
				break;
			case PART:
			case PARTS:
				panel = new PartEdit(this, entity);
				break;
			case PERSON:
			case PERSONS:
				panel = new PersonEdit(this, entity);
				break;
			case PLOT:
			case PLOTS:
				panel = new PlotEdit(this, entity);
				break;
			case RELATION:
			case RELATIONS:
				panel = new RelationEdit(this, entity);
				break;
			case STRAND:
			case STRANDS:
				panel = new StrandEdit(this, entity);
				break;
			case SCENE:
			case SCENES:
				panel = new SceneEdit(this, entity);
				break;
			case TAG:
			case TAGS:
				panel = new TagEdit(this, entity);
				break;
			default:
				break;
		}
	}

	public void initFooter() {
		//LOG.trace("Editor.initFooter()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3)));
		if (panel.getAssistant() != null) {
			JButton btAssistant = Ui.initButton(BT_ASSISTANT, "assistant",
					ICONS.K.TARGET, "", e -> doAssistant());
			if (objType == Book.TYPE.CHAPTER || objType == Book.TYPE.SCENE) {
				btAssistant.setVisible(App.getAssistant().isExists(book, objType.toString()));
			} else {
				btAssistant.setVisible(App.getAssistant().isExists(objType.toString()));
			}
			p.add(btAssistant);
		}
		if (entity instanceof Idea) {
			JButton btIdea = Ui.initButton(BT_IDEA, "idea.to_scene",
					ICONS.K.ENT_SCENE, "", (ActionEvent evt) -> {
						IdeaEdit pidea = (IdeaEdit) panel;
						pidea.toScene();
					});
			add(btIdea, MIG.get(MIG.SPAN, "split 4", MIG.RIGHT));
		}
		if (!(entity instanceof Endnote)) {
			JButton btUpdate = Ui.initButton(BT_UPDATE, "editor.update",
					ICONS.K.F_SAVE, "", evt -> doUpdate());
			p.add(btUpdate);
		}
		p.add(Ui.initButton(BT_CANCEL, "cancel", ICONS.K.CANCEL, "", e -> doCancel()));
		p.add(Ui.initButton(BT_OK, "ok", ICONS.K.OK, "", e -> doOk()));
		add(p, MIG.RIGHT);
	}

	public void setBtExternal(String str) {
		if (btExternal != null) {
			btExternal.setEnabled(!str.isEmpty());
		}
	}

	public Editor getThis() {
		return (this);
	}

	public void showError() {
		JOptionPane.showMessageDialog(this, panel.msgError,
				I18N.getMsg(objType.toString()) + " " + I18N.getMsg("error"),
				JOptionPane.ERROR_MESSAGE);
	}

	private void doAssistant() {
		String origin = entity.getAssistant();
		AssistantDlg dlg = new AssistantDlg(fromDialog.getContentPane(), entity);
		dlg.setVisible(true);
		if (dlg.isCanceled()) {
			return;
		}
		String str = dlg.getResult();
		if (origin.equals(str)) {
			return;
		}
		//LOG.trace(TT + "doAssistant result '" + str + "'");
		entity.setAssistant(str);
		panel.setAssistant(str);
		panel.selectAssistantTab();
		panel.modified = true;
		revalidate();
	}

	private void doUpdate() {
		if (!panel.verifier()) {
			showError();
			return;
		}
		panel.apply();
		panel.save();
		canceled = false;
		if (!mainFrame.isTypist && !mainFrame.isScenario) {
			mainFrame.setUpdated();
		}
	}

	private void doOk() {
		//LOG.trace(TT + "doOk()");
		if (!panel.verifier()) {
			showError();
			return;
		}
		panel.apply();
		panel.save();
		canceled = false;
		if (entity.getId() == -1L) {
			mainFrame.getBookModel().ENTITY_New(entity);
		} else {
			mainFrame.getBookModel().ENTITY_Update(entity);
		}
		mainFrame.setUpdated();
		fromDialog.dispose();
	}

	private void doCancel() {
		canceled = true;
		fromDialog.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	public void setIcon(String str) {
		lbIcon.setIcon(IconUtil.getIconSmall(str));
	}

	public void setTitle(String str) {
		lbTitle.setText(str);
	}

	public void hideTitle() {
		toolbar.setVisible(false);
	}

}
