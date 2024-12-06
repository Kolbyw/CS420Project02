package ClientDir;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import ServerDir.ServerInterface;
import ServerDir.CRCCalculations;

public class Client {
    private String host = "localhost";
    private int port = 1099;
    private ServerInterface server;
    private boolean authenticated;

    public Client() throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        server = (ServerInterface) Naming.lookup("rmi://localhost/Server");
        authenticated = false;
    }

    public void authenticate(String otp) throws RemoteException {
        String msg = server.verifyOTP(otp);
        if (msg.equals("Hello")) {
            System.out.println("You have been authenticated.");
            this.authenticated = true;
        } else {
            System.out.println(msg);
        }
    }

    public void putFile(String fileName) throws RemoteException {
        try {
            File clientFile = new File("C:\\Users\\user\\Documents\\CS420\\Project02\\ClientDir\\Files\\" + fileName);
            byte[] fileData = new byte[(int) clientFile.length()];
            try (FileInputStream fileIn = new FileInputStream(clientFile)) {
                fileIn.read(fileData);
            }

            // Calculate CRC checksum of the file data
            int checksum = CRCCalculations.calculateCRC(fileData);
            byte[] crcBytes = CRCCalculations.getCRCBytes(checksum);

            System.out.println("Uploading file " + fileName);
            String status = server.putFile(fileName, fileData, crcBytes);
            System.out.println("File upload status: " + status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFile(String filename) throws RemoteException {
        System.out.println("Downloading file " + filename);
        byte[] fileDataWithCRC = server.getFile(filename);
        
        // get check sum (last 2 bytes)
        byte[] fileData = new byte[fileDataWithCRC.length - 2];
        System.arraycopy(fileDataWithCRC, 0, fileData, 0, fileData.length);
        byte[] crcBytes = new byte[2];
        System.arraycopy(fileDataWithCRC, fileData.length, crcBytes, 0, 2);

        // calculate the crc
        int receivedCRC = CRCCalculations.extractCRC(crcBytes);
        int calculatedCRC = CRCCalculations.calculateCRC(fileData);

        if (receivedCRC == calculatedCRC) {
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\user\\Documents\\CS420\\Project02\\ClientDir\\Files\\" + filename)) {
                fileOut.write(fileData);
                System.out.println("File downloaded and verified successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Bad Data.");
        }
    }

    public boolean getAuthenticatedStatus() {
        return (authenticated);
    }

    public static void main(String[] args) {
        try {
            String host = "localhost";
            int port = 1099;

            Client client = new Client();

            Scanner scanner = new Scanner(System.in);
            boolean authenticated = false;
            while (authenticated == false) {
                System.out.print("Enter the Server given OTP: ");
                String otp = scanner.nextLine();
                client.authenticate(otp);
                authenticated = client.getAuthenticatedStatus();
            }

            while (true) {
                System.out.println(
                        "\nEnter 'put <filename>' to upload a file, 'get <file_name>' to receive a file, or 'quit' to end session: ");
                String input = scanner.nextLine();
                String command = input.substring(0, 3);
                String fileName = input.substring(4);

                if (input.equals("quit")) {
                    break;
                }

                if (command.equals("put")) {
                    client.putFile(fileName);
                } else {
                    client.getFile(fileName);
                }
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}