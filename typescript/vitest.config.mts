import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    isolate: true,
    restoreMocks: true,
    coverage: {
      provider: 'istanbul',
      include: ['src/**/*.ts'],
      reporter: ['text', 'html', 'lcov'],
    },
    reporters: ['verbose'],
  },
})
