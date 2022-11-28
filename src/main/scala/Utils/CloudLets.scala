package Utils

import com.typesafe.config.Config
import common.GetConfigClass
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic

import java.util
import java.util.{ArrayList, List}

class CloudLets {

  def createCloudLets(Config : String, cloudLetsNumber: Int) :util.ArrayList[Cloudlet] = {

    val config: Config = GetConfigClass(s"$Config") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val cloudList = new util.ArrayList[Cloudlet](cloudLetsNumber)
    //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
    (1 to cloudLetsNumber)foreach(x => getCloudList(cloudList,config,Config))
    cloudList
  }

  def getCloudList(cloudList: util.ArrayList[Cloudlet],config : Config,Config:String): Unit ={
    val pes = config.getInt(s"$Config.pes")
    val length = config.getLong(s"$Config.length")
    val size = config.getInt(s"$Config.size")
    val utilization = config.getDouble(s"$Config.utilization")
    val utilizationModel = new UtilizationModelDynamic(utilization)
    val cloudlet = new CloudletSimple(length, pes, utilizationModel)
    cloudlet.setSizes(size)
    cloudList.add(cloudlet);
  }
}
