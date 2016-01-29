package test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import shnulaa.fx.util.Collections3;
import shnulaa.fx.util.Lists;

public class Java8Test1 {

	interface aa {
		int add();

		default int m(int a, int b) {
			return a + b;
		}
	}

	@FunctionalInterface
	interface Converter<F, T> {
		T convert(F from);
	}

	static class Something {
		String startsWith(String s) {
			return String.valueOf(s.charAt(0));
		}
	}

	static class Person {
		String firstName;
		String lastName;

		public Person() {
		}

		public Person(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

	}

	interface PersonFactory<P extends Person> {
		P create(String firstName, String lastName);
	}

	public static void main(String[] args) {
		List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");

		Collections.sort(names, (a, b) -> b.compareTo(b));

		Converter<Integer, String> converter1 = (from) -> String.valueOf(from);
		System.out.println(converter1.convert(10));

		Converter<Integer, String> converter2 = String::valueOf;
		System.out.println(converter2.convert(10));

		Something something = new Something();
		Converter<String, String> converter3 = something::startsWith;
		System.out.println(converter3.convert("java"));

		PersonFactory<Person> personFactory = Person::new;
		Person p = personFactory.create("liu", "yanqi");
		System.out.println(p.getFirstName() + p.getLastName());

		int sum = 100;
		Converter<String, Integer> converter4 = (from) -> Integer.valueOf(from) + sum;
		System.out.println(converter4.convert("1"));

		Predicate<String> predicate1 = (s) -> s.length() > 0;
		System.out.println(predicate1.test("111"));
		System.out.println(predicate1.negate().test("111"));

		Predicate<String> predicate2 = (s) -> s.length() == 0;

		Predicate<String> predicate3 = predicate1.or(predicate2);
		System.out.println(predicate1.test(""));
		System.out.println(predicate2.test(""));
		System.out.println(predicate3.test(""));

		Function<String, Integer> toInteger = Integer::valueOf;
		Function<String, String> backToString = toInteger.andThen(String::valueOf);
		backToString.apply("123");

		Supplier<Person> personSupplier = Person::new;
		personSupplier.get(); // new Person

		Consumer<Person> greeter = (person) -> System.out.println("Hello, " + person.firstName);
		greeter.accept(new Person("Luke", "Skywalker"));

		Comparator<Person> comparator = (p1, p2) -> p1.firstName.compareTo(p2.firstName);
		System.out.println(comparator.compare(new Person("1", "1"), new Person("2", "2")));

		List<String> list = Lists.newArrayList();
		list.add("aaa1");
		list.add("aaa2");
		list.add("aaa3");
		list.add("aaa4");

		list.add("bbb1");
		list.add("bbb2");
		list.add("bbb3");
		list.add("bbb4");

		list.add("ccc1");
		list.add("ccc2");
		list.add("ccc3");
		list.add("ccc4");

		Predicate<String> p1 = (s) -> (s.startsWith("a"));
		list.stream().filter(p1).sorted().forEach(System.out::println);
		list.stream().filter(p1).sorted().reduce((s1, s2) -> s1 + "," + s2).ifPresent(System.out::println);

		System.out.println("");

		Map<Integer, String> map = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			map.putIfAbsent(i, "val" + i);
		}
		map.forEach((id, val) -> System.out.println(id + "->" + val));

		System.out.println("");

		List<Person> personList = Lists.newArrayList();
		personList.add(new Person("p5-1", "p5-2"));
		personList.add(new Person("p4-1", "p4-2"));
		personList.add(new Person("p3-1", "p3-2"));
		personList.add(new Person("p2-1", "p2-2"));
		personList.add(new Person("p1-1", "p1-2"));

		personList.stream().sorted((a, b) -> a.firstName.compareTo(b.firstName))
				.forEach((per) -> System.out.println(per.firstName));

		System.out.println("");
		List<String> list2 = Lists.newArrayList();
		personList.stream().filter((a) -> a.firstName.equalsIgnoreCase("p3-1"))
				.forEach((per) -> list2.add(per.firstName));
		System.out.println(Collections3.getFirst(list2));
	}
}
