import java.util.Date;

/**
 * Created by prakhash on 22/03/16.
 */
public class CommitDetails {
    Date commitedDate;
    String branch;
    String message;

    public CommitDetails() {
    }

    public CommitDetails(Date commitedDate, String branch, String message) {
        this.commitedDate = commitedDate;
        this.branch = branch;
        this.message = message;
    }

    public Date getCommitedDate() {
        return commitedDate;
    }

    public void setCommitedDate(Date commitedDate) {
        this.commitedDate = commitedDate;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
