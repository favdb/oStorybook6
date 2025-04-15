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
package storybook.ideabox;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.idea.Idea;
import storybook.dialog.MessageDlg;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSColumnControlIcon;
import storybook.tools.swing.js.JSTable;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class IdeaxFrm extends JFrame implements ListSelectionListener, TableModelListener {

	private static final String TT = "IdeaxFrm";

	@SuppressWarnings("NonPublicExported")
	public IdeaboxParam param;

	private static IdeaxFrm instance;
	private static boolean started = false;
	private JButton btAdd;
	private JButton btDelete;
	private JToolBar toolbar;
	public JSTable table;

	public static void setInstance(IdeaxFrm h) {
		instance = h;
	}

	/**
	 * get the current instance
	 *
	 * @return
	 */
	public static IdeaxFrm getInstance() {
		if (instance == null) {
			instance = new IdeaxFrm();
		}
		return instance;
	}
	private JButton btCtrlColumn;
	private DefaultTableModel tableModel;
	private List<AbsColumn> columns;
	public IdeaxXml ideaX;

	public IdeaxFrm() {
		super();
		this.param = new IdeaboxParam();
		initialize();
	}

	private void initialize() {
		started = true;
		instance = this;
		ideaX = new IdeaxXml();
		setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.WRAP1)));
		setTitle(I18N.getMsg("ideabox"));
		setIconImage(IconUtil.getIconImage("png/ideabox"));
		addWindowListener(new IdeaxFrmWindowAdaptor());
		setContentPane(initPanel());
		setPreferredSize(param.frmSize);
		pack();
		setLocation(param.frmLoc);
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
		table.requestFocus();
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FILLX)));
		initToolbar();
		panel.add(toolbar, MIG.get(MIG.SPAN, MIG.GROWX));
		initTable();
		initEditor();
		initRenderer();
		JScrollPane scroller = new JScrollPane(table);
		btCtrlColumn = new JButton(new JSColumnControlIcon());
		btCtrlColumn.addActionListener(e -> {
			table.changeVisibility(btCtrlColumn.getLocationOnScreen());
		});
		scroller.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, btCtrlColumn);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		SwingUtil.setMaxPreferredSize(scroller);
		panel.add(scroller, MIG.get(MIG.SPAN, MIG.GROW));
		table.setEditable(false);
		fillTable();
		table.pack();
		return panel;
	}

	private void initToolbar() {
		toolbar = new JToolBar();
		toolbar.setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.HIDEMODE3)));
		JPanel p = new JPanel(new MigLayout(MIG.INS0));
		btAdd = Ui.initButton("btAdd", "", ICONS.K.NEW, "idea.new", e -> ideaAdd());
		p.add(btAdd);
		btDelete = Ui.initButton("btDelete", "", ICONS.K.DELETE, "delete", e -> ideaDelete());
		p.add(btDelete);
		toolbar.add(p, MIG.GROWX);
		JButton btExit = Ui.initButton("btExit", "", ICONS.K.EXIT, "exit", e -> close());
		toolbar.add(btExit, MIG.RIGHT);
	}

	private void initTable() {
		columns = getColumns(new Idea());
		List<String> colNames = getColumnNames();
		tableModel = new DefaultTableModel(colNames.toArray(), 0);
		table = new JSTable();
		table.setFont(App.fonts.defGet());
		table.setModel(tableModel);
		table.setName("ideabox");
		table.addMouseListener(new IdeaxMouseAdapter());
		table.getSelectionModel().addListSelectionListener(this);
		AbsTable.tableDesignLoad(table, columns, tableModel);
		table.addKeyListener(new IdeaxFrmKeyListener());
		AbstractAction actEnter = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				int r = table.getSelectedRow();
				if (r != -1) {
					ideaUpdate();
				}
			}
		};
		table.getActionMap().put("Enter", actEnter);
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
	 * initialize the cells renderers and comparators
	 *
	 */
	private void initRenderer() {
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		for (AbsColumn sbcol : columns) {
			TableColumn tcol;
			try {
				tcol = table.getColumn(sbcol.toString());
			} catch (IllegalArgumentException ex) {
				LOG.err(TT + ".initRenderer() error tcol");
				continue;
			}
			initCellRenderer(sbcol, tcol);
			if (sbcol.hasComparator()) {
				sorter.setComparator(tcol.getModelIndex(), sbcol.getComparator());
			}
			if (!sbcol.isSortable()) {
				sorter.setSortable(tcol.getModelIndex(), false);
			}
			// visible on start
			tcol.setMaxWidth(800);
			table.setColumnVisible(sbcol.toString(), !sbcol.isHideOnStart());
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
	 * initialize the cells editors
	 *
	 */
	private void initEditor() {
		//LOG.trace(TT + ".initEditor()");
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
	 * load the table design
	 *
	 */
	public void loadTableDesign() {
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
	}

	public List<AbsColumn> getColumns(Idea entity) {
		List<AbsColumn> cols = new ArrayList<>();

		cols.add(new AbsColumn(null, cols, DB.DATA.DATE_CREATION, TCR_HIDE));
		cols.add(new AbsColumn(null, cols, DB.DATA.DATE_MAJ));
		cols.add(new AbsColumn(null, cols, DB.DATA.UUID, TCR_HIDE));
		cols.add(new AbsColumn(null, cols, DB.DATA.NAME));
		cols.add(new AbsColumn(null, cols, DB.DATA.STATUS, TCR_STATUS));
		cols.add(new AbsColumn(null, cols, DB.DATA.CATEGORY));
		cols.add(new AbsColumn(null, cols, DB.DATA.NOTES));

		return (cols);
	}

	/**
	 * add a new idea
	 */
	private void ideaAdd() {
		//LOG.trace(TT + ".ideaAdd()");
		Idea idea = new Idea();
		idea.setName(DateUtil.dateToCode());
		IdeaDialog dlg = new IdeaDialog(this);
		dlg.setIdea(idea);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			idea = dlg.getIdea();
			idea.setUuid();
			ideaX.ideas.add(idea);
			ideaX.fileSave();
			fillTable();
			table.setRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
		}
	}

	/**
	 * delete the current idea
	 */
	private void ideaDelete() {
		//LOG.trace(TT + ".ideaDelete()");
		Idea idea = getCurrentIdea();
		if (IdeaConfirmDelete.show(this, idea)) {
			return;
		}
		if (idea != null) {
			int idx = table.getSelectedRow() - 1;
			ideaX.ideas.remove(table.getSelectedRow());
			ideaX.fileSave();
			fillTable();
			if (idx >= 0) {
				table.setRowSelectionInterval(idx, idx);
			}
		}
	}

	/**
	 * update the current idea
	 */
	public void ideaUpdate() {
		//LOG.trace(TT + ".ideaUpdate()");
		Idea idea = getCurrentIdea();
		if (idea != null) {
			showDialog(table.getSelectedRow(), idea);
		}
	}

	public int ideaSelect(Idea idea) {
		int n = ideaX.findUUID(idea.getUuid());
		table.setRowSelectionInterval(n, n);
		return n;
	}

	public Idea showDialog(int index, Idea idea) {
		//LOG.trace(TT + ".showDialog()");
		IdeaDialog dlg = new IdeaDialog(this);
		if (idea.getUuid() == null || idea.getUuid().isEmpty()) {
			idea.setUuid();
		}
		dlg.setIdea(idea);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			Idea id = dlg.getIdea();
			ideaX.ideas.set(index, id);
			ideaX.fileSave();
			fillTable();
			table.setRowSelectionInterval(index, index);
		}
		return idea;
	}

	private void fillTable() {
		//LOG.trace(TT+".fillTable()");
		if (table == null) {
			return;
		}
		table.getSelectionModel().removeListSelectionListener(this);
		btDelete.setEnabled(false);
		DefaultTableModel dm = (DefaultTableModel) table.getModel();
		int rowCount = dm.getRowCount();
		for (int i = rowCount - 1; i >= 0; i--) {
			dm.removeRow(i);
		}
		rowCount = 0;
		List<Idea> ls = ideaX.ideas;
		for (Idea idea : ls) {
			addRow(idea);
			rowCount++;
		}
		table.getSelectionModel().addListSelectionListener(this);
	}

	private void addRow(Idea idea) {
		//LOG.trace(TT+".addRow(idea="+LOG.trace(idea)+")");
		List<Object> cols = new ArrayList<>();
		cols.add(idea.getCreation());
		cols.add(idea.getMaj());
		cols.add(idea.getUuid());
		cols.add(idea.getName());
		cols.add(idea.getStatus());
		cols.add(idea.getCategory());
		cols.add(Html.htmlToText(idea.getNotes(), 64));
		tableModel.addRow(cols.toArray());
	}

	private Idea getCurrentIdea() {
		int row = table.getSelectedRow();
		if (row != -1) {
			// find idea from name column
			int modelIndex = table.getRowSorter().convertRowIndexToModel(row);
			Object obj = tableModel.getValueAt(modelIndex, 3);
			if (obj instanceof String) {
				String n = (String) obj;
				for (Idea idea : ideaX.ideas) {
					if (idea.getName().equals(n)) {
						return idea;
					}
				}
			}
		}
		return null;
	}

	private void close() {
		started = false;
		dispose();
	}

	/**
	 * update an Idea from a Book in the IdeaBox
	 *
	 * @param mainFrame : the source MainFrame
	 * @param idea : the Idea
	 * @return the modified Idea if OK, else false
	 *
	 */
	public static Idea ideaboxUpdate(MainFrame mainFrame, Idea idea) {
		//LOG.trace(TT + ".ideaboxUpdate(mainFrame, idea=" + LOG.trace(idea) + ")");
		if (idea == null) {
			return null;
		}
		IdeaxXml ideax = new IdeaxXml();
		if (IdeaxFrm.instance != null) {
			ideax = IdeaxFrm.instance.ideaX;
		}
		if (idea.getUuid() == null || idea.getUuid().isEmpty()) {
			idea.setUuid();
		}
		int index = ideax.findUUID(idea.getUuid());
		if (index == -1) {
			MessageDlg msg = new MessageDlg(mainFrame,
					I18N.getMsg("ideabox.not_find"),
					I18N.getMsg("ideabox")
			);
			msg.hideCancel();
			msg.setVisible(true);
			return null;
		}
		ideax.ideas.set(index, idea);
		ideax.fileSave();
		if (IdeaxFrm.instance != null) {
			IdeaxFrm.instance.fillTable();
		}
		return idea;
	}

	/**
	 * copy an Idea from a Book to the IdeaBox
	 *
	 * @param mainFrame : the source MainFrame
	 * @param idea : the Idea
	 * @return the modified Idea if OK, else false
	 *
	 */
	public static Idea ideaboxAdd(MainFrame mainFrame, Idea idea) {
		//LOG.trace(TT + ".ideaboxAdd(mainFrame, idea=" + LOG.trace(idea) + ")");
		if (idea == null) {
			return null;
		}
		IdeaxXml ideax = new IdeaxXml();
		if (IdeaxFrm.instance != null) {
			ideax = IdeaxFrm.instance.ideaX;
		}
		if (idea.getUuid() == null || idea.getUuid().isEmpty()) {
			idea.setUuid();
		}
		if (ideax.findUUID(idea.getUuid()) != -1) {
			MessageDlg msg = new MessageDlg(mainFrame,
					I18N.getMsg("ideabox.copy_unable"),
					I18N.getMsg("ideabox")
			);
			msg.hideCancel();
			msg.setVisible(true);
			return null;
		}
		ideax.ideas.add(idea);
		ideax.fileSave();
		if (IdeaxFrm.instance != null) {
			IdeaxFrm.instance.fillTable();
		}
		return idea;
	}

	/**
	 * copy an Idea to the given project (Book)
	 *
	 * @param mainFrame
	 * @param idea
	 */
	private void copyToBook(MainFrame mainFrame, Idea idea) {
		//LOG.trace(TT + ".copyToBook(mainFrame, idea=" + LOG.trace(idea) + ")");
		if (mainFrame == null) {
			return;
		}
		mainFrame.getBookModel().ENTITY_New(idea);
	}

	/**
	 * show the Popup menu
	 *
	 * @param mouse
	 */
	public void showPopup(MouseEvent mouse) {
		//LOG.trace(TT + ".showPopup(mouse)");
		JPopupMenu popup = new JPopupMenu();

		JMenuItem item = new JMenuItem(I18N.getMsg("add"));
		item.setIcon(IconUtil.getIconSmall(ICONS.K.ADD));
		item.addActionListener(e -> ideaAdd());
		popup.add(item);
		int row = table.getSelectedRow();
		if (row > -1) {
			item = new JMenuItem(I18N.getMsg("delete"));
			item.setIcon(IconUtil.getIconSmall(ICONS.K.DELETE));
			item.addActionListener(e -> ideaDelete());
			popup.add(item);
		}
		JMenu lb = new JMenu(I18N.getColonMsg("ideabox.copy_from"));
		popup.add(lb);
		Idea idea = getCurrentIdea();
		for (MainFrame m : App.getInstance().getMainFrames()) {
			item = new JMenuItem(m.getBook().getTitle());
			item.setIcon(IconUtil.getIconSmall(ICONS.K.VW_BOOK));
			item.addActionListener(ex -> copyToBook(m, idea));
			lb.add(item);
		}
		popup.show(table, mouse.getX(), mouse.getY());
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		btDelete.setEnabled(table.getSelectedRow() != -1);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// empty
	}

	private class IdeaxFrmKeyListener implements KeyListener {

		public IdeaxFrmKeyListener() {
		}

		@Override
		public void keyTyped(KeyEvent e) {
			//LOG.trace("IdeaxFrmKeyListener.keyType(e=" + e.toString() + ")");
			switch (e.getKeyChar()) {
				case KeyEvent.VK_ENTER:
					ideaUpdate();
					break;
				case KeyEvent.VK_INSERT:
					ideaAdd();
					break;
				case KeyEvent.VK_DELETE:
					ideaDelete();
					break;
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// empty
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// empty
		}
	}

	private class IdeaxMouseAdapter implements MouseListener {

		public IdeaxMouseAdapter() {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger() || e.getModifiers() == InputEvent.BUTTON3_MASK) {
				showPopup(e);
			} else if (e.getClickCount() > 1) {
				ideaUpdate();
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

		@Override
		public void mouseEntered(MouseEvent e) {
			// empty
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// empty
		}
	}

	/**
	 * windows adaptor for the Ideabox JFrame
	 */
	private class IdeaxFrmWindowAdaptor extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent evt) {
			//LOG.trace("IdeaxDlgWindowAdaptor.windowClosing(evt=" + evt.toString() + ")");
			param.saveFrm(instance);
			AbsTable.tableDesignSave(table);
		}
	}

}
