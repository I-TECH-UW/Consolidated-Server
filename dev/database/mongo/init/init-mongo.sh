mongo -- "$MONGO_INITDB_DATABASE" <<EOF
    var rootUser = '$MONGO_INITDB_ROOT_USERNAME';
    var rootPassword = '$MONGO_INITDB_ROOT_PASSWORD';
    var admin = db.getSiblingDB('admin');
    admin.auth(rootUser, rootPassword);

    var user = '$(cat "$MONGO_INITDB_USERNAME_FILE")';
    var passwd = '$(cat "$MONGO_INITDB_PASSWORD_FILE")';
    db = db.getSiblingDB('openhim');
    db.createUser({user: user, pwd: passwd, roles: ["readWrite"]});

EOF