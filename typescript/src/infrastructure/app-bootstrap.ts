import Fastify from 'fastify'
import fastifyCors from '@fastify/cors'
import fastifySwagger from '@fastify/swagger'
import fastifySwaggerUi from '@fastify/swagger-ui'
import { app } from './app'
import { AppDependencies } from './app-dependencies'
import * as fs from 'node:fs'
import type { OpenAPIV2, OpenAPIV3 } from 'openapi-types'
import yaml from 'js-yaml'
import fastifyGracefulShutdown from 'fastify-graceful-shutdown'
import sensible from '@fastify/sensible'

export async function bootstrapApp(dependencies: AppDependencies) {
  const fastify = Fastify({
    pluginTimeout: 30000,
  })

  console.log('Loading swagger documentation...')
  const swaggerDocument = yaml.load(fs.readFileSync('./APIs.yaml', 'utf8')) as OpenAPIV2.Document | OpenAPIV3.Document
  await fastify.register(fastifyCors, { origin: true })
  await fastify.register(fastifySwagger, {
    mode: 'static',
    specification: { document: swaggerDocument },
  })
  await fastify.register(fastifySwaggerUi, {
    routePrefix: '/docs',
    staticCSP: true,
    uiConfig: { docExpansion: 'full' },
  })
  await fastify.register(sensible)
  await fastify.register(fastifyGracefulShutdown)

  console.log('Starting the application...')
  await fastify.register(app, dependencies)

  return {
    async start() {
      return fastify.listen({ port: dependencies.config.port, host: '0.0.0.0' })
    },
    async stop() {
      return fastify.close()
    },
  }
}
