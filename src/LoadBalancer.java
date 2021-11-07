/*
 * Title:        LoadBalancer
 * Description:  A simulation to identify different approaches in Load Balancing of Cloud Computing
 * Authors:      Ajinkya Dandvate
 *               Ajinkya Taranekar
 *               Chirayu Mehta
 *               Malay Saxena
 *               Murtaza Ali
 */

import java.text.DecimalFormat;
import java.util.*;


import RoundRobin.RoundRobinDatacenterBroker;
import org.cloudbus.cloudsim.*;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class LoadBalancer {

    private static List<Vm> createVM(int userId) {

        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[15];

        for(int i = 0; i< 15; i++){
            vm[i] = new Vm(i, userId, mips+(i*10), pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            //for creating a VM with a space shared scheduling policy for cloudlets:
            //vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

            list.add(vm[i]);
        }

        return list;
    }


    private static List<Cloudlet> createCloudlet(int userId){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<>();

        //cloudlet parameters
        long length = 1000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[40];

        for(int i = 0; i< 40; i++){
            //	Random r=new Random();
            cloudlet[i] = new Cloudlet(i, (length + 2*i*10), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }


    ////////////////////////// STATIC METHODS ///////////////////////

    public static void main(String[] args) {
        Log.printLine("Load Balancer");

        try {
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();

            // First step: Initialize the CloudSim package.
            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, false);

            // Second step: Create Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            @SuppressWarnings("unused")
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            Log.printLine("Select method for LoadBalancing:");
            Log.printLine("1. Round Robin");
            Log.printLine("2. Shortest Job First");
            Log.printLine("3. First Come First Serve");
            Log.printLine("4. Genetic Algorithm");

            //Third step: Create Broker
            DatacenterBroker broker = null;
            boolean gotBroker = false;

            Scanner scanner = new Scanner(System.in);

            while(!gotBroker) {
                int option = scanner.nextInt();
                try {
                    switch (option) {
                        case 1 -> {
                            broker = new RoundRobinDatacenterBroker("Broker");
                            gotBroker = true;
                        }
                        case 2 -> {
                            //broker = new ShortestJobFirstDatacenterBroker("Broker");
                            Log.printLine("SJF not implemented yet");
                            gotBroker = false;
                        }
                        case 3 -> {
                            //broker = new FCFSDatacenterBroker("Broker");
                            Log.printLine("FCFS not implemented yet");
                            gotBroker = false;
                        }
                        case 4 -> {
                            //broker = new GeneticAlgorithmDatacenterBroker("Broker");
                            Log.printLine("GA not implemented yet");
                            gotBroker = false;
                        }
                        default -> Log.printLine("Please, select from [1-] only:");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            assert broker != null;
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudl1ets and send them to broker
            List<Vm> vmList = createVM(brokerId); //creating 15 vms
            List<Cloudlet> cloudletList = createCloudlet(brokerId); // creating 40 cloudlets

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            List<Vm> newList1 = broker.getVmsCreatedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

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
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");

        for (Cloudlet value : list) {
            Log.print(indent + value.getCloudletId() + indent + indent);

            if (value.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + value.getResourceId() +
                        indent + indent + indent + value.getVmId() +
                        indent + indent + indent + dft.format(value.getActualCPUTime()) +
                        indent + indent + dft.format(value.getExecStartTime()) +
                        indent + indent + indent + dft.format(value.getFinishTime()));
            }
        }


    }


}