
import Utils.{CloudLets, DataCenterTypeSimple, ModelUtilization, VirtualMachines}
import com.typesafe.config.Config
import common.GetConfigClass
import common.Utils.{getDatacenterTimeZone, getVmTimeZone}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.resources.{Processor, Ram}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudsimplus.autoscaling.{HorizontalVmScalingSimple, VerticalVmScaling, VerticalVmScalingSimple}
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.cloudsimplus.listeners.EventInfo
import org.slf4j.LoggerFactory

import java.util
import java.util.Comparator

object ScalingSimulation {

  private val logger = LoggerFactory.getLogger(getClass)
  val config: Config = GetConfigClass("Scaling") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def scaling(): Unit = {

    // Space shared simulationHorizontalScaling
    val simulationHorizontalScaling = new CloudSim
    val dataCenterOneHorizontalScaling = new DataCenterTypeSimple().getDataCenter(simulationHorizontalScaling, "datacenterOne")
    val dataCenterTwoHorizontalScaling = new DataCenterTypeSimple().getDataCenter(simulationHorizontalScaling, "datacenterTwo")
    val dataCenterThreeHorizontalScaling = new DataCenterTypeSimple().getDataCenter(simulationHorizontalScaling, "datacenterThree")


    val cloudletListHorizontalScaling = new CloudLets().createCloudLets("cloudLetOne", 100)
    val vmListHorizontalScaling = new VirtualMachines().createVirtualMachines("vmTwo", 10)
    (0 until(vmListHorizontalScaling.size()) foreach  (x => vmListHorizontalScaling.get(x).setHorizontalScaling(createHorizontalVmScaling)))
    //vmTimeZonesHorizontalScaling.indices foreach (x => vmListHorizontalScaling.get(x).setTimeZone(vmTimeZonesHorizontalScaling(x)))
    val brokerHorizontalScaling = new DatacenterBrokerSimple(simulationHorizontalScaling)
    brokerHorizontalScaling.submitVmList(vmListHorizontalScaling).submitCloudletList(cloudletListHorizontalScaling)

    simulationHorizontalScaling.start

    val finishedCloudletsHorizontalScaling: util.List[Cloudlet] = brokerHorizontalScaling.getCloudletFinishedList
    finishedCloudletsHorizontalScaling.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))

    new CloudletsTableBuilder(finishedCloudletsHorizontalScaling)
      .addColumn(3, new TextTableColumn("   DC   ", "TimeZone"), getDatacenterTimeZone)
      .addColumn(8, new TextTableColumn("VM Expected", " TimeZone "), getVmTimeZone).
      addColumn(new TextTableColumn("Total Cost", "USD"),
        (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP)).build()

    ModelUtilization.calculateCost(brokerHorizontalScaling)
    ModelUtilization.getDataCenterUtilization(dataCenterOneHorizontalScaling)
    ModelUtilization.getDataCenterUtilization(dataCenterTwoHorizontalScaling)
    ModelUtilization.getDataCenterUtilization(dataCenterThreeHorizontalScaling)
    ModelUtilization.getVmsCpuUtilizationAndPowerConsumption(vmListHorizontalScaling)


    val simulationVerticalScaling = new CloudSim
    val dataCenterVerticalScaling = new DataCenterTypeSimple().getDataCenter(simulationVerticalScaling, "datacenterOne")
    val dataCenterTwoVerticalScaling = new DataCenterTypeSimple().getDataCenter(simulationVerticalScaling, "datacenterTwo")
    val dataCenterThreeVerticalScaling = new DataCenterTypeSimple().getDataCenter(simulationVerticalScaling, "datacenterThree")


    val cloudletListVerticalScaling = new CloudLets().createCloudLets("cloudLetOne", 100)
    val vmListVerticalScaling = new VirtualMachines().createVirtualMachines("vmTwo", 10)
    (0 until(vmListVerticalScaling.size()) foreach(x => vmListVerticalScaling.get(x).
      setRamVerticalScaling(createVerticalRamScaling).setPeVerticalScaling(createVerticalPeScaling)))
    //vmTimeZonesVerticalScaling.indices foreach (x => vmListVerticalScaling.get(x).setTimeZone(vmTimeZonesVerticalScaling(x)))
    val brokerVerticalScaling = new DatacenterBrokerSimple(simulationVerticalScaling)
    brokerVerticalScaling.submitVmList(vmListVerticalScaling).submitCloudletList(cloudletListVerticalScaling)

    simulationVerticalScaling.start

    val finishedCloudletsVerticalScaling: util.List[Cloudlet] = brokerVerticalScaling.getCloudletFinishedList
    finishedCloudletsVerticalScaling.sort(Comparator.comparingDouble((cl: Cloudlet) => cl.getVm.getTimeZone))

    new CloudletsTableBuilder(finishedCloudletsVerticalScaling).
      addColumn(new TextTableColumn("Total Cost", "USD"),
        (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP)).build()

    ModelUtilization.calculateCost(brokerVerticalScaling)
    ModelUtilization.getDataCenterUtilization(dataCenterVerticalScaling)
    ModelUtilization.getDataCenterUtilization(dataCenterTwoVerticalScaling)
    ModelUtilization.getDataCenterUtilization(dataCenterThreeVerticalScaling)
    ModelUtilization.getVmsCpuUtilizationAndPowerConsumption(vmListVerticalScaling)
  }


  def lowerCpuUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("Scaling.vmVerticalScaling.CpuScaling.LowerCpuUtilizationThreshold")
  }

  def upperCpuUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("Scaling.vmVerticalScaling.CpuScaling.UpperCpuUtilizationThreshold")
  }

  def lowerRamUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("Scaling.vmVerticalScaling.RamScaling.LowerRamUtilizationThreshold")
  }


  def upperRamUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("Scaling.vmVerticalScaling.RamScaling.UpperRamUtilizationThreshold")
  }

  private def createVerticalRamScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getDouble("Scaling.vmVerticalScaling.ScalingFactor")
    val verticalRamScaling = new VerticalVmScalingSimple(classOf[Ram], scalingFactor)

    verticalRamScaling.setLowerThresholdFunction(lowerRamUtilizationThreshold)
    verticalRamScaling.setUpperThresholdFunction(upperRamUtilizationThreshold)
    verticalRamScaling
  }

  private def createVerticalPeScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getDouble("Scaling.vmVerticalScaling.ScalingFactor")
    val verticalCpuScaling = new VerticalVmScalingSimple(classOf[Processor], scalingFactor)

    verticalCpuScaling.setResourceScaling((vs: VerticalVmScaling) => 2 * vs.getScalingFactor * vs.getAllocatedResource)
    verticalCpuScaling.setLowerThresholdFunction(lowerCpuUtilizationThreshold)
    verticalCpuScaling.setUpperThresholdFunction(upperCpuUtilizationThreshold)
    verticalCpuScaling
  }

  private def createHorizontalVmScaling = {
    val horizontalScaling = new HorizontalVmScalingSimple
    horizontalScaling.setVmSupplier(() => new VirtualMachines().getVM(config,"vmTwo")).setOverloadPredicate(isVmOverloaded)
    horizontalScaling
  }
  private def isVmOverloaded(vm: Vm) = vm.getCpuPercentUtilization > config.getLong("Scaling.vmHorizontalScaling.OverLoadThreshold")


}
