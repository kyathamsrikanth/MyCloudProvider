package Utils

import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyRoundRobin
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.funsuite.AnyFunSuite

class DataCenterTypeNetworkTest extends AnyFunSuite {

  test("testGetDataCenter") {
    // Unit test to see if a Data Center  is created
    val simulationVerticalScaling = new CloudSim
    val dataCenterVerticalScaling = DataCenterTypeNetwork().getDataCenter(simulationVerticalScaling, "datacenterOne", new VmAllocationPolicyRoundRobin())

    assert(1 == dataCenterVerticalScaling.getId)
  }

  test("testCreateHost") {
    // Unit test to see if a Host is created
    val config: Config = GetConfigClass("datacenterOne") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    val hostList = new DataCenterTypeNetwork().getHostList(config, "datacenterOne")
    assert(2 == hostList.size())
  }

}
