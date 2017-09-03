# Primitive-Android
A mobile version of the Primitive library developed by @fogleman to generate SVG images from raster ones. Developed by @rahulk64 and @jordanbuchman

# What It Does
This app reproduces images with geometric primitives, saved in vector format.

# How It Works
A target image to reproduce is provided as an input. The algorithm in the library written by @fogleman 
(see here: https://github.com/fogleman/primitive) tries to find the most optimal shape that can be added to the image
to minimize the error between the input image and the drawn image.

# How to Use It
We created an easy way to access the library written by @fogleman through this Android app. Similar to his Mac Application,
we provide a GUI with easy and intuitive actions to customize your drawn images, which can be saved as vector (SVG) images.
