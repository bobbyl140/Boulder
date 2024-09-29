# Boulder

Boulder is a plugin for Velocity that adds whitelist functionality compatible with Bedrock clients connecting through the Geyser proxy plugin.

Work is still in development to make this production-ready (see roadmap below), but will work for this purpose in its current state. 

> [!WARNING]  
> Please keep a separate list of names and UUIDs. When UUID support is integrated, your existing whitelist will need to be manually reconstructed because I have no good way to map Bedrock usernames to XUIDs. 

### Roadmap:
- [x] Initial whitelist support
- [ ] UUID support
  - [ ] Message online staff with `boulder.notify` permission the blocked UUID
  - [ ] Allow whitelisting a connection attempt by command
    - [ ] Suggest these commands from clickable chat components
    - [ ] Allow whitelisting by username, with UUID mapped from the connection attempt
  - [ ] Store whitelist entries as UUIDs