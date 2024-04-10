/*
 * Copyright (C) 2022 favdb
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.episode;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Node;
import storybook.db.abs.AbstractEntity;
import static storybook.db.abs.AbstractEntity.fromXmlBeg;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;

/**
 *
 * @author favdb
 */
public class Episode extends AbstractEntity {

	private Integer number = -1;
	private Strand strand = null;
	private Scene scene = null;
	private Chapter chapter = null;
	private Long strand_id = -1L;
	private Long chapter_id = -1L;
	private Long scene_id = -1L;

	public Episode() {
		super(Book.TYPE.EPISODE, "010");
	}

	public Episode(ResultSet rs) {
		this();
		try {
			number = rs.getInt("number");
			strand = rs.getObject("strand", Strand.class);
			chapter = rs.getObject("chapter", Chapter.class);
			scene = rs.getObject("scene", Scene.class);
		} catch (SQLException ex) {
			//empty
		}
	}

	public Episode(int number) {
		this();
		this.number = number;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = (number == null ? -1 : number);
	}

	public Strand getStrand() {
		return strand;
	}

	public void setStrand(Strand strand) {
		this.strand = strand;
	}

	public Long getStrandId() {
		return strand_id;
	}

	public void setStrandId(Long value) {
		this.strand_id = value;
	}

	public Chapter getChapter() {
		return chapter;
	}

	public void setChapter(Chapter entity) {
		this.chapter = entity;
	}

	public Long getChapterId() {
		return chapter_id;
	}

	public void setChapterId(Long value) {
		this.chapter_id = value;
	}

	public boolean hasChapter() {
		return chapter != null;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene entity) {
		this.scene = entity;
	}

	public Long getSceneId() {
		return scene_id;
	}

	public void setSceneId(Long value) {
		this.scene_id = value;
	}

	public boolean hasScene() {
		return scene != null;
	}

	public void setLink(AbstractEntity entity) {
		if (entity instanceof Chapter) {
			chapter = (Chapter) entity;
			scene = null;
		} else if (entity instanceof Scene) {
			chapter = null;
			scene = (Scene) entity;
		} else {
			chapter = null;
			scene = null;
		}
	}

	public AbstractEntity getLink() {
		if (hasChapter()) {
			return chapter;
		}
		if (hasScene()) {
			return scene;
		}
		return null;
	}

	public boolean hasLink() {
		return hasChapter() || hasScene();
	}

	public void setStrandNotes(Strand strand, String val) {
		setStrand(strand);
		setNotes(val);
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.NUMBER, number));
		b.append(XmlUtil.setAttribute(0, XK.STRAND, strand));
		b.append(XmlUtil.setAttribute(0, XK.CHAPTER, chapter));
		b.append(XmlUtil.setAttribute(0, XK.SCENE, scene));
		b.append(toXmlFooter());
		return b.toString();
	}

	public static Episode fromXml(Node node) {
		Episode p = new Episode();
		fromXmlBeg(node, p);
		p.setNumber(XmlUtil.getInteger(node, XK.NUMBER));
		p.setStrandId(XmlUtil.getLong(node, XK.STRAND));
		p.setChapterId(XmlUtil.getLong(node, XK.CHAPTER));
		p.setSceneId(XmlUtil.getLong(node, XK.SCENE));
		fromXmlEnd(node, p);
		return p;
	}

}
