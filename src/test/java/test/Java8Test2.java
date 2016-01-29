package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Java8Test2 {

	public static void main(String[] args) {

		int capcity = 10000000;
		List<String> list = new ArrayList<>(capcity);
		for (int i = 0; i < capcity; i++) {
			UUID uuid = UUID.randomUUID();
			list.add(uuid.toString());
		}

		System.out.println("start");
		long s = System.nanoTime();
		System.out.println(list.parallelStream().sorted((a, b) -> a.compareTo(b)).count());
		long e = System.nanoTime();
		long millis = TimeUnit.NANOSECONDS.toMillis(e - s);
		System.out.println("end, cost time1:" + millis + "ms");

		s = System.nanoTime();
		System.out.println(list.stream().sorted((a, b) -> a.compareTo(b)).count());
		e = System.nanoTime();
		millis = TimeUnit.NANOSECONDS.toMillis(e - s);
		System.out.println("end, cost time2:" + millis + "ms");

		s = System.nanoTime();
		Collections.sort(list, (a, b) -> a.compareTo(b));
		e = System.nanoTime();
		millis = TimeUnit.NANOSECONDS.toMillis(e - s);
		System.out.println("end, cost time3:" + millis + "ms");
		
		

	}
}
