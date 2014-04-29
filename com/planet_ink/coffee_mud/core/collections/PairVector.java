package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
Copyright 2000-2014 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

public class PairVector<T,K> extends Vector<Pair<T,K>> implements List<Pair<T,K>>, SPairList<T,K>
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1672867955945287259L;

	public Pair.FirstConverter<T,K> getFirstConverter()
	{
		return new Pair.FirstConverter<T, K>();
	}
	public Pair.SecondConverter<T,K> getSecondConverter()
	{
		return new Pair.SecondConverter<T, K>();
	}
	public Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Pair<T,K>,T>(
				elements(),getFirstConverter());
	}
	public Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Pair<T,K>,K>(
			elements(),getSecondConverter());
	}
	public Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Pair<T,K>,T>(
			iterator(),getFirstConverter());
	}
	public Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Pair<T,K>,K>(
			iterator(),getSecondConverter());
	}
	public synchronized int indexOfFirst(T t)
	{
		return indexOfFirst(t,0);
	}
	public synchronized int indexOfSecond(K k)
	{
		return indexOfSecond(k,0);
	}
	public T getFirst(int index)
	{
		return get(index).first;
	}
	public K getSecond(int index)
	{
		return get(index).second;
	}
	public void add(T t, K k)
	{
		add(new Pair<T,K>(t,k));
	}
	public void addElement(T t, K k)
	{
		add(new Pair<T,K>(t,k));
	}

	@SuppressWarnings("unchecked")
	@Override public boolean contains(Object o)
	{
		if(o instanceof Pair)
			return super.contains(o);
		if(containsFirst((T)o))
			return true;
		return containsSecond((K)o);
	}

	@SuppressWarnings("unchecked")
	@Override public int indexOf(Object o)
	{
		if(o instanceof Pair)
			return super.indexOf(o);
		int x=indexOfFirst((T)o);
		if(x>=0)
			return x;
		return indexOfSecond((K)o);
	}

	@SuppressWarnings("unchecked")
	@Override public synchronized int indexOf(Object o, int index)
	{
		if(o instanceof Pair)
			return super.indexOf(o, index);
		int x=indexOfFirst((T)o, index);
		if(x>=0)
			return x;
		return indexOfSecond((K)o, index);
	}

	public boolean containsFirst(T t)
	{
		for(Iterator<Pair<T,K>> i=iterator();i.hasNext();)
			if((t==null)?i.next()==null:t.equals(i.next().first))
				return true;
		return false;
	}
	public boolean containsSecond(K k)
	{
		for(Iterator<Pair<T,K>> i=iterator();i.hasNext();)
			if((k==null)?i.next()==null:k.equals(i.next().second))
				return true;
		return false;
	}
	public T elementAtFirst(int index)
	{
		return get(index).first;
	}
	public K elementAtSecond(int index)
	{
		return get(index).second;
	}
	public int indexOfFirst(T t, int index)
	{
		try
		{
			for(int i=index;i<size();i++)
				if((t==null ? get(i).first==null : t.equals(get(i).first)))
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int indexOfSecond(K k, int index)
	{
		try
		{
			for(int i=index;i<size();i++)
				if((k==null ? get(i).second==null : k.equals(get(i).second)))
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int lastIndexOfFirst(T t, int index)
	{
		try
		{
			for(int i=index;i>=0;i--)
				if((t==null ? get(i).first==null : t.equals(get(i).first)))
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int lastIndexOfSecond(K k, int index)
	{
		try
		{
			for(int i=index;i>=0;i--)
				if((k==null ? get(i).second==null : k.equals(get(i).second)))
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int lastIndexOfFirst(T t)
	{
		return lastIndexOfFirst(t,size()-1);
	}
	public int lastIndexOfSecond(K k)
	{
		return lastIndexOfSecond(k,size()-1);
	}
	public boolean removeFirst(T t)
	{
		Pair<T,K> pair;
		for(final Iterator<Pair<T,K>> i=iterator();i.hasNext();)
		{
			pair=i.next();
			if((t==null ? pair.first==null : t.equals(pair.first)))
			{
				i.remove();
				return true;
			}
		}
		return false;
	}
	public boolean removeSecond(K k)
	{
		Pair<T,K> pair;
		for(final Iterator<Pair<T,K>> i=iterator();i.hasNext();)
		{
			pair=i.next();
			if((k==null ? pair.second==null : k.equals(pair.second)))
			{
				i.remove();
				return true;
			}
		}
		return false;
	}
	public boolean removeElementFirst(T t)
	{
		return removeFirst(t);
	}
	public boolean removeElementSecond(K k)
	{
		return removeSecond(k);
	}
	public T firstFirstElement(int index)
	{
		return firstElement().first;
	}
	public K firstSecondElement(int index)
	{
		return firstElement().second;
	}
	public T lastFirstElement(int index)
	{
		return lastElement().first;
	}
	public K lastSecondElement(int index)
	{
		return lastElement().second;
	}
	public T[] toArrayFirst(T[] a)
	{
		T[] objs= toArray(a);
		for(int x=0;x<size();x++)
			objs[x] = getFirst(x);
		return objs;
	}
	public K[] toArraySecond(K[] a)
	{
		K[] objs= toArray(a);
		for(int x=0;x<size();x++)
			objs[x] = getSecond(x);
		return objs;
	}
}
