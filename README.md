# Steps
- generate RGB 352 x 288 frames
- apply H.261 compression algorithm
    - preprocessing
      - map JPEGs into two dimensional YCbCr 4:2:0 pixel arrays
      - divide pixel arrays into Groups Of Block (GOB's)
      - divide GOB's into Macro Blocks (MB's)
    - decide how to encode MB (don't encode/encode I-frame compression/encode P-frame compression)
    - algorithm
      - if P-frame 
        - calculate difference with previous frame 
        - calculate motion vector
      - DCT
      - quantize
      - zigzag
      - run length encoding
      - huffman encoding
- serving data
  - put H.261 block coefficients into H.261 packets
  - put H.261 packets into RTP packets
  - serve RTP packets in UDP datagrams

# Notes
- keep constant bitrate according to H.261 standard
- serve the data in a loop
- log network debug information

# Nice to haves
- print bitrate in every frame
- print compression ratio in every frame
- print network debug information in every frame

# Future
- Look into RTCP and RTSP
- Look into MPEG-TS
- H.264 support

# ffmpeg
ffmpeg -stream_loop 10 -re -i "echo-hereweare.mp4" -f mpegts "udp://localhost:9999"
ffmpeg -stream_loop 10 -re -i "echo-hereweare.mp4" -f h264 "udp://localhost:9999"
ffmpeg -stream_loop 10 -re -i "page18-movie-4.3gp" -f h261 "udp://localhost:9999"

# ffplay
ffplay udp://127.0.0.1:9999 -loglevel debug
