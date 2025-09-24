import { AppDependencies } from '../app-dependencies'
import { FastifyPluginAsync } from 'fastify/types/plugin'
import { LoginHandler, type LoginUser } from '../../application/handlers/LoginHandler'

export const loginRoutes: FastifyPluginAsync<AppDependencies> = async (fastify, deps) => {
  const loginHandler = new LoginHandler(deps.userRepository())

  fastify.post('/login', async (request, response) => {
    try {
      const loggedInUser = await loginHandler.handle(request.body as LoginUser)
      if (loggedInUser === null) {
        response.status(401).send({ message: 'Invalid credentials.' })
        return
      }

      response.status(200).send(loggedInUser)
    } catch (error) {
      console.log(error)
      response.status(500).send({ message: 'Internal server error' })
    }
  })
}
