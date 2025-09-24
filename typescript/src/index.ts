import { bootstrapApp } from './infrastructure/app-bootstrap'
import { AppDependencies } from './infrastructure/app-dependencies'
import { config } from './config'

bootstrapApp(new AppDependencies(config))
  .then((app) => app.start())
  .then(() => {
    console.log(`Application started on port ${config.port}`)
  })
  .catch((err) => {
    console.error(err)
    process.exit(1)
  })
