#!/bin/sh
ruby badge $@ | lpr -PS2B &
