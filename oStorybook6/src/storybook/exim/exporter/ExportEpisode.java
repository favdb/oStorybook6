/*
 * Copyright (C) 2022 favdb
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.exim.exporter;

import i18n.I18N;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import storybook.db.book.Book;
import storybook.db.episode.Episode;
import storybook.db.strand.Strand;
import static storybook.exim.exporter.AbstractExport.SILENT;
import storybook.tools.DateUtil;
import storybook.tools.html.Html;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

/**
 * export the Episode content to HTML beautify
 *
 * @author favdb
 */
public class ExportEpisode extends AbstractExport {

	private static final String TT = "ExportEpisode";
	private List<Episode> episodes;
	private List<Strand> strands;
	private int nbStrands;

	public ExportEpisode(MainFrame m) {
		super(m, "html");
		init();
	}

	public ExportEpisode(MainFrame m, String format) {
		super(m, format);
		init();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		//LOG.printInfos(TT + ".init()");
		strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		nbStrands = strands.size();
		episodes = (List) mainFrame.project.getList(Book.TYPE.EPISODE);
	}

	/**
	 * do an export to a file
	 *
	 * @param mainFrame
	 * @param format
	 * @return true if export to file is ok
	 */
	public static boolean toFile(MainFrame mainFrame, String format) {
		//LOG.trace(TT + ".toFile(mainFrame, format='" + format + "')");
		ExportEpisode export = new ExportEpisode(mainFrame, format);
		return export.writeFile();
	}

	public static String toHtml(MainFrame mf) {
		ExportEpisode export = new ExportEpisode(mf, "html");
		return export.toHtml();
	}

	@SuppressWarnings("unchecked")
	private String toHtml() {
		String title = I18N.getColonMsg("episodes") + " " + book.getTitle();
		StringBuilder buf = new StringBuilder();
		buf.append(Html.DOCTYPE);
		buf.append(Html.HTML_B_LANG);
		buf.append(Html.HEAD_B);
		buf.append(Html.META_UTF8);
		if (!title.isEmpty()) {
			buf.append(Html.intoTag("title", title));
		} else {
			buf.append(Html.intoTag("title", mainFrame.getBook().getTitle()));
		}
		//String st1 = "style=\"border: 1px solid black; border-collapse: collapse;\"\n";
		String st1 = "border=\"1\" cellspacing=\"0\" cellpadding=\"0\"";
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		buf.append(Html.intoH(1, title, "style=\"text-align:center;\""));
		buf.append(Html.P_CENTER).append(Html.intoI("("
		   + DateUtil.simpleDateTimeToString(new Date(), true)
		   + ")")).append(Html.P_E);
		// table start
		buf.append("<table ").append(st1).append(">\n");
		// heading columns
		StringBuilder bx = new StringBuilder();
		bx.append(Html.intoTD(I18N.getMsg("number"), st1));
		bx.append(Html.intoTD(I18N.getMsg("name"), st1));
		bx.append(Html.intoTD(I18N.getMsg("chapters") + "/" + I18N.getMsg("scenes"), st1));
		bx.append(Html.intoTD(I18N.getMsg("plot"), st1));
		for (Strand strand : strands) {
			bx.append(Html.intoTD(strand.getName(), st1));
		}
		buf.append(Html.intoTR(bx.toString(), st1));
		// table content
		buf.append(getHtmlEpisodes());
		// table end
		buf.append(Html.TABLE_E);
		buf.append(Html.BODY_E);
		buf.append(Html.HTML_E);

		return buf.toString();
	}

	/**
	 * write a write
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean writeFile() {
		//LOG.printInfos(TT + ".writeFile()");
		String fileName = "Episode";
		if (askFileExists(fileName)) {
			Object[] options = {
				I18N.getMsg("replace"),
				I18N.getMsg("export.replace_add_date"),
				I18N.getMsg("cancel"),};
			int r = JOptionPane.showOptionDialog(mainFrame,
			   I18N.getMsg("export.replacenew", param.getFileName()),
			   I18N.getMsg("export"),
			   JOptionPane.YES_NO_CANCEL_OPTION,
			   JOptionPane.QUESTION_MESSAGE,
			   null,
			   options,
			   options[0]);
			switch (r) {
				case 0:
					break;
				case 1:
					SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
					fileName = fileName + formatter.format(new Date());
					break;
				case 2:
					return (false);
			}
		}
		switch (param.getFormat()) {
			case "txt":
			case "csv":
				return writeTxtFile(fileName);
			case "xml":
				return writeXmlFile(fileName);
			default:
				return writeHtmlFile(fileName);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean writeHtmlFile(String fileName) {
		String title = I18N.getColonMsg("episodes") + " " + book.getTitle();
		setHtmlTitle(title);
		if (!openFile(fileName, true)) {
			return false;
		}
		writeText(toHtml());
		closeFile(SILENT);
		JOptionPane.showMessageDialog(mainFrame,
		   I18N.getMsg("export.success", param.getFileName()),
		   I18N.getMsg("export"), JOptionPane.INFORMATION_MESSAGE);
		return true;
	}

	private String getHtmlEpisodes() {
		//String st1 = "border-collapse: collapse;border: 1px solid;", stx = "style=\"" + st1 + "\"";
		StringBuilder buf = new StringBuilder();
		boolean b = false;
		int col = 0;
		//String style1 = "style=\"vertical-align: top;text-align: center;" + st1 + "\"",
		//   style2 = "style=\"vertical-align: top;" + st1 + "\"";
		String style1 = "style=\"vertical-align: top;text-align: center;" + "\"",
		   style2 = "style=\"vertical-align: top;" + "\"";
		for (int i = 0; i < episodes.size(); i++) {
			Episode episode = episodes.get(i);
			if (episode.getStrand() == null) {
				if (b) {
					while (col < nbStrands) {
						buf.append(Html.intoTD(Html.nbsp(1)));
						col++;
					}
					buf.append(Html.TR_E);
				}
				buf.append(Html.TR_B);
				buf.append(Html.intoTD(episode.getNumber().toString(), style1));
				buf.append(Html.intoTD(episode.getName(), style2));
				if (episode.hasLink()) {
					buf.append(Html.intoTD(episode.getLink().getName(), style1));
				} else {
					buf.append(Html.intoTD("&nbsp;"));
				}
				String txt = episode.getNotes();
				if (txt.isEmpty()) {
					buf.append(Html.intoTD(Html.nbsp(1)));
				} else {
					buf.append(Html.intoTD(txt, style2));
				}
				b = true;
				col = 0;
			} else {
				int ncol = strands.indexOf(episode.getStrand());
				for (int j = col; j < ncol; j++) {
					buf.append(Html.intoTD(Html.nbsp(1)));
					col++;
				}
				String txt = episode.getNotes();
				if (txt.isEmpty()) {
					buf.append(Html.intoTD(Html.nbsp(1)));
				} else {
					buf.append(Html.intoTD(txt, style2));
				}
				col++;
			}
		}
		if (b) {
			while (col < nbStrands) {
				buf.append(Html.intoTD(Html.nbsp(1)));
				col++;
			}
			buf.append(Html.TR_E);
		}
		return buf.toString();
	}

	private void writeHtmlEpisodes() {
		boolean b = false;
		int col = 0;
		String style1 = "style=\"vertical-align: top;text-align: center;\"",
		   style2 = "style=\"vertical-align: top;\"";
		for (int i = 0; i < episodes.size(); i++) {
			Episode episode = episodes.get(i);
			if (episode.getStrand() == null) {
				if (b) {
					while (col < nbStrands) {
						writeText(Html.intoTD(Html.nbsp(1)));
						col++;
					}
					writeText(Html.TR_E);
				}
				writeText(Html.TR_B);
				writeText(Html.intoTD(episode.getNumber().toString(), style1));
				writeText(Html.intoTD(episode.getName(), style2));
				if (episode.hasLink()) {
					writeText(Html.intoTD(episode.getLink().getName(), style1));
				} else {
					writeText(Html.intoTD("&nbsp;"));
				}
				String txt = episode.getNotes();
				if (txt.isEmpty()) {
					writeText(Html.intoTD(Html.nbsp(1)));
				} else {
					writeText(Html.intoTD(txt, style2));
				}
				b = true;
				col = 0;
			} else {
				int ncol = strands.indexOf(episode.getStrand());
				for (int j = col; j < ncol; j++) {
					writeText(Html.intoTD(Html.nbsp(1)));
					col++;
				}
				String txt = episode.getNotes();
				if (txt.isEmpty()) {
					writeText(Html.intoTD(Html.nbsp(1)));
				} else {
					writeText(Html.intoTD(txt, style2));
				}
				col++;
			}
		}
		if (b) {
			while (col < nbStrands) {
				writeText(Html.intoTD(Html.nbsp(1)));
				col++;
			}
			writeText(Html.TR_E);
		}
	}

	private boolean writeXmlFile(String fileName) {
		boolean rc = false;
		if (!openFile(fileName, true)) {
			return (false);
		}
		StringBuilder b = new StringBuilder();
		b.append("<episodes>");
		// first export all Strands
		for (Strand strand : strands) {
			b.append(strand.toXml());
		}
		// second export Episodes
		for (int i = 0; i < episodes.size(); i++) {
			Episode episode = episodes.get(i);
			b.append(episode.toXml());
		}
		b.append("</episodes>");
		writeText(XmlUtil.beautify(b.toString()));
		return rc;
	}

	private boolean writeTxtFile(String fileName) {
		if (!openFile(fileName, true)) {
			return (false);
		}
		boolean rc = false;
		for (int i = 0; i < episodes.size(); i++) {
			Episode episode = episodes.get(i);
			if (param.isTxt()) {
				writeText(episode.toCsv("", "", param.getTxtSeparator()));
			} else {
				writeText(episode.toCsv(param.getCsvQuote(),
				   param.getCsvComma(), param.getTxtSeparator()));
			}
			rc = true;
		}
		return rc;
	}

}
