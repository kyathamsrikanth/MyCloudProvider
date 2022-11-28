package common

import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.datacenters.TimeZoned

object Utils {


  def getDatacenterTimeZone(cloudlet: Cloudlet): String = TimeZoned.format(cloudlet.getVm.getHost.getDatacenter.getTimeZone)

  def getVmTimeZone(cloudlet: Cloudlet): String = TimeZoned.format(cloudlet.getVm.getTimeZone)
}
