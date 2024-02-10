package storybook.ui.panel.planning;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import resources.icons.IconUtil;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.status.Status.STATUS;
import storybook.model.Model;
import storybook.tools.DateUtil;
import storybook.tools.swing.CircleProgressBar;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.chart.occurences.Dataset;
import storybook.ui.chart.occurences.DatasetItem;
import storybook.ui.chart.occurences.Occurence;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.tree.Tree;

/**
 * Panel for planification vision.
 *
 * @author Jean
 *
 */
@SuppressWarnings("serial")
public class Planning extends AbstractPanel implements MouseListener {

	private JTabbedPane tabbedPane;
	private JTree tree;
	CircleProgressBar[] progress = new CircleProgressBar[5];
	private Dataset dataset;
	private Occurence timeline;
	private DefaultMutableTreeNode topNode;
	private PlanningElement topSp;
	private JPanel globalPanel;

	/**
	 * Constructor.
	 *
	 * @param mainframe to include panel in.
	 */
	public Planning(MainFrame mainframe) {
		super(mainframe);
	}

	@Override
	public void init() {
		//empty
	}

	/**
	 * initialize UI
	 */
	@Override
	public void initUi() {
		//LOG.printInfos("Planning.initUi()");
		//no toolbar to set
		// Create tabbed pane
		tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[]"));
		// add all subpanels to tabbed pane
		add(initGlobalPanel(), MIG.get(MIG.GROWX));
		//addTimePanel();
		add(addProgressPanel(), MIG.get(MIG.GROWX));
	}

	/**
	 * initialize panel for showing global progress information
	 */
	private JPanel initGlobalPanel() {
		//LOG.printInfos("Planning.addGlobalPanel()");
		globalPanel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP + " 5"),
		   "[center, sizegroup]", "[][grow]"));
		//tabbedPane.addTab(I18N.getMsg("plan.title.global"), globalPanel);
		// add progress bars
		for (int i = 0; i < STATUS.values().length; i++) {
			progress[i] = new CircleProgressBar(0, 100);
			int sz = IconUtil.getDefSize() * 4;
			progress[i].setPreferredSize(new Dimension(sz, sz));
			progress[i].setBorderPainted(false);
			globalPanel.add(progress[i], MIG.get(MIG.SPLIT2, MIG.FLOWY));
			globalPanel.add(new JSLabel(STATUS.values()[i].getLabel(), SwingConstants.CENTER));
		}
		refreshProgressBarsValues();
		return globalPanel;
	}

	/**
	 * refresh the progress bars
	 */
	private void refreshProgressBarsValues() {
		//LOG.printInfos(TT+".setProgressBarsValues()");
		Model model = mainFrame.getBookModel();
		int allScenes = mainFrame.project.scenes.getList().size();
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
	 * initialize panel for showing size progress information
	 */
	private JScrollPane addProgressPanel() {
		topSp = new PlanningElement();
		topSp.setElement(mainFrame.getProject().getName());
		// create panel
		topNode = new DefaultMutableTreeNode(topSp);
		tree = new Tree(topNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new PlanningTCR());
		JScrollPane scroller = new JScrollPane(tree);
		SwingUtil.setMaxPreferredSize(scroller);
		refreshProgressValues();
		tree.expandRow(0);
		tabbedPane.addTab(I18N.getMsg("plan.title.progress"), scroller);
		tree.addMouseListener(this);
		return scroller;
	}

	/**
	 * refresh progress values
	 */
	private void refreshProgressValues() {
		//LOG.printInfos(TT+".setProgressValues()");
		topNode.removeAllChildren();
		Map<Object, Integer> sizes = BookUtil.getBookSize(mainFrame);
		// get elements
		List<Part> parts = mainFrame.project.parts.getRoots();
		int topsize = 0;
		int topmaxsize = 0;
		int n = 1;
		for (Part part : parts) {
			createSubStructure(topNode, part, sizes);
			topmaxsize += part.getObjectiveChars();
			if (sizes.get(part) != null) {
				topsize += sizes.get(part);
			}
			n++;
		}
		if (topmaxsize == 0) {
			topSp.setSize(100);
			topSp.setMaxSize(100);
		} else {
			topSp.setSize(topsize);
			topSp.setMaxSize(topmaxsize);
		}
	}

	/**
	 * create substructure for a Part
	 *
	 * @param father
	 * @param part
	 * @param sizes
	 */
	private void createSubStructure(DefaultMutableTreeNode father,
	   Part part, Map<Object, Integer> sizes) {
		/*LOG.printInfos(TT+".createSubStructure("
				+ "father=" + father.toString()
				+ ", part=" + part.getName()
				+ ", sizes)");*/
		PlanningElement sp = new PlanningElement();
		sp.setElement(part);
		sp.setSize(sizes.get(part));
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(sp);
		father.add(node);

		List<Part> subparts = mainFrame.project.parts.getParts(part);
		List<Chapter> chapters = mainFrame.project.chapters.find(part);
		for (Part subpart : subparts) {
			createSubStructure(node, subpart, sizes);
		}
		for (Chapter chapter : chapters) {
			createSubStructure(node, chapter, sizes);
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

	/**
	 * create substructure
	 *
	 * @param father
	 * @param chapter
	 * @param sizes
	 */
	private void createSubStructure(DefaultMutableTreeNode father,
	   Chapter chapter,
	   Map<Object, Integer> sizes) {
		/*LOG.printInfos(TT+".createSubStructure("
				+ "father=" + father.toString()
				+ ", chapter" + chapter.getTitle()
				+ ", sizes)");*/
		PlanningElement sp = new PlanningElement();
		sp.setElement(chapter);
		sp.setSize(sizes.get(chapter));
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(sp);
		father.add(node);
		Model model = mainFrame.getBookModel();
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		for (Scene scene : scenes) {
			PlanningElement ssp = new PlanningElement();
			ssp.setElement(scene);
			try {
				ssp.setSize(sizes.get(scene));
			} catch (NullPointerException ex) {
				ssp.setSize(0);
			}
			DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(ssp);
			node.add(subnode);
		}
	}

	/**
	 * add panel for showing timeline information
	 */
	private void addTimePanel() {
		// create panel and scroller to contain it
		JPanel timePanel = new JPanel();
		JScrollPane scroller = new JScrollPane(timePanel);
		add(scroller, MIG.GROW);
		timePanel.setLayout(new MigLayout(MIG.get(MIG.WRAP + " 5", MIG.FILL), "[center]", "[][grow]"));
		tabbedPane.add(I18N.getMsg("plan.title.timeline"), scroller);
		// add chart
		timeline = getTimeline();
		timePanel.add(timeline, "wrap, grow");
	}

	/**
	 * get timeline panel
	 *
	 * @return
	 */
	private Occurence getTimeline() {
		//LOG.printInfos("Planning.createTimeline()");
		dataset = new Dataset();
		dataset.items = new ArrayList<>();
		List<Part> parts = mainFrame.project.parts.getRoots();
		Color[] color = ColorUtil.getNiceColors();
		int nc = 0;
		for (Part part : parts) {
			dataset.items.add(createPartItem(part));
			if (nc >= color.length) {
				nc = 0;
			}
		}
		dataset.idList = new ArrayList<>();
		return (new Occurence("date", dataset));
	}

	/**
	 * refresh timeline values
	 */
	private void refreshTimelineValues() {
		dataset.items.clear();
		List<Part> parts = mainFrame.project.parts.getRoots();
		Color[] color = ColorUtil.getNiceColors();
		int nc = 0;
		for (Part part : parts) {
			dataset.items.add(createPartItem(part));
			if (nc >= color.length) {
				nc = 0;
			}
		}
		dataset.idList = new ArrayList<>();
		timeline.redraw();
	}

	/**
	 * create Part items
	 *
	 * @param part
	 * @return
	 */
	private DatasetItem createPartItem(Part part) {
		//LOG.printInfos(TT+".createPartItem(part" + LOG.printInfos(part) + ")");
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
		Color[] colors = {ColorUtil.PALETTE.LIGHT_RED.getColor(),
			ColorUtil.PALETTE.LIGHT_BLUE.getColor(),
			ColorUtil.PALETTE.JADE.getColor()};
		DatasetItem item = new DatasetItem(part.getName(), debut, fin, colors[value]);
		List<DatasetItem> subItems = createChapterItems(part);
		item.setSubItem(subItems);
		return (item);
	}

	/**
	 * create Chapter items for the given Part
	 *
	 * @param part
	 * @return
	 */
	private List<DatasetItem> createChapterItems(Part part) {
		//LOG.printInfos(TT+".createChapterItems(part=" + LOG.printInfos(part) + ")");
		List<DatasetItem> items = new ArrayList<>();
		List<Chapter> chapters = mainFrame.project.chapters.find(part);
		Color[] colors = {ColorUtil.PALETTE.LIGHT_RED.getColor(),
			ColorUtil.PALETTE.LIGHT_BLUE.getColor(),
			ColorUtil.PALETTE.JADE.getColor()};
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

	/**
	 * model property change
	 *
	 * @param evt
	 */
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
		switch (Ctrl.getPROPS(propName)) {
			case REFRESH:
			case SHOWINFO:
				refreshValues();
				break;
			default:
				break;
		}
	}

	/**
	 * refresh all values
	 */
	private void refreshValues() {
		//LOG.printInfos(TT+".refreshValues()");
		refreshProgressBarsValues();
		refreshProgressValues();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.reload();
		//refreshTimelineValues();
	}

	/**
	 * show popup menu
	 *
	 * @param tree
	 * @param evt
	 */
	private void showPopupMenu(JTree tree, MouseEvent evt) {
		//LOG.printInfos("Planning.showPopupMenu(tree, evt)");
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
		if (!(eltObj instanceof PlanningElement)) {
			return;
		}
		Object userObj = ((PlanningElement) eltObj).getElement();
		if (userObj instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) userObj;
			JPopupMenu menu = EntityUtil.createPopupMenu(mainFrame, entity, EntityUtil.WITH_CHRONO);
			tree.setSelectionPath(selectedPath);
			JComponent comp = (JComponent) tree.getComponentAt(evt.getPoint());
			Point p = SwingUtilities.convertPoint(comp, evt.getPoint(), this);
			menu.show(this, p.x, p.y);
			evt.consume();
		}
	}

	/**
	 * mouse event listeners
	 *
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//LOG.printInfos(TT+".mouseClicked(e)");
		if (e.isPopupTrigger() || e.getModifiers() == InputEvent.BUTTON3_MASK) {
			showPopupMenu(tree, e);
		}
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

	/**
	 * action perform
	 *
	 * @param evt
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		// empty
	}

}
