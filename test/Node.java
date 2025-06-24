package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
    private String name;
    private List<Node> edges;
    private Message message;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
        this.message = null;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void addEdge(Node other) {
        edges.add(other);
    }

    public boolean hasCycles() {
        Set<Node> visited = new HashSet<>();
        Set<Node> recursionStack = new HashSet<>();
        return hasCyclesUtil(visited, recursionStack);
    }

    private boolean hasCyclesUtil(Set<Node> visited, Set<Node> recursionStack) {
        visited.add(this);
        recursionStack.add(this);

        for (Node neighbor : edges) {
            if (!visited.contains(neighbor)) {
                if (neighbor.hasCyclesUtil(visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(this);
        return false;
    }
}