import type { FastifyPluginAsync } from 'fastify/types/plugin'
import type { CreateUser } from '../../domain/commands/CreateUser'
import { AppDependencies } from '../app-dependencies'
import { UsernameAlreadyInUseError, UserRegistrationHandler } from '../../application/handlers/UserRegistrationHandler'
import { PostCreationHandler, UserNotFound } from '../../application/handlers/PostCreationHandler'
import { TimelineQueryHandler, TimelineUserNotFound } from '../../application/handlers/TimelineQueryHandler'
import { InvalidPost } from '../../domain/entities/Post'

export const usersRoute: FastifyPluginAsync<AppDependencies> = async (fastify, deps) => {
  const userRegistrationHandler = new UserRegistrationHandler(
    deps.usernameService(),
    deps.userRepository(),
    deps.uuidWrapper()
  )
  const postCreationHandler = new PostCreationHandler(deps.uuidWrapper(), deps.userRepository())
  const timelineQueryHandler = new TimelineQueryHandler(deps.userRepository())

  fastify.post('/users', async (request, response) => {
    try {
      const userCreated = await userRegistrationHandler.handle(request.body as CreateUser)
      response.status(201).send(userCreated)
    } catch (error) {
      if (error instanceof UsernameAlreadyInUseError) {
        response.status(400).send({ message: 'Username already in use' })
      } else {
        console.log(error)
        response.status(500).send({ message: 'Internal server error' })
      }
    }
  })

  fastify.post('/users/:userId/timeline', async (request, response) => {
    try {
      const params = request.params as { userId: string }
      const body = request.body as { text?: string }

      if (!deps.uuidWrapper().isValidUuid(params.userId)) {
        response.status(404).send({ message: 'User does not exist.' })
        return
      }

      if (!body || typeof body.text !== 'string' || body.text.trim() === '') {
        response.status(400).send({ message: 'Text is required.' })
        return
      }

      const postCreated = await postCreationHandler.handle({ userId: params.userId, text: body.text })

      response.status(201).send(postCreated)
    } catch (error) {
      if (error instanceof UserNotFound) {
        response.status(404).send({ message: 'User does not exist.' })
      } else if (error instanceof InvalidPost) {
        response.status(400).send({ message: 'Post contains inappropriate language' })
      } else {
        console.log(error)
        response.status(500).send({ message: 'Internal server error' })
      }
    }
  })

  fastify.get('/users/:userId/timeline', async (request, response) => {
    try {
      const params = request.params as { userId: string }

      if (!deps.uuidWrapper().isValidUuid(params.userId)) {
        response.status(404).send({ message: 'User does not exist.' })
        return
      }

      const timeline = await timelineQueryHandler.handle({ userId: params.userId })
      response.status(200).send(timeline)
    } catch (error) {
      if (error instanceof TimelineUserNotFound) {
        response.status(404).send({ message: 'User does not exist.' })
      } else {
        console.log(error)
        response.status(500).send({ message: 'Internal server error' })
      }
    }
  })
}
