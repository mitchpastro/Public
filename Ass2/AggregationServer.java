/*  Aggregation Server. Receives, stores from Content server 
    and provides to clients.

    Takes no arguments

    Will preform the following actions
        1) Set up a Server socket and relative connections
        2) Set up exiting thread
        3) Set up stop watch thread
        4) Wait for socket to connect and create thread
        5) Check if it is a Content server or Client
        6) Sets up data streams
        7) Recive instrucions, update the clock and parse instructions (in that order)
        8) Apply instructions
        9) Once exit is typed, close the server
        
    Terminal commands
        1) exit - close the server
*/

package Ass2;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
//import org.json.simple.JSONObject/;

public class AggregationServer {
    public AggregationServer(int port) throws IOException {

        var clock = new Object() {
            int tick = 0;
            int stop_watch = (int) (System.nanoTime()/1000000000);
        };



        // 1) Set up a Server socket and relative connections
        System.out.println("Aggregation Server started at tick " + clock.tick);
        System.out.println("    Setting up server scanner");
        ServerSocket local_server = new ServerSocket(port);
        Scanner read_terminal = new Scanner(System.in);
        AtomicReference<String> local_filename = new AtomicReference<>("");



        // 2) Set up exiting thread
        System.out.println("    Initializing exit thread, type 'exit' to end server gracefully.");
        new Thread(() -> {
            String waiting_for_exit = "";
            while(!waiting_for_exit.equals("exit")) {
                waiting_for_exit = read_terminal.nextLine();
                System.out.println("    Server says: " + waiting_for_exit + " at tick " + clock.tick);}
            System.out.println("Closing this server gracefully.");
            System.exit(0);
        }).start();


        
        // 3) Set up stop watch thread
        new Thread(() -> {
            while(true) {
                if(System.nanoTime()/1000000000 - clock.stop_watch > 5) {
                    clock.stop_watch = (int) (System.nanoTime()/1000000000);
                    PrintWriter PW;
                    try {
                        PW = new PrintWriter("AggregationServer.json");
                        PW.print("");
                        PW.close();
                        System.out.println("    Local .json file has been cleared");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        }).start();



        // 4) Wait for socket to connect and create thread
        System.out.println("    Waiting for connect to client or content server.");
        while (true) {
            Socket mysocket = local_server.accept();
            System.out.println("       Socket accepted, making new thread.");
            new Thread(() -> {



                // 5) Check if it is a Content server or Client
                System.out.println("    Are you the Content Server or a client? At tick " + clock.tick);
                String Address = mysocket.getLocalAddress().toString();



                // 5) Set up data streams
                if(Address.equals("/127.0.0.2")) {
                    System.out.println("        I'm the Content Server");
                    DataInputStream DIS;
                    DataOutputStream DOS;

                    try {
                        DIS = new DataInputStream(mysocket.getInputStream());
                        DOS = new DataOutputStream(mysocket.getOutputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }



                    // 7) Recive instrucions, update the clock and parse instructions (in that order)
                    System.out.println("        Connection to the CS completed, begin communication.");
                    String instruction = "";

                    while (!instruction.equals("exit")) {
                        try {
                            
                            // Recive instructions
                            String instruction_and_tick = DIS.readUTF();
                            String[] instruction_and_tick_parse = instruction_and_tick.split(" ");

                            // Update clock tick
                            int in_tick = Integer.parseInt(instruction_and_tick.substring(instruction_and_tick.lastIndexOf(" ") + 1));
                            clock.tick = Math.max(clock.tick, in_tick) + 1;

                            // Parse instructions
                            instruction = instruction_and_tick.substring(0, instruction_and_tick.length() - Integer.toString(in_tick).length() - 1);
                            System.out.println("        Content server says: '" + instruction_and_tick_parse[0] + "' at tick " + clock.tick);



                            // 8) Apply instructions
                            if (instruction_and_tick_parse[0].equals("exit")) {
                                System.out.println("    Disconnecting from content server at tick " + clock.tick);
                            }
                            else if(instruction_and_tick_parse[0].equals("PUT")) {
                                    int bytes = 0;
                                    FileOutputStream FOS = new FileOutputStream("AggregationServer.json");
                                    long size = DIS.readLong();
                                    byte[] byte_buffer = new byte[4 * 1024];

                                    while (size > 0 && (bytes = DIS.read( byte_buffer, 0, (int)Math.min(byte_buffer.length, size))) != -1) {
                                        FOS.write(byte_buffer, 0, bytes);
                                        size -= bytes;
                                    }
                                    System.out.println("    File is Received, reseting clearing timer");
                                    clock.stop_watch = (int) (System.nanoTime()/1000000000);

                                    FOS.close();
                            }
                            else {
                                System.out.println("    Nothing happens, but the stop watch has reset");
                                clock.stop_watch = (int) (System.nanoTime()/1000000000);
                            }

                            DOS.writeUTF("tick " + clock.tick);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("        The connection to CS has been closed.");
                }



                // 5) Set up data streams
                else {
                    System.out.println("        I'm a Client.");
                    DataInputStream DIS;
                    DataOutputStream DOS;

                    try {
                        DIS = new DataInputStream(mysocket.getInputStream());
                        DOS = new DataOutputStream(mysocket.getOutputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }



                    // 7) Recive instrucions, update the clock and parse instructions (in that order)
                    System.out.println("        Connection to client completed, begin communication.");
                    String instruction = "";

                    while (!instruction.equals("Over")) {
                        try {
                            // Recive instructions
                            String instruction_line_and_tick = DIS.readUTF();
                            String[] instruction_and_tick_parse = instruction_line_and_tick.split(" ");

                            // Update clock tick
                            int in_tick = Integer.parseInt(instruction_line_and_tick.substring(instruction_line_and_tick.lastIndexOf(" ") + 1));
                            clock.tick = Math.max(clock.tick, in_tick) + 1;

                            // Parse instructions
                            instruction = instruction_line_and_tick.substring(0, instruction_line_and_tick.length() - Integer.toString(in_tick).length() - 1);
                            System.out.println("        Client says: " + instruction + " at tick " + clock.tick);



                            // 8) Apply instructions
                            if (instruction.equals("Over")) {
                                DOS.writeUTF("Over and out " + clock.tick);
                            }
                            else if (instruction.equals("GET")) {
                                local_filename.set("AggregationServer.json");
                                DOS.writeUTF(String.valueOf(local_filename) + " " + clock.tick);
                                try {
                                    int bytes = 0;
                                    File local_file = new File("AggregationServer.json");
                                    FileInputStream FIS = new FileInputStream(local_file);
                                    DOS.writeLong(local_file.length());
                                    byte[] buffer = new byte[4 * 1024];
                                    while ((bytes = FIS.read(buffer)) != -1) {
                                        DOS.write(buffer, 0, bytes);
                                        DOS.flush();
                                    }
                                    FIS.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                System.out.println("    Nothing occours at tick " + clock.tick);
                                DOS.writeUTF("Nothing " + clock.tick);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("   A connection to a client has been closed.");
                }
            }).start();
        }
    }

    public static void main(String[] args) throws IOException {
        AggregationServer server = new AggregationServer(4567);
    }

}
