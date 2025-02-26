package wfos.detector

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import com.typesafe.config.Config
import csw.command.client.CommandResponseManager
import csw.event.api.scaladsl.EventPublisher
import csw.framework.models.CswContext
import csw.params.commands.CommandIssue.UnsupportedCommandIssue
import csw.params.commands.CommandResponse.{Accepted, Completed, Invalid}
import csw.params.core.models.Id
import csw.params.events.{OpticalDetectorEvent, ObserveEvent}
import csw.prefix.models.Prefix
import csw.time.core.models.UTCTime
import csw.time.scheduler.api.TimeServiceScheduler
import wfos.detector.commands.{ControllerMessage, FitsData, FitsMessage}
import wfos.detector.commands.ControllerMessage._
import wfos.detector.commands.FitsMessage.WriteData

import java.time.Instant

class ControllerActor(cswContext: CswContext, config: Config) {
  private val crm: CommandResponseManager                = cswContext.commandResponseManager
  private val timeServiceScheduler: TimeServiceScheduler = cswContext.timeServiceScheduler
  private val eventPublisher: EventPublisher             = cswContext.eventService.defaultPublisher
  private val detectorPrefix: Prefix                     = cswContext.componentInfo.prefix
  private val detectorDimensions: (Int, Int)             = (config.getInt("xs"), config.getInt("ys"))
  private val log                                        = cswContext.loggerFactory.getLogger

  private def generateFakeImageData(xs: Int, ys: Int) = Array.tabulate(xs, ys)((x, y) => x * xs + y)

  lazy val uninitialized: Behavior[ControllerMessage] = {
    log.info("Detector is now in 'UnInitialised' state")
    receiveWithDefaultBehavior("UnInitialised") {
      case Initialize(runId) =>
        crm.updateCommand(Completed(runId))
        idle
      case IsValid(runId, commandName, replyTo) if commandName == Constants.Initialize =>
        replyTo ! Accepted(runId)
        Behaviors.same
    }
  }

  private lazy val idleHandler: PartialFunction[ControllerMessage, Behavior[ControllerMessage]] = {
    case ConfigureExposure(runId, exposureId, filename, ramps, rampIntegrationTime) =>
      crm.updateCommand(Completed(runId))
      loaded(ControllerData(filename, exposureId, ramps, rampIntegrationTime, 0))
    case Shutdown(runId) =>
      crm.updateCommand(Completed(runId))
      uninitialized
    case IsValid(runId, commandName, replyTo) if commandName == Constants.Shutdown || commandName == Constants.LoadConfiguration =>
      replyTo ! Accepted(runId)
      Behaviors.same
  }

  private lazy val idle: Behavior[ControllerMessage] = {
    log.info("Detector is now in 'Non-Configured' state")
    receiveWithDefaultBehavior("Non-Configured")(idleHandler)
  }

  private def loaded(data: ControllerData): Behavior[ControllerMessage] = Behaviors.setup { ctx =>
    log.info(s"Detector is now in 'Configured' state with config $data")
    receiveWithDefaultBehavior("configured") {
      idleHandler.orElse {
        case StartExposure(runId, replyTo) =>
          ctx.self ! ExposureInProgress(runId)
          eventPublisher.publish(OpticalDetectorEvent.exposureStart(detectorPrefix, data.exposureId))
          exposureInProgress(data.resetRamp(), replyTo)
        case IsValid(runId, commandName, replyTo) if commandName == Constants.StartExposure =>
          replyTo ! Accepted(runId)
          Behaviors.same
      }
    }
  }

  private def exposureInProgress(data: ControllerData, replyTo: ActorRef[FitsMessage]): Behavior[ControllerMessage] =
    Behaviors.setup { ctx =>
      log.info("Exposure Started and Detector is now in 'Exposing' state")
      var isExposureRunning = true

      def finishExposure(runId: Id, event: ObserveEvent): Behavior[ControllerMessage] = {
        isExposureRunning = false
        eventPublisher.publish(event)
        val fitsData = FitsData(generateFakeImageData(detectorDimensions._1, detectorDimensions._2))
        replyTo ! WriteData(runId, fitsData, data.exposureId, data.filename)
        loaded(data)
      }

      receiveWithDefaultBehavior("exposing") {
        case ExposureInProgress(runId) if isExposureRunning =>
          // publish ObserveEvent
          eventPublisher.publish(
            OpticalDetectorEvent.exposureData(
              detectorPrefix,
              data.exposureId,
              data.ramps,
              data.currentRamp,
              data.ramps * data.rampIntegrationTime,
              calculateTimeRemaining(data)
            )
          )
          if (data.currentRamp == data.ramps) ctx.self ! ExposureFinished(runId)
          else {
            timeServiceScheduler.scheduleOnce(UTCTime(Instant.ofEpochMilli(System.currentTimeMillis() + data.rampIntegrationTime))) {
              ctx.self ! ExposureInProgress(runId)
            }
          }
          exposureInProgress(data.incrementRamp(), replyTo)
        case ExposureInProgress(runId) if !isExposureRunning =>
          crm.updateCommand(Completed(runId))
          loaded(data)
        case AbortExposure(runId) =>
          log.info(s"Exposure Aborted for runId $runId")
          finishExposure(runId, OpticalDetectorEvent.exposureAborted(detectorPrefix, data.exposureId))
        case ExposureFinished(runId) =>
          log.info(s"Exposure Finished for runId $runId")
          finishExposure(runId, OpticalDetectorEvent.exposureEnd(detectorPrefix, data.exposureId))
        case IsValid(runId, commandName, replyTo) if commandName == Constants.Shutdown || commandName == Constants.AbortExposure =>
          replyTo ! Accepted(runId)
          Behaviors.same
      }
    }

  private def receiveWithDefaultBehavior(
      state: String
  )(handle: PartialFunction[ControllerMessage, Behavior[ControllerMessage]]): Behavior[ControllerMessage] =
    Behaviors.receiveMessage(handle.orElse {
      case IsValid(runId, command, replyTo) =>
        val errMsg = s"Command: ${command.name} is not valid in $state state."
        replyTo ! Invalid(runId, UnsupportedCommandIssue(errMsg))
        Behaviors.same
      case _ => Behaviors.unhandled
    })

  private def calculateTimeRemaining(data: ControllerData) = (data.ramps - data.currentRamp) * data.rampIntegrationTime

}
