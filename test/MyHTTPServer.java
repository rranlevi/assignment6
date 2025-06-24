package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import test.RequestParser.RequestInfo;

public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final ExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean closed = false;
    
    // Three thread-safe maps for GET, POST, and DELETE commands
    private final Map<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();
    
    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand);
        if (servletMap != null) {
            servletMap.put(uri, s);
        }
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand);
        if (servletMap != null) {
            servletMap.remove(uri);
        }
    }

    private Map<String, Servlet> getServletMap(String httpCommand) {
        if (httpCommand == null) {
            return null;
        }
        
        switch (httpCommand.toUpperCase()) {
            case "GET":
                return getServlets;
            case "POST":
                return postServlets;
            case "DELETE":
                return deleteServlets;
            default:
                return null;
        }
    }
    
    @Override
    public void start() {
        super.start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // 1s timeout for accept()
            
            while (!closed) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (!closed) {
                        executor.submit(() -> handleClient(clientSocket));
                    }
                } catch (SocketTimeoutException e) {
                    // This is expected due to the timeout, we just continue the loop
                }
            }
        } catch (IOException e) {
            if (!closed) {
                System.err.println("Error starting server on port " + port + ": " + e.getMessage());
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
            
            RequestInfo requestInfo = RequestParser.parseRequest(in);
            
            if (requestInfo != null) {
                String httpCommand = requestInfo.getHttpCommand();
                String uri = requestInfo.getUri();
                
                Map<String, Servlet> servletMap = getServletMap(httpCommand);
                if (servletMap != null) {
                    // Find the servlet with the longest matching prefix
                    String bestMatch = "";
                    Servlet servlet = null;
                    
                    for (Map.Entry<String, Servlet> entry : servletMap.entrySet()) {
                        String servletUri = entry.getKey();
                        if (uri.startsWith(servletUri) && servletUri.length() > bestMatch.length()) {
                            bestMatch = servletUri;
                            servlet = entry.getValue();
                        }
                    }
                    
                    if (servlet != null) {
                        servlet.handle(requestInfo, out);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        
        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        
        // Close all servlets
        closeServlets(getServlets.values());
        closeServlets(postServlets.values());
        closeServlets(deleteServlets.values());
    }
    
    private void closeServlets(Iterable<Servlet> servlets) {
        for (Servlet servlet : servlets) {
            try {
                servlet.close();
            } catch (IOException e) {
                System.err.println("Error closing servlet: " + e.getMessage());
            }
        }
    }
}
