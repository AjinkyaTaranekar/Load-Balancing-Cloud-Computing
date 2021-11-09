package Priority;

import org.cloudbus.cloudsim.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

class Task {
    int cloudletIndex;
    int priorityLevel;			// Assigned a value from 1 to 10 (not enforced yet)
    float priority;				// The priority value calculated using priorityLevel and the taskTime
    int taskStartTime;			// When the task started (in ms)
    int taskTime;				// How long the task has been waiting

    // Initialises a task object, sets passed values to object properties
    Task(int index, int p_level){
        this.cloudletIndex = index;			// sets the cloudlet index
        this.priorityLevel = p_level;		// sets the priority level

        this.taskStartTime = (int) System.currentTimeMillis();	// sets the current time (initial task time)
        this.priority = this.priorityLevel * 100;				// doesn't use taskTime as task has just been initialised
    }

    // Finds the time the task has been waiting and recalculates its priority value
    public void refreshTask() {
        // retrieves time waiting
        this.taskTime = (int) (System.currentTimeMillis() - this.taskStartTime);
        // Equation below defines how priority escalates with time
        this.priority = this.priorityLevel * 100 + (this.taskTime*this.priorityLevel/1000);
    }

    // Used to nicely format output header
    public void printHeader() {
        System.out.printf("%-17s%-17s%-17s%-17s\n",
                "Cloudlet Index", "Priority Level", "Priority Value", "Time Elapsed");
    }

    // Nicely formats task output
    public void printTask() {
        System.out.printf("%-17d%-17d%-17.2f% -17d\n",
                this.cloudletIndex, this.priorityLevel, this.priority, this.taskTime);
    }
}

public class PriorityDatacenterBroker extends DatacenterBroker {

    public PriorityDatacenterBroker(String name) throws Exception {
        super(name);
    }


    public void runPriority() {
        try {
            Scanner scanner = new Scanner(System.in);
            List<Cloudlet> submissionList = new ArrayList<Cloudlet>();
            List<Task> priorityList = new ArrayList<Task>();

            int[] priorityTestVals = new int[getCloudletList().size()];
            Log.printLine("Assigning random priorities (1-10) to tasks");

            Random random = new Random();
            for (int i = 0; i < getCloudletList().size(); i++) {
                priorityTestVals[i] = random.nextInt(10);
            }

            for (int id = 0; id < getCloudletList().size(); id++) {
                priorityList.add(new Task(id, priorityTestVals[id]));
            }

            for (int i = 0; i < getCloudletList().size(); i++) {
                TimeUnit.SECONDS.sleep(2);
                priorityList.get(i).refreshTask();
            }

            Collections.sort(priorityList, (Task t1, Task t2) -> Float.compare(t2.priority, t1.priority));
            priorityList.forEach((t) -> submissionList.add(cloudletList.get(t.cloudletIndex)));


            getCloudletList().addAll(submissionList);
        }catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    @Override
    public void submitCloudletList(List<? extends Cloudlet> list) {
        getCloudletList().addAll(list);
        runPriority();
    }
}