# H.261 standard compliant video Encoder written in Java

# Application structure
- FrameGenerator --[RGB frames]--> H261Encoder --[H261 packets]--> UdpStreamer --[UDP datagrams]--> 127.0.0.1:55555

## Features
- Generates RGB 352 x 288 frames
- Applies H.261 compression algorithm
- Serves data in UDP datagrams
- Currently only supports I-frames

## How to start
- install ffmpeg
- start H261 Encoder
- open stream with 'ffplay udp://127.0.0.1:55555 -loglevel debug' 
