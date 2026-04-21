package storybook.tools.swing.js;

import api.mig.swing.MigLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.Book.TYPE;
import storybook.db.strand.Strand;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panels.AbstractPanel;

/**
 * Panel containing a list of check box
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class JSCheckList extends AbstractPanel implements IRefreshable {

	private static final String TT = "CkListPanel";

	public List<EntityCB> ckList;
	private AbstractEntity entity, pointedEntity;
	public List<AbstractEntity> entities;
	public boolean autoSel = true;
	private Book.TYPE type = Book.TYPE.NONE;
	private AbstractEntity toOmmit = null;

	public JSCheckList(MainFrame mainFrame, Book.TYPE type) {
		super(mainFrame);
		this.type = type;
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void init() {
		//LOG.trace(TT+".init() for search="+search);
		ckList = new ArrayList<>();
		reload();
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP, MIG.INS0, MIG.GAP0)));
		setMaximumSize(new Dimension(1024, 1024));
		refresh();
	}

	public void reload() {
		//LOG.trace(TT+".reload()");
		ckList.clear();
		@SuppressWarnings("unchecked")
		List<AbstractEntity> ls = (List) mainFrame.project.getList(type);
		if (ls == null) {
			return;
		}
		ls.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
		ls.forEach(obj -> {
			if (!obj.equals(toOmmit)) {
				addEntity((AbstractEntity) obj);
			}
		});
	}

	public void addEntity(AbstractEntity entity) {
		//LOG.trace(TT+".addEntity(entity="+LOG.traceEntity(entity)+")");
		EntityCB ckb = new EntityCB(entity);
		ckb.getCB().addActionListener((ActionEvent evt) -> {
			pointedEntity = entity;
		});
		ckList.add(ckb);
		add(ckb.getCB());
	}

	public void removeEntity(AbstractEntity entity) {
		//LOG.trace(TT+".removeEntity(entity="+LOG.traceEntity(entity)+")");
		int z = -1;
		if (entity == null) {
			return;
		}
		for (int i = 0; i < ckList.size(); i++) {
			if (entity.equals(ckList.get(i).entity)) {
				z = i;
			}
		}
		if (z != -1) {
			ckList.remove(z);
			refresh();
			toOmmit = entity;
		}
	}

	@Override
	public void refresh() {
		//LOG.trace(TT+".refresh()");
		removeAll();
		ckList.forEach(ecb -> {
			JLabel lb = new JLabel();
			Icon icon = ecb.entity.getIcon(App.fonts.defGet().getSize());
			if (ecb.entity.getObjType() == TYPE.ATTRIBUTE) {
				icon = null;
			}
			lb.setIcon(icon);
			if (ecb.entity.getObjType() == TYPE.STRAND) {
				lb.setBackground(((Strand) ecb.entity).getJColor());
			}
			add(ecb.cb, MIG.SPLIT2);
			add(lb);
		});
		revalidate();
		repaint();
	}

	public void selectEntity(AbstractEntity entity) {
		//LOG.trace(TT+".selectEntity(entity="+LOG.traceEntity(entity)+")");
		JCheckBox cb = findCB(entity);
		if (cb != null) {
			cb.setSelected(true);
		}
	}

	private JCheckBox findCB(AbstractEntity ent) {
		//LOG.trace(TT+".findCB(entity="+LOG.traceEntity(entity)+")");
		for (EntityCB ecb : ckList) {
			if (ecb.entity.getId().equals(ent.getId())) {
				return ecb.cb;
			}
		}
		return (null);
	}

	public void setSelectedEntities(List<AbstractEntity> entities) {
		for (EntityCB ecb : ckList) {
			ecb.cb.setSelected(false);
		}
		entities.forEach((e) -> selectEntity(e));
	}

	public List<AbstractEntity> getSelectedEntities() {
		//LOG.trace(TT+".getSelectedEntities()");
		ArrayList<AbstractEntity> ret = new ArrayList<>();
		ckList.forEach((ecb) -> {
			if (ecb.cb.isSelected()) {
				ret.add(ecb.entity);
			}
		});
		return ret;
	}

	public AbstractEntity getPointedEntity() {
		return pointedEntity;
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

	public Book.TYPE getType() {
		return type;
	}

	public void setType(Book.TYPE type) {
		this.type = type;
	}

	public boolean getAutoselect() {
		return autoSel;
	}

	public void setAutoselect(boolean flag) {
		this.autoSel = flag;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public static class EntityCB {

		AbstractEntity entity;
		JCheckBox cb;

		public EntityCB(AbstractEntity entity) {
			this.entity = entity;
			this.cb = new JCheckBox();
			this.cb.setText(entity.toString());
		}

		public EntityCB(AbstractEntity entity, JCheckBox cb) {
			this.entity = entity;
			this.cb = cb;
		}

		public JCheckBox getCB() {
			return cb;
		}

	}

}
