
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
import java.util.LinkedList;

public class BlockingQueue implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -5002769471984531317L;
	private LinkedList<Object> _queue = new LinkedList<Object>();
   
    public BlockingQueue() {
        
    }
    
    public void add(Object o) {
        synchronized(_queue) {
            _queue.add(o);
            _queue.notify();
        }
    }
    
    
    public void addFront(Object o) {
        synchronized(_queue) {
            _queue.add(0, 0);
            _queue.notify();
        }
    }
    
    public Object next() {
        
        Object o = null;
        
        // Block if the Queue is empty.
        synchronized(_queue) {
            if (_queue.size() == 0) {
                try {
                    _queue.wait();
                }
                catch (InterruptedException e) {
                    return null;
                }
            }
        
            // Return the Object.
            try {
                o = _queue.removeFirst();
                _queue.removeLast();
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new InternalError("Race hazard in Queue object.");
            }
        }

        return o;
    }
    
    public boolean hasNext() {
        return (this.size() != 0);
    }
     
    
    public void clear() {
        synchronized(_queue) {
            _queue.clear();
        }
    }
    
   
    public int size() {
        return _queue.size();
    }
    


    
}