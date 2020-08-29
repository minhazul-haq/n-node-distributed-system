import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;


public class Process {
    private int processId;
    private boolean isDaemon;
    private boolean multicastWithVectorClocks;
    private int totalProcesses;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private int localClock;

    public Process(int processId, int vectorClockChoice) throws IOException {
        this.processId = processId;
        this.isDaemon = (processId == Constants.DAEMON_PROCESS_ID);
        this.multicastWithVectorClocks = (vectorClockChoice!=0);
        this.totalProcesses = Constants.TOTAL_PROCESSES;
        this.socket = new Socket(Constants.HOST_ADDRESS, Constants.PORT_NUMBER);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //randomly initialize local clock
        this.localClock = (int)((Math.random() * 30) + 1); //localClock will have a value between 1 and 30
    }

    private void runProcess() throws IOException, InterruptedException {
        int iteration = 0;

        while(true) {
            if (iteration==10) {
                synchronizeUsingBerkeley();
                System.out.println("Synchronized local clock: " + localClock);

                if (multicastWithVectorClocks) {
                    multicastMesssagesWithVectorClocks();
                } else {
                    multicastMessagesWithoutVectorClocks();
                }

                iteration = 0;
            } else {
                localClock += (int)((Math.random() * 5) + 1); //increment localClock by 1~5
                System.out.println("Local clock: " + localClock);
                iteration++;
            }

            Thread.sleep(1000);
        }
    }

    private void synchronizeUsingBerkeley() throws IOException {
        //broadcast daemon's local clock
        if (isDaemon) {
            ProcessUtils.broadcastMessage(writer, processId, Integer.toString(localClock));
        }

        //read broadcasted message from daemon
        Message messageFromDaemon = ProcessUtils.readMessage(reader);
        int daemonLocalClock = Integer.parseInt(messageFromDaemon.getMessage());

        //compute time difference and send it back to daemon
        int timeDifference = localClock - daemonLocalClock;
        ProcessUtils.sendMessage(writer, processId, Constants.DAEMON_PROCESS_ID, Integer.toString(timeDifference));

        //compute average and tells other how to adjust
        if (isDaemon) {
            int localTimeDifferences[] = new int[totalProcesses];
            int totalTimeDifference = 0;

            //get timeDifferenceMessage from each of the processes including daemon itself
            for (int i=0; i<totalProcesses; i++) {
                Message timeDifferenceMessage = ProcessUtils.readMessage(reader);
                int senderId = timeDifferenceMessage.getSenderId();
                String timeDifferenceStr = timeDifferenceMessage.getMessage();

                localTimeDifferences[senderId] = Integer.parseInt(timeDifferenceStr);
                totalTimeDifference += localTimeDifferences[senderId];
            }

            //compute average
            int averageTimeDifference = totalTimeDifference / totalProcesses;

            //send adjusting time to each of the processes including daemon itself
            for (int i=0; i<totalProcesses; i++) {
                ProcessUtils.sendMessage(writer, processId, i, Integer.toString(-localTimeDifferences[i] + averageTimeDifference));
            }
        }

        //receive adjusting time
        Message adjustingMessage = ProcessUtils.readMessage(reader);
        localClock = localClock + Integer.parseInt(adjustingMessage.getMessage());
    }

    private void multicastMesssagesWithVectorClocks() throws IOException, InterruptedException {
        int vc[] = new int[totalProcesses];

        //initialize vector clocks with synchronized local clocks
        for(int i=0; i<totalProcesses; i++) {
            vc[i] = localClock;
        }

        //1. Before sending a message, Pi executes VCi[i] = VCi[i] + 1
        vc[processId] = vc[processId] + 1;

        //2. When process Pi sends a message m to Pj, it sets m’s (vector) timestamp
        //ts (m) equal to VCi after having executed the previous step
        Thread.sleep(processId*100);

        //multicast message
        for(int receiverId=0; receiverId<totalProcesses; receiverId++) {
            if (receiverId!=processId) {
                int messageId = 1;
                String messageToSend = processId + "." + messageId;
                ProcessUtils.sendMessageVC(writer, processId, receiverId, messageToSend, vc);
            }
        }

        //3. Read all messages and append to a list
        List messageList = new ArrayList<MessageVC>();
        int totalMessagesToRead = totalProcesses - 1;

        while(totalMessagesToRead > 0) {
            MessageVC messageVC = ProcessUtils.readMessageVC(reader);
            messageList.add(messageVC);

            totalMessagesToRead--;
        }

        //delivery messages to user when both conditions are met
        System.out.println("Received messages using vector clocks:");

        while(!messageList.isEmpty()) {
            for (Iterator<MessageVC> iter = messageList.listIterator(); iter.hasNext(); ) {
                MessageVC message = iter.next();
                int i = message.getSenderId();
                int ts[] = message.getVectorClocks();

                //check if message is deliverable to user
                boolean isDeliverable = (ts[i] == vc[i] + 1);

                for(int k=0; k<totalProcesses; k++) {
                    if (k!=i) {
                        isDeliverable = isDeliverable && (ts[k] <= vc[k]);
                    }
                }

                if (isDeliverable) {
                    for(int k=0; k<totalProcesses; k++) {
                        vc[k] = Math.max(vc[k], ts[k]);
                    }

                    System.out.println(message.getMessage());
                    iter.remove();
                }
            }
        }
    }

    private void multicastMessagesWithoutVectorClocks() throws IOException {
        //multicast message
        for(int receiverId=0; receiverId<totalProcesses; receiverId++) {
            if (receiverId!=processId) {
                int messageId = 1;
                String messageToSend = processId + "." + messageId;
                ProcessUtils.sendMessage(writer, processId, receiverId, messageToSend);
            }
        }

        //receive multicasted messages
        System.out.println("Received messages without using vector clocks:");
        int totalMessagesToRead = totalProcesses - 1;

        while(totalMessagesToRead > 0) {
            Message message = ProcessUtils.readMessage(reader);
            System.out.println(message.getMessage());

            totalMessagesToRead--;
        }
    }

    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException {
        Scanner input = new Scanner(System.in);

        System.out.print("Please enter process_id: ");
        int processId = input.nextInt();

        System.out.print("Want to test multicast WITH or WITHOUT vector clocks?");
        System.out.println("Type 0 for WITHOUT vector clocks, Type 1 for WITH vector clocks: ");
        int vectorClockChoice = input.nextInt();

        Process process = new Process(processId, vectorClockChoice);
        process.runProcess();
    }
}
