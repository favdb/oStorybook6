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
package storybook.tools.swing.js;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class JSPanelViewsRadioButton extends AbstractPanel {

	private JRadioButton rbChrono;
	private JRadioButton rbBook;
	private JRadioButton rbManage;
	private boolean showManage;

	public JSPanelViewsRadioButton(MainFrame mainFrame) {
		this(mainFrame, true);
	}

	public JSPanelViewsRadioButton(MainFrame mainFrame, boolean showManage) {
		super(mainFrame);
		this.showManage = showManage;
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.WRAP), "[32px][][]"));
		ButtonGroup btGroup = new ButtonGroup();
		add(new JLabel(" "));
		add(new JLabel(IconUtil.getIconSmall(ICONS.K.VW_CHRONO)));
		rbChrono = new JRadioButton(I18N.getMsg("view.chrono"));
		rbChrono.setSelected(true);
		add(rbChrono);
		btGroup.add(rbChrono);

		add(new JLabel(" "));
		add(new JLabel(IconUtil.getIconSmall(ICONS.K.VW_BOOK)));
		rbBook = new JRadioButton(I18N.getMsg("view.book"));
		add(rbBook);
		btGroup.add(rbBook);

		if (showManage) {
			add(new JLabel(" "));
			add(new JLabel(IconUtil.getIconSmall(ICONS.K.VW_MANAGE)));
			rbManage = new JRadioButton(I18N.getMsg("view.manage"));
			add(rbManage);
			btGroup.add(rbManage);
		}
	}

	public boolean isChronoSelected() {
		return rbChrono.isSelected();
	}

	public boolean isBookSelected() {
		return rbBook.isSelected();
	}

	public boolean isManageSelected() {
		return rbManage.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
