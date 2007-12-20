/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.util;

import java.io.Serializable;
import java.util.*;
	
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.Map;

/**
* This class is used to combine the key-value lookup capabilities of a
* Hashtable along with order preserving capabilities of a Vector.
* Iterator on a Set of Hashtable keys, obtained by keySet() or an
* Enumeration obtained by keys() method, both are not guaranteed to
* iterate in the same order as the values were put in.
*
* This class behaves like a queue, (FIFO). Objects are returned in the
* same order they were put in.
*
* @author Animesh Srivastava
* @author James Cook
*/
public class OrderedHashtable extends Hashtable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4354636208294138924L;
	//member variables
    private Vector _serialOrder;

    //----------------------------------------------------------
    // constructors
    //----------------------------------------------------------

    /** Public Constructor */
    public OrderedHashtable() {
        super();
        _serialOrder = new Vector();
    }

    //----------------------------------------------------------
    // overridden methods
    //----------------------------------------------------------

    /**
     * Clears this OrderedHashtable so that it has no keys.
     *
     * @exception UnsupportedOperationException - clear is not supported by
     * the underlying Interface java.util.Map.
     */
    synchronized public void clear() throws UnsupportedOperationException {
        super.clear();
        _serialOrder.clear();
    }

    /**
     * Removes the key (and its corresponding value) from this OrderedHashtable.
     * Does nothing if key is not in the OrderedHashtable.
     *
     * @param key - the key that needs to be removed.
     * @returns the value to which the key had been mapped in this OrderedHashtable,
     *                      or null if the key did not have a mapping.
     */
    synchronized public Object remove(Object key) {
        _serialOrder.remove(key);
        return super.remove(key);
    }

    /**
     * Maps the specified key to the specified value in this OrderedHashtable.
     * Neither the key nor the value can be null. If the key already exists
     * then the ordering is not changed. If it does not exists then it is added
     * at the     end of the OrderedHastable.
     *
     * @param key - the key.
     * @param value - the value.
     * @throws NullPointerException if the key or value is null.
     * @returns the previous value of the specified key in this hashtable, or
     *                      null if it did not have one.
     */
    synchronized public Object put(Object key, Object value) throws NullPointerException {
        Object toReturn = super.put(key, value);
        if (toReturn == null) {
            _serialOrder.add(key);
        }
        return toReturn;
    }

    //----------------------------------------------------------
    // public methods
    //----------------------------------------------------------

    /**
     * Returns an Iterator to iterate through the keys of the OrderedHashtable.
     * Iteration will occur in the same order as the keys were put in into the
     * OrderedHashtable.
     *
     * The remove() method of Iterator interface is optional in jdk1.3 and hence
     * not implemented.
     *
     * @return the Iterator of keys.
     */
    public Iterator iterateKeys() {
        return new Enumerator(true);
    }

    /**
     * Returns an Iterator to iterate through the values of the OrderedHashtable.
     * Iteration will occur in the same order as the values were put in into the
     * OrderedHashtable.
     *
     * The remove() method of Iterator interface is optional in jdk1.3 and hence
     * not implemented.
     *
     * @return the Iterator of values.
     */
    public Iterator iterateValues() {
        return new Enumerator(false);
    }

    /**
     * Returns an Enumeration to enumerate through the keys of the OrderedHashtable.
     * Enumeration will occur in the same order as the keys were put in into the
     * OrderedHashtable.
     *
     * @return the Enumeration of keys.
     */
    public Enumeration enumerateKeys() {
        return new Enumerator(true);
    }

    /**
     * Returns an Enumeration to enumerate through the values of the OrderedHashtable.
     * Enumeration will occur in the same order as the values were put in into the
     * OrderedHashtable.
     *
     * @return the Enumeration of values.
     */
    public Enumeration enumerateValues() {
        return new Enumerator(false);
    }

    /**
     * Returns a string representation of the OrderedHashtable.
     *
     * @return a String representation of this class
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("{ ");
        Object key = null;
        int i = 0;
        while (i < _serialOrder.size()) {
            key = _serialOrder.elementAt(i++);
            s.append(key);
            s.append("=");
            s.append(get(key));
            s.append("; ");
        }
        s.append(" }");
        return s.toString();
    }

    //----------------------------------------------------------
    // inner class
    //----------------------------------------------------------

    private class Enumerator implements Enumeration, Iterator {
        private int _count = _serialOrder.size(); //number of elements in the Vector
        private int _serial = 0; //keep track of the current element
        private boolean _keys;

        public Enumerator(boolean keys) {
            _keys = keys;
        }

        public boolean hasMoreElements() {
            return _serial < _count;
        }

        public Object nextElement() {
            synchronized (OrderedHashtable.this) {
                if ((_count == 0) || (_serial == _count)) {
                    throw new NoSuchElementException("OrderedHashtable Enumerator");
                }
                if (_keys) {
                    return _serialOrder.elementAt(_serial++);
                } else {
                    return get(_serialOrder.elementAt(_serial++));
                }
            }
        }

        public boolean hasNext() {
            return hasMoreElements();
        }

        public Object next() {
            return nextElement();
        }

        //optional in jdk1.3
        public void remove() {
        }
    }

}
