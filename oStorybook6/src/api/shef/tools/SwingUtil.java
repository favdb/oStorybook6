/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.tools;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import api.shef.actions.manager.ActionBasic;
import api.shef.actions.manager.ActionList;
import api.shef.actions.manager.Separator;

/**
 *
 * @author favdb
 */
public class SwingUtil {

	public static JMenu createMenu(ActionList actions) {
		JMenu menu = new JMenu();
		int c = 0;
		for (Object ac : actions) {
			//first action is the JMenu, others are JMenuItem
			if (c == 0) {
				ActionBasic act = (ActionBasic) ac;
				if (act == null) {
					continue;
				}
				menu.setText(act.getActionName());
				if (act.getMnemonic() > 0) {
					menu.setMnemonic(act.getMnemonic());
				}
				if (act.getMenuShowsIcon() && act.getSmallIcon() != null) {
					menu.setIcon(act.getSmallIcon());
				}
				c++;
				continue;
			}
			//other actions
			if (ac instanceof Separator) {
				menu.addSeparator();
			} else if (ac instanceof ActionList) {
				menu.add(createMenu((ActionList) ac));
			} else if (ac instanceof Action) {
				menu.add(createMenuItem((Action) ac));
			}
		}
		return (menu);
	}

	public static JMenuItem createMenuItem(Action action) {
		JMenuItem item = new JMenuItem();
		ActionBasic act = (ActionBasic) action;
		item.setText(act.getActionName());
		if (act.getMnemonic() > 0) {
			item.setMnemonic(act.getMnemonic());
		}
		if (act.getMenuShowsIcon()) {
			item.setIcon(act.getSmallIcon());
		}
		return (item);
	}

	public static JPopupMenu createPopupMenu(ActionList actions) {
		JPopupMenu popup = new JPopupMenu();
		for (Object element : actions) {
			if (element == null || element instanceof Separator) {
				popup.addSeparator();
			} else if (element instanceof ActionList) {
				popup.add(createMenu((ActionList) element));
			} else if (element instanceof Action) {
				popup.add(createMenuItem((Action) element));
			}
		}
		return (popup);
	}

	public AbstractButton createButton(ActionBasic action) {
		ActionBasic.BUTTON_TYPE buttonType = action.getButtonType();
		AbstractButton button;
		switch (buttonType) {
			case TOGGLE:
				button = new JToggleButton(action);
				break;
			case RADIO:
				button = new JRadioButtonMenuItem(action);
				break;
			case CHECKBOX:
				button = new JCheckBoxMenuItem(action);
				break;
			case NORMAL:
			default:
				button = new JButton(action);
				break;
		}
		//never show any text
		button.setText("");
		button.setIcon(action.getSmallIcon());
		button.setToolTipText(action.getShortDescription());
		return button;
	}

	public static void setColumns(JSpinner spin, int columns) {
		JComponent editor = spin.getEditor();
		JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
		tf.setColumns(columns);
	}
}
