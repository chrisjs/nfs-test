Overview
--
Quick POC to setup NFS on CF, read and write files to the NFS mount by launching and killing off a CF app.

CF Setup:
--
* activate service offering:
  * `cf enable-service-access nfs`

* verify:
  * `cf marketplace | grep nfs`

    -> nfs		   Existing	Existing NFSv3 volumes (see: https://code.cloudfoundry.org/nfs-volume-release/)

Build and run:
---
* `mvn clean package ; cf push nfstest -p target/nfs-0.0.1-SNAPSHOT.jar`

* `cf create-service nfs Existing nfs_test_service -c '{"share": "10.194.2.6/export"}'`

* `cf bind-service nfstest nfs_test_service -c '{"uid":"65534","gid":"65534"}'`
  * 65534 is the gid & uid to use in this setup

* `cf restage nfstest`

* `cf env nfstest`
  * `container_dir` is your mount point and translates to boot property: `vcap.services.nfs_test_service.volume_mounts[0].container_dir`, ie:

```
{
 "VCAP_SERVICES": {
  "nfs": [
   {
    "credentials": {},
    "label": "nfs",
    "name": "nfs_test_service",
    "plan": "Existing",
    "provider": null,
    "syslog_drain_url": null,
    "tags": [
     "nfs"
    ],
    "volume_mounts": [
     {
      "container_dir": "/var/vcap/data/17c7024c-a276-43db-9c81-1cd36998f89e",
      "device_type": "shared",
      "mode": "rw"
     }
    ]
   }
  ]
 }
}
```

* check `cf logs nfstest --recent`:

```
OUT **** NFS mount is: /var/vcap/data/17c7024c-a276-43db-9c81-1cd36998f89e
OUT **** File: /var/vcap/data/17c7024c-a276-43db-9c81-1cd36998f89e/hi.nfo does not exist, creating new with contents of: HI-NFS
OUT **** Kill and restart the application to simulate new containers accessing shared FS
```

* restage: `cf restage nfstest`

* check `cf logs nfstest --recent`:

```
OUT **** NFS mount is: /var/vcap/data/17c7024c-a276-43db-9c81-1cd36998f89e
OUT **** File exists on storage: /var/vcap/data/17c7024c-a276-43db-9c81-1cd36998f89e/hi.nfo
OUT **** Contents match previously written file. Deleting file: /var/vcap/data/17c7024c-a276-43db-9c81-1cd36998f89e/hi.nfo
OUT **** Kill and restart the application to simulate new containers accessing shared FS
```

* do restage as many times as you would like

* verification of files can also be done on the NFS server itself

Cleanup:
---
* unbind app from nfs service:
  * cf unbind-service nfstest nfs_test_service

* delete nfs service (the referenced path above will no longer be bound to apps on recreate):
  * cf delete-service nfs_test_service
