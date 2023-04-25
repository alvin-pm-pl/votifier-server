# votifier-server
A simple standalone server which implements both v1 and v2 of the Votifier protocol.

# Why?
PHP sucks. No other reasons.

# Pre-requisites
- Java 19 or above.
- 1GB of RAM or more.
- Your brain.

# Setup
1. Download the latest release from the [releases page](https://github.com/alvin0319/votifier-server/releases/latest).
2. Run following:
```shell
java -jar votifier-server*.jar
```
3. Edit `config.json` with your preferred settings. (config.json is a file for setting up a socket server which communicates with the PocketMine server)
4. Edit `config.yml` if you need. (This is a configuration for Votifier, you don't need to edit it unless you want to change the port)
5. Register your server to voting sites with the IP and port you set in `config.yml`, with the content of `rsa/public.key`.
6. Confirm vote is working by checking the console.
7. Set up [PocketMine plugin](https://github.com/alvin0319/VotifierAPI).
8. Enjoy.

# License
This project is licensed under the MIT license. See [LICENSE](./LICENSE) for more details.