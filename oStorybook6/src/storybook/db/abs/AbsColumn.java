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
package storybook.db.abs;

import i18n.I18N;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import storybook.db.DB.DATA;
import storybook.db.attribute.AttributeTCR;
import storybook.db.category.CategoryTCR;
import storybook.db.event.EventTCR;
import storybook.db.gender.GenderTCR;
import storybook.db.status.StatusTCR;
import storybook.renderer.BooleanTCR;
import storybook.renderer.ColorTCR;
import storybook.renderer.DateOnlyTCE;
import storybook.renderer.DateOnlyTCR;
import storybook.renderer.DateTCE;
import storybook.renderer.DateTCR;
import storybook.renderer.EntityTCR;
import storybook.renderer.HtmlTCR;
import storybook.renderer.IconTCR;
import storybook.renderer.IdTCR;
import storybook.renderer.NameTCR;
import storybook.renderer.NumericTCE;
import storybook.renderer.NumericTCR;
import storybook.tools.comparator.DateComparator;
import storybook.tools.comparator.EntityComparator;
import storybook.tools.comparator.NullComparator;
import storybook.tools.comparator.ObjectComparator;
import storybook.ui.frames.main.MainFrame;

/**
 * standard definition of a column used as a base class for each table
 *
 * @author favdb
 */
public class AbsColumn {

	public static final String AL_CENTER = "center",
			TCR_NAME = "NameTCR",
			TCR_HIDE = "hide",
			//SORTABLE = "sort",
			TCR_ATTRIBUTES = "AttributesTCR",
			TCR_BOOLEAN = "BooleanTCR",
			TCR_CATEGORY = "CategoryTCR",
			TCR_ENTITY = "EntityTCR",
			TCR_ENTITIES = "EntitiesTCR",
			TCR_EVENT = "EventTCR",
			TCR_GENDER = "GenderTCR",
			TCR_HTML = "HtmlTCR",
			TCR_ID = "IdTCR",
			TCR_STATUS = "StatusTCR",
			NUMERIC_EDITABLE = "NumericEditable",
			NUM_LONG = "Long",
			NUMERIC_RENDERER = "NumericRenderer",
			NUMERIC = "Numeric",
			TCR_DATE = "Date",
			TCR_DATE_EDITABLE = "DateEditable",
			TCR_DAY = "Day",
			TCR_DAY_EDITABLE = "DayEditable",
			TCR_ICON = "Icon",
			TCR_COLOR = "Color",
			NO_SORT = "noSort";

	private final int id;
	private String name;
	private Integer size = 0;// current size
	private Integer order = 0;// current order
	private boolean hideOnStart = false;
	private TableCellEditor tableCellEditor = null;
	private TableCellRenderer tableCellRenderer = null;
	private Comparator<?> comparator = null;
	private int maxLength = -1;
	private boolean showDateTime = false;
	private boolean defaultSort = false;
	private int align = SwingConstants.LEFT;
	private boolean sortable = true;
	private MainFrame mainFrame;
	private String code;

	public AbsColumn(MainFrame mainFrame, List<AbsColumn> cols, DATA data) {
		this.mainFrame = mainFrame;
		this.id = cols.size();
		this.name = data.i18n();
		this.code = data.name();
	}

	public AbsColumn(MainFrame mainFrame, List<AbsColumn> cols, String data, String... opt) {
		this.mainFrame = mainFrame;
		this.id = cols.size();
		this.name = data;
		setOpt(opt);
	}

	public AbsColumn(MainFrame mainFrame, List<AbsColumn> cols, DATA data, String... opt) {
		this(mainFrame, cols, data);
		setOpt(opt);
	}

	private void setOpt(String... opt) {
		if (opt != null && opt.length > 0) {
			for (String s : opt) {
				switch (s) {
					case AL_CENTER:
						setAlign(SwingConstants.CENTER);
						break;
					case TCR_NAME://name renderer with potential format
						setTCR(new NameTCR());
						break;
					case TCR_HIDE:
						setHideOnStart(true);
						break;
					case NUMERIC://numeric data without renderer and editor
						setComparator(new ObjectComparator());
						break;
					case NUMERIC_EDITABLE://numeric data with editor
						setTCE(new NumericTCE());
						setComparator(new ObjectComparator());
						break;
					case NUMERIC_RENDERER://numeric data with renderer
						setTCR(new NumericTCR());
						setComparator(new ObjectComparator());
						break;
					case NUM_LONG://Long numeric data, default renderer is simple Long.toString()
						setComparator(new ObjectComparator());
						break;
					case TCR_DATE:
						setTCR(new DateTCR());
						setComparator(new DateComparator());
						break;
					case TCR_DATE_EDITABLE:
						setTCR(new DateTCR());
						setTCE(new DateTCE());
						setComparator(new DateComparator());
						break;
					case TCR_DAY:
						setTCR(new DateOnlyTCR());
						setComparator(new DateComparator());
						break;
					case TCR_DAY_EDITABLE:
						setTCR(new DateOnlyTCR());
						setTCE(new DateOnlyTCE());
						setComparator(new DateComparator());
						break;
					case TCR_ICON:
						setAlign(SwingConstants.CENTER);
						setTCR(new IconTCR());
						break;
					case TCR_COLOR:
						setTCR(new ColorTCR());
						break;
					case TCR_ATTRIBUTES:
						setTCR(new AttributeTCR());
						setComparator(new NullComparator());
						break;
					case TCR_BOOLEAN:
						setTCR(new BooleanTCR());
						break;
					case TCR_CATEGORY:
						setTCR(new CategoryTCR());
						setComparator(new EntityComparator());
						break;
					case TCR_ENTITY:
						setTCR(new EntityTCR(mainFrame));
						setComparator(new EntityComparator());
						break;
					case TCR_ENTITIES:
						setTCR(new EntityTCR(mainFrame));
						setComparator(new NullComparator());
						sortable = false;
						break;
					case TCR_EVENT:
						setTCR(new EventTCR());
						setComparator(new EntityComparator());
						break;
					case TCR_GENDER:
						setTCR(new GenderTCR());
						setComparator(new EntityComparator());
						break;
					case TCR_HTML:
						setTCR(new HtmlTCR());
						break;
					case TCR_ID:
						setTCR(new IdTCR());
						break;
					case TCR_STATUS:
						setTCR(new StatusTCR());
						break;
					case NO_SORT:
						this.sortable = false;
						break;
					default:
						break;
				}
			}
		}
	}

	public void setMsgKey(String m) {
		this.name = m;
	}

	@Override
	public String toString() {
		if (name != null && !name.isEmpty()) {
			return I18N.getMsg(name);
		} else {
			return "";
		}
	}

	public int getColId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isHideOnStart() {
		return hideOnStart;
	}

	public void setHideOnStart(boolean hideOnStart) {
		this.hideOnStart = hideOnStart;
	}

	public boolean hasTCR() {
		return tableCellRenderer != null;
	}

	public TableCellRenderer getTCR() {
		return tableCellRenderer;
	}

	public void setTCR(TableCellRenderer renderer) {
		this.tableCellRenderer = renderer;
	}

	public boolean hasTCE() {
		return tableCellEditor != null;
	}

	public TableCellEditor getTCE() {
		return tableCellEditor;
	}

	public void setTCE(TableCellEditor editor) {
		this.tableCellEditor = editor;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public boolean hasMaxLength() {
		return maxLength > 0;
	}

	public void setShowDateTime(boolean showDateTime) {
		this.showDateTime = showDateTime;
	}

	public boolean hasDateTime() {
		return showDateTime;
	}

	/*public boolean isDefaultSort() {
		return defaultSort;
	}

	public void setDefaultSort(boolean defaultSort) {
		this.defaultSort = defaultSort;
	}*/
	public boolean hasComparator() {
		return comparator != null;
	}

	public Comparator<?> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<?> comparator) {
		this.comparator = comparator;
	}

	public int getAlign() {
		return align;
	}

	public void setAlign(int i) {
		align = i;
	}

	public boolean isSortable() {
		return this.sortable;
	}
}
