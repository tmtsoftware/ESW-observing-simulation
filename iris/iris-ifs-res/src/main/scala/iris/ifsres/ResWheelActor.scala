package iris.ifsres

import org.apache.pekko.Done
import org.apache.pekko.actor.typed.Behavior
import csw.framework.models.CswContext
import iris.commons.WheelAssembly
import iris.commons.models.{WheelCommand, AssemblyConfiguration}
import iris.ifsres.events.IfsPositionEvent
import iris.ifsres.models.ResWheelPosition

import scala.concurrent.Future

class ResWheelActor(cswContext: CswContext, configuration: AssemblyConfiguration)
    extends WheelAssembly[ResWheelPosition](cswContext, configuration) {
  private lazy val eventPublisher = cswContext.eventService.defaultPublisher

  protected val name: String = "Res Wheel"
  override def publishPosition(current: ResWheelPosition, target: ResWheelPosition, dark: Boolean): Future[Done] =
    eventPublisher.publish(IfsPositionEvent.make(current, target))
}

object ResWheelActor {
  val InitialPosition: ResWheelPosition = ResWheelPosition.R4000_Z

  def behavior(cswContext: CswContext, configuration: AssemblyConfiguration): Behavior[WheelCommand[ResWheelPosition]] =
    new ResWheelActor(cswContext, configuration).idle(InitialPosition)
}
