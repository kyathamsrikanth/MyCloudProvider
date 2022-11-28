import NetworkSimulationBuild.getClass
import Utils.{CloudLets, DataCenterTypeSimple, ModelUtilization, VirtualMachines}
import com.typesafe.config.Config
import common.GetConfigClass
import common.Utils.{getDatacenterTimeZone, getVmTimeZone}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.LoggerFactory

import java.util
import java.util.Comparator


/*
 In Infra Structure as Service User can request for Vm with custom resource Configuration
*/
object IaaSServiceModel {
  
  private val logger = LoggerFactory.getLogger(getClass)
  
  def iaasService(): Unit = {

    val simulation = new CloudSim
    val dataCenterIaaS = new DataCenterTypeSimple().getDataCenter(simulation, "datacenterOne")
    // Getting User Config
    logger.info("Getting User Config")
    val config: Config = GetConfigClass(s"IaaSUserConfig") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    // Create User requested CloudLets  from config
    val cloudletList = new CloudLets().createCloudLets("IaaSUserConfig.IaaScloudLetOne", config.getInt("IaaSUserConfig.IaaScloudLet"))

    // Create User requested Vms from config
    logger.info("Creating User requested Vms from config")
    val vmList = new VirtualMachines().createVirtualMachines("IaaSUserConfig.IaaSvmTwo", config.getInt("IaaSUserConfig.IaaSvmCount"))
    // Creat Datacenter Broker
    val broker0 = new DatacenterBrokerSimple(simulation)
    broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList)
    // Start Simulation
    simulation.start

    // get Finished Cloudlets
    val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList
    finishedCloudlets.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))
    /// Create Utilization Table
    logger.info("Create Utilization Table")
    new CloudletsTableBuilder(finishedCloudlets)
      .addColumn(new TextTableColumn("Total Cost", "USD"),
        (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP)).build()
    // Calculate Power Consumption
    logger.info("Calculate Power Consumption")
    ModelUtilization.calculateCost(broker0)
    ModelUtilization.getDataCenterUtilization(dataCenterIaaS)
    ModelUtilization.getVmsCpuUtilizationAndPowerConsumption(vmList)
  }




}
