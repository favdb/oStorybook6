package storybook.ui.panel.planning;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.status.Status.STATUS;
import storybook.project.Project;
import storybook.tools.DateUtil;
import storybook.tools.swing.CircleProgressBar;
import storybook.tools.swing.ColorUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.chart.occurences.Dataset;
import storybook.ui.chart.occurences.DatasetItem;
import storybook.ui.chart.occurences.Occurence;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.tree.TreeEntity;

/**
 * Panel for planification vision.
 *
 * @author Jean
 *
 */
@SuppressWarnings("serial")
public class PlanningPanel extends AbstractPanel implements MouseListener {

	private static final String TT = "PlanningPanel.";

	private JTree tree;
	CircleProgressBar[] progress = new CircleProgressBar[5];
	private Dataset dataset;
	private Occurence timeline;
	private DefaultMutableTreeNode topNode;
	private PlanningTreeItem topSp;
	private Project project;
	private JPanel treePanel;
	private JScrollPane treeScroll;

	/**
	 * Constructor.
	 *
	 * @param mainframe to include panel in.
	 */
	public PlanningPanel(MainFrame mainframe) {
		super(mainframe);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see storybook.ui.panel.AbstractPanel#init()
	 */
	@Override
	public void init() {
		this.project = mainFrame.project;
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+"initUi()");
		//no toolbar to set
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP)));
		//add(globalPanelInit(), MIG.get(MIG.CENTER, MIG.GROW));
		//add(treePanelInit(), MIG.GROW);
		JTabbedPane tab = new JTabbedPane();
		tab.setTabPlacement(JTabbedPane.TOP);
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP, MIG.GAP1), "[]"));
		p.add(globalInit(), MIG.GROWX);
		p.add(treeInit(), MIG.GROW);
		tab.add(I18N.getMsg("plan.title.global"), p);
		tab.add(I18N.getMsg("plan.title.timeline"), timelineInit());
		this.add(tab, MIG.GROW);
	}

	/**
	 * Generate panel for showing global information
	 */
	private JPanel globalInit() {
		//LOG.trace(TT+"globalInit()");
		JPanel p = new JPanel();
		MigLayout migLayout = new MigLayout(MIG.get(MIG.HIDEMODE3), "[20%][20%][20%][20%][20%]");
		p.setLayout(migLayout);
		// add progress bars
		for (int i = 0; i < STATUS.values().length; i++) {
			JPanel px = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP + " 1"), "[center]"));
			progress[i] = new CircleProgressBar(0, 100);
			progress[i].setPreferredSize(new Dimension(100, 100));
			progress[i].setBorderPainted(false);
			px.add(progress[i]);
			px.add(new JLabel(STATUS.values()[i].getLabel()));
			p.add(px);
		}
		globalRefresh();
		return p;
	}

	private void globalRefresh() {
		//LOG.trace(TT+"refreshProgressValues()");
		// get neded elements
		int allScenes = mainFrame.project.scenes.findByName().size();
		int[] nbs = new int[STATUS.values().length];
		for (STATUS st : STATUS.values()) {
			int i = st.ordinal();
			nbs[i] = mainFrame.project.scenes.findByStatus(i).size();
		}
		for (STATUS st : STATUS.values()) {
			int i = st.ordinal();
			progress[i].setValue((nbs[i] * 100) / ((allScenes == 0) ? 1 : allScenes));
		}

	}

	/**
	 * Generate panel for showing sizes progress information in a JTree
	 */
	private JPanel treeInit() {
		//LOG.trace(TT + "treeInit()");
		treePanel = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.HIDEMODE3)));
		topSp = new PlanningTreeItem(project.book.getTitle(), 0);
		// create panel
		topNode = new DefaultMutableTreeNode(topSp);

		tree = new TreeEntity(topNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new PlanningTreeTCR());
		treeScroll = new JScrollPane(tree);
		treeRefresh();
		tree.addMouseListener(this);
		treePanel.add(treeScroll, MIG.GROW);
		return treePanel;
	}

	private void treeRefresh() {
		//LOG.trace(TT + "treeRefresh()");
		topNode.removeAllChildren();
		int topsize = 0;
		int topmaxsize = 0;

		// get elements
		List<Part> parts = project.parts.getRoots();

		for (Part part : parts) {
			treeCreateSub(topNode, part);
			topmaxsize += part.getObjectiveChars();
			topsize += BookUtil.getNbChars(project, project.chapters.find(part));
		}
		if (topmaxsize == 0) {
			topSp.size = 0;
			topSp.max = 0;
		} else {
			topSp.size = topsize;
			topSp.max = topmaxsize;
		}
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private void treeCreateSub(DefaultMutableTreeNode father, Part part) {
		//LOG.trace(TT + "treeCreateSub(" + father.toString() + ", part=" + LOG.trace(part) + ")");
		PlanningTreeItem sp = new PlanningTreeItem(part, part.getObjectiveChars());
		sp.size = (BookUtil.getNbChars(project, project.chapters.find(part)));
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(sp);
		father.add(node);

		List<Part> subparts = project.parts.getParts(part);
		List<Chapter> chapters = project.chapters.find(part);

		for (Part subpart : subparts) {
			treeCreateSub(node, subpart);
		}
		for (Chapter chapter : chapters) {
			treeCreateSub(node, chapter);
		}

		// align objective chars value with the one of contained elements
		int subObjective = 0;
		for (Part subpart : subparts) {
			subObjective += subpart.getObjectiveChars();
		}
		for (Chapter chapter : chapters) {
			subObjective += chapter.getObjectiveChars();
		}
		if (subObjective > part.getObjectiveChars()) {
			part.setObjectiveChars(subObjective);
		}
	}

	private void treeCreateSub(DefaultMutableTreeNode father, Chapter chapter) {
		//LOG.trace(TT + "treeCreateSub(" + father.toString() + ", chapter=" + LOG.trace(chapter) + ")");
		PlanningTreeItem sp = new PlanningTreeItem(chapter, chapter.getObjectiveChars());
		sp.size = (BookUtil.getNbChars(project, chapter));
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(sp);
		father.add(node);
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		for (Scene scene : scenes) {
			PlanningTreeItem ssp = new PlanningTreeItem(scene, chapter.getObjectiveChars());
			try {
				ssp.size = scene.getChars();
			} catch (NullPointerException ex) {
				ssp.size = 0;
			}
			DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(ssp);
			node.add(subnode);
		}
	}

	/**
	 * Generate panel for showing timeline information
	 */
	private JPanel timelineInit() {
		//LOG.trace(TT+"timelineInit()");
		JPanel timelinePanel = new JPanel(new MigLayout("wrap 5,fill", "[center]", "[][grow]"));
		// add chart
		timeline = timelineCreate();
		timelinePanel.add(timeline, "wrap, grow");
		return timelinePanel;
	}

	private Occurence timelineCreate() {
		//LOG.trace(TT+"timelineCreate()");
		dataset = new Dataset();
		dataset.items = new ArrayList<>();
		List<Part> parts = mainFrame.project.parts.getRoots();
		Color[] color = ColorUtil.getNiceColors();
		int nc = 0;
		for (Part part : parts) {
			dataset.items.add(timelineCreatePart(part));
			if (nc >= color.length) {
				nc = 0;
			}
		}
		dataset.idList = new ArrayList<>();
		Occurence localTimeline = new Occurence("date", dataset);
		return localTimeline;
	}

	/**
	 * refresh timeline values
	 */
	private void timelineRefresh() {
		//LOG.trace(TT + "timelineRefresh()");
		dataset.items.clear();
		List<Part> parts = mainFrame.project.parts.getRoots();
		Color[] color = ColorUtil.getNiceColors();
		int nc = 0;
		for (Part part : parts) {
			dataset.items.add(timelineCreatePart(part));
			if (nc >= color.length) {
				nc = 0;
			}
		}
		dataset.idList = new ArrayList<>();
		timeline.redraw();
	}

	/**
	 * create timeline for the given Part
	 *
	 * @param part
	 * @return
	 */
	private DatasetItem timelineCreatePart(Part part) {
		//LOG.trace(TT+"timelineCreatePart(part=" + LOG.trace(part) + ")");
		Date ddj = java.util.Calendar.getInstance().getTime();
		Date debut;
		if (part.hasCreationTime()) {
			debut = new Date(part.getCreationTime().getTime());
		} else {
			debut = DateUtil.stringToDate(book.getCreation());
		}
		Date fin;
		int value = 0;
		if (part.hasDoneTime()) {
			value = 2;
			fin = part.getDoneTime();
		} else {
			fin = ddj;
			if (part.hasObjectiveTime()) {
				if (part.getObjectiveTime().after(ddj)) {
					value = 1;
				}
			} else {
				value = 2;
			}
		}
		Color[] colors = {ColorUtil.getNiceRed(), ColorUtil.getNiceBlue(), ColorUtil.getDarkGreen()};
		DatasetItem item = new DatasetItem(part.getName(), debut, fin, colors[value]);
		List<DatasetItem> subItems = timelineCreateChapter(part);
		item.setSubItem(subItems);
		return item;
	}

	/**
	 * create timeline for chapters of the given Part
	 *
	 * @param part
	 * @return
	 */
	private List<DatasetItem> timelineCreateChapter(Part part) {
		//LOG.trace(TT+"timelineCreateChapter(" + part.getName() + ")");
		List<DatasetItem> items = new ArrayList<>();
		List<Chapter> chapters = mainFrame.project.chapters.find(part);
		Color[] colors = {ColorUtil.getNiceRed(), ColorUtil.getNiceBlue(), ColorUtil.getNiceGreen()};
		Date ddj = java.util.Calendar.getInstance().getTime();
		for (Chapter chapter : chapters) {
			Date debut;
			if (chapter.hasCreationTime()) {
				debut = chapter.getCreationTime();
			} else {
				debut = DateUtil.stringToDate(book.getCreation());
			}
			Date fin;
			int value = 0;
			if (chapter.hasDoneTime()) {
				fin = chapter.getDoneTime();
				value = 2;
			} else {
				fin = ddj;
				if (chapter.hasObjectiveTime()) {
					if (chapter.getObjectiveTime().after(ddj)) {
						value = 1;
					}
				} else {
					value = 2;
				}
			}
			DatasetItem item = new DatasetItem(chapter.getName(), debut, fin, colors[value]);
			items.add(item);
		}
		return (items);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case SCENE:
				if (isInit(act)) {
					refresh();
				}
				if (isUpdate(act)) {
					refreshValues();
				}
				break;
			case CHAPTER:
			case PART:
				if (isUpdate(act)) {
					refreshValues();
				}
				break;
			default:
				break;
		}
		if (evt.getNewValue() instanceof View
				&& (((View) evt.getNewValue()).getName().equals(SbView.VIEWNAME.PLANNING.toString()))) {
			switch (Ctrl.getPROPS(propName)) {
				case REFRESH:
					refreshValues();
					break;
			}
		}
	}

	/**
	 * refresh all values
	 */
	private void refreshValues() {
		//LOG.trace(TT+"refreshValues()");
		globalRefresh();
		treeRefresh();
		timelineRefresh();
	}

	/**
	 * show the popup menu
	 *
	 * @param tree
	 * @param evt
	 */
	private void showPopupMenu(JTree tree, MouseEvent evt) {
		//LOG.trace(TT+"showPopupMenu(tree, evt)");
		TreePath selectedPath = tree.getPathForLocation(evt.getX(), evt.getY());
		DefaultMutableTreeNode selectedNode = null;
		try {
			selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
		} catch (Exception e) {
			// ignore
		}
		if (selectedNode == null) {
			return;
		}
		Object eltObj = selectedNode.getUserObject();
		if (!(eltObj instanceof PlannngElement)) {
			return;
		}
		JPopupMenu menu = null;
		Object userObj = ((PlannngElement) eltObj).getElement();
		if (userObj instanceof Category) {
			Category cat = (Category) userObj;
			menu = EntityUtil.createPopupMenu(mainFrame, cat, false);
		}
		if (userObj instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) userObj;
			menu = EntityUtil.createPopupMenu(mainFrame, entity, EntityUtil.WITH_CHRONO);
		}
		if (menu == null) {
			return;
		}
		tree.setSelectionPath(selectedPath);
		JComponent comp = (JComponent) tree.getComponentAt(evt.getPoint());
		Point p = SwingUtilities.convertPoint(comp, evt.getPoint(), this);
		menu.show(this, p.x, p.y);
		evt.consume();
	}

	///// Mouse listener /////
	@Override
	public void mouseReleased(MouseEvent e) {
		//LOG.trace(TT+"mouseReleased(e)");
		if (e.isPopupTrigger()) {
			showPopupMenu(tree, e);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
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

	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
