/*
 * Copyright (C) 2018 FaVdB
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
package storybook.tools.file;

import i18n.I18N;
import java.io.File;
import javax.swing.JOptionPane;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import storybook.db.abs.AbstractEntity;
import storybook.db.scene.Scene;
import storybook.tools.xml.Xml;
import storybook.ui.MainFrame;

/**
 * temp file for a Scene
 *
 * @author FaVdB
 */
public class TempUtil {

	/**
	 * get the temp File for the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	private static File getFile(MainFrame mainFrame, AbstractEntity entity) {
		String file = entity.getObjType().toString() + "_" + entity.getId() + ".tmp";
		return new File(mainFrame.getProject().getPath() + File.separator + file);
	}

	/**
	 * restore a scene temp file if exists
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	public static AbstractEntity restore(MainFrame mainFrame, AbstractEntity entity) {
		if (entity instanceof Scene) {
			Scene scene = (Scene) entity;
			if (scene.getId() == -1L) {
				return entity;
			}
			Long id = scene.getId();
			File file = getFile(mainFrame, entity);
			if (file.exists()) {
				//check if content changed
				String saved = IOUtil.fileReadAsString(file.getAbsolutePath());
				if (!saved.equals(scene.toXml())) {
					int rc = JOptionPane.showConfirmDialog(null,
					   I18N.getMsg("file.backup_askrestore"),
					   I18N.getMsg("file.backup_rest"),
					   JOptionPane.YES_NO_OPTION);
					if (rc == JOptionPane.YES_OPTION) {
						Element rootNode = Xml.getRootNode(Xml.intoXml(saved));
						if (rootNode == null) {
							return scene;
						}
						NodeList nodes = rootNode.getElementsByTagName("scene");
						Element n = (Element) nodes.item(0);
						if (n != null) {
							scene = Scene.fromXml(n);
							scene.setId(id);
						}
					}
				}
			}
			write(mainFrame, entity);
			return scene;
		}
		return entity;
	}

	/**
	 * remove a temp file
	 *
	 * @param mainFrame
	 * @param entity
	 * @param force
	 */
	public static void remove(MainFrame mainFrame, AbstractEntity entity, boolean... force) {
		if (entity instanceof Scene) {
			File f = getFile(mainFrame, entity);
			if (f.exists() && force != null && force[0]) {
				f.delete();
			}
		}
	}

	/**
	 * write a temp file for the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	public static AbstractEntity write(MainFrame mainFrame, AbstractEntity entity) {
		if (entity instanceof Scene) {
			Scene scene = (Scene) entity;
			IOUtil.fileWriteString(getFile(mainFrame, entity), scene.toXml());
		}
		return entity;
	}

}
