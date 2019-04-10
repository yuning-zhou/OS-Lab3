import java.util.HashMap;
import java.util.LinkedList;

public class Task {
    private int taskNo;
    private HashMap<Integer,Integer> resourceUsage; // Resources, amount
    private LinkedList<Actions> process; // stores all of the actions of a task
    public int state;
    public int finishTime;
    public int waitingTime;
    public Boolean aborted;
    public int delay;
    public int finalDelay;

    public Task(int i){
        this.taskNo = i;
        this.process = new LinkedList<>();
        this.state = 0;
        this.finishTime = 0;
        this.waitingTime = 0;
        this.aborted = false;
        this.delay = 0;
        this.finalDelay = 0;
    }

    public void initiate(HashMap<Integer, Integer> j){
        this.resourceUsage = j;
    }

    // update resources as the task requests and releases them
    public void request(int i, int j){
        this.resourceUsage.put(i, this.resourceUsage.get(i) + j);
    }

    // adds an action to the task
    public void pushAction(Actions a){
        this.process.add(a);
    }

    public HashMap<Integer, Integer> getResourceUsage() {
        return this.resourceUsage;
    }

    public LinkedList<Actions> getProcess() {
        return process;
    }

    public int getTaskNo() {
        return taskNo;
    }

    @Override
    public String toString() {
        return taskNo + " " + delay + " ff: " + finalDelay;
    }
}
