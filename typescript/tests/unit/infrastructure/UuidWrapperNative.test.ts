import { describe, expect, it } from 'vitest'
import { UUID_PATTERN } from '../../../src/application/wrapper/UuidWrapper'
import { UuidWrapperNative } from '../../../src/infrastructure/wrapper/UuidWrapperNative'

describe('UuidWrapperNative', () => {
  const uuidWrapper = new UuidWrapperNative()

  describe('generateUuid', () => {
    it('should generate a valid UUID v4', () => {
      const uuid = uuidWrapper.generateUuid()

      expect(uuid).toMatch(UUID_PATTERN)
      expect(typeof uuid).toBe('string')
      expect(uuid.length).toBe(36)
    })

    it('should generate unique UUIDs on multiple calls', () => {
      const uuid1 = uuidWrapper.generateUuid()
      const uuid2 = uuidWrapper.generateUuid()

      expect(uuid1).not.toBe(uuid2)
    })

    it('should generate UUIDs in correct format', () => {
      const uuid = uuidWrapper.generateUuid()

      // UUID v4 format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
      const parts = uuid.split('-')
      expect(parts).toHaveLength(5)
      expect(parts[0]).toHaveLength(8)
      expect(parts[1]).toHaveLength(4)
      expect(parts[2]).toHaveLength(4)
      expect(parts[2].charAt(0)).toBe('4') // UUID version 4
      expect(parts[3]).toHaveLength(4)
      expect(parts[4]).toHaveLength(12)
    })
  })

  describe('isValidUuid', () => {
    it('should return true for valid UUID v4', () => {
      const validUuid = '550e8400-e29b-41d4-a716-446655440000'

      expect(uuidWrapper.isValidUuid(validUuid)).toBe(true)
    })

    it('should return true for generated UUID', () => {
      const generatedUuid = uuidWrapper.generateUuid()

      expect(uuidWrapper.isValidUuid(generatedUuid)).toBe(true)
    })

    it.each([
      ['empty string', ''],
      ['non-UUID string', 'not-a-uuid'],
      ['incomplete UUID', '550e8400-e29b-41d4-a716'],
      ['UUID with extra parts', '550e8400-e29b-41d4-a716-446655440000-extra'],
      ['UUID without hyphens', '550e8400e29b41d4a716446655440000'],
      ['UUID with invalid character g', '550e8400-e29b-41d4-a716-44665544000g'],
      ['UUID with invalid character z', '550e8400-e29b-41d4-z716-446655440000'],
    ])('should return false for invalid UUID format: %s', (_, invalidUuid) => {
      expect(uuidWrapper.isValidUuid(invalidUuid)).toBe(false)
    })

    it('should return false for null and undefined', () => {
      expect(uuidWrapper.isValidUuid(null as any)).toBe(false)
      expect(uuidWrapper.isValidUuid(undefined as any)).toBe(false)
    })

    it.each([[123], [{}], [[]]])('should return false for non-string value: %j', (value) => {
      expect(uuidWrapper.isValidUuid(value as any)).toBe(false)
    })

    it.each([
      ['UUID v1', '550e8400-e29b-11d4-a716-446655440000'],
      ['UUID v3', '550e8400-e29b-31d4-a716-446655440000'],
      ['UUID v5', '550e8400-e29b-51d4-a716-446655440000'],
    ])('should work with %s if it matches the pattern', (_, uuid) => {
      const isValid = uuidWrapper.isValidUuid(uuid)

      expect(isValid).toBe(false)
    })
  })
})
