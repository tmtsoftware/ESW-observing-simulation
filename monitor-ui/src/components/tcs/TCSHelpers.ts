import {
  altAzCoordKey,
  doubleKey,
  eqCoordKey,
  EventKey,
  EventName,
  Prefix,
  Units
} from '@tmtsoftware/esw-ts'

export type AngleP = {
  current: string | number | undefined
  target: string | number | undefined
  error: string | number | undefined
}

export type EqCoordP = {
  current?: string
  target?: string
}

export const mountPositionEventKey = new EventKey(
  Prefix.fromString('TCS.MCSAssembly'),
  new EventName('MountPosition')
)

export const currentPositionEventKey = new EventKey(
  Prefix.fromString('TCS.ENCAssembly'),
  new EventName('CurrentPosition')
)

export const currentAltAzCoordKey = altAzCoordKey('current')
export const demandAltAzCoordKey = altAzCoordKey('demand')
export const baseCurrentKey = doubleKey('baseCurrent')
export const capCurrentKey = doubleKey('capCurrent')
export const baseDemandKey = doubleKey('baseDemand')
export const capDemandKey = doubleKey('capDemand')
export const currentPosKey = eqCoordKey('currentPos')
export const demandPosKey = eqCoordKey('demandPos')
export const siderealTimeKey = doubleKey('siderealTime', Units.hour)
export const currentHourAngleKey = doubleKey('currentHourAngle', Units.degree)
export const demandHourAngleKey = doubleKey('demandHourAngle', Units.degree)

export const round = (num?: number) =>
  num ? Math.floor(Math.abs(num) * 100) / 100 : num
