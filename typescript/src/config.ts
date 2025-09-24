export type Config = {
  port: number
  databaseUrl: string
}

export const config: Config = {
  port: Number(process.env.PORT) || 3000,
  databaseUrl: process.env.DATABASE_URL || 'database/database.db'
}
