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

|             | qBittorrent(<5.0.0) | qBittorrent(>=5.0.0) | Transmission |
|-------------|:-------------------:|:--------------------:|:------------:|
| all         |                     |                      |              |
| active      |                     |                      |              |
| downloading |                     |                      |              |
| seeding     |                     |                      |              |
| completed   |                     |                      |              |
| paused      |                     |         N/A          |              |
| stopped     |         N/A         |                      |              |
| stalled     |                     |                      |              |
| errored     |                     |                      |     N/A      |
