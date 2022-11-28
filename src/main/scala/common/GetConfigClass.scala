package common


import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
 * GetConfigRef: Common methods used to fectch values from application Config
 *
 */

object GetConfigClass {
  private val config = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger(classOf[GetConfigClass.type])

  def apply(confEntry: String): Option[Config] = Try(config.getConfig(confEntry)) match {
    case Failure(exception) => logger.error(s"Failed to retrieve config entry $confEntry for reason $exception"); None
    case Success(_) => Some(config)
  }
}
