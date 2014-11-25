package srt.util;

import java.util.HashMap;
import java.util.HashSet;

public class FunctionUtil {
	
	public static <T> boolean setIntersection(HashSet<T> set1, HashSet<T> set2) {
		for (T elem : set1)
			if (set2.contains(elem))
				return true;
		return false;
	}
	
	public static <T> HashSet<T> getIntersection(HashSet<T> set1, HashSet<T> set2) {
		HashSet<T> set = new HashSet<T>();
		
		for (T elem : set1)
			if (set2.contains(elem))
				set.add(elem);
		return set;
	}
	
	public static <T, F> HashMap<T, F> getIntersection(HashMap<T, F> map1, HashMap<T, F> map2) {
		HashMap<T, F> map = new HashMap<T, F>();
		
		for (T elem : map1.keySet())
			if (map2.containsKey(elem) && map2.get(elem).equals(map1.get(elem)))
				map.put(elem, map1.get(elem));
		return map;
	}
	
	public static <T> HashSet<T> getUnion(HashSet<T> set1, HashSet<T> set2) {
		HashSet<T> set = new HashSet<T>();
		
		set.addAll(set1);
		set.addAll(set2);
		return set;
	}
}
