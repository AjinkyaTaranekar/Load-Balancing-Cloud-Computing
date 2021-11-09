package GeneticAlgorithm;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithmDatacenterBroker extends DatacenterBroker {

    public GeneticAlgorithmDatacenterBroker(String name) throws Exception {
        super(name);
    }


    public void runGeneticAlgorithm() {

        List<Cloudlet> sortedList = new ArrayList<>(getCloudletList());

        int numCloudlets = sortedList.size();

        for(int i=0;i<numCloudlets;i++){
            Cloudlet tmp = sortedList.get(i);
            int idx = i;
            for(int j=i+1;j<numCloudlets;j++)
            {
                if(sortedList.get(j).getCloudletLength() < tmp.getCloudletLength())
                {
                    idx = j;
                    tmp = sortedList.get(j);
                }
            }
            Cloudlet tmp2 = sortedList.get(i);
            sortedList.set(i, tmp);
            sortedList.set(idx,tmp2);
        }

        ArrayList<Vm> toBeUsedVm = new ArrayList<Vm>();
        ArrayList<Vm> leftOutVm = new ArrayList<Vm>();

        ArrayList<Vm> sortedListVm = new ArrayList<Vm>(getVmList());

        int numVms=sortedListVm.size();

        for(int i=0;i<numVms;i++){
            Vm tmp=sortedListVm.get(i);
            int idx=i;
            if(i<numCloudlets)
                toBeUsedVm.add(tmp);
            else
                leftOutVm.add(tmp);
            for(int j=i+1;j<numVms;j++)
            {
                if(sortedListVm.get(j).getMips()>tmp.getMips())
                {
                    idx=j;
                    tmp=sortedListVm.get(j);
                }
            }
            Vm tmp2 = sortedListVm.get(i);
            sortedListVm.set(i, tmp);
            sortedListVm.set(idx,tmp2);
        }

        ArrayList<Chromosomes> initialPopulation = new ArrayList<Chromosomes>();
        for(int j=0;j<numCloudlets;j++) {
            ArrayList<Gene> firstChromosome = new ArrayList<Gene>();

            for(int i=0;i<numCloudlets;i++) {
                int k=(i+j)%numVms;
                k=(k+numCloudlets)%numCloudlets;
                Gene geneObj = new Gene(sortedList.get(i),sortedListVm.get(k));
                firstChromosome.add(geneObj);
            }
            Chromosomes chromosome = new Chromosomes(firstChromosome);
            initialPopulation.add(chromosome);
        }

        int populationSize=initialPopulation.size();
        Random random = new Random();
        for(int itr=0;itr<20;itr++)
        {
            int index1,index2;
            index1=random.nextInt(populationSize) % populationSize;
            index2=random.nextInt(populationSize) % populationSize;
            ArrayList<Gene> l1 = initialPopulation.get(index1).getGeneList();
            Chromosomes chromosome1 = new Chromosomes(l1);
            ArrayList<Gene> l2 = initialPopulation.get(index2).getGeneList();
            Chromosomes chromosome2 = new Chromosomes(l2);
            double rangeMin = 0.0f;
            double rangeMax = 1.0f;
            Random r = new Random();
            double crossProb = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
            if(crossProb<0.5)
            {
                int i,j;
                i=random.nextInt(numCloudlets) % numCloudlets;
                j=random.nextInt(numCloudlets) % numCloudlets;
                Vm vm1 = l1.get(i).getVmFromGene();
                Vm vm2 = l2.get(j).getVmFromGene();
                chromosome1.updateGene(i, vm2);
                chromosome2.updateGene(j, vm1);
                initialPopulation.set(index1, chromosome1);
                initialPopulation.set(index2, chromosome2);
            }
            double mutProb = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
            if(mutProb<0.5)
            {
                int i;
                i=random.nextInt(populationSize) % populationSize;
                ArrayList<Gene> l= new ArrayList<Gene>();
                l=initialPopulation.get(i).getGeneList();
                Chromosomes mutchromosome = new Chromosomes(l);
                int j;
                j=random.nextInt(numCloudlets) % numCloudlets;
                Vm vm1 = sortedListVm.get(0);
                mutchromosome.updateGene(j,vm1);
            }
        }
        int fittestIndex=0;
        double time=1000000;

        for(int i=0;i<populationSize;i++)
        {
            ArrayList<Gene> l = initialPopulation.get(i).getGeneList();
            double sum=0;
            for(int j=0;j<numCloudlets;j++)
            {
                Gene g = l.get(j);
                Cloudlet c = g.getCloudletFromGene();
                Vm v = g.getVmFromGene();
                double temp = c.getCloudletLength()/v.getMips();
                sum+=temp;
            }
            if(sum<time)
            {
                time=sum;
                fittestIndex=i;
            }
        }

        ArrayList<Gene> result = initialPopulation.get(fittestIndex).getGeneList();

        List<Cloudlet> finalcloudletList = new ArrayList<>();
        List<Vm> finalvmlist = new ArrayList<>();


        for (Gene gene : result) {
            finalcloudletList.add(gene.getCloudletFromGene());
            finalvmlist.add(gene.getVmFromGene());
        }

        getVmList().clear();
        getCloudletList().clear();

        getVmList().addAll(finalvmlist);
        getCloudletList().addAll(finalcloudletList);
    }

    @Override
    public void submitCloudletList(List<? extends Cloudlet> list) {
        getCloudletList().addAll(list);
        runGeneticAlgorithm();
    }

}
