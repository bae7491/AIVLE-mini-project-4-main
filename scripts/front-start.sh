#!/bin/bash
cd /home/ec2-user/front
npm install
npm run build
pm2 restart next || pm2 start npm --name next -- start
