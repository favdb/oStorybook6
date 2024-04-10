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
package storybook.exim.importer;

import i18n.I18N;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import storybook.App;
import storybook.Const;
import storybook.db.book.Book;
import storybook.db.part.Part;
import storybook.exim.doc.EPUB;
import storybook.project.Project;
import storybook.tools.LOG;
import storybook.tools.StringUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.zip.ZipUtil;

/**
 *
 * @author favdb
 */
public class ImportEpub {

	private static final String TT = "ImportEpub";

	/**
	 * static for import a EPUB file
	 *
	 * @param file
	 */
	public static void doImport(File file) {
		ImportEpub imp = new ImportEpub(file);
		imp.exec();
	}

	private final String epubDirName;
	private Project dbFile;
	private final File file;
	private final File epubDir;
	private EPUB epub;
	private String destDir;

	public ImportEpub(File file) {
		this.file = file;
		this.epubDirName = file.getParent() + File.separator + ".tmpepub";
		epubDir = new File(epubDirName);
	}

	/**
	 * open the DB file to import
	 *
	 * @return
	 */
	public boolean openDB() {
		//LOG.printInfos(TT + ".importDB()");
		dbFile = null;
		String thetitle = epub.getTitle();
		if (thetitle == null) {
			return false;
		}
		if (thetitle.isEmpty()) {
			LOG.trace("properties title empty, file import ignored");
			return false;
		}
		String filename = destDir + File.separator + StringUtil.escapeTxt(thetitle);
		if (filename.isEmpty()) {
			return false;
		}
		if (filename.endsWith(Const.STORYBOOK.FILE_EXT_OSBK.toString())) {
			filename = filename.replace(Const.STORYBOOK.FILE_EXT_OSBK.toString(), "");
		}
		File fOsbk = new File(filename + Const.STORYBOOK.FILE_EXT_OSBK.toString());
		if (fOsbk.exists()) {
			int ret = JOptionPane.showConfirmDialog(null,
			   I18N.getMsg("file.exists", filename),
			   I18N.getMsg("file.save.overwrite.title"),
			   JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.NO_OPTION) {
				return false;
			}
			IOUtil.fileDelete(fOsbk);
		}
		dbFile = new Project(fOsbk);
		dbFile.initDefaultEntities();
		dbFile.parts.add(new Part(1, "I"));
		return dbFile.getFile() != null;
	}

	/**
	 * exec import
	 *
	 * @return
	 */
	public boolean exec() {
		LOG.trace(TT + ".exec()");
		if (EPUB.check(file)) {
			epub = new EPUB(file);
			if (epub.open()) {
				File dest = IOUtil.directorySelect(null, "");
				if (dest == null) {
					return false;
				}
				destDir = dest.getAbsolutePath();
				String fileName = file.getAbsolutePath();
				//initialize the DBfile
				if (!openDB()) {
					return false;
				}
				//create new Images directory
				String dirImages = IOUtil.dirCreate(dbFile.getPath(), "Images");
				//create new Documents directory
				IOUtil.dirCreate(dbFile.getPath(), "Documents");
				String workDir = System.getProperty("java.io.tmpdir")
				   + File.separator + "epub" + Long.toString(System.nanoTime());
				try {
					//unzip the complete EPUB file
					ZipUtil.deflate(file.getAbsolutePath(), workDir);
				} catch (IOException ex) {
					LOG.err("unzip error", ex);
					return false;
				}
				//set the Book informations
				Book book = dbFile.book;
				book.setTitle(epub.getTitle());
				book.setAuthor(epub.getAuthor());
				book.setUUID(epub.getUUID());
				book.setISBN(epub.getISBN());
				book.setBlurb(epub.getDescription());
				// import each contened text files as chapters
				for (String s : epub.getTextFiles()) {
					epub.readChapter(dbFile, workDir, s);
				}
				// copy the images
				if (!epub.getImgFiles().isEmpty()) {
					for (String s : epub.getImgFiles()) {
						File infile = new File(workDir + File.separator + s);
						File outfile = new File(dirImages + File.separator + infile.getName());
						IOUtil.fileCopy(infile, outfile);
					}
				}
				File cover = new File(dirImages + File.separator + "cover.jpeg");
				if (cover.exists()) {
					book.param.getParamExport().setEpubCover(true);
					book.param.getParamExport().setEpubCoverNoText(true);
				}
				dbFile.save();
				if (!App.isDev()) {
					IOUtil.dirDelete(new File(workDir));
				}
				return true;
			}
		}
		return false;
	}

}
