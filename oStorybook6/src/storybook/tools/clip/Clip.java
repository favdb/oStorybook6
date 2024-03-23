/*
 * Copyright (C) 2023 favdb
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
package storybook.tools.clip;

import i18n.I18N;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * clipboard class
 *
 * @author favdb
 */
public class Clip {

	private Clip() {
		//empty
	}

	public static void okMessage(Component comp, String title) {
		JOptionPane.showMessageDialog(comp,
		   I18N.getMsg("copy.to_clip_ok"),
		   I18N.getMsg(title),
		   JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * copy data to clipboard for the given flavor
	 *
	 * @param data
	 * @param flavor
	 */
	public static void to(String data, String flavor) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		switch (flavor) {
			case "html":
				cb.setContents(new ClipHtml(data), null);
				break;
			case "xml":
				cb.setContents(new ClipXml(data), null);
				break;
			default:
				cb.setContents(new StringSelection(data), null);
				break;
		}
	}

	/**
	 * copy html and alternative txt to clipboard for the given HTML flavor
	 *
	 * @param html
	 * @param txt
	 * @param flavor
	 */
	public static void to(String html, String txt, String flavor) {
		if (!flavor.equals("html")) {
			return;
		}
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(new ClipHtml(html, txt), null);
	}

	public static class SbTransferable implements Transferable {

		private Object datas[] = null;
		private DataFlavor flavors[] = {new DataFlavor("text/plain", "Plain Flavor")};

		//Transferable class constructor
		public SbTransferable(String data, String inflavors[]) {
			//Set the data passed in to the local variable
			datas = new String[]{data};
			//Set the flavors passes in to the local variable
			List<DataFlavor> flvs = new ArrayList<>();
			for (String f : inflavors) {
				switch (f) {
					case "html":
						flavors[0] = (new DataFlavor("text/html", "Html Flavor"));
						break;
					case "xml":
						flavors[0] = (new DataFlavor("text/xml", "Xml Flavor"));
						break;
					default:
						flavors[0] = (new DataFlavor("text/plain", "Plain Flavor"));
						break;
				}
			}
		}

		@Override
		public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
			if (df.getMimeType().contains("text/html")
			   || df.getMimeType().contains("text/xml")
			   || df.getMimeType().contains("text/plain")) {
				return datas[0];
			} else {
				throw new UnsupportedFlavorException(df);
			}
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor df) {
			return df.getMimeType().contains("text/html")
			   || df.getMimeType().contains("text/xml")
			   || df.getMimeType().contains("text/plain");
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

	}

}
