# H.261 standard compliant video Encoder written in Java

## Application structure
- FrameGenerator --[RGB frames]--> H261Encoder --[H261 packets]--> UdpStreamer --[UDP datagrams]--> 127.0.0.1:55555

## Features
- Generates RGB 352 x 288 frames
- Applies H.261 compression algorithm
- Serves data in UDP datagrams
- Currently only supports I-frames

## Future
- P-frames support
- Motion vectors support

## How to start
- Install ffmpeg
- Start H261 Encoder
- Open stream: 'ffplay udp://127.0.0.1:55555 -loglevel debug' 

## Remarks
- Made to understand how the algorithm works
- Not intended for production use
- Use this project at your own risk
- Create test stream: 'ffmpeg -stream_loop 10 -re -i "page18-movie-4.3gp" -f h261 "udp://127.0.0.1:55555"'
