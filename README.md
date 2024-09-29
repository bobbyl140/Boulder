# Boulder

Boulder is a plugin for Velocity that adds whitelist functionality compatible with Bedrock clients connecting through the Geyser proxy plugin.

Work is still in development to make this production-ready (see roadmap below), but will work for this purpose in its current state. 

> [!WARNING]  
> Please keep a separate list of names and UUIDs. When UUID support is integrated, your existing whitelist will need to be manually reconstructed because I have no good way to map Bedrock usernames to XUIDs. 

### Roadmap:
- [x] Initial whitelist support
- [x] UUID support
  - [x] Message online staff with `boulder.notify` (and console) the blocked UUID
  - [x] Allow whitelisting a connection attempt by command
    - [x] Suggest these commands from clickable chat components
  - [x] Store whitelist entries as UUIDs