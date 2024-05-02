[![Test](https://github.com/kid1412621/subspace/actions/workflows/test.yml/badge.svg)](https://github.com/kid1412621/subspace/actions/workflows/test.yml)

# Project setup

1. Add a dummy google-services.json to app directory:

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
    },
    {
      "client_info": {
        "mobilesdk_app_id": "app-id",
        "android_client_info": { "package_name": "me.nanova.subspace.debug" }
      },
      "api_key": [{"current_key": "key"}]
    }
  ]
}
EOF
```

2. Add a dummy keystore.properties to project root directory:

```bash
cat << EOF > keystore.properties
storeFile:keystore.jks
storePassword:fake-password
keyAlias:fake-key-alias
keyPassword:fake-password
EOF
```
