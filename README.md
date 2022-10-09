# [ccg-r3d](https://github.com/agdturner/ccg-r3d)
A Java library for rendering 3D spatial data. This is built on top of [ccg-v3d]((https://github.com/agdturner/ccg-v3d)) and uses [ccg-grids]((https://github.com/agdturner/ccg-grids))

Success at rendering the [Utah Teapot](https://en.wikipedia.org/wiki/Utah_teapot):

<img alt="A yellow rendering of the Utah Teapot" src="data/Utah_teapot_(solid)_500x375_-8.png" />

Running [RenderImage.java](https://github.com/agdturner/ccg-r3d/tree/main/src/main/java/uk/ac/leeds/ccg/r3d/RenderImage.java) produced this image in a few minutes on a basic desktop machine.

This is the classic ["Hello World!"](https://en.wikipedia.org/wiki/%22Hello,_World!%22_program) for computer graphics. ([Wikipedia List_of_common_3D_test_models](https://en.wikipedia.org/wiki/List_of_common_3D_test_models))

There are about 9000 triangles considered in the input data: [Utah_teapot_(solid).stl](data/Utah_teapot_(solid).stl).

Here is a rendering of [Geographos](https://en.wikipedia.org/wiki/1620_Geographos) using a [Geographos 3D Model provided by NASA]():

<img alt="A yellow rendering of Geographos" src="data/geographos/1620geographos_500x375_-8.png" />

The plan is to use this library to help explain [ccg-v3d](https://github.com/agdturner/ccg-v3d) - the underlying 3D Euclidean geometry library, and also to generate basic rendering of objects. The longer term goal is to support the development and integration of Environmental Digital Twins. The UK National Oceanography Centre [An Information Management Framework for Environmental Digital Twins (IMFe) Report](https://noc.ac.uk/files/documents/about/NOC%20IMFe%20Summary%20Report2.pdf) explains what an Environmental Digital Twin is.
