/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * A collection of static UI helper methods.
 *
 * @author Bob Tantlinger
 *
 */
public class UIUtils {

	private UIUtils() {
		//empty
	}

	/**
	 * Shows an error message dialog
	 *
	 * @param msg
	 */
	public static void showError(String msg) {
		showError(null, msg);
	}

	public static void showError(Component c, Throwable ex) {
		Window w = SwingUtilities.getWindowAncestor(c);
		if (w instanceof Frame) {
			showError((Frame) w, ex);
		} else if (w instanceof Dialog) {
			showError((Dialog) w, ex);
		} else {
			showError(c, ex.getLocalizedMessage());
		}
	}

	/**
	 * Shows an error message dialog
	 *
	 * @param owner
	 * @param msg
	 */
	public static void showError(Component owner, String msg) {
		showError(owner, "Error", msg);
	}

	/**
	 * Shows an error message dialog
	 *
	 * @param owner
	 * @param title
	 * @param msg
	 */
	public static void showError(Component owner, String title, String msg) {
		JOptionPane.showMessageDialog(
				owner, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Shows an exception dialog
	 *
	 * @param owner
	 * @param title
	 * @param th
	 */
	public static void showError(Frame owner, String title, Throwable th) {
		JDialog d = new ExceptionDialog(owner, th);
		if (title != null) {
			d.setTitle(title);
		}
		d.setLocationRelativeTo(owner);
		d.setVisible(true);
		th.printStackTrace(System.err);
	}

	/**
	 * Shows an exception dialog
	 *
	 * @param owner
	 * @param th
	 */
	public static void showError(Frame owner, Throwable th) {
		showError(owner, null, th);
	}

	/**
	 * Shows an exception dialog
	 *
	 * @param owner
	 * @param title
	 * @param th
	 */
	public static void showError(Dialog owner, String title, Throwable th) {
		JDialog d = new ExceptionDialog(owner, th);
		if (title != null) {
			d.setTitle(title);
		}
		d.setLocationRelativeTo(owner);
		d.setVisible(true);
		th.printStackTrace(System.err);
	}

	/**
	 * Shows an exception dialog
	 *
	 * @param owner
	 * @param th
	 */
	public static void showError(Dialog owner, Throwable th) {
		showError(owner, null, th);
	}

	/**
	 * Shows a warning dialog
	 *
	 * @param owner
	 * @param title
	 * @param msg
	 */
	public static void showWarning(Component owner, String title, String msg) {
		JOptionPane.showMessageDialog(
				owner, msg, title, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Shows a warning dialog
	 *
	 * @param owner
	 * @param msg
	 */
	public static void showWarning(Component owner, String msg) {
		showWarning(owner, "Warning", msg);
	}

	/**
	 * Shows a warning dialog
	 *
	 * @param msg
	 */
	public static void showWarning(String msg) {
		showWarning(null, msg);
	}

	/**
	 * Shows an info dialog
	 *
	 * @param owner
	 * @param title
	 * @param msg
	 */
	public static void showInfo(Component owner, String title, String msg) {
		JOptionPane.showMessageDialog(owner, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shows an info dialog
	 *
	 * @param owner
	 * @param msg
	 */
	public static void showInfo(Component owner, String msg) {
		showInfo(owner, "Information", msg);
	}

	/**
	 * Shows an info dialog
	 *
	 * @param msg
	 */
	public static void showInfo(String msg) {
		showInfo(null, msg);
	}

	public static AbstractButton addToolBarButton(JToolBar tb, Action a) {
		JButton bt = new JButton(a);
		bt.setName((String) a.getValue(Action.NAME));
		return addToolBarButton(tb, new JButton(a));
	}

	public static AbstractButton addToolBarButton(JToolBar tb, Action a, boolean focusable, boolean showIconOnly) {
		return addToolBarButton(tb, new JButton(a), false, true);
	}

	public static AbstractButton addToolBarButton(JToolBar tb, AbstractButton button) {
		return addToolBarButton(tb, button, false, true);
	}

	public static AbstractButton addToolBarButton(JToolBar tb, AbstractButton button, boolean focusable, boolean showIconOnly) {
		if (button.getAction() != null) {
			button.setToolTipText((String) button.getAction().getValue(Action.NAME));
			//prefer large icons for toolbar buttons
			if (button.getAction().getValue("LARGE_ICON") != null) {
				try {
					button.setIcon((Icon) button.getAction().getValue("LARGE_ICON"));
				} catch (ClassCastException cce) {
				}
			}
		}

		Icon ico = button.getIcon();
		if (ico != null && showIconOnly) {
			button.setText(null);
			button.setMnemonic(0);
			button.putClientProperty("hideActionText", Boolean.TRUE);
			int square = Math.max(ico.getIconWidth(), ico.getIconHeight()) + 6;
			Dimension size = new Dimension(square, square);
			button.setPreferredSize(size);
			//button.setMinimumSize(size);
			//button.setMaximumSize(size);
		}

		if (!focusable) {
			button.setFocusable(false);
			button.setFocusPainted(false);
		}

		button.setMargin(new Insets(1, 1, 1, 1));
		tb.add(button);
		return button;
	}

	public static JMenuItem addMenuItem(JMenu menu, Action action) {
		JMenuItem item = menu.add(action);
		configureMenuItem(item, action);
		return item;
	}

	public static JMenuItem addMenuItem(JPopupMenu menu, Action action) {
		JMenuItem item = menu.add(action);
		configureMenuItem(item, action);
		return item;
	}

	private static void configureMenuItem(JMenuItem item, Action action) {
		KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
		if (keystroke != null) {
			item.setAccelerator(keystroke);
		}

		item.setIcon(null);
		item.setToolTipText(null);
	}
}
