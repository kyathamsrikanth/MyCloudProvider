import PaaSServiceModel.logger
import Utils.ModelUtilization.getClass
import Utils.{CloudLets, DataCenterTypeNetwork, DataCenterTypeSimple, ModelUtilization, VirtualMachines}
import networktopologies.{BriteNetwork, TreeNetworkTopology}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyRoundRobin}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, TimeZoned}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.{AggregateSwitch, EdgeSwitch, RootSwitch}
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.LoggerFactory
import common.Utils.{getDatacenterTimeZone, getVmTimeZone}

import scala.collection.JavaConverters.*
import java.util
import java.util.Comparator

/*
  Tree Network Topology used for Creating Network in side Data Center and
   Brite Topology Network is used to to connect outside Date Structure
*/

object NetworkSimulationBuild {
  private val logger = LoggerFactory.getLogger(getClass)
  def networkSimulation(): Unit ={

    logger.info("Create Cloud Sim Object")
    val simulation = new CloudSim
    // Create Three Data Centers with Different VM allocation Policy

    logger.info("Creating Three Data Center with Different Allocation Policy")
    val dataCenterOne = new DataCenterTypeNetwork().getDataCenter(simulation, "datacenterOne",new  VmAllocationPolicyRoundRobin())
    val dataCenterTwo = new DataCenterTypeNetwork().getDataCenter(simulation, "datacenterTwo",new VmAllocationPolicyBestFit())
    val dataCenterThree = new DataCenterTypeNetwork().getDataCenter(simulation, "datacenterThree",new VmAllocationPolicyRoundRobin())
    // Assign time zone to all  datacenter 

    logger.info("Assign time zone to all  datacenter ")
    dataCenterOne.setTimeZone(2.0)
    dataCenterTwo.setTimeZone(8.0)
    dataCenterThree.setTimeZone(-6.0)
    // Assigning Timezones to Virual Machines
    logger.info("Assigning Timezones to Virual Machines ")
    val vmTimeZones = Array(1.0, 7.0, -3.0, -4.0, 5.0)

    TreeNetworkTopology.createNetwork(dataCenterOne, simulation)
    TreeNetworkTopology.createNetwork(dataCenterTwo, simulation)
    TreeNetworkTopology.createNetwork(dataCenterThree, simulation)
    //  Creating Cloudlets
    logger.info("Creating Cloudlets ")
    val cloudletList = new CloudLets().createCloudLets("cloudLetOne", 40)

    //  Creating Vms  using config 
    logger.info("Creating Vms  using config  ")
    val vmList = new VirtualMachines().createVirtualMachines("vmTwo", 10)
    vmTimeZones.indices foreach (x => vmList.get(x).setTimeZone(vmTimeZones(x)))
    val broker0 = new DatacenterBrokerSimple(simulation)
    broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList)
    // Load Brite Topology from file and assign to data centers and brokers
    logger.info("Load Brite Topology from file and assign to data centers and brokers")
    BriteNetwork.networkConfiguration("src/main/resources/topology.brite", simulation, dataCenterOne,dataCenterTwo,dataCenterThree,broker0)
    simulation.start
   
    val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList
    finishedCloudlets.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))
    // Build Table utilization
    new CloudletsTableBuilder(finishedCloudlets)
      .addColumn(3, new TextTableColumn("   DC   ", "TimeZone"), getDatacenterTimeZone)
      .addColumn(8, new TextTableColumn("VM Expected", " TimeZone "), getVmTimeZone).
      addColumn(new TextTableColumn("Total Cost", "USD"),
        (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP)).build()
    // Calculate Power consumption
    ModelUtilization.calculateCost(broker0)
    ModelUtilization.getDataCenterUtilization(dataCenterOne)
    ModelUtilization.getDataCenterUtilization(dataCenterTwo)
    ModelUtilization.getDataCenterUtilization(dataCenterThree)
    ModelUtilization.getVmsCpuUtilizationAndPowerConsumption(vmList)
  }





}
