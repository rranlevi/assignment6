package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Active-Object decorator for Agent. 
 * This class wraps an Agent and processes callbacks asynchronously using a worker thread.
 */
public class ParallelAgent implements Agent {
    private final Agent agent;
    private final BlockingQueue<Runnable> queue;
    private final Thread workerThread;
    private volatile boolean isRunning = true;
      /**
     * Construct a ParallelAgent that wraps another Agent.
     * @param agent The agent to wrap
     * @param capacity Maximum size of the internal job queue
     */
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        // Make sure capacity is at least 1
        int queueCapacity = Math.max(1, capacity);
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        
        // Create and start worker thread (as daemon)
        this.workerThread = new Thread(() -> {
            try {
                while (isRunning) {
                    // Take a job from queue (blocks if empty)
                    Runnable job = queue.take();
                    // Process the job
                    job.run();
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, exit cleanly
                Thread.currentThread().interrupt();
            }
        });
        
        // Set as daemon thread so it won't block JVM shutdown
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    @Override
    public String getName() {
        // Delegate to wrapped agent
        return agent.getName();
    }    @Override
    public void reset() {
        // Delegate to wrapped agent
        agent.reset();
    }
    
    @Override
    public void callback(String topic, Message msg) {
        try {
            // Place job as lambda in queue
            queue.put(() -> agent.callback(topic, msg));
        } catch (InterruptedException e) {
            // Restore interrupt flag
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        // Signal worker thread to stop
        isRunning = false;
        
        // Wake up worker in case it's blocked on take()
        workerThread.interrupt();
        
        // Wait for worker thread to finish
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Delegate close to wrapped agent
        agent.close();
    }
}
