/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: CategorySet.java,v 1.1 2003-04-15 11:25:32 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import java.util.*;
/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class CategorySet implements Comparable {

	/**
	 * 
	 * @return The ids of all CategorySet which might be cyclic. 
	 */
	static int[] cyclicSets() {
		return null;
	}

	/**
	 * 
	 * @return The ids of all CategorySet which are of illegal 
	 * untyped nodes  in the graph. 
	 */
	static int[] untypedSets() {
		return null;
	}

	/**
	 * 
	 * @return The ids of all CategorySet which are of illegal orphans
	 *  in the graph. 
	 */
	static int[] orphanSets() {
		return null;
	}
	/**
	 * The ids of all orphaned unnamed individuals, which are
	 * not known not to be cyclic.
	 * In fact, these are not cyclic.
	 * @return
	 */
	static int[] cyclicOrphanSets() {
		return null;
	}

	/**
	 * @return the ids of all categories for which the node must be structured
	 * with one member.
	 */
	static int[] structuredOne() {
		return null;
	}
	/**
		 * @return the ids of all categories for which the node must be structured
		 * with two members.
		 */
	static int[] structuredTwo() {
		return null;
	}
	static private final SortedSet sorted = new TreeSet();
	static private final Vector unsorted = new Vector();
	/**
	 * 
	 * @param k A sorted array of integers, each reflecting a category.
	 */
	private CategorySet(int k[]) {
		cats = k;
	}
	private int cats[];
	private int id;
	public int compareTo(Object o) {
		CategorySet c = (CategorySet) o;
		int diff = cats.length - c.cats.length;
		for (int j = 0; diff != 0 && j < cats.length; j++)
			diff = cats[j] = c.cats[j];
		return diff;
	}
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}
	public int hashCode() {
		int rslt = 0;
		for (int i = 0; i < cats.length; i++) {
			rslt ^= cats[i] << (i % 4);
		}
		return rslt;
	}
	/**
	 * This is intended for use by Grammar.java
	 * and SubCategorize.java
	 * @param s
	 * @param isSorted True if s is known to already be in sort order.
	 * @return
	 */
	synchronized static int find(int s[], boolean isSorted) {
		if (!isSorted)
			Arrays.sort(s);
		CategorySet cs = new CategorySet(s);
		CategorySet close = (CategorySet) sorted.tailSet(cs).first();
		if (close.equals(cs))
			return close.id;
		cs.id = unsorted.size();
		unsorted.add(cs);
		sorted.add(cs);
		return cs.id;
	}
	static int[] getSet(int id) {
		return ((CategorySet) unsorted.elementAt(id)).cats;
	}
	/*
	 * toString(int)
	 * setName(int,String)
	 * 
	 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
	 *
	 */

}

/*
	(c) Copyright Hewlett-Packard Company 2003
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/