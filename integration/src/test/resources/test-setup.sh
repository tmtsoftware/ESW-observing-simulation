#!/usr/bin/env bash

if [[ "$OSTYPE" == "darwin"* ]]; then
  V_SLICE_ZIP=https://github.com/tmtsoftware/tcs-vslice-0.4/releases/download/v0.7/tcs-vslice-0.7.zip

else
  V_SLICE_ZIP=https://github.com/tmtsoftware/tcs-vslice-0.4/releases/download/v0.7/tcs-vslice-07-Ubuntu-20.04.zip

fi

echo "Setting INTERFACE_NAME tcs assemblies:"$INTERFACE_NAME

export TPK_USE_FAKE_SYSTEM_CLOCK=1

ROOT="$(
    cd "$(dirname "$0")" >/dev/null 2>&1 || exit
    pwd -P
)"

CONTAINER_CONF_PATH=$1


cd $ROOT/../../../target

if [ -d "tcs-vslice-04" ]
then
    echo "starting tcs-vslice container"
else
   echo "downloading.." $V_SLICE_ZIP
   curl -L $V_SLICE_ZIP -o tcs-vslice-04.zip
   unzip -o tcs-vslice-04.zip
fi

tcs-vslice-04/bin/tcs-deploy --local $CONTAINER_CONF_PATH
