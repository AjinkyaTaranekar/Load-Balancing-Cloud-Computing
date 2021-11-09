/*
 * Title:        LoadBalancer
 * Description:  A simulation to identify different approaches in Load Balancing of Cloud Computing
 * Authors:      Ajinkya Dandvate
 *               Ajinkya Taranekar
 *               Chirayu Mehta
 *               Malay Saxena
 *               Murtaza Ali
 */

import java.util.*;


import Priority.PriorityDatacenterBroker;
import RoundRobin.RoundRobinDatacenterBroker;
import ShortestJobFirst.ShortestJobFirstDatacenterBroker;
import org.cloudbus.cloudsim.*;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class LoadBalancer {

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
            
            // Initialize the CloudSim library
            CloudSim.init(numUsers, calendar, false);

            Log.printLine();
            Log.printLine("Second step: Create Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation.");
            Log.printLine("Enter number of datacenters:");
            int numberOfDatacenters = scanner.nextInt();

            for (int i = 0; i < numberOfDatacenters; i++) {
                createDatacenter("Datacenter_" + i);
            }

            Log.printLine();
            Log.printLine("Third step: Create Broker");
            Log.printLine("Select method for LoadBalancing:" +
                    "\n1. Round Robin" +
                    "\n2. Shortest Job First" +
                    "\n3. First Come First Serve" +
                    "\n4. Genetic Algorithm"+
                    "\n5. Priority");

            DatacenterBroker broker = null;
            boolean gotBroker = false;

            while(!gotBroker) {
                int option = scanner.nextInt();
                try {
                    switch (option) {
                        case 1 :
                            broker = new RoundRobinDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 2 :
                            broker = new ShortestJobFirstDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 3 :
                            //broker = new FCFSDatacenterBroker("Broker");
                            Log.printLine("FCFS not implemented yet");
                            gotBroker = false;
                            break;
                        case 4 :
                            //broker = new GeneticAlgorithmDatacenterBroker("Broker");
                            Log.printLine("GA not implemented yet");
                            gotBroker = false;
                            break;
                        case 5 :
                            broker = new PriorityDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        default:
                            Log.printLine("Please, select from [1-] only:");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int brokerId = broker.getId();

            Log.printLine();
            Log.printLine("Fourth step: Create VMs and send them to broker");
            Log.printLine("Enter number of vms:");
            int numberOfVm = scanner.nextInt();

            List<Vm> vmList = createVM(brokerId, numberOfVm); 
            broker.submitVmList(vmList);

            Log.printLine();
            Log.printLine("Fifth step: Create Cloudlets and send them to broker");
            Log.printLine("Enter number of cloudlet");
            int numberOfCloudlet = scanner.nextInt();

            List<Cloudlet> cloudletList = createCloudlet(brokerId, numberOfCloudlet); 
            broker.submitCloudletList(cloudletList);

            Log.printLine();
            Log.printLine("Sixth step: Starts the simulation");
            Log.printLine("Press any key to continue...");
            scanner.next();

            CloudSim.startSimulation();

            Log.printLine();
            Log.printLine("Final step: Print results when simulation is over");
            Log.printLine("Press any key to continue...");
            scanner.next();

            List<Cloudlet> cloudletReceivedList = broker.getCloudletReceivedList();
            List<Vm> vmsCreatedList = broker.getVmsCreatedList();

            CloudSim.stopSimulation();

            printCloudletList(cloudletReceivedList);

            Log.printLine();
            Log.printLine("Simulation Complete");
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


        // 5. Create a DatacenterCharacteristics object that stores the
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


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {

        String indent = "    ";
        Log.printLine();
        Log.printLine();
        Log.printLine("========================================== OUTPUT ==========================================");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Datacenter ID" + indent + "VM ID" + indent + " " + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet value : list) {
            Log.print(indent + String.format("%02d", value.getCloudletId()) + indent + indent);

            if (value.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + String.format("%02d", value.getResourceId()) +
                        indent + indent + indent + String.format("%02d", value.getVmId()) +
                        indent + indent + String.format("%.2f", value.getActualCPUTime()) +
                        indent + indent + String.format("%.2f", value.getExecStartTime()) +
                        indent + indent + indent + String.format("%.2f", value.getFinishTime()));
            }
        }

    }

}