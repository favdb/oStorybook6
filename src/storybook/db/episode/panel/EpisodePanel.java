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
package storybook.db.episode.panel;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.episode.Episode;
import storybook.db.idea.Idea;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.ListUtil;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSMenuItem;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;

/**
 * JPanel to manage the Episodes
 *
 * @author favdb
 */
public class EpisodePanel extends AbstractPanel implements ChangeListener, MouseListener {

	private JButton btRenumber;
	private JButton btScreen;

	public enum NCOL {
		NUMBER, NAME, LINKS, PLOT, STRAND;

		public String getName() {
			return name().toLowerCase();
		}
	}

	private static final String TT = "EpisodePanel",
	   BT_STRAND = "new.strand",
	   BT_EPISODE = "episode.add",
	   BT_RENUMBER = "episode.renumber",
	   CK_TITLE = "ckTitle",
	   BT_EXIT = "btExit", BT_IDEA = "btIdea", BT_SCREEN = "btScreen";

	private static final int ZOOM_MIN = 1, ZOOM_MAX = 10;

	private EpisodeTable episodeTable;
	private JScrollPane scrollPane;
	private List<Strand> strands;
	private int charW, charH, rowHeight, zoom = ZOOM_MIN;
	private boolean modified;
	private JCheckBox ckTitle;

	public EpisodePanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	/**
	 * initialize the class
	 *
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void init() {
		//LOG.printInfos(TT + ".init()");
		strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		charW = SwingUtil.getCharWidth(App.getInstance().fonts.defGet());
		charH = SwingUtil.getCharHeight(App.getInstance().fonts.defGet());
		zoom = App.preferences.episodesGetZoom();
		rowHeight = (3 + zoom) * charH;
	}

	public List<Strand> getStrands() {
		return strands;
	}

	public int getCharW() {
		return charW;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * initialize the user interface
	 *
	 */
	@Override
	public void initUi() {
		//LOG.printInfos(TT + ".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		this.removeAll();
		add(initToolbar(), MIG.GROWX);
		episodeTable = new EpisodeTable(this);
		initKeyStroke("enter", KeyEvent.VK_ENTER, 0, new ActionEnter(this));
		initKeyStroke("down", KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, new ActionPlus(this));
		initKeyStroke("up", KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK, new ActionMinus(this));
		initKeyStroke("right", KeyEvent.VK_RIGHT, 0, new ActionArrow(this, KeyEvent.VK_RIGHT));
		initKeyStroke("left", KeyEvent.VK_LEFT, 0, new ActionArrow(this, KeyEvent.VK_LEFT));

		scrollPane = new JScrollPane(episodeTable);
		scrollPane.setPreferredSize(SwingUtil.getScreenSize());

		add(scrollPane, MIG.GROW);
		tableLoad();
		designLoad();

		episodeTable.addMouseListener(this);
		episodeTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
				int ncol = episodeTable.columnAtPoint(evt.getPoint());
				TableColumnModel colModel = episodeTable.getColumnModel();
				if (ncol > 2) {
					TableColumn column = colModel.getColumn(ncol);
					int sz = column.getWidth();
					for (int col = 3; col < episodeTable.getColumnCount(); col++) {
						if (ncol != col) {
							colModel.getColumn(col).setPreferredWidth(sz);
						}
					}
				}
				designSave();
				mainFrame.setUpdated();
				scrollPane.revalidate();
				evt.consume();
			}
		});
		modified = false;
		episodeTable.requestFocusInWindow();
	}

	private void initKeyStroke(String title, int key, int mask, AbstractAction action) {
		KeyStroke ks = KeyStroke.getKeyStroke(key, mask);
		episodeTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, title + "Action");
		episodeTable.getActionMap().put(title + "Action", action);
	}

	/**
	 * initialize the ToolBar
	 *
	 * @return
	 */
	@Override
	public JToolBar initToolbar() {
		//LOG.printInfos(TT + ".initToolbar()");
		super.initToolbar();
		toolbar.setFloatable(false);
		toolbar.setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.HIDEMODE3)));
		toolbar.setName("EpisodeToolbar");
		toolbar.add(initTB1(), MIG.GROWX);
		if (mainFrame.isEpisode) {
			toolbar.add(initTB11(), MIG.get(MIG.SPAN, MIG.RIGHT));
		}
		return toolbar;
	}

	private JPanel initTB1() {
		JPanel tb1 = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		tb1.setOpaque(false);
		tb1.add(SwingUtil.createButton("", ICONS.K.ADD, "episode.add", false,
		   evt -> episodeCreate()));
		btRenumber = SwingUtil.createButton("", ICONS.K.SORT, "episode.renumber", false,
		   evt -> episodeRenum());
		btRenumber.setEnabled(false);
		tb1.add(btRenumber);
		tb1.add(Ui.initButton(BT_STRAND, "", ICONS.K.ENT_STRAND, "new.strand",
		   evt -> strandCreate()));
		tb1.add(new JLabel(I18N.getColonMsg("table.row")));
		JSlider slider = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, zoom);
		slider.setToolTipText(I18N.getMsg("table.row_height"));
		slider.setName("zoom");
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.addChangeListener(this);
		slider.setFocusable(false);
		tb1.add(slider);
		ckTitle = new JCheckBox(I18N.getMsg("title"));
		ckTitle.setName(CK_TITLE);
		ckTitle.addActionListener(this);
		ckTitle.setSelected(App.preferences.episodesGetWithTitle());
		ckTitle.setToolTipText(I18N.getMsg("episode.link_with_title"));
		tb1.add(ckTitle);
		return tb1;
	}

	private JPanel initTB11() {
		//LOG.printInfos(TT + ".initTB11()");
		JPanel tb = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP1)));
		tb.setOpaque(false);
		tb.setBorder(BorderFactory.createEmptyBorder());
		// idea
		tb.add(Ui.initButton(BT_IDEA, "", ICONS.K.ENT_IDEA,
		   "foi.new", e -> mainFrame.showEditorAsDialog(new Idea())));
		tb.add(new JSeparator(), MIG.GROWX);
		//sortie du mode typist
		btScreen = Ui.initButton(BT_SCREEN, "", ICONS.K.SCREEN_NORMAL,
		   "screen.normal", e -> returnToMainFrame());
		tb.add(btScreen, MIG.RIGHT);
		// exit
		tb.add(Ui.initButton(BT_EXIT, "", ICONS.K.EXIT,
		   "file.exit", e -> doExit()));
		return tb;
	}

	public boolean isTitle() {
		return ckTitle.isSelected();
	}

	/**
	 * load the table
	 *
	 */
	private void tableLoad() {
		//LOG.printInfos(TT + ".tableLoad()");
		while (episodeTable.getRowCount() > 0) {
			episodeTable.removeRow(0);
		}
		int row = -1;
		@SuppressWarnings("unchecked")
		List<Episode> episodes = (List) mainFrame.project.getList(Book.TYPE.EPISODE);
		if (episodes.isEmpty()) {
			episodeCreate();
			return;
		}
		for (Episode episode : episodes) {
			if (episode.getStrand() == null) {
				episodeTable.addRow();
				row++;
				episodeTable.setValueAt(episode.getNumber(), row, NCOL.NUMBER.ordinal());
				episodeTable.setValueAt(episode.getName(), row, NCOL.NAME.ordinal());
				if (episode.getLink() != null) {
					episodeTable.setValueAt(episode.getLink(), row, NCOL.LINKS.ordinal());
				} else {
					episodeTable.setValueAt(IconUtil.getIconImageSmall(ICONS.K.UNKNOWN), row, NCOL.LINKS.ordinal());
				}
				episodeTable.setValueAt(episode.getNotes(), row, NCOL.PLOT.ordinal());
			} else {
				int col = strands.indexOf(episode.getStrand());
				episodeTable.setValueAt(episode.getNotes(), row, col + NCOL.PLOT.ordinal() + 1);
			}
		}
	}

	/**
	 * property change
	 *
	 * @param evt
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.printInfos(TT + ".modelPropertyChange(evt=" + evt.toString() + ")");
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case STRAND:
			case CHAPTER:
			case SCENE:
				refresh();
		}
	}

	/**
	 * action to perform
	 *
	 * @param evt
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		//LOG.printInfos(TT + ".actionPerformed(evt source=" + evt.getSource().toString() + ")");
		if (evt.getSource() instanceof JCheckBox) {
			App.preferences.episodesSetWithTitle(ckTitle.isSelected());
			refresh();
		}
	}

	/**
	 * state change for slider
	 *
	 * @param evt
	 */
	@Override
	public void stateChanged(ChangeEvent evt) {
		//LOG.printInfos(TT + "stateChanged(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JSlider) {
			zoom = ((JSlider) evt.getSource()).getValue();
			designSave();
			rowHeight = (3 + zoom) * charH;
			episodeTable.setRowHeight(rowHeight);
		}
	}

	private void showPopupMenu(JPopupMenu menu, int row, int col) {
		Rectangle r = episodeTable.getCellRect(row, col, false);
		menu.show(episodeTable, r.x, r.y);
	}

	/**
	 * get popup menu for Epsiode number from row and column
	 *
	 * @param row
	 * @param col
	 * @return
	 */
	public JPopupMenu getPopupNumber(int row, int col) {
		int[] rows = episodeTable.getSelectedRows();
		JPopupMenu menu = new JPopupMenu();
		menu.add(new JSMenuItem(BT_EPISODE, ICONS.K.ADD, evt -> episodeCreate()));
		if (rows.length > 0 && rows[0] > 0) {
			if (row == rows[0]) {
				menu.add(new JSMenuItem("move.up", ICONS.K.AR_UP, evt -> moveBefore(row - 1)));
			}
		}
		if (rows.length > 0 && rows[rows.length - 1] < episodeTable.getRowCount() - 1) {
			if (row == rows[0]) {
				menu.add(new JSMenuItem("move.down", ICONS.K.AR_DOWN, evt -> moveAfter(row)));
			} else {
				if (!episodeTable.isArrayContains(rows, row)) {
					menu.add(new JSMenuItem("move.below", ICONS.K.AR_DOWN, evt -> moveAfter(row)));
				}
			}
		}
		JMenu jm = new JMenu(I18N.getMsg("move.below"));
		for (int r = 0; r < episodeTable.getRowCount(); r++) {
			int n = r + 1;
			Integer nr = (Integer) episodeTable.getValueAt(r, 0);
			if (!episodeTable.isArrayContains(rows, r)
			   && !episodeTable.isArrayContains(rows, r + 1)) {
				JMenuItem it = new JMenuItem(nr.toString());
				it.addActionListener(evt -> moveAfter(n - 1));
				jm.add(it);
			}
		}
		menu.add(jm);
		if (!checkNumber()) {
			menu.add(new JSMenuItem(BT_RENUMBER, ICONS.K.SHOW_ALL, evt -> episodeRenum()));
		}
		menu.add(new JSMenuItem("episode.remove", ICONS.K.DELETE, evt -> episodeRemove()));
		return menu;
	}

	/**
	 * show popup menu for Epsiode number from MouseEvent
	 *
	 * @param e : the MouseEvent
	 */
	public void showPopupNumber(MouseEvent e) {
		JPopupMenu menu = getPopupNumber(episodeTable.pointToRow(e), episodeTable.pointToCol(e));
		menu.show(episodeTable, e.getX(), e.getY());
	}

	/**
	 * show popup menu for Episode number for row and column
	 *
	 * @param row
	 * @param col
	 */
	public void showPopupNumber(int row, int col) {
		//LOG.printInfos(TT + ".showPopupNumber(row=" + row + ", col=" + col + ")");
		JPopupMenu menu = getPopupNumber(row, col);
		showPopupMenu(menu, row, col);
	}

	/**
	 * show popup menu for chapter or scene
	 *
	 * @param row
	 * @param col
	 */
	public void showPopupScene(int row, int col) {
		//LOG.printInfos(TT + ".showPopupScene(e=" + e.toString() + ")");
		if (row == -1 || col != 2) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		@SuppressWarnings("unchecked")
		List<Chapter> chapters = (List<Chapter>) mainFrame.project.chapters.getList();
		JMenu chapMenu = new JMenu(I18N.getMsg("chapters"));
		for (Chapter chapter : chapters) {
			chapMenu.add(createMenuItem(row, chapter));
		}
		menu.add(chapMenu);
		JMenu chapScene = new JMenu(I18N.getMsg("scenes"));
		for (Chapter chapter : chapters) {
			JMenu ch = new JMenu(TextUtil.ellipsize(chapter.getName(), 24));
			List<Scene> scenes = mainFrame.project.scenes.find(chapter);
			for (Scene scene : scenes) {
				ch.add(createMenuItem(row, scene));
			}
			chapScene.add(ch);
		}
		menu.add(chapScene);
		menu.add(new JSeparator());
		menu.add(new JSMenuItem("chapter.new", ICONS.K.NEW_CHAPTER, evt -> entityCreate(row, new Chapter())));
		menu.add(new JSMenuItem("scene.new", ICONS.K.NEW, evt -> entityCreate(row, new Scene())));
		if (episodeTable.getValueAt(row, NCOL.LINKS.ordinal()) instanceof AbstractEntity) {
			menu.add(new JSMenuItem("link.remove", ICONS.K.REMOVE, evt -> linkTo(row, null)));
		}
		showPopupMenu(menu, row, col);
	}

	/**
	 * get HTML string for key and value
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	private String getKeyValue(String key, String value) {
		return Html.P_B + key + ": " + value + Html.P_E;
	}

	/**
	 * get description content from episode for chapter ou scene
	 *
	 * @param row
	 * @return
	 */
	private String getDescription(int row) {
		StringBuilder b = new StringBuilder();
		b.append(getKeyValue(I18N.getMsg(NCOL.PLOT.getName()),
		   getStringValue(row, NCOL.PLOT.ordinal())));
		for (int c = 0; c < strands.size(); c++) {
			String s = (String) episodeTable.getValueAt(row, c + NCOL.PLOT.ordinal());
			if (s != null && !s.isEmpty()) {
				b.append(getKeyValue(strands.get(c).getName(), s));
			}
		}
		return b.toString();
	}

	/**
	 * get a String value from a cell
	 *
	 * @param row
	 * @param col
	 * @return
	 */
	private String getStringValue(int row, int col) {
		return episodeTable.getValueAt(row, col).toString();
	}

	/**
	 * create a chapter or a scene
	 *
	 * @param row
	 * @param entity
	 */
	private void entityCreate(int row, AbstractEntity entity) {
		String s = getStringValue(row, NCOL.NAME.ordinal());
		if (!s.isEmpty()) {
			entity.setName(s);
		}
		entity.setDescription(getDescription(row));
		if (entity instanceof Scene) {
			Strand mstrand = null;
			List<Strand> lstrands = new ArrayList<>();
			for (int col = 4; col < episodeTable.getColumnCount(); col++) {
				String t = (String) episodeTable.getValueAt(row, col);
				if (!t.isEmpty()) {
					if (mstrand == null) {
						mstrand = strands.get(col - NCOL.STRAND.ordinal());
					} else {
						lstrands.add(strands.get(col - NCOL.STRAND.ordinal()));
					}
				}
			}
			if (mstrand != null) {
				((Scene) entity).setStrand(mstrand);
			}
			((Scene) entity).setStrands(lstrands);
		}
		boolean r = mainFrame.showEditorAsDialog(entity);
		if (!r) {
			linkTo(row, entity);
			if (s.isEmpty()) {
				episodeTable.setValueAt(entity.getName(), row, NCOL.NAME.ordinal());
			}
		}
	}

	/**
	 * get a JMenuItem
	 *
	 * @param row
	 * @param entity
	 * @return
	 */
	private JMenuItem createMenuItem(int row, AbstractEntity entity) {
		if (entity instanceof Scene) {
			return new JSMenuItem(entity, ICONS.K.ENT_SCENE, evt -> linkTo(row, entity));
		}
		return new JSMenuItem(entity, ICONS.K.ENT_CHAPTER, evt -> linkTo(row, entity));
	}

	/**
	 * set a link to a Chapter or a Scene or null to remove link
	 *
	 * @param row
	 * @param entity
	 */
	private void linkTo(int row, AbstractEntity entity) {
		//LOG.printInfos(TT + ".linkTo(row=" + row + ", entity=" + AbstractEntity.printInfos(entity) + ")");
		episodeTable.setValueAt(entity, row, NCOL.LINKS.ordinal());
		if (getStringValue(row, NCOL.NAME.ordinal()).isEmpty()) {
			episodeTable.setValueAt(entity.getName(), row, NCOL.NAME.ordinal());
		}
		setModified();
	}

	/**
	 * create a Strand
	 */
	private void strandCreate() {
		//LOG.printInfos(TT + ".createStrand()");
		Strand strand = new Strand();
		boolean r = mainFrame.showEditorAsDialog(strand);
		if (r) {
			refresh();
		}
	}

	/**
	 * create an Episode
	 */
	public void episodeCreate() {
		//LOG.printInfos(TT + ".episodeCreate()");
		episodeTable.addRow();
	}

	/**
	 * mouse action to perform
	 *
	 * @param e
	 */
	private void mouseAction(MouseEvent e) {
		//LOG.printInfos(TT + ".mouseAction(e=" + e.toString() + ")");
		int col = episodeTable.pointToCol(e);
		if (col == NCOL.NUMBER.ordinal() && e.isPopupTrigger()) {
			episodeTable.selectRow(episodeTable.pointToRow(e));
			showPopupNumber(e);
		} else if (col == NCOL.LINKS.ordinal()) {
			showPopupScene(episodeTable.pointToRow(e), episodeTable.pointToCol(e));
		}
		e.consume();
	}

	/**
	 * mouse clicked event
	 *
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		//LOG.printInfos(TT + ".mouseClicked(e=" + e.toString() + ")");
		int col = episodeTable.pointToCol(e);
		int row = episodeTable.pointToRow(e);
		if (e.getClickCount() == 2 && e.getSource() instanceof JTable) {
			mouseAction(e);
		} else if (col == NCOL.NUMBER.ordinal()) {
			if (e.isPopupTrigger()) {
				mouseAction(e);
			}
			if (e.isShiftDown()) {
				episodeTable.selectAddRow(episodeTable.pointToRow(e));
				e.consume();
			} else if (e.isControlDown()) {
				// non contigus selection not allowed
				e.consume();
			} else {
				episodeTable.selectRow(episodeTable.pointToRow(e));
				e.consume();
			}
		}
	}

	/**
	 * mous pressed event
	 *
	 * @param e : mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		//LOG.printInfos(TT + ".mousePressed(e=" + e.toString() + ")");
		if (e.isPopupTrigger() && e.getSource() instanceof JTable) {
			mouseAction(e);
		}
	}

	/**
	 * mouse released event
	 *
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		//LOG.printInfos(TT + ".mouseReleased(e=" + e.toString());
	}

	/**
	 * mouse entered event
	 *
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		//LOG.printInfos(TT + ".mouseEntered(e=" + e.toString());
	}

	/**
	 * mouse exited event
	 *
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		//LOG.printInfos(TT + ".mouseExited(e=" + e.toString());
	}

	/**
	 * remove episode(s) from the table
	 *
	 */
	private void episodeRemove() {
		//LOG.printInfos(TT + "episodeRemove()");
		int[] rows = episodeTable.getSelectedRows();
		for (int i = rows.length - 1; i > -1; i--) {
			int row = rows[i];
			episodeTable.removeRow(row);
		}
		episodeRenum();
	}

	/**
	 * renumber the episodes
	 *
	 */
	private void episodeRenum() {
		//LOG.printInfos(TT + ".episodeRenum()");
		for (int row = 0; row < episodeTable.getRowCount(); row++) {
			episodeTable.setValueAt(row + 1, row, 0);
		}
		setModified();
	}

	/**
	 * write the table to Episodes
	 *
	 */
	@SuppressWarnings("unchecked")
	public synchronized void save() {
		//LOG.printInfos(TT + ".write() modified=" + (modified ? "true" : "false"));
		//first remove all episodes from DB
		if (episodeTable.isEditing()) {
			episodeTable.getCellEditor().stopCellEditing();
		}
		if (!modified) {
			//LOG.printInfos("not modified");
			return;
		}
		if (episodeTable.getRowCount() > 0) {
			removeEpisodes();
		}
		//second create all episodes into DB
		int nrow = 1;
		Long id = 1L;
		for (int row = 0; row < episodeTable.getRowCount(); row++) {
			if (getStringValue(row, NCOL.NAME.ordinal()).isEmpty()) {
				//if title is empty then no write for this row
				continue;
			}
			Episode episode = new Episode();
			episode.setId(id++);
			//reward strand and scene are null when Epsiode was initialized
			episode.setNumber(nrow++);//automatic renumber the episode
			episode.setName((String) episodeTable.getValueAt(row, NCOL.NAME.ordinal()));
			Object obj = episodeTable.getValueAt(row, NCOL.LINKS.ordinal());
			if (obj instanceof AbstractEntity) {
				episode.setLink((AbstractEntity) obj);
			} else {
				episode.setLink(null);
			}
			episode.setNotes((String) episodeTable.getValueAt(row, NCOL.PLOT.ordinal()));
			episodeSave(episode);
			for (int col = 0; col < strands.size(); col++) {
				String val = (String) episodeTable.getValueAt(row, col + NCOL.PLOT.ordinal() + 1);
				if (val != null && !val.isEmpty()) {
					episode.setId(id++);
					episode.setStrandNotes(strands.get(col), val);
					episodeSave(episode);
				}
			}
		}
		modified = false;
	}

	/**
	 * remove all episodes from the data base
	 *
	 */
	private void removeEpisodes() {
		//LOG.printInfos(TT + ".removeEpisodes()");
		for (Object obj : mainFrame.project.getList(Book.TYPE.EPISODE)) {
			if (((Episode) obj).getId() != -1L) {
				mainFrame.getBookModel().EPISODE_Delete((Episode) obj);
			}
		}
	}

	/**
	 * write an Episode into the DB
	 *
	 * @param episode
	 */
	private void episodeSave(Episode episode) {
		//LOG.printInfos(TT + ".episodeSave(" + AbstractEntity.printInfos(episode) + ")"
		//	+"<=>" + AbstractEntity.printInfos(episode.getStrand());
		episode.setId(-1L);
		mainFrame.getBookModel().ENTITY_New(episode);
		mainFrame.setUpdated();
	}

	/**
	 * get the EpisodeTable for external usage
	 *
	 * @return
	 */
	public EpisodeTable getEpisodeTable() {
		return episodeTable;
	}

	/**
	 * set modified
	 */
	public void setModified() {
		modified = true;
		btRenumber.setEnabled(!checkNumber());
		save();
	}

	/**
	 * write the design of the table
	 *
	 */
	public void designSave() {
		//LOG.printInfos(TT+".designSave()");
		List<String> b = new ArrayList<>();
		b.add(zoom + "");
		for (int col = 0; col < NCOL.PLOT.ordinal(); col++) {
			b.add(episodeTable.getColumnModel().getColumn(col).getPreferredWidth() + "");
		}
		App.preferences.episodesSet(ListUtil.join(b, ","));
	}

	/**
	 * load the design of the table
	 *
	 */
	private void designLoad() {
		//LOG.printInfos(TT + ".designLoad() " + App.preferences.episodesGet());
		String param[] = App.preferences.episodesGet().split(",");
		TableColumnModel colModel = episodeTable.getColumnModel();
		//zoom
		if (param.length > 0) {
			zoom = Integer.parseInt(param[0]);
			rowHeight = (3 + zoom) * charH;
		}
		//column 0
		if (param.length > 1) {
			colModel.getColumn(NCOL.NUMBER.ordinal()).setPreferredWidth(Integer.parseInt(param[1]));
		}
		//column 1
		if (param.length > 2) {
			colModel.getColumn(NCOL.NAME.ordinal()).setPreferredWidth(Integer.parseInt(param[2]));
		}
		//column 2
		if (param.length > 3) {
			colModel.getColumn(NCOL.LINKS.ordinal()).setPreferredWidth(Integer.parseInt(param[3]));
		}
		//column 3 and others
		int sz = 32 * charW;
		if (param.length > 4) {
			sz = Integer.parseInt(param[4]);
		}
		for (int col = NCOL.PLOT.ordinal(); col < episodeTable.getColumnCount(); col++) {
			episodeTable.getColumnModel().getColumn(col).setPreferredWidth(sz);

		}
	}

	private void moveBefore(int row) {
		episodeTable.moveBefore(row);
		setModified();
		//episodeRenum();
	}

	private void moveAfter(int row) {
		episodeTable.moveAfter(row);
		setModified();
		//episodeRenum();
	}

	/**
	 * check if episodes are ordered
	 *
	 * @return true when all episodes are ordered
	 */
	private boolean checkNumber() {
		for (int row = 0; row < episodeTable.getRowCount(); row++) {
			int nx = Integer.parseInt(episodeTable.getValueAt(row, 0).toString());
			if (nx != row + 1) {
				return false;
			}
		}
		return true;
	}

	private void doExit() {
		mainFrame.fileSave(true);
		SwingUtilities.invokeLater(() -> {
			mainFrame.close(true);
		});
	}

	private void returnToMainFrame() {
		//LOG.printInfos(TT + ".returnToMainFrame()");
		if (modified) {
			mainFrame.setUpdated();
		}
		mainFrame.episodeActivate();
		mainFrame.setVisible(true);
	}

	/**
	 * action class when Enter was typed in EpisodeTable
	 *
	 */
	private static class ActionEnter extends AbstractAction {

		private final EpisodePanel panel;

		public ActionEnter(EpisodePanel panel) {
			this.panel = panel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//LOG.printInfos(TT + ".ActionEnter" + ".actionPerformed(e=" + e.toString() + ")");
			int curRow = panel.getEpisodeTable().getSelectedRow();
			int curCol = panel.getEpisodeTable().getSelectedColumn();
			if (curCol == NCOL.NUMBER.ordinal()) {
				panel.showPopupNumber(curRow, curCol);
			} else if (curCol == NCOL.LINKS.ordinal()) {
				panel.showPopupScene(curRow, curCol);
			} else {
				panel.getEpisodeTable().editCellAt(curRow, curCol);
			}
		}
	}

	/**
	 * action class when Enter was typed in EpisodeTable
	 *
	 */
	private static class ActionPlus extends AbstractAction {

		private final EpisodePanel panel;

		public ActionPlus(EpisodePanel panel) {
			this.panel = panel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//LOG.printInfos(TT + ".ActionPlus" + ".actionPerformed(e=" + e.toString() + ")");
			int curRow = panel.getEpisodeTable().getSelectedRow();
			int curCol = panel.getEpisodeTable().getSelectedColumn();
			if (curCol == NCOL.NUMBER.ordinal()) {
				panel.moveAfter(curRow);
			}
		}
	}

	/**
	 * action class when Enter was typed in EpisodeTable
	 *
	 */
	private static class ActionMinus extends AbstractAction {

		private final EpisodePanel panel;

		public ActionMinus(EpisodePanel panel) {
			this.panel = panel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//LOG.printInfos(TT + ".ActionMinus" + ".actionPerformed(e=" + e.toString() + ")");
			int curRow = panel.getEpisodeTable().getSelectedRow();
			int curCol = panel.getEpisodeTable().getSelectedColumn();
			if (curCol == NCOL.NUMBER.ordinal()) {
				panel.moveBefore(curRow - 1);
			}
		}
	}

	/**
	 * action class when Enter was typed in EpisodeTable
	 *
	 */
	private static class ActionArrow extends AbstractAction {

		private final EpisodePanel panel;
		private final int vk;

		public ActionArrow(EpisodePanel panel, int vk) {
			this.panel = panel;
			this.vk = vk;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//LOG.printInfos(TT + ".ActionArrow" + ".actionPerformed(e=" + e.toString()
			//		+ ") for key=" + (vk == KeyEvent.VK_RIGHT ? "VK_RIGHT" : "VK_LEFT"));
			EpisodeTable tb = panel.getEpisodeTable();
			int row = tb.getSelectedRow();
			int col = tb.getSelectedColumn() + (this.vk == KeyEvent.VK_LEFT ? -1 : 1);
			if (col >= tb.getColumnCount()) {
				row++;
				col = 0;
			} else if (col < 0) {
				row--;
				col = tb.getColumnCount() - 1;
			}
			if (col >= tb.getColumnCount() || row >= tb.getRowCount()
			   || col < 0 || row < 0) {
				return;
			}
			panel.getEpisodeTable().selectCell(row, col);
		}
	}

}
