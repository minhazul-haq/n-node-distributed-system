import java.io.*;
import java.net.Socket;


public class MessageHandler implements Runnable {
    private int totalProcesses;
    private BufferedReader reader;
    private PrintWriter writers[];

    public MessageHandler(Socket sockets[], int processId) throws IOException {
        this.totalProcesses = Constants.TOTAL_PROCESSES;
        this.reader = new BufferedReader(new InputStreamReader(sockets[processId].getInputStream()));
        this.writers = new PrintWriter[totalProcesses];

        for(int i=0; i<totalProcesses; i++) {
            this.writers[i] = new PrintWriter(sockets[i].getOutputStream(), true);
        }
    }

    @Override
    public void run() {
        while(true) {
            String completeMessage = null;

            try {
                completeMessage = reader.readLine();

                Message message = ProcessUtils.parseAndGenerateMessageObject(completeMessage);
                int receiverId = message.getReceiverId();

                if (receiverId == Constants.TYPE_BROADCAST) {
                    for(int i=0; i<totalProcesses; i++) {
                        writers[i].println(completeMessage);
                    }
                } else {
                    writers[receiverId].println(completeMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
