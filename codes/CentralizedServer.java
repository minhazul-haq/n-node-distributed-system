import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class CentralizedServer {
    private ServerSocket serverSocket;
    private Socket sockets[];
    private Thread workerThreads[];
    private int totalProcesses;

    public CentralizedServer() throws IOException {
        serverSocket = new ServerSocket(Constants.PORT_NUMBER);
        totalProcesses = Constants.TOTAL_PROCESSES;
        sockets = new Socket[totalProcesses];
        workerThreads = new Thread[totalProcesses];
    }

    public void connectProcesses() throws IOException {
        for(int processId=0; processId<totalProcesses; processId++) {
            sockets[processId] = serverSocket.accept();
        }

        System.out.println("all processes are now connected...");
    }

    public void startWorkerThreads() throws IOException {
        for(int processId=0; processId<totalProcesses; processId++) {
            MessageHandler messageHandler = new MessageHandler(sockets, processId);
            workerThreads[processId] = new Thread(messageHandler);
            workerThreads[processId].start();
        }
    }

    public void closeConnections() throws IOException {
        for(int processId=0; processId<totalProcesses; processId++) {
            sockets[processId].close();
        }
    }

    public static void main(String args[]) throws IOException {
        CentralizedServer centralizedServer = new CentralizedServer();
        centralizedServer.connectProcesses();
        centralizedServer.startWorkerThreads();
    }
}
