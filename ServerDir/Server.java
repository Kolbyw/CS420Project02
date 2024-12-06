package ServerDir;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;

public class Server {
    public static void main(String[] args) {
        try {
            int port = 1099;

            ServerClass server = new ServerClass();
            LocateRegistry.createRegistry(port);
            Naming.rebind("Server", server);

            System.out.println("Server is ready.");
            server.printOTP();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

class ServerClass extends UnicastRemoteObject implements ServerInterface {
    private String otp = "";

    public ServerClass() throws RemoteException {
        for (int x = 0; x < 5; x++) {
            int rand = (int) (Math.random() * 9);
            String number = String.valueOf(rand);
            otp = otp + number;
        }
    }

    @Override
    public void printOTP() throws RemoteException {
        System.out.println("One Time Password: " + this.otp);
    }

    @Override
    public String verifyOTP(String otp) throws RemoteException {
        if (this.otp.equals(otp)) {
            return ("Hello");
        } else {
            return ("Sorry, that is not the correct OTP\n");
        }
    }

    @Override
    public String putFile(String fileName, byte[] fileData, byte[] crcBytes) throws RemoteException {
        int receivedCRC = CRCCalculations.extractCRC(crcBytes);
        int checksum = CRCCalculations.calculateCRC(fileData);

        if (receivedCRC == checksum) {
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\user\\Documents\\CS420\\Project02\\ServerDir\\Files\\" + fileName)) {
                fileOut.write(fileData);
                System.out.println("Received and saved file: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Good data.";
        } else {
            return "Bad data.";
        }
    }

    @Override
    public byte[] getFile(String fileName) throws RemoteException {
        File file = new File("C:\\Users\\user\\Documents\\CS420\\Project02\\ServerDir\\Files\\" + fileName);
        if (file.exists()) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                int crc = CRCCalculations.calculateCRC(fileData);
                byte[] crcBytes = CRCCalculations.getCRCBytes(crc);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(fileData);
                byteArrayOutputStream.write(crcBytes);
                return byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("File not found: " + fileName);
        return new byte[0];
    }
}