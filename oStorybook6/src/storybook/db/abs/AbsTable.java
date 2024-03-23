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

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import assistant.Assistant;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import resources.icons.ICONS;
import resources.icons.ICONS.K;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import static storybook.ctrl.Ctrl.PROPS.UPDATE;
import storybook.db.DB;
import storybook.db.DB.DATA;
import storybook.db.EntityUtil;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.scene.ScenesChange;
import storybook.dialog.ConfirmDeleteDlg;
import storybook.exim.EXIM;
import storybook.shortcut.Shortcuts;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.TextUtil;
import storybook.tools.clip.Clip;
import storybook.tools.file.XEditorFile;
import storybook.tools.html.Html;
import storybook.tools.print.TablePrinter;
import storybook.tools.swing.SwingUtil;
import static storybook.tools.swing.SwingUtil.showModalDialog;
import storybook.tools.swing.js.JSColumnControlIcon;
import storybook.tools.swing.js.JSTable;
import storybook.tools.swing.js.JSToolBar;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.SbView.VIEWNAME;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;

/**
 * abstract tabe class contains an optional header, a JTable, an optional footer
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public abstract class AbsTable extends AbstractPanel implements
   ActionListener, FocusListener, ListSelectionListener, TableModelListener {

	public static final String TT = "AbsTable.",
	   TABLE_HEADER = "Table",
	   BT_EDIT = "BtEdit",
	   BT_NEW = "BtNew",
	   BT_DUPLICATE = "BtCopy",
	   BT_DELETE = "BtDelete";
	private final Book.TYPE type;
	protected List<AbsColumn> columns;
	private Part currentPart;
	protected JSTable table;
	protected DefaultTableModel tableModel;
	protected boolean allowMultiDelete = true;
	public JButton btNew,
	   btDelete,
	   btEdit,
	   btCopy,
	   btExternal;
	private int currentRow;
	private JButton btCtrlColumn;
	public JComboBox cbCategories;
	private AbstractEntity currentEntity;
	@SuppressWarnings("NonPublicExported")
	public ObjectifPanel objectifPanel;
	private JSToolBar footer;

	/**
	 * class definition
	 *
	 * @param mainFrame
	 * @param type
	 */
	protected AbsTable(MainFrame mainFrame, Book.TYPE type) {
		super(mainFrame);
		this.type = type;
		this.setName(type.toString());
		ctrl = mainFrame.getBookController();
		currentPart = null;
		initColumns();
	}

	/**
	 * get the entity type
	 *
	 * @return
	 */
	public Book.TYPE getType() {
		return type;
	}

	/**
	 * initialize Columns
	 *
	 */
	private void initColumns() {
		AbstractEntity entity = BookUtil.getNewEntity(mainFrame, type);
		columns = getColumns(entity);
	}

	/**
	 * get entity columnns
	 *
	 * @param e
	 * @return
	 */
	public List<AbsColumn> getColumns(AbstractEntity e) {
		List<AbsColumn> cols = new ArrayList<>();
		cols.add(new AbsColumn(mainFrame, cols,
		   DATA.ID, NUM_LONG, TCR_HIDE, AL_CENTER));
		if (!(e instanceof Endnote)) {
			cols.add(new AbsColumn(mainFrame, cols,
			   DATA.NAME, TCR_NAME));
		}
		return cols;
	}

	/**
	 * get common entity columns
	 *
	 * @param columns
	 * @param entity
	 */
	public void getColumnsEnd(List<AbsColumn> columns, AbstractEntity entity) {
		String common = "000";
		if (entity != null) {
			if (entity.getCommon() != null) {
				common = entity.getCommon();
			}
			if (common.length() < 3) {
				common = common + "000";
			}
		}
		if (common.charAt(0) == '1') {
			columns.add(new AbsColumn(mainFrame, columns,
			   DB.DATA.DESCRIPTION));
		}
		if (common.charAt(1) == '1') {
			columns.add(new AbsColumn(mainFrame, columns,
			   DB.DATA.NOTES));
		}
		if (common.charAt(2) == '1') {
			columns.add(new AbsColumn(mainFrame, columns,
			   DB.DATA.ASSISTANT));
		}
	}

	/**
	 * get a new empty entity
	 *
	 * @return
	 */
	protected AbstractEntity getNewEntity() {
		//LOG.printInfos(TT + "getNewEntity()");
		return (BookUtil.getNewEntity(mainFrame, type));
	}

	/**
	 * get the entity with an ID
	 *
	 * @param id
	 * @return
	 */
	protected abstract AbstractEntity getEntity(Long id);

	/**
	 * send an entity to edit from a row
	 *
	 * @param row
	 */
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		currentRow = row;
		AbstractEntity e = getEntityFromRow(row);
		boolean r = mainFrame.showEditorAsDialog(e);
		if (!r) {
			updateEntity(e);
		}
	}

	/**
	 * send an entity to edit from entity
	 *
	 * @param entity
	 */
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		//LOG.printInfos(TT + ".sendSetNewEntityToEdit(entity)");
		boolean r = mainFrame.showEditorAsDialog(entity);
		if (!r) {
			newEntity(entity);
		}
	}

	/**
	 * send an entity to delete from a row
	 *
	 * @param row
	 */
	protected synchronized void sendDeleteEntity(int row) {
		AbstractEntity e = getEntityFromRow(row);
		if (e != null) {
			deleteEntity(e);
			if (currentRow > table.getRowCount() - 1) {
				currentRow = 0;
			}
		}
	}

	/**
	 * send entities to delete from multiple rows
	 *
	 * @param entities
	 */
	protected synchronized void sendDeleteEntities(List<AbstractEntity> entities) {
		for (AbstractEntity e : entities) {
			mainFrame.getBookModel().ENTITY_Delete(e);
		}
		mainFrame.getBookModel().fireAgain();
	}

	/**
	 * *
	 * copy the given row for clipboard in bh and bt
	 *
	 * @param row
	 * @param bh
	 * @param bt
	 */
	protected synchronized void copyRowTo(int row, StringBuilder bh, StringBuilder bt) {
		//LOG.printInfos(TT + ".copyRowTo(row=" + row+", bh, bt, )");
		if (row < 0 || row > table.getRowCount() - 1) {
			return;
		}
		bh.append(Html.TR_B);
		List<Object> lso = getRow(getEntityFromRow(row));
		for (Object obj : lso) {
			if (obj != null) {
				bh.append(Html.intoTD(obj.toString().replace("◘", "")));
				bt.append(Html.htmlToCleanText(obj.toString())).append("\t");
			}
		}
		bh.append(Html.TR_E);
		bt.append("\n");
	}

	/**
	 * copy row to clipboard action
	 *
	 * @param row
	 */
	protected synchronized void copyEntityTo(int row) {
		//LOG.printInfos(TT + ".copyEntityTo(r=" + row + ")");
		if (row < 0 || row > table.getRowCount() - 1) {
			return;
		}
		AbstractEntity entity = getEntityFromRow(row);
		table.copyToClipboard(mainFrame, entity);
	}

	/**
	 * copy to clipboard for PhpBB, usable only for SceneTable
	 *
	 * @param row
	 */
	protected synchronized void copyToPhpBB(int row) {
		// empty usable only for SceneTable
	}

	/**
	 * property local change
	 *
	 * @param evt
	 */
	protected abstract void modelPropertyChangeLocal(PropertyChangeEvent evt);

	/**
	 * action to sort up this entity row
	 *
	 * @param row
	 */
	protected void sendOrderUpEntity(int row) {
	}

	/**
	 * action to sort down this entity row
	 *
	 * @param row
	 */
	protected void sendOrderDownEntity(int row) {
	}

	/**
	 * get the current Part
	 *
	 * @return
	 */
	public Part getCurrentPart() {
		if (cbPartFilter == null) {
			currentPart = null;
		} else if (cbPartFilter.getSelectedIndex() == 0) {
			currentPart = null;
		} else {
			currentPart = (Part) cbPartFilter.getSelectedItem();
		}
		return (currentPart);
	}

	/**
	 * get all entities
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractEntity> getAllEntities() {
		//LOG.printInfos(TT+".getAllEntities()"
		//		+" type="+type.name()
		//		+", currentPart="+(currentPart==null?"null":currentPart.toString()));
		return mainFrame.project.getList(type);
	}

	/**
	 * Property change action
	 *
	 * @param evt
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		ActKey act = new ActKey(evt);
		if (evt.getNewValue() instanceof View) {
			View newView = (View) evt.getNewValue();
			View oldView = (View) getParent().getParent();
			switch (Ctrl.getPROPS(propName)) {
				case UPDATE:
					modelPropertyChangeLocal(evt);
					return;
				case REFRESH:
					if (oldView == newView) {
						refreshAll();
					}
					return;
				case EXPORT:
					EXIM.exporter(mainFrame, (SbView) evt.getNewValue());
					return;
				case SHOWINFO:
					return;
				case PRINT:
					if (newView.getName().equalsIgnoreCase(table.getName().toLowerCase().replace("table", "") + "s")) {
						printAction();
						fillTable();
					}
					return;
				default:
			}
		}
		if (getTableName().toLowerCase().contains(act.type.toLowerCase())) {
			switch (Ctrl.getPROPS(act.getCmd())) {
				case INIT:
					initTableModel(evt);
					break;
				case NEW:
					newEntity(evt);
					break;
				case DELETE:
					deleteEntity(evt);
					break;
				case UPDATE:
					updateEntity(evt);
					break;
				case ORDERUP:
					orderUpEntity(evt);
					break;
				case ORDERDOWN:
					orderDownEntity(evt);
					break;
				default:
					break;
			}
			modelPropertyChangeLocal(evt);
		}
		revalidate();
	}

	/**
	 * initialize the user interface
	 *
	 */
	@Override
	public void initUi() {
		//LOG.printInfos(TT+".initUi() for table "+type.toString());
		removeAll();
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1, MIG.INS1, MIG.HIDEMODE3)));
		initToolbar();
		if (toolbar.getComponents().length != 0) {
			add(toolbar, MIG.get(MIG.GROWX));
		}
		initTable();
		initEditor();
		initRenderer();
		JScrollPane scroller = new JScrollPane(table);
		btCtrlColumn = new JButton(new JSColumnControlIcon());
		btCtrlColumn.addActionListener(e -> {
			table.changeVisibility(btCtrlColumn.getLocationOnScreen());
		});
		scroller.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, btCtrlColumn);
		scroller.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scroller, MIG.GROW);
		if (type == Book.TYPE.CHAPTER) {
			objectifPanel = new ObjectifPanel(this);
			add(objectifPanel, MIG.get(MIG.RIGHT));
		}
		table.getModel().addTableModelListener(this);
		add(initFooter(), MIG.get(MIG.SPAN, MIG.GROWX));
		SwingUtil.setEnable(footer, false);
		addFocusListener(this);
		if (type != Book.TYPE.ENDNOTE) {
			Ui.addShortcut(this, table, "copyEntityTo",
			   Shortcuts.getKeyAsString("osbk", "copy_full"),
			   copyEntityAction());
		}
	}

	/**
	 * initialize the toolbar
	 *
	 * @return
	 */
	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		return toolbar;
	}

	/**
	 * initialize the footer
	 *
	 * @return
	 */
	public JToolBar initFooter() {
		footer = new JSToolBar(false);
		footer.setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3)));
		// INSERT new Entity
		btNew = Ui.initButton(BT_NEW, "", K.NEW, "new", insertAction());
		table.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insert");
		table.getActionMap().put("insert", insertAction());
		footer.add(btNew);
		// edit current selected Entity
		btEdit = Ui.initButton(BT_EDIT, "", K.EDIT, "edit", this);
		btEdit.setEnabled(false);
		table.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");
		table.getActionMap().put("edit", editEntityAction());
		footer.add(btEdit);
		// duplicate the current Entity
		btCopy = Ui.initButton(BT_DUPLICATE, "", K.EDIT_COPY, "", duplicateAction());
		btCopy.setToolTipText(Shortcuts.getTooltips("osbk", "duplicate"));
		table.getInputMap(WHEN_FOCUSED).put(Shortcuts.getKeyStroke("osbk", "duplicate"), "edit");
		table.getActionMap().put("edit", editEntityAction());
		btCopy.setEnabled(false);
		footer.add(btCopy);
		// delete the current Entity
		btDelete = Ui.initButton(BT_DELETE, "", K.DELETE, "delete", deleteAction());
		btDelete.setEnabled(false);
		table.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		table.getActionMap().put("delete", deleteAction());
		footer.add(btDelete);

		return footer;
	}

	/**
	 * initialize the table object
	 */
	private void initTable() {
		List<String> colNames = getColumnNames();
		tableModel = new DefaultTableModel(colNames.toArray(), 0);
		table = new JSTable();
		table.setFont(App.getInstance().fonts.defGet());
		table.setModel(tableModel);
		table.setName(this.getClass().getSimpleName());
		table.addMouseListener(new TableMouse());
		table.getSelectionModel().addListSelectionListener(this);
		table.addFocusListener(this);
	}

	/**
	 * get the action to edit an Entity
	 *
	 * @return
	 */
	private AbstractAction editEntityAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				editEntityDo();
			}
		};
	}

	/**
	 * exec editing action
	 */
	private void editEntityDo() {
		int r = table.getSelectedRow();
		if (r != -1) {
			sendSetEntityToEdit(r);
		}
	}

	/**
	 * get the action to edit the current selected Entity.<br>
	 * if multi selection then use the first one.
	 *
	 * @return
	 */
	private AbstractAction copyEntityAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				copyEntityDo();
			}
		};
	}

	/**
	 * copy the current Entity to clipboard
	 */
	private void copyEntityDo() {
		//LOG.printInfos(TT + ".doCopyEntity()");
		int row = table.getSelectedRow();
		AbstractEntity entity = getEntityFromRow(row);
		table.copyToClipboard(mainFrame, entity);
	}

	/**
	 * get the action to copy current selected rows into clipboard
	 *
	 * @return
	 */
	private AbstractAction copyRowsAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				copyRowsDo();
			}
		};
	}

	/**
	 * copy current selected rows to clipboard
	 */
	protected synchronized void copyRowsDo() {
		//LOG.printInfos(TT + ".copyRowsTo()");
		StringBuilder bh = new StringBuilder();
		StringBuilder bt = new StringBuilder();
		bh.append(Html.HTML_B).
		   append(Html.BODY_B).
		   append(Html.TABLE_B);
		int[] rows = table.getSelectedRows();
		for (int row : rows) {
			copyRowTo(row, bh, bt);
		}
		bh.append(Html.TABLE_E)
		   .append(Html.BODY_E)
		   .append(Html.HTML_E);
		Clip.to(bh.toString(), bt.toString(), "html");
		Clip.okMessage(mainFrame, table.getName());
	}

	/**
	 * get action for inserting a new Entity
	 *
	 * @return
	 */
	private AbstractAction insertAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				insertDo();
			}
		};
	}

	/**
	 * insert a new Entity
	 */
	private void insertDo() {
		//table.clearSelection();
		sendSetNewEntityToEdit(BookUtil.getNewEntity(mainFrame, type));
	}

	/**
	 * get action to duplicate the current selected Entity
	 *
	 * @return
	 */
	private AbstractAction deleteAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				deleteDo();
			}
		};
	}

	/**
	 * delete the current selected Entity
	 */
	public void deleteDo() {
		AbstractEntity e;
		if (table.getSelectedRowCount() == 1) {
			// delete one entity
			e = (AbstractEntity) getEntityFromRow(table.getSelectedRow());
			if (e != null) {
				EntityUtil.delete(mainFrame, e);
			}
			return;
		}
		if (table.getSelectedRowCount() > 1) {
			// delete multiple entities
			List<AbstractEntity> entities = new ArrayList<>();
			int[] rows = table.getSelectedRows();
			for (int row2 : rows) {
				AbstractEntity entity = getEntityFromRow(row2);
				if (entity != null) {
					List<Long> readOnlyIds = EntityUtil.getReadOnlyIds(entity);
					if (!readOnlyIds.contains(entity.getId())) {
						entities.add(entity);
					}
				}
			}
			ConfirmDeleteDlg dlg = new ConfirmDeleteDlg(mainFrame, entities);
			showModalDialog(dlg, mainFrame, true);
			if (dlg.isCanceled()) {
				return;
			}
			sendDeleteEntities(entities);
		}
	}

	/**
	 * get action to duplicate the current selected Entity
	 *
	 * @return
	 */
	private AbstractAction duplicateAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				AbstractEntity e
				   = (AbstractEntity) getEntityFromRow(table.getSelectedRow());
				if (e != null) {
					EntityUtil.copyEntity(mainFrame, e);
					mainFrame.setUpdated();
				}
			}
		};
	}

	/**
	 * duplicate the current selected Entity
	 */
	private void duplicateDo() {
		if (table.getSelectedRowCount() > 0) {
			AbstractEntity e = (AbstractEntity) getEntityFromRow(table.getSelectedRow());
			if (e != null) {
				EntityUtil.copyEntity(mainFrame, e);
				mainFrame.setUpdated();
			}
		}
	}

	private void printAction() {
		LOG.trace(TT + "printAction()");
		TablePrinter.pr(mainFrame, this.getTable(), "", "");
		refresh();
	}

	/**
	 * initialize the cells editors
	 *
	 */
	private void initEditor() {
		//LOG.printInfos(TT + ".initEditor()");
		for (AbsColumn sbcol : columns) {
			TableColumn tcol;
			try {
				tcol = table.getColumn(sbcol.toString());
				if (sbcol.getTCE() != null) {
					tcol.setCellEditor(sbcol.getTCE());
					table.setColumnEditable(sbcol.toString());
				}
			} catch (IllegalArgumentException ex) {
				LOG.err(TT + ".initEditor() Error tcol", ex);
			}
		}
	}

	/**
	 * initialize the cells renderers and comparators
	 *
	 */
	private void initRenderer() {
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
		for (AbsColumn sbc : columns) {
			TableColumn tcol;
			try {
				tcol = table.getColumn(sbc.toString());
			} catch (IllegalArgumentException ex) {
				LOG.err(TT + "initUi() Error tcol");
				continue;
			}
			initCellRenderer(sbc, tcol);
			int mdidx = tcol.getModelIndex();
			if (sbc.hasComparator()) {
				sorter.setComparator(mdidx, sbc.getComparator());
			}
			if (!sbc.isSortable()) {
				sorter.setSortable(mdidx, false);
			}
			// visible on start
			tcol.setMaxWidth(800);
			table.setColumnVisible(sbc.toString(), !sbc.isHideOnStart());
		}
		table.setRowSorter(sorter);
	}

	/**
	 * initialize a cell renderer
	 *
	 * @param col
	 * @param tcol
	 */
	private void initCellRenderer(AbsColumn col, TableColumn tcol) {
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		if (col.getTCR() != null) {
			renderer = (DefaultTableCellRenderer) col.getTCR();
		}
		if (col.getAlign() == SwingConstants.CENTER) {
			renderer.setHorizontalAlignment(SwingConstants.CENTER);
		}
		tcol.setCellRenderer(renderer);
	}

	/**
	 * remove all data from the table
	 */
	public void clearTable() {
		// clear table
		for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
			tableModel.removeRow(i);
		}
	}

	/**
	 * fill data in the table
	 */
	public void fillTable() {
		//LOG.printInfos(TT+".fillTable() for table="+this.getName());
		if (table == null) {
			return;
		}
		table.getModel().removeTableModelListener(this);
		currentRow = table.getSelectionModel().getMinSelectionIndex();
		DefaultTableModel dm = (DefaultTableModel) table.getModel();
		int rowCount = dm.getRowCount();
		for (int i = rowCount - 1; i >= 0; i--) {
			dm.removeRow(i);
		}
		List<AbstractEntity> ls = getAllEntities();
		// fill in data
		rowCount = 0;
		for (AbstractEntity e : ls) {
			List<Object> cols = getRow(e);
			tableModel.addRow(cols.toArray());
			rowCount++;
		}
		if (type == Book.TYPE.CHAPTER && objectifPanel != null) {
			Integer nbTotalObjectif = 0, nbRealized = 0;
			for (AbstractEntity entity : ls) {
				nbTotalObjectif += ((Chapter) entity).getObjectiveChars();
				for (Scene s : mainFrame.project.scenes.findBy((Chapter) entity)) {
					nbRealized += s.getChars();
				}
			}
			if (nbTotalObjectif > 0) {
				objectifPanel.setTotal(nbTotalObjectif);
				objectifPanel.setRealized(nbRealized);
				objectifPanel.setVisible(true);
				this.revalidate();
			} else {
				objectifPanel.setVisible(false);
			}
		}
		if (currentRow != -1) {
			table.getSelectionModel().setSelectionInterval(currentRow, currentRow);
		}
		tableModel.addTableModelListener(this);
	}

	/**
	 * initialize table model
	 *
	 * @param evt
	 */
	protected void initTableModel(PropertyChangeEvent evt) {
		//LOG.printInfos(TT+".initTableModel("+evt.toString()+")");
		table.putClientProperty("MainFrame", mainFrame);
		fillTable();
	}

	/**
	 * get the table object
	 *
	 * @return the table
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * get the name of this table
	 *
	 * @return the table name
	 */
	public String getTableName() {
		return (this.getClass().getSimpleName());
	}

	/**
	 * get the column names
	 *
	 * @return the list of columns names
	 */
	public List<String> getColumnNames() {
		List<String> cols = new ArrayList<>();
		for (AbsColumn col : columns) {
			cols.add(col.toString());
		}
		return cols;
	}

	/**
	 * abstract for updating the row
	 *
	 * @param entity
	 */
	public abstract void updateRow(AbstractEntity entity);

	/**
	 * write the entity at the specified row
	 *
	 * @param entity
	 * @param row
	 */
	public void updateRow(AbstractEntity entity, int row) {
		List<Object> rowVector = getRow(entity);
		int col = 0;
		for (Object val : rowVector) {
			tableModel.setValueAt(val, row, col);
			++col;
		}
		tableModel.fireTableDataChanged();
	}

	/**
	 * get the row content as a list of objects
	 *
	 * @param entity
	 * @return
	 */
	public List<Object> getRow(AbstractEntity entity) {
		//LOG.printInfos(TT + ".getRow(" + AbstractEntity.printInfos(entity) + ") table=" + getName());
		List<Object> cols = new ArrayList<>();
		if (entity == null) {
			return (cols);
		}
		cols.add(entity.getId());
		if (!(entity instanceof Endnote)) {
			cols.add(entity.getName() + "◘" + entity.getAspect());
		}
		return cols;
	}

	/**
	 * get the common informations
	 *
	 * @param row
	 * @param entity
	 */
	public static void getRowEnd(List<Object> row, AbstractEntity entity) {
		String c = entity.getCommon();
		if (c.length() < 3) {
			c = c + "000";
		}
		if (c.charAt(0) == '1') {
			row.add(TextUtil.ellipsize(Html.htmlToText(entity.getDescription()), 64));
		}
		if (c.charAt(1) == '1') {
			row.add(TextUtil.ellipsize(Html.htmlToText(entity.getNotes()), 64));
		}
		if (c.charAt(2) == '1') {
			row.add(TextUtil.ellipsize(Assistant.toText(entity.getAssistant()), 64));
		}
	}

	/**
	 * write the entity from an event
	 *
	 * @param evt
	 */
	protected void updateEntity(PropertyChangeEvent evt) {
		//LOG.printInfos(TT+".updateEntity(evt="+evt.toString()+")");
		AbstractEntity entity = (AbstractEntity) evt.getNewValue();
		updateEntity(entity);
	}

	/**
	 * write an entity
	 *
	 * @param entity
	 */
	protected void updateEntity(AbstractEntity entity) {
		//LOG.printInfos(TT + ".updateEntity(entity=" + entity.toString() + ")");
		int row = table.getSelectedRow();
		fillTable();
		if (row >= 0 && row < table.getRowCount()) {
			table.selectRow(row);
		} else {
			table.selectRow(currentRow);
		}
	}

	/**
	 * sort this table in order up
	 *
	 * @param evt
	 */
	protected void orderUpEntity(PropertyChangeEvent evt) {
	}

	/**
	 * sort this table in order down
	 *
	 * @param evt
	 */
	protected void orderDownEntity(PropertyChangeEvent evt) {
	}

	/**
	 * sort this table by the given column index
	 *
	 * @param col
	 */
	protected void sortByColumn(int col) {
		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>) table.getRowSorter());
		ArrayList<SortKey> list = new ArrayList<>();
		list.add(new RowSorter.SortKey(col, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.sort();
	}

	/**
	 * new entity action from an event
	 *
	 * @param evt
	 */
	protected void newEntity(PropertyChangeEvent evt) {
		AbstractEntity entity = (AbstractEntity) evt.getNewValue();
		newEntity(entity);
	}

	/**
	 * add a new entity to the table and change the selected row
	 *
	 * @param entity
	 */
	protected void newEntity(AbstractEntity entity) {
		fillTable();
		for (int row = 0; row < tableModel.getRowCount(); ++row) {
			AbstractEntity e = getEntityFromRow(row);
			if (e == null) {
				return;
			}
			if (e.getId().equals(entity.getId())) {
				table.setRowSelectionInterval(row, row);
				table.scrollRectToVisible(table.getCellRect(row, 0, true));
			}
		}
	}

	/**
	 * delete entity action from an event
	 *
	 * @param evt
	 */
	protected synchronized void deleteEntity(PropertyChangeEvent evt) {
		AbstractEntity entity = (AbstractEntity) evt.getOldValue();
		deleteEntity(entity);
	}

	/**
	 * delete the given entity
	 *
	 * @param entity
	 */
	protected synchronized void deleteEntity(AbstractEntity entity) {
		//LOG.printInfos(TT + ".deleteEntity(entity=" + (entity == null ? "null" : entity.getName()) + ")");
		if (entity == null) {
			return;
		}
		for (int row = 0; row < tableModel.getRowCount(); ++row) {
			AbstractEntity e = getEntityFromRow(row);
			if (e != null && e.getId().equals(entity.getId())) {
				tableModel.removeRow(row);
				fillTable();
				break;
			}
		}
		tableModel.fireTableDataChanged();
	}

	/**
	 * get the entity from given row
	 *
	 * @param row
	 * @return null if no row selected
	 */
	protected synchronized AbstractEntity getEntityFromRow(int row) {
		//LOG.printInfos(TT+".getEntityFromRow(row=" + row + ")");
		if (row == -1 || row > table.getRowCount() - 1) {
			return null;
		}
		try {
			if (table.getRowSorter() == null) {
				return null;
			}
			int col = 0;
			int modelIndex = table.getRowSorter().convertRowIndexToModel(row);
			Object obj = tableModel.getValueAt(modelIndex, col);
			if (obj instanceof Long) {
				Long id = (Long) table.getModel().getValueAt(modelIndex, col);
				return getEntity(id);
			}
		} catch (Exception ex) {
			//LOG.err(TT + ".getEntityFromRow(" + row + ") Exception: " + ex.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * Performed action
	 *
	 * @param evt
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent evt) {
		//LOG.printInfos(TT + ".actionPerformed(" + evt.toString() + ") for table " + this.getName());
		Component comp = (Component) evt.getSource();
		if (comp instanceof JComboBox) {
			fillTable();
			return;
		}
		AbstractEntity e;
		/*if (comp instanceof JToggleButton) {
	    JToggleButton bt = (JToggleButton) evt.getSource();
	    if (bt.getName().equals("table.editable")) {
		table.setEditable(bt.isSelected());
	    }
	} else */
		if (comp instanceof JButton) {
			switch (comp.getName()) {
				case BT_EDIT:
					editEntityDo();
					break;
				case BT_NEW:
					insertDo();
					break;
				case BT_DUPLICATE:
					duplicateDo();
					break;
				case BT_DELETE:
					deleteDo();
					break;
				default:
					break;
			}
		}
	}

	/**
	 * action when focus is gained
	 *
	 * @param e
	 */
	@Override
	public void focusGained(FocusEvent e) {
		//SwingUtil.setEnable(footer, true);
		boolean b = table.getSelectedRow() != -1;
		btDelete.setEnabled(b);
		btCopy.setEnabled(b);
		btEdit.setEnabled(b);
		if (btExternal != null) {
			btExternal.setEnabled(b);
		}
	}

	/**
	 * action when focus is lost
	 *
	 * @param e
	 */
	@Override
	public void focusLost(FocusEvent e) {
		//SwingUtil.setEnable(footer, false);
	}

	/**
	 * Value changed action
	 *
	 * @param evt
	 */
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		//LOG.printInfos(TT + ".valueChanged(e=" + evt.toString() + ")");
		if (evt.getValueIsAdjusting()) {
			return;
		}
		DefaultListSelectionModel selectionModel = (DefaultListSelectionModel) evt.getSource();
		int count = selectionModel.getMaxSelectionIndex() - selectionModel.getMinSelectionIndex() + 1;
		if (count > 1) {
			btEdit.setEnabled(false);
			btCopy.setEnabled(false);
			btDelete.setEnabled(allowMultiDelete);
			return;
		}
		int row = selectionModel.getMinSelectionIndex();
		currentEntity = getEntityFromRow(row);
		boolean b = true;
		if (currentEntity == null) {
			b = false;
		}
		if (btEdit != null) {
			btEdit.setEnabled(b);
		}
		if (btCopy != null) {
			btCopy.setEnabled(b);
		}
		if (btDelete != null) {
			btDelete.setEnabled(b);
		}
		mainFrame.getBookController().infoSetTo(currentEntity);
		setEnableExternal(currentEntity);
	}

	/**
	 * enable the button to call external editor
	 *
	 * @param entity
	 */
	private void setEnableExternal(AbstractEntity entity) {
		if (entity instanceof Scene && btExternal != null) {
			Path path = Paths.get(((Scene) entity).getOdf());
			btExternal.setEnabled(Files.exists(path) && Files.isRegularFile(path));
		}
	}

	/**
	 * external editor action
	 *
	 * @param row
	 */
	public void sendSetExternal(int row) {
		//LOG.printInfos(TT + ".sendSetLibreOffice(" + row + ")");
		Scene scene = (Scene) getEntityFromRow(row);
		if (scene == null) {
			return;
		}
		String name = scene.getOdf();
		if (name == null || name.isEmpty()) {
			name = XEditorFile.getDefaultFilePath(mainFrame, scene.getName());
		}
		File file = new File(name);
		if (!file.exists()) {
			file = XEditorFile.createFile(mainFrame, scene);
			if (file == null) {
				return;
			}
			scene.setOdf(file.getAbsolutePath());
			ctrl.updateEntity(scene);
		}
		XEditorFile.launchExternal(file.getAbsolutePath());
	}

	/**
	 * set the current row
	 *
	 * @param entity
	 */
	public void setCurrentRow(AbstractEntity entity) {
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			String id = (String) tableModel.getValueAt(i, 0);
			if (id.equals(entity.getId().toString())) {
				currentRow = i;
				table.getSelectionModel().setSelectionInterval(currentRow, currentRow);
			}
		}
	}

	/**
	 * refresh the table
	 */
	@Override
	public void refresh() {
		saveTableDesign();
		table.getSelectionModel().removeListSelectionListener(this);
		super.refresh();
		table.getSelectionModel().removeListSelectionListener(this);
		loadTableDesign();
		if (currentRow > -1 && currentRow < table.getRowCount()) {
			table.setRowSelectionInterval(currentRow, currentRow);
		}
		table.getSelectionModel().addListSelectionListener(this);
	}

	/**
	 * refresh all data
	 */
	public void refreshAll() {
		saveTableDesign();
		table.getSelectionModel().removeListSelectionListener(this);
		this.removeAll();
		initUi();
		loadTableDesign();
		table.getSelectionModel().addListSelectionListener(this);
	}

	/**
	 * Table change event (simple change selected row)
	 *
	 * @param e
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		//LOG.printInfos(TT + ".tableChanged(e=" + e.toString() + ")" + " for table=" + table.getName());
		int row = e.getFirstRow();
		if (row < 0 || row != e.getLastRow()) {
			return;
		}
		AbstractEntity c = (AbstractEntity) this.getEntityFromRow(row);
		if (c != null) {
			mainFrame.getBookController().infoSetTo(c);
		}
	}

	/**
	 * add a JCombobox for categories
	 *
	 * @param categories
	 * @param selected
	 * @param empty
	 * @param all
	 */
	public void addCbCategories(List<String> categories,
	   AbstractEntity selected, boolean empty, boolean all) {
		toolbar.add(new JLabel(I18N.getColonMsg("categories")));
		cbCategories = Ui.initComboBox("cbCategories", "", categories, selected, empty, all, this);
		toolbar.add(cbCategories);
	}

	/**
	 * check if the given name is the current table name
	 *
	 * @param tablename
	 * @return
	 */
	public boolean isTable(String tablename) {
		return table.getName().equals(tablename);
	}

	/**
	 * change status and/or chapter for multiple scenes
	 */
	private void sceneChangeStatus(List<AbstractEntity> scenes) {
		if (scenes == null || scenes.isEmpty()) {
			return;
		}
		if (!ScenesChange.show(mainFrame, scenes) && mainFrame.getView(VIEWNAME.TREE).isLoaded()) {
			mainFrame.getBookController().refresh(mainFrame.getView(VIEWNAME.TREE));
		}
	}

	/**
	 * panel to show the objective
	 */
	private static class ObjectifPanel extends JPanel {

		private final AbsTable table;
		private JLabel lbTotal;
		private JLabel lbRealized;
		private Integer total = 0;

		public ObjectifPanel(AbsTable table) {
			this.table = table;
			initUi();
		}

		private void initUi() {
			setLayout(new MigLayout(MIG.INS0));
			lbTotal = new JLabel("0");
			lbTotal.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lbRealized = new JLabel("0%");
			lbRealized.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			add(lbTotal);
			add(lbRealized);
		}

		public void setTotal(Integer value) {
			lbTotal.setText(String.format("%,d", value));
			total = value;
		}

		public void setRealized(Integer value) {
			String str = "0%";
			if (total > 0) {
				float c = (value * 100), t = total;
				str = (String.format("%.2f%%", c / t));
				if (c / t >= 100) {
					lbRealized.setForeground(Color.GREEN);
				} else {
					lbRealized.setForeground(lbTotal.getForeground());
				}
			}
			lbRealized.setText(str);
		}
	}

	/**
	 * Mouse adapter class
	 */
	private class TableMouse implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			//LOG.printInfos(TT + ".TableMouse.mouseClicked(e=" + e.toString() + ")");
			if (e.isPopupTrigger() || e.getModifiers() == InputEvent.BUTTON3_MASK) {
				if (type == Book.TYPE.INTERNAL) {
					return;
				}
				DefaultListSelectionModel selectionModel = (DefaultListSelectionModel) table.getSelectionModel();
				int count = selectionModel.getMaxSelectionIndex() - selectionModel.getMinSelectionIndex() + 1;
				if (count < 2) {
					showPopup(e);
				} else {
					showPopupMulti(e);
				}
				e.consume();
				return;
			}
			DefaultListSelectionModel selectionModel = (DefaultListSelectionModel) table.getSelectionModel();
			int row = selectionModel.getMinSelectionIndex();
			int count = selectionModel.getMaxSelectionIndex() - selectionModel.getMinSelectionIndex() + 1;
			if (e.getClickCount() == 2) {
				if (count == 1) {
					sendSetEntityToEdit(row);
				}
			} else if (e.getClickCount() == 1 && count == 1) {
				AbstractEntity ent = getEntityFromRow(row);
				if (ent != null) {
					if (type == Book.TYPE.SCENE && btExternal != null) {
						setEnableExternal(ent);
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// empty
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// empty
		}

		/**
		 * show the Popup menu
		 *
		 * @param e
		 */
		private void showPopup(MouseEvent e) {
			if (!(e.getSource() instanceof JSTable)) {
				return;
			}
			JSTable source = (JSTable) e.getSource();
			int row = source.rowAtPoint(e.getPoint());
			int column = source.columnAtPoint(e.getPoint());
			if (!source.isRowSelected(row)) {
				source.changeSelection(row, column, false, false);
			}
			AbstractEntity entity = getEntityFromRow(row);
			if (entity != null) {
				JPopupMenu popup
				   = EntityUtil.createPopupMenu(mainFrame, entity, EntityUtil.WITH_CHRONO);
				if (popup == null) {
					LOG.err(TT + " getting popup return null");
					return;
				}
				if (entity instanceof Scene || entity instanceof Chapter) {
					popup.add(new JPopupMenu.Separator());
					JMenuItem it = new JMenuItem(I18N.getMsg("copy.to_phpbb"));
					it.setIcon(IconUtil.getIconSmall(ICONS.K.EDIT_COPY));
					it.addActionListener(evt -> copyToPhpBB(row));
					popup.add(it);
				}
				try {
					popup.show(e.getComponent(), e.getX(), e.getY());
				} catch (Exception ex) {

				}
			}
		}

		/**
		 * show Popup menu for multiline selection
		 *
		 * @param e
		 */
		private void showPopupMulti(MouseEvent e) {
			//LOG.printInfos(TT + ".showPopupMulti() for table " + table.getName());
			if (isTable("ChapterTable") || isTable("SceneTable")) {
				int min = table.getSelectedRow();
				int max = table.getSelectionModel().getMaxSelectionIndex();
				List<AbstractEntity> rows = new ArrayList<>();
				for (int i = min; i <= max; i++) {
					AbstractEntity ent = getEntityFromRow(i);
					if (table.isRowSelected(i) && ent != null) {
						rows.add(ent);
					}
				}
				JPopupMenu popup = new JPopupMenu();
				JMenuItem item;
				if (isTable("SceneTable")) {
					item = new JMenuItem(I18N.getMsg("scenes.change"));
					item.addActionListener(evt -> sceneChangeStatus(rows));
					popup.add(item);
				}
				item = new JMenuItem(I18N.getMsg("delete"));
				item.setIcon(IconUtil.getIconSmall(ICONS.K.DELETE));
				item.addActionListener(evt -> sendDeleteEntities(rows));
				popup.add(item);
				if (isTable("SceneTable") || isTable("ChapterTable")) {
					item = new JMenuItem(I18N.getMsg("join"));
					item.setIcon(IconUtil.getIconSmall(K.COGS));
					item.addActionListener(evt -> EntityUtil.joinEntities(mainFrame, rows));
					popup.add(item);
				}
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			//empty
		}

		@Override
		public void mouseExited(MouseEvent e) {
			//empty
		}

	}

	/**
	 * write table columns size and position
	 */
	public void saveTableDesign() {
		saveTableDesign(table);
	}

	public static void saveTableDesign(JSTable table) {
		//LOG.printInfos("SbViewFactory.saveTableDesign(mainFrame, table="+table.toString());
		String tableKey = TABLE_HEADER + "_" + table.getName();
		try {
			String cols = getTableDesign(table);
			App.preferences.setString(tableKey, cols);
		} catch (Exception e) {
			LOG.err(TT + ".saveTableDesign(table=" + table.getName() + ")", e);
		}
	}

	/**
	 * get TABCOLs for a given table as a String
	 *
	 * @return
	 */
	private static String getTableDesign(JSTable table) {
		//LOG.printInfos(TT + ".getTableDesign() for " + getName());
		List<String> list = new ArrayList<>();
		int n = 0;
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn col = table.getColumnModel().getColumn(i);
			if (col.getPreferredWidth() > 0) {
				String l1 = (String) col.getHeaderValue();
				int modelIdx = col.getModelIndex();
				if (modelIdx < 0) {
					continue;
				}
				int idx = table.convertColumnIndexToView(modelIdx);
				if (idx < 0) {
					continue;
				}
				list.add(String.format("%s,%d,%d", l1, col.getPreferredWidth(), n++));
			}
		}
		return ListUtil.join(list, "#");
	}

	/**
	 * load TABCOLs
	 */
	public void loadTableDesign() {
		//LOG.printInfos(TT + ".loadTableDesign() for " + getName());
		loadTableDesign(table, columns, tableModel);
	}

	/**
	 * load TABCOLs for a given table
	 *
	 * @param table
	 * @param columns
	 * @param tableModel
	 */
	public static void loadTableDesign(JSTable table,
	   List<AbsColumn> columns,
	   DefaultTableModel tableModel) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		String tableKey = TABLE_HEADER + "_" + table.getName();
		String z = App.preferences.getString(tableKey, "");
		if (z == null || z.isEmpty() || z.startsWith("(")) {
			return;
		}
		String[] xlist = z.split("#");
		for (String scol : xlist) {
			if (!scol.isEmpty()) {
				String[] x = scol.split(",");
				for (AbsColumn sbc : columns) {
					if (sbc.toString().equals(x[0])) {
						sbc.setSize(Integer.parseInt(x[1]));
						sbc.setOrder(Integer.parseInt(x[2]));
					}
				}
			}
		}
		// restore size and order
		int nbcols = table.getColumnCount();
		for (int i = 0; i < nbcols; i++) {
			table.setColumnVisible(tableModel.getColumnName(i), false);
		}
		for (AbsColumn c : columns) {
			TableColumn col = table.getColumn(c.toString());
			if (col != null && c.getSize() > 0) {
				table.setColumnVisible(c.toString(), true);
				col.setMinWidth(5);
				col.setPreferredWidth(c.getSize());
			}
		}
		List<AbsColumn> sbc = new ArrayList<>(columns);
		Collections.copy(sbc, columns);
		Collections.sort(sbc, (AbsColumn e1, AbsColumn e2) -> e1.getOrder().compareTo(e2.getOrder()));
		boolean idColumn = false;
		TableColumnModel model = table.getColumnModel();
		for (AbsColumn c : sbc) {
			if (c.getName().equals("ID")) {
				idColumn = true;
			}
			if (c.getSize() > 0) {
				for (int i = 0; i < nbcols; i++) {
					if (((String) model.getColumn(i).getHeaderValue()).equals(c.toString())
					   && c.getOrder() != i
					   && c.getOrder() < nbcols) {
						table.moveColumn(i, c.getOrder());
						break;
					}
				}
			}
		}
		if (!App.isDev() && idColumn) {
			table.setColumnVisible("ID", false);
		}
	}

	/**
	 * dump content of the given table columns (for dev only)
	 *
	 * @param table
	 */
	public static void dumpDesign(JSTable table) {
		String tableKey = TABLE_HEADER + "_" + table.getName();
		String z = App.preferences.getString(tableKey, "");
		LOG.trace("table " + table.getName() + "='" + z + "'");
	}

}
