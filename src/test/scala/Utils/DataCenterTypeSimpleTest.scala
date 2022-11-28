package Utils

import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.scalatest.funsuite.AnyFunSuite

class DataCenterTypeSimpleTest extends AnyFunSuite {

  test("testGetDataCenter") {
    // Unit test to see if a Data Center  is created 
    val simulationVerticalScaling = new CloudSim
    val dataCenterVerticalScaling = new DataCenterTypeSimple().getDataCenter(simulationVerticalScaling, "datacenterOne")

    assert(1 == dataCenterVerticalScaling.getId)
  }

  test("testCreateHost") {
    // Unit test to see if a Host is created 
    val config: Config = GetConfigClass("datacenterOne") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    val hostList = new DataCenterTypeSimple().getHostList(config,"datacenterOne" )
    assert(2 == hostList.size())
  }

}
