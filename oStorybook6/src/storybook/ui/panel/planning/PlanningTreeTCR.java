package storybook.ui.panel.planning;

import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.tools.html.Html;
import storybook.tools.swing.FontUtil;

/**
 * class for renderer of a Planning element
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class PlanningTreeTCR extends DefaultTreeCellRenderer {

	private static final int BARSIZE = 200, DELTA = FontUtil.getHeight();
	private int maxval, currentval;
	private final JLabel textLabel, percentLabel;

	public PlanningTreeTCR() {
		setLayout(null);
		setBackground(UIManager.getColor("Tree.textBackground"));
		textLabel = new JLabel("Test");
		textLabel.setFont(UIManager.getFont("Tree.font"));
		add(textLabel);
		percentLabel = new JLabel("Test");
		percentLabel.setFont(UIManager.getFont("Tree.font"));
		add(percentLabel);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = textLabel.getPreferredSize();
		if (maxval > 0) {
			size.width += BARSIZE + 2 * DELTA + percentLabel.getPreferredSize().width;
		}
		return size;
	}

	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		textLabel.setBounds(0, 0, textLabel.getPreferredSize().width, textLabel.getPreferredSize().height);
		percentLabel.setBounds(
			w - percentLabel.getPreferredSize().width,
			0, percentLabel.getPreferredSize().width,
			percentLabel.getPreferredSize().height);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean sel, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object uobj = node.getUserObject();
		textLabel.setText(uobj.toString());
		maxval = -1;
		currentval = 0;
		int percent;
		if (uobj instanceof PlanningTreeItem) {
			PlanningTreeItem item = (PlanningTreeItem) uobj;
			textLabel.setText(item.toString());
			maxval = item.max;
			if (item.type == 0) {
				textLabel.setIcon(UIManager.getIcon("Tree.closedIcon"));// default icon for book
			} else {
				AbstractEntity entity = item.entity;
				if (entity instanceof Scene) {
					textLabel.setIcon(((Scene) entity).getScenestate().getIcon());
				} else {
					textLabel.setIcon(entity.getIcon());
				}
				if (item.entity instanceof Part) {
					maxval = Math.max(currentval, ((Part) item.entity).getObjectiveChars());
				} else if (item.entity instanceof Chapter) {
					maxval = Math.max(currentval, ((Chapter) item.entity).getObjectiveChars());
				}
			}

			setToolTipText(setNotes(item.entity));
			currentval = 0;
			if (maxval == 0) {
				textLabel.setText(textLabel.getText() + " (" + I18N.getMsg("objective.no") + ")");
				percentLabel.setVisible(false);
			} else {
				percent = (item.size * 100) / maxval;
				currentval = Math.min(percent, 100);
				percentLabel.setVisible(true);
				percentLabel.setText(percent + " %");
			}
		}
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (maxval > 0 && currentval > 0) {
			Rectangle rect = getBounds();
			g.setColor(Color.LIGHT_GRAY);
			int begin = rect.width - BARSIZE - DELTA - percentLabel.getPreferredSize().width + 1;
			g.fillRect(begin, 1, BARSIZE, DELTA - 2);
			g.setColor(Color.GREEN);
			int width = (BARSIZE * Math.min(currentval, 100)) / 100;
			g.fillRect(begin, 2, width - 2, DELTA - 4);
		}

	}

	private String setNotes(Object obj) {
		if (obj instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) obj;
			if (entity.hasNotes()) {
				return Html.HTML_B + entity.getNotes() + Html.HTML_E;
			}
		}
		return null;
	}

}
