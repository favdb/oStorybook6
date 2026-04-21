package storybook.decorator;

import api.mig.swing.MigLayout;
import api.shef.tools.LOG;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.strand.Strand;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panels.AbstractPanel;

@SuppressWarnings("serial")
public class CheckBoxPanel extends AbstractPanel implements IRefreshable {

	private static final String TT = "CheckBoxPanel.";

	public final Map<AbstractEntity, JCheckBox> cbMap;
	private CbPanelDecorator decorator;
	private AbstractEntity entity;
	public List<AbstractEntity> entities = new ArrayList<>();
	private AbstractEntity ommit = null;
	private final Book.TYPE type;
	public boolean autoSelect = true;
	private AbstractEntity pointedEntity;

	public CheckBoxPanel(MainFrame mainFrame, Book.TYPE type, AbstractEntity ommit) {
		this.mainFrame = mainFrame;
		this.type = type;
		this.ommit = ommit;
		cbMap = new TreeMap<>();
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		cbMap.clear();
		List<AbstractEntity> allEntities = null;
		if (type != null) {
			switch (type) {
				case ITEM:
					allEntities = (List) mainFrame.project.items.findByCategory();
					break;
				case LOCATION:
					allEntities = (List) mainFrame.project.locations.findOrdered();
					break;
				case PERSON:
					allEntities = (List) mainFrame.project.persons.findByCategory();
					break;
				case PLOT:
					allEntities = (List) mainFrame.project.plots.getList();
					break;
				case STRAND:
					allEntities = (List) mainFrame.project.strands.getList((Strand) ommit);
					break;
				default:
					break;
			}
		}
		if (allEntities != null) {
			for (AbstractEntity entity2 : allEntities) {
				addEntity(entity2);
			}
		} else {
			LOG.err(TT + "init() allEntities is null");
		}
		// refresh entities, must be before selectEntity()
		refresh();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap"));
		//setBackground(Color.white);
		refresh();
	}

	@Override
	public void refresh() {
		removeAll();
		if (decorator != null) {
			decorator.decorateBeforeFirstEntity();
		}
		Iterator<Entry<AbstractEntity, JCheckBox>> it = cbMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<AbstractEntity, JCheckBox> pairs = (Map.Entry<AbstractEntity, JCheckBox>) it.next();
			AbstractEntity ent = pairs.getKey();
			if (decorator != null) {
				decorator.decorateBeforeEntity(ent);
				decorator.decorateEntity(pairs.getValue(), ent);
			} else {
				add(pairs.getValue(), "split 2");
				add(new JLabel(ent.getIcon()));
			}
			if (decorator != null) {
				decorator.decorateAfterEntity(ent);
			}
		}
		revalidate();
		repaint();
	}

	public void selectEntities(List<?> entities) {
		for (Object ent : entities) {
			JCheckBox cb = cbMap.get((AbstractEntity) ent);
			if (cb != null) {
				cb.setSelected(true);
			}
		}
	}

	public void selectEntity(AbstractEntity ent) {
		JCheckBox cb = cbMap.get(ent);
		if (cb != null) {
			cb.setSelected(true);
		}
	}

	public List<AbstractEntity> getSelectedEntities() {
		List<AbstractEntity> ret = new ArrayList<>();
		Iterator<Entry<AbstractEntity, JCheckBox>> it = cbMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<AbstractEntity, JCheckBox> pairs = (Map.Entry<AbstractEntity, JCheckBox>) it.next();
			JCheckBox cb = pairs.getValue();
			if (cb.isSelected()) {
				ret.add(pairs.getKey());
			}
		}
		return ret;
	}

	public void addEntities(List<AbstractEntity> entities) {
		this.removeAll();
		for (AbstractEntity e : entities) {
			addEntity(e);
		}
	}

	public void addEntity(AbstractEntity entity) {
		JCheckBox ck = new JCheckBox();
		ck.setOpaque(false);
		ck.addActionListener(evt -> pointedEntity = entity);
		cbMap.put(entity, ck);
		ck.setText(entity.toString());
		add(ck);
		add(new JLabel(entity.getIcon()));
	}

	public AbstractEntity getPointedEntity() {
		return (pointedEntity);
	}

	public CbPanelDecorator getDecorator() {
		return decorator;
	}

	public void setDecorator(CbPanelDecorator decorator) {
		this.decorator = decorator;
	}

	@Override
	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public AbstractEntity getEntity() {
		return entity;
	}

	public void setEntity(AbstractEntity entity) {
		this.entity = entity;
	}

	public void setEntityList(List<AbstractEntity> entities) {
		this.entities = entities;
	}

	public boolean getAutoSelect() {
		return autoSelect;
	}

	public void setAutoSelect(boolean flag) {
		this.autoSelect = flag;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
