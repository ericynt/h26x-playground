# Steps
- load JPEGs from directory
- apply H.261 compression algorithm
    - preprocessing
      - adjust resolution to 352 x 288, if needed
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
  - put H.261 compressed data bytes into H.261 packets
  - put H.261 packets into RTP packets
  - serve RTP packets in UDP datagrams

# Notes
- keep constant bitrate according to H.261 standard
- serve the data in a loop
- log network debug information

# Future
- Look into RTCP and RTSP
- Look into MPEG-TS
- H.264 support