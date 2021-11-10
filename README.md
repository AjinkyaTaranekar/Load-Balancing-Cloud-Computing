# Cloud Computing Project
# Load-Balancing-Cloud-Computing

Following Algorithms have been implemented :

* FCFS considers arrival time of tasks which means task which arrives first will be executed first. It is non-preemptive in nature. Although it is not a complex algorithm but in some cases it leads to inefficiency. For instance if a longer task arrives first then the shorter task which arrives later will have to wait for its execution, increasing the waiting time of shorter tasks.

* It dispatches the tasks in FIFO manner but it assigns the resources to each task for fixed time quantum and if the task is not completed before the time slot allocated then resources are preempted until its next turn for execution and allocated to the next waiting task. It is easy to implement, distributes resources evenly and independent of starvation but it is critical to decide the time quantum

* Priority Scheduling is a method of scheduling processes that is based on priority. In this algorithm, the scheduler selects the tasks to work as per the priority.
The processes with higher priority should be carried out first, whereas jobs with equal priorities are carried out on a round-robin or FCFS basis. Priority depends upon memory requirements, time requirements, etc.

* Shortest Job First is a dynamic  load balancing  algorithm  which handles the  process  with priority basis.  It determines the priority by checking the size of the process. This algorithm distributes the load randomly by first checking the size of the process and then transferring the load to a Virtual Machine, which is  lightly loaded.  In that case that process size is lowest, this process will get first priority to execute whether we suppose lowest sized process executes in minimum time.  The load  balancer spreads the load on to different nodes  known as spread spectrum technique.

* It is a evolutionary technique based on the concept of "survival of the fittest". It is divided into four main steps: population generation, chromosome evaluation, crossover and mutation, evaluation of modified chromosome. The above steps make one iteration of the algorithm and number of iterations are fixed according to the problem. These steps are iteratively performed until a stopping criteria is met.

