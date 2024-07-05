# H.261 standard compliant video Encoder written in Java
<br/>
<br/>

![encoder input and output](/src/main/resources/h261.png) <br/>
On the left the RGB input frame, on the right the H.261 encoded and then decoded output frame

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
- By updating the Quant in H261Constants the amount of compression can be adjusted
