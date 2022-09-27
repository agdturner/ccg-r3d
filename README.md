# [ccg-r3d](https://github.com/agdturner/ccg-r3d)
A Java library for rendering 3D spatial data. This is built on top of [ccg-v3d]((https://github.com/agdturner/ccg-v3d))

Hello World!

Some success at rendering the Utah Teapot:

<img alt="A yellow rendering of the Utah Teapot" src="data/HelloWorldUtahTeapot1.png" />

There are some black pixels (which is probably to do with intersection issues rather than rendering issues).

Running [RenderImage.java](src/main/java/uk/ac/leeds/ccg/r3d/RenderImage.java) produced this image for me in a few minutes on a basic desktop machine.

There are about 9000 triangles considered in the input data: [Utah_teapot_(solid).stl](data/Utah_teapot_(solid).stl).
