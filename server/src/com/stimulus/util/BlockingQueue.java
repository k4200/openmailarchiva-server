/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.util;

import java.util.Vector;

public class BlockingQueue {
    
    private Vector _queue = new Vector();
   
    public BlockingQueue() {
        
    }
    
    public void add(Object o) {
        synchronized(_queue) {
            _queue.addElement(o);
            _queue.notify();
        }
    }
    
    
    public void addFront(Object o) {
        synchronized(_queue) {
            _queue.insertElementAt(o, 0);
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
                o = _queue.firstElement();
                _queue.removeElementAt(0);
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
            _queue.removeAllElements();
        }
    }
    
   
    public int size() {
        return _queue.size();
    }
    


    
}