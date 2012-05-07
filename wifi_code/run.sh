#!/bin/sh
ruby wifi_code $@ | lpr -PS2B &
