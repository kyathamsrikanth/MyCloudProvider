package Utils

import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.slf4j.LoggerFactory

import java.util

class DataCenterTypeNetwork {

  private val logger = LoggerFactory.getLogger(getClass)

  //  val config: Config = GetConfigClass("datacenterone") match {
  //    case Some(value) => value
  //    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  //  }

  def getDataCenter(simulation: CloudSim, Config: String,vmPolicy: VmAllocationPolicy): NetworkDatacenter = {


    val config: Config = GetConfigClass(s"$Config") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val dataCenter = new NetworkDatacenter(simulation, getHostList(config, Config),vmPolicy)
    setCharacteristics(dataCenter, config, Config)
    dataCenter.setSchedulingInterval(10)
    dataCenter
  }

  //  def getHostList(config :Config,Config : String): util.List[Host] = {
  //    val hostOne = config.getConfig(s"$Config.hostOne")
  //    val hostTwo = config.getConfig(s"$Config.hostTwo")
  //    val hostList = new util.ArrayList[Host](2)
  //    hostList.add(createHost(hostOne))
  //    //hostList.add(createHost(hostTwo))
  //    hostList
  //  }

  def getHostList(config: Config, Config: String): util.List[Host] = {
    val hostNumber = config.getInt(s"$Config.hostNumber")
    val hostConfig = config.getConfig(s"$Config.hostConfig")
    val hostList = new util.ArrayList[Host](hostNumber)
    (0 until (hostNumber)) foreach (x => hostList.add(createHost(hostConfig, x)))
    hostList
  }

  def createHost(config: Config, id: Long): Host = {
    val hostPES = config.getInt("cores")
    val peList = new util.ArrayList[Pe](hostPES);
    val mips = config.getInt("mips")
    (1 to hostPES) foreach (x => peList.add(new PeSimple(mips)))
    val ram = config.getInt("RAM")
    val bandwidth = config.getInt("bw")
    val storage = config.getInt("storageGB")
    val host_start_up_delay = config.getInt("host_start_up_delay")
    val host_shut_down_delay = config.getInt("host_shut_down_delay")
    val host_start_up_power = config.getInt("host_start_up_power")
    val host_shut_down_power = config.getInt("host_shut_down_power")
    val max_power = config.getInt("max_power")
    val static_power = config.getInt("static_power")
    val host = new NetworkHost(ram, bandwidth, storage, peList)
    val powerModel = new PowerModelHostSimple(max_power, static_power)
    //powerModel.setStartupDelay(host_start_up_delay).setShutDownDelay(host_shut_down_delay).setStartupPower(host_start_up_power).setShutDownPower(host_shut_down_power)
    host.setVmScheduler(new VmSchedulerTimeShared).setPowerModel(powerModel)
    host.setId(id)
    host.enableUtilizationStats()
    host
  }

  def setCharacteristics(dataCenter: DatacenterSimple, config: Config, Config: String): Unit = {
    val costByMemory = config.getDouble(s"$Config.costByMemory")
    val costBySec = config.getDouble(s"$Config.costBySec")
    val costByBW = config.getDouble(s"$Config.costByBW")
    val costByStorage = config.getDouble(s"$Config.costByStorage")
    val arch = config.getString(s"$Config.arch")
    val vmm = config.getString(s"$Config.vmm")
    val os = config.getString(s"$Config.os")
    dataCenter.getCharacteristics
      .setVmm(vmm)
      .setCostPerBw(costByBW)
      .setCostPerMem(costByMemory)
      .setCostPerSecond(costBySec)
      .setCostPerStorage(costByStorage)
      .setArchitecture(arch)
      .setOs(os)
  }

}
