import { FastifyPluginAsync } from 'fastify'
import { AppDependencies } from './app-dependencies'
import { usersRoute } from './api/UsersRoute'
import { loginRoutes } from './api/LoginRoute'

export const app: FastifyPluginAsync<AppDependencies> = async (fastify, deps) => {
  await fastify.register(usersRoute, deps)
  await fastify.register(loginRoutes, deps)
}
