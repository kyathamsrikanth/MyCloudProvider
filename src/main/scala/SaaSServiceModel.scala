import FaaSServiceModel.{getClass, logger}
import IaaSServiceModel.logger
import Utils.{CloudLets, DataCenterTypeSimple, ModelUtilization, VirtualMachines}
import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.LoggerFactory

import java.util
import java.util.Comparator


/*
 In Function  as a Service User submits cloudlets cloud provider provides vms and Cloud provider handles allocation and
  auto scaling of resources based on need
*/

object SaaSServiceModel {
  private val logger = LoggerFactory.getLogger(getClass)
  def saasService(): Unit = {
    
    logger.info("Getting User Config")
    val simulation = new CloudSim
    val dataCenterIaaS = new DataCenterTypeSimple().getDataCenter(simulation, "ProviderConfig")

    val config: Config = GetConfigClass(s"SaaSUserConfig") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    val cloudletList = new CloudLets().createCloudLets("SaaSUserConfig.SaaScloudLetOne", config.getInt("SaaSUserConfig.SaaScloudLet"))
    val vmList = new VirtualMachines().createVirtualMachines("vmTwo",  config.getInt("ProviderConfig.vmAmount"))

    val broker0 = new DatacenterBrokerSimple(simulation)
    broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList)
    logger.info("Create Utilization Table")
    simulation.start
    // get Finished Cloudlets
    val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList
    finishedCloudlets.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))

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
