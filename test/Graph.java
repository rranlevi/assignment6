package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Graph extends ArrayList<Node> {
    
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }
    
    public static Graph createFromTopics(TopicManagerSingleton.TopicManager tm) {
        Graph graph = new Graph();
        Map<String, Node> nodeMap = new HashMap<>();
        
        // Get or create node helper method
        Function<String, Node> getOrCreateNode = name -> nodeMap.computeIfAbsent(name, Node::new);
        
        for (Topic topic : tm.getTopics()) {
            // Create topic node if it has subscribers
            Node topicNode = getOrCreateNode.apply("T" + topic.name);

            if (!topic.getSubscribers().isEmpty()) {
                // Add edges from topic to each subscriber agent
                for (Agent agent : topic.getSubscribers()) {
                    Node agentNode = getOrCreateNode.apply("A" + agent.getName());
                    topicNode.addEdge(agentNode);
                }
            }
            
            // Create publisher agent nodes and add edges to topic
            if (!topic.getPublishers().isEmpty()) {
                for (Agent agent : topic.getPublishers()) {
                    Node agentNode = getOrCreateNode.apply("A" + agent.getName());
                    agentNode.addEdge(topicNode);
                }
            }
        }
        
        // Add all nodes to the graph
        graph.addAll(nodeMap.values());
        return graph;
    }
    
    public void createFromTopics() {
        this.clear();
        Graph newGraph = createFromTopics(TopicManagerSingleton.get());
        this.addAll(newGraph);
    }
}
