package test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenericConfig implements Config {
    private Path confFile;
    private final List<Agent> agents = new ArrayList<>();
    
    public void setConfFile(Path file) {
        this.confFile = file;
    }
    
    // Method that accepts a String path
    public void setConfFile(String path) {
        this.setConfFile(java.nio.file.Paths.get(path));
    }

    @Override
    public void create() {
        try {
            List<String> lines = Files.readAllLines(confFile);
            
            // Remove empty lines and trim whitespace
            lines.removeIf(line -> line.trim().isEmpty());
            
            // Ensure we have a multiple of 3 lines
            if (lines.size() % 3 != 0) {
                throw new RuntimeException("Configuration file must contain triplets of lines");
            }
            
            // Process every three lines as an agent configuration
            for (int i = 0; i < lines.size(); i += 3) {
                String className = lines.get(i).trim();
                String[] subs = lines.get(i + 1).trim().split(",");
                String[] pubs = lines.get(i + 2).trim().split(",");
                
                try {                    
                    // Use reflection to instantiate the agent
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> ctor = clazz.getConstructor(String[].class, String[].class);
                    Agent agent = (Agent) ctor.newInstance(new Object[]{subs, pubs});
                    
                    // Register agent with topics
                    registerAgentWithTopics(agent, subs, pubs);
                    
                    // Wrap with ParallelAgent for concurrent execution
                    ParallelAgent parallelAgent = new ParallelAgent(agent, 10);
                    agents.add(parallelAgent);
                      } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | 
                        IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    throw new RuntimeException("Failed to create agent " + className, e);
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file", e);
        }
    }
    
    @Override
    public void close() {
        for (Agent agent : agents) {
            agent.close();
        }
        agents.clear();
    }
    
    @Override
    public String getName() {
        return "Generic Configuration";
    }
    
    @Override
    public int getVersion() {
        return 1;
    }
    
    private void registerAgentWithTopics(Agent agent, String[] subs, String[] pubs) {
        // Subscribe to input topics
        for (String topic : subs) {
            TopicManagerSingleton.get().getTopic(topic).subscribe(agent);
        }
        
        // Register as publisher for output topics
        for (String topic : pubs) {
            TopicManagerSingleton.get().getTopic(topic).addPublisher(agent);
        }
    }
}
