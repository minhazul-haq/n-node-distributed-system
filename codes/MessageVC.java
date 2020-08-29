public class MessageVC {
    private int senderId;
    private int receiverId;
    private String message;
    private int vectorClocks[];

    public MessageVC(int senderId, int receiverId, String message, int vectorClocks[]) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.vectorClocks = vectorClocks;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int[] getVectorClocks() {
        return vectorClocks;
    }

    public void setVectorClocks(int[] vectorClocks) {
        this.vectorClocks = vectorClocks;
    }
}
