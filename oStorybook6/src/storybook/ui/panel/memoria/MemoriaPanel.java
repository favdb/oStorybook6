package storybook.ui.panel.memoria;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.Animator;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.EntityCbItem;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.dialog.OptionsDlg;
import storybook.exim.EXIM;
import storybook.renderer.EntityLCR;
import storybook.tools.LOG;
import storybook.tools.print.ComponentPrinter;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.Ui.BNONE;
import storybook.ui.panel.AbstractPanel;

public class MemoriaPanel extends AbstractPanel implements ActionListener {

    private static final String TT = "MemoriaPanel.";

    public DelegateForest<AbstractEntity, Long> graph;
    private VisualizationViewer<AbstractEntity, Long> vv;
    private TreeLayout<AbstractEntity, Long> treeLayout;
    private BalloonLayout<AbstractEntity, Long> balloonLayout;
    private SpringLayout springLayout;
    private GraphZoomScrollPane graphPanel;
    public Map<AbstractEntity, String> labelMap;
    public Map<AbstractEntity, Icon> iconMap;
    private AbstractEntity shownEntity;
    public boolean allEntities = true;
    public long graphIndex;
    private ScalingControl scaler;
    private JComboBox cbType;
    private JComboBox cbEntity;
    private String[] listType = {
	"",
	I18N.getMsg("scene"),
	I18N.getMsg("person"),
	I18N.getMsg("location"),
	I18N.getMsg("item"),
	//I18N.getMsg("itemlink"),
	I18N.getMsg("tag"),
	//I18N.getMsg("taglink"),
	I18N.getMsg("plot"),
	I18N.getMsg("relationship"),
	I18N.getMsg("strand")
    };
    private Book.TYPE[] listBookType = {
	Book.TYPE.NONE,
	Book.TYPE.SCENE,
	Book.TYPE.PERSON,
	Book.TYPE.LOCATION,
	Book.TYPE.ITEM,
	//Book.TYPE.ITEMLINK,
	Book.TYPE.TAG,
	//Book.TYPE.TAGLINK,
	Book.TYPE.PLOT,
	Book.TYPE.RELATION,
	Book.TYPE.STRAND
    };
    /*private JPanel pDate;
	public Date chosenDate = null;
	private JComboBox cbDate;*/
    private JCheckBox ckAutoRefresh;
    //icons
    public final Icon emptyIcon = IconUtil.getIconSmall(ICONS.K.EMPTY);
    //layout
    private JComboBox cbLayout;
    public JCheckBox ckAll;
    private JCheckBoxMenuItem ckItems,
	    ckLocations,
	    ckPersons, ckPlots, ckScenes, ckStrands;
    private GraphEntity graphEntity;
    private JMenuBar ckList;

    public MemoriaPanel(MainFrame paramMainFrame) {
	super(paramMainFrame);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init() {
	//LOG.trace(TT+"init()");
	try {
	    scaler = new CrossoverScalingControl();
	} catch (Exception exc2) {
	    LOG.err(TT + ".init()", exc2);
	}
    }

    @Override
    public void initUi() {
	//LOG.trace(TT+"initUi()");
	try {
	    setLayout(new MigLayout("wrap,fill, ins 0", "[]", "[][grow]"));
	    if (!LaF.isDark()) {
		setBackground(SwingUtil.getBackgroundColor());
	    }
	    add(initToolbar(), "growx, span");
	    add(initGraph(), "grow, span");
	} catch (Exception exc) {
	    LOG.err(TT + "modelPropertyChange()", exc);
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public JToolBar initToolbar() {
	super.initToolbar();
	List<Object> list = new ArrayList<>();
	list.add(I18N.getMsg("memoria.layout.balloon"));
	list.add(I18N.getMsg("memoria.layout.tree"));
	//list.add(I18N.getMsg("memoria.layout.spring"));
	cbLayout = Ui.initComboBox("cbLayout", "", list, null, !EMPTY, !ALL, this);
	cbLayout.removeActionListener(this);
	cbLayout.setSelectedIndex(App.preferences.getInteger(Pref.KEY.MEMORIA_LAYOUT));
	cbLayout.addActionListener(this);
	toolbar.add(cbLayout);
	ckAll = Ui.initCheckBox(null, "ckAll", "memoria.all",
		App.preferences.getBoolean(Pref.KEY.MEMORIA_ALL), BNONE, this);
	if (cbLayout.getSelectedIndex() != 1) {
	    ckAll.setVisible(false);
	}
	toolbar.add(ckAll);

	cbType = new JComboBox(listType);
	cbType.setName("cbType");
	cbType.setMaximumRowCount(listType.length);
	cbType.addActionListener(this);
	toolbar.add(cbType);

	cbEntity = Ui.initComboBox("cbEntity", "", (List) null, null, !EMPTY, !ALL, this);
	cbEntity.setRenderer(new EntityLCR());
	cbEntity.setVisible(false);
	toolbar.add(cbEntity);

	toolbar.add(ckList = initCkSel());
	ckList.setVisible(false);
	return (toolbar);
    }

    private JCheckBoxMenuItem initCkSelItem(String str) {
	JCheckBoxMenuItem addItem = new JCheckBoxMenuItem(I18N.getMsg(str));
	addItem.addActionListener(this);
	return addItem;
    }

    private JMenuBar initCkSel() {
	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu(I18N.getMsg("show"));
	menuBar.add(menu);
	menu.add(ckItems = initCkSelItem("items"));
	menu.add(ckLocations = initCkSelItem("locations"));
	menu.add(ckPersons = initCkSelItem("persons"));
	menu.add(ckPlots = initCkSelItem("plots"));
	menu.add(ckScenes = initCkSelItem("scenes"));
	menu.add(ckStrands = initCkSelItem("strands"));
	return menuBar;
    }

    private void setCkSel() {
	String b = App.preferences.getString(Pref.KEY.MEMORIA_SEL);
	ckItems.setSelected(b.charAt(0) == '1');
	ckLocations.setSelected(b.charAt(1) == '1');
	ckPersons.setSelected(b.charAt(2) == '1');
	ckPlots.setSelected(b.charAt(3) == '1');
	ckScenes.setSelected(b.charAt(4) == '1');
	ckStrands.setSelected(b.charAt(5) == '1');
    }

    private void changeSel() {
	StringBuilder b = new StringBuilder();
	b.append(ckItems.isSelected() ? "1" : "0");
	b.append(ckLocations.isSelected() ? "1" : "0");
	b.append(ckPersons.isSelected() ? "1" : "0");
	b.append(ckPlots.isSelected() ? "1" : "0");
	b.append(ckScenes.isSelected() ? "1" : "0");
	b.append(ckStrands.isSelected() ? "1" : "0");
	App.preferences.setString(Pref.KEY.MEMORIA_SEL, b.toString());
	refresh();
    }

    public boolean isItems() {
	return ckItems.isSelected();
    }

    public boolean isLocations() {
	return ckLocations.isSelected();
    }

    public boolean isPersons() {
	return ckPersons.isSelected();
    }

    public boolean isPlots() {
	return ckPlots.isSelected();
    }

    public boolean isScenes() {
	return ckScenes.isSelected();
    }

    public boolean isStrands() {
	return ckStrands.isSelected();
    }

    private void refreshCbEntity(AbstractEntity entity) {
	if (entity == null) {
	    return;
	}
	refreshCbEntity(entity.getObjType(), entity);
	EntityCbItem memo = new EntityCbItem(entity);
	cbEntity.setSelectedItem(memo);
    }

    @SuppressWarnings("unchecked")
    private void refreshCbEntity(Book.TYPE type, AbstractEntity toSel) {
	List list;
	switch (type) {
	    case ITEM:
		list = mainFrame.project.items.findByName();
		break;
	    case LOCATION:
		list = mainFrame.project.locations.findByName();
		break;
	    case PERSON:
		list = mainFrame.project.persons.findByName();
		break;
	    case PLOT:
		list = mainFrame.project.plots.findByName();
		break;
	    case RELATION:
		list = mainFrame.project.relations.getList();
		break;
	    case SCENE:
		list = mainFrame.project.scenes.getList();
		break;
	    case STRAND:
		list = mainFrame.project.strands.findByName();
		break;
	    case TAG:
		list = mainFrame.project.tags.findByName();
		break;
	    default:
		return;
	}
	refreshCbEntity(toSel, list);
    }

    @SuppressWarnings("unchecked")
    private void refreshCbEntity(AbstractEntity toSel, List<? extends AbstractEntity> pList) {
	cbEntity.removeActionListener(this);
	boolean b = false;
	try {
	    DefaultComboBoxModel combo = (DefaultComboBoxModel) cbEntity.getModel();
	    combo.removeAllElements();
	    combo.addElement("");
	    EntityCbItem memo = null;
	    for (AbstractEntity entity : pList) {
		b = true;
		EntityCbItem memoria = new EntityCbItem(entity);
		combo.addElement(memoria);
		if (toSel != null && toSel.equals(entity)) {
		    memo = memoria;
		}
	    }
	    if (memo != null) {
		cbEntity.setSelectedItem(memo);
	    }
	} catch (Exception exc) {
	    LOG.err(TT + ".refreshCombo error", exc);
	}
	cbEntity.setVisible(b);
	cbEntity.addActionListener(this);
    }

    @SuppressWarnings("unchecked")
    private void setLayoutGraph() {
	Dimension dim = mainFrame.getSize();
	dim = new Dimension(dim.width / 2, dim.height / 2);
	balloonLayout = new BalloonLayout(graph);
	balloonLayout.setSize(dim);
	springLayout = new SpringLayout(graph);
	springLayout.setSize(dim);
	treeLayout = new TreeLayout(graph);
    }

    @SuppressWarnings("unchecked")
    private void makeLayoutTransition() {
	if (vv == null) {
	    return;
	}
	LayoutTransition layout;
	switch (cbLayout.getSelectedIndex()) {
	    case 0:
		layout = new LayoutTransition(vv, vv.getGraphLayout(), balloonLayout);
		ckAll.setVisible(false);
		break;
	    case 1:
		layout = new LayoutTransition(vv, vv.getGraphLayout(), treeLayout);
		ckAll.setVisible(false);
		break;
	    case 2:
		layout = new LayoutTransition(vv, vv.getGraphLayout(), springLayout);
		ckAll.setVisible(true);
		break;
	    default:
		return;
	}
	Animator animator = new Animator(layout);
	animator.start();
	vv.repaint();
    }

    @SuppressWarnings("unchecked")
    private void clearGraph() {
	//LOG.trace(TT+"clearGraph()");
	try {
	    if (graph == null) {
		graph = new DelegateForest();
		return;
	    }
	    Collection collections = graph.getRoots();
	    for (Object obj : collections) {
		AbstractEntity entity = (AbstractEntity) obj;
		if (entity != null) {
		    graph.removeVertex(entity);
		}
	    }
	} catch (Exception exc) {
	    graph = new DelegateForest();
	}
	if (graphPanel != null) {
	    graphPanel.repaint();
	}
	shownEntity = null;
    }

    public void zoomIn() {
	scaler.scale(vv, 1.1F, vv.getCenter());
    }

    public void zoomOut() {
	scaler.scale(vv, 0.9090909F, vv.getCenter());
    }

    @SuppressWarnings("unchecked")
    private JPanel initGraph() {
	//LOG.trace(TT+"initGraph()");
	try {
	    labelMap = new HashMap();
	    iconMap = new HashMap();
	    graph = new DelegateForest();
	    setLayoutGraph();
	    vv = new VisualizationViewer(balloonLayout);
	    vv.setName("Memoria");
	    vv.setSize(new Dimension(800, 800));
	    if (!LaF.isDark()) {
		vv.setBackground(Color.white);
	    }
	    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
	    vv.setVertexToolTipTransformer(new EntityTransformer());
	    graphPanel = new GraphZoomScrollPane(vv);
	    DefaultModalGraphMouse mouse = new DefaultModalGraphMouse();
	    vv.setGraphMouse(mouse);
	    mouse.add(new MemoriaMouse(this));
	    VertexStringerImpl localVertexStringerImpl = new VertexStringerImpl(labelMap);
	    vv.getRenderContext().setVertexLabelTransformer(localVertexStringerImpl);
	    vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S);
	    VertexIconShapeTransformer transformer = new VertexIconShapeTransformer(new EllipseVertexShapeTransformer());
	    DefaultVertexIconTransformer iconTransformer = new DefaultVertexIconTransformer();
	    transformer.setIconMap(iconMap);
	    iconTransformer.setIconMap(iconMap);
	    vv.getRenderContext().setVertexShapeTransformer(transformer);
	    vv.getRenderContext().setVertexIconTransformer(iconTransformer);
	} catch (Exception exc) {
	    LOG.err(TT + ".initGraph error", exc);
	}
	return (graphPanel);
    }

    private void refreshGraph() {
	refreshGraph(shownEntity);
    }

    @SuppressWarnings("unchecked")
    private void refreshGraph(AbstractEntity entity) {
	try {
	    clearGraph();
	    if (entity == null) {
		return;
	    }
	    graphEntity = new GraphEntity(this, entity);
	    if (graph == null) {
		return;
	    }
	    if (shownEntity != null && !shownEntity.equals(entity)) {
		updateToolbar(entity);
	    }
	    shownEntity = entity;
	    setLayoutGraph();
	    switch (cbLayout.getSelectedIndex()) {
		case 0:
		    vv.setGraphLayout(balloonLayout);
		    ckAll.setVisible(false);
		    break;
		case 1:
		    vv.setGraphLayout(treeLayout);
		    ckAll.setVisible(false);
		    break;
		case 2:
		    vv.setGraphLayout(springLayout);
		    ckAll.setVisible(true);
		    break;
		default:
		    break;
	    }
	    vv.getGraphLayout().initialize();
	    vv.repaint();
	} catch (Exception exc) {
	    LOG.err(TT + ".refreshGraph()", exc);
	}
    }

    @SuppressWarnings("NonPublicExported")
    public void refresh(EntityCbItem toFind) {
	AbstractEntity entity;
	Long id = toFind.getId();
	switch (toFind.getType()) {
	    case ITEM:
		entity = mainFrame.project.get(Book.TYPE.ITEM, id);
		break;
	    /*case ITEMLINK:
				entity = mainFrame.project.get(Book.TYPE.ITEMLINK, id);
				break;*/
	    case LOCATION:
		entity = mainFrame.project.get(Book.TYPE.LOCATION, id);
		break;
	    case PERSON:
		entity = mainFrame.project.get(Book.TYPE.PERSON, id);
		break;
	    case PLOT:
		entity = mainFrame.project.get(Book.TYPE.PLOT, id);
		break;
	    case RELATION:
		entity = mainFrame.project.get(Book.TYPE.RELATION, id);
		break;
	    case SCENE:
		entity = mainFrame.project.get(Book.TYPE.SCENE, id);
		break;
	    case STRAND:
		entity = mainFrame.project.get(Book.TYPE.STRAND, id);
		break;
	    case TAG:
		entity = mainFrame.project.get(Book.TYPE.TAG, id);
		break;
	    /*case TAGLINK:
				entity = mainFrame.project.get(Book.TYPE.TAGLINK, id);
				break;*/
	    default:
		return;
	}
	if (entity != null) {
	    refresh(entity);
	}
    }

    public void refresh(AbstractEntity entity) {
	if (entity == null) {
	    return;
	}
	/*switch (Book.getTYPE(entity)) {
			case ITEMLINK:
			case TAGLINK:
				return;//nothing to do for these entities
			default:
				break;
		}*/
	try {
	    updateToolbar(entity);
	    refreshGraph(entity);
	} catch (Exception exc) {
	    LOG.err(TT + ".refresh(" + entity.toString() + ")", exc);
	}
    }

    private int findObjType(String type) {
	for (int i = 0; i < listType.length; i++) {
	    if (listType[i].equals(type)) {
		return (i);
	    }
	}
	return (0);
    }

    private void updateToolbar(AbstractEntity entity) {
	if (entity == null) {
	    cbEntity.removeActionListener(this);
	    cbEntity.removeAllItems();
	    cbEntity.setVisible(false);
	    cbEntity.addActionListener(this);
	}
	int oldType = cbType.getSelectedIndex();
	int newType = (entity == null ? -1 : findObjType(I18N.getMsg(entity.getObjType().toString())));
	if (oldType != newType) {
	    cbType.removeActionListener(this);
	    cbType.setSelectedIndex(newType);
	    cbType.addActionListener(this);
	    refreshCbEntity(entity);
	    return;
	}
	EntityCbItem oldMemoria = (EntityCbItem) cbEntity.getSelectedItem();
	EntityCbItem newMemoria = new EntityCbItem(entity);
	if (!newMemoria.equals(oldMemoria)) {
	    cbEntity.removeActionListener(this);
	    cbEntity.setSelectedItem(newMemoria);
	    cbEntity.addActionListener(this);
	}
    }

    public boolean hasAutoRefresh() {
	return ckAutoRefresh.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
	Object src = evt.getSource();
	if (src == null) {
	    return;
	}
	if (src instanceof JButton) {
	    return;
	}
	if (src instanceof JCheckBox) {
	    changeSel();
	    return;
	}
	if (src instanceof JComboBox) {
	    JComboBox cb = (JComboBox) src;
	    switch (cb.getName()) {
		case "cbLayout": {
		    if (cbLayout.getSelectedIndex() != 2 && ckAll != null) {
			ckAll.removeActionListener(this);
			ckAll.setSelected(false);
			ckAll.setVisible(false);
			ckAll.addActionListener(this);
		    }
		    App.preferences.setInteger(Pref.KEY.MEMORIA_LAYOUT, cbLayout.getSelectedIndex());
		    makeLayoutTransition();
		    return;
		}
		case "cbType":
		    if (cbType.getSelectedIndex() == -1) {
			return;
		    }
		    if (cbType.getSelectedIndex() == 0) {
			clearGraph();
			updateToolbar(null);
			return;
		    }
		    refreshCbEntity(listBookType[cbType.getSelectedIndex()], null);
		    clearGraph();
		    return;
		case "cbEntity":
		    if (cbEntity.getSelectedIndex() < 0) {
			return;
		    }
		    EntityCbItem memo = (EntityCbItem) cbEntity.getSelectedItem();
		    refreshGraph(mainFrame.project.get(memo.getType(), memo.getId()));
		    return;
		default:
		    LOG.err(TT + ".actionperformed(...) cb unknown !!! " + cb.getName());
		    return;
	    }
	}
	if (src instanceof JCheckBox) {
	    App.preferences.setBoolean(Pref.KEY.MEMORIA_ALL, ckAll.isSelected());
	    refreshGraph();
	}
    }

    @Override
    public void refresh() {
	refreshGraph();
    }

    @Override
    public MainFrame getMainFrame() {
	return mainFrame;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
	//LOG.trace(TT+"modelPropertyChange(evt=" + evt.toString() + ")");
	String propName = evt.getPropertyName();
	if ("SHOWINFO".equalsIgnoreCase(propName)) {
	    return;
	}
	View newView;
	View oldView;
	try {
	    if (propName == null) {
		return;
	    }
	    switch (Ctrl.getPROPS(propName)) {
		case REFRESH:
		    newView = (View) evt.getNewValue();
		    oldView = (View) getParent().getParent();
		    if (oldView == newView) {
			refresh();
		    }
		    return;
		case SHOWINMEMORIA:
		    AbstractEntity entity = (AbstractEntity) evt.getNewValue();
		    refresh(entity);
		    return;
		case SHOWOPTIONS:
		    newView = (View) evt.getNewValue();
		    if (newView.getName().equals(SbView.VIEWNAME.MEMORIA.toString())) {
			OptionsDlg.show(mainFrame, newView.getName());
		    }
		    return;
		case MEMORIA_LAYOUT:
		    cbLayout.removeActionListener(this);
		    cbLayout.setSelectedIndex((int) evt.getNewValue());
		    cbLayout.addActionListener(this);
		    makeLayoutTransition();
		    return;
		case EXPORT:
		    newView = (View) evt.getNewValue();
		    oldView = (View) getParent().getParent();
		    if (newView == oldView) {
			EXIM.exporter(mainFrame, vv);
		    }
		    return;
		case PRINT:
		    newView = (View) evt.getNewValue();
		    if (newView.getName().equals(SbView.VIEWNAME.MEMORIA.toString())) {
			printAction();
		    }
		    return;
		default:
		    if ((propName.startsWith("Update"))
			    || (propName.startsWith("Delete"))
			    || (propName.startsWith("New"))) {
			refresh();
		    }
		    break;
	    }
	    ActKey act = new ActKey(evt);
	    switch (Book.getTYPE(act.type)) {
		case SCENE:
		case PERSON:
		case LOCATION:
		case ITEM:
		//case ITEMLINK:
		case TAG:
		//case TAGLINK:
		case RELATION:
		    if (Ctrl.PROPS.NEW.check(act.getCmd())
			    || Ctrl.PROPS.UPDATE.check(act.getCmd())
			    || Ctrl.PROPS.DELETE.check(act.getCmd())) {
			refresh();
		    }
		    break;
		default:
		    break;
	    }
	} catch (Exception exc) {
	    LOG.err(TT + "modelPropertyChange(" + evt.toString() + ") exception", exc);
	}
    }

    public JPanel getPanelToExport() {
	return (vv);
    }

    private void printAction() {
	ComponentPrinter.pr(mainFrame, vv, book.getTitle(), "");
    }

    public class MemoriaMouse extends AbstractPopupGraphMousePlugin implements MouseListener {

	public static final String ACTION_KEY_DB_OBECT = "DbObject";
	private MemoriaPanel parent;

	public MemoriaMouse(MemoriaPanel parent) {
	    this.parent = parent;
	}

	public MemoriaMouse() {
	    this(4);
	}

	public MemoriaMouse(int i) {
	    super(i);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void handlePopup(MouseEvent evt) {
	    VisualizationViewer vvw = (VisualizationViewer) evt.getSource();
	    Point point = evt.getPoint();
	    GraphElementAccessor accel = vvw.getPickSupport();
	    if (accel != null) {
		AbstractEntity entity
			= (AbstractEntity) accel.getVertex(vvw.getGraphLayout(), point.getX(), point.getY());
		if (entity != null) {
		    JPopupMenu localJPopupMenu
			    = EntityUtil.createPopupMenu(this.parent.getMainFrame(), entity, EntityUtil.WITH_CHRONO);
		    localJPopupMenu.show(vvw, evt.getX(), evt.getY());
		}
	    }
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mouseClicked(MouseEvent evt) {
	    if (evt.getClickCount() == 2) {
		VisualizationViewer viewer = (VisualizationViewer) evt.getSource();
		Point point = evt.getPoint();
		GraphElementAccessor accel = viewer.getPickSupport();
		if (accel != null) {
		    AbstractEntity entity
			    = (AbstractEntity) accel.getVertex(viewer.getGraphLayout(), point.getX(), point.getY());
		    if (entity != null) {
			this.parent.refresh(entity);
		    }
		}
	    }
	    super.mouseClicked(evt);
	}

	public MemoriaPanel getParent() {
	    return this.parent;
	}
    }

}
