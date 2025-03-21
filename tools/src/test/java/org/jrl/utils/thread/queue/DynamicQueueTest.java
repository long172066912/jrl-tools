package org.jrl.utils.thread.queue;

import org.jrl.tools.thread.core.factory.JrlDynamicQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DynamicQueueTest {

    @Test
    public void test() {
        JrlDynamicQueue<Integer> queue = new JrlDynamicQueue<>(10);
        for (int i = 0; i < 10; i++) {
            queue.offer(i);
        }
        Assertions.assertEquals(10, queue.size());
        Assertions.assertFalse(queue.offer(11));
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(i, queue.poll());
        }
        Assertions.assertNull(queue.poll());
        Assertions.assertNull(queue.peek());
    }

    @Test
    public void testDynamicUp() {
        JrlDynamicQueue<Integer> queue = new JrlDynamicQueue<>(10, true, false);
        for (int i = 0; i < 10; i++) {
            queue.offer(i);
        }
        Assertions.assertEquals(10, queue.size());
        Assertions.assertTrue(queue.offer(11));
        Assertions.assertEquals(20, queue.getCapacity());
    }

    @Test
    public void testDynamicDown() {
        JrlDynamicQueue<Integer> queue = new JrlDynamicQueue<>(10, false, true);
        queue.offer(1);
        Assertions.assertEquals(1, queue.size());
        Assertions.assertEquals(1, queue.poll());
        Assertions.assertEquals(5, queue.getCapacity());
        queue.offer(1);
        Assertions.assertEquals(2, queue.getCapacity());
    }
}
