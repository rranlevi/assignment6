package test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A channel in the pub/sub graph.
 */
public class Topic {
    public final String name;
    private final List<Agent> subs = new CopyOnWriteArrayList<>();
    private final List<Agent> pubs = new CopyOnWriteArrayList<>();

    /** Package-private constructor. */
    Topic(String name) {
        this.name = name;
    }

    /** Subscribe an agent to this topic. */
    public void subscribe(Agent a) {
        subs.add(a);
    }

    /** Unsubscribe an agent from this topic. */
    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    /** Add a publisher to this topic. */
    public void addPublisher(Agent a) {
        pubs.add(a);
    }

    /** Remove a publisher from this topic. */
    public void removePublisher(Agent a) {
        pubs.remove(a);
    }

    /** Publish a message to all subscribers. */
    public void publish(Message m) {
        for (Agent agent : subs) {
            agent.callback(name, m);
        }
    }

    /** Get all subscribers. */
    public List<Agent> getSubscribers() {
        return new CopyOnWriteArrayList<>(subs);
    }

    /** Get all publishers. */
    public List<Agent> getPublishers() {
        return new CopyOnWriteArrayList<>(pubs);
    }
}
