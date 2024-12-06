package ServerDir;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void printOTP() throws RemoteException;
    String verifyOTP(String otp) throws RemoteException;
    String putFile(String fileName, byte[] fileData, byte[] crcBytes) throws RemoteException;
    byte[] getFile(String fileName) throws RemoteException;
}