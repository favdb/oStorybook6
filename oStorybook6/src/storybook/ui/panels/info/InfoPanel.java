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
package storybook.ui.panels.info;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.exim.exporter.ExportInfoView;
import storybook.project.Project;
import storybook.tools.file.XEditorFile;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panels.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class InfoPanel extends AbstractPanel implements HyperlinkListener {

	private static final String TT = "InfoPanel.";

	private AbstractEntity entity;
	private Object last;
	private JTextPane infoPane;
	private JButton btExternal;
	private JComboBox cbDetailed;

	public InfoPanel(MainFrame mainFrame) {
		super(mainFrame);
		this.setName("QuickInfo");
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString());
		String propName = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		if (Ctrl.isUpdate(evt)) {
			refreshInfo();
		} else if (Ctrl.PROPS.EXPORT.check(propName)) {
			ExportInfoView export = new ExportInfoView(mainFrame);
			export.exec(this);
		} else if (Ctrl.PROPS.INIT.check(propName)) {
			return;
		} else if (Ctrl.PROPS.REFRESH.check(propName)) {
			View newView = (View) newValue;
			View view = (View) getParent().getParent();
			if (view == newView) {
				refreshInfo();
			}
		} else if (Ctrl.PROPS.SHOWINFO.check(propName)) {
			if (newValue == null) {
				infoPane.setText("");
				last = null;
			} else if (newValue instanceof AbstractEntity) {
				AbstractEntity en = (AbstractEntity) newValue;
				if (en.equals(last)) {
					return;
				}
				entity = (AbstractEntity) newValue;
				if (entity.isTransient()) {
					return;
				}
				last = entity;
				refreshInfo();
			} else if (newValue instanceof Project) {
				last = mainFrame.getProject();
				refreshInfo();
			}
		} else if (newValue == null && Ctrl.isDelete(evt)) {
			entity = null;
			infoPane.setText("");
		} else if (entity != null && newValue instanceof AbstractEntity) {
			AbstractEntity updatedEntity = (AbstractEntity) newValue;
			if (updatedEntity.getId().equals(entity.getId())) {
				refreshInfo();
			}
		}
	}

	@Override
	public void init() {
		this.withPart = false;
		last = null;
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.WRAP, MIG.FILL, MIG.INS0)));
		add(initToolbar(), MIG.GROWX);
		infoPane = new JTextPane();
		infoPane.setEditable(false);
		infoPane.setOpaque(true);
		infoPane.setContentType(Html.TYPE);
		infoPane.addHyperlinkListener(this);
		JScrollPane scroller = new JScrollPane(infoPane);
		SwingUtil.setMaxPreferredSize(scroller);
		add(scroller);
		cbDetailed.setSelectedIndex(App.preferences.getInteger(Pref.KEY.INFO_DETAIL));
		if (entity != null) {
			refreshInfo();
		}
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		String options[] = {
			"view.info_option_simple",
			"view.info_option_detailed",
			"view.info_option_more",
			"view.info_option_full"};
		toolbar.add(new JLabel(I18N.getColonMsg("view.info_option")));
		cbDetailed = Ui.initComboBox("cbDetailed", "", options, 0, !EMPTY, !ALL, e -> changeDetail());
		toolbar.add(cbDetailed);
		btExternal = new JButton(book.getParam().getParamEditor().getName());
		btExternal.setName("btExternal");
		btExternal.addActionListener(evt -> {
			String name = XEditorFile.launchExternal(mainFrame, (Scene) entity);
			((Scene) entity).setOdf(name);
			mainFrame.getBookModel().ENTITY_Update((Scene) entity);
		});
		btExternal.setVisible(false);
		toolbar.add(btExternal, MIG.CENTER);
		return toolbar;
	}

	private void refreshInfo() {
		//LOG.trace(TT+"refreshInfo()");
		btExternal.setVisible(false);
		if (last == null) {
			infoPane.setText("");
			return;
		}
		if (last instanceof Project) {
			infoPane.setText(mainFrame.project.getInfo(cbDetailed.getSelectedIndex() != 0));
			infoPane.setCaretPosition(0);
			return;
		} else if (entity != null) {
			infoPane.setText(entity.toDetail(cbDetailed.getSelectedIndex()));
		}
		infoPane.setCaretPosition(0);
		infoPane.setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, entity, EntityUtil.WITH_CHRONO));
		if (entity instanceof Chapter) {
			mainFrame.lastChapterSet((Chapter) entity);
		}
		if (entity instanceof Scene) {
			Scene scene = (Scene) entity;
			mainFrame.lastSceneSet(scene);
			if (mainFrame.getBook().isXeditorUse()) {
				btExternal.setVisible(true);
				btExternal.setEnabled(!scene.getOdf().isEmpty() && new File(scene.getOdf()).exists());
			}
		}
	}

	public AbstractEntity getEntity() {
		return entity;
	}

	public void setEntity(AbstractEntity en) {
		this.entity = en;
		refreshInfo();
	}

	public JTextPane getInfoPane() {
		return infoPane;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			Net.openUrl(evt);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	private void changeDetail() {
		App.preferences.setInteger(Pref.KEY.INFO_DETAIL, cbDetailed.getSelectedIndex());
		refreshInfo();
	}

}
