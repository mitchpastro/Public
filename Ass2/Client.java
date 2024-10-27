/*  Client. Requests .json file and stores locally.

    Takes no arguments

    Will preform the following actions
        1) Look for Aggregation Server
        2) Wait for input from terminal
        3) Recive response from Aggregation server
        4) Apply instructions from Aggregation server
        5) Close up socket
        6) Print out contents of local .json file if possible
        7) Apply instructions
        8) Once exit is typed, close the server
        
    Terminal commands
        1) Over - Stops the client and prints local .json file if possible
        2) GET - Requests local .json from Aggregation server
*/

 package Ass2;

import java.io.*;
import java.nio.file.*;
import java.net.*;
import java.util.Scanner;
//import Gson;
import java.util.concurrent.TimeUnit;

public class Client {

    private Socket local_socket = null;
    private BufferedReader BR = null;
    private DataInputStream DIS = null;
    private DataOutputStream DOS = null;
    Integer tick = 0;

    public Client(String address, int port) {

        System.out.println("Client started at Tick " + tick);



        // 1) Look for Aggregation Server
        System.out.println("    Setting up Connection");

        while(true) {
            try {
                local_socket = new Socket(address, port);
                BR     = new BufferedReader(new InputStreamReader(System.in));
                DIS    = new DataInputStream(local_socket.getInputStream());
                DOS    = new DataOutputStream(local_socket.getOutputStream());
                break;
            } catch (UnknownHostException u) {
                System.out.println("UnknownHostException: " + u);
                System.out.println("Trying again in one second...");
                System.out.println("");
            } catch (IOException e) {
                System.out.println("IOException: " + e);
                System.out.println("Trying again in one second...");
                System.out.println("");
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException during constructor: " + e);
            } 
        }
        

        System.out.println("    Connected, type Over to close Client gracefully.");

        String terminal_line = "";
        String Receive_line_and_tick = "";
        String[] Receive_line_and_tick_parse;
        String Receive_line = "";
        String local_filename = "Client.json";
        int in_tick = 0;



        // 2) Wait for input from terminal
        while(!terminal_line.equals("Over")) {
            try {
                tick = tick + 1;
                terminal_line = BR.readLine();
                System.out.println("    Sending command: '" + terminal_line + "' at tick " + tick);
                DOS.writeUTF(terminal_line + " " + tick);



                // 3) Recive response from Aggregation server
                Receive_line_and_tick = DIS.readUTF();
                Receive_line_and_tick_parse = Receive_line_and_tick.split(" ");
                in_tick = Integer.parseInt(Receive_line_and_tick.substring(Receive_line_and_tick.lastIndexOf(" ") + 1));
                tick = Math.max(tick, in_tick) + 1;
                Receive_line = Receive_line_and_tick.substring(0, Receive_line_and_tick.length() - Integer.toString(in_tick).length() - 1);
                System.out.println("    Received '" + Receive_line + "' at tick " + tick);



                // 4) Apply instructions from Aggregation server
                if(terminal_line.equals("Over")) {
                    System.out.println("    A request to stop the client has been recived at tick " + tick);
                }
                else if (terminal_line.equals("GET")) {
                    int bytes = 0;
                    FileOutputStream FOS = new FileOutputStream("Client.json");
                    long size = DIS.readLong();
                    byte[] byte_buffer = new byte[4 * 1024];
                    while (size > 0 && (bytes = DIS.read( byte_buffer, 0, (int)Math.min(byte_buffer.length, size))) != -1) {
                        FOS.write(byte_buffer, 0, bytes);
                        size -= bytes; 
                    }
                    System.out.println("    File is Received");
                    FOS.close();
                }
                else {
                    System.out.println("    Nothing is happening at tick " + tick);
                }

            } catch (IOException e) {
                System.out.println("IOException during while loop: " + e);
            }
        }



        // 5) Close up socket
        try {
            local_socket.close();
            BR.close();
            DIS.close();
            DOS.close();
        } catch (IOException e) {
            System.out.println("IOException when closing the connection: " + e);
            return;
        }
        System.out.println("Closing the connection");



        // 6) Print out contents of local .json file if possible
        System.out.println("Printing out the contents of the file if possible");
        try {
            System.out.println("    Filename to look for = " + local_filename);
            File local_File = new File(local_filename);

            if(local_File.exists()) {
                Scanner myReader = new Scanner(local_File);

                while (myReader.hasNextLine()) {
                    String read_line = myReader.nextLine();
                    System.out.println(read_line);
                }
                System.out.println("File name: " + local_File.getName());
                System.out.println("Absolute path: " + local_File.getAbsolutePath());
                System.out.println("Writeable: " + local_File.canWrite());
                System.out.println("Readable " + local_File.canRead());
                System.out.println("File size in bytes " + local_File.length());

                myReader.close();
            }
            else {
                System.out.println("There is no file with the name = " + local_filename);
            }
        } catch(IOException e) {
            System.out.println("FileNotFoundException " + e);
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 4567);
    }
}