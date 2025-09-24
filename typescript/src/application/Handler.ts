export type Handler<I, O> = {
  handle: (input: I) => Promise<O>
}
