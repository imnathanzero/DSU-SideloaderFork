#!/system/bin/sh

# unmount sdcard
umount_sd=%UNMOUNT_SD
SDCARD=""
if [ "$umount_sd" = "true" ]; then
  SDCARD=$(sm list-volumes | grep -v null | grep public)
  if [ -z "$SDCARD" ]; then
    echo "Unmount SD card option is enabled, but there is no sdcard detected, skipping.."
    umount_sd=false
  else
    echo "Unmount SD card option is enabled, sdcard will be ejected temporary, preventing DSU allocation on SD.."
    sm unmount "$SDCARD"
  fi
fi

# required prop
setprop persist.sys.fflag.override.settings_dynamic_system true

# invoke DSU activity
%ACTION_INSTALL

echo "DSU installation activity has been started!"

if [ "$umount_sd" = "true" ]; then
  echo "Remounting sdcard in 60 secs.."
  # Run the delay in a background subshell: command substitution would block here
  # for the full 60 seconds and remount before nohup ever saw it.
  nohup sh -c 'sleep 60 && sm mount "$1"' _ "$SDCARD" >/dev/null 2>&1 &
fi
