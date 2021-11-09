import FirstComeFirstServe.FCFSDatacenterBroker;
import GeneticAlgorithm.GeneticAlgorithmDatacenterBroker;
import Priority.PriorityDatacenterBroker;
import RoundRobin.RoundRobinDatacenterBroker;
import ShortestJobFirst.ShortestJobFirstDatacenterBroker;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.*;

public class LoadBalancerComparison {

    private static List<Map<String, String>> results = new ArrayList<>();
    
    private static List<Vm> createVM(int userId, int numberOfVm) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        for(int i = 0; i< numberOfVm; i++){
            Vm vm = new Vm(i, userId, mips+(i*10), pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int numberOfCloudlet){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<>();

        //cloudlet parameters
        long length = 1000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for(int i = 0; i < numberOfCloudlet; i++){
            Cloudlet cloudlet = new Cloudlet(i, (length + 2L * i * 10), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            list.add(cloudlet);
        }

        return list;
    }

    public static void main(String[] args) {
        Log.printLine();
        Log.printLine("===================================== Load Balancer ==================================");
        Log.printLine("Title:        LoadBalancer" +
                "\nDescription:  A simulation to identify different approaches in Load Balancing of Cloud Computing" +
                "\nAuthors:      Ajinkya Dandvate" +
                "\n              Ajinkya Taranekar" +
                "\n              Chirayu Mehta" +
                "\n              Malay Saxena" +
                "\n              Murtaza Ali");
        try {
            Calendar calendar = Calendar.getInstance();

            Scanner scanner = new Scanner(System.in);

            Log.printLine();
            Log.printLine("First step: Initialize the CloudSim package.");
            Log.printLine("Enter number of grid users:");
            int numUsers = scanner.nextInt();

            Log.printLine();
            Log.printLine("Second step: Create Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation.");
            Log.printLine("Enter number of datacenters:");
            int numberOfDatacenters = scanner.nextInt();

            Log.printLine();
            Log.printLine("Third step: Create Broker");

            Log.printLine("Enter number of vms:");
            int numberOfVm = scanner.nextInt();

            Log.printLine("Enter number of cloudlet");
            int numberOfCloudlet = scanner.nextInt();

            for (int i = 1; i <= 5 ; i++) {
                CloudSim.init(numUsers, calendar, true);

                DatacenterBroker broker = null;
                try {
                    switch (i) {
                        case 1:
                            broker = new RoundRobinDatacenterBroker("RoundRobinDatacenterBroker");
                            break;
                        case 2:
                            broker = new ShortestJobFirstDatacenterBroker("ShortestJobFirstDatacenterBroker");
                            break;
                        case 3:
                            broker = new PriorityDatacenterBroker("PriorityDatacenterBroker");
                            break;
                        case 4:
                            broker = new FCFSDatacenterBroker("FirstComeFirstServeDatacenterBroker");
                            break;
                        case 5:
                            broker = new GeneticAlgorithmDatacenterBroker("GeneticAlgorithmDatacenterBroker");
                            break;
                        default:
                            Log.printLine("Please, select from [1-5] only:");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (int j = 0; j < numberOfDatacenters; j++) {
                    createDatacenter("Datacenter_" + j);
                }

                assert broker != null;
                int brokerId = broker.getId();
                String brokerName = broker.getName();

                Log.printLine("Broker: " + brokerName);
                Log.printLine("Create VMs");

                List<Vm> vmList = createVM(brokerId, numberOfVm);

                Log.printLine();
                Log.printLine("Create Cloudlets");

                List<Cloudlet> cloudletList = createCloudlet(brokerId, numberOfCloudlet);

                Log.printLine("Sending them to broker...");

                broker.submitVmList(vmList);
                broker.submitCloudletList(cloudletList);

                Log.printLine();
                Log.printLine("Starts the simulation");

                CloudSim.startSimulation();

                Log.printLine();
                Log.printLine("Results when simulation is over");

                List<Cloudlet> cloudletReceivedList = broker.getCloudletReceivedList();
                List<Vm> vmsCreatedList = broker.getVmsCreatedList();

                CloudSim.stopSimulation();

                printResult(cloudletReceivedList, brokerName);

                Log.printLine();
                Log.printLine("Simulation Complete");
            }
            String indent = "    ";
            Log.printLine("Broker" + indent + indent + indent + indent +  "Total CPU Time" + indent + "Average Time");
            for (Map<String, String> result: results){
                Log.printLine(result.get("broker") + indent + result.get("total_cpu_time") + indent + result.get("average_cpu_time"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name){

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        List<Pe> peList1 = new ArrayList<>();

        int mips = 10000;

        // 3. Create PEs and add these into the list.
        //for a quad-core machine, a list of 4 PEs is required:
        peList1.add(new Pe(0, new PeProvisionerSimple(mips + 500))); // need to store Pe id and MIPS Rating
        peList1.add(new Pe(1, new PeProvisionerSimple(mips + 1000)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips + 1500)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips + 700)));

        //Another list, for a dual-core machine
        List<Pe> peList2 = new ArrayList<>();

        peList2.add(new Pe(0, new PeProvisionerSimple(mips + 700)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips + 900)));

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 1002048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1)
                )
        ); // This is our first machine

        hostId++;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        ); // Second machine


        //To create a host with a space-shared allocation policy for PEs to VMs:
        //hostList.add(
        //		new Host(
        //			hostId,
        //			new CpuProvisionerSimple(peList1),
        //			new RamProvisionerSimple(ram),
        //			new BwProvisionerSimple(bw),
        //			storage,
        //			new VmSchedulerSpaceShared(peList1)
        //		)
        //	);

        //To create a host with an opportunistic space-shared allocation policy for PEs to VMs:
        //hostList.add(
        //		new Host(
        //			hostId,
        //			new CpuProvisionerSimple(peList1),
        //			new RamProvisionerSimple(ram),
        //			new BwProvisionerSimple(bw),
        //			storage,
        //			new VmSchedulerOpportunisticSpaceShared(peList1)
        //		)
        //	);


        // 5. Create a DatacenterCharacteristics String that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter String.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints the Cloudlet Strings
     * @param list  list of Cloudlets
     */
    private static void printResult(List<Cloudlet> list, String broker) {

        Log.printLine();
        Log.printLine();
        Log.printLine("========================================== OUTPUT ==========================================");
        Log.printLine("Broker: " + broker);
        
        double time = 0;

        for (Cloudlet value : list) {
            time += value.getActualCPUTime();
        }

        double avgTime = time/list.toArray().length;
        Log.printLine("Total CPU Time: " + time);
        Log.printLine("Average CPU Time: " + avgTime);

        Map<String, String> result = new HashMap<>();
        result.put("broker", broker);
        result.put("total_cpu_time", String.format("%.2f", time));
        result.put("average_cpu_time", String.format("%.2f", avgTime));

        results.add(result);
    }

}