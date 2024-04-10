/*
 * Copyright (C) 2016 favdb
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
package storybook.ui.panel.memoria;

/**
 *
 * @author martin
 */
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;
import storybook.db.abs.AbstractEntity;

public class VertexStringerImpl<V> implements Transformer<V, String> {

	private Map<AbstractEntity, String> map;
	private boolean enabled;

	public VertexStringerImpl(Map<AbstractEntity, String> map) {
		this.map = new HashMap<>();
		this.enabled = true;
		this.map = map;
	}

	public VertexStringerImpl() {
	}

	@Override
	public String transform(V paramV) {
		if (this.isEnabled()) {
			// taille des caractères à modifier éventuellement
			return map.get(paramV);
		}
		return ("");
	}

	public boolean isEnabled() {
		return (enabled);
	}

	public void setEnabled(boolean val) {
		enabled = val;
	}
}
