import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let host
let port
let previousCommand

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    host = args.host
    port = args.port
    previousCommand = null

    // if a host and port are specified
    if (host && port) {
      server = connect({ host: host, port: port }, () => {
        server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
        callback()
      })
    } else if (host || port) {  // if only one optional argument is given, but not both
      this.log(`You must provide a host AND a port`)
      callback()
    } else {
      server = connect({ host: 'localhost', port: 8080 }, () => {
        server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
        callback()
      })
    }

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[^, ]+/g)
    const contents = rest.join(' ')

    switch (true) {
      case (command === 'disconnect'):
        server.end(new Message({ username, command }).toJSON() + '\n')
        break
      case (command === 'echo'):
        previousCommand = command
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        break
      case (command === 'broadcast'):
        previousCommand = command
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        break
      case (command === 'users'):
        previousCommand = command
        server.write(new Message({ username, command }).toJSON() + '\n')
        break
      case (command[0] === '@'):
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        break
      case (previousCommand !== null):
        server.write(new Message({ username, 'command': previousCommand, 'contents': command + contents }).toJSON() + '\n')
        break
      default:
        this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
