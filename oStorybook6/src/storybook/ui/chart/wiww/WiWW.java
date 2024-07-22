package storybook.ui.chart.wiww;

import i18n.I18N;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.location.Location;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.project.Project;
import storybook.tools.DateUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.ReadOnlyTable;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.table.TableFixedColumn;
import storybook.ui.MainFrame;
import storybook.ui.chart.AbstractPersonsChart;
import storybook.ui.chart.legend.PersonsLegendPanel;

public class WiWW extends AbstractPersonsChart implements ChangeListener {

    private static final String TT = "WiWW.";
    private Set<Person> foundCharacters;
    public JTable table;
    private JSlider colSlider;
    private int colWidth = 50;

    public WiWW(MainFrame m) {
	super(m, "report.person.location.time.title");
	this.partRelated = true;
	this.needsFullRefresh = true;
    }

    @Override
    protected void initChart() {
	super.initChart();
	this.foundCharacters = new TreeSet<>();
    }

    @Override
    protected void initChartUi() {
	JLabel label = new JLabel(this.chartTitle);
	label.setFont(FontUtil.getBold());
	table = createTable();
	table.setName(chartTitle);
	TableFixedColumn fixed = new TableFixedColumn(this.table, 2);
	fixed.getRowHeader().setPreferredSize(new Dimension(300, 20));
	panel.add(label, "center");
	panel.add(fixed, "grow, h pref-40");
	panel.add(new PersonsLegendPanel(this.mainFrame, this.foundCharacters), "gap push");
    }

    @Override
    protected void initOptionsUi() {
	super.initOptionsUi();
	JLabel lb = new JLabel(IconUtil.getIconSmall(ICONS.K.SIZE));
	colSlider = SwingUtil.createSafeSlider(0, 5, 300, this.colWidth);
	colSlider.setMinorTickSpacing(1);
	colSlider.setMajorTickSpacing(2);
	colSlider.setSnapToTicks(false);
	colSlider.addChangeListener(this);
	colSlider.setOpaque(false);
	optionsPanel.add(lb, "gap push,right");
	optionsPanel.add(this.colSlider);
    }

    @Override
    public void refresh() {
	this.colWidth = this.colSlider.getValue();
	super.refresh();
	this.colSlider.setValue(this.colWidth);
	setTableColumnWidth();
    }

    @SuppressWarnings("unchecked")
    private JTable createTable() {
	//LOG.trace(TT + "createTable()");
	Project project = mainFrame.project;
	Part part = getCbPart();
	List<Person> persons = project.persons.findByCategories(this.selectedCategories);
	List<Date> dates = project.scenes.findDistinctDates(part);
	List<Location> locations = (List) project.locations.getList();
	List<String> dateColNames = new ArrayList<>();
	dateColNames.add(I18N.getMsg("location"));
	dateColNames.add(I18N.getMsg("country"));
	for (Date date : dates) {
	    dateColNames.add(DateUtil.simpleDateTimeToString(date, false));
	}
	Object[] colNames = dateColNames.toArray();
	this.foundCharacters.clear();
	ArrayList list = new ArrayList();
	for (Location location : locations) {
	    Object[] obj2 = new Object[colNames.length];
	    int j = 0;
	    obj2[(j++)] = location.getName();
	    obj2[(j++)] = location.getCountryCity();
	    int m = 0;
	    for (Date dt : dates) {
		if (dt == null) {
		    continue;
		}
		WiWWContainer container = new WiWWContainer(this.mainFrame, dt, location, persons);
		obj2[j] = container;
		if (container.isFound()) {
		    this.foundCharacters.addAll(container.getCharacterList());
		    m = 1;
		}
		j++;
	    }
	    if (m != 0) {
		list.add(obj2);
	    }
	}
	ReadOnlyTable tbl = new ReadOnlyTable((Object[][]) list.toArray(new Object[0][]), colNames);
	for (int k = 2; k < tbl.getColumnCount(); k++) {
	    TableColumn tcol = tbl.getColumnModel().getColumn(k);
	    tcol.setPreferredWidth(120);
	    tcol.setCellRenderer(new WiWWTableCellRenderer());
	}
	tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tbl.getTableHeader().setReorderingAllowed(false);
	return tbl;
    }

    @Override
    public void stateChanged(ChangeEvent paramChangeEvent) {
	setTableColumnWidth();
    }

    private void setTableColumnWidth() {
	this.colWidth = this.colSlider.getValue();
	for (int i = 0; i < this.table.getColumnCount(); i++) {
	    TableColumn column = this.table.getColumnModel().getColumn(i);
	    column.setPreferredWidth(this.colWidth);
	}
    }

    @Override
    public JPanel getPanelToExport() {
	//TODO à faire
	return (null);
    }

}
