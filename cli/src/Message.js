import colors from 'colors'

export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents }) {
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    let time = new Date()
    switch (this.command) {
      case ('echo'):
        return colors.green(`${time} <${this.username}> (${this.command}): ${this.contents}`)
      case ('broadcast'):
        return colors.america(`${time}: <${this.username}> (all): ${this.contents}`)
      case ('users') :
        return colors.white(`${time}: currently connected users: \n${this.contents}`)
      case ('connect') :
        return colors.blue(`${time}: <${this.username}> has connected`)
      case ('disconnect'):
        return colors.red(`${time}: <${this.username}> has disconnected`)
      default:
        return colors.cyan(`${time}: <${this.username}> (whisper): ${this.contents}`)
    }
  }

}
