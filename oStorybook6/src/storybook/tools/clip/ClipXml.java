/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools.clip;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import storybook.tools.html.Html;
import storybook.tools.xml.Xml;

/**
 * clipboard for XML
 *
 * @author FaVdB
 *
 */
public class ClipXml implements Transferable, ClipboardOwner {

	public static final DataFlavor xmlFlavor = new DataFlavor("text/xml", "XML"),
			htmlFlavor = new DataFlavor("text/html", "HTML"),
			plainFlavor = DataFlavor.stringFlavor;
	private final DataFlavor[] supportedFlavors = {
		xmlFlavor, htmlFlavor, plainFlavor
	};
	private final String xmlText, htmlText, plainText;

	public ClipXml(String xmlText) {
		this.xmlText = Xml.intoXml(xmlText);
		this.htmlText = Html.textToHTML(xmlText);
		this.plainText = Xml.intoXml(xmlText);
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// empty
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor f : supportedFlavors) {
			if (f.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(xmlFlavor)) {
			return new ByteArrayInputStream(xmlText.getBytes());
		}
		if (flavor.equals(htmlFlavor)) {
			return new ByteArrayInputStream(htmlText.getBytes());
		} else {
			return xmlText;
		}
	}
}
