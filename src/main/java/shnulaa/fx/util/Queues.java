package shnulaa.fx.util;

import java.util.concurrent.LinkedBlockingQueue;

public class Queues {

	public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue() {
		return new LinkedBlockingQueue<E>();
	}

}
