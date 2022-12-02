package iris.ifsscale.models

import csw.params.core.generics.GChoiceKey
import csw.params.core.generics.KeyType.ChoiceKey
import csw.params.core.models.{Choices, Units}
import enumeratum.Enum
import iris.commons.models.Position

import scala.collection.immutable.IndexedSeq

sealed class ScaleLevel(override val entryName: String) extends Position[ScaleLevel] {
  override def nextPosition(target: ScaleLevel): ScaleLevel = target
}

object ScaleLevel extends Enum[ScaleLevel] {
  override def values: IndexedSeq[ScaleLevel] = findValues

  private lazy val choices: Choices              = Choices.from(ScaleLevel.values.map(_.entryName): _*)
  def makeChoiceKey(keyName: String): GChoiceKey = ChoiceKey.make(keyName, Units.mas, choices)

  case object S4  extends ScaleLevel("4")
  case object S9  extends ScaleLevel("9")
  case object S25 extends ScaleLevel("25")
  case object S50 extends ScaleLevel("50")
}
