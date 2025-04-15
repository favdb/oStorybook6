/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import i18n.I18N;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Dialogue pour l'affichage d'une exception
 *
 * @author favdb
 */
public class ExceptionDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final int PREFERRED_WIDTH = 450;
	private static final String DEFAULT_TITLE = I18N.getMsg("shef.error");
	private JPanel jContentPane = null;
	private JLabel iconLabel = null;
	private JLabel titleLabel = null;
	private JLabel msgLabel = null;
	private JPanel buttonPanel = null;
	private JButton okButton = null;
	private JButton detailsButton = null;
	private JScrollPane scrollPane = null;
	private JTextArea textArea = null;
	private JSeparator separator = null;

	/**
	 * Constructeur par défaut
	 */
	public ExceptionDialog() {
		super();
		init(new Exception());

	}

	/**
	 * Constructeur si le parent est une Frame
	 *
	 * @param owner : frame parent
	 */
	public ExceptionDialog(Frame owner, Throwable th) {
		super(owner, DEFAULT_TITLE);
		init(th);

	}

	/**
	 * Constructeur si le parent est un Dialogue
	 *
	 * @param owner : dialog parent
	 */
	public ExceptionDialog(Dialog owner, Throwable th) {
		super(owner, DEFAULT_TITLE);
		init(th);
	}

	private void init(Throwable th) {
		setModal(true);
		initialize();
		setThrowable(th);
		showDetails(false);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		getRootPane().setDefaultButton(getOkButton());
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setBorder(BorderFactory.createEmptyBorder(12, 5, 10, 5));

			iconLabel = new JLabel();
			iconLabel.setText("");
			iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
			jContentPane.add(iconLabel, new GBC("0,0, insets 0 0 0 0, weighty 0.0, anchor N"));
			titleLabel = new JLabel();
			titleLabel.setText(I18N.getMsg("shef.error_prompt"));
			jContentPane.add(titleLabel, new GBC("0,1, fill N, anchor W, insets 0 0 0 5"));
			msgLabel = new JLabel();
			msgLabel.setText("");
			jContentPane.add(msgLabel, new GBC("1, 1, anchor W, insets 0 25 5 5"));
			jContentPane.add(getButtonPanel(), new GBC("0, 2, fill V, gridheight 3, insets 0 0 10 0, anchor W"));
			jContentPane.add(getScrollPane(), new GBC("4, 0, fill B, weightx 1.0, weighty 1.0, gridwidth 3, insets 0 0 0 0"));
			jContentPane.add(getSeparator(), new GBC("3, 0, fill H, anchor W, gridwidth 3,  insets 0 0 0 0"));
		}
		return jContentPane;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(2);
			gridLayout.setHgap(0);
			gridLayout.setVgap(5);
			gridLayout.setColumns(1);
			buttonPanel = new JPanel();
			buttonPanel.setLayout(gridLayout);
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getDetailsButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(I18N.getMsg("shef.ok"));
			okButton.addActionListener((java.awt.event.ActionEvent e) -> {
				dispose();
			});
		}
		return okButton;
	}

	/**
	 * This method initializes detailsButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDetailsButton() {
		if (detailsButton == null) {
			detailsButton = new JButton();
			detailsButton.setText(I18N.getMsg("shef.details.prior"));
			detailsButton.addActionListener((java.awt.event.ActionEvent e) -> {
				toggleDetails();
			});
		}
		return detailsButton;
	}

	private void toggleDetails() {
		showDetails(!isDetailsVisible());
	}

	public void showDetails(boolean b) {
		if (b) {
			detailsButton.setText(I18N.getMsg("shef.details.next"));
			scrollPane.setVisible(true);
			separator.setVisible(false);
		} else {
			detailsButton.setText(I18N.getMsg("shef.details.prior"));
			scrollPane.setVisible(false);
			separator.setVisible(true);
		}
		setResizable(true);
		pack();
		setResizable(false);
	}

	public boolean isDetailsVisible() {
		return scrollPane.isVisible();
	}

	public void setThrowable(Throwable th) {
		String msg = I18N.getMsg("shef.no_message_given");
		if (th.getLocalizedMessage() != null && !th.getLocalizedMessage().equals("")) {
			msg = th.getLocalizedMessage();
		}
		msgLabel.setText(msg);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		th.printStackTrace(new PrintStream(bout));
		String stackTrace = new String(bout.toByteArray());
		textArea.setText(stackTrace);
		textArea.setCaretPosition(0);
	}

	/**
	 * @return a JSeparator that gets swapped out with the detail pane.
	 */
	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
			separator.setPreferredSize(new Dimension(PREFERRED_WIDTH, 3));
		}
		return separator;
	}

	/**
	 * This method initializes scrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, 200));
			scrollPane.setViewportView(getTextArea());
		}
		return scrollPane;
	}

	/**
	 * This method initializes textArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
			textArea.setTabSize(2);
			textArea.setEditable(false);
			textArea.setOpaque(false);
		}
		return textArea;
	}
}
