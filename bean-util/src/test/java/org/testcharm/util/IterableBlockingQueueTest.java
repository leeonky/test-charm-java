package org.testcharm.util;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IterableBlockingQueueTest {

    @Test
    void should_iterate_in_order_until_closed() {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();

        q.put("a");
        q.put("b");
        q.put("c");
        q.close();

        Iterator<String> it = q.iterator();

        assertTrue(it.hasNext());
        assertEquals("a", it.next());

        assertTrue(it.hasNext());
        assertEquals("b", it.next());

        assertTrue(it.hasNext());
        assertEquals("c", it.next());

        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void next_should_work_without_calling_hasNext_first() {
        IterableBlockingQueue<Integer> q = new IterableBlockingQueue<>();

        q.put(1);
        q.put(2);
        q.close();

        Iterator<Integer> it = q.iterator();

        assertEquals(1, (int) it.next());
        assertEquals(2, (int) it.next());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void iterator_should_block_until_item_arrives() throws Exception {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();
        Iterator<String> it = q.iterator();

        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread t = new Thread(() -> {
            started.countDown();
            try {
                result.set(it.next());
            } catch (Throwable e) {
                error.set(e);
            }
        });
        t.start();

        assertTrue(started.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);
        assertNull(result.get(), "next() should still be blocked before put()");

        q.put("hello");

        t.join(1000);
        assertNull(error.get(), () -> "unexpected error: " + error.get());
        assertEquals("hello", result.get());
    }

    @Test
    void hasNext_should_block_until_item_or_end_arrives() throws Exception {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();
        Iterator<String> it = q.iterator();

        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<Boolean> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread t = new Thread(() -> {
            started.countDown();
            try {
                result.set(it.hasNext());
            } catch (Throwable e) {
                error.set(e);
            }
        });
        t.start();

        assertTrue(started.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);
        assertNull(result.get(), "hasNext() should still be blocked before put()/close()");

        q.put("x");

        t.join(1000);
        assertNull(error.get(), () -> "unexpected error: " + error.get());
        assertTrue(result.get());

        assertEquals("x", it.next());
    }

    @Test
    void close_should_eventually_end_iteration() {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();
        q.close();

        Iterator<String> it = q.iterator();

        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void should_reject_null_values() {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();

        assertThrows(NullPointerException.class, () -> q.put(null));
    }

    @Test
    void should_handle_close_after_items() {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();

        q.put("a");
        q.close();

        Iterator<String> it = q.iterator();

        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void iterable_should_support_for_each_iteration() {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();

        q.put("a");
        q.put("b");
        q.put("c");
        q.close();

        StringBuilder sb = new StringBuilder();
        for (String s : q) {
            sb.append(s);
        }

        assertEquals("abc", sb.toString());
    }

    @Test
    void hasNext_can_be_called_multiple_times_without_consuming_element() {
        IterableBlockingQueue<String> q = new IterableBlockingQueue<>();

        q.put("a");
        q.close();

        Iterator<String> it = q.iterator();

        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());

        assertEquals("a", it.next());

        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }
}
