/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import javax.swing.Icon;
import javax.swing.JPanel;

public class OptionDialog extends StandardDialog {

	private JPanel internalContentPane;
	private Container contentPane;

	public OptionDialog(Frame parent, String headerTitle, String desc, Icon icon) {
		super(parent, headerTitle, BUTTONS_RIGHT);
		init(headerTitle, desc, icon);
	}

	public OptionDialog(Dialog parent, String headerTitle, String desc, Icon icon) {
		super(parent, headerTitle, BUTTONS_RIGHT);
		init(headerTitle, desc, icon);
	}

	private void init(String title, String desc, Icon icon) {
		internalContentPane = new JPanel(new BorderLayout());
		HeaderPanel hp = new HeaderPanel(title, desc, icon);
		internalContentPane.add(hp, BorderLayout.NORTH);
		super.setContentPane(internalContentPane);
	}

	@Override
	public Container getContentPane() {
		return contentPane;
	}

	@Override
	public void setContentPane(Container c) {
		contentPane = c;
		internalContentPane.add(c, BorderLayout.CENTER);
	}

}
