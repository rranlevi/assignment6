package test;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lazy, thread-safe singleton for TopicManager.
 */
public final class TopicManagerSingleton {
      /** Disallow instantiation. */
    private TopicManagerSingleton() {}

    private static TopicManager instance = null;

    /**
     * Central registry for topics in the pub/sub system.
     */
    public static class TopicManager {
        private final ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<>();        /** Get or create a topic atomically. */
        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, (key) -> new Topic(key));
        }

        /** Get all topics. */
        public Collection<Topic> getTopics() {
            return topics.values();
        }

        /** Clear all topics. */
        public void clear() {
            topics.clear();
        }
    }

    /** Get the singleton TopicManager instance. */
    public static TopicManager get() {
        if (instance == null) {
            instance = new TopicManager();
        }
        return instance;
    }
}
