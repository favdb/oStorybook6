/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.test;

import i18n.I18N;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import api.shef.ShefEditor;
import api.shef.tools.LOG;

/**
 *
 * @author favdb
 */
public class SHEF extends JFrame implements ActionListener {

	private JFrame mainFrame;
	private ShefEditor editor;
	private String fileName = "";
	private String dlg_lang = "lang_all";
	private String mode = "full";
	private int endnotesStart = -1;
	private String endnotesLink = "";

	public SHEF() {
		super();
	}

	public void init() {
		String texte = "";
		InputStream in = SHEF.class.getResourceAsStream("htmlsnip.txt");
		try {
			texte = read(in);
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
		editor = new ShefEditor("", "colored, full, lang_all");
		editor.setText(texte);
		editor.setCaretPosition(0);
		mainFrame = new JFrame();
		//mainFrame.setLayout(new BorderLayout());
		mainFrame.setName("mainFrame");
		JMenuBar menubar = initMenu();
		mainFrame.setJMenuBar(menubar);
		mainFrame.setTitle("HTML Editor Demo");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(800, 600);
		mainFrame.setVisible(true);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.add(editor);
	}

	private JMenuBar initMenu() {
		JMenuBar menu = new JMenuBar();
		JMenu fichier = new JMenu(I18N.getMsg("shef.file"));
		fichier.add(initMenuItem("file.new"));
		fichier.add(initMenuItem("file.open"));
		fichier.add(initMenuItem("file.save"));
		fichier.add(initMenuItem("file.saveas"));
		fichier.add(initMenuItem("file.exit"));
		menu.add(fichier);
		JMenu param = new JMenu(I18N.getMsg("shef.options"));
		param.add(initMenuItem("endnotes.set"));
		JMenu paramMode = new JMenu(I18N.getMsg("shef.mode"));
		paramMode.add(initMenuItem("mode.none"));
		paramMode.add(initMenuItem("mode.reduced"));
		paramMode.add(initMenuItem("mode.extended"));
		param.add(paramMode);
		JMenu paramLang = new JMenu(I18N.getMsg("shef.lang"));
		paramLang.add(initMenuItem("lang.en"));
		paramLang.add(initMenuItem("lang.fr"));
		paramLang.add(initMenuItem("lang.all"));
		param.add(paramLang);
		menu.add(param);
		return (menu);
	}

	private JMenuItem initMenuItem(String name) {
		JMenuItem item = new JMenuItem(I18N.getMsg("shef." + name));
		item.setName(name);
		item.addActionListener(this);
		return item;
	}

	public static void main(String args[]) {
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.contains("--trace")) {
					LOG.setLevel(LOG.DEV);
				}
				if (arg.contains("--debug")) {
					LOG.setLevel(LOG.DEBUG);
				}
			}
		}
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					Font fnt = (Font) UIManager.getLookAndFeelDefaults().get("defaultFont");
					if (fnt != null) {
						float sz = fnt.getSize();
						Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
						float fc;
						if (d.width < 1080) {
							fc = (float) 1.0;
						} else if (d.width < 1245) {
							fc = (float) 1.2;
						} else if (d.width < 1935) {
							fc = (float) 1.4;
						} else {
							fc = (float) 1.6;
						}
						sz = (float) (sz * fc);
						Font fx = fnt.deriveFont(sz);
						UIManager.getLookAndFeelDefaults().put("defaultFont", fx);
					}
					break;
				}
			}
		} catch (ClassNotFoundException | IllegalAccessException
			| InstantiationException | UnsupportedLookAndFeelException e) {
		}

		SwingUtilities.invokeLater(() -> {
			SHEF demo = new SHEF();
			demo.init();
		});
	}

	public String read(InputStream input) throws IOException {
		InputStreamReader stream = new InputStreamReader(input);
		StringBuilder sb;
		try (BufferedReader reader = new BufferedReader(stream)) {
			sb = new StringBuilder();
			int ch;
			while ((ch = reader.read()) != -1) {
				sb.append((char) ch);
			}
		}
		return sb.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src instanceof JMenuItem) {
			switch (((JMenuItem) src).getName()) {
				case "file.exit":
					System.exit(0);
					return;
				case "file.new":
					fileNew();
					return;
				case "file.open":
					fileOpen();
					return;
				case "file.save":
					fileSave();
					return;
				case "file.saveas":
					fileSaveAs();
					return;
				case "mode.none":
					mode = "none";
					break;
				case "mode.reduced":
					mode = "reduced";
					break;
				case "mode.extended":
					mode = "full";
					break;
				case "lang.en":
					dlg_lang = "lang_en";
					break;
				case "lang.fr":
					dlg_lang = "lang_fr";
					break;
				case "lang.all":
					dlg_lang = "lang_all";
					break;
			}
			String texte = editor.getText();
			mainFrame.remove(editor);
			editor = new ShefEditor("", mode + ", " + dlg_lang);
			editor.setText(texte);
			editor.setCaretPosition(0);
			mainFrame.add(editor);
		}
	}

	private void fileNew() {
		fileName = "";
		editor.setText("");
		editor.setCaretPosition(0);
	}

	private void fileOpen() {
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File(fileName));
		fc.setApproveButtonText(I18N.getMsg("shef.file.open"));
		if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.exists() || !file.isFile()) {
				return;
			}
			try {
				try (InputStream in = new FileInputStream(file)) {
					editor.setText(read(in));
					fileName = file.getAbsolutePath();
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace(System.err);
			} catch (IOException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	private void fileSave() {
		File file = new File(fileName);
		if (fileName.isEmpty()) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(fileName));
			fc.setApproveButtonText(I18N.getMsg("shef.file.save"));
			if (fc.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			file = fc.getSelectedFile();
			if (!file.exists() || !file.isFile()) {
				return;
			}
		}
		try {
			try (OutputStream out = new FileOutputStream(file)) {
				out.write(editor.getText().getBytes());
				fileName = file.getAbsolutePath();
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace(System.err);
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
	}

	private void fileSaveAs() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(new File(fileName));
		fc.setApproveButtonText(I18N.getMsg("shef.file.saveas"));
		if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists() || !file.isFile()) {
				return;
			}
			try {
				try (OutputStream out = new FileOutputStream(file)) {
					out.write(editor.getText().getBytes());
					fileName = file.getAbsolutePath();
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace(System.err);
			} catch (IOException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

}
