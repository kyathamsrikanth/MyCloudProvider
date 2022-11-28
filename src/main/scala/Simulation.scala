
import FaaSServiceModel.getClass
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation {
  private val logger = LoggerFactory.getLogger(getClass)

  @main def runSimulation =
    logger.info("Strating all the Simulations")
    println("\n\n\n")
    println("Simulation 1")
    println("\n\n\n")
    logger.info("Starting the Space Shared Simulation for the configuration given in application.conf")

    SpaceSharedSimulation.spaceShared()

    println("\n\n\n")
    println("Simulation 2")
    println("\n\n\n")

    logger.info("Starting the Time Shared Simulation for the configuration given in application.conf")
    TimeSharedSimulation.timeShared()

    println("\n\n\n")
    println("Simulation 3")
    println("\n\n\n")

    logger.info("Started the Simulation for Auto Scaling")
    ScalingSimulation.scaling()

    println("\n\n\n")
    println("Simulation 4")
    println("\n\n\n")

    logger.info("Started the Simulation for SAAS")
    SaaSServiceModel.saasService()

    println("\n\n\n")
    println("Simulation 5")
    println("\n\n\n")

    logger.info("Started the  Simulation for PAAS")
    PaaSServiceModel.paasService()

    println("\n\n\n")
    println("Simulation 6")
    println("\n\n\n")

    logger.info("Started the Simulation for network")
    NetworkSimulationBuild.networkSimulation()


    println("\n\n\n")
    println("Simulation 7")
    println("\n\n\n")

    logger.info("Started the Simulation for IAAS")
    IaaSServiceModel.iaasService()

    println("\n\n\n")
    println("Simulation 8")
    println("\n\n\n")

    logger.info("Started the Simulation for FAAS")
    FaaSServiceModel.faasService()


    println("\n\n\n")
    println("Simulation 9")
    println("\n\n\n")

    logger.info("Started the Simulation for VM Allocation")
    VmAllocationPolicyWithTimeZonePreferance.vmAllocationPolicyWithTimeZonePreferance()

    println("\n\n\n")
    println("Simulation 10")
    println("\n\n\n")

    logger.info("Started the Simulation for VM Allocation")
    VmAllocationPolicyWithOutTimeZonePreferance.vmAllocationPolicyWithOutTimeZonePreferance()

    println("\n")


    logger.info("Completed cloud simulation...")
}