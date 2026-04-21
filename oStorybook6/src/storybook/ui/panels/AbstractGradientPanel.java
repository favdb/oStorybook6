package storybook.ui.panels;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import storybook.tools.swing.ColorUtil;
import storybook.ui.frames.main.MainFrame;

@SuppressWarnings("serial")
public abstract class AbstractGradientPanel extends AbstractPanel {

	private Color startColor = Color.white;
	private Color endColor = Color.black;
	private boolean showGradient = true;

	public AbstractGradientPanel() {
		showGradient = false;
	}

	public AbstractGradientPanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	public AbstractGradientPanel(MainFrame mainFrame, boolean showBgGradient,
			Color startBgColor, Color endBgColor) {
		super(mainFrame);
		this.showGradient = showBgGradient;
		this.startColor = startBgColor;
		this.endColor = endBgColor;
	}

	public void setGradient(Color start, Color end) {
		this.showGradient = true;
		this.startColor = start;
		this.endColor = end;
		refresh();
	}

	@Override
	public abstract void modelPropertyChange(PropertyChangeEvent evt);

	@Override
	public void refresh() {
		removeAll();
		init();
		initUi();
		invalidate();
		validate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		if (showGradient) {
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gradient = new GradientPaint(0, 0,
					startColor,
					this.getWidth(),
					this.getHeight(),
					ColorUtil.blend(Color.white, endColor));
			g2d.setPaint(gradient);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		} else {
			super.paintComponent(g);
		}
	}

	public Color getEndBgColor() {
		return endColor;
	}

	public Color getStartBgColor() {
		return startColor;
	}

	public void setStartBgColor(Color startBgColor) {
		this.startColor = startBgColor;
	}

	public void setEndBgColor(Color endBgColor) {
		this.endColor = endBgColor;
	}
}
