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
    if (this.command === 'echo') {
      return colors.green(`${time} <${this.username}> (${this.command}): ${this.contents}`)
    } else if (this.command === 'broadcast') {
      return colors.america(`${time} <${this.username}> (all): ${this.contents}`)
    } else if (this.command === 'users') {
      return colors.white(`${time}: currently connected users: \n${this.contents}`)
    } else if (this.command === 'connect') {
      return colors.blue(`${time}: <${this.username}> has connected`)
    } else if (this.command === 'disconnect') {
      return colors.red(`${time}: <${this.username}> has disconnected`)
    } else {
      return colors.cyan(`${time}: <${this.username}> (whisper): ${this.contents}`)
    }
  }
}
