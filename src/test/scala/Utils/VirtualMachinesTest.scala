package Utils

import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.vms.VmSimple
import org.scalatest.funsuite.AnyFunSuite

class VirtualMachinesTest extends AnyFunSuite {

  test("testGetVM") {
    // Unit test to see if a VM is created
    val config: Config = GetConfigClass("vmOne") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val vm: VmSimple = VirtualMachines().getVM(config, "vmOne")
    assert(-1 == vm.getId)
  }

  test("testCreateVirtualMachines") {
    // Unit test to see if a VirtualMachines are created
    val vmList = new VirtualMachines().createVirtualMachines("vmTwo", 10)
    assert(10 == vmList.size())
  }

}
