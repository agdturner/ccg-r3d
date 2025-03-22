# [ccg-r3d](https://github.com/agdturner/ccg-r3d)
A Java library for rendering 3D spatial data. This is built on top of [ccg-v3d](https://github.com/agdturner/ccg-v3d) to provide the ability to reproducibly render scenes with [6 degrees of freedom](https://en.wikipedia.org/wiki/Six_degrees_of_freedom). The 6 degrees of freedom are realised through translation of camera focal points and rectangular screens. The centre point of each screen relative to the focal point gives the pitch and yaw and the orientation of the rectangle gives the roll.

The library has been used to render some scenes using a single colour for shade that is modified by a vector lighting condition that uses the normal direction of each triangle to define the shade and the colour for a triangle. An ambient lighting condition is applied so that triangles facing directly away from the lighting vector are not completely black.

Some other experiments with shade have been done, but more realistic texture rendering and rendering with more complex lighting conditions has not yet been attempted. Realistic scene rendering is not yet a focus of development, the focus is more on geometry, structure, physics and on visualising dynamically by moving cameras and the geometry being visualised. 

Support for rendering raster data using [ccg-grids](https://github.com/agdturner/ccg-grids) may also be added if the difference between raster and vector models become a research focus.

## Uses
* Rendering 3D Geometry.
* Helping explain and develop [ccg-v3d](https://github.com/agdturner/ccg-v3d) - the underlying 3D Euclidean geometry library.
* Supporting the development and integration of Environmental Digital Twins. The UK National Oceanography Centre have published [An Information Management Framework for Environmental Digital Twins (IMFe) Report](https://noc.ac.uk/files/documents/about/NOC%20IMFe%20Summary%20Report2.pdf) that explains what an Environmental Digital Twin is - basically it is a model of a region that provides feedback to the sensing and surveying of that region in order to more accurately and usefully represent it.

## Hello World!
Rendering of [Utah Teapot](https://en.wikipedia.org/wiki/Utah_teapot) using [Utah_teapot_(solid).stl](data/Utah_teapot_(solid).stl) with 9438 triangles (resolution 500x375, [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) of precision -10):

<img alt="A yellow scale rendering of the Utah Teapot" src="data/output/Utah_teapot_(solid)/oom=-10/lighting(i=0.2673_j=0.5345_k=0.8018)/Utah_teapot_(solid)_500x500_pt(i=-12.3089_j=13.0269_k=17.2189)_lighting(i=0.2673_j=0.5345_k=0.8018)_oom=-10.png" />

This is a classic ["Hello World!"](https://en.wikipedia.org/wiki/%22Hello,_World!%22_program) for computer graphics. For this rendering, there is no shadow, there is effectively a low ambient light and a vector for a general light.  

Rendering of the asteroid [Geographos](https://en.wikipedia.org/wiki/1620_Geographos) using a [Geographos 3D Model provided by NASA](https://nasa3d.arc.nasa.gov/detail/geographos) with 16380 triangles (resolution 500x375, [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) of precision -8):

<img alt="A yellow scale rendering of Geographos" src="data/output/geographos/files/oom=-8/lighting(i=-0.2673_j=-0.5345_k=-0.8018)/1620geographos_500x500_pt(i=-3.3194_j=3.4588_k=-3.4339)_lighting(i=-0.2673_j=-0.5345_k=-0.8018)_oom=-8.png" />

Both renderings reveal triangular data artefacts and are unrealistic due to the lack of shadow, yet the 3D nature of the objects is apparent.

The images of the Geographos and the Utah Teapot were produced by running [RenderImage.java](https://github.com/agdturner/ccg-r3d/tree/main/src/main/java/uk/ac/leeds/ccg/r3d/RenderImage.java). With some small edits to the source code it should be possible to reproduce them. The rendering of larger images takes longer. Also with greater precision settings the rendering takes longer.

For faster rendering, the implementation has been extended to alternatively use [IEEE double precision floating point format](https://en.wikipedia.org/wiki/Double-precision_floating-point_format) numbers. This precision is often good enough. The image below is a rendering of [Hurricane Katrina](https://en.wikipedia.org/wiki/Hurricane_Katrina) using a [Hurricane_Katrina 3D Model provided by NASA](https://nasa3d.arc.nasa.gov/detail/hurricane-katrina). Instead of specifying an [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) precision and a [RoundingMode](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/math/RoundingMode.java), for floating point 3D calculations it is necessary to set a small number used to determine if two values are significantly different (epsilon). For the rendering of Hurricane Katrina below the epsilon used is 1.0E-7.

<img alt="A yellow scale rendering of Hurricane Katrina" src="data/output/Hurricane_Katrina/files/epsilon=1.0E-7/lighting(i=-0.27_j=-0.53_k=-0.80)_ambientLight(0.05)/Katrina_1000x1000.png" />

The Hurricane Katrina image was produced by running [RenderImageDouble.java](https://github.com/agdturner/ccg-r3d/tree/main/src/main/java/uk/ac/leeds/ccg/r3d/d/RenderImageDouble.java). With some small edits to the source code it should be possible to reproduce it.

Based on the simple complete [3D Model of Curiosity from NASA](https://nasa3d.arc.nasa.gov/detail/mars-rover-curiosity) below is another yellow scale image generated from 384942 Triangles in about a minute.

<img alt="A yellow scale rendering of a Curiosity model" src="data/output/Curiosity Rover 3D Printed Model/files/epsilon=1.0E-7/lighting(i=-0.27_j=-0.53_k=-0.80)_ambientLight(0.05)/Curiosity SM (Complete Print 200uM)_1000x1000pt(i=123.83_j=108.92_k=143.96)_lighting(i=-0.27_j=-0.53_k=-0.80)_ambientLight(0.05)_epsilon=1.0E-7.png" />

Based on the [3D Model of the Apollo 17 Landing Site from NASA](https://nasa3d.arc.nasa.gov/detail/Apollo17-Landing)) below is a grey shade image generated from  533506 Triangles in about 3.5 minutes.

<img alt="A grey scale rendering of the Apollo 17 Landing Site" src="data/output/Apollo_17\files\epsilon=1.0E-7\lighting(i=-0.27_j=-0.53_k=-0.80)_ambientLight(0.05)/Apollo_17_1000x1000pt(i=208.79_j=208.79_k=215.65)_lighting(i=-0.27_j=-0.53_k=-0.80)_ambientLight(0.05)_epsilon=1.0E-7.png" />

More reproducible results will be generated in due course...

## Links
* [Various Printable 3D Models from NASA](https://nasa3d.arc.nasa.gov/models/printable) in [STL File Format](https://en.wikipedia.org/wiki/STL_(file_format)).
* [Wikipedia List_of_common_3D_test_models](https://en.wikipedia.org/wiki/List_of_common_3D_test_models).
