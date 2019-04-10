public class Actions {
    private String action;
    private int taskNumber;
    private int delay;
    private int resourceNumber;
    private int resourceAmount;

    public Actions(String a, int b, int c, int d, int e){
        this.action = a;
        this.taskNumber = b;
        this.delay = c;
        this.resourceNumber = d;
        this.resourceAmount = e;
    }

    public String getAction(){
        return this.action;
    }

    public int getTaskNumber(){
        return this.taskNumber;
    }

    public int getResourceNumber(){
        return this.resourceNumber;
    }

    public int getResourceAmount() {
        return resourceAmount;
    }

    @Override
    public String toString() {
        return action + " " + taskNumber + " " + delay + " " + resourceNumber
                + " " + resourceAmount;
    }
}
