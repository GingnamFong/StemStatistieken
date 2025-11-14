package nl.hva.ict.sm3.backend.utils.xml;

import java.util.*;

public class QueueDemo {
    public static void main(String[] args) {

        // Queue using LinkedList
        Queue<Integer> linkedListQueue = new LinkedList<>();

        // Queue using ArrayDeque
        Queue<Integer> arrayDequeQueue = new ArrayDeque<>();

        // Demo: basic queue operations
        System.out.println("=== Basic Queue Operations ===");

        linkedListQueue.offer(10);
        linkedListQueue.offer(20);
        linkedListQueue.offer(30);

        arrayDequeQueue.offer(10);
        arrayDequeQueue.offer(20);
        arrayDequeQueue.offer(30);

        System.out.println("LinkedList poll(): " + linkedListQueue.poll());
        System.out.println("ArrayDeque poll(): " + arrayDequeQueue.poll());

        // Simple Performance Comparison
        System.out.println("\n=== Performance Test (adding 1,000,000 elements) ===");

        int n = 1_000_000;

        long startLL = System.nanoTime();
        for (int i = 0; i < n; i++) {
            linkedListQueue.offer(i);
        }
        long timeLL = System.nanoTime() - startLL;

        long startAD = System.nanoTime();
        for (int i = 0; i < n; i++) {
            arrayDequeQueue.offer(i);
        }
        long timeAD = System.nanoTime() - startAD;

        System.out.println("LinkedList time: " + timeLL / 1_000_000 + " ms");
        System.out.println("ArrayDeque time: " + timeAD / 1_000_000 + " ms");

        System.out.println("\nConclusion:");
        System.out.println("- ArrayDeque is usually faster for queue operations.");
        System.out.println("- LinkedList has higher overhead and is slower in practice.");
    }
}
