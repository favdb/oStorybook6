/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

/**
 * Classe de gestion des notes de renvoi
 *
 * @author favdb
 */
public class ShefNote {

	private String text = "";

	public ShefNote(String text) {
		this.text = text;
	}

	public String getText() {
		return (text);
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Link to the endnote from text
	 * <a href="#endnote_001" name="innote_001"><small><sup>&#160;(1)</sup></small></a>
	 *
	 * @param link : for the directory of the endnote.html
	 * @param id : number of th endnote
	 * @return
	 */
	public String getLinkToSHEFnote(String link, int number) {
		StringBuilder b = new StringBuilder();
		b.append(String.format("<a href=\"%s#endnote_%03d\"", link, number))
				.append(String.format(" name=\"innote_%03d\">", number))
				.append("<small><sup>")
				.append(String.format("&nbsp;(%d)", number))
				.append("</sup></small>")
				.append("</a>");
		return (b.toString());
	}

	/**
	 * Link from the endnote to text
	 * <a href="#endnote_001" name="innote_001"><small><sup>&#160;(1)</sup></small></a>
	 *
	 * @param link : for export the directory of the text
	 * @param id : number of th endnote
	 * @return
	 */
	public String getLinkFromSHEFnote(String link, int number) {
		StringBuilder b = new StringBuilder();
		b.append(String.format("<a href=\"%s#innote_%03d\"", link, number))
				.append(String.format(" name=\"endnote_%03d\">", number))
				.append(String.format("%d", number))
				.append("</a> ")
				.append(this.text);
		return (b.toString());
	}

}
