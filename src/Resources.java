public class Resources {
    private int maxUnits;
    private int index;
    private int availableUnits;

    public Resources(int i, int j){
        maxUnits = i;
        index = j;
        availableUnits = maxUnits;
    }


    // grant units to tasks
    public void use(int i){
        this.availableUnits -= i;
    }

    // gets units back when a task releases
    public void reclaim(int i){
        this.availableUnits += i;
    }

    public int getAvailableUnits(){
        return this.availableUnits;
    }

    public int getIndex(){
        return this.index;
    }

    @Override
    public String toString() {
        return index + " " + availableUnits;
    }

}
