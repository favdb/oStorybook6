/*
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 */


// $Id: TabAreaVisiblePolicyProperty.java,v 1.2 2005/12/04 13:46:05 jesper Exp $
package api.infonode.tabbedpanel;

import api.infonode.properties.base.PropertyGroup;
import api.infonode.properties.types.EnumProperty;
import api.infonode.properties.util.PropertyValueHandler;

/**
 * Property for TabAreaVisiblePolicy
 *
 * @author $Author: jesper $
 * @version $Revision: 1.2 $
 * @see api.infonode.tabbedpanel.TabAreaVisiblePolicy
 * @since ITP 1.4.0
 */
public class TabAreaVisiblePolicyProperty extends EnumProperty {
  /**
   * Constructs a TabAreaVisiblePolicyProperty object
   *
   * @param group        property group
   * @param name         property name
   * @param description  property description
   * @param valueStorage storage for property
   */
  public TabAreaVisiblePolicyProperty(PropertyGroup group,
                                      String name,
                                      String description,
                                      PropertyValueHandler valueStorage) {
    super(group,
          name,
          TabAreaVisiblePolicy.class,
          description,
          valueStorage,
          TabAreaVisiblePolicy.getVisiblePolicies());
  }

  /**
   * Gets the TabAreaVisiblePolicy
   *
   * @param object storage object for property
   * @return the TabAreaVisiblePolicy
   */
  public TabAreaVisiblePolicy get(Object object) {
    return (TabAreaVisiblePolicy) getValue(object);
  }

  /**
   * Sets the TabAreaVisiblePolicy
   *
   * @param object storage object for property
   * @param policy the TabAreaVisiblePolicy
   */
  public void set(Object object, TabAreaVisiblePolicy policy) {
    setValue(object, policy);
  }
}
