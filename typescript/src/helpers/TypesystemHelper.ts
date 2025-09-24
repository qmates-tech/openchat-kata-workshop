type IfAny<T, Y, N> = 0 extends 1 & T ? Y : N
type PickKeysByValue<T, V> = {
  [K in keyof T]: IfAny<T[K], never, T[K] extends V ? K : never>
}[keyof T]
type OmitProperties<T, P> = Omit<T, PickKeysByValue<T, P>>

/**
 * Extracts only the non-function properties from a type
 * @example
 * type User = { name: string; age: number; save: () => void }
 * type UserAttrs = GetAttributes<User> // { name: string; age: number }
 */
export type GetAttributes<T> = OmitProperties<T, Function>
