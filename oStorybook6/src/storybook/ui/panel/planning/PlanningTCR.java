package storybook.ui.panel.planning;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.Icon;
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

/**
 * class for renderer of a Planning element
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class PlanningTCR extends DefaultTreeCellRenderer {

	private static final int BARSIZE = 200;
	private static final int DELTA = 20;
	private int maxval;
	private int currentval;
	private final JLabel textLabel;
	private final JLabel percentLabel;

	public PlanningTCR() {
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
		Object userObject = node.getUserObject();
		textLabel.setText(userObject.toString());
		maxval = -1;
		int percent = 0;
		if (userObject instanceof PlanningElement) {
			currentval = ((PlanningElement) userObject).getSize();
			//currentwords=((PlanningElement)userObject).getWords();
			maxval = -1;
			Object obj = ((PlanningElement) userObject).getElement();
			percentLabel.setVisible(false);
			if (obj instanceof Scene) {
				textLabel.setIcon(((Scene) obj).getScenestate().getIcon());
			} else if ((leaf) && (obj instanceof AbstractEntity)) {
				Icon icon = ((AbstractEntity) obj).getIcon();
				textLabel.setIcon(icon);
			} else if (obj instanceof String) {
				// default icon for title
				textLabel.setIcon(UIManager.getIcon("Tree.closedIcon"));
				maxval = ((PlanningElement) userObject).getMaxSize();
				//maxwords  = ((PlanningElement)userObject).getMaxWords();
				percent = (currentval * 100) / ((maxval == 0) ? 100 : maxval);
				percentLabel.setVisible(true);
				percentLabel.setText(percent + " %");
			}
			setToolTipText(setNotes(obj));
			if (!leaf && obj instanceof AbstractEntity) {
				Icon icon = ((AbstractEntity) obj).getIcon();
				textLabel.setIcon(icon);
				if (obj instanceof Part) {
					maxval = Math.max(currentval, ((Part) obj).getObjectiveChars());
				} else if (obj instanceof Chapter) {
					maxval = Math.max(currentval, ((Chapter) obj).getObjectiveChars());
				}
				if (maxval == 0) {
					percentLabel.setVisible(false);
				} else {
					percent = (currentval * 100) / maxval;
					percentLabel.setVisible(true);
					percentLabel.setText(percent + " %");
				}
			}
		}
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (maxval > 0) {
			Rectangle dimension = getBounds();
			g.setColor(Color.BLUE);
			g.fillRect(dimension.width - BARSIZE - DELTA - percentLabel.getPreferredSize().width,
				1,
				BARSIZE, dimension.height - 2);
			g.setColor(Color.GREEN);
			int width = (BARSIZE * currentval) / maxval;
			g.fillRect(dimension.width - BARSIZE - DELTA - percentLabel.getPreferredSize().width + 1,
				2,
				width - 2, dimension.height - 4);
		}

	}

	private String setNotes(Object obj) {
		String txt = null;
		if (obj instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) obj;
			if (entity.hasNotes()) {
				txt = Html.HTML_B + entity.getNotes() + Html.HTML_E;
			}
		}
		return txt;
	}

}
