[![Test](https://github.com/kid1412621/subspace/actions/workflows/test.yml/badge.svg)](https://github.com/kid1412621/subspace/actions/workflows/test.yml)

[![Build, Release](https://github.com/kid1412621/subspace/actions/workflows/release.yml/badge.svg)](https://github.com/kid1412621/subspace/actions/workflows/release.yml)

Subspace is an android app which is able to
remote
control [BitTorrent clients](https://en.wikipedia.org/wiki/Glossary_of_BitTorrent_terms#Client)
includes [qBittorrent](https://www.qbittorrent.org/) (
wip), [Transmission](https://transmissionbt.com/) (planned).

# Project setup

## Local Dev

TBD

## Build Release

1. Add `google-services.json` to app directory:

    ```bash
    cat << EOF > app/google-services.json
    {
      "project_info": {
        "project_number": "0",
        "project_id": "id",
        "storage_bucket": "bucket"
      },
      "client": [
        {
          "client_info": {
            "mobilesdk_app_id": "app-id",
            "android_client_info": { "package_name": "me.nanova.subspace" }
          },
          "api_key": [{"current_key": "key"}]
        }
      ]
    }
    EOF
    ```

2. Create `keystore.jks` and `keystore.properties` to project root directory:

   keystore.jks:

    ```bash
    keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias 
    ```

   keystore.properties:

    ```bash
    cat << EOF > keystore.properties
    storeFile:keystore.jks
    storePassword:fake-password
    keyAlias:my-alias
    keyPassword:fake-password
    EOF
    ```

# Torrent state mapping

| State       | qBittorrent (<5.0.0)                 | qBittorrent (>=5.0.0)                | Transmission                   |
|-------------|:------------------------------------:|:------------------------------------:|:------------------------------:|
| all         | `all`                                | `all`                                | `all`                          |
| active      | `downloading`, `uploading`           | `downloading`, `uploading`           | `4` (Download), `6` (Seed)     |
| downloading | `downloading`, `metaDL`, `forcedDL`  | `downloading`, `metaDL`, `forcedDL`  | `4` (`TR_STATUS_DOWNLOAD`)     |
| seeding     | `uploading`, `forcedUP`              | `uploading`, `forcedUP`              | `6` (`TR_STATUS_SEED`)         |
| completed   | `pausedUP`                           | `stoppedUP`                          | `0` (`STOPPED` with 100%)      |
| paused      | `pausedDL`                           | N/A (renamed to `stoppedDL`)         | `0` (`STOPPED` with <100%)     |
| stopped     | N/A (was `pausedDL`)                 | `stoppedDL`                          | `0` (`TR_STATUS_STOPPED`)      |
| stalled     | `stalledDL`, `stalledUP`             | `stalledDL`, `stalledUP`             | `4`, `6` (0 transfer speed)    |
| errored     | `error`, `missingFiles`              | `error`, `missingFiles`              | `error > 0`                    |
| queued      | `queuedDL`, `queuedUP`               | `queuedDL`, `queuedUP`               | `1`, `3`, `5` (Queued states)  |
| checking    | `checkingDL`, `checkingUP`           | `checkingDL`, `checkingUP`           | `2` (`TR_STATUS_CHECK`)        |
