package esw.observing.simulation

import csw.location.api.models.Connection.PekkoConnection
import csw.location.api.models.{ComponentId, ComponentType}
import csw.params.commands.{CommandName, Observe, Sequence, Setup}
import csw.params.core.generics.KeyType.{BooleanKey, ChoiceKey, IntKey, StringKey}
import csw.params.core.generics.{GChoiceKey, Key, Parameter}
import csw.params.core.models.{Choices, ObsId, Units}
import csw.params.events.{EventKey, EventName, ObserveEventNames}
import csw.prefix.models.Subsystem.Container
import csw.prefix.models.{Prefix, Subsystem}

object WFOSTestData {
  val eswSequencerPrefix: Prefix = Prefix(Subsystem.ESW, "WFOS_Science")

  val filterPositionEventName: EventName = EventName("Wheel1Position")
  val filterDarkKey: Key[Boolean]        = BooleanKey.make("dark")

  val redFilterChoices: Choices               = Choices.from("r'", "i'", "z'", "fused-silica")
  val redFilterCurrentPositionKey: GChoiceKey = ChoiceKey.make("current", redFilterChoices)
  val redFilterDemandPositionKey: GChoiceKey  = ChoiceKey.make("demand", redFilterChoices)
  val redFilterKey: GChoiceKey                = ChoiceKey.make("redFilter", Units.NoUnits, redFilterChoices)

  val blueFilterChoices: Choices               = Choices.from("u'", "g'", "fused-silica")
  val blueFilterCurrentPositionKey: GChoiceKey = ChoiceKey.make("current", blueFilterChoices)
  val blueFilterDemandPositionKey: GChoiceKey  = ChoiceKey.make("demand", blueFilterChoices)
  val blueFilterKey: GChoiceKey                = ChoiceKey.make("blueFilter", Units.NoUnits, blueFilterChoices)

  val wfosContainerConnection: PekkoConnection = PekkoConnection(
    ComponentId(Prefix(Container, "WfosContainer"), ComponentType.Container)
  )

  val wfosRedFilterPrefix: Prefix        = Prefix(Subsystem.WFOS, "red.filter")
  val wfosBlueFilterPrefix: Prefix       = Prefix(Subsystem.WFOS, "blue.filter")
  val wfosRedPositionEventKey: EventKey  = EventKey(wfosRedFilterPrefix, filterPositionEventName)
  val wfosBluePositionEventKey: EventKey = EventKey(wfosBlueFilterPrefix, filterPositionEventName)
  val wfosBlueDetectorPrefix: Prefix     = Prefix("WFOS.blue.detector")
  val wfosRedDetectorPrefix: Prefix      = Prefix("WFOS.red.detector")

  val obsId: Option[ObsId]          = Some(ObsId("2020A-001-123"))
  val directoryP: Parameter[String] = StringKey.make("directory").set("/tmp")

  val exposureIdStr: String                = "2020A-001-123-IRIS-BLU-SKY1-0002"
  val blueExposureIdP: Parameter[String]   = StringKey.make("blueExposureId").set(exposureIdStr)
  val blueIntegrationTimeP: Parameter[Int] = IntKey.make("blueIntegrationTime").set(2000)
  val blueNumRampsP: Parameter[Int]        = IntKey.make("blueNumRamps").set(2)

  val redExposureIdP: Parameter[String]   = StringKey.make("redExposureId").set(exposureIdStr)
  val redIntegrationTimeP: Parameter[Int] = IntKey.make("redIntegrationTime").set(2000)
  val redNumRampsP: Parameter[Int]        = IntKey.make("redNumRamps").set(2)

  private val wfosSourcePrefix: Prefix = Prefix("ESW.WFOS_Science")

  val setupAcquisition: Setup = Setup(wfosSourcePrefix, CommandName("setupAcquisition"), obsId).add(
    blueFilterKey.set("g'")
  )

  val setupObservation: Setup = Setup(wfosSourcePrefix, CommandName("setupObservation"), obsId).madd(
    blueFilterKey.set("fused-silica"),
    redFilterKey.set("z'")
  )

  val acquisitionExposure: Observe = Observe(wfosSourcePrefix, CommandName("acquisitionExposure"), obsId).madd(
    directoryP,
    blueExposureIdP,
    blueIntegrationTimeP,
    blueNumRampsP
  )

  val singleExposure: Observe = Observe(wfosSourcePrefix, CommandName("singleExposure"), obsId).madd(
    directoryP,
    blueExposureIdP,
    redExposureIdP,
    blueIntegrationTimeP,
    redIntegrationTimeP,
    blueNumRampsP,
    redNumRampsP
  )

  def detectorObsEvents(detectorPrefix: Prefix) = Set(
    EventKey(detectorPrefix, ObserveEventNames.ExposureStart),
    EventKey(detectorPrefix, ObserveEventNames.ExposureEnd),
    EventKey(detectorPrefix, ObserveEventNames.ExposureAborted),
    EventKey(detectorPrefix, ObserveEventNames.DataWriteEnd),
    EventKey(detectorPrefix, ObserveEventNames.DataWriteStart)
  )

  private val observationStartKey: EventKey  = EventKey(eswSequencerPrefix, ObserveEventNames.ObservationStart)
  private val observationEndKey: EventKey    = EventKey(eswSequencerPrefix, ObserveEventNames.ObservationEnd)
  private val presetStartKey: EventKey       = EventKey(eswSequencerPrefix, ObserveEventNames.PresetStart)
  private val presetEndKey: EventKey         = EventKey(eswSequencerPrefix, ObserveEventNames.PresetEnd)
  private val guidestarAcqStartKey: EventKey = EventKey(eswSequencerPrefix, ObserveEventNames.GuidestarAcqStart)
  private val guidestarAcqEndKey: EventKey   = EventKey(eswSequencerPrefix, ObserveEventNames.GuidestarAcqEnd)
  private val scitargetAcqStartKey: EventKey = EventKey(eswSequencerPrefix, ObserveEventNames.ScitargetAcqStart)
  private val scitargetAcqEndKey: EventKey   = EventKey(eswSequencerPrefix, ObserveEventNames.ScitargetAcqEnd)
  private val observeStartKey: EventKey      = EventKey(eswSequencerPrefix, ObserveEventNames.ObserveStart)
  private val observeEndKey: EventKey        = EventKey(eswSequencerPrefix, ObserveEventNames.ObserveEnd)

  val observeEventKeys = Set(
    observationStartKey,
    observationEndKey,
    presetStartKey,
    presetEndKey,
    guidestarAcqStartKey,
    guidestarAcqEndKey,
    scitargetAcqStartKey,
    scitargetAcqEndKey,
    observeStartKey,
    observeEndKey
  )

  private val blueExposureTypeKey = StringKey.make("blueExposureType")
  private val redExposureTypeKey  = StringKey.make("redExposureType")

  val eswObservationStart: Setup = Setup(eswSequencerPrefix, CommandName("observationStart"), obsId)
  val eswObservationEnd: Setup   = Setup(eswSequencerPrefix, CommandName("observationEnd"), obsId)
  val preset: Setup              = Setup(eswSequencerPrefix, CommandName("preset"), obsId).add(blueFilterKey.set("fused-silica"))

  val coarseAcquisition: Observe = Observe(eswSequencerPrefix, CommandName("coarseAcquisition"), obsId).madd(
    directoryP,
    blueExposureIdP,
    blueIntegrationTimeP,
    blueNumRampsP,
    blueExposureTypeKey.set("SKY")
  )

  val fineAcquisition: Observe = Observe(eswSequencerPrefix, CommandName("fineAcquisition"), obsId)

  val observe: Observe = Observe(eswSequencerPrefix, CommandName("observe"), obsId).madd(
    directoryP,
    blueExposureIdP,
    redExposureIdP,
    blueIntegrationTimeP,
    redIntegrationTimeP,
    blueNumRampsP,
    redNumRampsP,
    blueExposureTypeKey.set("SKY"),
    redExposureTypeKey.set("SKY")
  )

  val sequence: Sequence = Sequence(setupAcquisition, acquisitionExposure, setupObservation, singleExposure)

  val eswSequence: Sequence = Sequence(
    eswObservationStart,
    preset,
    coarseAcquisition,
    fineAcquisition,
    setupObservation,
    observe,
    eswObservationEnd
  )
}
