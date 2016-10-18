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
      return `${time} <${this.username}> (${this.command}): ${this.contents}`
    } else if (this.command === 'broadcast') {
      return `${time} <${this.username}> (${this.command}): ${this.contents}`
    } else if (this.command === 'users') {
      return `${this.contents}`
    }
  }
}
