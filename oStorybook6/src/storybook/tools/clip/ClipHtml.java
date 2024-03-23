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

/**
 * clip for HTML
 *
 * @author FaVdB
 *
 */
public class ClipHtml implements Transferable, ClipboardOwner {

	public static final DataFlavor htmlFlavor = new DataFlavor("text/html", "HTML"),
	   textFlavor = new DataFlavor("text/plain", "TEXT");
	private final DataFlavor[] supportedFlavors = {
		htmlFlavor//,
	//textFlavor
	};
	private final String htmlText, plainText;

	public ClipHtml(String htmlText, String plainText) {
		this.htmlText = htmlText;
		this.plainText = plainText;
	}

	public ClipHtml(String html) {
		this.htmlText = html;
		this.plainText = Html.htmlToText(html);
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
		if (flavor.equals(htmlFlavor)) {
			return new ByteArrayInputStream(htmlText.getBytes());
		}
		if (flavor.equals(textFlavor)) {
			return new ByteArrayInputStream(plainText.getBytes());
		} else {
			throw new UnsupportedFlavorException(htmlFlavor);
		}
	}
}
