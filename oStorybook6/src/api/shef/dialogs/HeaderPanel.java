/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import api.mig.swing.MigLayout;
import storybook.App;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;

public class HeaderPanel extends JPanel {

	private JLabel titleLabel = null;
	private JLabel msgLabel = null;
	private JLabel iconLabel = null;

	/**
	 * This is the default constructor
	 */
	public HeaderPanel() {
		super();
		initialize();
	}

	public HeaderPanel(String title, String desc, Icon ico) {
		super();
		initialize();
		setTitle(title);
		setDescription(desc);
		setIcon(ico);
	}

	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	public void setDescription(String desc) {
		msgLabel.setText(desc);
	}

	public void setIcon(Icon icon) {
		iconLabel.setIcon(icon);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new MigLayout(MIG.FILLX));
		setBorder(BorderFactory.createLineBorder(Color.black, 1));

		Font fnt = App.getInstance().fonts.defGet();
		titleLabel = new JLabel();
		titleLabel.setOpaque(false);
		titleLabel.setFont(FontUtil.getBold(fnt));
		add(titleLabel, MIG.get(MIG.GROWX));

		iconLabel = new JLabel();
		iconLabel.setOpaque(true);
		add(iconLabel, MIG.get(MIG.RIGHT, MIG.WRAP));

		msgLabel = new JLabel();
		msgLabel.setFont(FontUtil.getItalic(fnt));
		msgLabel.setOpaque(false);
		msgLabel.setVerticalAlignment(SwingConstants.TOP);
		add(msgLabel, MIG.get(MIG.GROWX));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Rectangle bounds = getBounds();
		// Set Paint for filling Shape
		Color blue = new Color(153, 204, 127);
		Paint gradientPaint = new GradientPaint(bounds.width * 0.02f, bounds.y, Color.white, bounds.width, 0f, blue);
		g2.setPaint(gradientPaint);
		g2.fillRect(0, 0, bounds.width, bounds.height);
	}

}
