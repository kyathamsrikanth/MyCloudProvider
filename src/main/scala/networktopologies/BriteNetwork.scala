package networktopologies

import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.slf4j.LoggerFactory

object BriteNetwork {

  private val logger = LoggerFactory.getLogger(getClass)
  def networkConfiguration(topology: String, cloudSim: CloudSim, datacenter: Datacenter,datacenter1: Datacenter,datacenter2: Datacenter, datacenterBroker: DatacenterBroker): Unit = {
    val networkTopology = BriteNetworkTopology.getInstance(topology)
    cloudSim.setNetworkTopology(networkTopology)
    networkTopology.mapNode(datacenter, 0)
    logger.info(s"$datacenter mapped to 0")
    networkTopology.mapNode(datacenterBroker, 2)
    logger.info(s"$datacenter mapped to 2")
    networkTopology.mapNode(datacenter1, 3)
    logger.info(s"$datacenter mapped to 2")
    networkTopology.mapNode(datacenter2, 4)
    logger.info(s"$datacenter mapped to 2")
    
  }

}
