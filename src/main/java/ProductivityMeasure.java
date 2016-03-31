/**
 * Created by prakhash on 22/03/16.
 */
public class ProductivityMeasure {
    String Branchname;
    int numberOfCommits=0;
    double Productivity;
    int score=0;

    public String getBranchname() {
        return Branchname;
    }

    public void setBranchname(String branchname) {
        Branchname = branchname;
    }

    public int getNumberOfCommits() {
        return numberOfCommits;
    }

    public void setNumberOfCommits(int numberOfCommits) {
        this.numberOfCommits = numberOfCommits;
    }

    public double getProductivity() {
        return Productivity;
    }

    public void setProductivity(double productivity) {
        Productivity = productivity;
    }

    public void increaseCommits(){
        numberOfCommits+=1;
        score+=100;
    }

    public void commitDeduction(){
        score-=25;
    }

    public void commitIncreament(){
        score+=25;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
