
/**
 * Created by prakhash on 22/03/16.
 */

class Messages {
    
    public static final String DANGEROURS =
    "Warning: The command you entered may alter the files "
    + "in your working directory. Uncommitted changes may be lost. "
    + "Are you sure you want to continue? (yes/no)";

    public static final String GITLET_EXIST =
    "A gitlet version control system already exists in the current directory.";

    public static final String NO_FILE =
    "File does not exist";

    public static final String FILE_UNCHANGED =
    "File has not been modified since the last commit.";

    public static final String NOTHING_TO_COMMIT =
    "No changes added to the commit.";

    public static final String NO_COMMIT_MSG =
    "Please enter a commit message.";

    public static final String CANNOT_REMOVE_FILE =
    "No reason to remove the file.";

    public static final String CANNOT_FIND_COMMIT =
    "Found no commit with that message.";

    public static final String CANNOT_CHECKOUT =
    "File does not exist in the most recent commit, or no such branch exists.";

    public static final String ALREADY_ON_BRANCH =
    "No need to checkout the current branch.";

    public static final String COMMIT_MISSING =
    "No commit with that id exists.";

    public static final String COMMIT_NO_FILE =
    "File does not exist in that commit.";

    public static final String BRANCH_EXIST =
    "A branch with that name already exists.";

    public static final String CANNOT_FIND_BRANCH =
    "A branch with that name does not exist.";

    public static final String CANNOT_REMOVE_BRANCH =
    "Cannot remove the current branch.";

    public static final String INVALID_COMMAND =
    "Invalid command.";

    public static final String ARGUMENT_MISSING =
    "Argument missing. Format: java Gitlet [command...]";

    public static final String WRONG_ARGUMENT_LENGTH =
    "Invalid length of argument. Please try again.";

    public static final String CANNOT_MERGE_SELF =
    "Cannot merge a branch with itself.";

    public static final String CANNOT_REBASE_SELF =
    "Cannot rebase a branch onto itself.";

    public static final String UP_TO_DATE =
    "Already up-to-date";

    public static final String REPLAYING =
    "Currently replaying:";

    public static final String REMOTE_EXIST =
    "A remote with that name already exists.";

    public static final String CANNOT_FIND_REMOTE =
    "A remote with that name does not exist.";

    public static final String PULL_BEFORE_PUSH =
    "Please pull down remote changes before pushing.";

    public static final String REMOTE_MISSING_BRANCH =
    "That remote does not have that branch.";
}
