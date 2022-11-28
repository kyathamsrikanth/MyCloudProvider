package Utils

import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple
import org.cloudbus.cloudsim.vms.{HostResourceStats, Vm, VmCost, VmResourceStats}
import org.slf4j.LoggerFactory

import java.util
import java.util.Comparator.comparingLong
import scala.collection.JavaConverters.*

/*
   Calculate Utilization Cost and Power Utilization Cost for resources used

  Gets the total cost of executing this Cloudlet.
  Total Cost = input data transfer + processing cost + output transfer cost.
  Realize costs must be defined for Datacenters by accessing the DatacenterCharacteristics
  object from each Datacenter instance and setting costs for each resource.
*/
object ModelUtilization {
  
  private val logger = LoggerFactory.getLogger(getClass)

  def calculateCost(broker: DatacenterBroker): Double = {
   /// Calculating Cost from  Cloudlets 
    val cloudletList = broker.getCloudletFinishedList.asInstanceOf[java.util.List[Cloudlet]].asScala
    logger.info("Calculating Cost")
    val list: List[Int] = cloudletList.toList.map { (cloudlet: Cloudlet) =>
      cloudlet.getTotalCost.toInt
    }
    /// Finding Sum
    val totalCost = sum(list)

    logger.info("Total cost fo running all the task: " + totalCost)
    totalCost
  }

  def sum(list: List[Int]): Int = list match {
    case Nil => 0
    case x :: xs => x + sum(xs)
  }

  
  def getHostCpuUtilizationAndPowerConsumption(host: Host,id : Long): Unit = {
    val cpuStats = host.getCpuUtilizationStats
    //The total Host's CPU utilization for the time specified by the map key
    val utilizationPercentMean = cpuStats.getMean
    val watts = host.getPowerModel.getPower(utilizationPercentMean)
    logger.info("Data Center" + id + "  Host " + host.getId + " CPU Usage mean: " + utilizationPercentMean * 100 + " | Power Consumption mean: " +  watts)
  }

  def getDataCenterUtilization(dataCenter: DatacenterSimple): Unit ={

    (0 until(dataCenter.getHostList.size())) foreach(x => getHostCpuUtilizationAndPowerConsumption(dataCenter.getHostList.get(x),dataCenter.getId))

  }

  def getVmsCpuUtilizationAndPowerConsumption(vmList :util.ArrayList[Vm]): Unit = {
    vmList.sort(comparingLong((vm: Vm) => vm.getHost.getId))
    (0 until(vmList.size())) foreach(x => getVmCpuUtilizationAndPowerConsumption(vmList.get(x)))

  }


  def getVmCpuUtilizationAndPowerConsumption(vm: Vm): Unit = {
    val powerModel = vm.getHost.getPowerModel
    //VM CPU utilization relative to the host capacity
    val vmRelativeCpuUtilization = vm.getCpuUtilizationStats.getMean / vm.getHost.getVmCreatedList.size
    val vmPower = powerModel.getPower(vmRelativeCpuUtilization) 
    val cpuStats = vm.getCpuUtilizationStats
    logger.info("Vm  " + vm.getId + "  CPU Usage Mean: " + cpuStats.getMean * 100 + "| Power Consumption Mean: " + vmPower)

  }

}
