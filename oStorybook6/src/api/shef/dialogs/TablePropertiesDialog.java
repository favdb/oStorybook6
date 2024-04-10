/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.editors.wys.HTMLElementPropertiesAction;
import i18n.I18N;

public class TablePropertiesDialog extends OptionDialog {

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.TABLE);
	private static String title = I18N.getMsg("shef.table_properties");
	private static String desc = I18N.getMsg("shef.table_properties_desc");
	private TableAttributesPanel tableProps = new TableAttributesPanel();
	private RowAttributesPanel rowProps = new RowAttributesPanel();
	private CellAttributesPanel cellProps = new CellAttributesPanel();
	public boolean doRemoveTable;

	public TablePropertiesDialog(Frame parent) {
		super(parent, title, desc, icon);
		init();
	}

	public TablePropertiesDialog(Dialog parent, HTMLElementPropertiesAction elemProp) {
		super(parent, title, desc, icon);
		init();
	}

	public TablePropertiesDialog(Frame parent, HTMLElementPropertiesAction elemProp) {
		super(parent, title, desc, icon);
		init();
	}

	private void init() {
		Border emptyBorder = new EmptyBorder(5, 5, 5, 5);
		Border titleBorder = BorderFactory.createTitledBorder(I18N.getMsg("shef.table_properties"));
		tableProps.setBorder(BorderFactory.createCompoundBorder(emptyBorder, titleBorder));
		rowProps.setBorder(emptyBorder);
		cellProps.setBorder(emptyBorder);
		JTabbedPane tabs = new JTabbedPane();
		tabs.add(tableProps, I18N.getMsg("shef.table"));
		tabs.add(rowProps, I18N.getMsg("shef.row"));
		tabs.add(cellProps, I18N.getMsg("shef.cell"));
		JButton suppr = new JButton(I18N.getMsg("shef.table.remove"));
		suppr.addActionListener((ActionEvent evt) -> {
			doRemoveTable = true;
			dispose();
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(tabs, BorderLayout.NORTH);
		panel.add(suppr, BorderLayout.SOUTH);
		setContentPane(panel);
		setMinimumSize(new Dimension(450, 400));
		pack();
	}

	public void setTableAttributes(Map at) {
		tableProps.setAttributes(at);
	}

	public void setRowAttributes(Map at) {
		rowProps.setAttributes(at);
	}

	public void setCellAttributes(Map at) {
		cellProps.setAttributes(at);
	}

	public Map getTableAttributes() {
		return tableProps.getAttributes();
	}

	public Map getRowAttribures() {
		return rowProps.getAttributes();
	}

	public Map getCellAttributes() {
		return cellProps.getAttributes();
	}

}
