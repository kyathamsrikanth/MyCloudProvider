import Utils.{CloudLets, ModelUtilization, DataCenterTypeSimple, VirtualMachines}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple, TimeZoned}
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.{Logger, LoggerFactory}

import java.util
import java.util.{Comparator, List}

import common.Utils.{getDatacenterTimeZone, getVmTimeZone}
object SpaceSharedSimulation {




  def spaceShared(): Unit ={
    
    // Space shared simulation
    val simulation = new CloudSim
    val dataCenterOne = new DataCenterTypeSimple().getDataCenter(simulation,"datacenterOne")
    val dataCenterTwo = new DataCenterTypeSimple().getDataCenter(simulation,"datacenterTwo")
    val dataCenterThree = new DataCenterTypeSimple().getDataCenter(simulation,"datacenterThree")
    dataCenterOne.setTimeZone(2.0)
    dataCenterTwo.setTimeZone(8.0)
    dataCenterThree.setTimeZone(-6.0)

    val vmTimeZones = Array(1.0, 7.0, -3.0,-4.0,5.0)

    val cloudletList = new CloudLets().createCloudLets("cloudLetOne",40)
    val vmList = new VirtualMachines().createVirtualMachines("vmTwo",10)
    vmTimeZones.indices foreach (x => vmList.get(x).setTimeZone(vmTimeZones(x)))
    val broker0 = new DatacenterBrokerSimple(simulation)
    broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList)

    simulation.start

    val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList
    finishedCloudlets.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))

    new CloudletsTableBuilder(finishedCloudlets)
       .addColumn(3, new TextTableColumn("   DC   ", "TimeZone"), getDatacenterTimeZone)
      .addColumn(8, new TextTableColumn("VM Expected", " TimeZone "), getVmTimeZone).
      addColumn(new TextTableColumn("Total Cost", "USD"), 
        (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP)).build()

    ModelUtilization.calculateCost(broker0)
    ModelUtilization.getDataCenterUtilization(dataCenterOne)
    ModelUtilization.getDataCenterUtilization(dataCenterTwo)
    ModelUtilization.getDataCenterUtilization(dataCenterThree)
    ModelUtilization.getVmsCpuUtilizationAndPowerConsumption(vmList)
  }


}
