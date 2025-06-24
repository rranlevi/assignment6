package test;

public class IncAgent implements Agent {
    private final String[] subs;
    private final String[] pubs;
    
    public IncAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
    }
    
    @Override
    public String getName() {
        return "IncAgent";
    }
    
    @Override
    public void reset() {
        // Nothing to reset
    }
    
    @Override
    public void callback(String topic, Message msg) {
        // When a numeric value arrives, increment it and publish to the output topic
        if (subs.length >= 1 && topic.equals(subs[0]) && !Double.isNaN(msg.asDouble) && pubs.length >= 1) {
            double incrementedValue = msg.asDouble + 1;
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(incrementedValue));
        }
    }
      @Override
    public void close() {
        // Unsubscribe from all input topics
        for (String topic : subs) {
            TopicManagerSingleton.get().getTopic(topic).unsubscribe(this);
        }
        
        // Remove as publisher from all output topics
        for (String topic : pubs) {
            TopicManagerSingleton.get().getTopic(topic).removePublisher(this);
        }
    }
}
