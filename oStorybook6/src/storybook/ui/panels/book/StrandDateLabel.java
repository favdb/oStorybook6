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
package storybook.ui.panels.book;

import java.util.Date;
import storybook.db.strand.Strand;
import storybook.tools.html.Html;
import storybook.ui.interfaces.IRefreshable;

@SuppressWarnings("serial")
public class StrandDateLabel extends DateLabel implements IRefreshable {

	private Strand strand;

	public StrandDateLabel(Strand strand, Date date, boolean seconds) {
		super(date);
		this.strand = strand;
		refresh();
	}

	@Override
	public final void refresh() {
		String text = getDateText();
		setText(text);
		setToolTipText(Html.intoHTML(text + Html.BR + strand.toString()));
	}

	public Strand getStrand() {
		return strand;
	}

	public void setStrand(Strand strand) {
		this.strand = strand;
	}
}
