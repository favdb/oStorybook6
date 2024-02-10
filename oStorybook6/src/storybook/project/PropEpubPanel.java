/*
 * Copyright (C) 2020 favdb
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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.project;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import resources.MainResources;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 * JPanel class for publication properties
 *
 * @author favdb
 */
public class PropEpubPanel extends JPanel implements ActionListener, CaretListener {

	private static final String TT = "EpubPanel";

	private final MainFrame mainFrame;
	private JTextField tfUUID;
	private JTextField tfISBN;
	private JComboBox<Object> cbLanguage;
	private JCheckBox ckCover;
	private JSLabel lbCover;
	private final PropertiesDlg properties;
	private JPanel pCover;
	private JButton btUUID;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PropEpubPanel(MainFrame mainFrame, PropertiesDlg properties) {
		this.mainFrame = mainFrame;
		this.properties = properties;
		init();
		initUi();
	}

	public void init() {
		// empty
	}

	public void initUi() {
		//LOG.printInfos("EpubDlg.initUi()");
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE2, MIG.WRAP), "[][grow]"));
		// paramètres à saisir
		add(new JLabel(I18N.getMsg("epub.uuid")));
		tfUUID = new JTextField(mainFrame.project.book.getUUID());
		tfUUID.addCaretListener(this);
		add(tfUUID, MIG.get(MIG.SPLIT2, MIG.GROWX));
		btUUID = Ui.initButton("btUuid", "epub.uuid.create", ICONS.K.COGS, "uuid.create",
		   e -> createUUID());
		add(btUUID);
		if (tfUUID.getText().isEmpty()) {
			tfUUID.setVisible(false);
		} else {
			btUUID.setVisible(false);
		}
		add(new JLabel(I18N.getMsg("epub.isbn")));
		tfISBN = new JTextField(mainFrame.project.book.getISBN());
		add(tfISBN, MIG.GROWX);

		JLabel lbLanguage = new JLabel(I18N.getMsg("language"));
		String str = IOUtil.resourceRead("languages/languages.txt", MainResources.class);
		String list[] = str.split("\n");
		cbLanguage = new javax.swing.JComboBox<>();
		for (String s : list) {
			cbLanguage.addItem(s.substring(3));
		}
		add(lbLanguage);
		String x = I18N.getCountryLanguage(Locale.getDefault()).substring(0, 2);
		if (!mainFrame.getBook().getLanguage().isEmpty()) {
			x = mainFrame.getBook().getLanguage();
		}
		int n = 0;
		for (String s : list) {
			if (s.startsWith(x)) {
				cbLanguage.setSelectedIndex(n);
				break;
			}
			n++;
		}
		add(cbLanguage, MIG.WRAP);

		// cover personnalisation
		ckCover = new JCheckBox(I18N.getMsg("epub.cover_use"));
		ckCover.setSelected(mainFrame.project.book.param.getParamExport().getEpubCover());
		ckCover.addActionListener(this);
		add(ckCover, MIG.get("skip", MIG.SPLIT + " 3", MIG.TOP));
		add(initCover());
		changeCover();
	}

	public boolean check() {
		return (true);
	}

	public void apply() {
		mainFrame.project.book.setUUID(tfUUID.getText());
		mainFrame.project.book.setISBN(tfISBN.getText());
		mainFrame.project.book.param.getParamExport().setEpubCover(ckCover.isSelected());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		//LOG.traceEvent(TT + ".actionPerformed", event);
		if (event.getSource() instanceof JCheckBox) {
			changeCover();
		}
		if (event.getSource() instanceof JButton) {
			JButton bt = (JButton) event.getSource();
			if (bt.getName().equalsIgnoreCase("changeCover")) {
				PropEpubCover.show(properties);
				setCoverIcon();
			}
		}
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	private void changeCover() {
		pCover.setVisible(ckCover.isSelected());
	}

	private JPanel initCover() {
		//LOG.printInfos(TT+".initCover()");
		pCover = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP, MIG.INS0), "[]"));
		lbCover = new JSLabel();
		lbCover.setMinimumSize(new Dimension(130, 250));
		lbCover.setHorizontalAlignment(SwingConstants.CENTER);
		lbCover.setVerticalAlignment(SwingConstants.CENTER);
		lbCover.setBorder(BorderFactory.createEtchedBorder());
		setCoverIcon();
		pCover.add(lbCover, MIG.GROW);
		JButton change = new JButton(I18N.getMsg("epub.cover.create"));
		change.setName("changeCover");
		change.addActionListener(this);
		pCover.add(change, MIG.CENTER);
		return (pCover);
	}

	private void setCoverIcon() {
		//LOG.printInfos(TT + ".setCoverIcon()");
		File file = new File(mainFrame.getProject().getPath()
		   + File.separator
		   + "Images"
		   + File.separator
		   + "cover.jpeg");
		if (file.exists()) {
			lbCover.setIcon(IconUtil.getIconExternal(file.getAbsolutePath(), new Dimension(130, 250)));
		} else {
			lbCover.setIcon(null);
		}
	}

	private void createUUID() {
		UUID uuid = UUID.randomUUID();
		tfUUID.setText(uuid.toString());
		btUUID.setVisible(false);
		tfUUID.setVisible(true);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (tfUUID.getText().isEmpty()) {
			btUUID.setVisible(true);
			tfUUID.setVisible(false);
		}
	}

}
