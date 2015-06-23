package com.ucv.dnad;

/**
 * Created by S on 6/22/2015.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The Class Server.
 */
public class Server {

    public static List<Task> taskList = new ArrayList<>();
    /**
     * The Inner Class ConnectionThread.
     */
    private static class ConnectionThread extends Thread {

        /**
         * The connection socket that assures the communication with the client.
         */
        private Socket socket;

        /**
         * The connected client's number.
         */
        private int clientNumber;

        /**
         * The BufferedReader object that makes possible sending messages to the client.
         */
        private BufferedReader in;

        /**
         * The PrinterWrter object that makes possible getting the messages from the client.
         */
        private PrintWriter out;

        private static Object lock = new Object();

        /**
         * Constructor
         *
         * @param socket       - the connection socket that assures the communication with the client
         * @param clientNumber - the connected client's number
         */
        public ConnectionThread(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("Connected to client " + clientNumber + " at " + socket);
        }

        /*
         * (non-Javadoc)
         * Overriden run method that instantatiates the BufferedReader and PrintWriter objects and is permanently
         * listening for messages from clients. Depending on the requests received, the list of available tasks or
         * a the first one from the list is sent.
         */
        public void run() {
            String requestedTask;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    String[] clientMessage = input.split(":");
                    String clientRequest = clientMessage[1];
                    System.out.println(clientRequest);
                    switch (ClientRequest.valueOf(clientRequest)) {

                        case GET_TASK_LIST:
                            out.println(sendTaskList(taskList));
                            break;

                        case GET_TASK_SUM:
                            sendTaskToClient("Sum");
                            break;

                        case GET_TASK_POW:
                            sendTaskToClient("Pow");
                            break;

                        case GET_TASK_CRACK:
                            sendTaskToClient("Crack");
                            break;

                        case FINISHED:
                            log(clientRequest);
                            break;

                    }
                }
            } catch (IOException e) {
                log("Error handling client " + clientNumber + ": " + e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket.");
                }
                log("Connection with client " + clientNumber + " closed");
            }
        }

       public String sendTaskList(List<Task> taskList)throws IOException, InterruptedException{
           String tasksList = "";

           for (int i = 0 ; i < taskList.size() ; i++ ) {
               Task task = taskList.get(i);
               tasksList += task.getTaskName() +" ";
           }
           return tasksList;
       }
        /**
         * Method that prints a message to the console.
         *
         * @param message the message
         */
        private void log(String message) {
            System.out.println(message);
        }

        /**
         * Method that send a java file to the client in a StringBuilder
         *
         * @throws IOException          Signals that an I/O exception has occurred.
         * @throws InterruptedException the interrupted exception
         */
        public void sendTaskToClient(String requestedTask) throws IOException, InterruptedException {
            synchronized (lock) {
                for (Iterator<Task> iterator = taskList.iterator(); iterator.hasNext(); ) {
                    Task task = iterator.next();

                    if (task.getTaskName().equals(requestedTask)) {
                        iterator.remove();
                        log(requestedTask);

                        BufferedReader br = new BufferedReader(new FileReader(new File("res/tasks/"+requestedTask + ".java")));
                        String line;
                        StringBuilder javaTask = new StringBuilder();
                        System.out.println("Client_Requested_Task.\nTask.Name: " + requestedTask + " Task.Complexity " + task.getComplexityLevel());
                        while ((line = br.readLine()) != null){
                            javaTask.append(line);
                       }
                        out.println(javaTask);
                        br.close();
                    }
                }
                out.println("Task -> Unavailable");
            }
        }
    }

    public static void loadTaskList(List<Task> taskList){
        Task t = new Task("Sum","/tasks/Sum.java",1);
        taskList.add(t);
        t = new Task("Pow","/tasks/Pow.java",1);
        taskList.add(t);
        t = new Task("Crack","/tasks/Crack.java",5);
        taskList.add(t);
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        loadTaskList(taskList);
        System.out.println("The server is running.");
        int clientNumber = 1;
        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new ConnectionThread(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }
}