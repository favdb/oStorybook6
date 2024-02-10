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
package storybook.db.location;

import java.beans.PropertyChangeEvent;
import java.util.List;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class LocationTable extends AbsTable {

	public LocationTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.LOCATION);
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	public void initUi() {
		super.initUi();
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		//LOG.printInfos("LocationTable...changeLocal(evt="+evt.toString()+")");
		ActKey act = new ActKey(evt);
		if (act.isUpdate()
		   && (act.getType() == Book.TYPE.LOCATION)) {
			fillTable();
		}
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Location) mainFrame.project.get(Book.TYPE.LOCATION, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// if location refresh sub
		if (Book.getTYPE(entity) == Book.TYPE.LOCATION) {
			fillTable();
		}
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATION_ADDRESS, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATION_CITY, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATION_COUNTRY, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATION_SITE, TCR_ENTITY));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATION_ALTITUDE, NUMERIC, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATION_GPS, TCR_HIDE));

		this.getColumnsEnd(cols, entity);

		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Location e = (Location) entity;
		cols.add(e.getAddress());
		cols.add(e.getCity());
		cols.add(e.getCountry());
		cols.add(e.getSite());
		cols.add(e.getAltitude());
		cols.add(e.getGps());
		getRowEnd(cols, entity);
		return (cols);
	}

}
