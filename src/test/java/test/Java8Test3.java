package test;

import java.util.HashMap;
import java.util.Map;

public class Java8Test3 {

	public static void main(String[] args) {

		Map<Integer, String> map = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			map.put(i, "Val" + i);
		}

		map.forEach((key, value) -> System.out.println(key + " -> " + value));

		map.computeIfPresent(3, (num, val) -> (num + val));
		System.out.println(map.get(4));

		map.computeIfPresent(9, (num, val) -> null);
		System.out.println(map.containsKey(9)); // false

		map.computeIfAbsent(23, num -> "val" + num);
		System.out.println(map.containsKey(23)); // true

		map.computeIfAbsent(3, num -> "bam");
		System.out.println(map.get(3)); // val33

		map.merge(9, "val9", (value, newValue) -> value.concat(newValue));
		map.get(9); // val9

		map.merge(9, "concat", (value, newValue) -> value.concat(newValue));
		map.get(9); // val9concat

	}
}
