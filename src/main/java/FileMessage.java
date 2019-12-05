import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;


public class FileMessage extends Command implements Serializable {

    private String fileName;
    private User sourceUser;
    private User targetUser;
    private String sourceFilePath;
    private String targetFilePath;
    private byte[] file;

    public FileMessage(String[] str, Command.From from, String sourceUser) {
        super(Command.Type.User_File, from);
        try {
            this.targetUser = new User(str[0]);
            this.sourceUser = new User(sourceUser);
            this.sourceFilePath = str[1];
            setFileName(sourceFilePath);
            getFile(sourceFilePath);
            this.targetFilePath = "Client/src/downloads/".concat(this.fileName);
        } catch (Exception e) {
            this.setType(Type.Error);
            this.setFrom(From.IO);
            this.setResult(false, "Invalid file path");
        }
    }


    public User getTargetUser() {
        return this.targetUser;
    }

    public User getSourceUser() {
        return this.sourceUser;
    }

    public byte[] getFile() {
        return this.file;
    }

    public String getTargetFilePath() {
        return this.targetFilePath;
    }

    private void setFileName(String path) {
        try {
            String[] splitPath = path.split("/");
            String fullName = splitPath[splitPath.length - 1];
            String[] splitName = fullName.split("\\.");
            String fName = String.join(".", Arrays.copyOfRange(splitName, 0, splitName.length - 2));
            String fType = splitName[splitName.length - 1];
            File tmpFile = new File("Client/src/downloads/" + String.join(".", fullName));
            int i = 1;
            while (tmpFile.exists()) {
                fullName = fName + "(" + i++ + ")." + fType;
                tmpFile = new File("Client/src/downloads/" + fullName);
            }
            this.fileName = fullName;
        } catch (Exception e) {
            this.setType(Type.Error);
            this.setFrom(From.IO);
            this.setResult(false, "Invalid file path");
        }
    }

    private void getFile(String path) {
        try {
            this.file = Files.readAllBytes(new File(path).toPath());
            setResult(true, "File received: " + targetFilePath);

        } catch (Exception e) {
            setResult(false, sourceFilePath + "does not exist!");
        }
    }

}
