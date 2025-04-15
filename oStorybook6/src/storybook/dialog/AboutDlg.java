/*
 * Copyright (C) 2017 favdb
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
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Const.STORYBOOK;
import storybook.tools.LOG;
import storybook.tools.clip.Clip;
import storybook.tools.exec.CallProcess;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class AboutDlg extends AbsDialog {

	public AboutDlg(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		super.initUi();
		this.setTitle(I18N.getMsg("about.title"));
		setLayout(new MigLayout(MIG.FLOWY, "[center]", "[]10[]10[]10[]"));
		setPreferredSize(new Dimension(800, 600));

		// logo
		JLabel lbLogo = new JLabel((ImageIcon) IconUtil.getIcon("banner"));
		lbLogo.setBackground(Color.WHITE);
		lbLogo.setBorder(BorderFactory.createEtchedBorder());

		// application info
		JLabel lbInfo = new JLabel();
		StringBuilder buf = new StringBuilder();
		buf.append(Const.getFullName());
		buf.append(" (").append(STORYBOOK.RELEASE_DATE).append(")");
		lbInfo.setText(buf.toString());
		JTabbedPane pane = new JTabbedPane();
		// licenses
		pane.addTab("Copyright (GPL)", initCopyRights());
		// credits
		pane.addTab("Credits", initCredits());
		// versions
		pane.addTab("Versions", initVersions());
		// system properties
		pane.addTab("System Properties", initSytems());
		// layout
		add(lbLogo, MIG.GROWX);
		add(lbInfo);
		add(pane, MIG.GROWX);
		add(getCloseButton(), MIG.RIGHT);
	}

	private JPanel initSytems() {
		JPanel panel = new JPanel(new MigLayout(MIG.FILLX));
		JTextPane tx = new JTextPane();
		tx.setEditable(false);
		tx.setContentType(Html.TYPE);
		StringBuilder ta = new StringBuilder();
		if (!CallProcess.getPythonVersion().isEmpty()) {
			ta.append(Html.intoB("python version: "))
					.append(CallProcess.getPythonVersion())
					.append(Html.BR);
		}
		ta.append(Html.BR)
				.append(Html.intoB(Const.getName()))
				.append(Html.BR);
		ta.append(Const.getVersionFull()).append(Html.BR);
		String[] keys = {
			//about operating system
			"-Operating system", "os.name", "os.version", "os.arch",
			//about Java
			"-Java", "java.runtime.name", "java.version",
			"java.vm.version", "java.vm.vendor", "java.class.version",
			//about system
			"-System file", "path.separator", "line.separator",
			"file.encoding", "file.separator",
			//about user
			"-User parameters", "user.language", "user.country",
			"user.dir", "user.home", "user.timezone"};
		Properties props = System.getProperties();
		for (String key : keys) {
			if (key.charAt(0) == '-') {
				ta.append(Html.BR).append(Html.intoB(key.substring(1))).append(Html.BR);
				continue;
			}
			try {
				String k = props.getProperty(key);
				if (k == null) {
					continue;
				}
				ta.append(key);
				ta.append(": ");
				k = k.replace("\n", "LF").replace("\r", "CR");
				ta.append(k);
				ta.append(Html.BR);
			} catch (Exception e) {
			}
		}
		String txt = Html.HTML_B + Html.BODY_B + ta.toString().replace(Html.BR, "<br>\n")
				+ Html.BODY_E + Html.HTML_E;
		tx.setText(txt);
		tx.setCaretPosition(0);
		panel.add(new JScrollPane(tx), MIG.get(MIG.GROW, MIG.WRAP));
		JButton bt = this.getButton("copy.to_clip", "edit_copy");
		bt.addActionListener(e -> {
			try {
				Clip.to(txt, "txt");
				JOptionPane.showMessageDialog(this,
						I18N.getMsg("copy.to_clip_ok"),
						I18N.getMsg("copied.title"), 1);
			} catch (HeadlessException exc) {
			}
		});
		panel.add(bt, MIG.get(MIG.SPAN, MIG.CENTER));
		return panel;
	}

	private static Border ETCHED_BORDER = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

	private JScrollPane initCopyRights() {
		JTextPane taGpl = new JTextPane();
		taGpl.setContentType(Html.TYPE);
		taGpl.setEditable(false);
		taGpl.setText(Const.getGpl());
		taGpl.addHyperlinkListener(e -> openURL(e));
		JScrollPane scroller = new JScrollPane(taGpl);
		scroller.setBorder(ETCHED_BORDER);
		taGpl.setCaretPosition(0);
		return scroller;
	}

	private void getCredits(StringBuilder b, String[] lib) {
		b.append(Html.P_B);
		for (String s : lib) {
			b.append(s).append(Html.BR);
		}
		b.append(Html.P_E);
	}

	private JScrollPane initCredits() {
		String[] dev = {
			"<b>Developers</b>",
			"Franz-Albert(aka FaVdB)",
			"Jean Rebillat",
			"<b><i>Regular contributors</i></b>",
			"Bruno Raoult",
			"Bernard Méhaut",
			"Marc Torres",
			"Thibaud F",
			"Don Edwards"
		};
		String[] translators = {
			"<b>Translation</b>",
			"<b><i>Translators for " + Const.getVersionFull() + "</i></b>",
			"English: <b>The Storybook Developer Crew</b>",
			"Català: <b>Emili Cid</b>",
			"Français: <b>FaVdB</b>",
			"Hungarian: <b>Sinkovics Vivien</b>",
			"Portuguese: <b>Pedro Albuquerque</b>",
			"",
			"<b><i>Translators for older versions</i></b>",
			"English Proof-Reading: <b>Rory O'Farell, Mark Coolen</b>",
			"Japanese: <b>Asuka Yuki (飛香宥希/P.N.)</b>",
			"Spanish: <b>Stephan Miralles D&iacute;az</b>",
			"",
			"<b><i>The Permondo team</i></b> (for older version)",
			"Arabic: <b>Noha Amr</b>",
			"Bulgarian: <b>Elitsa Stoycheva</b>",
			"Chinese: <b>Feier Yang</b>",
			"Czech: <b>Linda Blättler</b>",
			"Danish: <b>Kira Petersen</b>",
			"Dutch: <b>Surayah de Visser</b>",
			"German: <b>Katharina Staab</b>",
			"Greek: <b>Dimitra Andrikou</b>",
			"Italian: <b>Flavia Guadagnino</b>",
			"Korean : <b>Kyunghee Jung</b>",
			"Polish: <b>Kamila El Khayati</b>",
			"Romanian : <b>Agnes Erika Stan</b>",
			"Russian: <b>Volodymyr Sushkov</b>",
			"Turkish: <b>Şeyma Aydın</b>",
			"Ukrainian : <b>Marina Nochniuk</b>"
		};
		String thanks[] = {
			"<b>Synonyms/antonyms dictionaries</b>",
			"English: online <a href=\"https://www.merriam-webster.com\">Merriam-Webster Dictionary</a>",
			"Français: Laboratoire"
			+ " <a href=\"http://crisco.unicaen.fr/des\">CRISCO</a>"
			+ ", UR4255, Université de Caen\" ",
			"",
			"<b>Grammar tool</b>",
			"- <a href=\"https://languagetool.org\">LanguageTool</a>",
			"- <a href=\"https://grammalecte.net\">Grammalecte</a> pour le Français",
			"<br><b>Templates image for Cover generator</b>",
			" <a href=\"https://victoriasjournals.ca\">Victoria's Journals</a>"
		};
		JTextPane taCredits = new JTextPane();
		StringBuilder b = new StringBuilder();
		b.append(Html.HTML_B)
				.append(Html.getBody(SwingUtil.setFontDefault(mainFrame)));
		getCredits(b, dev);
		b.append(Html.intoP(Html.intoB("Logo Designer : ")
				+ "Jose Campoy, modified by FaVdB"));
		getCredits(b, translators);
		getCredits(b, thanks);
		b.append(Html.BODY_E).append(Html.HTML_E);
		taCredits.setContentType(Html.TYPE);
		taCredits.setEditable(false);
		taCredits.setText(b.toString());
		taCredits.addHyperlinkListener(e -> openURL(e));
		JScrollPane scroller = new JScrollPane(taCredits);
		scroller.setBorder(ETCHED_BORDER);
		taCredits.setCaretPosition(0);
		return scroller;
	}

	private JPanel initVersions() {
		JPanel pVersions = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		JEditorPane lbVersions = new JEditorPane();
		lbVersions.setContentType("text/html");
		lbVersions.setEditable(false);
		lbVersions.setText("<html>please check the complete version file on <a href=\""
				+ Net.KEY.HOME.toString()
				+ "articles.php?lng=fr&pg=5&mnuid=1&tconfig=0\"> website</a>"
				+ "</html>");
		lbVersions.addHyperlinkListener(e -> openURL(e));
		JLabel lb = new JLabel();
		lbVersions.setForeground(lb.getForeground());
		lbVersions.setBackground(lb.getBackground());
		lbVersions.setFont(FontUtil.getItalic(lbVersions.getFont()));
		String filePath = EnvUtil.getUserDir() + File.separator + "Versions.txt";
		String d = IOUtil.fileReadAsString(filePath);
		if (d.isEmpty()) {
			d = IOUtil.resourceRead("Versions.txt", App.class);
			pVersions.add(lbVersions, MIG.GROW);
		}
		JTextPane text = new JTextPane();
		text.setFont(App.fonts.monoGet());
		text.setEditable(false);
		JScrollPane scroller = new JScrollPane(text);
		scroller.setBorder(ETCHED_BORDER);
		scroller.setMaximumSize(SwingUtil.getScreenSize());
		StringBuilder b = new StringBuilder();
		if (!d.isEmpty()) {
			b.append(Html.HTML_B)
					.append(Html.getBody(Html.getFontMono(mainFrame)))
					.append(Html.intoP(Html.intoI(Html.intoU("Build: "
							+ getBuildDate())), Html.AL_CENTER))
					.append(Html.textToHTML(d))
					.append(Html.BODY_E).append(Html.HTML_E);
		} else {
			b.append(Html.HTML_B).append(
					Html.getBody(SwingUtil.setFontDefault(mainFrame)));
			b.append(Html.intoP("No Versions.txt file", Html.AL_CENTER));
			b.append(Html.BODY_E).append(Html.HTML_E);
		}
		text.setContentType(Html.TYPE);
		text.setText(b.toString());
		text.setCaretPosition(0);
		pVersions.add(scroller, MIG.GROW);
		return pVersions;
	}

	public static void show(MainFrame m) {
		SwingUtil.showDialog(new AboutDlg(m), m, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public String getBuildDate() {
		try {
			Properties buildProps = new Properties();
			buildProps.load(getClass().getResourceAsStream("/buildinfo.properties"));
			return (buildProps.getProperty("build.date"));
		} catch (IOException ex) {
			LOG.err("AboutDlg.getBuildDate() exception", ex);
		}
		return "????";
	}

	private void openURL(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			Net.openBrowser(e.getDescription());
		}
	}
}
