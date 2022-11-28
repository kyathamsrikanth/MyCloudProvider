package networktopologies

import Utils.DataCenterTypeNetwork
import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyRoundRobin
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.funsuite.AnyFunSuite
class TreeNetworkTopologyTest extends AnyFunSuite {

  test("TestCreateNetwork") {
    
    val simulation = new CloudSim
    val dataCenterOne = new DataCenterTypeNetwork().getDataCenter(simulation, "datacenterOne",new  VmAllocationPolicyRoundRobin())
    TreeNetworkTopology.createNetwork(dataCenterOne, simulation)
    val switchMap = dataCenterOne.getSwitchMap
    assert(4 == switchMap.size())

  }

}
