# [ccg-r3d](https://github.com/agdturner/ccg-r3d)
A Java library for rendering 3D spatial data. This is built on top of [ccg-v3d]((https://github.com/agdturner/ccg-v3d)) and uses [ccg-grids]((https://github.com/agdturner/ccg-grids))

## Hello World!
Rendering of [Utah Teapot](https://en.wikipedia.org/wiki/Utah_teapot) using [Utah_teapot_(solid).stl](data/Utah_teapot_(solid).stl) with 9438 triangles (resolution 500x375, [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) of precision -8):

<img alt="A yellow rendering of the Utah Teapot" src="data/output/Utah_teapot_(solid)/oom=-10/lighting(i=0.2673_j=0.5345_k=0.8018)/Utah_teapot_(solid)_500x500_pt(i=-12.3089_j=13.0269_k=17.2189)_lighting(i=0.2673_j=0.5345_k=0.8018)_oom=-10.png" />

This is a classic ["Hello World!"](https://en.wikipedia.org/wiki/%22Hello,_World!%22_program) for computer graphics. For this rendering, there is no shadow, there is effectively a low ambient light and a vector for a general light.  

Rendering of [Geographos](https://en.wikipedia.org/wiki/1620_Geographos) using a [Geographos 3D Model provided by NASA](https://nasa3d.arc.nasa.gov/detail/geographos) with 16380 triangles (resolution 500x375, [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) of precision -8):

<img alt="A yellow rendering of Geographos" src="data/output/geographos/files/oom=-8/lighting(i=-0.2673_j=-0.5345_k=-0.8018)/1620geographos_500x500_pt(i=-3.3194_j=3.4588_k=-3.4339)_lighting(i=-0.2673_j=-0.5345_k=-0.8018)_oom=-8.png" />

For both renderings it is possible to pick out triangular features and data artefacts. They are sort of unrealistic, but the 3D nature of the objects is apparent.

The images of the Geographos and the Utah Teapot were produced by running [RenderImage.java](https://github.com/agdturner/ccg-r3d/tree/main/src/main/java/uk/ac/leeds/ccg/r3d/RenderImage.java). With some small edits to the source code it should be possible to reproduce them. The rendering of larger images takes longer. Also with greater precision settings the rendering takes longer. It is my intension to showcase more reproducible results in due course. These images for the time being serve to show that progress is being made...

## Uses
* Help explain and develop [ccg-v3d](https://github.com/agdturner/ccg-v3d) - the underlying 3D Euclidean geometry library.
* Generate basic rendering of 3D Objects.
* Support the development and integration of Environmental Digital Twins. The UK National Oceanography Centre [An Information Management Framework for Environmental Digital Twins (IMFe) Report](https://noc.ac.uk/files/documents/about/NOC%20IMFe%20Summary%20Report2.pdf) explains what an Environmental Digital Twin is.

## Links
* [Various Printable 3D Models from NASA](https://nasa3d.arc.nasa.gov/models/printable) in [STL File Format](https://en.wikipedia.org/wiki/STL_(file_format)).
* [Wikipedia List_of_common_3D_test_models](https://en.wikipedia.org/wiki/List_of_common_3D_test_models).
