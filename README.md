# ExplosiveMobs

A Minecraft Bukkit plugin which let mobs explode once they die

You can configure the plugin to just have explosive mobs if they are spawned using the */spawnexplosivemob* command or everytime a mob dies.

It's even possible to set per mob configurations. For example you only want to have explosive sheeps, but any other mob should just die on death without any explosion.


## Installation

Currently there is no jar file available. You have to build the plugin yourself.

See the *Build* step bellow for more details.


## Build

You can build the project in the following 2 steps:

 * Check out the repository
 * Build the jar file using maven: *mvn clean package*

**Note:** JDK 1.7 and Maven is required to build the project!


## Configuration

See [config.yml](src/main/resources/config.yml)


## Permissions

ExplosiveMobs knows the following permissions:

  * explosivemobs.spawn - Allows the player to spawn an explosive mob
  * explosivemobs.spawn.target - Allows the player to spawn an explosive mob in front of another player


## Commands

You can use the */spawnexplosivemob* command to spawn an explosive mob.

Just execute it without any arguments to see the command usage.
