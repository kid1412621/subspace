[![Test](https://github.com/kid1412621/subspace/actions/workflows/test.yml/badge.svg)](https://github.com/kid1412621/subspace/actions/workflows/test.yml)

# Project setup

## Local Dev

TBD

## Build Release

1. Provide an env var: `export PROD_RELEASE=true`;

2. Add `google-services.json` to app directory:

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

3. Create `keystore.jks` and `keystore.properties` to project root directory:

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
