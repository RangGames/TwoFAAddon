# TwoFAAddon for Skript

## Introduction

TwoFAAddon is a Skript addon that allows you to easily add Two-Factor Authentication (2FA) to your Minecraft server. This addon provides a set of Skript syntaxes to manage 2FA for players, enhancing the security of their accounts. It uses the Time-Based One-Time Password (TOTP) algorithm and can be used with any standard authenticator app like Google Authenticator, Authy, etc.

## Features

  * **Easy 2FA Setup:** Register and set up 2FA for players with a simple Skript command.
  * **In-game QR Codes:** Display QR codes on in-game maps for easy scanning with an authenticator app.
  * **Code Verification:** Secure your login process or other sensitive actions by verifying 2FA codes.
  * **Flexible and Customizable:** Use Skript to integrate 2FA into any part of your server's logic.
  * **Secret Key and URI Access:** Get direct access to the secret key and TOTP URI for custom implementations.

## Dependencies

  * **Spigot or a fork of it (e.g., Paper, Purpur)**
  * **Skript**

## Skript Syntaxes

Here are the Skript syntaxes provided by this addon:

### Effects

  * **Register a new 2FA key:**

    ```skript
    register 2fa for id "player_identifier"
    register 2fa for id "player_identifier" with label "YourServerName"
    ```

  * **Remove a 2FA key:**

    ```skript
    remove 2fa for id "player_identifier"
    ```

### Expressions

  * **Get the current 2FA code:**

    ```skript
    2fa code of "player_identifier"
    ```

  * **Get a map with the QR code:**

    ```skript
    2fa qr map of "player_identifier"
    2fa qr map of "player_identifier" with label "YourServerName"
    ```

  * **Get the 2FA secret key:**

    ```skript
    2fa secret of "player_identifier"
    ```

  * **Get the 2FA TOTP URI:**

    ```skript
    2fa uri of "player_identifier"
    ```

### Conditions

  * **Verify a 2FA code:**
    ```skript
    2fa code "123456" is valid for id "player_identifier"
    2fa code "123456" is not valid for id "player_identifier"
    ```

## Usage Examples

Here are some examples of how you can use this addon in your Skripts.

### Setting up 2FA for a player

```skript
command /2fa-setup:
  trigger:
    set {_identifier} to player's uuid
    if 2fa secret of {_identifier} is not set:
      register 2fa for id {_identifier} with label "My Awesome Server"
      give player 2fa qr map of {_identifier} with label "My Awesome Server"
      send "Scan the QR code in the map with your authenticator app!" to player
    else:
      send "You have already set up 2FA!" to player
```

### Requiring 2FA on login

```skript
on join:
  if 2fa secret of player's uuid is set:
    # Logic to prompt the player for their 2FA code
    # For example, you could put them in a separate world or use a chat-based input
```

### Verifying a 2FA code from a command

```skript
command /2fa-verify <text>:
  trigger:
    set {_identifier} to player's uuid
    if 2fa code arg-1 is valid for id {_identifier}:
      send "2FA successful!"
      # Grant access to whatever you are protecting
    else:
      send "Invalid 2FA code!"
```

## Building from Source

To build this project from source, you will need to have [Maven](https://maven.apache.org/) installed.

1.  Clone the repository:
    ```bash
    git clone https://github.com/ranggames/twofaaddon.git
    ```
2.  Navigate to the project directory:
    ```bash
    cd twofaaddon
    ```
3.  Build the project with Maven:
    ```bash
    mvn clean package
    ```

The compiled JAR file will be in the `target` directory.
