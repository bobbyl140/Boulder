# Boulder

Boulder is a plugin for Velocity that adds whitelist functionality compatible with Bedrock clients connecting through the Geyser proxy plugin.

To manipulate the whitelist, either add properly formatted UUIDs to the `whitelist.txt` file in the plugin's folder, or run `/whitelist add <UUID>`. To remove, change `add` to `remove` in the aforementioned command.

In order to run `/whitelist` you need the permission node `boulder.use`.

To get notifications when someone not whitelisted tries to join (which can be clicked to whitelist them), give the permission node `boulder.notify`.

### Roadmap:
- [x] Initial whitelist support
- [x] UUID support
  - [x] Message online staff with `boulder.notify` (and console) the blocked UUID
  - [x] Allow whitelisting a connection attempt by command
    - [x] Suggest these commands from clickable chat components
  - [x] Store whitelist entries as UUIDs