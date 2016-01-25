package shnulaa.fx.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "rawtypes" })
public final class Collections3 {

	/**
	 * convertToString
	 * 
	 * @param collection
	 * @param separator
	 * @return
	 */
	public static String convertToString(final Collection collection, final String separator) {
		return StringUtils.join(collection, separator);
	}

	/**
	 * convertToString
	 * 
	 * @param collection
	 * @param prefix
	 * @param postfix
	 * @return
	 */
	public static String convertToString(final Collection collection, final String prefix, final String postfix) {
		StringBuilder builder = new StringBuilder();
		for (Object o : collection) {
			builder.append(prefix).append(o).append(postfix);
		}
		return builder.toString();
	}

	/**
	 * isEmpty
	 * 
	 * @param collection
	 * @return
	 */
	public static boolean isEmpty(Collection collection) {
		return ((collection == null) || collection.isEmpty());
	}

	/**
	 * isNotEmpty
	 * 
	 * @param collection
	 * @return
	 */
	public static boolean isNotEmpty(Collection collection) {
		return ((collection != null) && !(collection.isEmpty()));
	}

	public static boolean isNotEmpty(Map map) {
		return ((map != null) && !(map.isEmpty()));
	}

	/**
	 * getFirst
	 * 
	 * @param collection
	 * @return
	 */
	public static <T> T getFirst(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}

		return collection.iterator().next();
	}

	/**
	 * getLast
	 * 
	 * @param collection
	 * @return
	 */
	public static <T> T getLast(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}

		if (collection instanceof List) {
			List<T> list = (List<T>) collection;
			return list.get(list.size() - 1);
		}

		Iterator<T> iterator = collection.iterator();
		while (true) {
			T current = iterator.next();
			if (!iterator.hasNext()) {
				return current;
			}
		}
	}

	/**
	 * union
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> List<T> union(final Collection<T> a, final Collection<T> b) {
		List<T> result = new ArrayList<T>(a);
		result.addAll(b);
		return result;
	}

	/**
	 * subtract
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> List<T> subtract(final Collection<T> a, final Collection<T> b) {
		List<T> list = new ArrayList<T>(a);
		for (T element : b) {
			list.remove(element);
		}

		return list;
	}

	/**
	 * intersection
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
		List<T> list = new ArrayList<T>();

		for (T element : a) {
			if (b.contains(element)) {
				list.add(element);
			}
		}
		return list;
	}
}
