package Utils

import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.slf4j.LoggerFactory

import java.util
import java.util.{ArrayList, List}
/*
  This Class creates VmSimple  Virtual Machines using  Config  with below Cloudlet Scheduling 
  CloudLetSchedulerSpaceShared - resources are shared across cloudlets
  CloudLetSchedulerTimeShared  - Context Switching is used to process cloudlets
*/

class VirtualMachines {
  private val logger = LoggerFactory.getLogger(getClass)

  def createVirtualMachines(Config: String, virtualMachinesNumber: Int): util.ArrayList[Vm] = {
    /// getting Config 
    logger.info("Getting Config ")
    val config: Config = GetConfigClass(s"$Config") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    /// Create Vms for each Data Center
    logger.info("Create Vms for each Data Center")
    val vmList = new util.ArrayList[Vm](virtualMachinesNumber)
    (1 to virtualMachinesNumber) foreach (x => getVMList(vmList, config,Config))
    vmList

  }

  def getVMList(vmList: util.ArrayList[Vm], config: Config,Config:String): Unit = {
    val vm: VmSimple = getVM(config, Config)
    vmList.add(vm);
  }

  def getVM(config: Config, Config: String) = {
    val pes = config.getInt(s"$Config.pes")
    val ram = config.getLong(s"$Config.ram")
    val bw = config.getInt(s"$Config.bw")
    val size = config.getInt(s"$Config.size")
    val mips = config.getInt(s"$Config.mips")
    val vm = new VmSimple(mips, pes);
    val CloudletScheduler = config.getString(s"$Config.CloudletScheduler")
    // Choose Scheduler basewd on config provided
    logger.info("Choose Scheduler basewd on config provided")
    CloudletScheduler match
      case "CloudletSchedulerSpaceShared" => vm.setRam(ram).setBw(bw).setSize(size).setCloudletScheduler(new CloudletSchedulerSpaceShared())
      case "CloudletSchedulerTimeShared" => vm.setRam(ram).setBw(bw).setSize(size).setCloudletScheduler(new CloudletSchedulerTimeShared())
    vm.enableUtilizationStats()
    vm
  }
}
