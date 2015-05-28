#! /bin/bash

if [ ! -d "dist" ]; then
  mkdir dist
fi

for ((INSTALL_CHANNEL = 1; INSTALL_CHANNEL <= 1; ++INSTALL_CHANNEL));
do
    ant ship -Dapp.channel=${INSTALL_CHANNEL};
done
