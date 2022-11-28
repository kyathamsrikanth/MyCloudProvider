import IaaSServiceModel.getClass
import SaaSServiceModel.logger
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
 In Platform  as Service User can request for Vm with custom resource Configuration
*/
object PaaSServiceModel {

  private val logger = LoggerFactory.getLogger(getClass)
  def paasService(): Unit = {

    val simulation = new CloudSim
    val dataCenterIaaS = new DataCenterTypeSimple().getDataCenter(simulation, "datacenterThree")
    // Getting User Config
    logger.info("Getting User Config")
    val config: Config = GetConfigClass(s"IaaSUserConfig") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    val cloudletList = new CloudLets().createCloudLets("PaaSUserConfig.PaaScloudLetOne", config.getInt("PaaSUserConfig.PaaScloudLet"))
    val vmList = new VirtualMachines().createVirtualMachines("vmTwo", config.getInt("PaaSUserConfig.PaaSvmCount"))

    val broker0 = new DatacenterBrokerSimple(simulation)
    broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList)

    simulation.start
    logger.info("Create Utilization Table")
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
