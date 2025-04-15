/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.tools.swing.js;

/**
 * class for a JTextArea drawing with characters shape
 *
 * @author favdb
 */
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class JSTextArea extends JTextArea {

	private Color charColor = Color.BLACK;  // characters color
	private Color borderColor = Color.WHITE; // background color
	private int borderSize = 1; // size of shape
	private int marginLeft = 10;  // default left margin
	private int marginRight = 10; // défault right margin
	private int textAlign = 1; // 0=left, 1=centre, 2=droite

	public JSTextArea(String text) {
		this();
		this.setText(text);
		this.setOpaque(false);
	}

	public JSTextArea() {
		super();
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
		this.setOpaque(false);
	}

	/**
	 * set the alignment
	 *
	 * @param alignment
	 */
	public void setAlignment(int alignment) {
		if (alignment == 0 || alignment == 1 || alignment == 2) {
			this.textAlign = alignment;
		} else {
			this.textAlign = 1; // Valeur par défaut
		}
		this.repaint();
	}

	/**
	 * set margins
	 *
	 * @param left
	 * @param right
	 */
	public void setMargins(int left, int right) {
		this.marginLeft = left;
		this.marginRight = right;
		this.repaint();
	}

	/**
	 * set characters color
	 *
	 * @param c1
	 */
	public void setCharColor(Color c1) {
		this.charColor = c1;
		this.repaint(); // On redessine le texte après avoir changé les paramètres
	}

	/**
	 * set characters color shape color
	 *
	 * @param c1 : characters color
	 * @param c2 : shape color
	 * @param sz : size of shape
	 */
	public void setCharColor(Color c1, Color c2, int sz) {
		this.charColor = c1;
		this.borderColor = c2;
		this.borderSize = sz;
		this.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight()); // Remplir l'arrière-plan uniquement si opaque
		}
		Font font = getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		String text = getText().trim();
		if (text != null && !text.isEmpty()) {
			int y = font.getSize();
			int paneWidth = getWidth() - marginLeft - marginRight;  // Largeur disponible
			String[] words = text.split(" ");
			StringBuilder line = new StringBuilder();
			float lineWidth = 0;
			for (String word : words) {
				TextLayout layout = new TextLayout(word, font, frc);
				float wordWidth = (float) layout.getBounds().getWidth();
				if (lineWidth + wordWidth > paneWidth) {
					float x = calculateAlignmentX(lineWidth, paneWidth);
					drawLine(g2, line.toString().trim(), x + (marginLeft + borderSize + 1), y, font, frc);
					line = new StringBuilder();
					lineWidth = 0;
					y += layout.getAscent() + layout.getDescent() + layout.getLeading();
				}
				line.append(word).append(" ");
				//lineWidth += wordWidth + layout.getAdvance() / word.length();
				layout = new TextLayout(line.toString().trim(), font, frc);
				lineWidth = ((int) layout.getBounds().getWidth()) + borderSize + 1;
			}

			if (line.length() > 0) {
				float x = calculateAlignmentX(lineWidth, paneWidth);
				drawLine(g2, line.toString().trim(), x + marginLeft, y, font, frc);
			}
		}
	}

	// Méthode pour calculer la position X en fonction de l'alignement
	private float calculateAlignmentX(float lineWidth, int paneWidth) {
		switch (textAlign) {
			case 0://left
				return 0;
			case 2://right
				return paneWidth - lineWidth;
			default://center
				return (paneWidth - lineWidth) / 2;
		}
	}

	private void drawLine(Graphics2D g2, String line, float x, int y, Font font, FontRenderContext frc) {
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			TextLayout charLayout = new TextLayout(String.valueOf(ch), font, frc);
			Shape shape = charLayout.getOutline(AffineTransform.getTranslateInstance(x, y));

			g2.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.setColor(borderColor);
			g2.draw(shape);  // Contour du caractère

			g2.setColor(charColor);
			g2.fill(shape);  // Texte intérieur

			x += charLayout.getAdvance();
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("JsTextArea Alignement Demo");
		JSTextArea textArea = new JSTextArea();
		textArea.setFont(new Font("Arial", Font.BOLD, 24));
		textArea.setText("Ceci est un texte aligné avec marges et différentes options d'alignement.");
		textArea.setCharColor(Color.RED, Color.BLACK, 1);
		textArea.setMargins(30, 30); // Définir des marges gauche et droite
		textArea.setAlignment(2); // Aligner le texte à droite
		Dimension dim = new Dimension(380, 550);
		frame.add(textArea);
		frame.setSize(dim);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
