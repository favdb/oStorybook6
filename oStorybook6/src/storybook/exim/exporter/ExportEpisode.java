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
		//LOG.printInfos(TT + ".toFile(mainFrame)");
		ExportEpisode export = new ExportEpisode(mainFrame, format);
		return export.writeFile();
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
			return (false);
		}
		writeText(Html.intoH(1, title));
		writeText(Html.P_CENTER
		   + Html.intoI("("
			  + DateUtil.simpleDateTimeToString(new Date(), true)
			  + ")")
		   + Html.P_E);
		// table start
		writeText("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
		// heading columns
		StringBuilder b = new StringBuilder();
		b.append(Html.TR_B);
		b.append(Html.intoTD(I18N.getMsg("number")));
		b.append(Html.intoTD(I18N.getMsg("name")));
		b.append(Html.intoTD(I18N.getMsg("chapters") + "/" + I18N.getMsg("scenes")));
		b.append(Html.intoTD(I18N.getMsg("plot")));
		for (Strand strand : strands) {
			b.append(Html.intoTD(strand.getName()));
		}
		b.append(Html.TR_E);
		writeText(b.toString());
		// table content
		writeHtmlEpisodes();
		// table end
		writeText(Html.TABLE_E);

		closeFile(SILENT);
		JOptionPane.showMessageDialog(mainFrame,
		   I18N.getMsg("export.success", param.getFileName()),
		   I18N.getMsg("export"), JOptionPane.INFORMATION_MESSAGE);
		return true;
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
