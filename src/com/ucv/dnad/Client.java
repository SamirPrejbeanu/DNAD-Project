package com.ucv.dnad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by S on 6/22/2015.
 */
public class Client extends JFrame{

    private static final String SERVER_MASK = "localhost";
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9898;
    private static final String CON_SUCCESS = "Connection established with server: " + SERVER_IP;
    private static final String CON_EERROR = "Error connecting to server: " + SERVER_IP +". Please insert a valid IP address";
    private static List<String> tasksList;
    public boolean executingTask = false;

    private boolean taskStatus;

    private BufferedReader in;
    private PrintWriter out;

    private JTextField conIPField = new JTextField(40);
    private JButton connectionButton = new JButton("Connect");
    private JLabel messageArea = new JLabel();
    private JComboBox<String> taskList = new JComboBox<>();
    private JButton getTasksButton = new JButton("Get Tasks");
    private JButton executeTaskButton = new JButton("Execute Task");
    private JTextArea taskResultArea = new JTextArea(6,300);
    private JScrollPane scroll = new JScrollPane(taskResultArea);

    private List<String> clientTasks;
    private String requestedTask;
    private String serverResponse;
    private String clientMessage;

    public Client(){
        super("DNAD Project");
        this.setSize(500,350);
        this.setLocationRelativeTo(null);
        this.setLayout(null);

        this.add(conIPField);
        this.add(connectionButton);
        this.add(messageArea);
        this.add(taskList);
        this.add(getTasksButton);
        this.add(executeTaskButton);
        this.add(scroll);
        Insets bounds = this.getInsets();

        messageArea.setBounds(20 + bounds.left,15 + bounds.bottom , 440 , 30);
        conIPField.setBounds(20 + bounds.left,65 + bounds.bottom ,200,30);
        connectionButton.setBounds(250 + bounds.left,65 + bounds.bottom, 150,30);
        taskList.setBounds(20 + bounds.left,100 + bounds.bottom,200,30);
        getTasksButton.setBounds(250 + bounds.left, 100 + bounds.bottom,150,30);
        executeTaskButton.setBounds(20 + bounds.left,150+ bounds.bottom,200,30);
        scroll.setBounds(20  + bounds.left, 200 + bounds.bottom,440,100);

        messageArea.setText("Insert Server Adress ");
        taskList.addItem("=========================");

        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String userInputIP = conIPField.getText();
                if(userInputIP.equals(SERVER_MASK) || userInputIP.equals(SERVER_IP))
                    try {
                        connectToServer(userInputIP,SERVER_PORT);
                        messageArea.setText(CON_SUCCESS);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                else
                    messageArea.setText(CON_EERROR);
            }
        });

        getTasksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendListRequestToServer();
                try{
                    serverResponse = in.readLine();
                    if(serverResponse == null || serverResponse.isEmpty()){
                        System.out.println("Connection Error");
                        System.exit(0);
                    }
                    String[] responseTasks = serverResponse.split(" ");
                    for(int i = 0 ; i < responseTasks.length;i++) {
                        taskList.addItem(responseTasks[i]);
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        taskList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JComboBox comboBox = (JComboBox)actionEvent.getSource();
                requestedTask = (String)comboBox.getSelectedItem();
            }
        });

        executeTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendTaskRequestToServer(requestedTask);
                if(!executingTask){
                    try {
                        serverResponse = in.readLine();
                        String filePath = "C:\\Users\\SP\\Desktop\\DNAD\\Project DNAD\\";
                        filePath+=requestedTask+".java";
                        writeTaskContent(filePath,serverResponse);
                        System.out.println(filePath);
                        taskResultArea.setText("Executing task \"" + requestedTask + "\" at " + filePath + ".\n");
                        executeTask();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
                else
                    System.out.println("Task Executing");
            }
        });

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * Send request to server.
     */
    private void sendListRequestToServer() {
        out.println("Client requests:" + ClientRequest.GET_TASK_LIST);
        System.out.println("Client requested: " + ClientRequest.GET_TASK_LIST);
    }

    private void sendTaskRequestToServer(String taskCode) {
        if(taskCode.equals("Sum")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Client requests:" + ClientRequest.GET_TASK_SUM);
            out.println(stringBuilder);
            System.out.println("Client requested: " + ClientRequest.GET_TASK_SUM);
        }
        else if(taskCode.equals("Pow")){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Client requests:" + ClientRequest.GET_TASK_POW);
            out.println(stringBuilder);
            System.out.println("Client requested: " + ClientRequest.GET_TASK_POW);
        }
        else if(taskCode.equals("Crack")){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Client requests:" + ClientRequest.GET_TASK_CRACK);
            out.println(stringBuilder);
            System.out.println("Client requested: " + ClientRequest.GET_TASK_CRACK);
        }

    }

    /**
     * Method that executes the task given by the server as a file.
     */
    public void executeTask() {
        executingTask = true;
        try {
            Process p;
            p = Runtime.getRuntime().exec("javac "+requestedTask+".java");
            p.waitFor();
            p = Runtime.getRuntime().exec("java "+requestedTask);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            taskResultArea.setText("Task result -> " + line + "\n");

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Client requests: " + ClientRequest.FINISHED);
            stringBuilder.append("\nTask (\""+requestedTask+"\") result:" + line);
            System.out.println(stringBuilder);
            out.println(stringBuilder);
            executingTask = false;
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void writeTaskContent(String Path,String Content){
        try {

            File file = new File(Path);
            FileWriter writer = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(Content);
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that makes the connection to the server by creating a socket and instanciating the PrinterWriter and the BufferedReader objects.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void connectToServer(String IP,int Port) throws IOException {

        Socket socket = new Socket(IP, Port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
    }

}
