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
    switch (true) {
      case (this.command === 'echo'):
        return colors.green(`${time} <${this.username}> (${this.command}): ${this.contents}`)
      case (this.command === 'broadcast'):
        return colors.america(`${time}: <${this.username}> (all): ${this.contents}`)
      case (this.command === 'users') :
        return colors.white(`${time}: currently connected users: \n${this.contents}`)
      case (this.command === 'connect') :
        return colors.blue(`${time}: <${this.username}> has connected`)
      case (this.command === 'disconnect'):
        return colors.red(`${time}: <${this.username}> has disconnected`)
      case (this.command[0] === '@'):
        return colors.cyan(`${time}: <${this.username}> (whisper): ${this.contents}`)
      default:
        return '404 error. So embarrassing'
    }
  }

}
