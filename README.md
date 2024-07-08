# H.261 video Encoder written in Java
<br/>
<br/>

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
- Build: mvn compile
- Start H261 Encoder: mvn exec:java -Dexec.mainClass=org.ijntema.eric.encoders.H261Encoder
- Install ffmpeg
- Open stream: ffplay udp://127.0.0.1:55555 -loglevel debug 

## Remarks
- Made to understand how the algorithm works
- Not intended for production use
- Use this project at your own risk
- By updating the Quant (1 - 31) in H261Constants the amount of compression can be adjusted

## Known issues
- Can't handle completely black colors
- VLC for Huffman are not working
