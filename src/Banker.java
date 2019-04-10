// created by Yuning Zhou on 04/08/2019
// Banker's Algorithm

import java.io.*;
import java.util.*;


public class Banker {

    public static void main(String[] Args) {

        // reads in a file from commandline
        int numbTask = 0;
        int numbResource = 0;
        HashMap<Integer, Resources> resourcesList = new HashMap<>();
        ArrayList<Actions> actionList = new ArrayList<>();
        HashMap<Integer, Task> taskList = new HashMap<>();

        try {
            File file = new File(Args[0]);
            Scanner sc = new Scanner(file);
            numbTask = sc.nextInt();
            numbResource = sc.nextInt();

            // creating tasks
            for (int i = 0; i < numbTask; i++) {
                taskList.put(i + 1, new Task(i + 1));
            }

            // creating resources
            for (int i = 0; i < numbResource; i++) {
                resourcesList.put(i + 1, new Resources(sc.nextInt(), i + 1));
            }

            // reading actions
            while (sc.hasNext()) {
                actionList.add(new Actions(sc.next(),
                        sc.nextInt(), sc.nextInt(), sc.nextInt(), sc.nextInt()));
            }

            // at this point, we have populated lists of tasks, resources, and actions.

        } catch (Exception ex) {
            System.out.println("File not found. " +
                    "Please input another file name!");
        }


        optimistic(resourcesList, actionList, taskList);


    }

    // Optimistic (FIFO) Algorithm
    public static void optimistic(HashMap<Integer, Resources> resourcesList,
                                  ArrayList<Actions> actionList, HashMap<Integer, Task> taskList) {

        // initiate tasks with 0 claimed resources since this algorithm does not care about the claims
        int ini = 0;
        for (int i = 0; i < actionList.size(); i++) {
            if (actionList.get(i).getAction().equals("initiate")) {
                ini++;
                int taskKey = actionList.get(i).getTaskNumber();
                int resourceType = actionList.get(i).getResourceNumber();
                Task task = taskList.get(taskKey);

                HashMap<Integer, Integer> map = task.getResourceUsage();
                if (map == null) {
                    map = new HashMap<>();
                }
                map.put(resourceType, 0);
                task.initiate(map);
            } else {
                // reads and stores actions from the action list
                Actions a = actionList.get(i);
                int taskKey = actionList.get(i).getTaskNumber();
                Task task = taskList.get(taskKey);
                task.pushAction(a);
            }
        }

        // at this point, the actionList is obsolete; the taskList has all of the information.
        // all of the preparation work is done.

        int cycle = ini / taskList.size();
        Queue<Task> finished = new LinkedList<>();
        LinkedList<Task> blocked = new LinkedList<>();
        Queue<Task> ready = new LinkedList<>();
        Queue<Task> aborted = new LinkedList<>();
        ArrayList<int[]> buffer = new ArrayList<>(); // Index of Resources, Amount
        ArrayList<Task> garage = new ArrayList<>(); // temporary storage to modify the blocked q


        // populate the ready list
        for (int i = 0; i < taskList.size(); i++) {
            ready.add(taskList.get(i + 1));
        }

        // main cycle
        while (finished.size() + aborted.size() != taskList.size() && cycle<40) {

            // unblock first
            if (!blocked.isEmpty()) {

                int size = blocked.size();

                for (int i = 0; i < size; i++) {

                    // check if some requests can be fulfilled
                    LinkedList<Task> temp3 = blocked;
                    ListIterator<Task> itr2 = temp3.listIterator();
                    while (itr2.hasNext()) {
                        Task d = itr2.next();

                        Actions action = d.getProcess().peek();
                        if (resourcesList.get(action.getResourceNumber()).getAvailableUnits()
                                - action.getResourceAmount() >= 0) {
                            d.state = 1;
                            d.getProcess().pop();
                            // it will be granted
                            resourcesList.get(action.getResourceNumber()).use(action.getResourceAmount());
                            d.request(action.getResourceNumber(), action.getResourceNumber());
                            ready.add(d);
                            garage.add(d);
                        } else {
                            d.state = 1;
                        }

                    }

                    blocked.removeAll(garage);
                    garage.clear();

                }
            }


            // for the tasks not in the blocked state or was not involved in the previous step
            int k = ready.size();
            for (int i = 0; i < k; i++) {

                Task current = ready.remove();
                Actions action = current.getProcess().peek();
                System.out.println(current);
                if (current.state == 0 && current.delay == 0) {
                    if (action.getAction().equals("request")) {
                        if (resourcesList.get(action.getResourceNumber()).getAvailableUnits()
                                - action.getResourceAmount() >= 0) {
                            // it will be granted
                            resourcesList.get(action.getResourceNumber()).use(action.getResourceAmount());
                            current.request(action.getResourceNumber(), action.getResourceNumber());
                            current.delay += action.getDelay();
                            // put it back to the q
                            ready.add(current);
                            current.getProcess().pop(); // move on to the next action

                        } else {
                            // cannot grant the request
                            blocked.add(current);
                        }

                    } else if (action.getAction().equals("release")) {
                        // release the resource onto buffer
                        int[] temp = new int[2];
                        temp[0] = action.getResourceNumber();
                        temp[1] = action.getResourceAmount();
                        buffer.add(temp);
                        current.request(action.getResourceNumber(), -action.getResourceNumber());
                        current.getProcess().pop(); // move on to the next task
                        current.delay += action.getDelay();

                        // peek at the next action
                        action = current.getProcess().peek();
                        if (action.getAction().equals("terminate") && current.delay == 0) {
                            // process has finished
                            current.finishTime = cycle + 1;
                            finished.add(current);
                        } else {
                            ready.add(current);
                        }
                    } else {
                        // it has terminated
                        // record the time taken for the task
                        // record its waiting time

                            current.finishTime = cycle + 1;
                            finished.add(current);
                    }


                } else if (current.delay == 1 && current.getProcess().peek().getAction().equals("terminate")
                        && current.getProcess().peek().getDelay() == 0 && current.finalDelay == 0) {
                    // terminate
                    current.finishTime = cycle + 1;
                    finished.add(current);
                }else if (current.getProcess().peek().getAction().equals("terminate")
                        && current.getProcess().peek().getDelay() == 0 && current.finalDelay > 0) {
                    // continue delaying
                    current.delay -= 1;
                    ready.add(current);
                } else if (current.delay == 1 && current.getProcess().peek().getAction().equals("terminate")
                        && current.finalDelay == 0) {
                    // start delaying for the last time
                    current.finalDelay++;
                    current.delay --;
                    current.delay += current.getProcess().peek().getDelay();
                    ready.add(current);
                } else if (current.delay == 1 && current.getProcess().peek().getAction().equals("terminate") &&
                        current.finalDelay >0){
                    // terminate
                    current.finishTime = cycle + 1;
                    finished.add(current);
                } else if (current.delay == 0){
                    ready.add(current);

                } else {
                    current.delay --;
                    ready.add(current);
                }

                current.state = 0;
            }

            Boolean deadlock = deadlock(blocked, ready, resourcesList, buffer);
            while (deadlock) {
                // find the lowest numbered task
                int min = Integer.MAX_VALUE;

                ListIterator<Task> itr1 = blocked.listIterator();
                while (itr1.hasNext()) {
                    Task d = itr1.next();
                    if (d.getTaskNo() < min) {
                        min = d.getTaskNo();
                    }
                }

                Task abort = taskList.get(min);
                abort.aborted = true;
                aborted.add(abort);
                blocked.remove(abort);


                // release all of its resources onto buffer
                Iterator it = abort.getResourceUsage().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    int[] temp = new int[2];
                    temp[0] = (int) pair.getKey();
                    temp[1] = Integer.parseInt(pair.getValue().toString());
                    buffer.add(temp);
                    it.remove();
                }

                deadlock = deadlock(blocked, ready, resourcesList, buffer);
            }

            // check if there's anything in buffer that needs to be released
            if (!buffer.isEmpty()) {
                for (int i = 0; i < buffer.size(); i++) {
                    resourcesList.get(buffer.get(i)[0]).reclaim(buffer.get(i)[1]);

                }
                buffer.clear();
            }
            System.out.println(cycle);
            System.out.println("ready: " + ready);
            System.out.println("blocked: " + blocked);
            System.out.println("finished: " + finished);
            cycle++;
            update(blocked);
        }

        // all tasks have terminated
        // printing output
        int totalFinishTime = 0;
        int totalWaitingTime = 0;

        System.out.println("\t\t" + "FIFO");
        for (int i = 0; i < taskList.size(); i++){
            System.out.print("Task " + (i + 1) + ": \t");
            if (taskList.get(i + 1).aborted) {
                System.out.println("aborted");
            } else {
                System.out.print(taskList.get(i + 1).finishTime + "\t");
                totalFinishTime += taskList.get(i + 1).finishTime;
                System.out.print(taskList.get(i + 1).waitingTime + "\t");
                totalWaitingTime += taskList.get(i + 1).waitingTime;
                System.out.println((int)((double)taskList.get(i + 1).waitingTime / taskList.get(i + 1).finishTime * 100 )+ "%");
            }
        }

        System.out.print("total: " + "\t\t");
        System.out.print(totalFinishTime + "\t");
        System.out.print(totalWaitingTime + "\t");
        System.out.println((int)((double)totalWaitingTime/totalFinishTime * 100) + "%");

    }

    // function to test for deadlock
    public static Boolean deadlock(LinkedList<Task> a, Queue<Task> ready, HashMap<Integer, Resources> resourcesList,
                                   ArrayList<int[]> buffer) {
        if (a.isEmpty()) {
            return false;
        }
        if (!buffer.isEmpty()) {
            HashMap<Integer, Resources> temp = clone(resourcesList);

            for (Task t : a) {
                for (int i = 0; i < buffer.size(); i++) {
                    temp.get(buffer.get(i)[0]).reclaim(buffer.get(i)[1]);
                    if (temp.get(t.getProcess().peek().getResourceNumber()).getAvailableUnits()
                            >= t.getProcess().peek().getResourceAmount()) {
                        return false;
                    }
                }
                temp = clone(resourcesList);
            }
        }

        ListIterator<Task> itr = a.listIterator();
        while (itr.hasNext()) {
            Task c = itr.next();
            if (resourcesList.get(c.getProcess().peek().getResourceNumber()).getAvailableUnits()
                    >= c.getProcess().peek().getResourceAmount()) {
                return false;
            }
            if (!ready.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // function to deep clone a HashMap
    public static HashMap<Integer, Resources> clone(HashMap<Integer, Resources> a) {
        HashMap<Integer, Resources> copy = new HashMap<>();

        for (HashMap.Entry<Integer, Resources> entry : a.entrySet()) {
            Resources x = new Resources(entry.getValue().getAvailableUnits(), entry.getValue().getIndex());
            copy.put(entry.getKey(), x);
        }

        return copy;
    }

    // function to update status count
    public static void update(LinkedList<Task> a) {
        for (Task x : a){
            x.waitingTime++;
        }
    }

}
