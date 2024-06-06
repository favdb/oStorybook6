/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ui.MIG;

public class UnicodeDialog extends JDialog {

    private static Icon icon = IconUtil.getIconSmall(ICONS.K.CHAR_UNICODE);
    private static String title = I18N.getMsg("shef.unicode"),
	    desc = I18N.getMsg("shef.unicode_desc");
    private final ActionListener buttonHandler = new ButtonHandler();
    private boolean insertEntity;
    private JTextComponent editor;
    private static final int[] CODE_RANGES_START = {8592, 9632, 9985, 9728};
    private static final int[] CODE_RANGES_END = {8703, 9727, 10175, 9923};
    private static final String[] CODE_RANGES_NAMES = {
	"Arrows", "Geometric", "Dingbats", "Symbols"
    };
    private JPanel charPanel;
    private JComboBox cbType;

    public UnicodeDialog(Frame parent, JTextComponent ed) {
	super(parent, title);
	editor = ed;
	init();
    }

    public UnicodeDialog(Dialog parent, JTextComponent ed) {
	super(parent, title);
	editor = ed;
	init();
    }

    private void init() {
	getContentPane().setLayout(new BorderLayout());

	getContentPane().add(new HeaderPanel(title, "", icon), BorderLayout.NORTH);

	getContentPane().add(initCharPanel(), BorderLayout.CENTER);

	JButton close = new JButton(I18N.getMsg("shef.close"));
	close.addActionListener((ActionEvent e) -> {
	    setVisible(false);
	});
	JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	bottomPanel.add(close);
	getContentPane().add(bottomPanel, BorderLayout.SOUTH);

	pack();
	setResizable(false);
	this.getRootPane().setDefaultButton(close);
    }

    @SuppressWarnings("unchecked")
    private JPanel initCharPanel() {
	JPanel p = new JPanel(new MigLayout());
	JPanel p1 = new JPanel(new MigLayout());
	p1.add(new JLabel("bloc: "));
	cbType = new JComboBox(CODE_RANGES_NAMES);
	cbType.setSelectedIndex(0);
	cbType.addActionListener(e -> loadCharPanel());
	p1.add(cbType, MIG.get(MIG.GROW));
	p.add(p1, MIG.SPAN);
	charPanel = new JPanel(new MigLayout(MIG.WRAP + " 16"));
	charPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	JScrollPane scroll = new JScrollPane(charPanel);
	p.add(scroll);
	loadCharPanel();
	return p;
    }

    private void loadCharPanel() {
	int n = cbType.getSelectedIndex();
	charPanel.removeAll();
	for (int i = CODE_RANGES_START[n]; i <= CODE_RANGES_END[n]; i++) {
	    charPanel.add(inCharButton(i));
	}
	charPanel.revalidate();
    }

    private JButton inCharButton(int inchar) {
	JButton bt = new JButton(Character.toString((char) inchar));
	bt.setToolTipText("" + inchar);
	bt.addActionListener(buttonHandler);
	return bt;
    }

    public void setJTextComponent(JTextComponent ed) {
	editor = ed;
    }

    public JTextComponent getJTextComponent() {
	return editor;
    }

    private class ButtonHandler implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    JButton l = (JButton) e.getSource();
	    if (editor != null) {
		if (!editor.hasFocus()) {
		    editor.requestFocusInWindow();
		}
		editor.replaceSelection(l.getText());
	    }
	}

    }

    /**
     * @return the insertEntity
     */
    public boolean isInsertEntity() {
	return insertEntity;
    }

    /**
     * @param insertEntity the insertEntity to set
     */
    public void setInsertEntity(boolean insertEntity) {
	this.insertEntity = insertEntity;
    }

}
