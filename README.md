# Vectrize <a href='https://play.google.com/store/apps/details?id=com.solunaire.vectrize&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1' style="margin-bottom: 0;"><img alt='Get it on Google Play' height='80px'  src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>
Vectrize is an easy way to make any photo stand out. Choose from a wide variety of options like triangles or ellipses, select how much detail you want, and watch as your image is transformed into a beautiful collage of shapes. Then save the image and share it with your friends! Vectrize is a mobile version of the Primitive library developed by @fogleman to generate SVG images from raster ones. This Android App was developed by @rahulk64 and @jordanbuchman.

# What It Does
This app reproduces images with geometric primitives. This includes triangles, rectangles, ellipses, circles, rotated rectangles, beziers, rotated ellipses, and general polygons. 

# How It Works
A target image to reproduce is provided as an input. The algorithm in the library written by @fogleman 
(see here: https://github.com/fogleman/primitive) tries to find the most optimal shape (or shapes) that can be added to the image
to minimize the error between the input image and the drawn image. This is repeated based on the number of iterations that the user chooses. The more iterations that the algorithm runs, the closer the "vectrized" image gets to the original raster image.

# How to Use It
We created an easy way to access the library written by @fogleman through this Android app. Similar to his Mac Application,
we provide a GUI with easy and intuitive actions to customize your drawn images, which can be saved as PNG images.

# How to Download
This app is currently supported on Android and is published to the Play Store, where you can find it here: https://play.google.com/store/apps/details?id=com.solunaire.vectrize. Thanks for supporting our app!
