/*
 * Copyright (C) 2020 favdb
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
package storybook.ctrl;

import java.beans.PropertyChangeEvent;
import storybook.db.book.Book;

/**
 * Décodage/Codage des actions par type d'entité
 *
 * @author favdb
 */
public class ActKey {

	public final String type;
	private String cmd = Ctrl.PROPS.NONE.toString();

	public ActKey(String type, String cmd) {
		this.type = type;
		if (cmd != null) {
			this.cmd = cmd;
		}
	}

	public ActKey(Book.TYPE type, Ctrl.PROPS cmd) {
		this(type.name(), cmd.name());
	}

	public ActKey(String prop) {
		String v[] = prop.split("_");
		this.type = v[0];
		if (v.length > 1) {
			this.cmd = v[1];
		}
	}

	public ActKey(PropertyChangeEvent evt) {
		this(evt.getPropertyName());
	}

	public String getCmd() {
		return cmd;
	}

	public Book.TYPE getType() {
		return (Book.getTYPE(type));
	}

	public static Book.TYPE getType(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		return (Book.getTYPE(act.type));
	}

	@Override
	public String toString() {
		return (type + "_" + cmd);
	}

	public boolean isUpdate() {
		return cmd.toLowerCase().contains("update");
	}

	public boolean isNew() {
		return cmd.toLowerCase().contains("new");
	}

	public boolean isDelete() {
		return cmd.toLowerCase().contains("delete");
	}

	public boolean isEntity() {
		for (Book.TYPE t : Book.TYPE.values()) {
			if (t.name().equals(type)) {
				return (true);
			}
		}
		return (false);
	}

	public static boolean testCmd(PropertyChangeEvent evt, Ctrl.PROPS prop) {
		ActKey k = new ActKey(evt.getPropertyName());
		return k.getCmd().equals(prop.toString());
	}

}
