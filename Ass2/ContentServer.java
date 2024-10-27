/*  Content Server. Keeps the Aggregation server up to date.

    Takes the following arguments in order...
        1) IP address
        2) Port number
        3) Local filename

    Will preform the following actions
        1) Checks for the input file.
        2) Parse the input file and generate local .json file based on it
        3) Print out contents of local .json file and some details about it
        4) Attempt to connect to Aggregation Server
        5) Wait for user input
        6) Exit

    Terminal commands
        1) exit - Close the server
        2) PUT - Sends copy of input file to Aggregation Server
    
*/

package Ass2;

import java.io.*;
import java.net.*;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ContentServer {
 
    private Socket local_socket = null;
    private BufferedReader BR = null;
    private DataInputStream DIS = null;
    private static DataOutputStream DOS = null;
    
    Integer tick = 0;
    String local_filename = "ContentServer.json";
    // THIS IS A TEST
     
    public ContentServer(String address, int port, String inputfile) {
    
        System.out.println("Content Server started at tick " + tick);
    


        // 1) Checks for the input file.
        System.out.println("    Looking for " + local_filename + " file");
        try {
            File in_file = new File(inputfile);
            File local_file = new File(local_filename);

            if (local_file.createNewFile()) {
                System.out.println("        File created: " + local_file.getName());
            } else {
                System.out.println("        " + local_filename + " file already exists.");
            }

            FileInputStream FIS = new FileInputStream(in_file);
            FileOutputStream FOS = new FileOutputStream(local_file);
            FileReader FR = new FileReader(inputfile);
            BufferedReader  BR = new BufferedReader(FR);
            BufferedWriter BW = new BufferedWriter(new FileWriter(local_filename, true));



            // 2) Parse the input file and generate local .json file based on it
            System.out.println("    Parseing the input file and generating .json file");
            BW.write("{\n");
            while (BR.ready()) {
                String data = BR.readLine();
                String[] data_parse = data.split(":", 2);
                
                if(isInt(data_parse[1])) {
                    BW.write("  \"" + data_parse[0] + "\": " + Integer.parseInt(data_parse[1]));
                }
                else if(isDou(data_parse[1])) {
                    BW.write("  \"" + data_parse[0] + "\": " + Double.parseDouble(data_parse[1]));
                }
                else {
                    BW.write("  \"" + data_parse[0] + "\": \"" + data_parse[1] + "\"");
                }

                if(BR.ready()) {
                    BW.write(",");
                }
                BW.write("\n");
            }
            BW.write("}");

            FIS.close();
            FOS.close();
            BR.close();
            BW.close();



            // 3) Print out contents of local .json file and some details about it
            System.out.println("    Printing contents and details of " + local_file.getName());
            System.out.println("--------------local file contents---------------");
            if(local_file.exists()) {
                Scanner myReader = new Scanner(local_file);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    System.out.println(data);
                }
                System.out.println("---------------local file details---------------");
                System.out.println("File name: " + local_file.getName());
                System.out.println("Absolute path: " + local_file.getAbsolutePath());
                System.out.println("Writeable: " + local_file.canWrite());
                System.out.println("Readable " + local_file.canRead());
                System.out.println("File size in bytes " + local_file.length());

                myReader.close();

            } else {
                System.out.println("The file does not exist.");
            }

            System.out.println("------------------------------------------------");

        } catch (IOException e) {
            System.out.println("An error has occurred with either creating or reading the " + local_filename + " file.");
        }



        // 4) Attempt to connect to Aggregation Server
        System.out.println("    Setting up Data In/Output Stream");
        while(true) {
            try {
                local_socket = new Socket(address, port);
                BR  = new BufferedReader(new InputStreamReader(System.in));
                DIS = new DataInputStream(local_socket.getInputStream());
                DOS = new DataOutputStream(local_socket.getOutputStream());
                break;
            } catch(IOException u) {
                System.out.println("IOException during constructor: " + u);
                System.out.println("Trying again in one second...");
                System.out.println("");
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException during constructor: " + e);
            } 
        }
        



        // 5) Wait for user input
        System.out.println("    Connected, type exit to close Content Server gracefully.");
        String line = "";
        while (!line.equals("exit")) {
            try {
                // Sending
                line = BR.readLine();
                tick = tick + 1;
                System.out.println("Sending line: '" + line + "' at tick " + tick);

                // Terminal    
                if(line.equals("PUT")) {
                    DOS.writeUTF(line + " " + tick);
                    try {
                        int bytes = 0;
                        File PUTfile = new File(local_filename);
                        FileInputStream FIS = new FileInputStream(PUTfile);
                        
                        DOS.writeLong(PUTfile.length());

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
                DOS.writeUTF(line + " " + tick);
                }

                // Recive and update clock tick
                String recive_in_tick = DIS.readUTF();
                String[] recive_in_tick_parse = recive_in_tick.split(" ");
                int in_tick = Integer.parseInt(recive_in_tick_parse[1]);
                tick = Math.max(tick, in_tick) + 1;
                System.out.println("The current tick is " + tick);

            }
            catch (IOException e) {
                System.out.println("IOException during the while loop: " + e);
            }
        }

        // 6) Exit
        System.out.println("If you are reading this, the Content Server has closed gracefully. CONGRATULATIONS");
        System.exit(0);
    }

    // MAIN DRIVER, START HERE
    public static void main(String[] args) {
        ContentServer contentserver = new ContentServer(args[0], Integer.parseInt(args[1]), args[2]);
        // ContentServer contentserver = new ContentServer("127.0.0.2", 4567);
    }

    /* Local methods, used for parsing numbers in input
        Input - string to check
        Output - true or false based on if string is a number
    */
    public static boolean isDou(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
}