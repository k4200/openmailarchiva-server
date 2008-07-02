package org.apache.lucene.search;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.text.Collator;
import java.util.Locale;

/**
 * <p>Linguistic sort based on locale.
 * <p>This class builds a cache of collation keys (Comparable's) for a field.
 * Comparing two collation keys is much faster than comparing two strings, as
 * when using {@link SortField#SortField(String,Locale)}. The result is a
 * <b>faster sort</b>, but also a <b>higher memory consumption</b>.
 * <p>Example usage: <pre><code>
 *   final SortComparatorSource frenchComparator = new LocaleBasedSortComparator(Locale.FRANCE);
 *   final SortField french_asc = new SortField(&quot;fieldname&quot;, frenchComparator);
 *   final SortField french_desc = new SortField(&quot;fieldname&quot;, frenchComparator, true);</code>
 *   ...
 *   Hits h = searcher.search(query, new Sort(french_asc));</code></pre>
 * 
 * @author  <a href="mailto:ronnie.kolehmainen@ub.uu.se">Ronnie Kolehmainen</a>
 * @version $Date$, $Revision$
 * @see     Collator#getCollationKey(String)
 * @see     SortField#SortField(String,SortComparatorSource)
 */
public class LocaleBasedSortComparator extends SortComparator {
  
  private final Collator collator;
  
  /**
   * Creates a new instance of LocaleBasedSortComparator
   * @param  l the locale
   */
  public LocaleBasedSortComparator(Locale l) {
    collator = Collator.getInstance(l);
  }
  
  /**
   * Returns an object which, when sorted according to natural order, will be
   * ordered according to the collation rules defined by this collator.
   * @param  termtext the text
   * @return a {@link java.text.CollationKey collation key}
   */
  protected Comparable getComparable(String termtext) {
    return collator.getCollationKey(termtext);
  }
  
  /**
   * Test for equality with another comparator.
   * @param  o the other comparator
   * @return see {@link Collator#equals(Object)}
   */
  public boolean equals(Object o) {
    if (o instanceof LocaleBasedSortComparator) {
      return collator.equals(((LocaleBasedSortComparator) o).collator);
    }
    return false;
  }
  
  /**
   * The hashcode.
   * @return see {@link Collator#hashCode()}
   */
  public int hashCode() {
    return collator.hashCode();
  }
}
