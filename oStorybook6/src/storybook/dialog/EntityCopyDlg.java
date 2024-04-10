/*
 * Copyright (C) 2017 favdb
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
 * You should have received a copyCategory of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.*;
import storybook.db.category.Category;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.tools.ListUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 * copy elements (person, location, item or idea) from another project into the current one
 *
 * @author favdb
 */
public class EntityCopyDlg extends AbsDialog {

	private static final String TT = "EntityCopyDlg";

	private MainFrame fromFrame = null;//the from project
	private JComboBox<MainFrame> fromCombo;
	private Book.TYPE entityType = Book.TYPE.PERSON;
	private JComboBox<Book.TYPE> cbEntityType;
	private List<AbstractEntity> entities;
	private final Book.TYPE[] entityAllowed = {
		PERSON,
		LOCATION,
		ITEM,
		IDEA
	};
	private JPanel dataPanel;// panel for select the entities to copy
	private List<JCheckBox> entitiesCk;// checkbox to selected entities to copy
	private JPanel entitiesPanel;// panel to select the entities to copy
	private JButton okButton;

	public EntityCopyDlg(MainFrame m) {
		super(m);
		initAll();
	}

	/**
	 * show the dialog
	 *
	 * @param m
	 */
	public static void show(MainFrame m) {
		EntityCopyDlg dlg = new EntityCopyDlg(m);
		dlg.setVisible(true);
	}

	/**
	 * base initialization
	 */
	@Override
	public void init() {
		entityType = Book.TYPE.PERSON;
	}

	/**
	 * initialize the user interface
	 */
	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.get(MIG.WRAP), "", "[grow][]"));
		setTitle(I18N.getMsg("copy.title"));
		setIconImage(IconUtil.getIconImage("icon"));
		setPreferredSize(new Dimension(800, 600));
		add(initFromPanel(), MIG.GROWX);
		add(initEntitiesPanel(), MIG.GROWX);
		okButton = getOkButton();
		okButton.setEnabled(false);
		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.SG, MIG.RIGHT));
		add(okButton, MIG.SG);
		reloadEntities();
	}

	/**
	 * initialize the from project panel
	 *
	 * @return
	 */
	private JPanel initFromPanel() {
		JPanel panel = new JPanel(new MigLayout(MIG.WRAP, "[][][]"));
		panel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("copy.from")));
		panel.add(new JLabel(I18N.getColonMsg("copy.opened.projects")));
		DefaultComboBoxModel<MainFrame> model = new DefaultComboBoxModel<>();
		for (MainFrame frame : App.getInstance().getMainFrames()) {
			if (!mainFrame.getBook().getTitle().equals(frame.getBook().getTitle())) {
				model.addElement(frame);
			}
		}
		fromCombo = new JComboBox<>(model);
		fromCombo.setRenderer(new ProjectComboRenderer());
		panel.add(fromCombo, MIG.GROWX);
		panel.add(Ui.initButton("btOpen", "", ICONS.K.F_OPEN,
		   "copy.open.project", e -> openProjectAction()));
		if (fromCombo.getItemCount() > 0) {
			fromFrame = (MainFrame) fromCombo.getItemAt(0);
		}
		fromCombo.addItemListener(e -> changeFrom());
		return panel;
	}

	/**
	 * change action for the from project
	 */
	@SuppressWarnings("unchecked")
	private void changeFrom() {
		//LOG.printInfos(TT + ".changeFrom()");
		fromFrame = (MainFrame) fromCombo.getSelectedItem();
		reloadEntities();
	}

	/**
	 * initialize the entities panel
	 *
	 * @return the panel
	 */
	@SuppressWarnings("unchecked")
	private JPanel initEntitiesPanel() {
		//LOG.printInfos(TT + ".initEntitiesPanel()");
		cbEntityType = new JComboBox<>();
		cbEntityType.setRenderer(new EntityTypeRenderer());
		cbEntityType.setName("cbEntityType");
		for (Book.TYPE e : entityAllowed) {
			cbEntityType.addItem(e);
		}
		cbEntityType.addItemListener(e -> changeType());
		cbEntityType.setSelectedItem(entityType);
		add(cbEntityType, MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		JButton btSelectAll = new JButton(IconUtil.getIconSmall(ICONS.K.SHOW_ALL));
		btSelectAll.setToolTipText(I18N.getMsg("select.all"));
		btSelectAll.addActionListener(e -> selectAll(true));
		add(btSelectAll, MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		JButton btSelectNotAll = new JButton(IconUtil.getIconSmall(ICONS.K.SHOW_NONE));
		btSelectNotAll.setToolTipText(I18N.getMsg("select.notall"));
		btSelectNotAll.addActionListener(e -> selectAll(false));
		add(btSelectNotAll);
		dataPanel = new JPanel(new MigLayout());
		//dataPanel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("copy.elements")));
		entitiesPanel = new JPanel(new MigLayout(MIG.WRAP1));
		entitiesCk = new ArrayList<>();
		JScrollPane scroller = new JScrollPane(entitiesPanel);
		SwingUtil.setUnitIncrement(scroller);
		SwingUtil.setMaxPreferredSize(scroller);
		dataPanel.add(scroller, MIG.GROW);
		entities = new ArrayList<>();
		if (fromFrame != null) {
			entities = (List) fromFrame.project.getList(entityType);
		}
		return dataPanel;
	}

	private void selectAll(boolean all) {
		for (JCheckBox ck : entitiesCk) {
			ck.setSelected(all);
		}
	}

	/**
	 * execute the copy function and close the dialog
	 *
	 * @return the action
	 */
	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (copySelectedEntities()) {
					canceled = false;
					dispose();
				}
			}

		};
	}

	/**
	 * open another project
	 */
	public void openProjectAction() {
		mainFrame.cursorSetWaiting();
		//App.getInstance().openProject();
		App.getInstance().selectProject();
		SwingUtilities.invokeLater(() -> {
			DefaultComboBoxModel<MainFrame> model
			   = (DefaultComboBoxModel<MainFrame>) fromCombo.getModel();
			model.removeAllElements();
			for (MainFrame frame : App.getInstance().getMainFrames()) {
				if (frame != mainFrame) {
					model.addElement(frame);
				}
			}
			fromCombo.setSelectedIndex(fromCombo.getItemCount() - 1);
			mainFrame.requestFocus();
			mainFrame.cursorSetDefault();
			pack();
			setLocationRelativeTo(mainFrame);
		});
	}

	/**
	 * change action for the type combobox
	 */
	@SuppressWarnings("unchecked")
	private void changeType() {
		//LOG.printInfos(TT + ".changeType()");
		entityType = (Book.TYPE) cbEntityType.getSelectedItem();
		if (entityType == null) {
			entities = new ArrayList<>();
		} else {
			entities = (List) fromFrame.project.getList(entityType);
		}
		reloadEntities();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	/**
	 * reload the entities to select
	 */
	@SuppressWarnings("unchecked")
	private void reloadEntities() {
		//LOG.printInfos(TT + ".reloadEntities()");
		entitiesPanel.removeAll();
		if (fromCombo.getItemCount() < 1) {
			return;
		}
		fromFrame = (MainFrame) fromCombo.getSelectedItem();
		entityType = (Book.TYPE) cbEntityType.getSelectedItem();
		entities = (List) fromFrame.project.getList(entityType);
		for (AbstractEntity ent : entities) {
			JCheckBox ck = new JCheckBox();
			ck.addItemListener(e -> enableOkButton());
			ck.setText(ent.toString());
			entitiesCk.add(ck);
			entitiesPanel.add(ck);
		}
		enableOkButton();
		pack();
		setLocationRelativeTo(mainFrame);
	}

	/**
	 * enable/disable the OK button
	 */
	private void enableOkButton() {
		int n = 0;
		for (JCheckBox ck : entitiesCk) {
			if (ck.isSelected()) {
				n++;
				break;
			}
		}
		okButton.setEnabled(n > 0);
	}

	/**
	 * renderer clas for entity type
	 */
	private static class EntityTypeRenderer extends DefaultListCellRenderer {

		public EntityTypeRenderer() {
		}

		@Override
		public Component getListCellRendererComponent(JList list,
		   Object value, int index, boolean sel, boolean focus) {
			JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
			if (value instanceof Book.TYPE) {
				lb.setText(I18N.getMsg(((Book.TYPE) value).toString()));
			}
			return lb;
		}
	}

	/**
	 * render class for projects
	 */
	class ProjectComboRenderer extends JSLabel implements ListCellRenderer<MainFrame> {

		public ProjectComboRenderer() {
			super("");
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends MainFrame> list,
		   MainFrame value,
		   int index,
		   boolean isSelected,
		   boolean cellHasFocus) {
			if (value instanceof MainFrame) {
				MainFrame frame = (MainFrame) value;
				String title = frame.getBook().getTitle();
				if (title.isEmpty()) {
					title = "empty title";
				}
				setText(title);
			}
			return this;
		}
	}

	/**
	 * check if the entities name to copy are not present in the current project
	 *
	 * @return
	 */
	private boolean checkEntities() {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < entities.size(); i++) {
			if (entitiesCk.get(i).isSelected()) {
				AbstractEntity ent = entities.get(i);
				if (mainFrame.project.findByName(entityType, ent.getName()) != null) {
					list.add(ent.getName());
				}
			}
		}
		if (!list.isEmpty()) {
			MessageDlg.show(this, I18N.getMsg("copy.error", ListUtil.join(list)),
			   "copy", true);
			return false;
		}
		return true;
	}

	/**
	 * copy the selected entities
	 *
	 * @return
	 */
	private boolean copySelectedEntities() {
		try {
			if (!checkEntities()) {
				return false;
			}
			for (int i = 0; i < entities.size(); i++) {
				if (entitiesCk.get(i).isSelected()) {
					AbstractEntity ent = copyEntity(entities.get(i));
					mainFrame.getBookController().newEntity(ent);
				}
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * copy common informations for an entity
	 *
	 * @param in
	 * @param out
	 */
	private void copyCommon(AbstractEntity in, AbstractEntity out) {
		out.setName(in.getName());
		out.setDescription(in.getDescription());
		out.setNotes(in.getNotes());
		out.setAssistant(in.getAssistant());
	}

	/**
	 * copy an entity
	 *
	 * @param in
	 * @return
	 */
	private AbstractEntity copyEntity(AbstractEntity in) {
		AbstractEntity out;
		switch (in.getObjType()) {
			case PERSON:
				out = copyPerson((Person) in);
				break;
			case LOCATION:
				out = copyLocation((Location) in);
				break;
			case ITEM:
				out = copyItem((Item) in);
				break;
			case IDEA:
				out = copyIdea((Idea) in);
				break;
			default:
				return null;
		}
		copyCommon(in, out);
		return out;
	}

	/**
	 * copy the Person entity
	 *
	 * @param in
	 * @return
	 */
	private Person copyPerson(Person in) {
		Person out = new Person();
		Category minor = (Category) mainFrame.project.get(Book.TYPE.CATEGORY, 1L);
		out.setCategory(minor);
		out.setGender(in.getGender());
		out.setFirstname(in.getFirstname());
		out.setLastname(in.getLastname());
		out.setAbbreviation(in.getAbbreviation());
		out.setBirthday(in.getBirthday());
		out.setDayofdeath(in.getDayofdeath());
		out.setOccupation(in.getOccupation());
		out.setColor(in.getColor());
		out.setCategories(new ArrayList<>());
		out.setAttributes(new ArrayList<>());
		return out;
	}

	/**
	 * copy the Location entity
	 *
	 * @param in
	 * @return
	 */
	private Location copyLocation(Location in) {
		Location out = new Location();
		out.setAddress(in.getAddress());
		out.setCity(in.getCity());
		out.setCountry(in.getCountry());
		out.setAltitude(in.getAltitude());
		out.setGps(in.getGps());
		return out;
	}

	/**
	 * copy the Item entity
	 *
	 * @param in
	 * @return
	 */
	private Item copyItem(Item in) {
		Item out = new Item();
		out.setType(in.getType());
		out.setCategory(in.getCategory());
		return out;
	}

	/**
	 * copy the Idea entity
	 *
	 * @param in
	 * @return
	 */
	private Idea copyIdea(Idea in) {
		Idea out = new Idea();
		out.setStatus(in.getStatus());
		in.setCategory(in.getCategory());
		return out;
	}

}
