import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;


public class FileMessage extends Command implements Serializable {

    private String fileName;
    private User sourceUser;
    private String targetUserName;
    private String sourceFilePath;
    private User targetUser;
    private byte[] file;

    public FileMessage(String[] data, Command.From from) {
        super(Command.Type.UserFileMessage, from);
        this.targetUserName = data[0];
        this.sourceFilePath = data[1];
        this.targetUser = new User(targetUserName);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        this.fileName = LocalDateTime.now().toString();
        try {
            this.file = Files.readAllBytes(Paths.get(sourceFilePath));
        } catch (IOException e) {
            e.printStackTrace();
            this.setType(Type.Error);
        }
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public byte[] getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "sourceUser=" + sourceUser +
                ", targetUserName='" + targetUserName + '\'' +
                ", sourceFilePath='" + sourceFilePath + '\'' +
                ", targetUser=" + targetUser +
                ", file=" + Arrays.toString(file) +
                ", type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}
