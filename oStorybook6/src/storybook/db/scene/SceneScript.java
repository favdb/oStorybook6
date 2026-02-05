/*
 * Copyright (C) 2025 favdb
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
package storybook.db.scene;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.select.Elements;
import i18n.I18N;
import storybook.tools.html.Html;

/**
 *
 * @author favdb
 */
public class SceneScript {

	private static final String TT = "SceneScript.";

	public static final String EMPTY_LINE = "<p> </p> \n";
	public static final String SCRIPT_HEADER = "<div class=\"script\">";
	private String desc = "", visual = "", audio = "", voice = "";

	public SceneScript() {

	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public SceneScript(Scene scene) {
		decode(scene.getSummary());
	}

	/**
	 * decode a HTML String to load audio, desc, voice and visual
	 *
	 * @param html
	 */
	public void decode(String html) {
		if (isScript(html)) {
			Document doc = Jsoup.parse(html);
			desc = decodeKey(doc, "desc", html);
			voice = decodeKey(doc, "voice", html);
			audio = decodeKey(doc, "audio", html);
			visual = decodeKey(doc, "visual", html);
		} else {
			desc = html;
		}
	}

	/**
	 * decode a key value from the given HTML String
	 *
	 * @param key
	 * @param html
	 * @return empty String if no key found
	 */
	private String decodeKey(Document doc, String key, String html) {
		String r = "";
		Elements el = doc.getElementsByClass(key);
		if (!el.isEmpty()) {
			//clean the key name
			String t = el.first().html().trim();
			r = t.substring(t.indexOf(":") + 1).trim();
			if (r.startsWith(EMPTY_LINE)) {
				r = r.substring(EMPTY_LINE.length());
			}
		}
		return r;
	}

	public String recode() {
		StringBuilder b = new StringBuilder(SCRIPT_HEADER);
		b.append(recodeKey("desc", desc));
		b.append(recodeKey("visual", visual));
		b.append(recodeKey("audio", audio));
		b.append(recodeKey("voice", voice));
		return b.toString();
	}

	private String recodeKey(String key, String value) {
		String s = value.trim();
		if (s.startsWith(EMPTY_LINE)) {
			s = s.substring(EMPTY_LINE.length());
		}
		if (Html.isEmpty(s)) {
			return "";
		}
		return "<div class=\"" + key + "\">" + Html.intoB(I18N.getMsg("script." + key)) + " : " + s + "</div>";
	}

	public static boolean isScript(String html) {
		return html.contains(SCRIPT_HEADER);
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getVisual() {
		return visual;
	}

	public void setVisual(String visual) {
		this.visual = visual;
	}

	public String getAudio() {
		return audio;
	}

	public void setAudio(String audio) {
		this.audio = audio;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String text) {
		this.voice = text;
	}

	public String toHtml(Scene scene) {
		StringBuilder b = new StringBuilder();
		String style_t = "style=\""
				+ "width: 50%;"
				+ " border: 1px solid black;"
				+ " font-weight: bold;"
				+ "\"",
				//include old HTML3 "valign"
				style_v = "style=\""
				+ "width: 50%;"
				+ " border: 1px solid black;"
				+ " vertical-align: top;"
				+ "\" valign=\"top\"";
		//add the duration
		b.append("<table style=\"border-spacing: 0px;\">");
		b.append(Html.TR_B)
				.append("<td colspan=\"2\">")
				.append(toHtmlKey("duration", scene.getDurationToText()))
				.append(Html.TD_E)
				.append(Html.TR_E);
		if (!desc.isEmpty()) {
			b.append(Html.TR_B)
					.append("<td colspan=\"2\">")
					.append(toHtmlKey("desc", desc))
					.append(Html.TD_E).append(Html.TR_E);
		}
		//visual and audio title
		b.append(Html.TR_B)
				.append("<td ").append(style_t).append(">")
				.append(I18N.getMsg("script.visual")).append(Html.TD_E)
				.append("<td ").append(style_t).append(">")
				.append(I18N.getMsg("script.audio")).append(Html.TD_E)
				.append(Html.TR_E);
		//visual and audio values
		b.append(Html.TR_B)
				.append("<td ").append(style_v).append(">")
				.append(visual).append(Html.TD_E)
				.append("<td ").append(style_v).append(">")
				.append(audio).append(Html.TD_E)
				.append(Html.TR_E);

		if (!voice.isEmpty()) {
			b.append(Html.TR_B)
					.append("<td colspan=\"2\">")
					.append(toHtmlKey("voice", voice))
					.append(Html.TD_E).append(Html.TR_E);
		}
		b.append(Html.TABLE_E);
		return b.toString();
	}

	private String toHtmlKey(String key, String value) {
		String s = value.trim();
		if (s.startsWith(EMPTY_LINE)) {
			s = s.substring(EMPTY_LINE.length());
		}
		return Html.intoB(I18N.getMsg("script." + key)) + " : " + s;
	}

	private String getStyle(boolean b) {
		return "style=\""
				+ "width: 50%;"
				+ " border: 1px solid black;"
				+ (b ? " font-weight: bold;" : "")
				+ "\"";
	}
}
