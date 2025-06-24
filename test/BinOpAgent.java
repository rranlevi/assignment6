package test;

import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent {
    private final String agentName;
    private final String inputTopic1;
    private final String inputTopic2;
    private final String outputTopic;
    private final BinaryOperator<Double> op;
    
    private Double input1 = null;
    private Double input2 = null;
    
    public BinOpAgent(String agentName, String inputTopic1, String inputTopic2, 
                      String outputTopic, BinaryOperator<Double> op) {
        this.agentName = agentName;
        this.inputTopic1 = inputTopic1;
        this.inputTopic2 = inputTopic2;
        this.outputTopic = outputTopic;
        this.op = op;
        
        // Subscribe to input topics
        TopicManagerSingleton.get().getTopic(inputTopic1).subscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).subscribe(this);
        
        // Register as publisher for output topic
        TopicManagerSingleton.get().getTopic(outputTopic).addPublisher(this);
    }
    
    @Override
    public String getName() {
        return agentName;
    }
    
    @Override
    public void reset() {
        input1 = null;
        input2 = null;
    }
    
    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(inputTopic1)) {
            input1 = msg.asDouble;
        } else if (topic.equals(inputTopic2)) {
            input2 = msg.asDouble;
        }
        
        // If both inputs are available and valid, compute and publish result
        if (input1 != null && input2 != null && 
            !Double.isNaN(input1) && !Double.isNaN(input2)) {
            Double result = op.apply(input1, input2);
            TopicManagerSingleton.get().getTopic(outputTopic).publish(new Message(result));
        }
    }
    
    @Override
    public void close() {
        // Unsubscribe from topics
        TopicManagerSingleton.get().getTopic(inputTopic1).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(outputTopic).removePublisher(this);
    }
}
