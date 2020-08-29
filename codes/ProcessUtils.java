import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;


public class ProcessUtils {
    public static void sendMessage(PrintWriter writer, int senderId, int receiverId, String message) {
        String completeMessage = generateMessageString(senderId, receiverId, message);
        writer.println(completeMessage);
    }

    public static void broadcastMessage(PrintWriter writer, int senderId, String message) {
        String completeMessage = generateMessageString(senderId, Constants.TYPE_BROADCAST, message);
        writer.println(completeMessage);
    }

    public static String generateMessageString(int senderId, int receiverId, String message) {
        String completeMessage = senderId + "," + receiverId + "," + message;

        return completeMessage;
    }

    public static void sendMessageVC(PrintWriter writer, int senderId, int receiverId,
                                     String message, int vc[]) {
        String vectorClockStr = "";

        for (int i = 0; i<Constants.TOTAL_PROCESSES; i++) {
            vectorClockStr += vc[i];

            if (i < Constants.TOTAL_PROCESSES - 1) {
                vectorClockStr += ",";
            }
        }

        String completeMessage = generateMessageStringVC(senderId, receiverId, message, vectorClockStr);
        writer.println(completeMessage);
    }

    public static void broadcastMessageVC(PrintWriter writer, int senderId,
                                          String message, int vc[]) {
        String vectorClockStr = "";

        for (int i = 0; i<Constants.TOTAL_PROCESSES; i++) {
            vectorClockStr += vc[i];

            if (i < Constants.TOTAL_PROCESSES - 1) {
                vectorClockStr += ",";
            }
        }

        String completeMessage = generateMessageStringVC(senderId, Constants.TYPE_BROADCAST, message, vectorClockStr);
        writer.println(completeMessage);
    }

    public static String generateMessageStringVC(int senderId, int receiverId,
                                                 String message, String vectorClocksStr) {
        String completeMessage = senderId + "," + receiverId + "," + message + "," + vectorClocksStr;

        return completeMessage;
    }

    public static boolean isValidMessageVC(String completeMessage) {
        char comma = ',';
        int totalComma = 0;

        for(int i=0;i<completeMessage.length();i++) {
            if (completeMessage.charAt(i) == comma) {
                totalComma++;
            }
        }

        return (totalComma > 2);
    }

    public static Message readMessage(BufferedReader reader) throws IOException {
        String completeMessage = reader.readLine();
        Message message = parseAndGenerateMessageObject(completeMessage);

        return message;
    }

    public static Message parseAndGenerateMessageObject(String completeMessage) {
        char comma = ',';
        int commaIndex = completeMessage.indexOf(comma);
        int senderId = Integer.parseInt(completeMessage.substring(0,commaIndex));

        String restOfCompleteMessage = completeMessage.substring(commaIndex + 1);
        commaIndex = restOfCompleteMessage.indexOf(comma);
        int receiverId = Integer.parseInt(restOfCompleteMessage.substring(0,commaIndex));

        String message = restOfCompleteMessage.substring(commaIndex + 1);

        return new Message(senderId, receiverId, message);
    }

    public static MessageVC readMessageVC(BufferedReader reader) throws IOException {
        String completeMessage = reader.readLine();
        MessageVC messageVC = parseAndGenerateMessageVCObject(completeMessage);

        return messageVC;
    }

    public static MessageVC parseAndGenerateMessageVCObject(String completeMessage) {
        char comma = ',';
        int commaIndex = completeMessage.indexOf(comma);
        int senderId = Integer.parseInt(completeMessage.substring(0,commaIndex));

        String restOfCompleteMessage = completeMessage.substring(commaIndex + 1);
        commaIndex = restOfCompleteMessage.indexOf(comma);
        int receiverId = Integer.parseInt(restOfCompleteMessage.substring(0,commaIndex));

        restOfCompleteMessage = restOfCompleteMessage.substring(commaIndex + 1);
        commaIndex = restOfCompleteMessage.indexOf(comma);
        String message = restOfCompleteMessage.substring(0, commaIndex);

        restOfCompleteMessage = restOfCompleteMessage.substring(commaIndex + 1);
        String commaStr = ",";
        StringTokenizer stringTokenizer = new StringTokenizer(restOfCompleteMessage, commaStr);

        int vc[] = new int[Constants.TOTAL_PROCESSES];
        int i = 0;

        while(stringTokenizer.hasMoreTokens()) {
            vc[i++] = Integer.parseInt(stringTokenizer.nextToken());
        }

        return new MessageVC(senderId, receiverId, message, vc);
    }
}
