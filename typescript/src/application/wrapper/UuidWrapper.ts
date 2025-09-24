export const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export type Uuid = `${string}-${string}-${string}-${string}-${string}`

export interface UuidWrapper {
  isValidUuid(value: string): value is Uuid
  generateUuid(): Uuid
}
