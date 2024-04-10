/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors;

import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.ShefEditor;
import api.shef.actions.manager.ActionBasic;
import api.shef.actions.manager.ActionList;
import api.shef.editors.wys.HTMLBlockAction;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class AbstractPanel extends JPanel {

	public final JComponent parent;
	public JToolBar toolbar;
	public ActionList actionList;
	public String changeTo = "Wysiwyg";

	public AbstractPanel() {
		this.parent = null;
	}

	public AbstractPanel(ShefEditor parent) {
		this.parent = parent;
	}

	public void initAll() {
		init();
		initUi();
	}

	public void init() {
		actionList = new ActionList("editor-actions");
		setLayout(new BorderLayout());
		setOpaque(true);
	}

	public void initUi() {
		add(initToolbar(), BorderLayout.NORTH);
	}

	public JToolBar initToolbar() {
		toolbar = new JToolBar();
		toolbar.setLayout(
			new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.HIDEMODE3)
			));
		return toolbar;
	}

	/**
	 * create a JButton using an ActionBase
	 *
	 * @param actionList
	 * @param act
	 * @return a JButton
	 */
	public JButton initButton(ActionList actionList, ActionBasic act) {
		JButton bt = new JButton(act);
		if (act instanceof HTMLBlockAction) {
			bt.setName(((HTMLBlockAction) act).getActionCommandName());
		} else {
			bt.setName(act.getActionName());
		}
		bt.setText("");
		if (!act.getShortDescription().isEmpty()) {
			bt.setToolTipText(act.getShortDescription());
		}
		bt.setIcon(act.getSmallIcon());
		if (act.getAccelerator() != null) {
			bt.registerKeyboardAction(act,
				act.getAccelerator(),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		}
		if (actionList != null) {
			actionList.add(act);
		}
		return (bt);
	}

	public JButton initButton(ActionList actionList, Action act) {
		JButton bt = new JButton(act);
		bt.setText("");
		if (actionList != null) {
			actionList.add(act);
		}
		return bt;
	}

	public JButton initButton(String name,
		String title,
		String icon,
		String tooltips,
		ActionListener action) {
		JButton bt = initButton(name, title, icon, tooltips);
		if (action != null) {
			bt.addActionListener(action);
		}
		return bt;
	}

	public JButton initButton(String name,
		String title,
		ICONS.K icon,
		String tooltips,
		ActionListener action) {
		return initButton(name, title, icon.toString(), tooltips);
	}

	public JButton initButton(String name,
		String title,
		String icon,
		String tooltips) {
		JButton bt = new JButton();
		bt.setName(name);
		if (!title.isEmpty()) {
			bt.setText(I18N.getMsg("shef." + title));
		}
		if (!tooltips.isEmpty()) {
			bt.setToolTipText(I18N.getMsg("shef." + tooltips));
		}
		if (!icon.isEmpty()) {
			bt.setIcon(IconUtil.getIconSmall(icon));
		}
		return (bt);
	}

	public JMenuItem initMenuItem(ActionBasic act) {
		//JMenuItem bt = new JMenuItem();
		JMenuItem bt = new JMenuItem(act);
		bt.setName(act.getActionName());
		if (!act.getShortDescription().isEmpty()) {
			bt.setText(act.getShortDescription());
		}
		/*if (!act.getLongDescription().isEmpty()) {
			bt.setToolTipText(act.getLongDescription());
		}
		if (act.getAccelerator() != null) {
			bt.setAccelerator(act.getAccelerator());
		}*/
		if (act.getSmallIcon() != null) {
			bt.setIcon(act.getSmallIcon());
		}
		bt.addActionListener(act);
		return (bt);
	}

	public void setText(String text) {
		//empty
	}

	public void setCaretPosition(int position) {
		// empty
	}

}
