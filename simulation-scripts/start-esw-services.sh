#!/usr/bin/env bash
ROOT="$(
    cd "$(dirname "$0")" >/dev/null 2>&1 || exit
    pwd -P
)"
ESW_VERSION=0.4.0
SEQ_SCRIPT_VERSION=625e16b
cs launch esw-services:$ESW_VERSION -- start-eng-ui-services  --scripts-version $SEQ_SCRIPT_VERSION --obs-mode-config $ROOT/../sample-configs/smObsModeConfig.conf
