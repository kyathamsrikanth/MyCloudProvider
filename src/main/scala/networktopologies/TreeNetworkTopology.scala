package networktopologies


import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.{AggregateSwitch, EdgeSwitch, RootSwitch}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters.*
/*
  This Class creates Tree Topology where Root switches are connected to aggregate switches and aggregate switches are connected 
  edge switches which are eventually connected to hosts
*/
object TreeNetworkTopology {

  private val logger = LoggerFactory.getLogger(getClass)

  def createNetwork(datacenter: NetworkDatacenter, simulation: CloudSim): Unit = {
    logger.info("Getting User Config")
    val config: Config = GetConfigClass("treeNetwork") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    logger.info("Creating Root switches")
    val rootSwitches: RootSwitch = new RootSwitch(simulation, datacenter)


    logger.info("Creating Root switches")
    val aggregateSwitches = (0 until config.getInt("treeNetwork.aggregateSwitches")).map {
      _ =>
        val aggregateSwitch = new AggregateSwitch(simulation, datacenter)
        datacenter.addSwitch(aggregateSwitch)
        aggregateSwitch.getUplinkSwitches.add(rootSwitches)
        rootSwitches.getDownlinkSwitches.add(aggregateSwitch)
        aggregateSwitch
    }

    val edgeSwitches = (0 until config.getInt("treeNetwork.edgeSwitches")).map {

      value =>
        val edgeSwitch = new EdgeSwitch(simulation, datacenter)
        datacenter.addSwitch(edgeSwitch)
        edgeSwitch.getUplinkSwitches.add(aggregateSwitches(value / AggregateSwitch.PORTS))
        aggregateSwitches(value / AggregateSwitch.PORTS).getDownlinkSwitches.add(edgeSwitch)
        edgeSwitch
    }

    datacenter.getHostList[NetworkHost].asScala.foreach { host =>
      val switchNum = Math.round(host.getId % Integer.MAX_VALUE / EdgeSwitch.PORTS)
      edgeSwitches(switchNum).connectHost(host)
    }
  }

}
