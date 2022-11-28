import IaaSServiceModel.{getClass, logger}
import ScalingSimulation.{config, createHorizontalVmScaling}
import Utils.{CloudLets, DataCenterTypeSimple, ModelUtilization, VirtualMachines}
import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.LoggerFactory

import java.util
import java.util.Comparator


/*
 In Function  as a Service User submits cloudlets cloud provider provides vms and Cloud provider handles allocation and
  auto scaling of resources based on need
*/
object FaaSServiceModel {

  private val logger = LoggerFactory.getLogger(getClass)
  def faasService(): Unit = {
    
    val simulation = new CloudSim
    val dataCenterIaaS = new DataCenterTypeSimple().getDataCenter(simulation, "datacenterTwo")
    // Getting User Config
    logger.info("Getting User Config")
    val config: Config = GetConfigClass(s"SaaSUserConfig") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    // Create User requested CloudLets  from config
    val cloudletList = new CloudLets().createCloudLets("FaaSUserConfig.FaaScloudLetOne", config.getInt("FaaSUserConfig.FaaScloudLet"))
    // Create User requested Vms from config
    logger.info("Creating User requested Vms from config")
    val vmList = new VirtualMachines().createVirtualMachines("vmTwo", config.getInt("ProviderConfig.vmAmount"))
    (0 until(vmList.size()) foreach  (x => vmList.get(x).setHorizontalScaling(createHorizontalVmScaling)))
    // Creat Datacenter Broker
    val broker0 = new DatacenterBrokerSimple(simulation)
    broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList)

    simulation.start
    logger.info("Create Utilization Table")
    val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList
    finishedCloudlets.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))
    /// Create Utilization Table
    new CloudletsTableBuilder(finishedCloudlets)
      .addColumn(new TextTableColumn("Total Cost", "USD"),
        (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP)).build()
    // Calculate Power Consumption
    ModelUtilization.calculateCost(broker0)
    ModelUtilization.getDataCenterUtilization(dataCenterIaaS)
    ModelUtilization.getVmsCpuUtilizationAndPowerConsumption(vmList)

  }

  
  // Horizontal Scaling when Vm is overloaded
  private def createHorizontalVmScaling = {
    val horizontalScaling = new HorizontalVmScalingSimple
    horizontalScaling.setVmSupplier(() => new VirtualMachines().getVM(config, "vmTwo")).setOverloadPredicate(checkVMCapacity)
    horizontalScaling
  }
  // Check if Vm is OverLoaded or not 
  private def checkVMCapacity(vm: Vm) = vm.getCpuPercentUtilization > config.getLong("Scaling.vmHorizontalScaling.OverLoadThreshold")


}
