package test;

public class PlusAgent implements Agent {
    private final String[] subs;
    private final String[] pubs;
    private double x = 0;
    private double y = 0;
    private boolean xUpdated = false;
    private boolean yUpdated = false;
    
    public PlusAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
    }
    
    @Override
    public String getName() {
        return "PlusAgent";
    }
    
    @Override
    public void reset() {
        x = 0;
        y = 0;
        xUpdated = false;
        yUpdated = false;
    }
    
    @Override
    public void callback(String topic, Message msg) {
        // Update x or y based on which topic received the message
        if (subs.length >= 1 && topic.equals(subs[0])) {
            if (!Double.isNaN(msg.asDouble)) {
                x = msg.asDouble;
                xUpdated = true;
            }
        } else if (subs.length >= 2 && topic.equals(subs[1])) {
            if (!Double.isNaN(msg.asDouble)) {
                y = msg.asDouble;
                yUpdated = true;
            }
        }
        
        // If both x and y are updated, publish the sum
        if (xUpdated && yUpdated && pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(x + y));
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
