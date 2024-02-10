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
package storybook.ideabox;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.db.DB;
import storybook.db.idea.Idea;
import storybook.tools.ListUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class IdeaDialog extends JDialog {

	private static final String TT = "IdeaDialog";

	private final IdeaxFrm ideaxFrm;
	private Idea idea;
	private JLabel lbId;
	private JTextField tfName;
	public JComboBox cbCategory, cbStatus;
	public ShefEditor shef;
	private Border tfBorder, cbBorder, shBorder;
	private boolean canceled = false;

	public IdeaDialog(IdeaxFrm frm) {
		super(frm);
		this.ideaxFrm = frm;
		initialize();
	}

	private void initialize() {
		//LOG.trace(TT+".initialize()");
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.WRAP1)));
		setTitle(I18N.getMsg("idea"));
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3, MIG.WRAP), "[][]"));
		lbId = new JLabel("id");
		lbId.setEnabled(false);
		lbId.setVisible(App.isDev());
		p.add(lbId, MIG.get(MIG.SPAN, MIG.RIGHT));
		tfName = Ui.initStringField(p, DB.DATA.NAME, 0, "", BMANDATORY);
		cbCategory = Ui.initCategory(p, findCategories(), null, "010");
		cbCategory.setMinimumSize(IconUtil.getDefDim());
		cbStatus = Ui.initStatus(p, 0, BMANDATORY);
		shef = new ShefEditor("", "lang_all, allow, colored", "");
		shef.setName("notes");
		shef.setPreferredSize(new Dimension(1024, 780));
		shef.setMaximumSize(SwingUtil.getScreenSize());
		if (App.preferences.getBoolean(Pref.KEY.IDEAX_SHEF_SHOW_HIDE)) {
			shef.showHideTB();
		}
		p.add(new JLabel(I18N.getColonMsg("notes")), MIG.get(MIG.RIGHT, MIG.TOP));
		p.add(shef, MIG.GROW);
		add(p, MIG.GROW);
		add(Ui.initButton("btCancel", "cancel", ICONS.K.CANCEL, "", e -> close(true)),
			MIG.get(MIG.SPLIT2, MIG.RIGHT));
		add(Ui.initButton("btOk", "ok", ICONS.K.OK, "", e -> close(false)),
			MIG.RIGHT);
		setPreferredSize(ideaxFrm.param.dlgSize);
		pack();
		setLocation(ideaxFrm.param.dlgLoc);
		setModal(true);
		tfBorder = tfName.getBorder();
		cbBorder = cbCategory.getBorder();
		shBorder = BorderFactory.createEtchedBorder();
	}

	@SuppressWarnings("unchecked")
	private void initCbCategory() {
		cbCategory.removeAllItems();
		cbCategory.addItem("");
		List<String> ls = new ArrayList<>();
		for (Idea ide : ideaxFrm.ideaX.ideas) {
			ls.add(ide.getCategory());
		}
		ls = ListUtil.setUnique(ls);
		cbCategory.setModel(new DefaultComboBoxModel(ls.toArray()));
	}

	@SuppressWarnings("unchecked")
	public void setIdea(Idea ide) {
		//LOG.trace(TT + ".setIdea(idea=" + LOG.trace(ide) + ")");
		this.idea = ide;
		lbId.setText("");
		tfName.setText("");
		tfName.setBorder(tfBorder);
		initCbCategory();
		cbCategory.setSelectedIndex(-1);
		cbCategory.setBorder(cbBorder);
		cbStatus.setSelectedIndex(0);
		cbStatus.setBorder(cbBorder);
		shef.setText("");
		shef.setBorder(shBorder);
		if (ide != null) {
			lbId.setText(ide.getUuid());
			tfName.setText(ide.getName());
			cbStatus.setSelectedIndex(ide.getStatus());
			cbCategory.setSelectedItem(ide.getCategory());
			shef.setText(Html.intoHTML(ide.getNotes()));
		}
	}

	private List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Idea id : ideaxFrm.ideaX.ideas) {
			if (!ls.contains(id.getCategory())) {
				ls.add(id.getCategory());
			}
		}
		return ls;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public Idea getIdea() {
		Idea ide = new Idea();
		ide.setId(idea.getId());
		ide.setCreation(idea.getCreation());
		ide.setMaj();
		ide.setName(tfName.getText());
		ide.setStatus(cbStatus.getSelectedIndex());
		ide.setCategory((String) cbCategory.getSelectedItem());
		ide.setNotes(shef.getText());
		return ide;
	}

	/**
	 * close the dialog
	 */
	private void close(boolean b) {
		//LOG.trace("IdeaxDlgWindowAdaptor.windowClosing(evt=" + evt.toString() + ")");
		ideaxFrm.param.saveDlg(this);
		canceled = b;
		App.preferences.setBoolean(Pref.KEY.IDEAX_SHEF_SHOW_HIDE, shef.wysEditorGet().getShowHideTB());
		dispose();
	}

}
