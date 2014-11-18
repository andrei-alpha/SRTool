package srt.util;

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
}
