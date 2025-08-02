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
package storybook.ui.panel.tree;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.idea.IdeaStatusModel;
import storybook.db.item.Item;
import storybook.db.item.ItemCategory;
import storybook.db.location.CityCategory;
import storybook.db.location.CountryCategory;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.scene.Scene;
import storybook.db.status.AbstractStatus;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.db.tag.TagCategory;
import storybook.dialog.options.OptionsDlg;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.TreeUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.panel.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class TreePanel extends AbstractPanel implements
		TreeExpansionListener, TreeSelectionListener, MouseListener, ActionListener {

	private static final String TT = "TreePanel.";

	private Tree tree;
	private JScrollPane scroller;
	private DefaultMutableTreeNode topNode;
	private EntityNode partsNode, strandsNode, personsByCategoryNode,
			personsByGendersNode, locationsNode, itemsNode, tagsNode,
			plotsNode, ideasNode, memosNode;
	private JCheckBoxMenuItem mnuPart,
			//mnuChapter,
			mnuStrand, mnuPerson, mnuByGender,
			mnuLocation, mnuItem, mnuTag,
			mnuPlot, mnuIdea;
	private List<JCheckBoxMenuItem> mnuList;
	private JMenuItem mnuOptions;
	private DefaultTreeTransferHandler treeTransferHandler;

	public TreePanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		if (propName == null
				|| "SHOWINFO".equalsIgnoreCase(propName)) {
			return;
		}
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		switch (Ctrl.getPROPS(evt)) {
			case REFRESH:
				View newView = (View) newValue;
				View view = (View) getParent().getParent();
				if (view == newView) {
					treeRefresh();
				}
				return;
			case SHOWINFO:
				return;
			default:
				if (ActKey.testCmd(evt, Ctrl.PROPS.NEW)) {
					treeRefresh();
					return;
				}
				if (newValue instanceof AbstractEntity) {
					treeRefresh();
					return;
				}
				if (oldValue instanceof AbstractEntity) {
					treeRefresh();
				}
		}
	}

	public TreePath[] getPaths(Tree tree, boolean expanded) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		List<Object> list = new ArrayList<>();
		getPaths(tree, new TreePath(root), expanded, list);
		return new TreePath[list.size()];
	}

	public void getPaths(Tree tree, TreePath parent, boolean expanded, List<Object> list) {
		if (expanded && !tree.isVisible(parent)) {
			return;
		}
		list.add(parent);
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				getPaths(tree, path, expanded, list);
			}
		}
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL, MIG.INS0)));
		setFont(App.fonts.defGet());
		setMinimumSize(new Dimension(280, 180));
		initToolbar();
		add(toolbar, "growx");
		topNode = new DefaultMutableTreeNode(mainFrame.getBook().getTitle());
		tree = new Tree(topNode);
		tree.setFont(App.fonts.defGet());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeTransferHandler = new DefaultTreeTransferHandler(this, DnDConstants.ACTION_MOVE);
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setCellRenderer(new EntityTreeCellRenderer());
		tree.setLargeModel(true);
		scroller = new JScrollPane(tree);
		SwingUtil.setMaxPreferredSize(scroller);
		add(scroller, MIG.GROW);
		treeRefresh();
		String str = App.preferences.getString(Pref.KEY.TREE_OPTIONS);
		if (!str.isEmpty()) {
			TreeUtil.expanstionStateSet(tree, 0, str);
		}
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		tree.addTreeExpansionListener(this);
	}

	private JPopupMenu initPopup() {
		String str = App.preferences.getString(Pref.KEY.TREE_SHOW);
		mnuList = new ArrayList<>();
		JPopupMenu popupMenu = new JPopupMenu("Title");
		int nc = 0;
		mnuPart = initMenuItem("part", str.charAt(nc++) == '1');
		mnuList.add(mnuPart);
		mnuStrand = initMenuItem("strand", str.charAt(nc++) == '1');
		mnuList.add(mnuStrand);
		mnuPerson = initMenuItem("tree.persons.by.category", str.charAt(nc++) == '1');
		mnuPerson.setIcon(IconUtil.getIconSmall(ICONS.getIconKey("ent_person")));
		mnuList.add(mnuPerson);
		mnuByGender = initMenuItem("tree.persons.by.gender", str.charAt(nc++) == '1');
		mnuByGender.setIcon(IconUtil.getIconSmall(ICONS.getIconKey("ent_person")));
		mnuList.add(mnuByGender);
		mnuLocation = initMenuItem("location", str.charAt(nc++) == '1');
		mnuList.add(mnuLocation);
		mnuItem = initMenuItem("item", str.charAt(nc++) == '1');
		mnuList.add(mnuItem);
		mnuTag = initMenuItem("tag", str.charAt(nc++) == '1');
		mnuList.add(mnuTag);
		mnuPlot = initMenuItem("plot", str.charAt(nc++) == '1');
		mnuList.add(mnuPlot);
		mnuIdea = initMenuItem("idea", str.charAt(nc++) == '1');
		mnuList.add(mnuIdea);
		for (JCheckBoxMenuItem m : mnuList) {
			popupMenu.add(m);
		}
		popupMenu.add(new JSeparator());
		mnuOptions = new JMenuItem(I18N.getMsg("options"));
		mnuOptions.setName("mnuOptions");
		mnuOptions.addActionListener(this);
		popupMenu.add(mnuOptions);
		return (popupMenu);
	}

	private JCheckBoxMenuItem initMenuItem(String title, boolean checked) {
		JCheckBoxMenuItem menu = new JCheckBoxMenuItem(I18N.getMsg(title));
		menu.setName("menu" + title);
		menu.setIcon(IconUtil.getIconSmall(ICONS.getIconKey("ent_" + title)));
		menu.setSelected(checked);
		menu.addActionListener(this);
		return (menu);
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.GAP0)));
		p.setPreferredSize(SwingUtil.getScreenSize());
		p.setOpaque(false);

		JPopupMenu popup = initPopup();
		JButton bt = SwingUtil.createButton("", ICONS.K.SUMMARY, "", true);
		bt.setComponentPopupMenu(popup);
		bt.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == 1) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		p.add(bt);
		p.add(new JLabel(" "), MIG.GROWX);
		JPanel p2 = new JPanel(new MigLayout(MIG.get(MIG.FILLX)));
		p2.setOpaque(false);
		IconButton btShowAll = new IconButton("btShowAll",
				ICONS.K.SHOW_ALL, "tree.show.all", getShowAllAction());
		btShowAll.setControlButton();
		p2.add(btShowAll, MIG.RIGHT);
		IconButton btShowNone = new IconButton("btShowNone",
				ICONS.K.SHOW_NONE, "tree.show.none", getShowNoneAction());
		btShowNone.setControlButton();
		p2.add(btShowNone);
		IconButton btExpand = new IconButton("btExpand",
				ICONS.K.EXPAND, "tree.expand.all", getExpandAction());
		btExpand.setControlButton();
		p2.add(btExpand);
		IconButton btCollapse = new IconButton("btCollapse",
				ICONS.K.SHOW_COLLAPSE, "tree.collapse.all", getCollapseAction());
		btCollapse.setControlButton();
		p2.add(btCollapse);
		p.add(p2, MIG.get(MIG.TOP, MIG.RIGHT));
		toolbar.add(p);
		return toolbar;
	}

	void treeRefresh() {
		//LOG.trace(TT + "treeRefresh()");
		tree.setFont(App.fonts.defGet());
		List<String> treeState = TreeUtil.expansionSave(tree);
		topNode.removeAllChildren();
		if (mnuPart.isSelected()) {
			partsNode = new EntityNode("parts", new Scene());
			topNode.add(partsNode);
			refreshAllScenes();
		}
		if (mnuStrand.isSelected()) {
			strandsNode = new EntityNode("strands", new Strand());
			topNode.add(strandsNode);
			refreshStrands();
		}
		if (mnuPerson.isSelected()) {
			personsByCategoryNode = new EntityNode("tree.persons.by.category", new Person());
			topNode.add(personsByCategoryNode);
			refreshPersonsByCategory();
		}
		if (mnuByGender.isSelected()) {
			personsByGendersNode = new EntityNode("tree.persons.by.gender", new Gender());
			topNode.add(personsByGendersNode);
			refreshPersonsByGender();
		}
		if (mnuLocation.isSelected()) {
			locationsNode = new EntityNode("locations", new Location());
			topNode.add(locationsNode);
			locationsRefresh();
		}
		if (mnuItem.isSelected()) {
			itemsNode = new EntityNode("items", new Item());
			topNode.add(itemsNode);
			refreshItems();
		}
		if (mnuTag.isSelected()) {
			tagsNode = new EntityNode("tags", new Tag());
			topNode.add(tagsNode);
			refreshTags();
		}
		if (mnuPlot.isSelected()) {
			plotsNode = new EntityNode("plots", new Plot());
			topNode.add(plotsNode);
			refreshPlots();
		}
		if (mnuIdea.isSelected()) {
			ideasNode = new EntityNode("ideas.title", new Idea());
			topNode.add(ideasNode);
			refreshIdeas();
			memosNode = new EntityNode("memos", new Memo());
			topNode.add(memosNode);
			refreshMemos();
		}
		treeReloadTreeModel();
		TreeUtil.expansionRestore(tree, treeState);
	}

	private void treeSaveConfig() {
		String str = "";
		str += mnuPart.isSelected() ? "1" : "0";
		//str += mnuChapter.isSelected() ? "1" : "0";
		str += mnuStrand.isSelected() ? "1" : "0";
		str += mnuPerson.isSelected() ? "1" : "0";
		str += mnuByGender.isSelected() ? "1" : "0";
		str += mnuLocation.isSelected() ? "1" : "0";
		str += mnuItem.isSelected() ? "1" : "0";
		str += mnuTag.isSelected() ? "1" : "0";
		str += mnuPlot.isSelected() ? "1" : "0";
		str += mnuIdea.isSelected() ? "1" : "0";
		App.preferences.treeviewSetShow(str);
	}

	private void treeReloadTreeModel() {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.reload();
	}

	private void refreshStrands() {
		@SuppressWarnings("unchecked")
		List<Strand> strands = mainFrame.project.strands.getList();
		for (Strand strand : strands) {
			strandsNode.add(new DefaultMutableTreeNode(strand));
		}
	}

	private DefaultMutableTreeNode partCreateNode(Map<Part, DefaultMutableTreeNode> partMap,
			Part part, DefaultMutableTreeNode root) {
		DefaultMutableTreeNode node = partMap.get(part);
		if (node == null) {
			DefaultMutableTreeNode supernode = root;
			if (part.hasSuperpart()) {
				Part superPart = part.getSuperpart();
				supernode = partCreateNode(partMap, superPart, root);
			}
			node = new DefaultMutableTreeNode(part);
			supernode.add(node);
			partMap.put(part, node);
		}
		return node;
	}

	private void refreshIdeas() {
		IdeaStatusModel stateModel = new IdeaStatusModel();
		for (AbstractStatus state : stateModel.getStates()) {
			DefaultMutableTreeNode stateNode = new DefaultMutableTreeNode(state.getName());
			ideasNode.add(stateNode);
			List<Idea> ideas = mainFrame.project.ideas.findAllByStatus(state.getNumber());
			for (Idea idea : ideas) {
				stateNode.add(new DefaultMutableTreeNode(idea));
			}
		}
	}

	private void refreshMemos() {
		@SuppressWarnings("unchecked")
		List<Memo> memos = mainFrame.project.memos.getList();
		for (Memo memo : memos) {
			memosNode.add(new DefaultMutableTreeNode(memo));
		}
	}

	private void refreshPersonsByCategory() {
		Map<Category, DefaultMutableTreeNode> categoryMap = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>) mainFrame.project.categorys.getList();
		for (Category category : categories) {
			personsGetByCategoryNodeOwner(categoryMap, category);
		}
		for (Category category : categories) {
			DefaultMutableTreeNode categoryNode = categoryMap.get(category);
			List<Person> persons = mainFrame.project.persons.findByCategory(category);
			for (Person person : persons) {
				categoryNode.add(new DefaultMutableTreeNode(person));
			}
		}
	}

	private DefaultMutableTreeNode personsGetByCategoryNodeOwner(
			Map<Category, DefaultMutableTreeNode> categoryMap, Category category) {
		DefaultMutableTreeNode categoryNode = categoryMap.get(category);
		if (categoryNode == null) {
			categoryNode = new DefaultMutableTreeNode(category);
			DefaultMutableTreeNode supCategoryNode = personsByCategoryNode;
			Category supCategory = category.getSup();
			if (supCategory != null) {
				supCategoryNode = categoryMap.get(supCategory);
				if ((supCategoryNode == null) && (!supCategory.equals(category))) {
					supCategoryNode = personsGetByCategoryNodeOwner(categoryMap, supCategory);
				}
			}
			if (supCategoryNode != null) {
				supCategoryNode.add(categoryNode);
			}
			categoryMap.put(category, categoryNode);
		}
		return categoryNode;
	}

	private void refreshPersonsByGender() {
		@SuppressWarnings("unchecked")
		List<Gender> genders = mainFrame.project.genders.getList();
		for (Gender gender : genders) {
			DefaultMutableTreeNode genderNode = new DefaultMutableTreeNode(gender);
			personsByGendersNode.add(genderNode);
			List<Person> persons = mainFrame.project.persons.findByGender(gender);
			for (Person person : persons) {
				genderNode.add(new DefaultMutableTreeNode(person));
			}
		}
	}

	private void locationsRefresh() {
		Map<Location, DefaultMutableTreeNode> sites = new HashMap<>();
		Map<String, DefaultMutableTreeNode> nodes = new HashMap<>();
		List<String> countries = mainFrame.project.locations.findCountries();
		for (String country : countries) {
			DefaultMutableTreeNode countryNode = locationsNode;
			if ((country != null && (!country.isEmpty()))) {
				if (nodes.get(country) != null) {
					countryNode = nodes.get(country);
				} else {
					CountryCategory cat1 = new CountryCategory(country);
					countryNode = new DefaultMutableTreeNode(cat1);
					locationsNode.add(countryNode);
					nodes.put(country, countryNode);
				}
			}
			List<String> cities = mainFrame.project.locations.findCitiesByCountry(country);
			for (String city : cities) {
				DefaultMutableTreeNode cityNode = countryNode;
				if (city != null && (!city.isEmpty())) {
					if (nodes.get(city) != null) {
						cityNode = nodes.get(city);
					} else {
						CityCategory cat2 = new CityCategory(city);
						cityNode = new DefaultMutableTreeNode(cat2);
						countryNode.add(cityNode);
						nodes.put(city, cityNode);
					}
				}
				List<Location> locations = mainFrame.project.locations.findByContryCity(country, city);
				for (Location location : locations) {
					DefaultMutableTreeNode node = locationInsert(location, cityNode, sites);
					nodes.put(location.getName(), node);
				}
			}
		}
	}

	private DefaultMutableTreeNode locationInsert(Location location,
			DefaultMutableTreeNode cityNode,
			Map<Location, DefaultMutableTreeNode> sites) {
		// already inserted
		if (sites.get(location) != null) {
			return sites.get(location);
		}
		DefaultMutableTreeNode locationNode = new DefaultMutableTreeNode(location);
		if (location.hasSite()) {
			DefaultMutableTreeNode siteNode = sites.get(location.getSite());
			if (siteNode == null) {
				siteNode = locationInsert(location.getSite(), cityNode, sites);
			}
			siteNode.add(locationNode);
		} else {
			cityNode.add(locationNode);
		}
		sites.put(location, locationNode);
		return locationNode;
	}

	private void refreshAllScenes() {
		//LOG.trace(TT + "refreshAllScenes()");
		refreshUnassignedScenes();
		refreshUnassignedChapters();
		//refresh the parts
		Map<Part, DefaultMutableTreeNode> partMap = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Part> parts = mainFrame.project.parts.getList();
		for (Part part : parts) {
			refreshPart(partMap, part);
		}
	}

	private void refreshUnassignedChapters() {
		//LOG.trace(TT + "refreshUnassignedChapters()");
		if (mainFrame.project.chapters.findNullPart().isEmpty()) {
			return;
		}
		DefaultMutableTreeNode unode = new DefaultMutableTreeNode(new Part());
		partsNode.add(unode);
		List<Chapter> chapters = mainFrame.project.chapters.findNullPart();
		for (Chapter chapter : chapters) {
			DefaultMutableTreeNode chapNode = new DefaultMutableTreeNode(chapter);
			unode.add(chapNode);
			for (Scene scene : mainFrame.project.scenes.find(chapter)) {
				chapNode.add(new DefaultMutableTreeNode(scene));
			}
		}
	}

	private void refreshUnassignedScenes() {
		//LOG.trace(TT + "refreshUnassignedScenes()");
		DefaultMutableTreeNode unassignedNode = new DefaultMutableTreeNode(new Chapter());
		partsNode.add(unassignedNode);
		List<Scene> scenes = mainFrame.project.scenes.findUnassigned();
		for (Scene scene : scenes) {
			unassignedNode.add(new DefaultMutableTreeNode(scene));
		}
	}

	private void refreshPart(Map<Part, DefaultMutableTreeNode> partMap, Part part) {
		DefaultMutableTreeNode partNode = partCreateNode(partMap, part, partsNode);
		List<Chapter> chapters = mainFrame.project.chapters.find(part);
		for (Chapter chapter : chapters) {
			refreshChapter(partNode, chapter);
		}
	}

	private void refreshChapter(DefaultMutableTreeNode partNode, Chapter chapter) {
		DefaultMutableTreeNode chapterNode = new DefaultMutableTreeNode(chapter);
		partNode.add(chapterNode);
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		for (Scene scene : scenes) {
			chapterNode.add(new DefaultMutableTreeNode(scene));
		}
	}

	private void refreshTags() {
		List<String> categories = mainFrame.project.tags.findCategories();
		for (String category : categories) {
			String categoryName = category;
			if (category == null || category.isEmpty()) {
				categoryName = "-";
			}
			TagCategory cat = new TagCategory(categoryName);
			DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(cat);
			tagsNode.add(categoryNode);
			@SuppressWarnings("unchecked")
			List<Tag> tags = (List) mainFrame.project.tags.findCategory(category);
			for (Tag tag : tags) {
				categoryNode.add(new DefaultMutableTreeNode(tag));
			}
		}
	}

	private void refreshItems() {
		List<String> categories = mainFrame.project.items.findCategories();
		for (String category : categories) {
			String categoryName = category;
			if (category == null || category.isEmpty()) {
				categoryName = "-";
			}
			ItemCategory cat = new ItemCategory(categoryName);
			DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(cat);
			itemsNode.add(categoryNode);
			@SuppressWarnings("unchecked")
			List<Item> items = (List) mainFrame.project.items.findCategory(category);
			for (Item item : items) {
				categoryNode.add(new DefaultMutableTreeNode(item));
			}
		}
	}

	private void refreshPlots() {
		@SuppressWarnings("unchecked")
		List<Plot> plots = mainFrame.project.plots.getList();
		for (Plot entity : plots) {
			plotsNode.add(new DefaultMutableTreeNode(entity));
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		Object value = node.getUserObject();
		if (value == null) {
			return;
		}
		if (node.isRoot()) {
			mainFrame.getBookController().infoShow(mainFrame.getProject());
		}
		if (value instanceof AbstractEntity) {
			mainFrame.getBookController().infoSetTo((AbstractEntity) value);
		}
	}

	private AbstractAction getShowAllAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (JCheckBoxMenuItem button : mnuList) {
					button.setSelected(true);
				}
				treeRefresh();
				treeSaveConfig();
			}
		};
	}

	private AbstractAction getShowNoneAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (JCheckBoxMenuItem button : mnuList) {
					button.setSelected(false);
				}
				treeRefresh();
				treeSaveConfig();
			}
		};
	}

	private AbstractAction getExpandAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
				treeSaveConfig();
			}
		};
	}

	private AbstractAction getCollapseAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				DefaultMutableTreeNode currentNode = topNode.getNextNode();
				do {
					if (currentNode.getLevel() == 1) {
						tree.collapsePath(new TreePath(currentNode.getPath()));
					}
					currentNode = currentNode.getNextNode();
				} while (currentNode != null);
				treeSaveConfig();
			}
		};
	}

	private void showPopupMenu(MouseEvent evt) {
		TreePath selectedPath = tree.getPathForLocation(evt.getX(), evt.getY());
		DefaultMutableTreeNode selectedNode = null;
		try {
			selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
		} catch (Exception e) {
			return;
		}
		if (selectedNode == null) {
			return;
		}
		Object userObj = selectedNode.getUserObject();
		if (userObj instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) userObj;
			if (entity.getId() != -1L) {
				JPopupMenu menu = EntityUtil.createPopupMenu(mainFrame, entity, EntityUtil.WITH_CHRONO);
				tree.setSelectionPath(selectedPath);
				Point p = new Point(0, 0);
				JComponent comp = (JComponent) tree.getComponentAt(evt.getPoint());
				if (comp != null) {
					p = SwingUtilities.convertPoint(comp, evt.getPoint(), this);
				}
				if (menu != null) {
					menu.show(this, p.x, p.y);
				}
				evt.consume();
			}
		}
	}

	/**
	 * mouse clicked event, only right clicked or double click
	 *
	 * @param evt
	 */
	@Override
	public void mouseClicked(MouseEvent evt) {
		//LOG.trace(TT + ".mouseClicked(evt=" + evt.toString() + ")");
		if (SwingUtilities.isRightMouseButton(evt)) {
			showPopupMenu(evt);
			return;
		}
		// double click: open show dialog for editing the node if it is a leaf
		if (evt.getClickCount() == 2) {
			TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());
			DefaultMutableTreeNode node;
			try {
				node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node == null) {
					return;
				}
				if (node.isLeaf()) {
					Object object = node.getUserObject();
					if (object instanceof AbstractEntity) {
						AbstractEntity entity = (AbstractEntity) object;
						//setting last used
						if (entity instanceof Chapter && entity.getId() != -1L) {
							mainFrame.lastChapterSet((Chapter) entity);
						}
						if (entity instanceof Scene) {
							mainFrame.lastSceneSet((Scene) entity);
						}
						//calling editor
						if (entity.getId() != -1L) {
							mainFrame.showEditorAsDialog((AbstractEntity) object);
						}
					}
				}
			} catch (Exception ex) {
				// empty
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
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

	public Tree getTree() {
		return tree;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT + ".actionPerformed(event=" + e.toString() + ")");
		String compName = ((Component) e.getSource()).getName();
		if (compName == null || compName.isEmpty()) {
			return;
		}
		if (compName.equals("mnuOptions")) {
			OptionsDlg dlg = new OptionsDlg(mainFrame, SbView.VIEWNAME.TREE.toString());
			dlg.setVisible(true);
		}
		if (!compName.startsWith("menu")) {
			return;
		}
		treeSaveConfig();
		treeRefresh();
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		//LOG.trace(TT + ".treeExpanded(event=" + event.toString() + ")");
		treeSaveConfig();
		//mainFrame.setUpdated();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		//LOG.trace(TT + ".treeCollapsed(event=" + event.toString() + ")");
		treeExpanded(event);
	}

}
